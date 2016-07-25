package com.treexor.auth.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Random;

import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;
import org.springframework.http.HttpStatus;

import com.treexor.auth.AbstractTest;
import com.treexor.auth.entities.Profile;
import com.treexor.auth.exceptions.RegistrationException;
import com.treexor.auth.repositories.ProfileRepository;
import com.treexor.common.versioning.Versioner;

public class AuthServiceTest extends AbstractTest {

    @Mock
    private Versioner mockVersioner;
    @Mock
    private ProfileRepository mockRepository;
    @Mock
    private RedisAtomicLong mockAccountsCounter;

    @InjectMocks
    private AuthServiceImpl service;

    private static final String VALID_NAME = "name";
    private static final String VALID_PASSWORD = "ABCD1234";
    private static final String VALID_EMAIL = "valid@email.com";

    @Override
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Override
    public void tearDown() {
        // TODO Auto-generated method stub
    }

    /**
     * REGISTER
     */
    @Test
    public void given_An_Email_That_Already_Exists_Then_Throw_RegistrationException_With_Conflict_Http_Status() {
        when(mockRepository.setEmailIfNotExists(anyString())).thenReturn(false);

        try {
            service.register(VALID_NAME, VALID_EMAIL, VALID_PASSWORD);
            fail("Should have thrown an exception since email address was not available");
        } catch (RegistrationException e) {
            assertTrue("Failure - we were expecting the HTTP status to be 409", HttpStatus.CONFLICT.value() == e.getHttpStatus().value());
        }
        verify(mockRepository, times(1)).setEmailIfNotExists(anyString());
    }

    @Test
    public void given_That_Password_Encryption_Fails_Then_Throw_RegistrationException_With_Internal_Server_Exception_Http_Status() {
        when(mockRepository.setEmailIfNotExists(anyString())).thenReturn(true);
        when(mockAccountsCounter.incrementAndGet()).thenReturn(Math.abs(new Random().nextLong()));
        when(mockVersioner.getLastVersion(Profile.class)).thenReturn(Math.abs(new Random().nextInt(100)));

        try {
            service.register(VALID_NAME, VALID_EMAIL, null);
            fail("Should have thrown an exception since password was null / could not be created");
        } catch (RegistrationException e) {
            assertTrue("Failure - we were expecting the HTTP status to be 500", HttpStatus.INTERNAL_SERVER_ERROR.value() == e.getHttpStatus().value());
        }

        verify(mockRepository, times(1)).setEmailIfNotExists(anyString());
        verify(mockAccountsCounter, times(1)).incrementAndGet();
        verify(mockVersioner, times(1)).getLastVersion(Profile.class);
    }

    @Test
    public void given_Valid_Parameters_Then_Return_A_New_Profile() throws Exception {
        long accountId = Math.abs(new Random().nextLong());
        int lastVersion = Math.abs(new Random().nextInt(100));

        when(mockRepository.setEmailIfNotExists(anyString())).thenReturn(true);
        when(mockAccountsCounter.incrementAndGet()).thenReturn(accountId);
        when(mockVersioner.getLastVersion(Profile.class)).thenReturn(lastVersion);

        Profile profile = service.register(VALID_NAME, VALID_EMAIL, VALID_PASSWORD);
        assertNotNull("Failure - we were expecting a profile object to be returned", profile);
        assertEquals("Failure - we were expecting a the profile id to be " + accountId, accountId, profile.getId());
        assertEquals("Failure - we were expecting a the profile version to be " + lastVersion, lastVersion, profile.getVersion());

        verify(mockRepository, times(1)).setEmailIfNotExists(anyString());
        verify(mockRepository, times(1)).save(any(Profile.class));
        verify(mockAccountsCounter, times(1)).incrementAndGet();
        verify(mockVersioner, times(1)).getLastVersion(Profile.class);
    }
}

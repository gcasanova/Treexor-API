package com.treexor.auth.consumers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import com.treexor.auth.AbstractTest;
import com.treexor.auth.entities.Profile;
import com.treexor.auth.exceptions.LoginException;
import com.treexor.auth.exceptions.RegistrationException;
import com.treexor.auth.services.AuthService;

public class RabbitConsumerTest extends AbstractTest {

    @Mock
    private AuthService mockService;

    @InjectMocks
    private RabbitConsumer consumer;

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
     * VALIDATOR METHODS
     */
    @Test
    public void given_Invalid_Email_To_Validator_Then_Return_False() {
        String[] emails = { "plainaddress", "#@%^%#$@#$@#.com", "@example.com", "Joe Smith <email@example.com>", "email.example.com", "email@example@example.com", "あいうえお@example.com", "email@example.com (Joe Smith)", "email@example",
                "email@111.222.333.44444" };

        for (String email : emails) {
            assertFalse("Failure - we were expecting the email verifier to NOT accept this email: " + email, consumer.isValidEmail(email));
        }
    }

    @Test
    public void given_Valid_Email_To_Validator_Then_Return_True() {
        String[] emails = { "email@example.com", "firstname.lastname@example.com", "email@subdomain.example.com", "firstname+lastname@example.com", "1234567890@example.com", "email@example-one.com", "_______@example.com", "email@example.name",
                "email@example.museum" };

        for (String email : emails) {
            assertTrue("Failure - we were expecting the email verifier to accept this email: " + email, consumer.isValidEmail(email));
        }
    }

    @Test
    public void given_Invalid_Password_To_Validator_Then_Return_False() {
        String[] passwords = { "ABCDEFGH", "12345678", "ABCD123", "1234567%", "423-4234" };

        for (String password : passwords) {
            assertFalse("Failure - we were expecting the password verifier to NOT accept this password: " + password, consumer.isValidPassword(password));
        }
    }

    @Test
    public void given_Valid_Password_To_Validator_Then_Return_True() {
        String[] passwords = { "1234567A", "AAAbAaA1", "1A1A1A1A", "abcdefg2" };

        for (String password : passwords) {
            assertTrue("Failure - we were expecting the email password to accept this password: " + password, consumer.isValidPassword(password));
        }
    }

    /**
     * LOGIN
     */
    @Test
    public void given_Missing_Parameters_When_Loging_In_Then_Return_Bad_Request_Http_Status() {
        JSONObject jResponse = consumer.handleLogin(new JSONObject());
        assertNull("Failure - we expected no body", jResponse.optJSONObject("body"));
        assertTrue("Failure - we were expecting HTTP status 400", HttpStatus.BAD_REQUEST.value() == jResponse.optDouble("status"));

        jResponse = consumer.handleLogin(new JSONObject().put("email", "fdsafsf@tjasf.com"));
        assertNull("Failure - we expected no body", jResponse.optJSONObject("body"));
        assertTrue("Failure - we were expecting HTTP status 400", HttpStatus.BAD_REQUEST.value() == jResponse.optDouble("status"));

        jResponse = consumer.handleLogin(new JSONObject().put("password", "abcasdfsafjso"));
        assertNull("Failure - we expected no body", jResponse.optJSONObject("body"));
        assertTrue("Failure - we were expecting HTTP status 400", HttpStatus.BAD_REQUEST.value() == jResponse.optDouble("status"));
    }

    @Test
    public void given_A_LoginException_When_Loging_In_Then_Returns_Http_Status_From_Exception() throws Exception {
        HttpStatus status = HttpStatus.CONFLICT;
        when(mockService.login(anyString(), anyString())).thenThrow(new LoginException(status));

        JSONObject jResponse = consumer.handleLogin(new JSONObject().put("email", VALID_EMAIL).put("password", VALID_PASSWORD));
        assertNull("Failure - we expected no body", jResponse.optJSONObject("body"));
        assertTrue("Failure - we were expecting HTTP status: " + status.value(), status.value() == jResponse.optDouble("status"));

        verify(mockService, times(1)).login(anyString(), anyString());
    }

    @Test
    public void given_A_JSONException_When_Loging_In_Then_Returns_Internal_Server_Error_Http_Status() throws Exception {
        when(mockService.login(anyString(), anyString())).thenThrow(new JSONException(""));

        JSONObject jResponse = consumer.handleLogin(new JSONObject().put("email", VALID_EMAIL).put("password", VALID_PASSWORD));
        assertNull("Failure - we expected no body", jResponse.optJSONObject("body"));
        assertTrue("Failure - we were expecting HTTP status 500", HttpStatus.INTERNAL_SERVER_ERROR.value() == jResponse.optDouble("status"));

        verify(mockService, times(1)).login(anyString(), anyString());
    }

    @Test
    public void given_Valid_Credentials_When_Loging_In_Then_Returns_A_Body_And_Ok_Http_Status() throws Exception {
        when(mockService.login(anyString(), anyString())).thenReturn(new JSONObject());

        JSONObject jResponse = consumer.handleLogin(new JSONObject().put("email", VALID_EMAIL).put("password", VALID_PASSWORD));
        assertNotNull("Failure - we expected a body", jResponse.optJSONObject("body"));
        assertTrue("Failure - we were expecting HTTP status 200", HttpStatus.OK.value() == jResponse.optDouble("status"));

        verify(mockService, times(1)).login(anyString(), anyString());
    }

    /**
     * LOGOUT
     */
    @Test
    public void given_Invalid_User_Id_When_Loging_Out_Then_Service_Is_Not_Called() throws Exception {
        consumer.handleLogout(new JSONObject().put("id", 0));
        verify(mockService, times(0)).login(anyString(), anyString());
    }

    /**
     * REGISTER
     */
    @Test
    public void given_Missing_Parameters_When_Registering_Then_Return_Bad_Request_Http_Status() {
        JSONObject jResponse = consumer.handleRegister(new JSONObject());
        assertNull("Failure - we expected no body", jResponse.optJSONObject("body"));
        assertTrue("Failure - we were expecting HTTP status 400", HttpStatus.BAD_REQUEST.value() == jResponse.optDouble("status"));

        jResponse = consumer.handleRegister(new JSONObject().put("name", "somename").put("email", "fdsafsf@tjasf.com"));
        assertNull("Failure - we expected no body", jResponse.optJSONObject("body"));
        assertTrue("Failure - we were expecting HTTP status 400", HttpStatus.BAD_REQUEST.value() == jResponse.optDouble("status"));

        jResponse = consumer.handleRegister(new JSONObject().put("password", "abcasdfsafjso"));
        assertNull("Failure - we expected no body", jResponse.optJSONObject("body"));
        assertTrue("Failure - we were expecting HTTP status 400", HttpStatus.BAD_REQUEST.value() == jResponse.optDouble("status"));
    }

    @Test
    public void given_A_RegistrationException_When_Registering_Then_Returns_Http_Status_From_Exception() throws Exception {
        HttpStatus status = HttpStatus.CONFLICT;
        when(mockService.register(anyString(), anyString(), anyString())).thenThrow(new RegistrationException(status));

        JSONObject jResponse = consumer.handleRegister(new JSONObject().put("name", VALID_NAME).put("email", VALID_EMAIL).put("password", VALID_PASSWORD));
        assertNull("Failure - we expected no body", jResponse.optJSONObject("body"));
        assertTrue("Failure - we were expecting HTTP status: " + status.value(), status.value() == jResponse.optDouble("status"));

        verify(mockService, times(1)).register(anyString(), anyString(), anyString());
    }

    @Test
    public void given_Valid_Credentials_When_Registering_Then_Returns_A_Body_And_Created_Http_Status() throws Exception {
        long id = 1;
        Profile profile = new Profile();
        profile.setId(id);

        when(mockService.register(anyString(), anyString(), anyString())).thenReturn(profile);

        JSONObject jResponse = consumer.handleRegister(new JSONObject().put("name", VALID_NAME).put("email", VALID_EMAIL).put("password", VALID_PASSWORD));
        assertTrue("Failure - we were expecting HTTP status 201", HttpStatus.CREATED.value() == jResponse.optDouble("status"));

        JSONObject body = jResponse.optJSONObject("body");
        assertNotNull("Failure - we expected a body", body);
        assertEquals("Failure - we expected id to be " + id, id, body.optLong("id"));

        verify(mockService, times(1)).register(anyString(), anyString(), anyString());
    }
}

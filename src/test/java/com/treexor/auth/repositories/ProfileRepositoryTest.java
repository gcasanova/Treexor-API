package com.treexor.auth.repositories;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Random;

import org.json.JSON;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Strings;
import com.treexor.auth.AbstractTest;
import com.treexor.auth.entities.Profile;

import redis.clients.jedis.Jedis;
import redis.embedded.RedisServer;

public class ProfileRepositoryTest extends AbstractTest {

    @Autowired
    private ProfileRepository repository;

    private Jedis jedis;
    private static RedisServer redisServer;

    public ProfileRepositoryTest() {
        jedis = new Jedis();
    }

    @BeforeClass
    public static void init() throws IOException {
        redisServer = new RedisServer(6379);
        redisServer.start();
    }

    @AfterClass
    public static void destroy() {
        redisServer.stop();
    }

    @Override
    public void setUp() {
        // TODO Auto-generated method stub
    }

    @Override
    public void tearDown() {
        jedis.flushAll();
    }

    @Test
    public void given_A_Profile_Then_Save_It() throws Exception {
        Profile profile = createProfile();

        String sProfile = jedis.get(repository.getProfileKey(profile.getId()));
        String emailRerefence = jedis.get(repository.getProfileEmailKey(profile.getEmail()));
        assertTrue("Failure - we were expecting the profile to not be saved yet", Strings.isNullOrEmpty(sProfile));
        assertTrue("Failure - we were expecting the profile email reference to not be saved yet", Strings.isNullOrEmpty(emailRerefence));

        repository.save(profile);

        sProfile = jedis.get(repository.getProfileKey(profile.getId()));
        emailRerefence = jedis.get(repository.getProfileEmailKey(profile.getEmail()));
        assertTrue("Failure - we were expecting the profile to be saved!", !Strings.isNullOrEmpty(sProfile));
        assertTrue("Failure - we were expecting the profile email reference to be saved!", !Strings.isNullOrEmpty(emailRerefence));
        assertEquals("Failure - we were expecting the profile email reference to be the profile id", String.valueOf(profile.getId()), emailRerefence);

        Profile savedProfile = JSON.populateObject(new JSONObject(sProfile), Profile.class);
        assertEquals("Failure - we were expecting another profile id", profile.getId(), savedProfile.getId());
        assertEquals("Failure - we were expecting another profile name", profile.getName(), savedProfile.getName());
        assertEquals("Failure - we were expecting another profile email", profile.getEmail(), savedProfile.getEmail());
        assertEquals("Failure - we were expecting another profile password", profile.getPassword(), savedProfile.getPassword());
        assertEquals("Failure - we were expecting another profile version", profile.getVersion(), savedProfile.getVersion());
        assertEquals("Failure - we were expecting another profile createdAt", profile.getCreatedAt(), savedProfile.getCreatedAt());
        assertEquals("Failure - we were expecting another profile updatedAt", profile.getUpdatedAt(), savedProfile.getUpdatedAt());
    }

    @Test
    public void given_A_Profile_Then_Update_It() throws Exception {
        String name = "another_name";

        Profile profile = createProfile();
        repository.save(profile);

        profile.setName(name);
        repository.update(profile);

        String sProfile = jedis.get(repository.getProfileKey(profile.getId()));
        Profile savedProfile = JSON.populateObject(new JSONObject(sProfile), Profile.class);

        assertEquals("Failure - we were expecting another profile name", name, savedProfile.getName());
    }

    @Test
    public void given_A_Profile_Then_Delete_It() throws Exception {
        Profile profile = createProfile();
        repository.save(profile);

        repository.delete(profile.getId());
        String sProfile = jedis.get(repository.getProfileKey(profile.getId()));
        String emailRerefence = jedis.get(repository.getProfileEmailKey(profile.getEmail()));

        assertTrue("Failure - we were expecting the profile to be gone!", Strings.isNullOrEmpty(sProfile));
        assertTrue("Failure - we were expecting the profile email reference to be gone!", Strings.isNullOrEmpty(emailRerefence));
    }

    @Test
    public void given_An_Email_Then_Save_It_If_Not_In_Use_Yet() throws Exception {
        String email = "random@email.com";
        String email2 = "random2@email.com";

        assertTrue("Failure - we were expecting the email to be saved and true to be returned", repository.setEmailIfNotExists(email));
        assertFalse("Failure - we were expecting the email to not be saved and false to be returned", repository.setEmailIfNotExists(email));
        assertTrue("Failure - we were expecting the email to be saved and true to be returned", repository.setEmailIfNotExists(email2));
    }

    @Test
    public void given_An_Email_Then_Return_The_User_Id() throws Exception {
        Profile profile = createProfile();
        repository.save(profile);

        Long id = repository.getIdByEmail(profile.getEmail());
        assertNotNull("Failure - we were not expecting the id to be null", id);
        assertEquals("Failure - we were expecting the identifiers to be the same", profile.getId(), id.longValue());
    }

    @Test
    public void given_A_Token_Then_Save_It() throws Exception {
        long id = 342343423;
        String token = "dsafsafsfAAAAfjsao";

        repository.updateToken(id, token);

        String savedToken = jedis.get(repository.getProfileTokenKey(id));
        assertEquals("Failure - we were expecting the tokens to be equal", token, savedToken);
    }

    @Test
    public void given_A_Token_Then_Expire_It() throws Exception {
        long id = 342343423;
        String token = "dsafsafsfAAAAfjsao";

        repository.updateToken(id, token);
        repository.expireToken(id);

        String savedToken = jedis.get(repository.getProfileTokenKey(id));
        assertTrue("Failure - we were expecting the token to not be available anymore", Strings.isNullOrEmpty(savedToken));
    }

    private Profile createProfile() {
        Long id = Math.abs(new Random().nextLong());
        String name = "name";
        String email = "email@gmail.com";
        String password = "ABCD1234";
        int version = Math.abs(new Random().nextInt(100));
        return new Profile(id, name, email, password, version);
    }
}

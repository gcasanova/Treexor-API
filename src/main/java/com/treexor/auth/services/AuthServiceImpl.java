package com.treexor.auth.services;

import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.treexor.auth.entities.Profile;
import com.treexor.auth.exceptions.LoginException;
import com.treexor.auth.exceptions.RegistrationException;
import com.treexor.auth.repositories.ProfileRepository;
import com.treexor.auth.utils.EncryptionMD5;
import com.treexor.common.versioning.Versioner;

@Service
public class AuthServiceImpl implements AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private Versioner versioner;
    private RedisAtomicLong accountsCounter;
    private ProfileRepository profileRepository;

    @Autowired
    public AuthServiceImpl(Versioner versioner, RedisAtomicLong accountsCounter, ProfileRepository profileRepository) {
        this.versioner = versioner;
        this.accountsCounter = accountsCounter;
        this.profileRepository = profileRepository;
    }

    @Override
    public Profile register(String name, String email, String password) throws RegistrationException {

        // check no one is using this email
        boolean available = profileRepository.setEmailIfNotExists(email);
        if (!available)
            throw new RegistrationException(HttpStatus.CONFLICT);

        // get account id
        long accountId = accountsCounter.incrementAndGet();

        Profile profile = new Profile(accountId, name, email, password, versioner.getLastVersion(Profile.class));

        // check if password encryption failed
        if (profile.getPassword() == null)
            throw new RegistrationException(HttpStatus.INTERNAL_SERVER_ERROR);

        // save profile
        profileRepository.save(profile);
        log.debug("Created profile with id %s", accountId);
        return profile;
    }

    @Override
    public JSONObject login(String email, String password) throws LoginException, JSONException {

        // check if profile with that email exists
        Long accountId = profileRepository.getIdByEmail(email);
        if (accountId == null)
            throw new LoginException(HttpStatus.NOT_FOUND);

        // retrieve profile
        Profile profile = profileRepository.find(accountId);

        // verify password
        String hashedPass = EncryptionMD5.encrypt(password, profile.getId());
        if (!hashedPass.equals(profile.getPassword()))
            throw new LoginException(HttpStatus.FORBIDDEN);

        // create token (UUID usage might be replaced in real world)
        String token = UUID.randomUUID().toString().replaceAll("-", "");

        // save token for this session
        profileRepository.updateToken(accountId, token);

        JSONObject jResponse = new JSONObject();
        jResponse.put("token", token);

        // do not expose password (even if hashed)
        JSONObject json = new JSONObject(profile);
        json.remove("password");
        jResponse.put("profile", json);
        return jResponse;
    }

    @Override
    public void logout(long id) {
        profileRepository.expireToken(id);
    }
}

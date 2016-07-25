package com.treexor.auth.repositories;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import com.treexor.auth.entities.Profile;
import com.treexor.auth.versioning.AuthVersioner;
import com.treexor.common.locker.LockManager;
import com.treexor.common.versioning.redis.RedisVersionableEntityRepository;

@Repository
public class ProfileRepository extends RedisVersionableEntityRepository<Profile> {
    private static final Logger log = LoggerFactory.getLogger(ProfileRepository.class);
    
    @Value("${token.expiration.minutes}")
    private int tokenExpirationMinutes;

    public static final String PROFILE_PREFIX = "profile#";

    private StringRedisTemplate template;
    private RedisTemplate<String, Profile> templateProfile;

    @Autowired
    public ProfileRepository(@Qualifier("redisProfileTemplate") RedisTemplate<String, Profile> templateProfile, StringRedisTemplate template, AuthVersioner versioner, LockManager lock) {
        super(lock, versioner, Profile.class);

        this.template = template;
        this.templateProfile = templateProfile;
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void delete(long id) {
        Profile profile = templateProfile.opsForValue().get(getProfileKey(id));
        template.execute(new SessionCallback<List<Object>>() {
            public List<Object> execute(RedisOperations operations) throws DataAccessException {
                operations.multi();
                operations.delete(getProfileKey(id));
                operations.delete(getProfileEmailKey(profile.getEmail()));
                return operations.exec();
            }
        });
    }

    @Override
    protected JSONObject findJSON(long id) throws JSONException {
        return new JSONObject(template.opsForValue().get(getProfileKey(id)));
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void save(Profile entity) {
        template.execute(new SessionCallback<List<Object>>() {
            public List<Object> execute(RedisOperations operations) throws DataAccessException {
                operations.multi();
                operations.opsForValue().set(getProfileKey(entity.getId()), new JSONObject(entity).toString());
                operations.opsForValue().set(getProfileEmailKey(entity.getEmail()), String.valueOf(entity.getId()));
                return operations.exec();
            }
        });
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void update(Profile entity) {
        // update is the same than save for now, we might want to add custom
        // functionality in the future
        template.execute(new SessionCallback<List<Object>>() {
            public List<Object> execute(RedisOperations operations) throws DataAccessException {
                operations.multi();
                operations.opsForValue().set(getProfileKey(entity.getId()), new JSONObject(entity).toString());
                operations.opsForValue().set(getProfileEmailKey(entity.getEmail()), String.valueOf(entity.getId()));
                return operations.exec();
            }
        });
    }

    public boolean setEmailIfNotExists(String email) {
        return template.opsForValue().setIfAbsent(getProfileEmailKey(email), "TEMP EMAIL");
    }

    public Long getIdByEmail(String email) {
        String id = template.opsForValue().get(getProfileEmailKey(email));
        if (id != null) {
            try {
                return Long.valueOf(id);
            } catch (NumberFormatException e) {
                log.error("Id corresponding to profile with email %s could not be parsed to long", email);
            }
        }
        return null;
    }

    public void updateToken(long id, String token) {
        template.opsForValue().set(getProfileTokenKey(id), token, tokenExpirationMinutes, TimeUnit.MINUTES);
    }

    public void expireToken(long id) {
        template.delete(getProfileTokenKey(id));
    }

    protected String getProfileKey(long id) {
        return PROFILE_PREFIX + id;
    }

    protected String getProfileEmailKey(String email) {
        return PROFILE_PREFIX + email;
    }

    protected String getProfileTokenKey(long id) {
        return PROFILE_PREFIX + "token#" + id;
    }
}

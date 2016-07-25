package com.treexor.common.versioning.redis;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import org.json.JSON;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.treexor.common.locker.LockManager;
import com.treexor.common.versioning.Versionable;
import com.treexor.common.versioning.Versioner;

public abstract class RedisVersionableEntityRepository<T extends Versionable> {
    private static final Logger log = LoggerFactory.getLogger(RedisVersionableEntityRepository.class);

    private static final Duration MAX_WAITING = Duration.of(100, ChronoUnit.MILLIS);

    final Class<T> subclassType;

    protected LockManager lock;
    protected Versioner versioner;

    public RedisVersionableEntityRepository(LockManager lock, Versioner versioner, Class<T> subclassType) {
        this.lock = lock;
        this.versioner = versioner;
        this.subclassType = subclassType;
    }

    public final T find(long id) throws JSONException {
        JSONObject json = findJSON(id);
        int oldVersion = json.optInt("version");
        if (getVersioner().update(json, getSubclassType())) {
            // resource was upgraded, attempt to persist new version
            T entity = JSON.populateObject(json, getSubclassType());
            persistNewVersion(entity, id, oldVersion);
        }
        return JSON.populateObject(json, getSubclassType());
    }

    public Versioner getVersioner() {
        return versioner;
    }

    public Class<T> getSubclassType() {
        return subclassType;
    }

    public LockManager getLock() {
        return lock;
    }

    protected abstract JSONObject findJSON(long id) throws JSONException;

    public abstract void delete(long id);

    public abstract void save(T entity);

    public abstract void update(T entity);

    private void persistNewVersion(T entity, long id, int oldVersion) {
        // attempt to lock resource before persisting
        String lockAccepterContactsKey = getLockResourceKey(id, entity.getClass());
        if (getLock().obtainLock(lockAccepterContactsKey, MAX_WAITING)) {
            try {
                // lock obtained, lets double check the current version of the
                // resource before proceeding
                JSONObject json = findJSON(id);
                if (oldVersion == json.optInt("version")) {
                    update(entity);
                }
            } catch (Exception e) {
                log.error("Unexpected error upgrading resource: %s", e.getMessage());
            } finally {
                getLock().releaseLock(lockAccepterContactsKey);
            }
        } else {
            log.warn("We couldn't obtain lock for resource of class %s", entity.getClass().getName());
        }
    }

    private String getLockResourceKey(Long id, Class<?> clazz) {
        return new StringBuilder().append(LockManager.LOCKING_PREFIX).append(clazz.getName()).append(id).toString();
    }
}

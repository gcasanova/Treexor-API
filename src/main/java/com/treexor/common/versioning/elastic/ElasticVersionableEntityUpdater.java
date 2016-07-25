package com.treexor.common.versioning.elastic;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.json.JSON;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.treexor.common.locker.LockManager;
import com.treexor.common.versioning.Versionable;
import com.treexor.common.versioning.Versioner;

public abstract class ElasticVersionableEntityUpdater<T extends Versionable> {
    private static final Logger log = LoggerFactory.getLogger(ElasticVersionableEntityUpdater.class);

    private static final Duration MAX_WAITING = Duration.of(100, ChronoUnit.MILLIS);

    final Class<T> subclassType;

    protected LockManager lock;
    protected Versioner versioner;

    public ElasticVersionableEntityUpdater(LockManager lock, Versioner versioner, Class<T> subclassType) {
        this.lock = lock;
        this.versioner = versioner;
        this.subclassType = subclassType;
    }

    protected final List<T> upgrade(List<T> entities) {
        JSONObject json = null;
        for (T entity : entities) {
            int oldVersion = entity.getVersion();
            try {
                json = new JSONObject(entity);
                long id = json.optLong("id");
                if (id > 0 && getVersioner().update(json, getSubclassType())) {
                    // resource was upgraded, attempt to persist new version
                    entity = JSON.populateObject(json, getSubclassType());
                    persistNewVersion(entity, id, oldVersion);
                }
            } catch (JSONException e) {
                log.error("Error parsing object to JSONObject: %s", e.getMessage());
            }
        }
        return entities;
    }

    protected final T upgrade(T entity) {
        int oldVersion = entity.getVersion();
        try {
            JSONObject json = new JSONObject(entity);
            long id = json.optLong("id");
            if (id > 0 && getVersioner().update(json, getSubclassType())) {
                // resource was upgraded, attempt to persist new version
                entity = JSON.populateObject(json, getSubclassType());
                persistNewVersion(entity, id, oldVersion);
            }
        } catch (JSONException e) {
            log.error("Error parsing object to JSONObject: %s", e.getMessage());
        }
        return entity;
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

    public abstract void update(T entity);

    public abstract T findOne(long id);

    private void persistNewVersion(T entity, long id, int oldVersion) {
        // attempt to lock resource before persisting
        String lockAccepterContactsKey = getLockResourceKey(id, entity.getClass());
        if (getLock().obtainLock(lockAccepterContactsKey, MAX_WAITING)) {
            try {
                // lock obtained, lets double check the current version of the
                // resource before proceeding
                T currentEntity = findOne(id);
                if (oldVersion == currentEntity.getVersion()) {
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

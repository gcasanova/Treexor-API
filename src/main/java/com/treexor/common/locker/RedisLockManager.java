package com.treexor.common.locker;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisLockManager implements LockManager {
    private static final Logger log = LoggerFactory.getLogger(RedisLockManager.class);

    private static final long MAX_ATTEMPTS = 4;
    private static final long LOCK_TIME = Duration.of(5, ChronoUnit.MINUTES).toMillis();

    private RedisTemplate<String, String> template;

    @Autowired
    public RedisLockManager(RedisTemplate<String, String> template) {
        this.template = template;
    }

    @Override
    public boolean obtainLock(final String lockKey) {
        return obtainLock(lockKey, Duration.of(0, ChronoUnit.SECONDS));
    }

    @Override
    public boolean obtainLock(String lockKey, Duration maxWaiting) {
        boolean wait = maxWaiting.toMillis() > 0;
        final long end = Instant.now().toEpochMilli() + maxWaiting.toMillis();
        try {
            boolean result = false;
            do {
                result = template.opsForValue().setIfAbsent(lockKey, "");
                if (result) {
                    template.expire(lockKey, LOCK_TIME, TimeUnit.MILLISECONDS);
                    return true;
                }
                try {
                    Thread.sleep(maxWaiting.toMillis() / MAX_ATTEMPTS);
                } catch (InterruptedException ignore) {
                }
            } while (!result && wait && end > Instant.now().toEpochMilli());

            return result;
        } catch (Exception e) {
            log.error("Problem obtaining lock - {} - {}", lockKey, e.getMessage());
            return false;
        }
    }

    public void releaseLock(String key) {
        this.template.delete(key);
    }
}

package com.treexor.common.locker;

import java.time.Duration;

public interface LockManager {

    String LOCKING_PREFIX = "lock#";

    boolean obtainLock(final String key);

    boolean obtainLock(final String key, Duration maxWaiting);

    void releaseLock(String key);
}

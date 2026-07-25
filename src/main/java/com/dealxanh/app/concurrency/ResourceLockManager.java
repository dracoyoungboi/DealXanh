package com.dealxanh.app.concurrency;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * In-memory FIFO lock manager for entity-level concurrency control.
 *
 * When two threads request a lock for the same resource key,
 * they are queued in strict FIFO (first-arrived, first-served) order
 * thanks to fair-mode ReentrantLock.
 *
 * This replaces the need for Redis or external message queues.
 * Limitation: only works within a single JVM (acceptable for this monolith).
 */
@Component
public class ResourceLockManager {

    private static final Logger log = LoggerFactory.getLogger(ResourceLockManager.class);

    // fair=true ensures threads acquire locks in FIFO order
    private final ConcurrentHashMap<String, ReentrantLock> locks = new ConcurrentHashMap<>();

    /**
     * Acquire and return a fair ReentrantLock for the given entity type and ID.
     * The caller MUST use try { lock.lock(); ... } finally { lock.unlock(); }
     *
     * @param entityType e.g. "ORDER", "PRODUCT", "DEAL", "STORE"
     * @param entityId   the entity's primary key
     * @return a fair ReentrantLock unique to this resource, already locked
     */
    public ReentrantLock acquireLock(String entityType, Long entityId) {
        String key = entityType + ":" + entityId;
        ReentrantLock lock = locks.computeIfAbsent(key, k -> {
            log.debug("Created new fair lock for: {}", key);
            return new ReentrantLock(true); // true = fair ordering = FIFO
        });
        lock.lock();
        log.debug("Acquired lock for: {} (queue length: {})", key, lock.getQueueLength());
        return lock;
    }

    /**
     * Get the queue length for a resource (for monitoring/debugging).
     */
    public int getQueueLength(String entityType, Long entityId) {
        String key = entityType + ":" + entityId;
        ReentrantLock lock = locks.get(key);
        return lock == null ? 0 : lock.getQueueLength();
    }

    /**
     * Clean up locks that are no longer contended.
     * Runs every 5 minutes to prevent memory leaks from accumulating lock objects.
     */
    @Scheduled(fixedRate = 300_000)
    public void cleanupUnusedLocks() {
        int before = locks.size();
        locks.entrySet().removeIf(entry -> {
            ReentrantLock lock = entry.getValue();
            // Remove if not currently locked and no threads waiting
            return !lock.isLocked() && !lock.hasQueuedThreads();
        });
        int removed = before - locks.size();
        if (removed > 0) {
            log.debug("Cleaned up {} unused locks ({} remaining)", removed, locks.size());
        }
    }
}

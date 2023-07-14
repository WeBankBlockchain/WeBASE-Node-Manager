package com.webank.webase.node.mgr.lock;

/**
 * @author mawla
 * @describe
 * @date 2022/8/22 10:16 上午
 */
public interface WeLock {

    /**
     * Acquire lock by lockKey
     *
     * @param lockKey - lockKey of object
     * @throws Exception
     */
    boolean getLock(String lockKey) throws Exception;

    /**
     * Acquire lock by lockKey Set timeout in lockTimeOut milliseconds
     *
     * @param lockKey - lockKey of object
     * @throws Exception
     */
    boolean getLock(String lockKey, long lockTimeOut) throws Exception;

    /**
     * Release the lock according to the key
     * @param lockKey
     * @throws Exception
     */
    void unlock(String lockKey) throws Exception;
}

package com.webank.webase.node.mgr.lock.service;

/**
 * @author mawla
 * @describe
 * @date 2022/8/22 10:16 上午
 */
public interface WeLock {

    /**
     * Returns Lock instance by name.
     * <p>
     * Implements a <b>non-fair</b> locking so doesn't guarantees an acquire order by threads.
     * <p>
     * To increase reliability during failover, all operations wait for propagation to all Redis slaves.
     *
     * @param name - name of object
     * @return Lock object
     */
    boolean getLock(String name) throws Exception;
}

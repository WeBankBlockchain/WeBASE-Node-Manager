package com.webank.webase.node.mgr.lock.service;

import com.webank.webase.node.mgr.lock.LockMapper;
import com.webank.webase.node.mgr.lock.entity.TbLock;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

/**
 * @author mawla
 * @describe
 * @date 2022/8/22 2:10 下午
 */
@AllArgsConstructor
@Log4j2
@Service
public class LockService {
    private final LockMapper lockMapper;

    /**
     * add
     *
     * @param lockEntity
     * @return
     */
    public int add(TbLock lockEntity) {
        log.debug("add lock req:{}", lockEntity);
        int add = lockMapper.add(lockEntity);
        log.debug("add lock res:{}", add);
        return add;
    }

    /**
     * delete
     *
     * @param lockKey
     * @return
     */
    public int delete(String lockKey) {
        log.debug("delete lock key :{}", lockKey);
        int delete = lockMapper.delete(lockKey);
        log.debug("delete lock key res:{}", delete);
        return delete;
    }

    /**
     * update
     *
     * @param lockEntity
     * @return
     */
    public int update(TbLock lockEntity) {
        log.debug("update lock req:{}", lockEntity);
        int update = lockMapper.update(lockEntity);
        log.debug("update lock res:{}", update);
        return update;
    }

    /**
     * get
     *
     * @param lockKey
     * @return
     */
    public TbLock getLock(String lockKey) {
        log.debug("get lock key :{}", lockKey);
        TbLock lock = lockMapper.getLock(lockKey);
        log.debug("get lock key res:{}", lock);
        return lock;
    }
}

package com.webank.webase.node.mgr.lock;

import com.webank.webase.node.mgr.lock.entity.TbLock;
import com.webank.webase.node.mgr.lock.service.LockService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author mawla
 * @describe
 * @date 2022/8/22 10:17 上午
 */
@Component
@Log4j2
public class DbWeLock implements WeLock {
    /**
     * 将requestid保存在该变量中
     */
    private final static ThreadLocal<String> requestIdTL = new ThreadLocal<>();
    /**
     * 获取锁的超时时间，这个时间内获取不到将重试
     */
    private final static int GET_LOCK_TIME_OUT = 3000;
    @Autowired
    private LockService lockService;


    /**
     * 获取锁
     *
     * @param lockKey 锁key
     * @return
     **/
    @Override
    public boolean getLock(String lockKey) throws Exception {
        return getLock(lockKey, GET_LOCK_TIME_OUT);
    }

    /**
     * 获取锁
     *
     * @param lockKey         锁key
     * @param lockTimeOut(毫秒) 持有锁的有效时间，防止死锁
     * @return
     **/
    @Override
    public boolean getLock(String lockKey, long lockTimeOut) throws Exception {
        boolean lockResult = false;
        String requestId = getRequestId();
        long startTime = System.currentTimeMillis();
        while (true) {
            TbLock lockEntity = lockService.getLock(lockKey);
            if (Objects.isNull(lockEntity)) {
                //插入一条记录,重新尝试获取锁
                String reqId = this.getRequestId();
                int add = lockService.add(TbLock.builder().lockKey(lockKey).threadId(reqId).lockCount(1)
                        .timeout(System.currentTimeMillis() + lockTimeOut).version(0).build());
                if (add == 1) {
                    lockResult = true;
                    break;
                }
            } else {
                String reqId = lockEntity.getThreadId();
                //如果reqid为空字符，表示锁未被占用
                if ("".equals(reqId)) {
                    lockEntity.setThreadId(requestId);
                    lockEntity.setLockCount(1);
                    lockEntity.setTimeout(System.currentTimeMillis() + lockTimeOut);
                    if (lockService.update(lockEntity) == 1) {
                        lockResult = true;
                        break;
                    }
                } else if (requestId.equals(reqId)) {
                    //如果request_id和表中request_id一样表示锁被当前线程持有者，此时需要加重入锁
                    lockEntity.setTimeout(System.currentTimeMillis() + lockTimeOut);
                    lockEntity.setLockCount(lockEntity.getLockCount() + 1);
                    if (lockService.update(lockEntity) == 1) {
                        lockResult = true;
                        break;
                    }
                } else {
                    //锁不是自己的，并且已经超时了，则重置锁，继续重试
                    if (lockEntity.getTimeout() < System.currentTimeMillis()) {
                        this.resetLock(lockEntity);
                    } else {
                        //如果未超时，休眠100毫秒，继续重试
                        if (startTime + GET_LOCK_TIME_OUT > System.currentTimeMillis()) {
                            TimeUnit.MILLISECONDS.sleep(10000);
                        } else {
                            break;
                        }
                    }
                }
            }
        }
        return lockResult;
    }

    /**
     * 释放锁
     *
     * @param lockKey
     * @throws Exception
     **/
    @Override
    public void unlock(String lockKey) throws Exception {
        //获取当前线程requestId
        String requestId = this.getRequestId();
        TbLock lockEntity = lockService.getLock(lockKey);
        //当前线程requestId和库中request_id一致 && lock_count>0，表示可以释放锁
        if (Objects.nonNull(lockEntity) && requestId.equals(lockEntity.getThreadId()) && lockEntity.getLockCount() > 0) {
            if (lockEntity.getLockCount() == 1) {
                //重置锁
                resetLock(lockEntity);
            } else {
                lockEntity.setLockCount(lockEntity.getLockCount() - 1);
                lockService.update(lockEntity);
            }
        }
    }

    /**
     * 获取当前线程requestid
     *
     * @return
     **/
    private String getRequestId() {
        String requestId = requestIdTL.get();
        if (StringUtils.isBlank(requestId)) {
            requestId = UUID.randomUUID().toString();
            requestIdTL.set(requestId);
        }
        log.debug("requestId:{}", requestId);
        return requestId;
    }

    /**
     * 重置锁
     *
     * @param lockEntity
     * @return
     * @throws Exception
     **/
    private int resetLock(TbLock lockEntity) {
        lockEntity.setThreadId("");
        lockEntity.setLockCount(0);
        lockEntity.setTimeout(0L);
        return lockService.update(lockEntity);
    }


}

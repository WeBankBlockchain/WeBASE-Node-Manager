package com.webank.webase.node.mgr.lock;

import com.webank.webase.node.mgr.config.properties.ConstantProperties;
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
     * Save the thread ID in this variable
     */
    private final static ThreadLocal<String> threadIdTL = new ThreadLocal<>();
    @Autowired
    private LockService lockService;
    @Autowired
    private ConstantProperties constantProperties;

    /**
     * get Lock
     *
     * @param lockKey lock key
     * @return
     **/
    @Override
    public boolean getLock(String lockKey) throws Exception {
        return getLock(lockKey, constantProperties.getLockTimeOut());
    }

    /**
     * 获取锁
     *
     * @param lockKey         lock key
     * @param lockTimeOut(毫秒) Effective time of holding lock to prevent deadlock
     * @return
     **/
    @Override
    public boolean getLock(String lockKey, long lockTimeOut) throws Exception {
        boolean lockResult = false;
        String requestId =this.getThreadId();
        long startTime = System.currentTimeMillis();
        while (true) {
            TbLock lockEntity = lockService.getLock(lockKey);
            if (Objects.isNull(lockEntity)) {
                String reqId = this.getThreadId();
                int add = lockService.add(TbLock.builder().lockKey(lockKey).threadId(reqId).lockCount(1)
                        .timeout(System.currentTimeMillis() + lockTimeOut).version(0).build());
                if (add == 1) {
                    lockResult = true;
                    break;
                }
            } else {
                String threadId = lockEntity.getThreadId();
                //If ThreadID is empty, it indicates that the lock is not occupied
                if ("".equals(threadId)) {
                    lockEntity.setThreadId(requestId);
                    lockEntity.setLockCount(1);
                    lockEntity.setTimeout(System.currentTimeMillis() + lockTimeOut);
                    if (lockService.update(lockEntity) == 1) {
                        lockResult = true;
                        break;
                    }
                } else if (requestId.equals(threadId)) {
                    //If request_ ID and request in the table_ The same as ID indicates that the
                    // lock is held by the current thread, and the lock needs to be added
                    lockEntity.setTimeout(System.currentTimeMillis() + lockTimeOut);
                    lockEntity.setLockCount(lockEntity.getLockCount() + 1);
                    if (lockService.update(lockEntity) == 1) {
                        lockResult = true;
                        break;
                    }
                } else {
                    //If the lock is not own and has timed out, reset the lock and continue to try again
                    if (lockEntity.getTimeout() < System.currentTimeMillis()) {
                        this.resetLock(lockEntity);
                    } else {
                        //If it does not time out, sleep for 100 milliseconds and continue to try again
                        if (startTime + constantProperties.getGetLockTimeOut() > System.currentTimeMillis()) {
                            TimeUnit.MILLISECONDS.sleep(100);
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
     * unlock
     *
     * @param lockKey
     * @throws Exception
     **/
    @Override
    public void unlock(String lockKey) throws Exception {
        //获取当前线程requestId
        String requestId = this.getThreadId();
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
     * Get the current thread thread id
     *
     * @return
     **/
    private String getThreadId() {
        String threadId = threadIdTL.get();
        if (StringUtils.isBlank(threadId)) {
            threadId = UUID.randomUUID().toString();
            threadIdTL.set(threadId);
        }
        return threadId;
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

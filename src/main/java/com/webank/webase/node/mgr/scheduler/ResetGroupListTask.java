/**
 * Copyright 2014-2021  the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.webank.webase.node.mgr.scheduler;


import com.webank.webase.node.mgr.config.properties.ConstantProperties;
import com.webank.webase.node.mgr.group.GroupService;
import com.webank.webase.node.mgr.lock.DbWeLock;
import com.webank.webase.node.mgr.lock.WeLock;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.LongAdder;

/**
 * refresh group list
 */
@Log4j2
@Component
public class ResetGroupListTask {

    @Autowired
    private GroupService groupService;
    @Autowired
    private ConstantProperties constants;
    // interval of check node status
    private static LongAdder LAST_TIME_CHECK_GROUP = new LongAdder();
    @Autowired private DbWeLock weLock;
    private final static String RESET_GROUP_LIST_TASK_LOCK_KEY = "lock:reset_group_list_task";

    @Scheduled(fixedDelayString = "${constant.resetGroupListCycle}")
    public void taskStart() {
        try {
            boolean lock = weLock.getLock(RESET_GROUP_LIST_TASK_LOCK_KEY);
            if (lock) {
                resetGroupList();
            }
        } catch (Exception e) {
            log.error("获取锁失败{}", e);
        } finally {
            if (weLock != null) {
                try {
                    weLock.unlock(RESET_GROUP_LIST_TASK_LOCK_KEY);
                } catch (Exception e) {
                    log.error("释放锁失败{}", e);
                }
            }
        }
    }

    /**
     * async reset groupList.
     * v1.4.3: add internal
     */
    @Async(value = "mgrAsyncExecutor")
    public void asyncResetGroupList() {
        long now = System.currentTimeMillis();
        long gap = now - LAST_TIME_CHECK_GROUP.longValue();
        if (gap > constants.getResetGroupListInterval()) {
            log.info("resetGroupList start gap:{}.", gap);
            LAST_TIME_CHECK_GROUP.reset();
            LAST_TIME_CHECK_GROUP.add(now);
            resetGroupList();
        }
        return;
    }

    /**
     * reset groupList.
     */
    public void resetGroupList() {
        groupService.resetGroupList();
    }
}
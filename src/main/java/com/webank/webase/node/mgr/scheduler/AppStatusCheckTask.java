/**
 * Copyright 2014-2021 the original author or authors.
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


import com.webank.webase.node.mgr.appintegration.AppIntegrationService;
import com.webank.webase.node.mgr.lock.service.WeLock;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * app status check.
 */
@Log4j2
@Component
public class AppStatusCheckTask {

    @Autowired
    private AppIntegrationService appIntegrationService;
    @Autowired
    private WeLock weLock;
    private final static String APP_STATUS_CHECK_TASK_LOCK_KEY = "lock:app_status_check_task";

    @Scheduled(fixedDelayString = "${constant.appStatusCheckCycle}")
    public void taskStart() {
        try {
            boolean lock = weLock.getLock(APP_STATUS_CHECK_TASK_LOCK_KEY);
            if (lock){
                appStatusCheck();
            }
        } catch (Exception e) {
            log.error("获取锁失败{}", e);
        } finally {
            if (weLock != null) {
                try {
                    weLock.unlock(APP_STATUS_CHECK_TASK_LOCK_KEY);
                } catch (Exception e) {
                    log.error("释放锁失败{}", e);
                }
            }
        }
    }

    /**
     * appStatusCheck.
     */
    public void appStatusCheck() {
        log.debug("appStatusCheck start.");
        appIntegrationService.appStatusCheck();
    }
}

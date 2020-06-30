/**
 * Copyright 2014-2020  the original author or authors.
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


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.webank.webase.node.mgr.group.GroupService;

import lombok.extern.log4j.Log4j2;

/**
 * refresh group list
 */
@Log4j2
@Component
public class ResetGroupListTask {

    @Autowired
    private GroupService groupService;

//    @Scheduled(fixedDelayString = "${constant.resetGroupListCycle}")
//    public void taskStart() {
//        resetGroupList();
//    }

    /**
     * async reset groupList.
     */
    @Async(value = "mgrAsyncExecutor")
    public void asyncResetGroupList() {
        resetGroupList();
    }

    /**
     * reset groupList.
     */
    public void resetGroupList() {
        groupService.resetGroupList();
    }
}
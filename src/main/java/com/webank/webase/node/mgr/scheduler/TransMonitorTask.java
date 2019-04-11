/**
 * Copyright 2014-2019  the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webank.webase.node.mgr.scheduler;

import com.webank.webase.node.mgr.group.GroupService;
import com.webank.webase.node.mgr.group.TbGroup;
import com.webank.webase.node.mgr.monitor.MonitorService;
import com.webank.webase.node.mgr.transaction.entity.TbTransHash;
import com.webank.webase.node.mgr.transaction.TransHashService;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class TransMonitorTask {

    @Autowired
    MonitorService monitorService;
    @Autowired
    private GroupService groupService;
    @Autowired
    private TransHashService transHashService;


    /**
     * start mointor
     */
    public void monitorStart(){
        Instant startTime = Instant.now();
        log.info("start monitor. startTime:{}", startTime.toEpochMilli());
        //get group list
        List<TbGroup> groupList = groupService.getAllGroup();
        if (groupList == null || groupList.size() == 0) {
            log.info("monitor jump over .not found any group");
            return;
        }

        //delete block by groupId
        groupList.stream().forEach(group -> monitorHandle(group.getGroupId()));
        log.info("end monitor. useTime:{} ",
            Duration.between(startTime, Instant.now()).toMillis());
    }


    /**
     * monitor every group.
     */
    private void monitorHandle(int groupId) {
        List<TbTransHash> transHashList = transHashService.qureyUnStatTransHashList(groupId);
        if (transHashList != null && transHashList.size() > 0) {
            monitorService.insertTransMonitorInfo(groupId,transHashList);
        }
    }
}

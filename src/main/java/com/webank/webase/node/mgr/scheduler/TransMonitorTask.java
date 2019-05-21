/*
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

import com.webank.webase.node.mgr.monitor.MonitorService;
import com.webank.webase.node.mgr.network.NetworkService;
import com.webank.webase.node.mgr.network.TbNetwork;
import com.webank.webase.node.mgr.transhash.TbTransHash;
import com.webank.webase.node.mgr.transhash.TransHashService;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class TransMonitorTask {

    @Autowired
    MonitorService monitorService;
    @Autowired
    private NetworkService networkService;
    @Autowired
    private TransHashService transHashService;
    private static final int UNUSUAL_MAX_COUNT = 20;

    /**
     * init monitorInfoHandle.
     */
    public void monitorInfoHandle() {
        List<TbNetwork> networkList = networkService.getAllNetwork();
        if (networkList == null || networkList.isEmpty()) {
            log.error("fail monitorInfoHandle. networkList is empty");
            return;
        }
        List<Integer> idList = networkList.stream().map(n -> n.getNetworkId())
            .filter(id -> isNetworkContinueMonitor(id)).collect(
                Collectors.toList());
        if (idList == null || idList.isEmpty()) {
            log.error("fail monitorInfoHandle. networkIdList is empty");
            return;
        }
        List<TbTransHash> transHashList = transHashService.qureyUnStatTransHashList(idList);
        if (transHashList != null && transHashList.size() > 0) {
            monitorService.insertTransMonitorInfo(transHashList);
        }
    }


    /**
     * check networkId.
     */
    private boolean isNetworkContinueMonitor(int networkId) {
        int unusualUserCount = monitorService.countOfUnusualUser(networkId, null);
        int unusualContractCount = monitorService.countOfUnusualContract(networkId, null);
        if (unusualUserCount >= UNUSUAL_MAX_COUNT || unusualUserCount >= UNUSUAL_MAX_COUNT) {
            log.error(
                "monitorHandle jump over. networkId:{} unusualUserCount:{} unusualUserCount:{} UNUSUAL_MAX_COUNT:{}",
                networkId, unusualUserCount, unusualContractCount, UNUSUAL_MAX_COUNT);
            return false;
        }
        return true;
    }
}



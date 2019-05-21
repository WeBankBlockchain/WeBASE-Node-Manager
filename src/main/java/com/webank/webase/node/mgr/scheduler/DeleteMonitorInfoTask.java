/**
 * Copyright 2014-2019  the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.webank.webase.node.mgr.scheduler;

import com.webank.webase.node.mgr.base.properties.ConstantProperties;
import com.webank.webase.node.mgr.monitor.MonitorService;
import com.webank.webase.node.mgr.network.NetworkService;
import com.webank.webase.node.mgr.network.TbNetwork;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * delete block information periodically.
 */
@Log4j2
@Component
public class DeleteMonitorInfoTask {

    @Autowired
    private NetworkService networkService;
    @Autowired
    private MonitorService monitorService;
    @Autowired
    private ConstantProperties constant;

    /**
     * start delete monitor info.
     */
    public void deleteMonitorInfo() {
        List<TbNetwork> networkList = networkService.getAllNetwork();
        if (networkList == null || networkList.isEmpty()) {
            log.error("fail deleteMonitorInfo. networkList is empty");
            return;
        }
        networkList.stream().forEach(n -> deleteByNetworkId(n.getNetworkId()));
    }

    /**
     * delete monitor info by networkId.
     */
    private void deleteByNetworkId(int networkId) {
        try {
            int effectedRow = monitorService
                .deleteAndRetainMax(networkId, constant.getMonitorInfoRetainMax());
            log.info("end deleteByNetworkId. effectedRow:{}", effectedRow);
        } catch (Exception ex) {
            log.error("fail deleteByNetworkId", ex);
        }
    }

}

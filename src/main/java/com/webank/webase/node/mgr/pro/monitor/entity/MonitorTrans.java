/**
 * Copyright 2014-2021  the original author or authors.
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
package com.webank.webase.node.mgr.pro.monitor.entity;

import java.util.List;
import lombok.Data;

@Data
public class MonitorTrans {

    private Integer groupId;
    private String userName;
    private String interfaceName;
    private Integer totalCount;
    private List<PageTransInfo> transInfoList;

    public MonitorTrans() {
        super();
    }

    /**
     * init by groupId、userName、interfaceName、totalCount、transInfoList.
     */
    public MonitorTrans(Integer groupId, String userName, String interfaceName,
        Integer totalCount,
        List<PageTransInfo> transInfoList) {
        super();
        this.groupId = groupId;
        this.userName = userName;
        this.interfaceName = interfaceName;
        this.totalCount = totalCount;
        this.transInfoList = transInfoList;
    }
}

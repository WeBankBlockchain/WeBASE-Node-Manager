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
package com.webank.webase.node.mgr.monitor;

import com.webank.webase.node.mgr.base.enums.MonitorUserType;
import com.webank.webase.node.mgr.base.enums.TransType;
import com.webank.webase.node.mgr.base.enums.TransUnusualType;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class TbMonitor {

    private String userName;
    private Integer userType = MonitorUserType.NORMAL.getValue();
    private String contractName;
    private String contractAddress;
    private String interfaceName;
    private Integer transType = TransType.DEPLOY.getValue();
    private Integer transUnusualType = TransUnusualType.NORMAL.getValue();
    private Integer transCount;
    private String transHashs;
    private String transHashLastest;
    private LocalDateTime createTime;
    private LocalDateTime modifyTime;

}
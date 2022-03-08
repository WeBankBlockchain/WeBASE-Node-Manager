/*
 * Copyright 2014-2020 the original author or authors.
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
package com.webank.webase.node.mgr.precntauth.precompiled.sysconf;

import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.front.frontinterface.FrontRestTools;
import com.webank.webase.node.mgr.precntauth.precompiled.sysconf.entity.ReqSetSysConfigInfo;
import com.webank.webase.node.mgr.tools.HttpRequestTools;
import com.webank.webase.node.mgr.tools.JsonTools;
import com.webank.webase.node.mgr.user.UserService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *  Sys config service;
 */
@Slf4j
@Service
public class SysConfigServiceInWebase {

    @Autowired
    private FrontRestTools frontRestTools;
    @Autowired
    private UserService userService;

    /**
     * get system config list
     */
    public Object querySysConfigByGroupId(String groupId) {
        log.debug("start getSysConfigListService. groupId:{}", groupId);
        Map<String, String> map = new HashMap<>();
        map.put("groupId", groupId);
        String uri = HttpRequestTools.getQueryUri(
            FrontRestTools.RPC_PRECOM_SYS_CONFIG_LIST, map);
        Object frontRsp = frontRestTools.getForEntity(groupId, uri, List.class);
        return frontRsp;
    }

    /**
     * post set system config
     */
    public Object setSysConfigByKeyService(ReqSetSysConfigInfo reqSetSysConfigInfo) {
        log.debug("start reqSetSysConfigInfo. reqSetSysConfigInfo:{}",
            JsonTools.toJSONString(reqSetSysConfigInfo));
        if (Objects.isNull(reqSetSysConfigInfo)) {
            log.error("fail setSysConfigByKeyService. request param is null");
            throw new NodeMgrException(ConstantCode.INVALID_PARAM_INFO);
        }
        String groupId = reqSetSysConfigInfo.getGroupId();
        String signUserId = userService.getSignUserIdByAddress(groupId,
            reqSetSysConfigInfo.getFromAddress());
        reqSetSysConfigInfo.setSignUserId(signUserId);
        Object frontRsp = frontRestTools.postForEntity(
            groupId, FrontRestTools.RPC_PRECOM_SYS_CONFIG,
            reqSetSysConfigInfo, Object.class);
        log.debug("end setSysConfigByKeyService. frontRsp:{}", JsonTools.toJSONString(frontRsp));
        return frontRsp;
    }

}

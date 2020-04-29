/**
 * Copyright 2014-2019 the original author or authors.
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
package com.webank.webase.node.mgr.precompiled.sysconf;

import com.alibaba.fastjson.JSON;
import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.base.tools.HttpRequestTools;
import com.webank.webase.node.mgr.frontinterface.FrontInterfaceService;
import com.webank.webase.node.mgr.frontinterface.FrontRestTools;
import com.webank.webase.node.mgr.user.UserService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * System config service
 */
@Log4j2
@Service
public class PrecompiledSysConfigService {
    @Autowired
    private FrontRestTools frontRestTools;
    @Autowired
    private FrontInterfaceService frontInterfaceService;
    @Autowired
    private UserService userService;

    /**
     * get system config list
     */
    public Object getSysConfigListService(int groupId, int pageSize, int pageNumber) {
        log.debug("start getSysConfigListService. groupId:{}", groupId);
        Map<String, String> map = new HashMap<>();
        map.put("groupId", String.valueOf(groupId));
        map.put("pageSize", String.valueOf(pageSize));
        map.put("pageNumber", String.valueOf(pageNumber));

        String uri = HttpRequestTools.getQueryUri(FrontRestTools.URI_SYS_CONFIG_LIST, map);

        Object frontRsp = frontRestTools.getForEntity(groupId, uri, Object.class);
        log.debug("end getSysConfigListService. frontRsp:{}", JSON.toJSONString(frontRsp));
        return frontRsp;
    }


    /**
     * post set system config
     */

    public Object setSysConfigByKeyService(SysConfigParam sysConfigParam) {
        log.debug("start setSysConfigByKeyService. sysConfigParam:{}", JSON.toJSONString(sysConfigParam));
        if (Objects.isNull(sysConfigParam)) {
            log.error("fail setSysConfigByKeyService. request param is null");
            throw new NodeMgrException(ConstantCode.INVALID_PARAM_INFO);
        }
        int groupId = sysConfigParam.getGroupId();
        String signUserId = userService.getSignUserIdByAddress(groupId, sysConfigParam.getFromAddress());
        sysConfigParam.setSignUserId(signUserId);
        Object frontRsp = frontRestTools.postForEntity(
                groupId, FrontRestTools.URI_SYS_CONFIG,
                sysConfigParam, Object.class);
        log.debug("end setSysConfigByKeyService. frontRsp:{}", JSON.toJSONString(frontRsp));
        return frontRsp;
    }

}

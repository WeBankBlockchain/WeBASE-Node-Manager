/**
 * Copyright 2014-2020 the original author or authors.
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

package com.webank.webase.node.mgr.precompiled.permission;

import com.webank.webase.node.mgr.base.enums.RequestType;
import com.webank.webase.node.mgr.base.tools.HttpRequestTools;
import com.webank.webase.node.mgr.base.tools.JsonTools;
import com.webank.webase.node.mgr.frontinterface.FrontRestTools;
import com.webank.webase.node.mgr.precompiled.entity.ChainGovernanceHandle;
import com.webank.webase.node.mgr.precompiled.entity.AddressStatusHandle;
import com.webank.webase.node.mgr.user.UserService;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * permission manage after FISCO-BCOS v2.5.0
 */
@Log4j2
@Service
public class ChainGovernService {
    @Autowired
    private FrontRestTools frontRestTools;
    @Autowired
    private UserService userService;

    public List listCommittee(Integer groupId) {
        log.debug("start listCommittee. groupId:{}" , groupId);
        Map<String, String> map = new HashMap<>();
        map.put("groupId", String.valueOf(groupId));
        String uri = HttpRequestTools.getQueryUri(FrontRestTools.URI_GOVERNANCE_COMMITTEE_LIST, map);
        List frontRsp = frontRestTools.getForEntity(groupId, uri, List.class);
        log.debug("end listCommittee. frontRsp:{}", JsonTools.toJSONString(frontRsp));
        return frontRsp;
    }

    /**
     *  POST => grant, DELETE => revoke
     */
    public Object handleCommittee(ChainGovernanceHandle governanceHandle, RequestType requestType) {
        log.debug("start handleCommittee. governanceHandle:{},requestType:{}",
            JsonTools.toJSONString(governanceHandle), requestType);
        int groupId = governanceHandle.getGroupId();
        String signUserId = userService.getSignUserIdByAddress(groupId, governanceHandle.getFromAddress());
        governanceHandle.setSignUserId(signUserId);
        Object frontRsp = null;
        if (requestType == RequestType.POST) {
            frontRsp = frontRestTools.postForEntity(
                groupId, FrontRestTools.URI_GOVERNANCE_COMMITTEE,
                governanceHandle, Object.class);
        } else if (requestType == RequestType.DELETE) {
            frontRsp = frontRestTools.deleteForEntity(
                groupId, FrontRestTools.URI_GOVERNANCE_COMMITTEE,
                governanceHandle, Object.class);
        }
        log.debug("end handleCommittee. frontRsp:{}", frontRsp);
        return frontRsp;
    }

    public Object getCommitteeWeight(Integer groupId, String address) {
        log.debug("start getCommitteeWeight. groupId:{},address:{}", groupId, address);
        Map<String, String> map = new HashMap<>();
        map.put("groupId", String.valueOf(groupId));
        map.put("address", address);
        String uri = HttpRequestTools.getQueryUri(FrontRestTools.URI_GOVERNANCE_COMMITTEE_WEIGHT, map);
        Object frontRsp = frontRestTools.getForEntity(groupId, uri, Object.class);
        log.debug("end getCommitteeWeight. frontRsp:{}", JsonTools.toJSONString(frontRsp));
        return frontRsp;
    }

    public Object updateCommitteeWeight(ChainGovernanceHandle governanceHandle) {
        log.debug("start updateCommitteeWeight.  governanceHandle:{}", governanceHandle);
        int groupId = governanceHandle.getGroupId();
        String signUserId = userService.getSignUserIdByAddress(groupId, governanceHandle.getFromAddress());
        governanceHandle.setSignUserId(signUserId);
        Object frontRsp = frontRestTools.postForEntity(groupId, FrontRestTools.URI_GOVERNANCE_COMMITTEE_WEIGHT,
            governanceHandle, Object.class);
        log.debug("end updateCommitteeWeight. frontRsp:{}", JsonTools.toJSONString(frontRsp));
        return frontRsp;
    }

    public BigInteger getThreshold(Integer groupId) {
        log.debug("start getThreshold. groupId:{}" , groupId);
        Map<String, String> map = new HashMap<>();
        map.put("groupId", String.valueOf(groupId));
        String uri = HttpRequestTools.getQueryUri(FrontRestTools.URI_GOVERNANCE_THRESHOLD, map);
        BigInteger frontRsp = frontRestTools.getForEntity(groupId, uri, BigInteger.class);
        log.debug("end getThreshold. frontRsp:{}", JsonTools.toJSONString(frontRsp));
        return frontRsp;
    }

    public Object updateThreshold(ChainGovernanceHandle governanceHandle) {
        log.debug("start updateThreshold.  governanceHandle:{}", governanceHandle);
        int groupId = governanceHandle.getGroupId();
        String signUserId = userService.getSignUserIdByAddress(groupId, governanceHandle.getFromAddress());
        governanceHandle.setSignUserId(signUserId);
        Object frontRsp = frontRestTools.postForEntity(groupId, FrontRestTools.URI_GOVERNANCE_THRESHOLD,
            governanceHandle, Object.class);
        log.debug("end updateThreshold. frontRsp:{}", JsonTools.toJSONString(frontRsp));
        return frontRsp;
    }

    /**
     *  POST => grant, DELETE => revoke
     */
    public Object handleOperator(ChainGovernanceHandle governanceHandle, RequestType requestType) {
        log.debug("start grantOperator. governanceHandle:{},requestType:{}",
            JsonTools.toJSONString(governanceHandle), requestType);
        int groupId = governanceHandle.getGroupId();
        String signUserId = userService.getSignUserIdByAddress(groupId, governanceHandle.getFromAddress());
        governanceHandle.setSignUserId(signUserId);
        Object frontRsp = null;
        if (requestType == RequestType.POST) {
            frontRsp = frontRestTools.postForEntity(
                groupId, FrontRestTools.URI_GOVERNANCE_OPERATOR,
                governanceHandle, Object.class);
        } else if (requestType == RequestType.DELETE) {
            frontRsp = frontRestTools.deleteForEntity(
                groupId, FrontRestTools.URI_GOVERNANCE_OPERATOR,
                governanceHandle, Object.class);
        }
        log.debug("end handleOperator. frontRsp:{}", frontRsp);
        return frontRsp;
    }


    public List listOperator(Integer groupId) {
        log.debug("start listOperator. groupId:{}" , groupId);
        Map<String, String> map = new HashMap<>();
        map.put("groupId", String.valueOf(groupId));
        String uri = HttpRequestTools.getQueryUri(FrontRestTools.URI_GOVERNANCE_OPERATOR_LIST, map);
        List frontRsp = frontRestTools.getForEntity(groupId, uri, List.class);
        log.debug("end listOperator. frontRsp:{}", JsonTools.toJSONString(frontRsp));
        return frontRsp;
    }

    public String getAccountStatus(Integer groupId, String address) {
        log.debug("start getAccountStatus. groupId:{}" , groupId);
        Map<String, String> map = new HashMap<>();
        map.put("groupId", String.valueOf(groupId));
        map.put("address", String.valueOf(address));
        String uri = HttpRequestTools.getQueryUri(FrontRestTools.URI_GOVERNANCE_ACCOUNT_STATUS, map);
        String frontRsp = frontRestTools.getForEntity(groupId, uri, String.class);
        log.debug("end getAccountStatus. frontRsp:{}", JsonTools.toJSONString(frontRsp));
        return frontRsp;
    }

    public Map<String, String> listAccountStatus(AddressStatusHandle addressStatusHandle) {
        log.debug("start getAccountStatus. reqAccountStatus:{}" , addressStatusHandle);
        Integer groupId = Integer.MAX_VALUE;
        Map frontRsp = frontRestTools.postForEntity(groupId,
            FrontRestTools.URI_GOVERNANCE_ACCOUNT_STATUS_LIST, addressStatusHandle, Map.class);
        log.debug("end getAccountStatus. frontRsp:{}", JsonTools.toJSONString(frontRsp));
        return frontRsp;
    }

    /**
     * POST => FREEZE, DELETE => UNFREEZE
     */
    public Object handleAccountStatus(ChainGovernanceHandle governanceHandle, RequestType requestType) {
        log.debug("start handleAccountStatus. governanceHandle:{},requestType:{}",
            JsonTools.toJSONString(governanceHandle), requestType);
        int groupId = governanceHandle.getGroupId();
        String signUserId = userService.getSignUserIdByAddress(groupId, governanceHandle.getFromAddress());
        governanceHandle.setSignUserId(signUserId);
        Object frontRsp = null;
        if (requestType == RequestType.POST) {
            frontRsp = frontRestTools.postForEntity(
                groupId, FrontRestTools.URI_GOVERNANCE_ACCOUNT_FREEZE,
                governanceHandle, Object.class);
        } else if (requestType == RequestType.DELETE) {
            frontRsp = frontRestTools.deleteForEntity(
                groupId, FrontRestTools.URI_GOVERNANCE_ACCOUNT_UNFREEZE,
                governanceHandle, Object.class);
        }
        log.debug("end handleAccountStatus. frontRsp:{}", frontRsp);
        return frontRsp;
    }

}

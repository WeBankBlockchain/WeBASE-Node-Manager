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
package com.webank.webase.node.mgr.precntauth.precompiled.consensus;

import com.webank.webase.node.mgr.front.frontinterface.FrontRestTools;
import com.webank.webase.node.mgr.precntauth.precompiled.consensus.entity.ConsensusHandle;
import com.webank.webase.node.mgr.precntauth.precompiled.consensus.entity.ReqNodeListInfo;
import com.webank.webase.node.mgr.tools.HttpRequestTools;
import com.webank.webase.node.mgr.tools.JsonTools;
import com.webank.webase.node.mgr.user.UserService;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *  Node consensus status service;
 */
@Slf4j
@Service
public class ConsensusServiceInWebase {

  @Autowired
  private FrontRestTools frontRestTools;
  @Autowired
  private UserService userService;

  public Object getNodeList(ReqNodeListInfo reqNodeListInfo) {
    String frontRsp = frontRestTools.postForEntity(
        reqNodeListInfo.getGroupId(), FrontRestTools.RPC_PRECOM_CONSENSUS_LIST,
        reqNodeListInfo, String.class);
    return frontRsp;
  }

  public Object getNodeListService(String groupId, int pageSize, int pageNumber) {
        log.debug("start getNodeListService. groupId:{}", groupId);
        String uri;
        Map<String, String> map = new HashMap<>();
        map.put("groupId", String.valueOf(groupId));
        map.put("pageSize", String.valueOf(pageSize));
        map.put("pageNumber", String.valueOf(pageNumber));
        uri = HttpRequestTools.getQueryUri(FrontRestTools.RPC_PRECOM_CONSENSUS_LIST, map);
        Object frontRsp = frontRestTools.getForEntity(groupId, uri, Object.class);
        log.debug("end getNodeListService. frontRsp:{}", JsonTools.toJSONString(frontRsp));
        return frontRsp;
    }


  public String addSealer(ConsensusHandle consensusHandle) {
    String signUserId = userService.getSignUserIdByAddress(consensusHandle.getGroupId(),
        consensusHandle.getFromAddress());
    consensusHandle.setSignUserId(signUserId);
    String frontRsp = frontRestTools.postForEntity(
        consensusHandle.getGroupId(), FrontRestTools.RPC_PRECOM_CONSENSUS_MGR,
        consensusHandle, String.class);
    return frontRsp;
  }


  public String addObserver(ConsensusHandle consensusHandle) {
    String signUserId = userService.getSignUserIdByAddress(consensusHandle.getGroupId(),
        consensusHandle.getFromAddress());
    consensusHandle.setSignUserId(signUserId);
    String frontRsp = frontRestTools.postForEntity(
        consensusHandle.getGroupId(), FrontRestTools.RPC_PRECOM_CONSENSUS_MGR,
        consensusHandle, String.class);
    return frontRsp;
  }

  public String removeNode(ConsensusHandle consensusHandle) {
    String signUserId = userService.getSignUserIdByAddress(consensusHandle.getGroupId(),
        consensusHandle.getFromAddress());
    consensusHandle.setSignUserId(signUserId);
    String frontRsp = frontRestTools.postForEntity(
        consensusHandle.getGroupId(), FrontRestTools.RPC_PRECOM_CONSENSUS_MGR,
        consensusHandle, String.class);
    return frontRsp;
  }

}

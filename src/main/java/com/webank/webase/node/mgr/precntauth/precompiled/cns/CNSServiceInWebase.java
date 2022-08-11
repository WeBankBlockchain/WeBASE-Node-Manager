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
package com.webank.webase.node.mgr.precntauth.precompiled.cns;

import com.webank.webase.node.mgr.front.frontinterface.FrontRestTools;
import com.webank.webase.node.mgr.precntauth.precompiled.cns.entity.ReqCnsInfoByName;
import com.webank.webase.node.mgr.precntauth.precompiled.cns.entity.ReqInfoByNameVersion;
import com.webank.webase.node.mgr.precntauth.precompiled.cns.entity.ReqRegisterCnsInfo;
import com.webank.webase.node.mgr.user.UserService;
import org.fisco.bcos.sdk.v3.transaction.model.exception.ContractException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * CNS management service;
 */
@Service
public class CNSServiceInWebase {

  @Autowired
  private FrontRestTools frontRestTools;
  @Autowired
  private UserService userService;

  public Object registerCNS(ReqRegisterCnsInfo reqCnsInfo)
      {
    String signUserId = userService.getSignUserIdByAddress(reqCnsInfo.getGroupId(),
        reqCnsInfo.getFromAddress());
    reqCnsInfo.setSignUserId(signUserId);
    String frontRsp = frontRestTools.postForEntity(
        reqCnsInfo.getGroupId(), FrontRestTools.RPC_PRECOM_CNS_REGISTER,
        reqCnsInfo, String.class);
    return frontRsp;
  }

  public Object queryCnsInfoByName(ReqCnsInfoByName reqCnsInfoByName) {
    String frontRsp = frontRestTools.postForEntity(
        reqCnsInfoByName.getGroupId(), FrontRestTools.RPC_PRECOM_CNS_CNSINFO_BY_NAME,
        reqCnsInfoByName, String.class);
    return frontRsp;
  }

  public Object queryCnsByNameAndVersion(
      ReqInfoByNameVersion reqCnsInfoByNameVersion) {
    String frontRsp = frontRestTools.postForEntity(
        reqCnsInfoByNameVersion.getGroupId(), FrontRestTools.RPC_PRECOM_CNS_CNSINFO_BY_NAME_VERSION,
        reqCnsInfoByNameVersion, String.class);
    return frontRsp;
  }

  public String getAddressByContractNameAndVersion(
      ReqInfoByNameVersion reqAddressInfoByNameVersion) {
    String frontRsp = frontRestTools.postForEntity(
        reqAddressInfoByNameVersion.getGroupId(), FrontRestTools.RPC_PRECOM_CNS_ADDRESS_BY_NAME_VERSION,
        reqAddressInfoByNameVersion, String.class);
    return frontRsp;
  }

}
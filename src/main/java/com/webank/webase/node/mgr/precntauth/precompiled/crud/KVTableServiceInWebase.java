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
package com.webank.webase.node.mgr.precntauth.precompiled.crud;

import com.webank.webase.node.mgr.front.frontinterface.FrontRestTools;
import com.webank.webase.node.mgr.precntauth.precompiled.crud.entity.ReqCreateTableInfo;
import com.webank.webase.node.mgr.precntauth.precompiled.crud.entity.ReqGetTableInfo;
import com.webank.webase.node.mgr.precntauth.precompiled.crud.entity.ReqSetTableInfo;
import com.webank.webase.node.mgr.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.sdk.transaction.model.exception.ContractException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *  kvtable service
 */
@Slf4j
@Service
public class KVTableServiceInWebase {

  @Autowired
  private FrontRestTools frontRestTools;
  @Autowired
  private UserService userService;
  /***
   * createTable
   */
  public Object createTable(ReqCreateTableInfo reqCreateTableInfo) {
    String signUserId = userService.getSignUserIdByAddress(reqCreateTableInfo.getGroupId(),
        reqCreateTableInfo.getFromAddress());
    reqCreateTableInfo.setSignUserId(signUserId);
    String frontRsp = frontRestTools.postForEntity(
        reqCreateTableInfo.getGroupId(), FrontRestTools.RPC_PRECOM_CRUD_CREATE,
        reqCreateTableInfo, String.class);
    return frontRsp;
  }

  /**
   * set data
   */
  public Object set(ReqSetTableInfo reqSetTableInfo)
      throws ContractException {
    String signUserId = userService.getSignUserIdByAddress(reqSetTableInfo.getGroupId(),
        reqSetTableInfo.getFromAddress());
    reqSetTableInfo.setSignUserId(signUserId);
    String frontRsp = frontRestTools.postForEntity(
        reqSetTableInfo.getGroupId(), FrontRestTools.RPC_PRECOM_CRUD_SET,
        reqSetTableInfo, String.class);
    return frontRsp;
  }


  /**
   * read data
   */
  public Object get(ReqGetTableInfo reqGetTableInfo)
      throws ContractException {
    String frontRsp = frontRestTools.postForEntity(
        reqGetTableInfo.getGroupId(), FrontRestTools.RPC_PRECOM_CRUD_GET,
        reqGetTableInfo, String.class);
    return frontRsp;
  }

}

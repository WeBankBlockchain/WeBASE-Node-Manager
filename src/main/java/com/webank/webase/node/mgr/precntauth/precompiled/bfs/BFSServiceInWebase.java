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
package com.webank.webase.node.mgr.precntauth.precompiled.bfs;

import com.webank.webase.node.mgr.front.frontinterface.FrontRestTools;
import com.webank.webase.node.mgr.precntauth.precompiled.bfs.entity.ReqCreateBFSInfo;
import com.webank.webase.node.mgr.precntauth.precompiled.bfs.entity.ReqQueryBFSInfo;
import org.fisco.bcos.sdk.transaction.model.exception.ContractException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * BFS service;
 */
@Service
public class BFSServiceInWebase {

  @Autowired
  private FrontRestTools frontRestTools;

  /**
   * BFS创建某个目录
   */
  public Object createPath(ReqCreateBFSInfo reqCreateBFSInfo)
      throws ContractException {
    String frontRsp = frontRestTools.postForEntity(
        reqCreateBFSInfo.getGroupId(), FrontRestTools.RPC_PRECOM_BFS_CREATE,
        reqCreateBFSInfo, String.class);
    return frontRsp;
  }

  /**
   * BFS获取某个目录信息
   */
  public Object queryPath(ReqQueryBFSInfo reqQueryBFSInfoh)
      throws ContractException {
    String frontRsp = frontRestTools.postForEntity(
        reqQueryBFSInfoh.getGroupId(), FrontRestTools.RPC_PRECOM_BFS_QUERY,
        reqQueryBFSInfoh, String.class);
    return frontRsp;
  }

}

/**
 * Copyright 2014-2021 the original author or authors.
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

package com.webank.webase.node.mgr.precntauth.authmanager.everyone;

import com.webank.webase.node.mgr.front.frontinterface.FrontRestTools;
import com.webank.webase.node.mgr.precntauth.authmanager.everyone.entity.ReqCheckMethodAuthInfo;
import com.webank.webase.node.mgr.precntauth.authmanager.everyone.entity.ReqContractAdminInfo;
import com.webank.webase.node.mgr.precntauth.authmanager.everyone.entity.ReqProposalInfo;
import com.webank.webase.node.mgr.precntauth.authmanager.everyone.entity.ReqProposalListInfo;
import com.webank.webase.node.mgr.precntauth.authmanager.everyone.entity.ReqUsrDeployAuthInfo;
import com.webank.webase.node.mgr.tools.HttpRequestTools;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class EveryoneService {

  @Autowired
  private FrontRestTools frontRestTools;

  /**
   * 从front服务获取治理委员信息
   */
  public List<Object> queryCommitteeInfo(String groupId) {
    Map<String, String> map = new HashMap<>();
    map.put("groupId", groupId);
    String uri = HttpRequestTools.getQueryUri(
        FrontRestTools.RPC_AUTHMANAGER_EVERYONE_CMTINFO, map);
    List<Object> frontRsp = frontRestTools.getForEntity(groupId, uri, List.class);
    return frontRsp;
  }

  /**
   * 从front服务获取当前全局部署的权限策略,策略类型：0则无策略，1则为白名单模式，2则为黑名单模式
   */
  public Object queryDeployAuthType(String groupId) {
    Map<String, String> map = new HashMap<>();
    map.put("groupId", groupId);
    String uri = HttpRequestTools.getQueryUri(
        FrontRestTools.RPC_AUTHMANAGER_EVERYONE_DEPLOY_TYPE, map);
    BigInteger frontRsp = frontRestTools.getForEntity(groupId, uri, BigInteger.class);
    return frontRsp;
  }

  /**
   * 从front服务查询某个交易详情
   */
  public List<Object> queryProposalInfo(ReqProposalInfo reqProposalInfo) {
    List<Object> frontRsp = frontRestTools.postForEntity(reqProposalInfo.getGroupId(),
        FrontRestTools.RPC_AUTHMANAGER_EVERYONE_PROINFO, reqProposalInfo, List.class);
    return frontRsp;
  }

  /**
   * 从front服务查询交易列表
   */
  public List<Object> queryProposalListInfo(ReqProposalListInfo reqProposalListInfo) {
    List<Object> frontRsp = frontRestTools.postForEntity(reqProposalListInfo.getGroupId(),
        FrontRestTools.RPC_AUTHMANAGER_EVERYONE_PROINFOLIST, reqProposalListInfo, List.class);
    return frontRsp;
  }

  /**
   * 从front服务查询交易列表
   */
  public Object queryProposalListInfoCount(String groupId) {
    Map<String, String> map = new HashMap<>();
    map.put("groupId", groupId);
    String uri = HttpRequestTools.getQueryUri(
        FrontRestTools.RPC_AUTHMANAGER_EVERYONE_PROINFOCOUNT, map);
    BigInteger frontRsp = frontRestTools.getForEntity(groupId, uri, BigInteger.class);
    return frontRsp;
  }


  /**
   * 从front服务检查账号是否具有全局部署权限
   */
  public Object checkDeployAuth(ReqUsrDeployAuthInfo reqUsrDeployAuthInfo) {
    Boolean frontRsp = frontRestTools.postForEntity(reqUsrDeployAuthInfo.getGroupId(),
        FrontRestTools.RPC_AUTHMANAGER_EVERYONE_USR_DEPLOY, reqUsrDeployAuthInfo, Boolean.class);
    return frontRsp;
  }

  /**
   * 从front服务检查某个账号是否有某个合约的某接口的调用权限
   */
  public Object checkMethodAuth(ReqCheckMethodAuthInfo reqCheckMethodAuthInfo) {
    Boolean frontRsp = frontRestTools.postForEntity(reqCheckMethodAuthInfo.getGroupId(),
        FrontRestTools.RPC_AUTHMANAGER_EVERYONE_CNT_METHOD_AUTH, reqCheckMethodAuthInfo,
        Boolean.class);
    return frontRsp;
  }

  /**
   * 从front服务获取特定合约的管理员地址
   */
  public Object queryAdmin(ReqContractAdminInfo reqContractAdminInfo) {
    String frontRsp = frontRestTools.postForEntity(reqContractAdminInfo.getGroupId(),
        FrontRestTools.RPC_AUTHMANAGER_EVERYONE_CNT_ADMIN, reqContractAdminInfo,
        String.class);
    return frontRsp;
  }

  /**
   * 从front服务获取特定合约的管理员地址
   */
  public Object isContractAvailable(ReqContractAdminInfo reqContractStatus) {
    String frontRsp = frontRestTools.postForEntity(reqContractStatus.getGroupId(),
        FrontRestTools.RPC_AUTHMANAGER_EVERYONE_CNT_STATUS_GET, reqContractStatus,
        String.class);
    return frontRsp;
  }

}
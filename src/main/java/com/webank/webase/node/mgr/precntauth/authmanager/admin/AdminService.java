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

package com.webank.webase.node.mgr.precntauth.authmanager.admin;

import com.webank.webase.node.mgr.front.frontinterface.FrontRestTools;
import com.webank.webase.node.mgr.precntauth.authmanager.admin.entity.ReqAclAuthTypeInfo;
import com.webank.webase.node.mgr.precntauth.authmanager.admin.entity.ReqAclUsrInfo;
import com.webank.webase.node.mgr.precntauth.authmanager.committee.vote.GovernVoteService;
import com.webank.webase.node.mgr.user.UserService;
import lombok.extern.log4j.Log4j2;
import org.fisco.bcos.sdk.transaction.model.exception.ContractException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Log4j2
@Service
public class AdminService {

  @Autowired
  private FrontRestTools frontRestTools;
  @Autowired
  private UserService userService;
  @Autowired
  private GovernVoteService governVoteService;

//
//    /**
//     *  POST => grant, DELETE => revoke
//     */
//    public BaseResponse handleOperator(ChainGovernanceHandle governanceHandle, RequestType requestType) {
//        log.debug("start grantOperator. governanceHandle:{},requestType:{}",
//            JsonTools.toJSONString(governanceHandle), requestType);
//        String groupId = governanceHandle.getGroupId();
//        String signUserId = userService.getSignUserIdByAddress(groupId, governanceHandle.getFromAddress());
//        governanceHandle.setSignUserId(signUserId);
//        String frontRsp = null;
//        if (requestType == RequestType.POST) {
//            frontRsp = frontRestTools.postForEntity(
//                groupId, FrontRestTools.URI_GOVERNANCE_OPERATOR,
//                governanceHandle, String.class);
//        } else if (requestType == RequestType.DELETE) {
//            frontRsp = frontRestTools.deleteForEntity(
//                groupId, FrontRestTools.URI_GOVERNANCE_OPERATOR,
//                governanceHandle, String.class);
//        }
//        log.debug("end handleOperator. frontRsp:{}", frontRsp);
//        return PrecompiledTools.processResponse(frontRsp);
//    }
//
//
//    public List listOperator(String groupId) {
//        log.debug("start listOperator. groupId:{}" , groupId);
//        Map<String, String> map = new HashMap<>();
//        map.put("groupId", String.valueOf(groupId));
//        String uri = HttpRequestTools.getQueryUri(FrontRestTools.URI_GOVERNANCE_OPERATOR_LIST, map);
//        List frontRsp = frontRestTools.getForEntity(groupId, uri, List.class);
//        log.debug("end listOperator. frontRsp:{}", JsonTools.toJSONString(frontRsp));
//        return frontRsp;
//    }
//
//    /**
//     * get status code of account, 0-normal, 1-frozen
//     * @return 0 or 1
//     */
//    public String getAccountStatus(String groupId, String address) {
//        log.debug("start getAccountStatus. groupId:{}" , groupId);
//        Map<String, String> map = new HashMap<>();
//        map.put("groupId", String.valueOf(groupId));
//        map.put("address", String.valueOf(address));
//        String uri = HttpRequestTools.getQueryUri(FrontRestTools.URI_GOVERNANCE_ACCOUNT_STATUS, map);
//        String frontRsp = frontRestTools.getForEntity(groupId, uri, String.class);
//        log.debug("end getAccountStatus. frontRsp:{}", JsonTools.toJSONString(frontRsp));
//        return frontRsp;
//    }

  /**
   * set contract acl: white_list(type=1) or black_list(type=2)
   */
  public Object setMethodAuthType(ReqAclAuthTypeInfo reqAclAuthTypeInfo) {
    String signUserId = userService.getSignUserIdByAddress(reqAclAuthTypeInfo.getGroupId(),
        reqAclAuthTypeInfo.getFromAddress());
    reqAclAuthTypeInfo.setSignUserId(signUserId);
    String frontRsp = frontRestTools.postForEntity(
        reqAclAuthTypeInfo.getGroupId(), FrontRestTools.RPC_AUTHMANAGER_ADMIN_METHOD_AUTH_TYPE,
        reqAclAuthTypeInfo, String.class);
    return frontRsp;
  }


  /**
   * open  or close the method permission of contract
   */
  public Object setMethodAuth(ReqAclUsrInfo reqAclUsrInfo) throws ContractException {
    String signUserId = userService.getSignUserIdByAddress(reqAclUsrInfo.getGroupId(),
        reqAclUsrInfo.getFromAddress());
    reqAclUsrInfo.setSignUserId(signUserId);
    String frontRsp = frontRestTools.postForEntity(
        reqAclUsrInfo.getGroupId(), FrontRestTools.RPC_AUTHMANAGER_ADMIN_METHOD_AUTH_SET,
        reqAclUsrInfo, String.class);
    return frontRsp;
  }

}

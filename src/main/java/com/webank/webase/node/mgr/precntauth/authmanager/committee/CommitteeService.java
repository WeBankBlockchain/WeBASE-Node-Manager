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

package com.webank.webase.node.mgr.precntauth.authmanager.committee;

import com.webank.webase.node.mgr.front.frontinterface.FrontRestTools;
import com.webank.webase.node.mgr.precntauth.authmanager.committee.entity.ReqDeployAuthTypeInfo;
import com.webank.webase.node.mgr.precntauth.authmanager.committee.entity.ReqResetAdminInfo;
import com.webank.webase.node.mgr.precntauth.authmanager.committee.entity.ReqRevokeProposalInfo;
import com.webank.webase.node.mgr.precntauth.authmanager.committee.entity.ReqSetRateInfo;
import com.webank.webase.node.mgr.precntauth.authmanager.committee.entity.ReqUpdateGovernorInfo;
import com.webank.webase.node.mgr.precntauth.authmanager.committee.entity.ReqUsrDeployInfo;
import com.webank.webase.node.mgr.precntauth.authmanager.committee.entity.ReqVoteProposalInfo;
import com.webank.webase.node.mgr.precntauth.authmanager.committee.vote.GovernVoteService;
import com.webank.webase.node.mgr.user.UserService;
import lombok.extern.log4j.Log4j2;
import org.fisco.bcos.sdk.transaction.model.exception.ContractException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class CommitteeService {

    @Autowired
    private FrontRestTools frontRestTools;
    @Autowired
    private UserService userService;
    @Autowired
    private GovernVoteService governVoteService;

    /**
     * 更新治理委员信息。 如果是新加治理委员，新增地址和权重即可。如果是删除治理委员，将一个治理委员的权重设置为0 即可
     */
    public Object updateGovernor(ReqUpdateGovernorInfo reqUpdateGovernorInfo) {
        String signUserId = userService.getSignUserIdByAddress(reqUpdateGovernorInfo.getGroupId(),
            reqUpdateGovernorInfo.getFromAddress());
        reqUpdateGovernorInfo.setSignUserId(signUserId);
        String frontRsp = frontRestTools.postForEntity(
            reqUpdateGovernorInfo.getGroupId(), FrontRestTools.RPC_AUTHMANAGER_COMMITTEE_GOVERNOR,
            reqUpdateGovernorInfo, String.class);
        //todo check data into db
        //governVoteService.saveGovernVote(reqUpdateGovernorInfo);

        return frontRsp;
    }

    /**
     * 设置提案阈值，提案阈值分为参与阈值和权重阈值。
     */
    public Object setRate(ReqSetRateInfo reqSetRateInfo) throws ContractException {
        String signUserId = userService.getSignUserIdByAddress(reqSetRateInfo.getGroupId(),
            reqSetRateInfo.getFromAddress());
        reqSetRateInfo.setSignUserId(signUserId);
        String frontRsp = frontRestTools.postForEntity(
            reqSetRateInfo.getGroupId(), FrontRestTools.RPC_AUTHMANAGER_COMMITTEE_RATE,
            reqSetRateInfo, String.class);
        return frontRsp;
    }

    /**
     * 设置部署的ACL策略 只支持 white_list 和 black_list 两种策略 type为1时，设置为白名单，type为2时，设置为黑名单。
     */
    public Object setDeployAuthType(ReqDeployAuthTypeInfo reqDeployAuthTypeInfo) {
        String signUserId = userService.getSignUserIdByAddress(reqDeployAuthTypeInfo.getGroupId(),
            reqDeployAuthTypeInfo.getFromAddress());
        reqDeployAuthTypeInfo.setSignUserId(signUserId);
        String frontRsp = frontRestTools.postForEntity(
            reqDeployAuthTypeInfo.getGroupId(),
            FrontRestTools.RPC_AUTHMANAGER_COMMITTEE_DEPLOY_TYPE,
            reqDeployAuthTypeInfo, String.class);
        return frontRsp;
    }

    /**
     * 发起修改某个账户的部署权限提案
     */
    public Object modifyDeployUsrAuth(ReqUsrDeployInfo reqUsrDeployInfo) {
        String signUserId = userService.getSignUserIdByAddress(reqUsrDeployInfo.getGroupId(),
            reqUsrDeployInfo.getFromAddress());
        reqUsrDeployInfo.setSignUserId(signUserId);
        String frontRsp = frontRestTools.postForEntity(
            reqUsrDeployInfo.getGroupId(), FrontRestTools.RPC_AUTHMANAGER_COMMITTEE_USR_DEPLOY,
            reqUsrDeployInfo, String.class);
        return frontRsp;
    }

    /**
     * 重置某个合约的管理员账号提案
     */
    public Object resetAdmin(ReqResetAdminInfo reqResetAdminInfo) {
        String signUserId = userService.getSignUserIdByAddress(reqResetAdminInfo.getGroupId(),
            reqResetAdminInfo.getFromAddress());
        reqResetAdminInfo.setSignUserId(signUserId);
        String frontRsp = frontRestTools.postForEntity(
            reqResetAdminInfo.getGroupId(), FrontRestTools.RPC_AUTHMANAGER_COMMITTEE_CNT_ADMIN,
            reqResetAdminInfo, String.class);
        return frontRsp;
    }

    /**
     * 撤销提案的发起，该操作只有发起提案的治理委员才能操作
     */
    public Object revokeProposal(ReqRevokeProposalInfo reqRevokeProposalInfo) {
        String signUserId = userService.getSignUserIdByAddress(reqRevokeProposalInfo.getGroupId(),
            reqRevokeProposalInfo.getFromAddress());
        reqRevokeProposalInfo.setSignUserId(signUserId);
        String frontRsp = frontRestTools.postForEntity(
            reqRevokeProposalInfo.getGroupId(), FrontRestTools.RPC_AUTHMANAGER_COMMITTEE_PRO_REVOKE,
            reqRevokeProposalInfo, String.class);
        return frontRsp;
    }

    /**
     * 向某个提案进行投票
     */
    public Object voteProposal(ReqVoteProposalInfo reqVoteProposalInfo) {
        String signUserId = userService.getSignUserIdByAddress(reqVoteProposalInfo.getGroupId(),
            reqVoteProposalInfo.getFromAddress());
        reqVoteProposalInfo.setSignUserId(signUserId);
        String frontRsp = frontRestTools.postForEntity(
            reqVoteProposalInfo.getGroupId(), FrontRestTools.RPC_AUTHMANAGER_COMMITTEE_PRO_VOTE,
            reqVoteProposalInfo, String.class);
        return frontRsp;
    }
}

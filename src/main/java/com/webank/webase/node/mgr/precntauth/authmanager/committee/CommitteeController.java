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

import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.controller.BaseController;
import com.webank.webase.node.mgr.base.entity.BaseResponse;
import com.webank.webase.node.mgr.config.properties.ConstantProperties;
import com.webank.webase.node.mgr.precntauth.authmanager.committee.entity.ReqDeployAuthTypeInfo;
import com.webank.webase.node.mgr.precntauth.authmanager.committee.entity.ReqResetAdminInfo;
import com.webank.webase.node.mgr.precntauth.authmanager.committee.entity.ReqRevokeProposalInfo;
import com.webank.webase.node.mgr.precntauth.authmanager.committee.entity.ReqSetRateInfo;
import com.webank.webase.node.mgr.precntauth.authmanager.committee.entity.ReqUpdateGovernorInfo;
import com.webank.webase.node.mgr.precntauth.authmanager.committee.entity.ReqUsrDeployInfo;
import com.webank.webase.node.mgr.precntauth.authmanager.committee.entity.ReqVoteProposalInfo;
import com.webank.webase.node.mgr.tools.JsonTools;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import javax.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.fisco.bcos.sdk.codec.ABICodecException;
import org.fisco.bcos.sdk.transaction.model.exception.ContractException;
import org.fisco.bcos.sdk.transaction.model.exception.TransactionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * authmanager committee manage above FISCO-BCOS v3.0
 */
@Log4j2
@Api(value = "precntauth/authmanager/committee/", tags = "precntauth authmanager controller")
@RestController
@RequestMapping(value = "precntauth/authmanager/committee/")
public class CommitteeController extends BaseController {

    @Autowired
    private CommitteeService chainGovernService;
    @Autowired
    private CommitteeService committeeService;

    /**
     * 更新(新增、删除)治理委员信息(weight设置为0表示删除)
     */
    @ApiOperation(value = "update committee governor")
    @ApiImplicitParam(name = "reqUpdateGovernorInfo", value = "governor info", required = true
        , dataType = "ReqUpdateGovernorInfo")
    @PostMapping("governor")
    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN)
    public Object updateGovernor(
        @Valid @RequestBody ReqUpdateGovernorInfo reqUpdateGovernorInfo, BindingResult result)
        throws ContractException, ABICodecException, TransactionException, IOException {
        checkBindResult(result);
        Instant startTime = Instant.now();
        log.info("start updateGovernor startTime:{} reqUpdateGovernorInfo:{}",
            startTime.toEpochMilli(),
            JsonTools.toJSONString(reqUpdateGovernorInfo));
        Object res = committeeService.updateGovernor(reqUpdateGovernorInfo);
        log.info("end grantCommittee useTime:{} result:{}",
            Duration.between(startTime, Instant.now()).toMillis(), JsonTools.toJSONString(res));
        return res;
    }

    /**
     * 设置治理阈值rate
     */
    @ApiOperation(value = "set committee rate")
    @ApiImplicitParam(name = "reqSetRateInfo", value = "rate info", required = true,
        dataType = "ReqSetRateInfo")
    @PostMapping("rate")
    public Object setRate(@Valid @RequestBody ReqSetRateInfo reqSetRateInfo)
        throws ContractException, ABICodecException, TransactionException, IOException {
        return committeeService.setRate(reqSetRateInfo);
    }

    /**
     * 设置全局部署类型  (white_list和black_list两种策略.type为1时为白名单，type为2时为黑名单)
     */
    @ApiOperation(value = "set deploy type")
    @ApiImplicitParam(name = "reqDeployAuthTypeInfo", value = "DeployAuthTypeInfo", required = true,
        dataType = "ReqDeployAuthTypeInfo")
    @PostMapping("deploy/type")
    public Object setDeployAuthType(
        @Valid @RequestBody ReqDeployAuthTypeInfo reqDeployAuthTypeInfo) {
        return committeeService.setDeployAuthType(reqDeployAuthTypeInfo);
    }

    /**
     * 修改用户部署权限
     */
    @ApiOperation(value = "modify deploy user", notes = "openFlag value is true or false")
    @ApiImplicitParam(name = "reqUsrDeployInfo", value = "usrDeployAuth info", required = true,
        dataType = "ReqUsrDeployInfo")
    @PostMapping("usr/deploy")
    public Object modifyDeployUsrAuth(@Valid @RequestBody ReqUsrDeployInfo reqUsrDeployInfo) {
        return committeeService.modifyDeployUsrAuth(reqUsrDeployInfo);
    }

    /**
     * 重设合约管理员
     */
    @ApiOperation(value = "reset the admin of contract")
    @ApiImplicitParam(name = "reqResetAdminInfo", value = "resetAdmin info", required = true,
        dataType = "ReqResetAdminInfo")
    @PostMapping("contract/admin")
    public Object resetAdmin(@Valid @RequestBody ReqResetAdminInfo reqResetAdminInfo)
        throws ABICodecException, TransactionException, ContractException, IOException {
        return committeeService.resetAdmin(reqResetAdminInfo);
    }

    /**
     * 撤销某提案
     */
    @ApiOperation(value = "revoke the proposal")
    @ApiImplicitParam(name = "reqRevokeProposalInfo", value = "revokeProposal info", required = true,
        dataType = "ReqRevokeProposalInfo")
    @PostMapping("proposal/revoke")
    public Object revokeProposal(@Valid @RequestBody ReqRevokeProposalInfo reqRevokeProposalInfo)
        throws ABICodecException, TransactionException, ContractException, IOException {
        return committeeService.revokeProposal(reqRevokeProposalInfo);
    }

    /**
     * 对某提案进行投票
     */
    @ApiOperation(value = "vote the proposal")
    @ApiImplicitParam(name = "reqVoteProposalInfo", value = "voteProposal info", required = true,
        dataType = "ReqVoteProposalInfo")
    @PostMapping("proposal/vote")
    public Object voteProposal(@Valid @RequestBody ReqVoteProposalInfo reqVoteProposalInfo)
        throws ABICodecException, TransactionException, ContractException, IOException {
        return committeeService.voteProposal(reqVoteProposalInfo);
    }
}

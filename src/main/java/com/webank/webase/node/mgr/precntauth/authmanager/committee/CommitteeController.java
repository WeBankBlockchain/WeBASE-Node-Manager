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
import com.webank.webase.node.mgr.precntauth.authmanager.base.BaseService;
import com.webank.webase.node.mgr.precntauth.authmanager.committee.entity.*;
import com.webank.webase.node.mgr.tools.JsonTools;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.time.Duration;
import java.time.Instant;

/**
 * authmanager committee manage above FISCO-BCOS v3.0
 */
@Log4j2
//@Api(value = "precntauth/authmanager/committee/", tags = "precntauth authmanager controller")
@RestController
@RequestMapping(value = "precntauth/authmanager/committee/")
public class CommitteeController extends BaseController {

    @Autowired
    private CommitteeService committeeService;
    @Autowired
    private BaseService baseService;

    /**
     * 更新(新增、删除)治理委员信息(weight设置为0表示删除)
     */
//    @ApiOperation(value = "update committee governor")
//    @ApiImplicitParam(name = "reqUpdateGovernorInfo", value = "governor info", required = true
//        , dataType = "ReqUpdateGovernorInfo")
    @PostMapping("governor")
    public Object updateGovernor(
        @Valid @RequestBody ReqUpdateGovernorInfo reqUpdateGovernorInfo, BindingResult result) {
        if (baseService.queryExecEnvIsWasm(reqUpdateGovernorInfo.getGroupId())) {
            return new BaseResponse(ConstantCode.EXEC_ENV_IS_WASM);
        }
        if (!baseService.queryChainHasAuth(reqUpdateGovernorInfo.getGroupId())) {
            return new BaseResponse(ConstantCode.CHAIN_AUTH_NOT_ENABLE);
        }
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
//    @ApiOperation(value = "set committee rate")
//    @ApiImplicitParam(name = "reqSetRateInfo", value = "rate info", required = true,
//        dataType = "ReqSetRateInfo")
    @PostMapping("rate")
    public Object setRate(@Valid @RequestBody ReqSetRateInfo reqSetRateInfo) {
        if (baseService.queryExecEnvIsWasm(reqSetRateInfo.getGroupId())) {
            return new BaseResponse(ConstantCode.EXEC_ENV_IS_WASM);
        }
        if (!baseService.queryChainHasAuth(reqSetRateInfo.getGroupId())) {
            return new BaseResponse(ConstantCode.CHAIN_AUTH_NOT_ENABLE);
        }
        return committeeService.setRate(reqSetRateInfo);
    }

    /**
     * 设置全局部署类型  (white_list和black_list两种策略.type为1时为白名单，type为2时为黑名单)
     */
//    @ApiOperation(value = "set deploy type")
//    @ApiImplicitParam(name = "reqDeployAuthTypeInfo", value = "DeployAuthTypeInfo", required = true,
//        dataType = "ReqDeployAuthTypeInfo")
    @PostMapping("deploy/type")
    public Object setDeployAuthType(
        @Valid @RequestBody ReqDeployAuthTypeInfo reqDeployAuthTypeInfo) {
        if (baseService.queryExecEnvIsWasm(reqDeployAuthTypeInfo.getGroupId())) {
            return new BaseResponse(ConstantCode.EXEC_ENV_IS_WASM);
        }
        if (!baseService.queryChainHasAuth(reqDeployAuthTypeInfo.getGroupId())) {
            return new BaseResponse(ConstantCode.CHAIN_AUTH_NOT_ENABLE);
        }
        return committeeService.setDeployAuthType(reqDeployAuthTypeInfo);
    }

    /**
     * 修改用户部署权限
     */
//    @ApiOperation(value = "modify deploy user", notes = "openFlag value is true or false")
//    @ApiImplicitParam(name = "reqUsrDeployInfo", value = "usrDeployAuth info", required = true,
//        dataType = "ReqUsrDeployInfo")
    @PostMapping("usr/deploy")
    public Object modifyDeployUsrAuth(@Valid @RequestBody ReqUsrDeployInfo reqUsrDeployInfo) {
        if (baseService.queryExecEnvIsWasm(reqUsrDeployInfo.getGroupId())) {
            return new BaseResponse(ConstantCode.EXEC_ENV_IS_WASM);
        }
        if (!baseService.queryChainHasAuth(reqUsrDeployInfo.getGroupId())) {
            return new BaseResponse(ConstantCode.CHAIN_AUTH_NOT_ENABLE);
        }
        return committeeService.modifyDeployUsrAuth(reqUsrDeployInfo);
    }

    /**
     * 重设合约管理员
     */
//    @ApiOperation(value = "reset the admin of contract")
//    @ApiImplicitParam(name = "reqResetAdminInfo", value = "resetAdmin info", required = true,
//        dataType = "ReqResetAdminInfo")
    @PostMapping("contract/admin")
    public Object resetAdmin(@Valid @RequestBody ReqResetAdminInfo reqResetAdminInfo) {
        if (baseService.queryExecEnvIsWasm(reqResetAdminInfo.getGroupId())) {
            return new BaseResponse(ConstantCode.EXEC_ENV_IS_WASM);
        }
            if (!baseService.queryChainHasAuth(reqResetAdminInfo.getGroupId())) {
                return new BaseResponse(ConstantCode.CHAIN_AUTH_NOT_ENABLE);
            }
        return committeeService.resetAdmin(reqResetAdminInfo);
    }

    /**
     * 撤销某提案
     */
//    @ApiOperation(value = "revoke the proposal")
//    @ApiImplicitParam(name = "reqRevokeProposalInfo", value = "revokeProposal info", required = true,
//        dataType = "ReqRevokeProposalInfo")
    @PostMapping("proposal/revoke")
    public Object revokeProposal(@Valid @RequestBody ReqRevokeProposalInfo reqRevokeProposalInfo) {
        if (baseService.queryExecEnvIsWasm(reqRevokeProposalInfo.getGroupId())) {
            return new BaseResponse(ConstantCode.EXEC_ENV_IS_WASM);
        }
        if (!baseService.queryChainHasAuth(reqRevokeProposalInfo.getGroupId())) {
            return new BaseResponse(ConstantCode.CHAIN_AUTH_NOT_ENABLE);
        }
        return committeeService.revokeProposal(reqRevokeProposalInfo);
    }

    /**
     * 对某提案进行投票
     */
//    @ApiOperation(value = "vote the proposal")
//    @ApiImplicitParam(name = "reqVoteProposalInfo", value = "voteProposal info", required = true,
//        dataType = "ReqVoteProposalInfo")
    @PostMapping("proposal/vote")
    public Object voteProposal(@Valid @RequestBody ReqVoteProposalInfo reqVoteProposalInfo) {
        if (baseService.queryExecEnvIsWasm(reqVoteProposalInfo.getGroupId())) {
            return new BaseResponse(ConstantCode.EXEC_ENV_IS_WASM);
        }
        if (!baseService.queryChainHasAuth(reqVoteProposalInfo.getGroupId())) {
            return new BaseResponse(ConstantCode.CHAIN_AUTH_NOT_ENABLE);
        }
        return committeeService.voteProposal(reqVoteProposalInfo);
    }
}

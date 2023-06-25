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

import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.controller.BaseController;
import com.webank.webase.node.mgr.base.entity.BaseResponse;
import com.webank.webase.node.mgr.precntauth.authmanager.base.BaseService;
import com.webank.webase.node.mgr.precntauth.authmanager.everyone.entity.ReqCheckMethodAuthInfo;
import com.webank.webase.node.mgr.precntauth.authmanager.everyone.entity.ReqContractAdminInfo;
import com.webank.webase.node.mgr.precntauth.authmanager.everyone.entity.ReqContractStatusList;
import com.webank.webase.node.mgr.precntauth.authmanager.everyone.entity.ReqProposalInfo;
import com.webank.webase.node.mgr.precntauth.authmanager.everyone.entity.ReqProposalListInfo;
import com.webank.webase.node.mgr.precntauth.authmanager.everyone.entity.ReqUsrDeployAuthInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import javax.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *  authmanager everyone manage above FISCO-BCOS v3.0
 *  透传front的BaseResponse
 */
@Log4j2
@Api(value = "precntauth/authmanager/everyone/", tags = "precntauth authmanager controller")
@RestController
@RequestMapping(value = "precntauth/authmanager/everyone/")
public class EveryoneController extends BaseController {

  @Autowired
  private EveryoneService everyoneService;
  @Autowired
  private BaseService baseService;

  /**
   * 获取治理委员会信息
   */
  @ApiImplicitParam(name = "groupId", value = "committee info", required = true)
  @GetMapping("cmtInfo")
  public BaseResponse queryCommittee(@RequestParam(defaultValue = "group") String groupId) {
    if (baseService.queryExecEnvIsWasm(groupId)) {
      return new BaseResponse(ConstantCode.EXEC_ENV_IS_WASM);
    }
    return new BaseResponse(ConstantCode.SUCCESS, everyoneService.queryCommitteeInfo(groupId));
  }

  /**
   * 获取当前全局部署的权限策略 策略类型：0则无策略，1则为白名单模式，2则为黑名单模式
   */
  @ApiOperation(value = "query the deploy type")
  @ApiImplicitParam(name = "groupId", value = "groupId", required = true)
  @GetMapping("deploy/type")
  public Object queryDeployAuthType(String groupId) {
    if (baseService.queryExecEnvIsWasm(groupId)) {
      return new BaseResponse(ConstantCode.EXEC_ENV_IS_WASM);
    }
    return new BaseResponse(ConstantCode.SUCCESS, everyoneService.queryDeployAuthType(groupId));
  }

  /**
   * 获取某个提案信息
   */
  @ApiOperation(value = "query the proposal info")
  @ApiImplicitParam(name = "reqProposalInfo", value = "proposal info", required = true,
      dataType = "ReqProposalInfo")
  @PostMapping("proposalInfo")
  public BaseResponse queryProposalInfo(@Valid @RequestBody ReqProposalInfo reqProposalInfo) {
    if (baseService.queryExecEnvIsWasm(reqProposalInfo.getGroupId())) {
      return new BaseResponse(ConstantCode.EXEC_ENV_IS_WASM);
    }
    return new BaseResponse(ConstantCode.SUCCESS,
        everyoneService.queryProposalInfo(reqProposalInfo));
  }

  /**
   * 获取对应提案列表
   */
  @ApiOperation(value = "query the proposal info list")
  @ApiImplicitParam(name = "reqProposalListInfo", value = "proposal info list", required = true,
      dataType = "ReqProposalListInfo")
  @PostMapping("proposalInfoList")
  public BaseResponse queryProposalList(
      @Valid @RequestBody ReqProposalListInfo reqProposalListInfo) {
    if (baseService.queryExecEnvIsWasm(reqProposalListInfo.getGroupId())) {
      return new BaseResponse(ConstantCode.EXEC_ENV_IS_WASM);
    }
    return new BaseResponse(ConstantCode.SUCCESS,
        everyoneService.queryProposalListInfo(reqProposalListInfo));
  }

  /**
   * 获取提案总数
   */
  @ApiOperation(value = "query the proposal info count")
  @ApiImplicitParam(name = "groupId", value = "proposal info count", required = true)
  @GetMapping("proposalInfoCount")
  public BaseResponse queryProposalCount(String groupId) {
    if (baseService.queryExecEnvIsWasm(groupId)) {
      return new BaseResponse(ConstantCode.EXEC_ENV_IS_WASM);
    }
    return new BaseResponse(ConstantCode.SUCCESS,
        everyoneService.queryProposalListInfoCount(groupId));
  }

  /**
   * 检查账号是否具有全局部署权限
   */
  @ApiOperation(value = "query the user deploy auth")
  @ApiImplicitParam(name = "reqUsrDeployAuthInfo", value = "usrDeployAuth info", required = true,
      dataType = "ReqUsrDeployAuthInfo")
  @PostMapping("usr/deploy")
  public Object checkDeployAuth(@Valid @RequestBody ReqUsrDeployAuthInfo reqUsrDeployAuthInfo) {
    if (baseService.queryExecEnvIsWasm(reqUsrDeployAuthInfo.getGroupId())) {
      return new BaseResponse(ConstantCode.EXEC_ENV_IS_WASM);
    }
    return new BaseResponse(ConstantCode.SUCCESS,
        everyoneService.checkDeployAuth(reqUsrDeployAuthInfo));
  }

  /**
   * 查询合约的管理员
   */
  @ApiOperation(value = "query the contract's admin")
  @ApiImplicitParam(name = "reqContractAdminInfo", value = "contractAdmin info", required = true,
      dataType = "ReqContractAdminInfo")
  @PostMapping("contract/admin")
  public Object queryAdmin(@Valid @RequestBody ReqContractAdminInfo reqContractAdminInfo) {
    if (baseService.queryExecEnvIsWasm(reqContractAdminInfo.getGroupId())) {
      return new BaseResponse(ConstantCode.EXEC_ENV_IS_WASM);
    }
    return new BaseResponse(ConstantCode.SUCCESS, everyoneService.queryAdmin(reqContractAdminInfo));
  }

  /**
   * 查询合约是否可用（被冻结）
   */
  @ApiOperation(value = "query the contract status")
  @ApiImplicitParam(name = "reqContractStatus", value = "contractAdmin info", required = true,
      dataType = "ReqContractAdminInfo")
  @PostMapping("contract/status")
  public Object isContractAvailable(@Valid @RequestBody ReqContractAdminInfo reqContractStatus) {
    if (baseService.queryExecEnvIsWasm(reqContractStatus.getGroupId())) {
      return new BaseResponse(ConstantCode.EXEC_ENV_IS_WASM);
    }
    return new BaseResponse(ConstantCode.SUCCESS, everyoneService.isContractAvailable(reqContractStatus));
  }

  /**
   * 查询合约是否可用（被冻结）
   */
  @ApiOperation(value = "query the contract status")
  @ApiImplicitParam(name = "reqContractStatus", value = "contractAdmin info", required = true,
      dataType = "ReqContractAdminInfo")
  @PostMapping("contract/status/list")
  public Object isContractAvailable(@Valid @RequestBody ReqContractStatusList reqContractStatus) {
    if (baseService.queryExecEnvIsWasm(reqContractStatus.getGroupId())) {
      return new BaseResponse(ConstantCode.EXEC_ENV_IS_WASM);
    }
    return new BaseResponse(ConstantCode.SUCCESS, everyoneService.listContractStatus(reqContractStatus));
  }

  /**
   * 查询某用户地址对合约函数的访问是否有权限
   */
  @ApiOperation(value = "query the userAddress permission of contract")
  @ApiImplicitParam(name = "reqCheckMethodAuthInfo", value = "contractAdmin info", required = true,
      dataType = "ReqCheckMethodAuthInfo")
  @PostMapping("contract/method/auth")
  public Object checkMethodAuth(@Valid @RequestBody ReqCheckMethodAuthInfo reqCheckMethodAuthInfo) {
    if (baseService.queryExecEnvIsWasm(reqCheckMethodAuthInfo.getGroupId())) {
      return new BaseResponse(ConstantCode.EXEC_ENV_IS_WASM);
    }
    if (!baseService.queryChainHasAuth(reqCheckMethodAuthInfo.getGroupId())) {
      return new BaseResponse(ConstantCode.CHAIN_AUTH_NOT_ENABLE);
    }
    return new BaseResponse(ConstantCode.SUCCESS,
        everyoneService.checkMethodAuth(reqCheckMethodAuthInfo));
  }

}



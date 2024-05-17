package com.webank.webase.node.mgr.precntauth.precompiled.cns;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.webank.common.log.annotation.Log;
import com.webank.common.log.enums.BusinessType;
import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.entity.BaseResponse;
import com.webank.webase.node.mgr.config.properties.ConstantProperties;
import com.webank.webase.node.mgr.precntauth.precompiled.cns.entity.ReqCnsInfoByName;
import com.webank.webase.node.mgr.precntauth.precompiled.cns.entity.ReqInfoByNameVersion;
import com.webank.webase.node.mgr.precntauth.precompiled.cns.entity.ReqRegisterCnsInfo;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

//@Api(value = "precntauth/precompiled/cns", tags = "precntauth precompiled controller")
@Tag(name="CNS管理")
@SaCheckPermission("bcos3:contract:cnsManagement")
@Slf4j
@RestController
@RequestMapping(value = "precntauth/precompiled/cns")
public class CNSController {

  @Autowired
  private CNSServiceInWebase cnsServiceInWebase;

//  @ApiOperation(value = "register the cns info")
//  @ApiImplicitParam(name = "reqCnsInfo", value = "register info", required = true, dataType = "ReqRegisterCnsInfo")
  @Log(title = "BCOS3/合约管理/CNS", businessType = BusinessType.INSERT)
  @PostMapping("register")
  @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN_OR_DEVELOPER)
  public Object registerCNS(@Valid @RequestBody ReqRegisterCnsInfo reqCnsInfo)
      {
    return new BaseResponse(ConstantCode.SUCCESS, cnsServiceInWebase.registerCNS(reqCnsInfo));
  }

//  @ApiOperation(value = "query the cns info by name")
//  @ApiImplicitParam(name = "reqCnsInfoByName", value = "queryCns info", required = true, dataType = "ReqCnsInfoByName")
  @PostMapping("queryCnsByName")
  public Object queryCnsInfoByName(@Valid @RequestBody ReqCnsInfoByName reqCnsInfoByName) {
    return new BaseResponse(ConstantCode.SUCCESS,
        cnsServiceInWebase.queryCnsInfoByName(reqCnsInfoByName));
  }

//  @ApiOperation(value = "query the cns info by name version")
//  @ApiImplicitParam(name = "reqCnsInfoByNameVersion", value = "name and version info", required = true, dataType = "ReqInfoByNameVersion")
  @PostMapping("queryCnsByNameVersion")
  public Object queryCnsByNameVersion(
      @Valid @RequestBody ReqInfoByNameVersion reqCnsInfoByNameVersion) {
    return new BaseResponse(ConstantCode.SUCCESS,
        cnsServiceInWebase.queryCnsByNameAndVersion(reqCnsInfoByNameVersion));
  }

//  @ApiOperation(value = "query the address info by name version")
//  @ApiImplicitParam(name = "reqAddressInfoByNameVersion", value = "name and version info", required = true, dataType = "ReqInfoByNameVersion")
  @PostMapping("reqAddressInfoByNameVersion")
  @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN_OR_DEVELOPER)
  public Object queryAddressByNameVersion(
      @Valid @RequestBody ReqInfoByNameVersion reqAddressInfoByNameVersion)
      {
    return new BaseResponse(ConstantCode.SUCCESS,
        cnsServiceInWebase.getAddressByContractNameAndVersion(reqAddressInfoByNameVersion));
  }

}

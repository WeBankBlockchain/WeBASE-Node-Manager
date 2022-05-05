package com.webank.webase.node.mgr.precntauth.precompiled.cns;

import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.entity.BaseResponse;
import com.webank.webase.node.mgr.precntauth.precompiled.cns.entity.ReqCnsInfoByName;
import com.webank.webase.node.mgr.precntauth.precompiled.cns.entity.ReqInfoByNameVersion;
import com.webank.webase.node.mgr.precntauth.precompiled.cns.entity.ReqRegisterCnsInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.sdk.transaction.model.exception.ContractException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(value = "precntauth/precompiled/cns", tags = "precntauth precompiled controller")
@Slf4j
@RestController
@RequestMapping(value = "precntauth/precompiled/cns")
public class CNSController {

  @Autowired
  private CNSServiceInWebase cnsServiceInWebase;

  @ApiOperation(value = "register the cns info")
  @ApiImplicitParam(name = "reqCnsInfo", value = "register info", required = true, dataType = "ReqRegisterCnsInfo")
  @PostMapping("register")
  public Object registerCNS(@Valid @RequestBody ReqRegisterCnsInfo reqCnsInfo)
      throws ContractException {
    return cnsServiceInWebase.registerCNS(reqCnsInfo);
  }

  @ApiOperation(value = "query the cns info by name")
  @ApiImplicitParam(name = "reqCnsInfoByName", value = "queryCns info", required = true, dataType = "ReqCnsInfoByName")
  @PostMapping("queryCnsByName")
  public Object queryCnsInfoByName(@Valid @RequestBody ReqCnsInfoByName reqCnsInfoByName) {
    return new BaseResponse(ConstantCode.SUCCESS,
        cnsServiceInWebase.queryCnsInfoByName(reqCnsInfoByName));
  }

  @ApiOperation(value = "query the cns info by name version")
  @ApiImplicitParam(name = "reqCnsInfoByNameVersion", value = "name and version info", required = true, dataType = "ReqInfoByNameVersion")
  @PostMapping("queryCnsByNameVersion")
  public Object queryCnsByNameVersion(
      @Valid @RequestBody ReqInfoByNameVersion reqCnsInfoByNameVersion) {
    return new BaseResponse(ConstantCode.SUCCESS,
        cnsServiceInWebase.queryCnsByNameAndVersion(reqCnsInfoByNameVersion));
  }

  @ApiOperation(value = "query the address info by name version")
  @ApiImplicitParam(name = "reqAddressInfoByNameVersion", value = "name and version info", required = true, dataType = "ReqInfoByNameVersion")
  @PostMapping("reqAddressInfoByNameVersion")
  public Object queryAddressByNameVersion(
      @Valid @RequestBody ReqInfoByNameVersion reqAddressInfoByNameVersion)
      throws ContractException {
    return new BaseResponse(ConstantCode.SUCCESS,
        cnsServiceInWebase.getAddressByContractNameAndVersion(reqAddressInfoByNameVersion));
  }

}

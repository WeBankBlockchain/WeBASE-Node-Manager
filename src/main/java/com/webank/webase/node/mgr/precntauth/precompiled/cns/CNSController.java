package com.webank.webase.node.mgr.precntauth.precompiled.cns;

import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.entity.BaseResponse;
import com.webank.webase.node.mgr.precntauth.precompiled.cns.entity.ReqCnsInfoByName;
import com.webank.webase.node.mgr.precntauth.precompiled.cns.entity.ReqInfoByNameVersion;
import com.webank.webase.node.mgr.precntauth.precompiled.cns.entity.ReqRegisterCnsInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

//@Api(value = "precntauth/precompiled/cns", tags = "precntauth precompiled controller")
@Slf4j
@RestController
@RequestMapping(value = "precntauth/precompiled/cns")
public class CNSController {

  @Autowired
  private CNSServiceInWebase cnsServiceInWebase;

//  @ApiOperation(value = "register the cns info")
//  @ApiImplicitParam(name = "reqCnsInfo", value = "register info", required = true, dataType = "ReqRegisterCnsInfo")
  @PostMapping("register")
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
  public Object queryAddressByNameVersion(
      @Valid @RequestBody ReqInfoByNameVersion reqAddressInfoByNameVersion)
      {
    return new BaseResponse(ConstantCode.SUCCESS,
        cnsServiceInWebase.getAddressByContractNameAndVersion(reqAddressInfoByNameVersion));
  }

}

package com.webank.webase.node.mgr.precntauth.precompiled.bfs;

import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.entity.BaseResponse;
import com.webank.webase.node.mgr.precntauth.precompiled.bfs.entity.ReqCreateBFSInfo;
import com.webank.webase.node.mgr.precntauth.precompiled.bfs.entity.ReqQueryBFSInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.sdk.v3.transaction.model.exception.ContractException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(value = "precntauth/precompiled/bfs", tags = "precntauth precompiled controller")
@Slf4j
@RestController
@RequestMapping(value = "precntauth/precompiled/bfs")
public class BFSController {

  @Autowired
  private BFSServiceInWebase bfsServiceInWebase;

  /**
   * 创建BFS路径 eg:/apps/test
   */
  @ApiOperation(value = "create bfs path")
  @ApiImplicitParam(name = "reqCreateBFSInfo", value = "create bfs path info", required = true, dataType = "ReqCreateBFSInfo")
  @PostMapping("create")
  public Object createBfsPath(@Valid @RequestBody ReqCreateBFSInfo reqCreateBFSInfo)
      {
    return bfsServiceInWebase.createPath(reqCreateBFSInfo);
  }

  /**
   * 查询BFS目录信息 eg:/apps
   */
  @ApiOperation(value = "query the bfs path")
  @ApiImplicitParam(name = "reqQueryBFSInfo", value = "query bfs path info", required = true, dataType = "ReqQueryBFSInfo")
  @PostMapping("query")
  public Object queryBfsPath(@Valid @RequestBody ReqQueryBFSInfo reqQueryBFSInfo)
      {
    return new BaseResponse(ConstantCode.SUCCESS, bfsServiceInWebase.queryPath(reqQueryBFSInfo));
  }

}

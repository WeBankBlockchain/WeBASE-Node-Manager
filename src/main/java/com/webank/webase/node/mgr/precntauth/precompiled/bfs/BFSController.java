package com.webank.webase.node.mgr.precntauth.precompiled.bfs;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.qctc.common.log.annotation.Log;
import com.qctc.common.log.enums.BusinessType;
import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.entity.BaseResponse;
import com.webank.webase.node.mgr.config.properties.ConstantProperties;
import com.webank.webase.node.mgr.precntauth.precompiled.bfs.entity.ReqCreateBFSInfo;
import com.webank.webase.node.mgr.precntauth.precompiled.bfs.entity.ReqQueryBFSInfo;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Tag(name="BFS管理")
//@Api(value = "precntauth/precompiled/bfs", tags = "precntauth precompiled controller")
@Slf4j
@RestController
@RequestMapping(value = "precntauth/precompiled/bfs")
@SaCheckPermission("bcos3:contract:BFS")
public class BFSController {

  @Autowired
  private BFSServiceInWebase bfsServiceInWebase;

  /**
   * 创建BFS路径 eg:/apps/test
   */
//  @ApiOperation(value = "create bfs path")
//  @ApiImplicitParam(name = "reqCreateBFSInfo", value = "create bfs path info", required = true, dataType = "ReqCreateBFSInfo")
  @Log(title = "BCOS3/合约管理/BFS", businessType = BusinessType.INSERT)
  @PostMapping("create")
  @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN_OR_DEVELOPER)
  public Object createBfsPath(@Valid @RequestBody ReqCreateBFSInfo reqCreateBFSInfo)
      {
    return bfsServiceInWebase.createPath(reqCreateBFSInfo);
  }

  /**
   * 查询BFS目录信息 eg:/apps
   */
//  @ApiOperation(value = "query the bfs path")
//  @ApiImplicitParam(name = "reqQueryBFSInfo", value = "query bfs path info", required = true, dataType = "ReqQueryBFSInfo")
  @PostMapping("query")
  public Object queryBfsPath(@Valid @RequestBody ReqQueryBFSInfo reqQueryBFSInfo)
      {
    return new BaseResponse(ConstantCode.SUCCESS, bfsServiceInWebase.queryPath(reqQueryBFSInfo));
  }

}

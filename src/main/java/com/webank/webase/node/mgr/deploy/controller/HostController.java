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
package com.webank.webase.node.mgr.deploy.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.webank.common.log.annotation.Log;
import com.webank.common.log.enums.BusinessType;
import com.webank.host.api.RemoteHostService;
import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.controller.BaseController;
import com.webank.webase.node.mgr.base.entity.BaseResponse;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.deploy.entity.ReqAddHost;
import com.webank.webase.node.mgr.deploy.entity.ReqCheckHost;
import com.webank.webase.node.mgr.deploy.service.AnsibleService;
import com.webank.webase.node.mgr.deploy.service.HostService;
import com.webank.webase.node.mgr.tools.JsonTools;
import com.webank.webase.node.mgr.tools.ValidateUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.io.IOException;
import java.time.Instant;

/**
 * add host, delete host, init host
 */
@Tag(name="主机相关接口")
@Log4j2
@RestController
@RequestMapping("host")
@SaCheckPermission("bcos3:chain:initHost")
public class HostController extends BaseController {
//    @Autowired
//    private TbHostMapper tbHostMapper;
    @Autowired
    private HostService hostService;
    @Autowired
    private AnsibleService ansibleService;

    @DubboReference
    private RemoteHostService remoteHostService;

    /**
     * list added host
     * @return
     * @throws IOException
     */
//    @GetMapping(value = "list")
//    public BaseResponse listHost() throws IOException {
//        Instant startTime = Instant.now();
//        log.info("Start get host list info, now:[{}]",  startTime);
//        List<TbHost> resList = this.tbHostMapper.selectAll();
//        return new BaseResponse(ConstantCode.SUCCESS, resList);
//    }
//
//    /**
//     * Deploy by ipconf and tagId.
//     */
//    @PostMapping(value = "add")
//    // TODO:  使用sa-token鉴权(ConstantProperties.HAS_ROLE_ADMIN)
//    public BaseResponse addHost(@RequestBody @Valid ReqAddHost reqAddHost, BindingResult result) throws NodeMgrException {
//        checkBindResult(result);
//
//        if(!ValidateUtil.ipv4Valid(reqAddHost.getSshIp())) {
//            log.error("not valid ip!:{}", reqAddHost.getSshIp());
//            throw new NodeMgrException(ConstantCode.IP_FORMAT_ERROR);
//        }
//
//        if (!reqAddHost.getRootDir().startsWith("/")) {
//            log.error("not valid rootDir, must be absolute path!:{}", reqAddHost.getRootDir());
//            throw new NodeMgrException(ConstantCode.HOST_DIR_REQUIRE_ABSOLUTE);
//        }
//
//        Instant startTime = Instant.now();
//        log.info("Start addHost:[{}], start:[{}]", JsonTools.toJSONString(reqAddHost), startTime);
//        try {
//            // save host info
//            hostService.checkDirAndInsert(reqAddHost.getSshIp(), reqAddHost.getRootDir(), HostStatusEnum.ADDED,  "");
//
//            return new BaseResponse(ConstantCode.SUCCESS);
//        } catch (NodeMgrException e) {
//            return new BaseResponse(e.getRetCode());
//        }
//    }
//
//     /**
//     * Delete host without node(front)
//     */
//    @DeleteMapping("/{hostId}")
//    // TODO:  使用sa-token鉴权(ConstantProperties.HAS_ROLE_ADMIN)
//    public BaseResponse deleteHostWithout(@PathVariable("hostId") Integer hostId) throws NodeMgrException {
//        Instant startTime = Instant.now();
//        log.info("Start deleteHost hostId:[{}], start:[{}]", hostId, startTime);
//        try {
//            hostService.deleteHostWithoutNode(hostId);
//            return new BaseResponse(ConstantCode.SUCCESS);
//        } catch (NodeMgrException e) {
//            return new BaseResponse(e.getRetCode());
//        }
//    }

    /**
     * Deploy by ipconf and tagId.
     */
    //@Log(title = "BCOS3/节点管理", businessType = BusinessType.OTHER)
    @PostMapping(value = "ping")
    public BaseResponse pingHost(@RequestBody @Valid ReqAddHost reqAddHost, BindingResult result) throws NodeMgrException {
        checkBindResult(result);

        if(!ValidateUtil.ipv4Valid(reqAddHost.getSshIp())) {
            log.error("not valid ip!:{}", reqAddHost.getSshIp());
            throw new NodeMgrException(ConstantCode.IP_FORMAT_ERROR);
        }
        Instant startTime = Instant.now();
        log.info("Start ping:[{}], start:[{}]", JsonTools.toJSONString(reqAddHost), startTime);
        try {
            // check before add
            ansibleService.execPing(remoteHostService.getHostByIp(reqAddHost.getSshIp()));
            return new BaseResponse(ConstantCode.SUCCESS);
        } catch (NodeMgrException e) {
            return new BaseResponse(e.getRetCode());
        }
    }


    /**
     * check mem/cpu and docker dependency
     */
    //@Log(title = "BCOS3/节点管理", businessType = BusinessType.OTHER)
    @PostMapping(value = "check")
    // TODO:  使用sa-token鉴权(ConstantProperties.HAS_ROLE_ADMIN)
    public BaseResponse checkHostList(@RequestBody @Valid ReqCheckHost reqCheckHost,
        BindingResult result) throws NodeMgrException {
        checkBindResult(result);
        Instant startTime = Instant.now();
        log.info("Start check:[{}], start:[{}]", JsonTools.toJSONString(reqCheckHost), startTime);

        try {
            // check port and  check docker
            boolean checkStatus = this.hostService.batchCheckHostList(reqCheckHost.getHostIdList());
            if (!checkStatus) {
                return new BaseResponse(ConstantCode.CHECK_HOST_MEM_CPU_DOCKER_FAIL);
            }
            return new BaseResponse(ConstantCode.SUCCESS);
        } catch (NodeMgrException e) {
            return new BaseResponse(e.getRetCode());
        } catch (InterruptedException e) {
            log.error("Error check ex:", e);
            Thread.currentThread().interrupt();
            throw new NodeMgrException(ConstantCode.EXEC_CHECK_SCRIPT_INTERRUPT);
        }
    }

    /**
     * check ansible installed
     */
    //@Log(title = "BCOS3/节点管理", businessType = BusinessType.OTHER)
    @PostMapping(value = "ansible")
    // TODO:  使用sa-token鉴权(ConstantProperties.HAS_ROLE_ADMIN)
    public BaseResponse checkAnsibleInstalled() throws NodeMgrException {
        Instant startTime = Instant.now();
        log.info("Start checkAnsibleInstalled start:[{}]", startTime);
        ansibleService.checkAnsible();
        return new BaseResponse(ConstantCode.SUCCESS);
    }


}

/**
 * Copyright 2014-2020  the original author or authors.
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

import java.time.Instant;

import javax.validation.Valid;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.webank.webase.node.mgr.base.code.RetCode;
import com.webank.webase.node.mgr.base.controller.BaseController;
import com.webank.webase.node.mgr.base.entity.BaseResponse;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.deploy.entity.ReqDeploy;
import com.webank.webase.node.mgr.deploy.service.DeployService;

import lombok.extern.log4j.Log4j2;

/**
 * Controller for node data.
 */
@Log4j2
@RestController
@RequestMapping("deploy")
public class DeployController extends BaseController {

    @Autowired private DeployService deployService;

    /**
     * Deploy by ipconf and tagId.
     */
    @PostMapping(value = "init")
    public BaseResponse deploy(@RequestBody @Valid ReqDeploy deploy,
                               BindingResult result) throws NodeMgrException {
        checkBindResult(result);
        Instant startTime = Instant.now();
        log.info("start deploy chainName:[{}], rootDirOnHost:[{}] startTime:[{}], tagId:[{}], ipconf:[{}]",
                deploy.getChainName(), deploy.getRootDirOnHost(), startTime.toEpochMilli(),
                deploy.getTagId(), deploy.getIpconf());

        Pair<RetCode, String> deployResult = this.deployService.deploy(deploy.getChainName(),
                deploy.getIpconf(), deploy.getTagId(), deploy.getRootDirOnHost());
        return new BaseResponse(deployResult.getKey(), deployResult.getValue());
    }

    /**
     * @param ip         Host runs new node, maybe a new host.
     * @param agencyName If host ip is new one, agency name should not be null.
     * @param chainName  If agency name is a new one, chain name should not be null.
     * @param num        Count of new nodes , default is 1.
     * @return
     */
    @PostMapping(value = "add")
    public BaseResponse add(
            @RequestParam(value = "ip", required = true) String ip,
            @RequestParam(value = "agencyName", required = false, defaultValue = "") String agencyName,
            @RequestParam(value = "num", required = false, defaultValue = "1") int num,
            @RequestParam(value = "chainName", required = false, defaultValue = "default_chain") String chainName
    ) {
        Instant startTime = Instant.now();
        log.info("start add node ip:[{}], agencyName:[{}], num:[{}], chainName:[{}], now:[{}]",
                ip, agencyName, num, chainName, startTime);

        Pair<RetCode, String> addResult = this.deployService.add(chainName, ip, agencyName, num);
        return new BaseResponse(addResult.getKey(), addResult.getValue());
    }

    /**
     * delete chain by chainName.
     */
    @DeleteMapping(value = "delete")
    public BaseResponse delete(
            @RequestParam(value = "chainName", required = false, defaultValue = "default_chain") String chainName
    ) throws NodeMgrException {
        Instant startTime = Instant.now();
        log.info("start delete chainName:[{}], startTime:[{}]",
                chainName, startTime.toEpochMilli());

        RetCode deleteResult = this.deployService.delete(chainName);
        return new BaseResponse(deleteResult);
    }

}

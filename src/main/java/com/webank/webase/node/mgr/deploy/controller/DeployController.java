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

import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.code.RetCode;
import com.webank.webase.node.mgr.base.controller.BaseController;
import com.webank.webase.node.mgr.base.entity.BaseResponse;
import com.webank.webase.node.mgr.base.enums.FrontStatusEnum;
import com.webank.webase.node.mgr.base.enums.OptionType;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.base.properties.ConstantProperties;
import com.webank.webase.node.mgr.base.tools.JsonTools;
import com.webank.webase.node.mgr.base.tools.ProgressTools;
import com.webank.webase.node.mgr.deploy.entity.ReqAddNode;
import com.webank.webase.node.mgr.deploy.entity.ReqConfigChain;
import com.webank.webase.node.mgr.deploy.entity.ReqInitHost;
import com.webank.webase.node.mgr.deploy.entity.ReqNodeOption;
import com.webank.webase.node.mgr.deploy.entity.ReqUpgrade;
import com.webank.webase.node.mgr.deploy.entity.TbChain;
import com.webank.webase.node.mgr.deploy.entity.TbHost;
import com.webank.webase.node.mgr.deploy.mapper.TbChainMapper;
import com.webank.webase.node.mgr.deploy.mapper.TbHostMapper;
import com.webank.webase.node.mgr.deploy.service.DeployService;
import com.webank.webase.node.mgr.deploy.service.HostService;
import com.webank.webase.node.mgr.scheduler.ResetGroupListTask;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import javax.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for node data.
 */
@Log4j2
@RestController
@RequestMapping("deploy")
public class DeployController extends BaseController {

    @Autowired private TbChainMapper tbChainMapper;
    @Autowired private TbHostMapper tbHostMapper;

    @Autowired private DeployService deployService;
    @Autowired private HostService hostService;
    @Autowired private ResetGroupListTask resetGroupListTask;
    @Autowired private ConstantProperties constantProperties;

    /**
     *  @check:(in /host/check)
     *  a. check docker and cpu/mem
     *  @init:
     *  a. check node port(channel, front, rpc, p2p)
     *  b. init host docker & dependency
     *  @config
     *  c. generate chain config & scp config
     */

    @PostMapping(value = "init")
    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN)
    public BaseResponse initHostList(@RequestBody @Valid ReqInitHost reqInitHost,
                               BindingResult result) throws NodeMgrException {
        checkBindResult(result);
        Instant startTime = Instant.now();
        log.info("Start initHostList:[{}], start:[{}]", JsonTools.toJSONString(reqInitHost), startTime);

        try {
            // generate node config and return shell execution log
            hostService.initHostAndDocker(reqInitHost.getChainName(), reqInitHost.getImageTag(), reqInitHost.getHostIdList(),
                reqInitHost.getDockerImageType());
            return new BaseResponse(ConstantCode.SUCCESS);
        } catch (NodeMgrException e) {
            return new BaseResponse(e.getRetCode());
        }
    }

    /**
     * check init result and update if init time out
     * @param reqInitHost
     * @param result
     * @return
     * @throws NodeMgrException
     */
    @PostMapping(value = "initCheck")
    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN)
    public BaseResponse initCheckHostList(@RequestBody @Valid ReqInitHost reqInitHost, BindingResult result)
        throws NodeMgrException {
        checkBindResult(result);
        Instant startTime = Instant.now();
        log.info("Start initCheckHostList:[{}], start:[{}]", JsonTools.toJSONString(reqInitHost), startTime);

        try {
            // generate node config and return shell execution log
            List<TbHost> hostList = hostService.checkInitAndListHost(reqInitHost.getHostIdList());
            return new BaseResponse(ConstantCode.SUCCESS, hostList);
        } catch (NodeMgrException e) {
            return new BaseResponse(e.getRetCode());
        }
    }

    /**
     * config chain and init db data an async start chain
     * @param deploy
     * @param result
     * @return
     * @throws NodeMgrException
     */
    @PostMapping(value = "config")
    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN)
    public BaseResponse configChainAndHost(@RequestBody @Valid ReqConfigChain deploy,
                               BindingResult result) throws NodeMgrException {
        checkBindResult(result);
        Instant startTime = Instant.now();
        deploy.setWebaseSignAddr(constantProperties.getWebaseSignAddress());
        log.info("Start configChainAndHost:[{}], start:[{}]", JsonTools.toJSONString(deploy), startTime);

        try {
            // hostService.checkPortHostList(deploy.getDeployNodeInfoList());

            // generate node config and return shell execution log
            deployService.configChainAndScp(deploy.getChainName(), deploy.getDeployNodeInfoList(),
                    deploy.getIpconf(), deploy.getImageTag(), deploy.getEncryptType(),
                    deploy.getWebaseSignAddr(), deploy.getAgencyName());
            return new BaseResponse(ConstantCode.SUCCESS);
        } catch (NodeMgrException e) {
            return new BaseResponse(e.getRetCode());
        } catch (InterruptedException e) {
            throw new NodeMgrException(ConstantCode.EXEC_CHECK_SCRIPT_INTERRUPT);
        }
    }

    /**
     * check host's node's port, include channel p2p rpc front port
     * @param checkPort
     * @param result
     * @return
     * @throws NodeMgrException
     */
    @PostMapping(value = "checkPort")
    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN)
    public BaseResponse checkNodePort(@RequestBody @Valid ReqConfigChain checkPort,
                               BindingResult result) throws NodeMgrException {
        checkBindResult(result);
        Instant startTime = Instant.now();
        log.info("Start checkNodePort:[{}], start:[{}]", JsonTools.toJSONString(checkPort.getDeployNodeInfoList()), startTime);

        try {
            // generate node config and return shell execution log
            // boolean checkPortRes = hostService.checkPortHostList(checkPort.getDeployNodeInfoList());
            boolean checkPortRes = hostService.syncCheckPortHostList(checkPort.getDeployNodeInfoList());
            if (!checkPortRes) {
                return new BaseResponse(ConstantCode.CHECK_HOST_PORT_IN_USE);
            }
            return new BaseResponse(ConstantCode.SUCCESS, checkPortRes);
        } catch (NodeMgrException e) {
            return new BaseResponse(e.getRetCode());
        }
    }

    /**
     * 扩容一个节点：配置节点、更新其他节点配置、启动新节点（重启旧节点）
     * @param addNode
     * @param result
     * @return
     * @throws NodeMgrException
     */
    @PostMapping(value = "node/add")
    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN)
    public BaseResponse addNode(
            @RequestBody @Valid ReqAddNode addNode,
            BindingResult result) throws NodeMgrException {
        checkBindResult(result);
        Instant startTime = Instant.now();

        log.info("Start add node configNew:[{}] , start[{}]", JsonTools.toJSONString(addNode), startTime);

        Pair<RetCode, String> addResult = this.deployService.addNodes(addNode);
        return new BaseResponse(addResult.getKey(), addResult.getValue());
    }

    /**
     *
     * @param start
     * @param result
     * @return
     * @throws NodeMgrException
     */
    @PostMapping(value = "node/start")
    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN)
    public BaseResponse startNode(
            @RequestBody @Valid ReqNodeOption start, BindingResult result) throws NodeMgrException {
        checkBindResult(result);
        String nodeId = start.getNodeId();
        Instant startTime = Instant.now();

        log.info("Start node nodeId:[{}], now:[{}]", nodeId, startTime);

        this.deployService.startNode(start.getNodeId(), OptionType.MODIFY_CHAIN, FrontStatusEnum.STOPPED,
                FrontStatusEnum.RUNNING, FrontStatusEnum.STOPPED);
        return new BaseResponse(ConstantCode.SUCCESS);
    }

    /**
     *
     * @param stop
     * @param result
     * @return
     * @throws NodeMgrException
     */
    @PostMapping(value = "node/stop")
    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN)
    public BaseResponse stopNode(
            @RequestBody @Valid ReqNodeOption stop, BindingResult result) throws NodeMgrException {
        checkBindResult(result);
        String nodeId = stop.getNodeId();
        Instant startTime = Instant.now();

        log.info("Stop node nodeId:[{}], now:[{}]", nodeId, startTime);

        this.deployService.stopNode(stop.getNodeId());
        return new BaseResponse(ConstantCode.SUCCESS);
    }

    /**
     * todo update related node by db's config value with template when delete node
     * @param delete
     * @param result
     * @return
     * @throws NodeMgrException
     */
    @PostMapping(value = "node/delete")
    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN)
    public BaseResponse deleteNode(
            @RequestBody @Valid ReqNodeOption delete, BindingResult result) throws NodeMgrException {
        checkBindResult(result);
        String nodeId = delete.getNodeId();
        Instant startTime = Instant.now();

        log.info("Delete node delete:[{}], now:[{}]", delete, startTime);

        this.deployService.deleteNode(nodeId);
        return new BaseResponse(ConstantCode.SUCCESS);
    }

    /**
     *
     * @param upgrade
     * @param result
     * @return
     * @throws IOException
     */
    @PostMapping(value = "upgrade")
    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN)
    public BaseResponse upgradeChain(
            @RequestBody @Valid ReqUpgrade upgrade, BindingResult result ) throws IOException {
        checkBindResult(result);
        int newTagId = upgrade.getNewTagId();
        String chainName = upgrade.getChainName();
        Instant startTime = Instant.now();
        log.info("Start upgrade chain to version:[{}], chainName:[{}], now:[{}]", newTagId, chainName, startTime);
        this.deployService.upgrade(newTagId,chainName);
        return new BaseResponse(ConstantCode.SUCCESS);
    }

    /**
     * 1-检测机器内存与依赖，2-检测Docker服务，3-检测端口占用，4-初始化安装主机依赖，5-初始化加载Docker镜像中
     * 6-生成链证书与配置，7-初始化链与前置数据，8-传输链配置到主机
     * 9-配置完成，启动中
     * @return
     * @throws IOException
     */
    @GetMapping(value = "progress")
    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN)
    public BaseResponse progress() throws IOException {
        Instant startTime = Instant.now();
        log.info("Start get progress now:[{}]", startTime);
        int progress = ProgressTools.progress();
        return new BaseResponse(ConstantCode.SUCCESS, progress);
    }

    /**
     *
     * @param chainName
     * @return
     * @throws IOException
     */
    @GetMapping(value = "chain/info")
    public BaseResponse getChain(
            @RequestParam(value = "chainName", required = false, defaultValue = "default_chain") String chainName
    ) throws IOException {
        Instant startTime = Instant.now();
        log.info("Start get chain info chainName:[{}], now:[{}]", chainName, startTime);

        TbChain chain = this.tbChainMapper.getByChainName(chainName);
        return new BaseResponse(ConstantCode.SUCCESS, chain);
    }


    /**
     *
     * @param chainName
     * @return
     * @throws IOException
     */
    @GetMapping(value = "chain/start")
    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN)
    public BaseResponse startChain(
            @RequestParam(value = "chainName", required = false, defaultValue = "default_chain") String chainName) {
        Instant startTime = Instant.now();
        log.info("Start chain, chainName:[{}], now:[{}]", chainName, startTime);
        deployService.startChain(chainName, OptionType.DEPLOY_CHAIN);
        return new BaseResponse(ConstantCode.SUCCESS );
    }

    /**
     *
     * @param chainName
     * @return
     * @throws IOException
     */
    @GetMapping(value = "chain/stop")
    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN)
    public BaseResponse stopChain(
            @RequestParam(value = "chainName", required = false, defaultValue = "default_chain") String chainName
    ) throws IOException {
        Instant startTime = Instant.now();
        log.info("Stop chain, chainName:[{}], now:[{}]", chainName, startTime);

        return new BaseResponse(ConstantCode.SUCCESS);
    }

    /**
     * delete chain by chainName.
     */
    @DeleteMapping(value = "delete")
    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN)
    public BaseResponse deleteChain(
            @RequestParam(value = "chainName", required = false, defaultValue = "default_chain") String chainName
    ) throws NodeMgrException {
        Instant startTime = Instant.now();
        log.info("Start delete chainName:[{}], startTime:[{}]",
                chainName, startTime.toEpochMilli());
        // include delete chain files and stop node/front docker container(on remote host), delete chain file locally too
        RetCode deleteResult = this.deployService.deleteChain(chainName);
        return new BaseResponse(deleteResult);
    }

    /**
     *
     * @return
     * @throws IOException
     */
    @GetMapping(value = "type")
    public BaseResponse deployType() throws IOException {
        Instant startTime = Instant.now();
        log.info("Start get deploy type, now:[{}]",  startTime);
        return new BaseResponse(ConstantCode.SUCCESS, constantProperties.getDeployType());
    }

//    /**
//     *
//     * @return
//     * @throws IOException
//     */
//    @GetMapping(value = "host/list")
//    public BaseResponse listHost() throws IOException {
//        Instant startTime = Instant.now();
//        log.info("Start get host list info, now:[{}]",  startTime);
//        return new BaseResponse(ConstantCode.SUCCESS, this.tbHostMapper.selectAll());
//    }
}

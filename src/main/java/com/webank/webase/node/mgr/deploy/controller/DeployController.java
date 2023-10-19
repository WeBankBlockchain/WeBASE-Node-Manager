/**
 * Copyright 2014-2021  the original author or authors.
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
import com.qctc.common.log.annotation.Log;
import com.qctc.common.log.enums.BusinessType;
import com.qctc.host.api.model.HostDTO;
import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.code.RetCode;
import com.webank.webase.node.mgr.base.controller.BaseController;
import com.webank.webase.node.mgr.base.entity.BaseResponse;
import com.webank.webase.node.mgr.base.enums.FrontStatusEnum;
import com.webank.webase.node.mgr.base.enums.OptionType;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.config.properties.ConstantProperties;
import com.webank.webase.node.mgr.deploy.entity.*;
import com.webank.webase.node.mgr.deploy.mapper.TbChainMapper;
import com.webank.webase.node.mgr.deploy.service.DeployService;
import com.webank.webase.node.mgr.deploy.service.HostService;
import com.webank.webase.node.mgr.tools.JsonTools;
import com.webank.webase.node.mgr.tools.ProgressTools;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Controller for node data.
 */
@Tag(name="链、节点部署")
@Log4j2
@RestController
@RequestMapping("deploy")
public class DeployController extends BaseController {

    @Autowired private TbChainMapper tbChainMapper;

    @Autowired private DeployService deployService;
    @Autowired private HostService hostService;
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

    @Log(title = "BCOS3/节点管理", businessType = BusinessType.INSERT)
    @SaCheckPermission("bcos3:chain:initHost")
    @PostMapping(value = "init")
    public BaseResponse initHostList(@RequestBody @Valid ReqInitHost reqInitHost,
                               BindingResult result) throws NodeMgrException {
        checkBindResult(result);
        Instant startTime = Instant.now();
        log.info("Start initHostList:[{}], start:[{}]", JsonTools.toJSONString(reqInitHost), startTime);

        try {
            // generate node config and return shell execution log
            hostService.initHostAndDocker(reqInitHost.getChainName(), reqInitHost.getImageTag(), reqInitHost.getHostIdList(),
                reqInitHost.getDockerImageType());
            log.info("end initHostList. useTime:{}", Duration.between(startTime, Instant.now()).toMillis());
            return new BaseResponse(ConstantCode.SUCCESS);
        } catch (NodeMgrException e) {
            log.error("initHostList error:[]", e);
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
    @SaCheckPermission("bcos3:chain:checkHostInit")
    @PostMapping(value = "initCheck")
    public BaseResponse initCheckHostList(@RequestBody @Valid ReqInitHost reqInitHost, BindingResult result)
        throws NodeMgrException {
        checkBindResult(result);
        Instant startTime = Instant.now();
        log.info("Start initCheckHostList:[{}], start:[{}]", JsonTools.toJSONString(reqInitHost), startTime);

        try {
            // generate node config and return shell execution log
            List<HostDTO> hostList = hostService.checkInitAndListHost(reqInitHost.getHostIdList());
            log.info("end initCheckHostList. useTime:{}", Duration.between(startTime, Instant.now()).toMillis());
            return new BaseResponse(ConstantCode.SUCCESS, hostList);
        } catch (NodeMgrException e) {
            log.error("initHostList error:[]", e);
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
    @Log(title = "BCOS3/节点管理", businessType = BusinessType.INSERT)
    @SaCheckPermission("bcos3:chain:configChainAndHost")
    @PostMapping(value = "config")
    public BaseResponse configChainAndHost(@RequestBody @Valid ReqConfigChain deploy,
                               BindingResult result) throws NodeMgrException {
        checkBindResult(result);
        Instant startTime = Instant.now();
        deploy.setWebaseSignAddr(constantProperties.getWebaseSignAddress());
        log.info("Start configChainAndHost:[{}], start:[{}]", JsonTools.toJSONString(deploy), startTime);

        try {
            // todo 添加重复点击部署的报错提示
            // generate node config and return shell execution log
            deployService.configChainAndScp(deploy.getChainName(), deploy.getDeployNodeInfoList(),
                    deploy.getIpconf(), deploy.getImageTag(), deploy.getEncryptType(),
                    deploy.getWebaseSignAddr(), deploy.getAgencyName(), deploy.getEnableAuth());
            log.info("End configChainAndHost:[{}], usedTime:[{}]", JsonTools.toJSONString(deploy), Duration
                .between(startTime, Instant.now()).toMillis());
            return new BaseResponse(ConstantCode.SUCCESS);
        } catch (NodeMgrException e) {
            log.error("configChainAndHost error:[]", e);
            return new BaseResponse(e.getRetCode());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("configChainAndHost interrupt error:[]", e);
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
    @Log(title = "BCOS3/节点管理", businessType = BusinessType.UPDATE)
    @SaCheckPermission("bcos3:chain:checkPort")
    @PostMapping(value = "checkPort")
    public BaseResponse checkNodePort(@RequestBody @Valid ReqConfigChain checkPort,
                               BindingResult result) throws NodeMgrException {
        checkBindResult(result);
        Instant startTime = Instant.now();
        log.info("Start checkNodePort:[{}], start:[{}]", JsonTools.toJSONString(checkPort.getDeployNodeInfoList()), startTime);

        try {
            // generate node config and return shell execution log
            // boolean checkPortRes = hostService.checkPortHostList(checkPort.getDeployNodeInfoList());
            boolean checkPortRes = hostService.syncCheckPortHostList(checkPort.getDeployNodeInfoList());
            log.info("end checkNodePort. useTime:{}", Duration.between(startTime, Instant.now()).toMillis());
            if (!checkPortRes) {
                return new BaseResponse(ConstantCode.CHECK_HOST_PORT_IN_USE);
            }
            return new BaseResponse(ConstantCode.SUCCESS, checkPortRes);
        } catch (NodeMgrException e) {
            log.error("checkNodePort error:[]", e);
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
    @Log(title = "BCOS3/节点管理", businessType = BusinessType.INSERT)
    @SaCheckPermission("bcos3:chain:addNode")
    @PostMapping(value = "node/add")
    public BaseResponse addNode(
            @RequestBody @Valid ReqAddNode addNode,
            BindingResult result) throws NodeMgrException {
        checkBindResult(result);
        Instant startTime = Instant.now();

        log.info("Start addNodes configNew:[{}] , start[{}]", JsonTools.toJSONString(addNode), startTime);

        Pair<RetCode, String> addResult = null;
        try {
            addResult = this.deployService.batchAddNode(addNode);
        } catch (InterruptedException e) {
            log.error("Error addNodes interrupted ex:", e);
            Thread.currentThread().interrupt();
            throw new NodeMgrException(ConstantCode.EXEC_CHECK_SCRIPT_INTERRUPT);
        }
        log.info("End addNodes addResult:[{}], usedTime:[{}]", JsonTools.toJSONString(addResult), Duration
            .between(startTime, Instant.now()).toMillis());

        return new BaseResponse(addResult.getKey(), addResult.getValue());
    }

    /**
     * start node
     * @param start
     * @param result
     * @return
     * @throws NodeMgrException
     */
    @Log(title = "BCOS3/节点管理", businessType = BusinessType.OTHER)
    @SaCheckPermission("bcos3:chain:startNode")
    @PostMapping(value = "node/start")
    public BaseResponse startNode(
            @RequestBody @Valid ReqNodeOption start, BindingResult result) throws NodeMgrException {
        checkBindResult(result);
        String nodeId = start.getNodeId();
        Instant startTime = Instant.now();

        log.info("Start node nodeId:[{}], now:[{}]", nodeId, startTime);

        this.deployService.startNode(start.getNodeId(), OptionType.MODIFY_CHAIN, FrontStatusEnum.STOPPED,
                FrontStatusEnum.RUNNING, FrontStatusEnum.STOPPED);
        log.info("end startNode. useTime:{}", Duration.between(startTime, Instant.now()).toMillis());
        return new BaseResponse(ConstantCode.SUCCESS);
    }

    /**
     *
     * @param stop
     * @param result
     * @return
     * @throws NodeMgrException
     */
    @Log(title = "BCOS3/节点管理", businessType = BusinessType.OTHER)
    @SaCheckPermission("bcos3:chain:stopNode")
    @PostMapping(value = "node/stop")
    public BaseResponse stopNode(
            @RequestBody @Valid ReqNodeOption stop, BindingResult result) throws NodeMgrException {
        checkBindResult(result);
        String nodeId = stop.getNodeId();
        Instant startTime = Instant.now();

        log.info("Stop node nodeId:[{}], now:[{}]", nodeId, startTime);

        this.deployService.stopNode(stop.getNodeId());
        log.info("end stopNode. useTime:{}", Duration.between(startTime, Instant.now()).toMillis());
        return new BaseResponse(ConstantCode.SUCCESS);
    }

    /**
     * stop node force
     * @param stop
     * @param result
     * @return
     * @throws NodeMgrException
     */
    @Log(title = "BCOS3/节点管理", businessType = BusinessType.OTHER)
    @SaCheckPermission("bcos3:chain:stopNodeForce")
    @PostMapping(value = "node/stopForce")
    public BaseResponse stopNodeForce(
            @RequestBody @Valid ReqNodeOption stop, BindingResult result) throws NodeMgrException {
        checkBindResult(result);
        String nodeId = stop.getNodeId();
        Instant startTime = Instant.now();

        log.info("stopNodeForce nodeId:[{}], now:[{}]", nodeId, startTime);

        deployService.stopNodeForce(stop.getNodeId());
        log.info("end stopNodeForce. useTime:{}", Duration.between(startTime, Instant.now()).toMillis());
        return new BaseResponse(ConstantCode.SUCCESS);
    }

    /**
     * restart node
     * @param start
     * @param result
     * @return
     * @throws NodeMgrException
     */
    @Log(title = "BCOS3/节点管理", businessType = BusinessType.OTHER)
    @SaCheckPermission("bcos3:chain:restartNode")
    @PostMapping(value = "node/restart")
    public BaseResponse restartNode(
        @RequestBody @Valid ReqNodeOption start, BindingResult result) throws NodeMgrException {
        checkBindResult(result);
        String nodeId = start.getNodeId();
        Instant startTime = Instant.now();

        log.info("restartNode nodeId:[{}], now:[{}]", nodeId, startTime);

        this.deployService.startNode(start.getNodeId(), OptionType.MODIFY_CHAIN, FrontStatusEnum.STOPPED,
            FrontStatusEnum.RUNNING, FrontStatusEnum.STOPPED);
        log.info("end restartNode. useTime:{}", Duration.between(startTime, Instant.now()).toMillis());
        return new BaseResponse(ConstantCode.SUCCESS);
    }

    /**
     * todo update related node by db's config value with template when delete node
     * @param delete
     * @param result
     * @return
     * @throws NodeMgrException
     */
    @Log(title = "BCOS3/节点管理", businessType = BusinessType.DELETE)
    @SaCheckPermission("bcos3:chain:deleteNode")
    @PostMapping(value = "node/delete")
    public BaseResponse deleteNode(
            @RequestBody @Valid ReqNodeOption delete, BindingResult result) throws NodeMgrException {
        checkBindResult(result);
        String nodeId = delete.getNodeId();
        Instant startTime = Instant.now();

        log.info("Delete node delete:[{}], now:[{}]", delete, startTime);

        deployService.deleteNode(nodeId);
        log.info("end deleteNode. useTime:{}", Duration.between(startTime, Instant.now()).toMillis());
        return new BaseResponse(ConstantCode.SUCCESS);
    }

    /**
     *
     * @param upgrade
     * @param result
     * @return
     * @throws IOException
     */
    @Log(title = "BCOS3/节点管理", businessType = BusinessType.UPDATE)
    @SaCheckPermission("bcos3:chain:upgradeChain")
    @PostMapping(value = "upgrade")
    public BaseResponse upgradeChain(
            @RequestBody @Valid ReqUpgrade upgrade, BindingResult result ) throws IOException {
        checkBindResult(result);
        int newTagId = upgrade.getNewTagId();
        String chainName = upgrade.getChainName();
        Instant startTime = Instant.now();
        log.info("Start upgrade chain to version:[{}], chainName:[{}], now:[{}]", newTagId, chainName, startTime);
        this.deployService.upgrade(newTagId,chainName);
        log.info("end upgradeChain. useTime:{}", Duration.between(startTime, Instant.now()).toMillis());
        return new BaseResponse(ConstantCode.SUCCESS);
    }

    /**
     * 1-检测机器内存与依赖，2-检测Docker服务，3-检测端口占用，4-初始化安装主机依赖，5-初始化加载Docker镜像中
     * 6-生成链证书与配置，7-初始化链与前置数据，8-传输链配置到主机
     * 9-配置完成，启动中
     * @return
     * @throws IOException
     */
    @SaCheckPermission("bcos3:chain:progress")
    @GetMapping(value = "progress")
    public BaseResponse progress() throws IOException {
        int progress = ProgressTools.progress();
        log.debug("Start get progress status:{}", progress);
        return new BaseResponse(ConstantCode.SUCCESS, progress);
    }

    /**
     *
     * @param chainName
     * @return
     * @throws IOException
     */
    @SaCheckPermission("bcos3:chain:front")
    @GetMapping(value = "chain/info")
    public BaseResponse getChain(
            @RequestParam(value = "chainName", required = false, defaultValue = "default_chain_v3") String chainName) {
        Instant startTime = Instant.now();
        log.info("Start getChain info chainName:[{}], now:[{}]", chainName, startTime);

        TbChain chain = this.tbChainMapper.getByChainName(chainName);
        log.info("end getChain. useTime:{}", Duration.between(startTime, Instant.now()).toMillis());
        return new BaseResponse(ConstantCode.SUCCESS, chain);
    }


    /**
     *
     * @param chainName
     * @return
     * @throws IOException
     */
    @Log(title = "BCOS3/节点管理", businessType = BusinessType.OTHER)
    @SaCheckPermission("bcos3:chain:startChain")
    @GetMapping(value = "chain/start")
    public BaseResponse startChain(
            @RequestParam(value = "chainName", required = false, defaultValue = "default_chain_v3") String chainName) {
        Instant startTime = Instant.now();
        log.info("startChain, chainName:[{}], now:[{}]", chainName, startTime);
        deployService.startChain(chainName, OptionType.DEPLOY_CHAIN);
        log.info("end startChain. useTime:{}", Duration.between(startTime, Instant.now()).toMillis());
        return new BaseResponse(ConstantCode.SUCCESS );
    }

    /**
     *
     * @param chainName
     * @return
     * @throws IOException
     */
    @Log(title = "BCOS3/节点管理", businessType = BusinessType.OTHER)
    @SaCheckPermission("bcos3:chain:stopChain")
    @GetMapping(value = "chain/stop")
    public BaseResponse stopChain(
            @RequestParam(value = "chainName", required = false, defaultValue = "default_chain_v3") String chainName) {
        Instant startTime = Instant.now();
        log.info("Stop chain, chainName:[{}], now:[{}]", chainName, startTime);

        log.info("end stopChain. useTime:{}", Duration.between(startTime, Instant.now()).toMillis());
        return new BaseResponse(ConstantCode.SUCCESS);
    }

    /**
     *
     * @param chainName
     * @return
     * @throws IOException
     */
    @Log(title = "BCOS3/节点管理", businessType = BusinessType.OTHER)
    @SaCheckPermission("bcos3:chain:restartChain")
    @GetMapping(value = "chain/restart")
    public BaseResponse restartChain(@RequestParam(value = "chainName") String chainName,
        @RequestParam(value = "groupId") Integer groupId) throws IOException {
        Instant startTime = Instant.now();
        log.info("restartChain, chainName:[{}], groupId:{}, now:[{}]", chainName, groupId, startTime);
        deployService.restartChain(chainName, groupId.toString());
        log.info("end restartChain. useTime:{}", Duration.between(startTime, Instant.now()).toMillis());
        return new BaseResponse(ConstantCode.SUCCESS);
    }

    /**
     * delete chain by chainName.
     */
    @Log(title = "BCOS3/节点管理", businessType = BusinessType.DELETE)
    @SaCheckPermission("bcos3:chain:deleteChain")
    @DeleteMapping(value = "delete")
    public BaseResponse deleteChain(
            @RequestParam(value = "chainName", required = false, defaultValue = "default_chain_v3") String chainName
    ) throws NodeMgrException {
        Instant startTime = Instant.now();
        log.info("Start deleteChain chainName:[{}], startTime:[{}]",
                chainName, startTime.toEpochMilli());
        // include delete chain files and stop node/front docker container(on remote host), delete chain file locally too
        RetCode deleteResult = this.deployService.deleteChain(chainName);
        log.info("end deleteChain. useTime:{}", Duration.between(startTime, Instant.now()).toMillis());
        return new BaseResponse(deleteResult);
    }

    /**
     *
     * @return
     * @throws IOException
     */
    @SaCheckPermission("bcos3:chain:front")
    @GetMapping(value = "type")
    public BaseResponse deployType() throws IOException {
        Instant startTime = Instant.now();
        log.info("Start get deploy type, now:[{}]",  startTime);
        return new BaseResponse(ConstantCode.SUCCESS, constantProperties.getDeployType());
    }

}

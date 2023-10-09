/*
 * Copyright 2014-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.webank.webase.node.mgr.precntauth.precompiled.consensus;

import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.entity.BaseResponse;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.precntauth.precompiled.base.PrecompiledUtil;
import com.webank.webase.node.mgr.precntauth.precompiled.consensus.entity.ConsensusHandle;
import com.webank.webase.node.mgr.precntauth.precompiled.consensus.entity.ReqNodeListInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.time.Duration;
import java.time.Instant;

//@Api(value = "precntauth/precompiled/consensus", tags = "precntauth precompiled controller")
@Slf4j
@RestController
@RequestMapping(value = "precntauth/precompiled/consensus")
public class ConsensusController {

    @Autowired
    private ConsensusServiceInWebase consensusService;

    /**
     * Consensus
     */
//    @ApiOperation(value = "query consensus node list")
//    @ApiImplicitParam(name = "reqNodeListInfo", value = "node consensus list", required = true, dataType = "ReqNodeListInfo")
    @PostMapping("list")
    public Object getNodeList(@Valid @RequestBody ReqNodeListInfo reqNodeListInfo) {
        return consensusService.getNodeList(reqNodeListInfo);
    }

//    @ApiOperation(value = "manage node type", notes = "addSealer addObserver removeNode")
//    @ApiImplicitParam(name = "consensusHandle", value = "node consensus info", required = true, dataType = "ConsensusHandle")
    @PostMapping("manage")
    public Object nodeManageControl(@Valid @RequestBody ConsensusHandle consensusHandle) {
        log.info("start nodeManageControl. consensusHandle:{}", consensusHandle);
        String nodeType = consensusHandle.getNodeType();
        String nodeId = consensusHandle.getNodeId();
        if (!PrecompiledUtil.checkNodeId(nodeId)) {
            return ConstantCode.INVALID_NODE_ID;
        }
        switch (nodeType) {
            case PrecompiledUtil.NODE_TYPE_SEALER:
                return addSealer(consensusHandle);
            case PrecompiledUtil.NODE_TYPE_OBSERVER:
                return addObserver(consensusHandle);
            case PrecompiledUtil.NODE_TYPE_REMOVE:
                return removeNode(consensusHandle);
            default:
                log.debug("end nodeManageControl invalid node type");
                return ConstantCode.INVALID_NODE_TYPE;
        }
    }

    public Object addSealer(ConsensusHandle consensusHandle) {
        Instant startTime = Instant.now();
        if (consensusHandle.getWeight() == null) {
            throw new NodeMgrException(ConstantCode.ADD_SEALER_WEIGHT_CANNOT_NULL);
        }
        try {
            Object res = consensusService.addSealer(consensusHandle);
            log.info("end addSealer useTime:{} res:{}",
                Duration.between(startTime, Instant.now()).toMillis(), res);
            return res;
        } catch (Exception e) {
            log.error("addSealer exception:[]", e);
            return new BaseResponse(ConstantCode.FAIL_CHANGE_NODE_TYPE, e.getMessage());
        }
    }

    public Object addObserver(ConsensusHandle consensusHandle) {
        Instant startTime = Instant.now();
        try {
            Object res = consensusService.addObserver(consensusHandle);
            log.info("end addObserver useTime:{} res:{}",
                Duration.between(startTime, Instant.now()).toMillis(), res);
            return res;
        } catch (Exception e) {
            log.error("addObserver exception:[]", e);
            return new BaseResponse(ConstantCode.FAIL_CHANGE_NODE_TYPE, e.getMessage());
        }
    }

    public Object removeNode(ConsensusHandle consensusHandle) {
        Instant startTime = Instant.now();
        try {
            Object res = consensusService.removeNode(consensusHandle);
            log.info("end addSealer useTime:{} res:{}",
                Duration.between(startTime, Instant.now()).toMillis(), res);
            return res;
        } catch (Exception e) { // e.getCause
            log.error("removeNode exception:[]", e);
            return new BaseResponse(ConstantCode.FAIL_CHANGE_NODE_TYPE, e.getMessage());
        }
    }

}

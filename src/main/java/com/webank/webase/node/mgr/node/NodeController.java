/**
 * Copyright 2014-2021  the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.webank.webase.node.mgr.node;

import com.webank.webase.node.mgr.node.entity.ReqUpdate;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.entity.BasePageResponse;
import com.webank.webase.node.mgr.base.entity.BaseResponse;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.tools.JsonTools;
import com.webank.webase.node.mgr.node.entity.NodeParam;
import com.webank.webase.node.mgr.node.entity.TbNode;

import lombok.extern.log4j.Log4j2;

/**
 * Controller for node data.
 */
@Log4j2
@RestController
@RequestMapping("node")
public class NodeController {

    @Autowired private NodeService nodeService;

    /**
     * query node info list.
     */
    @GetMapping(value = "/nodeList/{groupId}/{pageNumber}/{pageSize}")
    public BasePageResponse queryNodeList(@PathVariable("groupId") Integer groupId,
        @PathVariable("pageNumber") Integer pageNumber,
        @PathVariable("pageSize") Integer pageSize,
        @RequestParam(value = "nodeName", required = false) String nodeName)
        throws NodeMgrException {
        BasePageResponse pageResponse = new BasePageResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info(
            "start queryNodeList startTime:{} groupId:{}  pageNumber:{} pageSize:{} nodeName:{}",
            startTime.toEpochMilli(), groupId, pageNumber,
            pageSize, nodeName);

        // param
        NodeParam queryParam = new NodeParam();
        queryParam.setGroupId(groupId);
        queryParam.setNodeName(nodeName);

        //check node status before query
        try{
            nodeService.checkAndUpdateNodeStatus(groupId);
        }catch (Exception e) {
            log.error("queryNodeList checkAndUpdateNodeStatus groupId:{}, error: []", groupId, e);
        }
        Integer count = nodeService.countOfNode(queryParam);
        if (count != null && count > 0) {
            Integer start = Optional.ofNullable(pageNumber).map(page -> (page - 1) * pageSize)
                .orElse(null);
            queryParam.setPageSize(pageSize);
            queryParam.setStart(start);

            List<TbNode> listOfnode = nodeService.queryNodeList(queryParam);
            pageResponse.setData(listOfnode);
            pageResponse.setTotalCount(count);

        }

        log.info("end queryNodeList useTime:{} result:{}",
            Duration.between(startTime, Instant.now()).toMillis(), JsonTools.toJSONString(pageResponse));
        return pageResponse;
    }

    /**
     * get node info.
     */
    @GetMapping(value = "/nodeInfo/{groupId}")
    public BaseResponse getNodeInfo(@PathVariable("groupId") Integer groupId)
        throws NodeMgrException {

        Instant startTime = Instant.now();
        log.info("start addNodeInfo startTime:{} groupId:{}",
            startTime.toEpochMilli(), groupId);

        // param
        NodeParam param = new NodeParam();
        param.setGroupId(groupId);

        // query node row
        TbNode tbNode = nodeService.queryNodeInfo(param);

        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        baseResponse.setData(tbNode);

        log.info("end addNodeInfo useTime:{} result:{}",
            Duration.between(startTime, Instant.now()).toMillis(), JsonTools.toJSONString(baseResponse));
        return baseResponse;
    }

    /**
     * get node id list
     */
    @GetMapping("/nodeIdList/{groupId}")
    public BaseResponse getNodeIdList(@PathVariable("groupId") Integer groupId) {
        Instant startTime = Instant.now();
        log.info("start getNodeIdList startTime:{} groupId:{}",
                startTime.toEpochMilli(), groupId);
        List<String> res = nodeService.getNodeIdListService(groupId);

        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        baseResponse.setData(res);

        log.info("end getNodeIdList useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(), JsonTools.toJSONString(baseResponse));
        return baseResponse;
    }

    /**
     * update tb_node info of city, agency, ip etc.
     */
    @PutMapping("/description")
    public BaseResponse updateDesc(@Valid @RequestBody ReqUpdate reqUpdate) {
        Instant startTime = Instant.now();
        log.info("updateDesc startTime:{},reqUpdate:{}",
            startTime.toEpochMilli(), reqUpdate);

        int res = nodeService.updateDescription(reqUpdate);

        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        baseResponse.setData(res);

        log.info("end updateDesc useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(), JsonTools.toJSONString(baseResponse));
        return baseResponse;
    }


}

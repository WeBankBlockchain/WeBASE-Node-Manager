/*
 * Copyright 2014-2019  the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webank.webase.node.mgr.logs;

import com.alibaba.fastjson.JSON;
import com.webank.webase.node.mgr.base.entity.BasePageResponse;
import com.webank.webase.node.mgr.base.entity.ConstantCode;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.base.tools.NodeMgrTools;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@RequestMapping(value = "nodeLog")
public class NodeLogController {

    @Autowired
    private NodeLogService nodeLogService;

    /**
     * query nde list.
     */
    @GetMapping(value = "/nodeLogList/{networkId}/{nodeId}/{pageNumber}/{pageSize}")
    public BasePageResponse queryLogList(@PathVariable("networkId") Integer networkId,
        @PathVariable("nodeId") Integer nodeId,
        @PathVariable("pageNumber") Integer pageNumber, @PathVariable("pageSize") Integer pageSize,
        @RequestParam(value = "startTime", required = false) String inputStartTime,
        @RequestParam(value = "endTime", required = false) String inputEndTime)
        throws NodeMgrException, Exception {
        BasePageResponse pageResponse = new BasePageResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info(
            "start queryLogList. startTime:{} networkId:{}  nodeId:{} pageNumber:{} "
                + "pageSize:{} startTime:{} endTime:{}",
            startTime.toEpochMilli(), networkId, pageNumber, JSON.toJSONString(inputStartTime),
            JSON.toJSONString(inputEndTime));

        // query param
        Integer start = Optional.ofNullable(pageNumber).map(page -> (page - 1) * pageSize)
            .orElse(null);
        NodeLogListParam param = new NodeLogListParam();
        param.setNetworkId(networkId);
        param.setNodeId(nodeId);
        param.setStart(start);
        param.setPageSize(pageSize);
        param.setStartTime(NodeMgrTools
            .string2LocalDateTime(inputStartTime, NodeMgrTools.DEFAULT_DATE_TIME_FORMAT));
        param.setEndTime(
            NodeMgrTools.string2LocalDateTime(inputEndTime, NodeMgrTools.DEFAULT_DATE_TIME_FORMAT));

        // query count
        Integer count = nodeLogService.countOfNodeLog(param);

        if (count != null && count > 0) {
            List<TbNodeLog> transList = nodeLogService.listOfNodeLog(param);
            pageResponse.setData(transList);
            pageResponse.setTotalCount(count);
        }

        log.info("end nodeLogList useTime:{} result:{}",
            Duration.between(startTime, Instant.now()).toMillis(), JSON.toJSONString(pageResponse));
        return pageResponse;
    }
}

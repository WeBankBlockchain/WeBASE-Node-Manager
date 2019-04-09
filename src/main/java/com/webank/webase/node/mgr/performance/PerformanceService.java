/**
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
package com.webank.webase.node.mgr.performance;

import com.alibaba.fastjson.JSON;
import com.webank.webase.node.mgr.base.entity.ConstantCode;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.base.tools.NodeMgrTools;
import com.webank.webase.node.mgr.node.NodeService;
import com.webank.webase.node.mgr.node.TbNode;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Log4j2
@Service
public class PerformanceService {

    @Autowired
    private NodeService nodeService;
    @Autowired
    private RestTemplate genericRestTemplate;

    /**
     * get ratio of performance.
     */
    public Object getPerformanceRatio(Integer nodeId, LocalDateTime beginDate,
        LocalDateTime endDate, LocalDateTime contrastBeginDate,
        LocalDateTime contrastEndDate, int gap) {
        log.debug(
            "start getPerformanceRatio.  nodeId:{} beginDate:{} endDate:{}"
                + " contrastBeginDate:{} contrastEndDate:{} gap:{}",
            nodeId, beginDate, endDate, contrastBeginDate, contrastEndDate, gap);

        List<String> nameList = Arrays
            .asList("beginDate", "endDate", "contrastBeginDate", "contrastEndDate", "gap");
        List<Object> valueList = Arrays
            .asList(beginDate, endDate, contrastBeginDate, contrastEndDate, gap);

        // request param to str
        String urlParam = NodeMgrTools.convertUrlParam(nameList, valueList);

        // query by node Id
        TbNode tbNode = nodeService.queryByNodeId(nodeId);
        if (tbNode == null) {
            throw new NodeMgrException(ConstantCode.INVALID_NODE_ID);
        }

        // request url
       // String url = String.format(RestTools.FRONT_URL, tbNode.getNodeIp(), tbNode.getFrontPort(),
      //          RestTools.FRONT_PERFORMANCE_RATIO); TODO
        String url = null;
        url = url + "?" + urlParam;
        log.info("getPerformanceRatio request url:{}", url);

        Object rspObj = genericRestTemplate.getForObject(url, Object.class);
        log.debug("end getPerformanceRatio. rspObj:{}", JSON.toJSONString(rspObj));
        return rspObj;

    }

    /**
     * get config of performance.
     */
    public Object getPerformanceConfig(Integer nodeId) {
        log.debug("start getPerformanceConfig.  nodeId:{} ", nodeId);
        // query by node Id
        TbNode tbNode = nodeService.queryByNodeId(nodeId);
        if (tbNode == null) {
            throw new NodeMgrException(ConstantCode.INVALID_NODE_ID);
        }

        // request url
      //  String url = String.format(RestTools.FRONT_URL, tbNode.getNodeIp(), tbNode.getFrontPort(),
       //         RestTools.FRONT_PERFORMANCE_CONFIG); TODO
        String url = null;
        log.info("getPerformanceConfig request url:{}", url);

        Object rspObj = genericRestTemplate.getForObject(url, Object.class);
        log.debug("end getPerformanceConfig. frontRsp:{}", JSON.toJSONString(rspObj));
        return rspObj;
    }

}

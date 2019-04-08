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
package com.webank.webase.node.mgr.report;

import com.alibaba.fastjson.JSON;
import com.webank.webase.node.mgr.base.entity.BaseResponse;
import com.webank.webase.node.mgr.base.entity.ConstantCode;
import com.webank.webase.node.mgr.base.enums.DataStatus;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.base.properties.ConstantProperties;
import com.webank.webase.node.mgr.base.tools.NodeMgrTools;
import com.webank.webase.node.mgr.block.BlockService;
import com.webank.webase.node.mgr.front.FrontService;
import com.webank.webase.node.mgr.logs.LatestLog;
import com.webank.webase.node.mgr.logs.NodeLogService;
import com.webank.webase.node.mgr.logs.TbNodeLog;
import com.webank.webase.node.mgr.node.NodeService;
import com.webank.webase.node.mgr.node.TbNode;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Processing services for reporting data.
 */
@Log4j2
@Service
public class ReportService {

    private static final String BLOCK_INFO_ATTR = "block_info";// report block info
    private static final String NODE_LOG_ATTR = "node_log"; // report node log info
    private static final String LATEST_BLOCK_ATTR = "latest_block";// query latest block
    private static final String LATEST_NODE_LOG_ATTR = "latest_node_log";// query latest node log
    // all attr for report blockChainInfo
    private static final List<String> REPORT_ARRT_NAME_LIST = Arrays
        .asList(BLOCK_INFO_ATTR, NODE_LOG_ATTR);
    // all attr for query blockChainInfo
    private static final List<String> QUERY_ARRT_NAME_LIST = Arrays
        .asList(LATEST_BLOCK_ATTR, LATEST_NODE_LOG_ATTR);

    @Autowired
    private NodeService nodeService;
    @Autowired
    private NodeLogService nodeLogService;
    @Autowired
    private BlockService blockService;
    @Autowired
    private FrontService frontService;
    @Autowired
    private ConstantProperties cProperties;


    /**
     * handler report block info.
     */
    @Async("asyncServiceExecutor")
    public void handlerBlockInfo(BaseReportInfo blockInfo) {
        log.debug("start handlerBlockInfo");

        MetricData[] metricDataLis = blockInfo.getMetricDataList();
        for (MetricData metricData : metricDataLis) {
            try {
                // case attr for method
                caseReportAttr(metricData);
            } catch (NodeMgrException ex) {
                log.error("handlerBlockInfo fail. continue next", ex);
                continue;
            }
        }
        log.debug("end handlerBlockInfo");
    }

    /**
     * query blockchain info from node-mgr.
     */
    public BaseResponse getBlockChainInfo(String attr, String nodeIp, Integer nodeP2PPort)
        throws NodeMgrException {
        log.debug("start getBlockChainInfo. attr:{} nodeIp:{} nodeP2PPort:{} param:{}", attr,
            nodeIp, nodeP2PPort);
        // check attr
        if (!QUERY_ARRT_NAME_LIST.contains(attr)) {
            log.debug("no need to save attr:{}", attr);
            throw new NodeMgrException(ConstantCode.INVALID_ATTR);
        }

        // query node info
        TbNode tbNode = nodeService.queryNodeByIpAndP2pPort(nodeIp, nodeP2PPort);
        if (tbNode == null) {
            throw new NodeMgrException(ConstantCode.INVALID_NODE_INFO);
        }

        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);

        switch (attr) {
            case LATEST_BLOCK_ATTR:
                getLatestBlockNumber(tbNode.getNetworkId(), nodeIp, nodeP2PPort, baseResponse);
                break;
            case LATEST_NODE_LOG_ATTR:
                getLatestNodeLog(nodeIp, nodeP2PPort, baseResponse);
                break;
            default:
                break;
        }
        log.debug("end getBlockChainInfo baseResponse:{}", JSON.toJSONString(baseResponse));
        return baseResponse;
    }

    /**
     * query latest block.
     */
    public void getLatestBlockNumber(Integer networkId, String nodeIp, Integer nodeP2PPort,
        BaseResponse baseResponse)
        throws NodeMgrException {
        log.debug("start getLatestBlockNumber.  nodeIp:{} nodeP2PPort:{} ", nodeIp, nodeP2PPort);

        // quer latest block
        BigInteger latestBlock = blockService.getLatestBlockNumber(nodeIp, nodeP2PPort);
        if (latestBlock == null && cProperties.getIsBlockPullFromZero()) {
            //report from 0
            latestBlock = BigInteger.ZERO;
        } else if (latestBlock == null) {
            //get latest blockNumber
            Map<String, String> rspRsp = frontService
                .getFrontForEntity(networkId, FrontService.FRONT_BLOCK_NUMBER_URI, HashMap.class);
            latestBlock = new BigInteger(String.valueOf(rspRsp.get("blockNumber")));
            if (BigInteger.ZERO != latestBlock) {
                latestBlock = latestBlock.subtract(BigInteger.ONE);
            }
        }

        Map<String, BigInteger> map = new HashMap<>();
        map.put("latestBlock", latestBlock);

        baseResponse.setData(map);
        log.debug("end getLatestBlockNumber.  baseResponse:{} ", JSON.toJSONString(baseResponse));
    }

    /**
     * query latest node log.
     */
    public void getLatestNodeLog(String nodeIp, Integer nodeP2PPort, BaseResponse baseResponse)
        throws NodeMgrException {
        log.debug("start getLatestNodeLog.  nodeIp:{} nodeP2PPort:{} ", nodeIp, nodeP2PPort);

        // query latest log
        LatestLog latestLog = nodeLogService.queryLatestNodeLog(nodeIp, nodeP2PPort);
        baseResponse.setData(latestLog);

        log.debug("end getLatestNodeLog.  baseResponse:{} ", JSON.toJSONString(baseResponse));

    }

    /**
     * case report attr.
     */
    private void caseReportAttr(MetricData metricData) throws NodeMgrException {
        String attr = metricData.getAttr().toLowerCase();

        // check attr
        if (!REPORT_ARRT_NAME_LIST.contains(attr)) {
            log.debug("no need to save attr:{}", attr);
            return;
        }

        // query node info
        TbNode tbNode = nodeService
            .queryNodeByIpAndP2pPort(metricData.getHostIp(), metricData.getNodeP2PPort());
        if (tbNode == null) {
            throw new NodeMgrException(ConstantCode.INVALID_NODE_INFO);
        }
        // case attr
        switch (attr) {
            case BLOCK_INFO_ATTR:
                handleReportBlockInfo(tbNode, metricData.getMetricValue());
                break;
            case NODE_LOG_ATTR:
                saveNodeLog(tbNode, metricData.getMetricValue());
                break;
            default:
                break;
        }

    }


    /**
     * save node log.
     */
    public void saveNodeLog(TbNode tbNode, Object metricValue) throws NodeMgrException {
        Integer nodeId = Optional.ofNullable(tbNode).map(TbNode::getNodeId)
            .orElseThrow(() -> new NodeMgrException(ConstantCode.INVALID_NODE_INFO));

        List<?> listNodeLog = (List<?>) metricValue;
        for (int i = 0; i < listNodeLog.size(); i++) {
            NodeLogInfo nodeLog = NodeMgrTools
                .object2JavaBean(listNodeLog.get(i), NodeLogInfo.class);
            if (nodeLog == null) {
                log.warn("fail saveNodeLog. logs null");
                return;
            }
            // save node log
            LocalDateTime logTime = nodeLog.getLogTime();
            Integer rowNumber = nodeLog.getRowNumber();
            String logMsg = nodeLog.getLogMsg();
            String fileName = nodeLog.getFileName();

            if (StringUtils.isAnyBlank(logMsg, fileName) || rowNumber == null || logTime == null) {
                log.warn(
                    "fail saveNodeLog. nodeId:{} fileName:{} rowNumber:{} logTimeStr:{} logMsg:{}",
                    nodeId, fileName, rowNumber, logTime,
                    logMsg);
                return;
            }

            TbNodeLog logRow = new TbNodeLog(nodeId, fileName, logTime, rowNumber, logMsg,
                DataStatus.NORMAL.getValue());
            nodeLogService.addNodeLog(logRow);
        }

    }


    /**
     * save block info.
     */
    private void handleReportBlockInfo(TbNode tbNode, Object metricValue) throws NodeMgrException {
        Integer networkId = Optional.ofNullable(tbNode).map(TbNode::getNetworkId)
            .orElseThrow(() -> new NodeMgrException(ConstantCode.INVALID_NODE_INFO));

        List<?> listBlockInfo = (List<?>) metricValue;
        for (int i = 0; i < listBlockInfo.size(); i++) {
            try {
                BlockInfo blockInfo = NodeMgrTools
                    .object2JavaBean(listBlockInfo.get(i), BlockInfo.class);
                blockService.saveBLockInfo(blockInfo, networkId);
            } catch (NodeMgrException ex) {
                log.error("saveBlockInfo fail. continue next", ex);
                continue;
            }

        }

    }

}

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
import com.webank.webase.node.mgr.base.entity.ConstantCode;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.node.NodeService;
import com.webank.webase.node.mgr.node.TbNode;
import java.util.List;
import java.util.Optional;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * services for node log.
 */
@Log4j2
@Service
public class NodeLogService {

    @Autowired
    private NodeLogMapper nodeLogMapper;
    @Autowired
    private NodeService nodeService;

    /**
     * add node log row.
     */
    public void addNodeLog(TbNodeLog tbNodeLog) {
        log.debug("start addNodeLog tbNodeLog:{}", JSON.toJSONString(tbNodeLog));
        nodeLogMapper.addNodeLogRow(tbNodeLog);
        log.debug("end addNodeLog");
    }

    /**
     * query latest node log.
     */
    public LatestLog queryLatestNodeLog(String nodeIp, Integer p2pPort) throws NodeMgrException {
        log.debug("start queryLatestNodeLog. nodeIp:{} nodeIp:{}", nodeIp, p2pPort);
        TbNode tbNode = nodeService.queryNodeByIpAndP2pPort(nodeIp, p2pPort);
        Integer nodeId = Optional.ofNullable(tbNode).map(TbNode::getNodeId)
            .orElseThrow(() -> new NodeMgrException(ConstantCode.INVALID_NODE_INFO));

        TbNodeLog logRow = nodeLogMapper.queryLatestNodeLog(nodeId);
        if (logRow == null) {
            log.info("fail queryLatestNodeLog. not find node log");
            return null;
        }

        LatestLog latestLog = new LatestLog(logRow.getLogTime(), logRow.getRowNumber(),
            logRow.getFileName());
        log.debug("end queryLatestNodeLog. LatestLog:{}", JSON.toJSONString(latestLog));

        return latestLog;
    }

    /**
     * query count of node log.
     */
    public int countOfNodeLog(NodeLogListParam param) {
        log.debug("start countOfNodeLog. param:{} ", JSON.toJSONString(param));
        Integer nodeLogCount = nodeLogMapper.countOfNodeLog(param);
        int count = nodeLogCount == null ? 0 : nodeLogCount.intValue();
        log.debug("end nodeLogCount. count:{} ", count);
        return count;
    }

    /**
     * query node log list.
     */
    public List<TbNodeLog> listOfNodeLog(NodeLogListParam param) {
        log.debug("start listOfNodeLog. param:{} ", JSON.toJSONString(param));
        List<TbNodeLog> list = nodeLogMapper.listOfNodeLog(param);
        log.debug("end listOfNodeLog. list:{} ", JSON.toJSONString(list));
        return list;
    }

    /**
     * query min and max rowNumber of tb_node_log.
     */
    public List<MinMaxRowNumber> queryMinMaxRowNumber() throws NodeMgrException {
        log.debug("start queryMinMaxRowNumber");
        try {
            List<MinMaxRowNumber> listMinMaxRowNumber = nodeLogMapper.queryMinMaxRowNumber();
            int listSize = Optional.ofNullable(listMinMaxRowNumber).map(list -> list.size())
                .orElse(0);
            log.info("end queryMinMaxRowNumber listSize:{}", listSize);
            return listMinMaxRowNumber;
        } catch (RuntimeException ex) {
            log.error("fail queryMinMaxRowNumber", ex);
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }
    }

    /**
     * delete node block info.
     */
    public Integer deleteSomeLogs(Integer nodeId, Integer deleteRowNumber) throws NodeMgrException {
        log.debug("start deleteSomeLogs. nodeId:{} deleteRowNumber:{}", nodeId, deleteRowNumber);

        Integer affectRow = 0;
        try {
            affectRow = nodeLogMapper.deleteSomeLogs(nodeId, deleteRowNumber);
        } catch (RuntimeException ex) {
            log.error("fail deleteSomeLogs. nodeId:{} deleteRowNumber:{}", nodeId, deleteRowNumber,
                ex);
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }

        log.debug("end deleteSomeLogs. nodeId:{} deleteRowNumber:{} affectRow:{}", nodeId,
            deleteRowNumber, affectRow);
        return affectRow;
    }
}

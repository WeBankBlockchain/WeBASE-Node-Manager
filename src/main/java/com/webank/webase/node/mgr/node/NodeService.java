/**
 * Copyright 2014-2019  the original author or authors.
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
package com.webank.webase.node.mgr.node;

import com.alibaba.fastjson.JSON;
import com.webank.webase.node.mgr.base.entity.ConstantCode;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.node.entity.PeerInfo;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * services for node data.
 */
@Log4j2
@Service
public class NodeService {

    @Autowired
    private NodeMapper nodeMapper;

    /**
     * add new node data.
     */
    public String addNodeInfo(Integer groupId,PeerInfo peerInfo) throws NodeMgrException {
        String[] ipPort = peerInfo.getIPAndPort().split(":");
        String nodeIp = ipPort[0];
        Integer nodeP2PPort = Integer.valueOf(ipPort[1]);

        String nodeName = nodeIp + "_" +nodeP2PPort;

        // add row
        TbNode tbNode = new TbNode();
        tbNode.setNodeId(peerInfo.getNodeId());
        tbNode.setGroupId(groupId);
        tbNode.setNodeIp(nodeIp);
        tbNode.setNodeName(nodeName);
        tbNode.setP2pPort(nodeP2PPort);
        nodeMapper.add(tbNode);

        return tbNode.getNodeId();
    }

    /**
     * query count of node.
     */
    public Integer countOfNode(NodeParam queryParam) throws NodeMgrException {
        log.debug("start countOfNode queryParam:{}", JSON.toJSONString(queryParam));
        try {
            Integer nodeCount = nodeMapper.getCount(queryParam);
            log.debug("end countOfNode nodeCount:{} queryParam:{}", nodeCount,
                JSON.toJSONString(queryParam));
            return nodeCount;
        } catch (RuntimeException ex) {
            log.error("fail countOfNode . queryParam:{}", queryParam, ex);
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }
    }

    /**
     * query all nodes
     */
    public List<TbNode> getAllNodes(){
        return qureyNodeList(new NodeParam());
    }

    /**
     * query node list by page.
     */
    public List<TbNode> qureyNodeList(NodeParam queryParam) throws NodeMgrException {
        log.debug("start qureyNodeList queryParam:{}", JSON.toJSONString(queryParam));

        // query node list
        List<TbNode> listOfNode = nodeMapper.getList(queryParam);

        log.debug("end qureyNodeList listOfNode:{}", JSON.toJSONString(listOfNode));
        return listOfNode;
    }

    /**
     * query node by groupId
     */
    public List<TbNode> queryByGroupId(int groupId){
        NodeParam nodeParam = new NodeParam();
        nodeParam.setGroupId(groupId);
        return qureyNodeList(nodeParam);
    }

    /**
     * query all node list
     */
    public List<TbNode> getAll(){
        return qureyNodeList(new NodeParam());
    }

    /**
     * query node info.
     */
    public TbNode queryByNodeId(Integer nodeId) throws NodeMgrException {
        log.debug("start queryNode nodeId:{}", nodeId);
        try {
            TbNode nodeRow = nodeMapper.queryByNodeId(nodeId);
            log.debug("end queryNode nodeId:{} TbNode:{}", nodeId, JSON.toJSONString(nodeRow));
            return nodeRow;
        } catch (RuntimeException ex) {
            log.error("fail queryNode . nodeId:{}", nodeId, ex);
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }
    }


    /**
     * update node info.
     */
    public void updateNode(TbNode tbNode) throws NodeMgrException {
        log.debug("start updateNodeInfo  param:{}", JSON.toJSONString(tbNode));
        Integer affectRow = 0;
        try {

            affectRow = nodeMapper.update(tbNode);
        } catch (RuntimeException ex) {
            log.error("updateNodeInfo exception", ex);
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }

        if (affectRow == 0) {
            log.warn("affect 0 rows of tb_node");
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }
        log.debug("end updateNodeInfo");
    }

    /**
     * query tb_node by nodeIp and p2pPort.
     */
    public TbNode queryNodeByIpAndP2pPort(String nodeIp, Integer p2pPort) throws NodeMgrException {
        log.debug("start queryNodeByIpAndP2pPort nodeIp:{} p2pPort:{}", nodeIp, p2pPort);

        if (StringUtils.isBlank(nodeIp)) {
            log.error("fail getGroupIdByNode. nodeIp blank");
            throw new NodeMgrException(ConstantCode.NODE_IP_EMPTY);
        }
        if (p2pPort == null) {
            log.error("fail getGroupIdByNode. p2pPort null");
            throw new NodeMgrException(ConstantCode.NODE_P2P_PORT_EMPTY);
        }
        TbNode tbNode = null;
        try {
            tbNode = nodeMapper.queryNodeByIpAndP2pPort(nodeIp, p2pPort);
        } catch (RuntimeException ex) {
            log.error("fail queryNodeByIpAndP2pPort. nodeIp:{} p2pPort:{}", nodeIp, p2pPort, ex);
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }
        log.debug("end queryNodeByIpAndP2pPort nodeIp:{} p2pPort:{}", nodeIp, p2pPort);
        return tbNode;
    }

    /**
     * query node info.
     */
    public TbNode queryNodeInfo(NodeParam nodeParam) {
        log.debug("start queryNodeInfo nodeParam:{}", JSON.toJSONString(nodeParam));
        TbNode tbNode = nodeMapper.queryNodeInfo(nodeParam);
        log.debug("end queryNodeInfo result:{}", tbNode);
        return tbNode;
    }

    /**
     * delete by node id.
     */
    public void deleteByNodeId(Integer nodeId) throws NodeMgrException {
        log.debug("start deleteByNodeId nodeId:{}", nodeId);
        TbNode nodeRow = queryByNodeId(nodeId);
        if (nodeRow == null) {
            log.info("fail deleteByNodeId. invalid node ip");
            throw new NodeMgrException(ConstantCode.INVALID_ROLE_ID);
        }

        Integer affectRow = nodeMapper.deleteByNodeId(nodeId);
        if (affectRow == 0) {
            log.warn("affect 0 rows of tb_node");
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }

        log.debug("end deleteByNodeId");
    }
}

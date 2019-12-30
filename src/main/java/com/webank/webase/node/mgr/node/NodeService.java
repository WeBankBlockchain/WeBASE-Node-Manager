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
import com.alibaba.fastjson.JSONArray;
import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.enums.DataStatus;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.base.tools.NodeMgrTools;
import com.webank.webase.node.mgr.frontinterface.FrontInterfaceService;
import com.webank.webase.node.mgr.frontinterface.entity.PeerOfConsensusStatus;
import com.webank.webase.node.mgr.frontinterface.entity.PeerOfSyncStatus;
import com.webank.webase.node.mgr.frontinterface.entity.SyncStatus;
import com.webank.webase.node.mgr.node.entity.PeerInfo;
import java.math.BigInteger;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
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
    @Autowired
    private FrontInterfaceService frontInterface;
    // interval of check node status
    private static final Long CHECK_NODE_WAIT_MIN_MILLIS = 7500L;

    /**
     * add new node data.
     */
    public void addNodeInfo(Integer groupId, PeerInfo peerInfo) throws NodeMgrException {
        String nodeIp = null;
        Integer nodeP2PPort = null;

        if (StringUtils.isNotBlank(peerInfo.getIPAndPort())) {
            String[] ipPort = peerInfo.getIPAndPort().split(":");
            nodeIp = ipPort[0];
            nodeP2PPort = Integer.valueOf(ipPort[1]);
        }
        String nodeName = groupId + "_" + peerInfo.getNodeId();

        // add row
        TbNode tbNode = new TbNode();
        tbNode.setNodeId(peerInfo.getNodeId());
        tbNode.setGroupId(groupId);
        tbNode.setNodeIp(nodeIp);
        tbNode.setNodeName(nodeName);
        tbNode.setP2pPort(nodeP2PPort);
        nodeMapper.add(tbNode);
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
    public List<TbNode> queryByGroupId(int groupId) {
        NodeParam nodeParam = new NodeParam();
        nodeParam.setGroupId(groupId);
        return qureyNodeList(nodeParam);
    }

    /**
     * query all node list
     */
    public List<TbNode> getAll() {
        return qureyNodeList(new NodeParam());
    }

    /**
     * query node info.
     */
    public TbNode queryByNodeId(String nodeId) throws NodeMgrException {
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
     * query node info.
     */
    public TbNode queryNodeInfo(NodeParam nodeParam) {
        log.debug("start queryNodeInfo nodeParam:{}", JSON.toJSONString(nodeParam));
        TbNode tbNode = nodeMapper.queryNodeInfo(nodeParam);
        log.debug("end queryNodeInfo result:{}", tbNode);
        return tbNode;
    }

    /**
     * delete by node and group.
     */
    public void deleteByNodeAndGroupId(String nodeId, int groupId) throws NodeMgrException {
        log.debug("start deleteByNodeAndGroupId nodeId:{} groupId:{}", nodeId, groupId);
        nodeMapper.deleteByNodeAndGroup(nodeId, groupId);
        log.debug("end deleteByNodeAndGroupId");
    }

    /**
     * delete by groupId.
     */
    public void deleteByGroupId(int groupId) {
        if (groupId == 0) {
            return;
        }
        nodeMapper.deleteByGroupId(groupId);
    }


    /**
     * check node status
     *
     */
    public void checkAndUpdateNodeStatus(int groupId) {
        //get local node list
        List<TbNode> nodeList = queryByGroupId(groupId);

        //getPeerOfConsensusStatus
        List<PeerOfConsensusStatus> consensusList = getPeerOfConsensusStatus(groupId);
        if(Objects.isNull(consensusList)){
            log.error("fail checkNodeStatus, consensusList is null");
            return;
        }
        
        // getObserverList
        List<String> observerList = frontInterface.getObserverList(groupId);

        for (TbNode tbNode : nodeList) {
            String nodeId = tbNode.getNodeId();
            BigInteger localBlockNumber = tbNode.getBlockNumber();
            BigInteger localPbftView = tbNode.getPbftView();
            LocalDateTime modifyTime = tbNode.getModifyTime();
            LocalDateTime createTime = tbNode.getCreateTime();

            Duration duration = Duration.between(modifyTime, LocalDateTime.now());
            Long subTime = duration.toMillis();
            if (subTime < CHECK_NODE_WAIT_MIN_MILLIS && createTime.isBefore(modifyTime)) {
                log.info("checkNodeStatus jump over. subTime:{}", subTime);
                return;
            }
            
            int nodeType = 0;   //0-consensus;1-observer
            if (observerList != null) {
                nodeType = observerList.stream()
                        .filter(observer -> observer.equals(tbNode.getNodeId())).map(c -> 1).findFirst()
                        .orElse(0);
            }

            BigInteger latestNumber = getBlockNumberOfNodeOnChain(groupId, nodeId);//blockNumber
            BigInteger latestView = consensusList.stream()
                .filter(cl -> nodeId.equals(cl.getNodeId())).map(c -> c.getView()).findFirst()
                .orElse(BigInteger.ZERO);//pbftView
            
            if (nodeType == 0) {    //0-consensus;1-observer
                // if local block number and pbftView equals chain's, invalid
                if (localBlockNumber.equals(latestNumber) && localPbftView.equals(latestView)) {
                    log.warn("node[{}] is invalid. localNumber:{} chainNumber:{} localView:{} chainView:{}",
                        nodeId, localBlockNumber, latestNumber, localPbftView, latestView);
                    tbNode.setNodeActive(DataStatus.INVALID.getValue());
                } else {
                    tbNode.setBlockNumber(latestNumber);
                    tbNode.setPbftView(latestView);
                    tbNode.setNodeActive(DataStatus.NORMAL.getValue());
                }
            } else { //observer
                // if latest block number not equal, invalid
                if (!latestNumber.equals(frontInterface.getLatestBlockNumber(groupId))) {
                    log.warn("node[{}] is invalid. localNumber:{} chainNumber:{} localView:{} chainView:{}",
                            nodeId, localBlockNumber, latestNumber, localPbftView, latestView);
                    tbNode.setNodeActive(DataStatus.INVALID.getValue());
                } else {
                    tbNode.setBlockNumber(latestNumber);
                    tbNode.setPbftView(latestView);
                    tbNode.setNodeActive(DataStatus.NORMAL.getValue());
                }
            }
            tbNode.setModifyTime(LocalDateTime.now());
            //update node
            updateNode(tbNode);
        }

    }


    /**
     * get latest number of peer on chain.
     */
    private BigInteger getBlockNumberOfNodeOnChain(int groupId, String nodeId) {
        SyncStatus syncStatus = frontInterface.getSyncStatus(groupId);
        if (nodeId.equals(syncStatus.getNodeId())) {
            return syncStatus.getBlockNumber();
        }
        List<PeerOfSyncStatus> peerList = syncStatus.getPeers();
        BigInteger latestNumber = peerList.stream().filter(peer -> nodeId.equals(peer.getNodeId()))
            .map(s -> s.getBlockNumber()).findFirst().orElse(BigInteger.ZERO);//blockNumber
        return latestNumber;
    }


    /**
     * get peer of consensusStatus
     */
    private List<PeerOfConsensusStatus> getPeerOfConsensusStatus(int groupId) {
        String consensusStatusJson = frontInterface.getConsensusStatus(groupId);
        if (StringUtils.isBlank(consensusStatusJson)) {
            log.debug("getPeerOfConsensusStatus is null: {}", consensusStatusJson);
            return null;
        }
        JSONArray jsonArr = JSONArray.parseArray(consensusStatusJson);
        List<Object> dataIsList = jsonArr.stream().filter(jsonObj -> jsonObj instanceof List)
            .map(arr -> {
                Object obj = JSONArray.parseArray(JSON.toJSONString(arr)).get(0);
                try {
                    NodeMgrTools.object2JavaBean(obj, PeerOfConsensusStatus.class);
                } catch (Exception e) {
                    return null;
                }
                return arr;
            }).collect(Collectors.toList());
        return JSONArray
            .parseArray(JSON.toJSONString(dataIsList.get(0)), PeerOfConsensusStatus.class);
    }

    /**
     * add sealer and observer in NodeList
     * return: List<String> nodeIdList
     */
    public List<PeerInfo> getSealerAndObserverList(int groupId) {
        log.debug("start getSealerAndObserverList groupId:{}", groupId);
        List<String> sealerList = frontInterface.getSealerList(groupId);
        List<String> observerList = frontInterface.getObserverList(groupId);
        List<PeerInfo> resList = new ArrayList<>();
        sealerList.stream().forEach(nodeId -> resList.add(new PeerInfo(nodeId)));
        observerList.stream().forEach(nodeId -> resList.add(new PeerInfo(nodeId)));
        log.debug("end getSealerAndObserverList resList:{}", resList);
        return resList;
    }
}

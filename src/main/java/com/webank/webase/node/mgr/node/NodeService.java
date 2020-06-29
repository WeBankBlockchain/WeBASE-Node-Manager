/**
 * Copyright 2014-2020  the original author or authors.
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

import java.math.BigInteger;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.enums.DataStatus;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.base.tools.JsonTools;
import com.webank.webase.node.mgr.base.tools.SshTools;
import com.webank.webase.node.mgr.base.tools.ValidateUtil;
import com.webank.webase.node.mgr.deploy.service.PathService;
import com.webank.webase.node.mgr.front.FrontService;
import com.webank.webase.node.mgr.front.entity.TbFront;
import com.webank.webase.node.mgr.frontinterface.FrontInterfaceService;
import com.webank.webase.node.mgr.frontinterface.entity.PeerOfConsensusStatus;
import com.webank.webase.node.mgr.frontinterface.entity.PeerOfSyncStatus;
import com.webank.webase.node.mgr.frontinterface.entity.SyncStatus;
import com.webank.webase.node.mgr.node.entity.NodeParam;
import com.webank.webase.node.mgr.node.entity.PeerInfo;
import com.webank.webase.node.mgr.node.entity.TbNode;

import lombok.extern.log4j.Log4j2;

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
    /**
     * update front status
     */
    @Autowired
    private FrontService frontService;

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
        String nodeName = getNodeName(groupId, peerInfo.getNodeId());

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
        log.debug("start countOfNode queryParam:{}", JsonTools.toJSONString(queryParam));
        try {
            Integer nodeCount = nodeMapper.getCount(queryParam);
            log.debug("end countOfNode nodeCount:{} queryParam:{}", nodeCount,
                JsonTools.toJSONString(queryParam));
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
        log.debug("start qureyNodeList queryParam:{}", JsonTools.toJSONString(queryParam));

        // query node list
        List<TbNode> listOfNode = nodeMapper.getList(queryParam);

        log.debug("end qureyNodeList listOfNode:{}", JsonTools.toJSONString(listOfNode));
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
     * update node info.
     */
    public void updateNode(TbNode tbNode) throws NodeMgrException {
        log.debug("start updateNodeInfo  param:{}", JsonTools.toJSONString(tbNode));
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
        log.debug("start queryNodeInfo nodeParam:{}", JsonTools.toJSONString(nodeParam));
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
                log.warn("checkNodeStatus jump over. for time internal subTime:{}", subTime);
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
            // to update front status
            TbFront updateFront = frontService.getByNodeId(nodeId);
            if (updateFront != null) {
                // update front status as long as update node (7.5s internal)
                log.debug("update front with node update nodeStatus:{}", tbNode.getNodeActive());
                updateFront.setStatus(tbNode.getNodeActive());
                frontService.updateFront(updateFront);
            }
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
        List jsonArr = JsonTools.toJavaObject(consensusStatusJson, List.class);
        if (jsonArr == null) {
            log.error("getPeerOfConsensusStatus error");
            throw new NodeMgrException(ConstantCode.FAIL_PARSE_JSON);
        }
        List<PeerOfConsensusStatus> dataIsList = new ArrayList<>();
        for (int i = 0; i < jsonArr.size(); i++ ) {
            if (jsonArr.get(i) instanceof List) {
                List<PeerOfConsensusStatus> tempList = JsonTools.toJavaObjectList(
                    JsonTools.toJSONString(jsonArr.get(i)), PeerOfConsensusStatus.class);
                if (tempList != null) {
                    dataIsList.addAll(tempList);
                } else {
                    throw new NodeMgrException(ConstantCode.FAIL_PARSE_JSON);
                }
            }
        }
        return dataIsList;
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

    public List<String> getNodeIdListService(int groupId) {
        log.debug("start getSealerAndObserverList groupId:{}", groupId);
        try {
            List<String> nodeIdList = frontInterface.getNodeIdList(groupId);
            log.debug("end getSealerAndObserverList nodeIdList:{}", nodeIdList);
            return nodeIdList;
        } catch (Exception e) {
            log.error("getNodeIdList error groupId:{}, exception:{}", groupId, e.getMessage());
            throw new NodeMgrException(ConstantCode.REQUEST_FRONT_FAIL.getCode(), e.getMessage());
        }

    }



    @Transactional(propagation = Propagation.REQUIRED)
    public TbNode insert(
            String nodeId,
            String nodeName,
            int groupId,
            String ip,
            int p2pPort,
            String description,
            final DataStatus dataStatus
    ) throws NodeMgrException {
        // TODO. params check
        if (! ValidateUtil.ipv4Valid(ip)){
            throw new NodeMgrException(ConstantCode.IP_FORMAT_ERROR);
        }

        DataStatus newDataStatus = dataStatus == null ? DataStatus.INVALID : dataStatus;

        TbNode node = TbNode.init(nodeId, nodeName, groupId, ip, p2pPort, description, newDataStatus);

        if (nodeMapper.add(node) != 1) {
            throw new NodeMgrException(ConstantCode.INSERT_NODE_ERROR);
        }
        return node;
    }

    /**
     * @param groupId
     * @param nodeId
     * @return
     */
    public static String getNodeName(int groupId, String nodeId) {
        return String.format("%s_%s", groupId, nodeId);
    }

    /**
     *
     * @param chainId
     * @param groupId
     * @return
     */
    public List<TbNode> selectNodeListByChainIdAndGroupId(int chainId,final int groupId){
        // select all fronts by all agencies
        List<TbFront> tbFrontList = this.frontService.selectFrontListByChainId(chainId);

        List<TbNode> tbNodeList = tbFrontList.stream()
                .map((front) -> nodeMapper.getByNodeIdAndGroupId(front.getNodeId(),groupId))
                .filter((node) -> node != null)
                .filter((node) -> node.getGroupId() == groupId)
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(tbNodeList)) {
            log.error("Chain:[{}] has no node.", chainId);
            return Collections.emptyList();
        }
        return tbNodeList;
    }


    /**
     * Find the first node for coping group config files.
     *
     * @param chainId
     * @param groupId
     * @return
     */
    public TbNode getOldestNodeByChainIdAndGroupId(int chainId, int groupId) {
        List<TbNode> tbNodeList = this.selectNodeListByChainIdAndGroupId(chainId, groupId);
        if (CollectionUtils.isEmpty(tbNodeList)) {
            return null;
        }
        TbNode oldest = null;

        for (TbNode tbNode : tbNodeList) {
            if (oldest == null){
                oldest = tbNode;
                continue;
            }
            if (tbNode.getCreateTime().isBefore(oldest.getCreateTime())){
                oldest = tbNode;
            }
        }
        return oldest;
    }

    /**
     * @param ip
     * @param rooDirOnHost
     * @param chainName
     * @param hostIndex
     * @param nodeId
     */
    public static void mvNodeOnRemoteHost(String ip, String rooDirOnHost, String chainName, int hostIndex, String nodeId,
            String sshUser, int sshPort,String privateKey) {
        // create /opt/fisco/deleted-tmp/default_chain-yyyyMMdd_HHmmss as a parent
        String chainDeleteRootOnHost = PathService.getChainDeletedRootOnHost(rooDirOnHost, chainName);
        SshTools.createDirOnRemote(ip, chainDeleteRootOnHost,sshUser,sshPort,privateKey);

        // e.g. /opt/fisco/default_chain
        String chainRootOnHost = PathService.getChainRootOnHost(rooDirOnHost, chainName);
        // e.g. /opt/fisco/default_chain/node[x]
        String src_nodeRootOnHost = PathService.getNodeRootOnHost(chainRootOnHost, hostIndex);

        // move to /opt/fisco/deleted-tmp/default_chain-yyyyMMdd_HHmmss/[nodeid(128)]
        String dst_nodeDeletedRootOnHost =
                PathService.getNodeDeletedRootOnHost(chainDeleteRootOnHost, nodeId);
        // move
        SshTools.mvDirOnRemote(ip, src_nodeRootOnHost, dst_nodeDeletedRootOnHost,sshUser,sshPort,privateKey);
    }
}

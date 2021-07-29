/**
 * Copyright 2014-2021  the original author or authors.
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

import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.enums.ConsensusType;
import com.webank.webase.node.mgr.base.enums.DataStatus;
import com.webank.webase.node.mgr.base.enums.FrontStatusEnum;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.config.properties.ConstantProperties;
import com.webank.webase.node.mgr.tools.JsonTools;
import com.webank.webase.node.mgr.tools.ValidateUtil;
import com.webank.webase.node.mgr.deploy.chain.ChainService;
import com.webank.webase.node.mgr.deploy.service.AnsibleService;
import com.webank.webase.node.mgr.deploy.service.PathService;
import com.webank.webase.node.mgr.front.FrontService;
import com.webank.webase.node.mgr.front.entity.TbFront;
import com.webank.webase.node.mgr.front.frontinterface.FrontInterfaceService;
import com.webank.webase.node.mgr.front.frontinterface.entity.PeerOfConsensusStatus;
import com.webank.webase.node.mgr.node.entity.NodeParam;
import com.webank.webase.node.mgr.node.entity.PeerInfo;
import com.webank.webase.node.mgr.node.entity.TbNode;
import java.math.BigInteger;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.sdk.client.protocol.response.ConsensusStatus.ConsensusInfo;
import org.fisco.bcos.sdk.client.protocol.response.ConsensusStatus.ViewInfo;
import org.fisco.bcos.sdk.client.protocol.response.SyncStatus.PeersInfo;
import org.fisco.bcos.sdk.client.protocol.response.SyncStatus.SyncStatusInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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
    @Autowired
    private ChainService chainService;
    @Autowired
    private ConstantProperties constantProperties;
    @Autowired
    private AnsibleService ansibleService;
    /**
     * update front status
     */
    @Autowired
    private FrontService frontService;

    // interval of check node status
    private static final Long EXT_CHECK_NODE_WAIT_MIN_MILLIS = 3500L;

    /**
     * add new node data.
     */
    @Transactional
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
    public List<TbNode> queryNodeList(NodeParam queryParam) throws NodeMgrException {
        log.debug("start queryNodeList queryParam:{}", JsonTools.toJSONString(queryParam));

        // query node list
        List<TbNode> listOfNode = nodeMapper.getList(queryParam);

        log.debug("end queryNodeList listOfNode:{}", JsonTools.toJSONString(listOfNode));
        return listOfNode;
    }

    /**
     * query node by groupId
     */
    public List<TbNode> queryByGroupId(int groupId) {
        NodeParam nodeParam = new NodeParam();
        nodeParam.setGroupId(groupId);
        return queryNodeList(nodeParam);
    }

    /**
     * query all node list
     */
    public List<TbNode> getAll() {
        return queryNodeList(new NodeParam());
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
        log.info("start deleteByNodeAndGroupId nodeId:{} groupId:{}", nodeId, groupId);
        nodeMapper.deleteByNodeAndGroup(nodeId, groupId);
        log.info("end deleteByNodeAndGroupId");
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
     * check node status, if pbftView or blockNumber not changing, invalid consensus
     * @1.4.3: if request consensus status but return -1, node is down
     *
     */
    public void checkAndUpdateNodeStatus(int groupId) {
        //get local node list
        List<TbNode> nodeList = queryByGroupId(groupId);

        //getPeerOfConsensusStatus
        List<PeerOfConsensusStatus> consensusList = getPeerOfConsensusStatus(groupId);
        if (Objects.isNull(consensusList)){
            log.error("fail checkNodeStatus, consensusList is null");
            return;
        }
        
        // getObserverList
        List<String> observerList = frontInterface.getObserverList(groupId);

        int nodeCount = CollectionUtils.size(consensusList) + CollectionUtils.size(observerList);

        for (TbNode tbNode : nodeList) {
            String nodeId = tbNode.getNodeId();
            BigInteger localBlockNumber = tbNode.getBlockNumber();
            BigInteger localPbftView = tbNode.getPbftView();
            LocalDateTime modifyTime = tbNode.getModifyTime();
            LocalDateTime createTime = tbNode.getCreateTime();

            Duration duration = Duration.between(modifyTime, LocalDateTime.now());
            Long subTime = duration.toMillis();
            if (subTime < (nodeCount * 1000 + EXT_CHECK_NODE_WAIT_MIN_MILLIS) && createTime.isBefore(modifyTime)) {
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
                .filter(cl -> nodeId.equals(cl.getNodeId())).map(PeerOfConsensusStatus::getView).findFirst()
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
            // only update front status if deploy manually
            if (chainService.runTask()) {
                TbFront updateFront = frontService.getByNodeId(nodeId);
                if (updateFront != null) {
                    // update front status as long as update node (7.5s internal)
                    log.debug("update front with node update nodeStatus:{}", tbNode.getNodeActive());
                    // update as 2, same as FrontStatuaEnum
                    if (tbNode.getNodeActive() == DataStatus.NORMAL.getValue()) {
                        updateFront.setStatus(FrontStatusEnum.RUNNING.getId());
                    } else if (tbNode.getNodeActive() == DataStatus.INVALID.getValue()) {
                        updateFront.setStatus(FrontStatusEnum.STOPPED.getId());
                    }
                    frontService.updateFront(updateFront);
                }
            }
        }

    }


    /**
     * get latest number of peer on chain.
     */
    private BigInteger getBlockNumberOfNodeOnChain(int groupId, String nodeId) {
        SyncStatusInfo syncStatus = frontInterface.getSyncStatus(groupId);
        if (nodeId.equals(syncStatus.getNodeId())) {
            return new BigInteger(syncStatus.getBlockNumber());
        }
        List<PeersInfo> peerList = syncStatus.getPeers();
        // blockNumber
        BigInteger latestNumber = peerList.stream().filter(peer -> nodeId.equals(peer.getNodeId()))
            .map(p -> new BigInteger(p.getBlockNumber())).findFirst().orElse(BigInteger.ZERO);
        return latestNumber;
    }


    /**
     * get peer of consensusStatus
     */
    private List<PeerOfConsensusStatus> getPeerOfConsensusStatus(int groupId) {
        ConsensusInfo consensusInfo = frontInterface.getConsensusStatus(groupId);
        if (consensusInfo == null) {
            log.debug("getPeerOfConsensusStatus is null");
            return null;
        }
        List<PeerOfConsensusStatus> dataIsList = new ArrayList<>();
        List<ViewInfo> viewInfos = consensusInfo.getViewInfos();
        for (ViewInfo viewInfo : viewInfos) {
            dataIsList.add(
                new PeerOfConsensusStatus(viewInfo.getNodeId(), new BigInteger(viewInfo.getView())));
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
        sealerList.forEach(nodeId -> resList.add(new PeerInfo(nodeId)));
        observerList.forEach(nodeId -> resList.add(new PeerInfo(nodeId)));
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
        log.info("start insert tb_node:{}", nodeId);
        if (!ValidateUtil.ipv4Valid(ip)){
            throw new NodeMgrException(ConstantCode.IP_FORMAT_ERROR);
        }

        DataStatus newDataStatus = dataStatus == null ? DataStatus.INVALID : dataStatus;

        TbNode node = TbNode.init(nodeId, nodeName, groupId, ip, p2pPort, description, newDataStatus);

        if (nodeMapper.add(node) != 1) {
            throw new NodeMgrException(ConstantCode.INSERT_NODE_ERROR);
        }
        log.info("end insert tb_node:{}", node);

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
    public List<TbNode> selectNodeListByChainIdAndGroupId(Integer chainId, final int groupId){
        // select all fronts by all agencies
        List<TbFront> tbFrontList = this.frontService.selectFrontListByChainId(chainId);
        log.info("selectNodeListByChainIdAndGroupId tbFrontList:{}", tbFrontList);

        // filter only not removed node will be added
        List<TbNode> tbNodeList = tbFrontList.stream()
                .map((front) -> nodeMapper.getByNodeIdAndGroupId(front.getNodeId(), groupId))
                .filter(Objects::nonNull)
                .filter((node) -> node.getGroupId() == groupId)
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(tbNodeList)) {
            log.error("Group of:[{}] chain:[{}] has no node.", groupId, chainId);
            return new ArrayList<>();
        }
        return tbNodeList;
    }

    /**
     * specific target frontList in batchAddNode
     * @param newFrontIdList specific front
     * @param chainId
     * @param groupId
     * @return
     */
    public List<TbNode> selectNodeListByChainIdAndGroupId(List<Integer> newFrontIdList, int chainId, final int groupId){
        log.info("selectNodeListByChainIdAndGroupId frontIdList:{}", newFrontIdList);
        List<TbFront> tbFrontList = this.frontService.selectFrontListByChainId(chainId);

        List<TbFront> newFrontList = frontService.selectByFrontIdList(newFrontIdList);

        tbFrontList.removeAll(newFrontList);
        tbFrontList.addAll(newFrontList);

        List<TbNode> tbNodeList = tbFrontList.stream()
                .map((front) -> nodeMapper.getByNodeIdAndGroupId(front.getNodeId(),groupId))
                .filter(Objects::nonNull)
                .filter((node) -> node.getGroupId() == groupId )
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(tbNodeList)) {
            log.error("Group of:[{}] of newFrontIdList:{} has no node.", groupId, newFrontIdList);
            return new ArrayList<>();
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
     * mv one node on host
     * @related with hostService mvHostChainDirByIdList(batch mv)
     */
    public void mvNodeOnRemoteHost(String ip, String rooDirOnHost, String chainName, int hostIndex, String nodeId) {
        // create /opt/fisco/deleted-tmp/default_chain-yyyyMMdd_HHmmss as a parent
        String chainDeleteRootOnHost = PathService.getChainDeletedRootOnHost(rooDirOnHost, chainName);
        ansibleService.execCreateDir(ip, chainDeleteRootOnHost);

        // e.g. /opt/fisco/default_chain
        String chainRootOnHost = PathService.getChainRootOnHost(rooDirOnHost, chainName);
        // e.g. /opt/fisco/default_chain/node[x]
        String src_nodeRootOnHost = PathService.getNodeRootOnHost(chainRootOnHost, hostIndex);

        // move to /opt/fisco/deleted-tmp/default_chain-yyyyMMdd_HHmmss/[nodeid(128)]
        String dst_nodeDeletedRootOnHost =
                PathService.getNodeDeletedRootOnHost(chainDeleteRootOnHost, nodeId);
        // move
        ansibleService.mvDirOnRemote(ip, src_nodeRootOnHost, dst_nodeDeletedRootOnHost);
    }

    /**
     * update node status by frontId and nodeStatus
     */
    public void updateNodeActiveStatus(int frontId, int nodeStatus) {
        TbFront front = frontService.getById(frontId);
        String nodeId = front.getNodeId();
        log.info("updateNodeActiveStatus by nodeId:{} of all group, status:{}", nodeId, nodeStatus);
        List<TbNode> nodeList = nodeMapper.selectByNodeId(nodeId);
        if (nodeList == null || nodeList.isEmpty()) {
            log.info("no node list of nodeId, jump over");
            return;
        }
        nodeList.forEach(node -> {
            node.setNodeActive(nodeStatus);
            this.updateNode(node);
        });
        log.info("end updateNodeActiveStatus affect size:{}", nodeList.size());
    }

    /**
     * check sealer list contain
     * return: true: is sealer
     */
    @Deprecated
    public boolean checkSealerListContains(int groupId, String nodeId, String ip, int port) {
        log.debug("start checkSealerListContains groupId:{},nodeId:{}", groupId, nodeId);
        List<String> sealerList = frontInterface.getSealerListFromSpecificFront(ip, port, groupId);
        boolean isSealer = sealerList.stream().anyMatch(n -> n.equals(nodeId));
        log.debug("end checkSealerListContains isSealer:{}", isSealer);
        return isSealer;
    }

    /**
     * check observer list contain
     * return: true: is sealer
     */
    @Deprecated
    public boolean checkObserverListContains(int groupId, String nodeId, String ip, int port) {
        log.debug("start checkObserverListContains groupId:{},nodeId:{}", groupId, nodeId);
        List<String> sealerList = frontInterface.getObserverListFromSpecificFront(ip, port, groupId);
        boolean isObserver = sealerList.stream().anyMatch(n -> n.equals(nodeId));
        log.debug("end checkObserverListContains isObserver:{}", isObserver);
        return isObserver;
    }

    /**
     * get local highest block height, if node equal, return 1, else return 2
     * @param groupId
     * @param nodeId
     * @return
     */
    public int checkNodeType(int groupId, String nodeId) {
        int localHighestHeight = nodeMapper.getHighestBlockHeight(groupId);
        TbNode node = nodeMapper.getByNodeIdAndGroupId(nodeId, groupId);
        int nodeBlockHeight = node != null ? node.getBlockNumber().intValue() : 0;
        log.info("local localHighestHeight:{},groupId:{} nodeId:{}, nodeBlockHeight:{}",
            localHighestHeight, groupId, nodeId, nodeBlockHeight);
        if (localHighestHeight == nodeBlockHeight) {
            return ConsensusType.SEALER.getValue();
        } else if (localHighestHeight > nodeBlockHeight) {
            return ConsensusType.OBSERVER.getValue();
        } else {
            return 0;
        }
//        if (checkObserverListContains(groupId, nodeId, ip, port)) {
//            return ConsensusType.OBSERVER.getValue();
//        } else if (checkSealerListContains(groupId, nodeId, ip, port)) {
//            return ConsensusType.SEALER.getValue();
//        }
//        return 0;
    }

}

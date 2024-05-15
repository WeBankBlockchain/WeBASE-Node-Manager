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

import com.qctc.host.api.model.HostDTO;
import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.enums.ConsensusType;
import com.webank.webase.node.mgr.base.enums.DataStatus;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.config.properties.ConstantProperties;
import com.webank.webase.node.mgr.deploy.chain.ChainService;
import com.webank.webase.node.mgr.deploy.service.AnsibleService;
import com.webank.webase.node.mgr.deploy.service.PathService;
import com.webank.webase.node.mgr.front.FrontService;
import com.webank.webase.node.mgr.front.entity.TbFront;
import com.webank.webase.node.mgr.front.frontinterface.FrontInterfaceService;
import com.webank.webase.node.mgr.front.frontinterface.entity.NodeStatusInfo;
import com.webank.webase.node.mgr.front.frontinterface.entity.PeerOfConsensusStatus;
import com.webank.webase.node.mgr.node.entity.NodeParam;
import com.webank.webase.node.mgr.node.entity.ReqUpdate;
import com.webank.webase.node.mgr.node.entity.TbNode;
import com.webank.webase.node.mgr.tools.JsonTools;
import com.webank.webase.node.mgr.tools.ValidateUtil;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.sdk.v3.client.protocol.response.ConsensusStatus.ConsensusStatusInfo;
import org.fisco.bcos.sdk.v3.client.protocol.response.Peers;
import org.fisco.bcos.sdk.v3.client.protocol.response.SyncStatus.PeersInfo;
import org.fisco.bcos.sdk.v3.client.protocol.response.SyncStatus.SyncStatusInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
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
    public void addNodeInfo(String groupId, String nodeId) throws NodeMgrException {
        String nodeIp = null;
        Integer nodeP2PPort = null;
        String nodeName = getNodeName(groupId, nodeId);
        // add row
        TbNode tbNode = new TbNode();
        tbNode.setNodeId(nodeId);
        tbNode.setGroupId(groupId);
        tbNode.setNodeIp(nodeIp);
        tbNode.setNodeName(nodeName);
        tbNode.setP2pPort(nodeP2PPort);
        nodeMapper.add(tbNode);
    }

    /**
     * add new node data.
     *
     * @param peerInfo 已经用group的nodeId覆盖peerInfo自带的nodeId
     */
    public void addNodeInfo(String groupId, Peers.PeerInfo peerInfo) {
        log.info("start exec method[addNodeInfo]. peerInfo:{} group:{}", peerInfo, groupId);
        String groupNodeId = peerInfo.getP2pNodeID();
        //add db
        TbNode tbNode = nodeMapper.getByNodeIdAndGroupId(groupNodeId, groupId);
        if (Objects.nonNull(tbNode)) {
            log.debug("finish exec method[addNodeInfo]. jump over, found record by node:{} group:{}",
                groupNodeId, groupId);
            return;
        }

        String nodeIp = null;
        Integer nodeP2PPort = null;

        if (StringUtils.isNotBlank(peerInfo.getEndPoint())) {
            String[] ipPort = peerInfo.getEndPoint().split(":");
            nodeIp = ipPort[0];
            nodeP2PPort = Integer.valueOf(ipPort[1]);
        }
        String nodeName = groupId + "_" + groupNodeId;

        // add row
        tbNode = new TbNode();
        tbNode.setAgency("fisco");
        tbNode.setNodeId(peerInfo.getP2pNodeID());
        tbNode.setGroupId(groupId);
        tbNode.setNodeIp(nodeIp);
        tbNode.setNodeName(nodeName);
        tbNode.setP2pPort(nodeP2PPort);
        tbNode.setNodeActive(DataStatus.NORMAL.getValue());
        LocalDateTime now = LocalDateTime.now();
        tbNode.setCreateTime(now);
        tbNode.setModifyTime(now);
        log.info("end exec method[addNodeInfo]. tbNode:{} group:{}", tbNode, groupId);

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
    public List<TbNode> queryByGroupId(String groupId) {
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
    public void deleteByNodeAndGroupId(String nodeId, String groupId) throws NodeMgrException {
        log.info("start deleteByNodeAndGroupId nodeId:{} groupId:{}", nodeId, groupId);
        nodeMapper.deleteByNodeAndGroup(nodeId, groupId);
        log.info("end deleteByNodeAndGroupId");
    }

    /**
     * delete by groupId.
     */
    public void deleteByGroupId(String groupId) {
        if (groupId.isEmpty()) {
            return;
        }
        nodeMapper.deleteByGroupId(groupId);
    }


    /**
     * check status by if node timeout
     */
    public void checkAndUpdateNodeStatus(String groupId) {
        //get local node list
        List<TbNode> nodeList = queryByGroupId(groupId);
        // 如果ip和机构为空，则补齐
        nodeList.stream().filter(n -> StringUtils.isBlank(n.getNodeIp()))
            .forEach(tbNode -> {
                log.info("ip is empty, now complete:{}", tbNode);
                try {
                    // 只有单个front的话，一直拿到的peers都是固定的
                    Peers.PeersInfo selfAndPeer = frontInterface.getPeers(groupId);
                    log.info("complete selfAndPeer:{}", selfAndPeer);
                    // 被访问的节点的groupNodeIDInfo找
                    String selfNodeId = "";
                    for (Peers.NodeIDInfo nodeIDInfo : selfAndPeer.getGroupNodeIDInfo()) {
                        if (nodeIDInfo.getGroup().equalsIgnoreCase(groupId)) {
                            selfNodeId = nodeIDInfo.getNodeIDList().get(0); // todo 用群组的第一个nodeId代表这个节点
                            break;
                        }
                    }
                    log.info("selfNodeId :{}|{}", selfNodeId, tbNode.getNodeId());
                    // 被访问的节点不会返回节点ip，因此不进行updateip的操作

                    // 不等于self，没找到
                    if (!tbNode.getNodeId().equalsIgnoreCase(selfNodeId)) {
                        // 则在peer里找ip
                        selfAndPeer.getPeers().forEach(peerInfo -> {
                            String peerNodeId = "";
                            for (Peers.NodeIDInfo nodeIDInfo : peerInfo.getGroupNodeIDInfo()) {
                                if (nodeIDInfo.getGroup().equalsIgnoreCase(groupId)) {
                                    peerNodeId = nodeIDInfo.getNodeIDList().get(0); // todo 用群组的第一个nodeId代表这个节点
                                    break;
                                }
                            }
                            log.info("peerNodeId :{}|{}", peerNodeId, tbNode.getNodeId());
                            // 找到了peer中对应的groupNodeId 和db的nodeId相等
                            if (peerNodeId.equalsIgnoreCase(tbNode.getNodeId())) {
                                log.info("peerInfo {}|{}", peerInfo.getP2pNodeID(), peerInfo.getEndPoint());
                                if (StringUtils.isNotBlank(peerInfo.getEndPoint())) {
                                    String[] ipPort = peerInfo.getEndPoint().split(":");
                                    String nodeIp = ipPort[0];
                                    Integer nodeP2PPort = Integer.valueOf(ipPort[1]);
                                    tbNode.setNodeIp(nodeIp);
                                    tbNode.setP2pPort(nodeP2PPort);
                                }
                                this.updateNode(tbNode);
                            }
                        });
                    }
                } catch (Exception ex) {
                    log.warn("error get peer error to update ip and agency {}", ex.getMessage());
                }
            });

        List<NodeStatusInfo> nodeStatusInfoList = frontInterface.getNodeStatusList(groupId);
        for (TbNode tbNode : nodeList) {
            String nodeId = tbNode.getNodeId();

            NodeStatusInfo nodeStatusInfo = nodeStatusInfoList.stream()
                .filter(status -> status.getNodeId().equalsIgnoreCase(nodeId))
                .findFirst().orElse(null);
            if (nodeStatusInfo == null) {
                log.info("checkAndUpdateNodeStatus not found status of :{}", nodeId);
                continue;
            }
            tbNode.setBlockNumber(new BigInteger(String.valueOf(nodeStatusInfo.getBlockNumber())));
            tbNode.setNodeActive(nodeStatusInfo.getStatus() == 1 ? DataStatus.NORMAL.getValue() : DataStatus.INVALID.getValue());
            tbNode.setModifyTime(LocalDateTime.now());
            //update node
            updateNode(tbNode);
        }

    }


    /**
     * get latest number of peer on chain.
     */
    private BigInteger getBlockNumberOfNodeOnChain(String groupId, String nodeId) {
        SyncStatusInfo syncStatus = frontInterface.getSyncStatus(groupId);
        if (nodeId.equals(syncStatus.getNodeId())) {
            return new BigInteger(String.valueOf(syncStatus.getBlockNumber()));
        }
        List<PeersInfo> peerList = syncStatus.getPeers();
        // blockNumber
        BigInteger latestNumber = peerList.stream().filter(peer -> nodeId.equals(peer.getNodeId()))
            .map(p -> new BigInteger(String.valueOf(p.getBlockNumber()))).findFirst().orElse(BigInteger.ZERO);
        return latestNumber;
    }


    /**
     * get peer of consensusStatus
     */
    private List<PeerOfConsensusStatus> getPeerOfConsensusStatus(String groupId) {
        ConsensusStatusInfo consensusInfo = frontInterface.getConsensusStatus(groupId);
        if (consensusInfo == null) {
            log.debug("getPeerOfConsensusStatus is null");
            return null;
        }
        //todo  check bug
        List<PeerOfConsensusStatus> dataIsList = new ArrayList<>();
        return dataIsList;
    }


    public List<String> getSealerAndObserverListBySyncStatus(String groupId) {
        log.debug("start getSealerAndObserverListBySyncStatus groupId:{}", groupId);
        List<String> resList = new ArrayList<>();
        SyncStatusInfo syncStatusInfo = frontInterface.getSyncStatus(groupId);
        resList.add(syncStatusInfo.getNodeId());
        resList.addAll(syncStatusInfo.getPeers().stream().map(PeersInfo::getNodeId).collect(
            Collectors.toList()));
        log.debug("end getSealerAndObserverListBySyncStatus resList:{}", resList);
        return resList;
    }



    @Transactional(propagation = Propagation.REQUIRED)
    public TbNode insert(
            String nodeId,
            String nodeName,
            String groupId,
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
    public static String getNodeName(String groupId, String nodeId) {
        return String.format("%s_%s", groupId, nodeId);
    }

    /**
     *
     * @param chainId
     * @param groupId
     * @return
     */
    public List<TbNode> selectNodeListByChainIdAndGroupId(Integer chainId, final String groupId){
        // select all fronts by all agencies
        List<TbFront> tbFrontList = this.frontService.selectFrontListByChainId(chainId);
        log.info("selectNodeListByChainIdAndGroupId tbFrontList:{}, groupId:{}", tbFrontList, groupId);

        // filter only not removed node will be added
        List<TbNode> tbNodeList = tbFrontList.stream()
                .map((front) -> nodeMapper.getByNodeIdAndGroupId(front.getNodeId(), groupId))
                .filter(Objects::nonNull)
                .filter((node) -> node.getGroupId().equals(groupId))
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
    public List<TbNode> selectNodeListByChainIdAndGroupId(List<Integer> newFrontIdList, int chainId, final String groupId){
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
    public TbNode getOldestNodeByChainIdAndGroupId(int chainId, String groupId) {
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
    public void mvNodeOnRemoteHost(HostDTO hostDTO, String rooDirOnHost, String chainName, int hostIndex, String nodeId) {
        // create /opt/fisco/deleted-tmp/default_chain-yyyyMMdd_HHmmss as a parent
        String chainDeleteRootOnHost = PathService.getChainDeletedRootOnHost(rooDirOnHost, chainName);
        ansibleService.execCreateDir(hostDTO, chainDeleteRootOnHost);

        // e.g. /opt/fisco/default_chain
        String chainRootOnHost = PathService.getChainRootOnHost(rooDirOnHost, chainName);
        // e.g. /opt/fisco/default_chain/node[x]
        String src_nodeRootOnHost = PathService.getNodeRootOnHost(chainRootOnHost, hostIndex);

        // move to /opt/fisco/deleted-tmp/default_chain-yyyyMMdd_HHmmss/[nodeid(128)]
        String dst_nodeDeletedRootOnHost =
                PathService.getNodeDeletedRootOnHost(chainDeleteRootOnHost, nodeId);
        // move
        ansibleService.mvDirOnRemote(hostDTO, src_nodeRootOnHost, dst_nodeDeletedRootOnHost);
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
     * get local highest block height, if node equal, return 1, else return 2
     * @param groupId
     * @param nodeId
     * @return
     */
    public int checkNodeType(String groupId, String nodeId) {
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

    }

    /**
     * update node's info in all group
     * @param reqUpdate
     * @return
     */
    public int updateDescription(ReqUpdate reqUpdate) {
        log.debug("updateDescription reqUpdate:{}", reqUpdate);
        int result = this.nodeMapper.updateNodeInfo(reqUpdate);
        log.debug("updateDescription result:{}", result);
        return result;
    }

}

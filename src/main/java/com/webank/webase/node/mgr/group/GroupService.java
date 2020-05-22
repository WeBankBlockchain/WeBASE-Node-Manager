/**
 * Copyright 2014-2020  the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.webank.webase.node.mgr.group;

import com.alibaba.fastjson.JSON;
import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.enums.GroupStatus;
import com.webank.webase.node.mgr.base.enums.GroupType;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.base.properties.ConstantProperties;
import com.webank.webase.node.mgr.base.tools.NodeMgrTools;
import com.webank.webase.node.mgr.block.BlockService;
import com.webank.webase.node.mgr.block.entity.BlockInfo;
import com.webank.webase.node.mgr.block.entity.TbBlock;
import com.webank.webase.node.mgr.contract.ContractService;
import com.webank.webase.node.mgr.front.FrontService;
import com.webank.webase.node.mgr.front.entity.FrontParam;
import com.webank.webase.node.mgr.front.entity.TbFront;
import com.webank.webase.node.mgr.front.entity.TotalTransCountInfo;
import com.webank.webase.node.mgr.frontgroupmap.FrontGroupMapService;
import com.webank.webase.node.mgr.frontgroupmap.entity.FrontGroupMapCache;
import com.webank.webase.node.mgr.frontinterface.FrontInterfaceService;
import com.webank.webase.node.mgr.frontinterface.entity.GenerateGroupInfo;
import com.webank.webase.node.mgr.group.entity.*;
import com.webank.webase.node.mgr.method.MethodService;
import com.webank.webase.node.mgr.node.NodeService;
import com.webank.webase.node.mgr.node.TbNode;
import com.webank.webase.node.mgr.node.entity.PeerInfo;
import com.webank.webase.node.mgr.table.TableService;
import com.webank.webase.node.mgr.transdaily.TransDailyService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

/**
 * services for group data.
 */
@Log4j2
@Service
public class GroupService {

    @Autowired
    private GroupMapper groupMapper;
    @Autowired
    private TableService tableService;
    @Autowired
    private FrontInterfaceService frontInterface;
    @Autowired
    private FrontService frontService;
    @Autowired
    private FrontGroupMapCache frontGroupMapCache;
    @Autowired
    private FrontGroupMapService frontGroupMapService;
    @Autowired
    private NodeService nodeService;
    @Autowired
    private ContractService contractService;
    @Autowired
    private MethodService methodService;
    @Autowired
    private TransDailyService transDailyService;
    @Autowired
    private ConstantProperties constants;
    @Autowired
    private BlockService blockService;

    /**
     * query count of group.
     */
    public Integer countOfGroup(Integer groupId, Integer groupStatus) throws NodeMgrException {
        log.debug("start countOfGroup groupId:{}", groupId);
        try {
            Integer count = groupMapper.getCount(groupId, groupStatus);
            log.debug("end countOfGroup groupId:{} count:{}", groupId, count);
            return count;
        } catch (RuntimeException ex) {
            log.error("fail countOfGroup", ex);
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }
    }

    /**
     * query all group info.
     */
    public List<TbGroup> getGroupList(Integer groupStatus) throws NodeMgrException {
        log.debug("start getGroupList");
        try {
            List<TbGroup> groupList = groupMapper.getList(groupStatus);

            log.debug("end getGroupList groupList:{}", JSON.toJSONString(groupList));
            return groupList;
        } catch (RuntimeException ex) {
            log.error("fail getGroupList", ex);
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }
    }


    /**
     * update status.
     */
    public void updateGroupStatus(int groupId, int groupStatus) {
        log.debug("start updateGroupStatus groupId:{} groupStatus:{}", groupId, groupStatus);
        groupMapper.updateStatus(groupId, groupStatus);
        log.debug("end updateGroupStatus groupId:{} groupStatus:{}", groupId, groupStatus);

    }

    /**
     * Check the validity of the groupId.
     * @throws NodeMgrException INVALID_GROUP_ID
     */
    public void checkGroupId(Integer groupId) throws NodeMgrException {
        log.debug("start checkGroupId groupId:{}", groupId);

        if (groupId == null) {
            log.error("fail checkGroupId groupId is null");
            throw new NodeMgrException(ConstantCode.GROUP_ID_NULL);
        }

        Integer groupCount = countOfGroup(groupId, null);
        log.debug("checkGroupId groupId:{} groupCount:{}", groupId, groupCount);
        if (groupCount == null || groupCount == 0) {
            throw new NodeMgrException(ConstantCode.INVALID_GROUP_ID);
        }
        log.debug("end checkGroupId");
    }

    /**
     * query latest statistical trans.
     */
    public List<StatisticalGroupTransInfo> queryLatestStatisticalTrans() throws NodeMgrException {
        log.debug("start queryLatestStatisticalTrans");
        try {
            // qurey list
            List<StatisticalGroupTransInfo> listStatisticalTrans = groupMapper
                    .queryLatestStatisticalTrans();
            log.debug("end queryLatestStatisticalTrans listStatisticalTrans:{}",
                    JSON.toJSONString(listStatisticalTrans));
            return listStatisticalTrans;
        } catch (RuntimeException ex) {
            log.error("fail queryLatestStatisticalTrans", ex);
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }
    }

    /**
     * query group overview information.
     */
    public GroupGeneral queryGroupGeneral(int groupId) throws NodeMgrException {
        log.debug("start queryGroupGeneral groupId:{}", groupId);
        GroupGeneral generalInfo = groupMapper.getGeneral(groupId);
        if (generalInfo != null) {
            TotalTransCountInfo transCountInfo = frontInterface.getTotalTransactionCount(groupId);
            generalInfo.setLatestBlock(transCountInfo.getBlockNumber());
            generalInfo.setTransactionCount(transCountInfo.getTxSum());
        }
        return generalInfo;
    }


    /**
     * reset groupList.
     */
    @Transactional
    public void resetGroupList() {
        Instant startTime = Instant.now();
        log.info("start resetGroupList. startTime:{}", startTime.toEpochMilli());

        //all groupId from chain
        Set<Integer> allGroupSet = new HashSet<>();

        //get all front
        List<TbFront> frontList = frontService.getFrontList(new FrontParam());
        if (frontList == null || frontList.size() == 0) {
            return;
        }
        //get group from chain
        for (TbFront front : frontList) {
            String frontIp = front.getFrontIp();
            int frontPort = front.getFrontPort();
            //query group list
            List<String> groupIdList;
            try {
                groupIdList = frontInterface.getGroupListFromSpecificFront(frontIp, frontPort);
            } catch (Exception ex) {
                log.error("fail getGroupListFromSpecificFront.", ex);
                continue;
            }
            for (String groupId : groupIdList) {
                Integer gId = Integer.valueOf(groupId);
                allGroupSet.add(gId);
                // peer in group
                List<String> groupPeerList = frontInterface.getGroupPeersFromSpecificFront(frontIp, frontPort, gId);
                // check group not existed or node count differs
                TbGroup checkGroup = getGroupById(gId);
                if (Objects.isNull(checkGroup) || groupPeerList.size() != checkGroup.getNodeCount()) {
                    // save group
                    saveGroup(gId, groupPeerList.size(), "synchronous",
                            GroupType.SYNC.getValue(), GroupStatus.NORMAL.getValue());
                }
                frontGroupMapService.newFrontGroup(front.getFrontId(), gId);
                //save new peers
                savePeerList(frontIp, frontPort, gId, groupPeerList);
                //remove invalid peers
                removeInvalidPeer(gId, groupPeerList);
                //refresh: add sealer and observer no matter validity
                frontService.refreshSealerAndObserverInNodeList(frontIp, frontPort, gId);
            }
        }

        // check group status(normal or maintaining)
        checkAndUpdateGroupStatus(allGroupSet);
        // check group's genesis block same with each other
        checkGroupGenesisSameWithEach();
        // check group if has dirty data
        checkSameChainDataWithLocal();
        // remove front_group_map that not in tb_front or tb_group
        frontGroupMapService.removeInvalidFrontGroupMap();
        //clear cache
        frontGroupMapCache.clearMapList();

        log.info("end resetGroupList. useTime:{} ",
                Duration.between(startTime, Instant.now()).toMillis());
    }


	/**
     * save new peers that not in group peers
     */
    private void savePeerList(String frontIp, Integer frontPort, int groupId, List<String> groupPeerList) {
        //get all local nodes
        List<TbNode> localNodeList = nodeService.queryByGroupId(groupId);
        //get peers on chain
        PeerInfo[] peerArr = frontInterface.getPeersFromSpecificFront(frontIp, frontPort, groupId);
        List<PeerInfo> peerList = Arrays.asList(peerArr);
        //save new nodes
        for (String nodeId : groupPeerList) {
            long count = localNodeList.stream().filter(
                    ln -> nodeId.equals(ln.getNodeId()) && groupId == ln.getGroupId()).count();
            log.error("=========== savePeerList:{}", count);
            // local node not contains this one:
            if (count == 0) {
                PeerInfo newPeer = peerList.stream().filter(peer -> nodeId.equals(peer.getNodeId()))
                        .findFirst().orElseGet(() -> new PeerInfo(nodeId));
                nodeService.addNodeInfo(groupId, newPeer);
            }
        }
    }


    /**
     * remove all group.
     */
    private void removeAllGroup() {
        List<TbGroup> allGroup = getGroupList(null);
        if (CollectionUtils.isEmpty(allGroup)) {
            log.info("removeAllGroup jump over. not fount any group");
            return;
        }

        //remove each group
        allGroup.stream().forEach(group -> removeAllDataByGroupId(group.getGroupId()));
    }

    /**
     * remove invalid peer.
     */
    private void removeInvalidPeer(int groupId, List<String> groupPeerList) {
        if (groupId == 0) {
            return;
        }
        //get local peers
        List<TbNode> localNodes = nodeService.queryByGroupId(groupId);
        if (CollectionUtils.isEmpty(localNodes)) {
            return;
        }
        //remove node that's not in groupPeerList and not in sealer/observer list
        localNodes.stream().filter(node -> !groupPeerList.contains(node.getNodeId())
                && !checkSealerAndObserverListContains(groupId, node.getNodeId()))
                .forEach(n -> nodeService.deleteByNodeAndGroupId(n.getNodeId(), groupId));
    }

    private boolean checkSealerAndObserverListContains(int groupId, String nodeId) {
        log.debug("checkSealerAndObserverListNotContains nodeId:{},groupId:{}",
                nodeId, groupId);
        //get sealer and observer on chain
        List<PeerInfo> sealerAndObserverList = nodeService.getSealerAndObserverList(groupId);
        for (PeerInfo peerInfo : sealerAndObserverList) {
            if (nodeId.equals(peerInfo.getNodeId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * check group status and Update
     */
    private void checkAndUpdateGroupStatus(Set<Integer> allGroupOnChain) {
        if (CollectionUtils.isEmpty(allGroupOnChain)) {
            return;
        }

        List<TbGroup> allLocalGroup = getGroupList(null);
        if (CollectionUtils.isEmpty(allLocalGroup)) {
            return;
        }

        for (TbGroup localGroup : allLocalGroup) {
            int localGroupId = localGroup.getGroupId();
            long count = 0;
            count = allGroupOnChain.stream().filter(id -> id == localGroupId).count();
            try {
                // found groupId in groupOnChain, local status is invalid, set as normal
                if (count > 0 && localGroup.getGroupStatus() == GroupStatus.MAINTAINING.getValue()) {
                    log.info("group is normal, localGroupId:{}", localGroupId);
                    //update NORMAL
                    updateGroupStatus(localGroupId, GroupStatus.NORMAL.getValue());
                    continue;
                }

                // if not found in groupOnChain and local status is normal, set as invalid
                if (count == 0 && GroupStatus.NORMAL.getValue() == localGroup.getGroupStatus()) {
                    // update invalid
					log.warn("group is invalid, localGroupId:{}", localGroupId);
					updateGroupStatus(localGroupId, GroupStatus.MAINTAINING.getValue());
                    continue;
                }

            } catch (Exception ex) {
                log.info("fail check group. localGroup:{}", JSON.toJSONString(localGroup));
                continue;
            }

        }
    }

	/**
	 * check group's genesis block the same with each other on chain
	 */
	private void checkGroupGenesisSameWithEach() {
	    log.info("start checkGroupGenesisSameWithEach.");
		// get all front
		List<TbFront> frontList = frontService.getFrontList(new FrontParam());
		if (frontList == null || frontList.size() == 0) {
            log.warn("checkGroupGenesisSameWithEach not found any front.");
            return;
		}
		List<TbGroup> allNormalGroupList = getGroupList(GroupStatus.NORMAL.getValue());
		if (allNormalGroupList.isEmpty()) {
            log.warn("checkGroupGenesisSameWithEach not found any group of front.");
            return;
		}

		for (TbGroup tbGroup : allNormalGroupList) {
			int groupId = tbGroup.getGroupId();
            String lastBlockHash = "";
            for (TbFront front : frontList) {
				String frontIp = front.getFrontIp();
				int frontPort = front.getFrontPort();
				// check genesis block
                BlockInfo genesisBlock = frontInterface.getBlockByNumberFromSpecificFront(frontIp,
                        frontPort, groupId, BigInteger.ZERO);
                if (genesisBlock == null) {
                    log.warn("checkGroupGenesisSameWithEach getGenesisBlock is null");
                    continue;
                }
                if (!"".equals(lastBlockHash) && !lastBlockHash.equals(genesisBlock.getHash())) {
                    log.warn("checkGroupGenesisSameWithEach genesis block hash conflicts with other group," +
                            " groupId:{}, frontId:{}", groupId, front.getFrontId());
                    updateGroupStatus(groupId, GroupStatus.CONFLICT_GROUP_GENESIS.getValue());
                }
                lastBlockHash = genesisBlock.getHash();
                log.debug("checkGroupGenesisSameWithEach, groupId:{}, frontId:{}, genesis blockHash:{}",
                        groupId, front.getFrontId(), lastBlockHash);
			}
		}
	}

    /**
     * check local block's hash same with blockHash on chain
     * group status abnormal cases:
     * @case1 rebuild chain(different genesis),
     * @case2 drop group data and restart chain(same genesis), when local smallest block height greater than 0, mark as CONFLICT
     */
    private void checkSameChainDataWithLocal() {
        log.info("start checkSameChainData.");
        // get all group
        List<TbGroup> allNormalGroupList = getGroupList(null);
        if (allNormalGroupList.isEmpty()) {
            log.warn("checkSameChainData not found any group of front.");
            return;
        }

        for (TbGroup tbGroup : allNormalGroupList) {
            int groupId = tbGroup.getGroupId();
            // find smallest block from db of group
            TbBlock smallestBlockLocal = blockService.getSmallestBlockInfo(groupId);
            // if no block in local db
            if (smallestBlockLocal == null) {
                log.warn("checkSameChainDataWithLocal groupId {} smallestBlockLocal is null", groupId);
                continue;
            }
            BigInteger blockHeightLocal = smallestBlockLocal.getBlockNumber();
			String blockHashLocal = smallestBlockLocal.getPkHash();

            // get same height block from chain, contrast block hash
            BlockInfo smallestBlockOnChain = frontInterface.getBlockByNumber(groupId, blockHeightLocal);
            // if no block in each node, not same chain
            if (smallestBlockOnChain == null) {
                log.warn("checkSameChainDataWithLocal groupId: {} block of height: {} on chain not exists, " +
                        "conflict with local block of same height", groupId, blockHeightLocal);
                updateGroupStatus(groupId, GroupStatus.CONFLICT_LOCAL_DATA.getValue());
                continue;
            }
			String blockHashOnChain = smallestBlockOnChain.getHash();
            log.debug("checkSameChainData groupId:{},blockHeight:{},localHash:{},chainHash:{} ",
                    groupId, blockHeightLocal, blockHashLocal, blockHashOnChain);
            // check same block hash, the same chain
            if (!blockHashLocal.equals(blockHashOnChain)) {
                log.warn("checkSameChainDataWithLocal group: {} block of height:{} on chain " +
								"conflicts with local block data",
                        groupId, blockHeightLocal);
                updateGroupStatus(groupId, GroupStatus.CONFLICT_LOCAL_DATA.getValue());
                continue;
            } else {
                log.warn("checkSameChainDataWithLocal set groupId:{} as normal", groupId);
                updateGroupStatus(groupId, GroupStatus.NORMAL.getValue());
            }
        }

    }


    /**
     * remove all data by groupId.
     * included: tb_group, tb_front_group_map, group contract/trans, group method, group node etc.
     */
    protected void removeAllDataByGroupId(int groupId) {
        if (groupId == 0) {
            return;
        }
        checkGroupId(groupId);
        log.warn("removeAllDataByGroupId! groupId:{}", groupId);
        //remove groupId.
        groupMapper.remove(groupId);
        //remove mapping.
        frontGroupMapService.removeByGroupId(groupId);
        //remove contract
        contractService.deleteByGroupId(groupId);
        //remove method
        methodService.deleteByGroupId(groupId);
        //remove node
        nodeService.deleteByGroupId(groupId);
        //remove transDaily
        transDailyService.deleteByGroupId(groupId);
        //drop table.
        tableService.dropTableByGroupId(groupId);
        log.warn("end removeAllDataByGroupId");
    }

    /**
     * generate group to single node.
     *
     * @param req info
     * @return
     */
    public TbGroup generateToSingleNode(String nodeId, ReqGenerateGroup req) {
        Integer generateGroupId = req.getGenerateGroupId();

        TbFront tbFront = frontService.getByNodeId(nodeId);
        if (tbFront == null) {
            log.error("fail generateToSingleNode node front not exists.");
            throw new NodeMgrException(ConstantCode.NODE_NOT_EXISTS);
        }
        // request front to generate
        GenerateGroupInfo generateGroupInfo = new GenerateGroupInfo();
        BeanUtils.copyProperties(req, generateGroupInfo);
        frontInterface.generateGroup(tbFront.getFrontIp(), tbFront.getFrontPort(),
                generateGroupInfo);
        // save group, saved as invalid status until start
        TbGroup tbGroup = saveGroup(generateGroupId, req.getNodeList().size(),
                req.getDescription(), GroupType.MANUAL.getValue(), GroupStatus.MAINTAINING.getValue(),
                req.getTimestamp(), req.getNodeList());
        return tbGroup;
    }

    /**
     * generate group.
     *
     * @param req info
     * @return
     */
    public TbGroup generateGroup(ReqGenerateGroup req) {
        Integer generateGroupId = req.getGenerateGroupId();
        if (checkGroupIdExisted(generateGroupId)) {
            throw new NodeMgrException(ConstantCode.GROUP_ID_EXISTS);
        }

        for (String nodeId : req.getNodeList()) {
            // get front
            TbFront tbFront = frontService.getByNodeId(nodeId);
            if (tbFront == null) {
                log.error("fail generateGroup node front not exists.");
                throw new NodeMgrException(ConstantCode.NODE_NOT_EXISTS);
            }
            // request front to generate
            GenerateGroupInfo generateGroupInfo = new GenerateGroupInfo();
            BeanUtils.copyProperties(req, generateGroupInfo);
            frontInterface.generateGroup(tbFront.getFrontIp(), tbFront.getFrontPort(),
                    generateGroupInfo);
        }
        // save group, saved as invalid status until start
        TbGroup tbGroup = saveGroup(generateGroupId, req.getNodeList().size(),
                req.getDescription(), GroupType.MANUAL.getValue(), GroupStatus.MAINTAINING.getValue(),
                req.getTimestamp(), req.getNodeList());
        return tbGroup;
    }

    /**
     * operate group.
     *
     * @param nodeId
     * @param groupId
     * @param type
     * @return
     */
    public Object operateGroup(String nodeId, Integer groupId, String type) {
        // get front
        TbFront tbFront = frontService.getByNodeId(nodeId);
        if (tbFront == null) {
            log.error("fail operateGroup node front not exists.");
            throw new NodeMgrException(ConstantCode.NODE_NOT_EXISTS);
        }
        // request front to operate
        Object groupOperateStatus = frontInterface.operateGroup(tbFront.getFrontIp(),
                tbFront.getFrontPort(), groupId, type);
        // refresh group status, not remove tb_group if within gray period (yaml-groupInvalidGrayscaleValue)
        resetGroupList();

        // return
        return groupOperateStatus;
    }

    /**
     * list groupStatus Of each node of NodeList
     * @param nodeIdList
     * @param groupIdList
     * @return GroupStatusInfo
     */
    public List<RspGroupStatus> listGroupStatus(List<String> nodeIdList, List<Integer> groupIdList) {
        List<RspGroupStatus> resList = new ArrayList<>(nodeIdList.size());
        for (String nodeId : nodeIdList) {
            Map<String, String> statusMap = new HashMap<>();
            statusMap = getGroupStatus(nodeId, groupIdList);

            RspGroupStatus rspGroupStatus = new RspGroupStatus(nodeId, statusMap);
            resList.add(rspGroupStatus);
        }
        return resList;
    }

    /**
     * getGroupStatus of single node's sight
     * @param nodeId
     * @param groupIdList
     * @return map of <groupId, status>
     */
    private Map<String, String> getGroupStatus(String nodeId, List<Integer> groupIdList) {
        // get front
        TbFront tbFront = frontService.getByNodeId(nodeId);
        if (tbFront == null) {
            log.error("fail getGroupStatus node front not exists.");
            throw new NodeMgrException(ConstantCode.NODE_NOT_EXISTS);
        }
        Map<String, String> statusRes = frontInterface.queryGroupStatus(tbFront.getFrontIp(),
                tbFront.getFrontPort(), nodeId, groupIdList);
        return statusRes;
    }

    /**
     * batch start group.
     *
     * @param req
     */
    public void batchStartGroup(ReqBatchStartGroup req) {
        log.debug("start batchStartGroup:{}", req);
        Integer groupId = req.getGenerateGroupId();
        // check id
        checkGroupId(groupId);
        for (String nodeId : req.getNodeList()) {
            // get front
            TbFront tbFront = frontService.getByNodeId(nodeId);
            if (tbFront == null) {
                log.error("fail batchStartGroup node not exists.");
                throw new NodeMgrException(ConstantCode.NODE_NOT_EXISTS);
            }
            // request front to start
            frontInterface.operateGroup(tbFront.getFrontIp(), tbFront.getFrontPort(), groupId,
                    "start");
        }
        // refresh group status
        resetGroupList();
        log.debug("end batchStartGroup.");
    }

    /**
     * save group id
     */
    @Transactional
    public TbGroup saveGroup(int groupId, int nodeCount, String description,
                             int groupType, int groupStatus) {
        if (groupId == 0) {
            return null;
        }
        // save group id
        String groupName = "group" + groupId;
        TbGroup tbGroup =
                new TbGroup(groupId, groupName, nodeCount, description,
                        groupType, groupStatus);
        groupMapper.save(tbGroup);
        // create table by group id
        tableService.newTableByGroupId(groupId);
        return tbGroup;
    }

    @Transactional
    public TbGroup saveGroup(int groupId, int nodeCount, String description, int groupType,
                             int groupStatus, BigInteger timestamp, List<String> nodeIdList) {
        if (groupId == 0) {
            return null;
        }
        // save group id
        String groupName = "group" + groupId;
        TbGroup tbGroup =
                new TbGroup(groupId, groupName, nodeCount, description,
                        groupType, groupStatus);
        tbGroup.setGroupTimestamp(timestamp.toString(10));
        tbGroup.setNodeIdList(JSON.toJSONString(nodeIdList));
        groupMapper.save(tbGroup);
        // create table by group id
        tableService.newTableByGroupId(groupId);
        return tbGroup;
    }

    /**
     * Check the validity of the groupId.
     */
    public boolean checkGroupIdExisted(Integer groupId) throws NodeMgrException {
        log.debug("start checkGroupIdExisted groupId:{}", groupId);

        if (groupId == null) {
            log.error("fail checkGroupIdExisted groupId is null");
            throw new NodeMgrException(ConstantCode.GROUP_ID_NULL);
        }

        Integer groupCount = countOfGroup(groupId, null);
        log.debug("checkGroupIdExisted groupId:{} groupCount:{}", groupId, groupCount);
        if (groupCount != null && groupCount > 0) {
            return true;
        }
        log.debug("end checkGroupIdExisted");
        return false;
    }

    public TbGroup getGroupById(Integer groupId) {
        log.debug("getGroupById groupId:{}", groupId);
        return groupMapper.getGroupById(groupId);
    }

}

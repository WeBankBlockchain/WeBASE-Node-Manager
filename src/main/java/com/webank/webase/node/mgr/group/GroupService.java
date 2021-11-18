/**
 * Copyright 2014-2021  the original author or authors.
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

import static com.webank.webase.node.mgr.base.code.ConstantCode.INSERT_GROUP_ERROR;

import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.enums.DataStatus;
import com.webank.webase.node.mgr.base.enums.FrontStatusEnum;
import com.webank.webase.node.mgr.base.enums.GroupStatus;
import com.webank.webase.node.mgr.base.enums.GroupType;
import com.webank.webase.node.mgr.base.enums.RunTypeEnum;
import com.webank.webase.node.mgr.base.enums.ScpTypeEnum;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.block.BlockService;
import com.webank.webase.node.mgr.block.entity.TbBlock;
import com.webank.webase.node.mgr.config.properties.ConstantProperties;
import com.webank.webase.node.mgr.contract.CnsService;
import com.webank.webase.node.mgr.contract.ContractService;
import com.webank.webase.node.mgr.contract.abi.AbiService;
import com.webank.webase.node.mgr.deploy.chain.ChainService;
import com.webank.webase.node.mgr.deploy.entity.NodeConfig;
import com.webank.webase.node.mgr.deploy.entity.TbChain;
import com.webank.webase.node.mgr.deploy.entity.TbHost;
import com.webank.webase.node.mgr.deploy.mapper.TbHostMapper;
import com.webank.webase.node.mgr.deploy.service.AnsibleService;
import com.webank.webase.node.mgr.deploy.service.DeployShellService;
import com.webank.webase.node.mgr.deploy.service.PathService;
import com.webank.webase.node.mgr.external.ExtAccountService;
import com.webank.webase.node.mgr.external.ExtContractService;
import com.webank.webase.node.mgr.front.FrontMapper;
import com.webank.webase.node.mgr.front.FrontService;
import com.webank.webase.node.mgr.front.entity.FrontParam;
import com.webank.webase.node.mgr.front.entity.TbFront;
import com.webank.webase.node.mgr.front.entity.TotalTransCountInfo;
import com.webank.webase.node.mgr.front.frontinterface.FrontInterfaceService;
import com.webank.webase.node.mgr.frontgroupmap.FrontGroupMapCache;
import com.webank.webase.node.mgr.frontgroupmap.FrontGroupMapService;
import com.webank.webase.node.mgr.frontgroupmap.entity.FrontGroup;
import com.webank.webase.node.mgr.frontgroupmap.entity.MapListParam;
import com.webank.webase.node.mgr.group.entity.GroupGeneral;
import com.webank.webase.node.mgr.group.entity.StatisticalGroupTransInfo;
import com.webank.webase.node.mgr.group.entity.TbGroup;
import com.webank.webase.node.mgr.method.MethodService;
import com.webank.webase.node.mgr.node.NodeService;
import com.webank.webase.node.mgr.node.entity.TbNode;
import com.webank.webase.node.mgr.statistic.StatService;
import com.webank.webase.node.mgr.table.TableService;
import com.webank.webase.node.mgr.tools.CleanPathUtil;
import com.webank.webase.node.mgr.tools.JsonTools;
import com.webank.webase.node.mgr.tools.ProgressTools;
import com.webank.webase.node.mgr.transdaily.TransDailyService;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.fisco.bcos.sdk.client.protocol.response.BcosBlock;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * services for group data.
 */
@Log4j2
@Service
public class GroupService {

    @Autowired
    private GroupMapper groupMapper;
    @Autowired
    private TbHostMapper tbHostMapper;
    @Autowired
    private FrontMapper frontMapper;

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
    private BlockService blockService;
    @Autowired
    private DeployShellService deployShellService;
    @Autowired
    private PathService pathService;
    @Autowired
    private ConstantProperties constantProperties;
    @Autowired
    private AbiService abiService;
    @Autowired
    private CnsService cnsService;
    @Autowired
    private ExtAccountService extAccountService;
    @Autowired
    private ExtContractService extContractService;
    @Autowired
    private StatService statService;


    @Autowired private ChainService chainService;
    @Autowired private AnsibleService ansibleService;

    public static final String RUNNING_GROUP = "RUNNING";
    public static final String OPERATE_START_GROUP = "start";
    public static final String OPERATE_STOP_GROUP = "stop";
    public static final String OPERATE_STATUS_GROUP = "getStatus";

    /**
     * query count of group.
     */
    public Integer countOfGroup(String groupId, Integer groupStatus) throws NodeMgrException {
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

            log.debug("end getGroupList groupList:{}", JsonTools.toJSONString(groupList));
            return groupList;
        } catch (RuntimeException ex) {
            log.error("fail getGroupList", ex);
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }
    }


    /**
     * update status.
     */
    public void updateGroupStatus(String groupId, int groupStatus) {
        log.debug("start updateGroupStatus groupId:{} groupStatus:{}", groupId, groupStatus);
        int res = groupMapper.updateStatus(groupId, groupStatus);
        log.debug("end updateGroupStatus res:{} groupId:{} groupStatus:{}", res, groupId, groupStatus);

    }

    /**
     * Check the validity of the groupId.
     *
     * @throws NodeMgrException INVALID_GROUP_ID
     */
    public void checkGroupId(String groupId) throws NodeMgrException {
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
            // query list
            List<StatisticalGroupTransInfo> listStatisticalTrans = groupMapper
                    .queryLatestStatisticalTrans();
            log.debug("end queryLatestStatisticalTrans listStatisticalTrans:{}",
                    JsonTools.toJSONString(listStatisticalTrans));
            return listStatisticalTrans;
        } catch (RuntimeException ex) {
            log.error("fail queryLatestStatisticalTrans", ex);
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }
    }

    /**
     * query group overview information.
     */
    public GroupGeneral queryGroupGeneral(String groupId) throws NodeMgrException {
        log.debug("start queryGroupGeneral groupId:{}", groupId);
        GroupGeneral generalInfo = this.getGeneralAndUpdateNodeCount(groupId);
        if (generalInfo != null) {
            TotalTransCountInfo transCountInfo = frontInterface.getTotalTransactionCount(groupId);
            if (transCountInfo != null) {
                generalInfo.setLatestBlock(transCountInfo.getBlockNumber());
                generalInfo.setTransactionCount(transCountInfo.getTxSum());
            }
        }
        return generalInfo;
    }

    /**
     * update group count when refresh
     * @param groupId
     * @return
     */
    private GroupGeneral getGeneralAndUpdateNodeCount(String groupId) {
        List<String> groupPeers = frontInterface.getGroupPeers(groupId);
        groupMapper.updateNodeCount(groupId, groupPeers.size());
        log.debug("getGeneralAndUpdateNodeCount gId:{} count:{}", groupId, groupPeers.size());
        GroupGeneral groupGeneral = groupMapper.getGeneral(groupId);
        // v1.5.0 count all contract on chain
        return groupGeneral;
    }

    /**
     * reset groupList.
     * synchronized to avoid deadlock
     */
    @Transactional(isolation= Isolation.READ_COMMITTED)
    public synchronized void resetGroupList() {
        if (!chainService.runTask()) {
            log.warn("resetGroupList jump over for runTask");
            return;
        }

        Instant startTime = Instant.now();
        log.info("start resetGroupList. startTime:{}", startTime.toEpochMilli());

        // all groupId from chain and all node, to check the whole group whether normal
        Set<String> allGroupSet = new HashSet<>();

        // get all front
        List<TbFront> frontList = frontMapper.getAllList();
        if (frontList == null || frontList.size() == 0) {
            log.info("resetGroupList frontList empty, jump over");
            return;
        }

        // clear cache
        frontGroupMapCache.clearMapList();

        // save group and nodes(peers, sealer, observer) and front_group_map from chain
        // update front_group_map by group list on chain
        saveDataOfGroup(frontList, allGroupSet);

        //remove invalid peers
        removeInvalidPeer(frontList);

        // check group status(normal or maintaining), update by local group list
        // if groupid not in allGroupSet, remove it
        // todo remove, because dynamic group manage removed
        checkAndUpdateGroupStatus(allGroupSet);


        // check group local whether has dirty data by contrast of local blockHash with chain blockHash
        // if not same, update group as DIRTY
        checkSameChainDataWithLocal();
        // check group's genesis block same with each other front,
        // if not, update group as CONFLICT
        checkGroupGenesisSameWithEach();
        // remove front_group_map that not in tb_front or tb_group by local data
        // v1.4.3 remove
        frontGroupMapService.removeInvalidFrontGroupMap();
        // update front_group_map status of local group
        checkGroupMapByLocalGroupList(frontList);
        log.info("end resetGroupList. useTime:{} ",
                Duration.between(startTime, Instant.now()).toMillis());
    }

    /**
     * check group status(normal or maintaining), update by local group list
     * if groupid not in allGroupSet, remove it
     * @param frontList all front
     * @param allGroupSet to record all group from each front
     */
    @Transactional(propagation = Propagation.REQUIRED)
    private void saveDataOfGroup(List<TbFront> frontList, Set<String> allGroupSet) {
        log.info("saveDataOfGroup frontList:{}", frontList);
        for (TbFront front : frontList) {
            String frontIp = front.getFrontIp();
            int frontPort = front.getFrontPort();
            // query group list from chain
            List<String> groupIdList;
            try {
                // if observer to removed, this observer would still return groupId
                groupIdList = frontInterface.getGroupListFromSpecificFront(frontIp, frontPort);
            } catch (Exception ex) {
                log.error("saveDataOfGroup fail getGroupListFromSpecificFront.", ex);
                continue;
            }
            // update by group list on chain
            log.info("saveDataOfGroup groupIdList:{}", groupIdList);
            for (String groupId : groupIdList) {

                allGroupSet.add(groupId);
                // peer in group
                List<String> groupPeerList;
                try {
                    // if observer set removed, it still return itself as observer
                    groupPeerList = frontInterface.getGroupPeersFromSpecificFront(frontIp, frontPort, groupId);
                } catch (Exception e) {
                    // case: if front1 group1 stopped, getGroupPeers error, update front1_group1_map invalid fail
                    log.warn("saveDataOfGroup getGroupPeersFromSpecificFront fail, frontId:{}, groupId:{}",
                            front.getFrontId(), groupId);
                    continue;
                }
                // check group not existed or node count differs
                // save group entity
                TbGroup checkGroupExist = getGroupById(groupId);
                if (Objects.isNull(checkGroupExist) || groupPeerList.size() != checkGroupExist.getNodeCount()) {
                    Integer encryptType = frontInterface.getEncryptType(groupId);
                    saveGroup(groupId, groupPeerList.size(), "synchronous",
                            GroupType.SYNC, GroupStatus.NORMAL,
                        front.getChainId(), front.getChainName(), encryptType);
                }
                // refresh front group map by group list on chain
                // different from checkGroupMapByLocalGroupList which update by local groupList
                // 1.4.3 add consensus type of front group map
                frontGroupMapService.newFrontGroup(front, groupId);

                //save new peers(tb_node)
                savePeerList(groupId, groupPeerList);

            }
        }
    }

    /**
     * separated from saveDataOfGroup, separated save and delete, delete first
     * @param frontList
     */
    private void removeInvalidPeer(List<TbFront> frontList) {
        log.info("removeInvalidPeer frontList:{}", frontList);
        for (TbFront front : frontList) {
            String frontIp = front.getFrontIp();
            int frontPort = front.getFrontPort();
            // query group list from chain
            List<String> groupIdList;
            try {
                // if observer to removed, this observer would still return groupId
                groupIdList = frontInterface.getGroupListFromSpecificFront(frontIp, frontPort);
            } catch (Exception ex) {
                log.error("removeInvalidPeer fail getGroupListFromSpecificFront.", ex);
                continue;
            }
            // update by group list on chain
            log.info("removeInvalidPeer groupIdList:{}", groupIdList);
            for (String groupId : groupIdList) {

                // peer in group
                List<String> nodeInGroup;
                try {
                    // if observer set removed, it still return itself as observer
                    nodeInGroup = frontInterface.getSealerObserverFromSpecificFront(frontIp, frontPort, groupId);
                } catch (Exception e) {
                    // case: if front1 group1 stopped, getGroupPeers error, update front1_group1_map invalid fail
                    log.warn("saveDataOfGroup getGroupPeersFromSpecificFront fail, frontId:{}, groupId:{}",
                        front.getFrontId(), groupId);
                    continue;
                }
                removeInvalidPeer(groupId, nodeInGroup);
            }
        }
    }

    /**
     * remove invalid peer.
     */
    private void removeInvalidPeer(String groupId, List<String> nodeInGroup) {
        if (groupId.isEmpty()) {
            return;
        }
        //get local peers
        List<TbNode> localNodes = nodeService.queryByGroupId(groupId);
        if (CollectionUtils.isEmpty(localNodes)) {
            return;
        }
        //remove node that's not in groupPeerList and not in sealer/observer list
        // 1.4.3 if observer is removed, observer's nodeId still in groupPeerList
        localNodes.stream()
                .filter(n -> !DataStatus.starting(n.getNodeActive()))
                .forEach(node -> {
                    // todo check: if front connected to rpc node, and this rpc node connected to observer node,
                    // and if this observer was removed, then nodeInGroup might still contains this observer as an observer but not a removed node
//                    boolean isRemoved = !checkSealerAndObserverListContains(groupId, node.getNodeId());
//                    if(isRemoved || !nodeInGroup.contains(node.getNodeId()) ) {
                    if(!nodeInGroup.contains(node.getNodeId()) ) {
                        nodeService.deleteByNodeAndGroupId(node.getNodeId(), groupId);
                    }
                });
    }

    /**
     * simular as frontInterface's getGroupPeers
     * but if observer is removed, observer's nodeId still in groupPeerList
     * @param groupId
     * @param nodeId
     * @return
     */
    @Deprecated
    private boolean checkSealerAndObserverListContains(String groupId, String nodeId) {
        //get sealer and observer on chain
        List<String> sealerAndObserverList = nodeService.getSealerAndObserverListBySyncStatus(groupId);
        for (String nId : sealerAndObserverList) {
            if (nodeId.equals(nId)) {
                log.debug("checkSealerAndObserverListNotContains true nodeId:{},groupId:{}",
                    nodeId, groupId);
                return true;
            }
        }
        log.debug("checkSealerAndObserverListNotContains false nodeId:{},groupId:{} ",
            nodeId, groupId);
        return false;
    }

    /**
     * save new peers that not in group peers
     */
    private void savePeerList(String groupId, List<String> groupPeerList) {
        //get all local nodes
        List<TbNode> localNodeList = nodeService.queryByGroupId(groupId);
        //save new nodes
        for (String nodeId : groupPeerList) {
            // if local has this node, count = 1
            long count = localNodeList.stream()
                .filter(ln -> nodeId.equals(ln.getNodeId()) && groupId.equals(ln.getGroupId()))
                .count();
            // local node not contains this one:
            if (count != 1) {
                nodeService.addNodeInfo(groupId, nodeId);
            }
        }
    }


    /**
     * check group status and Update
     * check group status(normal or maintaining), update by local group list
     *
     * @param allGroupOnChain if groupid not in allGroupSet, remove it
     */
    private void checkAndUpdateGroupStatus(Set<String> allGroupOnChain) {
        log.info("checkAndUpdateGroupStatus allGroupOnChain:{}", allGroupOnChain);
        if (CollectionUtils.isEmpty(allGroupOnChain)) {
            return;
        }

        List<TbGroup> allLocalGroup = getGroupList(null);
        log.info("checkAndUpdateGroupStatus allLocalGroup:{}", allLocalGroup);
        if (CollectionUtils.isEmpty(allLocalGroup)) {
            return;
        }

        for (TbGroup localGroup : allLocalGroup) {
            String localGroupId = localGroup.getGroupId();
            long count = 0;
            count = allGroupOnChain.stream().filter(id -> id.equals(localGroupId)).count();
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
                log.info("fail check group. localGroup:{}", JsonTools.toJSONString(localGroup));
                continue;
            }

        }
    }

    /**
     * check group's genesis block the same with each other on chain
     * @case: each front has different genesis conf, but add in same nodemgr
     */
    private void checkGroupGenesisSameWithEach() {
        log.info("start checkGroupGenesisSameWithEach.");
        // get all front
        List<TbFront> frontList = frontService.getFrontList(new FrontParam());
        if (frontList == null || frontList.size() == 0) {
            log.warn("checkGroupGenesisSameWithEach not found any front.");
            return;
        }
        List<TbGroup> allGroupList = getGroupList(null);
        if (allGroupList.isEmpty()) {
            log.warn("checkGroupGenesisSameWithEach not found any group of front.");
            return;
        }

        for (TbGroup tbGroup : allGroupList) {
            String groupId = tbGroup.getGroupId();
            String lastBlockHash = "";
            for (TbFront front : frontList) {
                if( ! FrontStatusEnum.isRunning(front.getStatus()) ){
                    log.warn("Front:[{}:{}] is not running.",front.getFrontIp(),front.getHostIndex());
                    continue;
                }
                String frontIp = front.getFrontIp();
                int frontPort = front.getFrontPort();
                // check genesis block
                BcosBlock.Block genesisBlock = frontInterface.getBlockByNumberFromSpecificFront(frontIp,
                        frontPort, groupId, BigInteger.ZERO);
                if (genesisBlock == null) {
                    log.debug("checkGroupGenesisSameWithEach getGenesisBlock is null");
                    continue;
                }
                if (!"".equals(lastBlockHash) && !MessageDigest.isEqual(lastBlockHash.getBytes(), genesisBlock.getHash().getBytes())) {
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
     *
     * @case1 rebuild chain(different genesis),
     * @case2 drop group data and restart chain(same genesis), when local smallest block height greater than 0, mark as CONFLICT
     */
    private void checkSameChainDataWithLocal() {
        log.info("start checkSameChainData.");
        // get all group
        List<TbGroup> allGroupList = getGroupList(null);
        if (allGroupList.isEmpty()) {
            log.warn("checkSameChainData not found any group of front.");
            return;
        }

        for (TbGroup tbGroup : allGroupList) {
            String groupId = tbGroup.getGroupId();
            // find smallest block from db of group
            TbBlock smallestBlockLocal = blockService.getSmallestBlockInfo(groupId);
            // if no block in local db
            if (smallestBlockLocal == null) {
                log.debug("checkSameChainDataWithLocal groupId {} smallestBlockLocal is null", groupId);
                continue;
            }
            BigInteger blockHeightLocal = smallestBlockLocal.getBlockNumber();
            String blockHashLocal = smallestBlockLocal.getPkHash();

            // get same height block from chain(if null, get from another front), contrast block hash
            String blockHashOnChain = "";
            // get all frontGroupMap list by group id
            List<FrontGroup> allFrontGroupList = frontGroupMapCache.getMapListByGroupId(groupId);
            if (allFrontGroupList == null) {
                continue;
            }
            log.debug("checkSameChainDataWithLocal allFrontGroupList:{}", allFrontGroupList);
            // case: if group's all front is stopped, front_group_map still normal, would set as CONFLICT for no data from front
            boolean flagEmptyFront = (allFrontGroupList.size() == 0);
            for(FrontGroup front: allFrontGroupList) {
                BcosBlock.Block smallestBlockOnChain = frontInterface.getBlockByNumberFromSpecificFront(
                        front.getFrontIp(), front.getFrontPort(), groupId, blockHeightLocal);
                if (smallestBlockOnChain == null) {
                    continue;
                } else {
                    blockHashOnChain = smallestBlockOnChain.getHash();
                    break;
                }
            }
            // if no block in each node, not same chain, else contrast with local hash
            // if all front group map invalid, ignore
            if (blockHashOnChain.isEmpty() && !flagEmptyFront) {
                log.warn("smallestBlockOnChain groupId: {} height: {} return null block, " +
                        "please check group's node", groupId, blockHeightLocal);
                // null block not means conflict
                 updateGroupStatus(groupId, GroupStatus.CONFLICT_LOCAL_DATA.getValue());
                continue;
            }

            log.debug("checkSameChainData groupId:{},blockHeight:{},localHash:{},chainHash:{} ",
                    groupId, blockHeightLocal, blockHashLocal, blockHashOnChain);
            // check same block hash, the same chain
            if (!blockHashOnChain.isEmpty() && !MessageDigest.isEqual(blockHashLocal.getBytes(), blockHashOnChain.getBytes())) {
                log.warn("checkSameChainDataWithLocal blockHashOnChain conflicts with local block data " +
                                "groupId: {} height:{} on chain ", groupId, blockHeightLocal);
                updateGroupStatus(groupId, GroupStatus.CONFLICT_LOCAL_DATA.getValue());
                continue;
            } else if (tbGroup.getGroupStatus() == GroupStatus.CONFLICT_LOCAL_DATA.getValue()){
                // if old group is conflict, now normal, set as normal
                log.info("checkSameChainDataWithLocal set groupId:{} as normal", groupId);
                updateGroupStatus(groupId, GroupStatus.NORMAL.getValue());
            }
        }

    }

    /**
     * check and update front group map by local group id list
     *
     * @case: front1 has group2, front2 not has group2,
     * so groupListOnChain from front don't contain group2, need to check if front_group_map of front2_group2 in db
     * if so, remove it
     */
    public void checkGroupMapByLocalGroupList(List<TbFront> frontList) {
        // local group id
        List<TbGroup> groupListLocal = getGroupList(null);
        log.debug("checkGroupMapByLocalGroupList frontList:{},groupListLocal:{}",
                frontList, groupListLocal);
        for (TbFront front : frontList) {
            if (!FrontStatusEnum.isRunning(front.getStatus())) {
                log.warn("Front:[{}:{}] is not running.",front.getFrontIp(),front.getHostIndex());
                continue;
            }
            // query roup list from chain
            List<String> groupListOnChain;
            try {
                groupListOnChain = frontInterface.getGroupListFromSpecificFront(front.getFrontIp(), front.getFrontPort());
            } catch (Exception ex) {
                log.error("checkGroupMapByLocalGroupList fail getGroupListFromSpecificFront.", ex);
                continue;
            }
            // group list local
            groupListLocal.forEach(group -> {
                String groupId = group.getGroupId();
                // only check local group id
                if (!groupListOnChain.contains(groupId)) {
                    log.info("update front_group_map by local data front:{}, groupId:{} ",
                            front, groupId);
                    // case: group2 in font1, not in front2, but local has group2, so add front1_group2_map but not front2_group2_map
                    frontGroupMapService.newFrontGroupWithStatus(front.getFrontId(), groupId, GroupStatus.MAINTAINING.getValue());
                }
            });
        }
    }

    /**
     * check all group and remove those without normal front group
     * @related removeGroupAccording2MapStatus
     */
    private void removeInvalidGroupByMap() {
        List<TbGroup> groupList = getGroupList(null);
        // if all front of group invalid, remove group and front_group_map
        groupList.forEach(tbGroup ->
                removeGroupBy2MapStatus(tbGroup.getGroupId()));
    }

    /**
     * remove group whose front(front_group_map) all invalid
     * case1: remove front, call this
     * case2: stop all front of one group, call this
     * @param groupId
     */
    private void removeGroupBy2MapStatus(String groupId) {
        log.debug("removeGroupByMapStatus groupId:{}", groupId);
        // get list of this group
        MapListParam param = new MapListParam();
        param.setGroupId(groupId);
        // count of group belonging to this front
        List<FrontGroup> frontListByGroup = frontGroupMapService.getList(param);

        // count of front's groupStatus normal
        long count = frontListByGroup.stream()
                .filter( f -> f.getStatus() == GroupStatus.NORMAL.getValue())
                .count();
        long countInvalid = frontListByGroup.size() - count;
        // front belong to this group all invalid
        if (countInvalid == frontListByGroup.size()) {
            log.warn("removeGroupByMapStatus all map is valid, remove group: countInvalid:{}", countInvalid);
            removeAllDataByGroupId(groupId);
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
     * remove all data by groupId.
     * included: tb_group, tb_front_group_map, group contract/trans, group method, group node etc.
     */
    @Transactional(isolation= Isolation.READ_COMMITTED)
    public void removeAllDataByGroupId(String groupId) {
        if (groupId.isEmpty()) {
            return;
        }
        checkGroupId(groupId);
        log.warn("removeAllDataByGroupId! groupId:{}", groupId);
        // remove groupId.
        groupMapper.remove(groupId);
        // remove mapping.
        frontGroupMapService.removeByGroupId(groupId);
        // remove contract
        contractService.deleteByGroupId(groupId);
        // remove method
        methodService.deleteByGroupId(groupId);
        // remove node
        nodeService.deleteByGroupId(groupId);
        // remove transDaily
        transDailyService.deleteByGroupId(groupId);
        // delete imported abi
        abiService.deleteAbiByGroupId(groupId);
        // delete cns record
        cnsService.deleteByGroupId(groupId);
        // delete external user
        extAccountService.deleteByGroupId(groupId);
        // delete external contract
        extContractService.deleteByGroupId(groupId);
        // delete statistic block data
        statService.deleteByGroupId(groupId);
        // drop table.
        tableService.dropTableByGroupId(groupId);
        log.warn("end removeAllDataByGroupId");
    }

    /**
     * save group id
     */


    @Transactional(propagation = Propagation.REQUIRED)
    public TbGroup saveGroup(String groupId, int nodeCount, String groupDesc, GroupType groupType,
        GroupStatus groupStatus, Integer chainId, String chainName, Integer encryptType) {
        log.info("saveGroup groupId:{},groupStatus:{},encryptType:{}",
            groupId, groupStatus, encryptType);
        if (groupId.isEmpty()) {
            throw new NodeMgrException(INSERT_GROUP_ERROR);
        }
        //save group id
        TbGroup tbGroup = new TbGroup(groupId,
            String.format("group_%s", groupId),
            nodeCount, groupDesc, groupType, groupStatus,
            chainId, chainName, encryptType);
        groupMapper.insertSelective(tbGroup);

        //create table by group id
        tableService.newTableByGroupId(groupId);
        return tbGroup;
    }

    @Transactional
    public TbGroup saveGroup(String groupId, int nodeCount, String description,
                             GroupType groupType, GroupStatus groupStatus, BigInteger timestamp, List<String> nodeIdList,
                             Integer chainId, String chainName, int encryptType) {
        log.debug("start saveGroup");
        if (groupId.isEmpty()) {
            return null;
        }
        // save group id
        String groupName = "group" + groupId;
        TbGroup tbGroup =
                new TbGroup(groupId, groupName, nodeCount, description,
                        groupType, groupStatus,chainId, chainName, encryptType);
        tbGroup.setGroupTimestamp(timestamp.toString(10));
        tbGroup.setNodeIdList(JsonTools.toJSONString(nodeIdList));
        log.debug("saveGroup tbGroup:{}", tbGroup);
        groupMapper.insertSelective(tbGroup);
        // create table by group id
        tableService.newTableByGroupId(groupId);
        return tbGroup;
    }

    /**
     * Check the validity of the groupId.
     */
    public boolean checkGroupIdExisted(String groupId) throws NodeMgrException {
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

    public TbGroup getGroupById(String groupId) {
        log.debug("getGroupById groupId:{}", groupId);
        return groupMapper.getGroupById(groupId);
    }


    /**
     * update status.
     */
    @Transactional
    public void updateGroupNodeCount(String groupId, int nodeCount) {
        log.debug("start updateGroupNodeCount groupId:{} nodeCount:{}", groupId, nodeCount);
        groupMapper.updateNodeCount(groupId, nodeCount);
        log.debug("end updateGroupNodeCount groupId:{} nodeCount:{}", groupId, nodeCount);

    }

    @Transactional
    public void updateTimestampNodeIdList(String groupId, long timestamp,List<String> nodeIdList) {
        log.debug("start updateTimestampNodeIdList groupId:{} nodeCount:{}", groupId, timestamp, nodeIdList);
        this.groupMapper.updateTimestampNodeList(groupId, timestamp,JsonTools.toJSONString(nodeIdList));
    }


    @Transactional
    public TbGroup insertIfNew(String groupId, int nodeCount, String groupDesc,
                               GroupType groupType, GroupStatus groupStatus, Integer chainId, String chainName) {

        TbGroup group = this.groupMapper.getGroupByChainIdAndGroupId(chainId,groupId);
        if (group != null) {
            return group;
        }

        return ((GroupService) AopContext.currentProxy()).saveGroup(groupId,nodeCount,
                groupDesc,groupType,groupStatus,chainId,chainName, 0);//todo 获取群组的加密类型
    }


    /**
     * Insert group when group id not exists.
     * <p>
     * When group exists, update node count with num.
     *
     * @param groupId
     * @param num
     * @param chainId
     * @param chainName
     * @return return true if insert.
     */
//    @Transactional(propagation = Propagation.REQUIRED)
//    public Pair<TbGroup, Boolean> saveOrUpdateNodeCount(String groupId, int num, Integer chainId, String chainName) {
//        TbGroup group = this.getGroupById(groupId);
//        if (group == null) {
//            // group not exists, insert a new one
//            return Pair.of(((GroupService) AopContext.currentProxy())
//                    .saveGroup(groupId, num, "deploy", GroupType.DEPLOY, GroupStatus.MAINTAINING, chainId, chainName), true);
//        } else {
//            // group exists, update group count
//            int newGroupCount = group.getNodeCount() + num;
//            ((GroupService) AopContext.currentProxy()).updateGroupNodeCount(groupId, newGroupCount);
//            // update node count
//            group.setNodeCount(newGroupCount);
//            return Pair.of(group, false);
//        }
//    }

    private void pullAllGroupFiles(String generateGroupId, TbFront tbFront) {
        this.pullGroupConfigFile(generateGroupId, tbFront);
        this.pullGroupStatusFile(generateGroupId, tbFront);
    }

    /**
     * pull docker node's group config file and group_status file
     * when generateGroup/operateGroup
     *
     * @include group.x.genesis, group.x.ini, .group_status
     */
    private void pullGroupConfigFile(String generateGroupId, TbFront tbFront) {
        // only support docker node/front
        if (tbFront.getRunType() != RunTypeEnum.DOCKER.getId()) {
            return;
        }
        String chainName = tbFront.getChainName();
        int nodeIndex = tbFront.getHostIndex();
        TbHost tbHost = tbHostMapper.selectByPrimaryKey(tbFront.getHostId());

        // scp group config files from remote to local
        // path pattern: /host.getRootDir/chain_name
        // ex: (in the remote host) /opt/fisco/chain1
        String remoteChainPath = PathService.getChainRootOnHost(tbHost.getRootDir(), chainName);
        // ".*" fit in ansible
        // ex: (in the remote host) /opt/fisco/chain1/node0/conf/group.1001.*
        // String remoteGroupConfSource = String.format("%s/node%s/conf/group.%s.*", remoteChainPath, nodeIndex, generateGroupId);
        String remoteGroupConfSource = String.format("%s/node%s/conf/group.%s.ini", remoteChainPath, nodeIndex, generateGroupId);
        String remoteGroupGenesisSource = String.format("%s/node%s/conf/group.%s.genesis", remoteChainPath, nodeIndex, generateGroupId);
        // path pattern: /NODES_ROOT/chain_name/[ip]/node[index]
        // ex: (node-mgr local) ./NODES_ROOT/chain1/127.0.0.1/node0
        String localNodePath = pathService.getNodeRoot(chainName, tbHost.getIp(),tbFront.getHostIndex()).toString();
        // ex: (node-mgr local) ./NODES_ROOT/chain1/127.0.0.1/node0/conf/group.1001.*
        String localDst = String.format("%s/conf/", localNodePath, generateGroupId);
        // copy group config files to local node's conf dir
        ansibleService.scp(ScpTypeEnum.DOWNLOAD, tbHost.getIp(), remoteGroupConfSource, localDst);
        ansibleService.scp(ScpTypeEnum.DOWNLOAD, tbHost.getIp(), remoteGroupGenesisSource, localDst);
    }



    private void pullGroupStatusFile(String generateGroupId, TbFront tbFront) {
        // only support docker node/front
        if (tbFront.getRunType() != RunTypeEnum.DOCKER.getId()) {
            return;
        }
        String chainName = tbFront.getChainName();
        int nodeIndex = tbFront.getHostIndex();
        TbHost tbHost = tbHostMapper.selectByPrimaryKey(tbFront.getHostId());
        // scp group status files from remote to local
        // path pattern: /host.getRootDir/chain_name
        // ex: (in the remote host) /opt/fisco/chain1
        String remoteChainPath = PathService.getChainRootOnHost(tbHost.getRootDir(), chainName);
        // ex: (in the remote host) /opt/fisco/chain1/node0/data/group1001/.group_status
        String remoteGroupStatusSource = String.format("%s/node%s/data/group%s/.group_status",
                remoteChainPath, nodeIndex, generateGroupId);
        // path pattern: /NODES_ROOT/chain_name/[ip]/node[index]
        // ex: (node-mgr local) ./NODES_ROOT/chain1/127.0.0.1/node0
        String localNodePath = pathService.getNodeRoot(chainName, tbHost.getIp(),tbFront.getHostIndex()).toString();
        // ex: (node-mgr local) ./NODES_ROOT/chain1/127.0.0.1/node0/data/group[groupId]/group.1001.*
        Path localDst = Paths.get(CleanPathUtil.cleanString(String.format("%s/data/group%s/.group_status", localNodePath,generateGroupId)));
        // create data parent directory
        if (Files.notExists(localDst.getParent())){
            try {
                Files.createDirectories(localDst.getParent());
            } catch (IOException e) {
                log.error("Create data group:[{}] file error", localDst.toAbsolutePath().toString(),e);
            }
        }
        // copy group status file to local node's conf dir
        ansibleService.scp(ScpTypeEnum.DOWNLOAD, tbHost.getIp(), remoteGroupStatusSource, localDst.toAbsolutePath().toString());
    }

//    private void pullGroupFile(int groupId,TbFront tbFront){
//        if (tbFront.getRunType() != RunTypeEnum.DOCKER.getId()) {
//            return;
//        }
//        String chainName = tbFront.getChainName();
//        int nodeIndex = tbFront.getHostIndex();
//        TbHost tbHost = tbHostMapper.selectByPrimaryKey(tbFront.getHostId());
//
//    }

    /**
     * generate group.x.ini group.x.genesis
     * @param chain
     * @param groupId
     * @param ip
     * @param newFrontList
     * @throws IOException
     */
    public void generateNewNodesGroupConfigsAndScp(TbChain chain, String groupId, String ip, List<TbFront> newFrontList) {
        log.info("start generateNewNodesGroupConfigsAndScp ip:{},newFrontList:{}", ip, newFrontList);
        int chainId = chain.getId();
        String chainName = chain.getChainName();

        // 1.4.3 not support add group when add node
        // long now = System.currentTimeMillis();
        // List<String> nodeIdList = newFrontList.stream().map(TbFront::getNodeId)
        //        .collect(Collectors.toList());

        // copy group.x.[genesis|conf] from old front
        TbNode oldNode = this.nodeService.getOldestNodeByChainIdAndGroupId(chainId, groupId);
        TbFront oldFront = null;
        if (oldNode != null){
             oldFront = this.frontMapper.getByNodeId(oldNode.getNodeId());
        }

        for (TbFront newFront : newFrontList) {
            // local node root
            Path nodeRoot = this.pathService.getNodeRoot(chainName, ip, newFront.getHostIndex());

            // 1.4.3 not support add group when add node
            //if (newGroup) {
            //    // generate conf/group.[groupId].ini
            //    ThymeleafUtil.newGroupConfigs(nodeRoot, groupId, now, nodeIdList);
            // copy old group files
            if (oldFront != null) {
                Path oldNodePath = this.pathService.getNodeRoot(chainName, oldFront.getFrontIp(), oldFront.getHostIndex());
                NodeConfig.copyGroupConfigFiles(oldNodePath, nodeRoot, groupId);
            }


            // scp node to remote host
            // NODES_ROOT/[chainName]/[ip]/node[index] as a {@link Path}, a directory.
            String src = String.format("%s", nodeRoot.toAbsolutePath().toString());
            // get host root dir
            TbHost tbHost = tbHostMapper.getByIp(ip);
            String dst = PathService.getChainRootOnHost(tbHost.getRootDir(), chainName);

            log.info("generateNewNodesGroupConfigsAndScp Send files from:[{}] to:[{}:{}].", src, ip, dst);
            ProgressTools.setScpConfig();
            try {
                ansibleService.scp(ScpTypeEnum.UP, ip, src, dst);
                log.info("generateNewNodesGroupConfigsAndScp scp success.");
            } catch (Exception e) {
                log.error("generateNewNodesGroupConfigsAndScp Send files from:[{}] to:[{}:{}] error.", src, ip, dst, e);
            }
        }
    }

    /**
     *
     * @param chainId
     */
    @Transactional
    public void deleteGroupByChainId(int chainId){
        log.info("Delete group data by chain id:[{}].", chainId);
        List<TbGroup> groupIdList = this.groupMapper.selectGroupList(chainId);
        if (CollectionUtils.isEmpty(groupIdList)) {
            log.warn("No group in chain:[{}]", chainId);
            return;
        }

        for (TbGroup tbGroup : groupIdList) {
            log.info("Delete all data of group:[{}:{}].", chainId, tbGroup.getGroupId());
            this.removeAllDataByGroupId(tbGroup.getGroupId());
        }
    }


    /**
     * update status.
     */
    public int updateGroupDescription(String groupId, String description) {
        log.debug("start updateGroupStatus groupId:{} description:{}", groupId, description);
        int res = groupMapper.updateDescription(groupId, description);
        log.debug("end updateGroupStatus res", res);
        return res;
    }

    public int getEncryptTypeByGroupId(String groupId) {
        log.debug("start getEncryptTypeByGroupId groupId:{}", groupId);
        int res = groupMapper.getEncryptType(groupId);
        log.debug("end getEncryptTypeByGroupId res", res);
        return res;
    }



}

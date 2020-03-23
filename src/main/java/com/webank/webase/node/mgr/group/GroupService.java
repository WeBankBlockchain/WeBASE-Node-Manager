/**
 * Copyright 2014-2019  the original author or authors.
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
import com.webank.webase.node.mgr.base.enums.DataStatus;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.base.properties.ConstantProperties;
import com.webank.webase.node.mgr.base.tools.NodeMgrTools;
import com.webank.webase.node.mgr.contract.ContractService;
import com.webank.webase.node.mgr.front.FrontService;
import com.webank.webase.node.mgr.front.entity.FrontParam;
import com.webank.webase.node.mgr.front.entity.TbFront;
import com.webank.webase.node.mgr.front.entity.TotalTransCountInfo;
import com.webank.webase.node.mgr.frontgroupmap.FrontGroupMapService;
import com.webank.webase.node.mgr.frontgroupmap.entity.FrontGroupMapCache;
import com.webank.webase.node.mgr.frontgroupmap.entity.MapListParam;
import com.webank.webase.node.mgr.frontinterface.FrontInterfaceService;
import com.webank.webase.node.mgr.frontinterface.entity.GenerateGroupInfo;
import com.webank.webase.node.mgr.frontinterface.entity.GroupHandleResult;
import com.webank.webase.node.mgr.group.entity.GroupGeneral;
import com.webank.webase.node.mgr.group.entity.StatisticalGroupTransInfo;
import com.webank.webase.node.mgr.group.entity.TbGroup;
import com.webank.webase.node.mgr.method.MethodService;
import com.webank.webase.node.mgr.node.NodeService;
import com.webank.webase.node.mgr.node.TbNode;
import com.webank.webase.node.mgr.node.entity.PeerInfo;
import com.webank.webase.node.mgr.table.TableService;
import com.webank.webase.node.mgr.transdaily.TransDailyService;
import com.webank.webase.node.mgr.user.UserService;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

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
    
    /**
     * generate group.
     * 
     * @return
     */
    public void generateGroup(GenerateGroupInfo generateGroupInfo, String frontIp, Integer frontPort) {
        // request front to generate
        GroupHandleResult groupHandleResult = frontInterface.generateGroup(frontIp,
                frontPort, generateGroupInfo);
        // check result
        int code = NodeMgrTools.parseHexStr2Int(groupHandleResult.getCode());
        if (code != 0) {
            log.error("fail generateGroupId:{} code:{}", generateGroupInfo.getGenerateGroupId(), code);
            throw new NodeMgrException(code, groupHandleResult.getMessage());
        }
    }
    
    /**
     * start group.
     * 
     * @param startGroupId
     */
    public void startGroup(Integer startGroupId, String frontIp, Integer frontPort) {
        // request front to start
        GroupHandleResult groupHandleResult = frontInterface.startGroup(frontIp,
                frontPort, startGroupId);
        // check result
        int code = NodeMgrTools.parseHexStr2Int(groupHandleResult.getCode());
        if (code != 0) {
            log.error("fail startGroup startGroupId:{} code:{}", startGroupId, code);
            throw new NodeMgrException(code, groupHandleResult.getMessage());
        }
        // @Deprecated: already refresh in front when start a group
        // frontInterface.refreshFront(frontIp, frontPort);
    }

    /**
     * save group id
     */
    public void saveGroupId(int groupId, int nodeCount) {
        if (groupId == 0) {
            return;
        }
        //save group id
        String groupName = "group" + groupId;
        TbGroup tbGroup = new TbGroup(groupId, groupName, nodeCount);
        groupMapper.save(tbGroup);

        //create table by group id
        tableService.newTableByGroupId(groupId);
    }

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
            log.info("not fount any front, start remove all group");
            //remove all group   TODO
            // removeAllGroup();
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
                //peer in group
                List<String> groupPeerList = frontInterface.getNodeIDListFromSpecificFront(frontIp, frontPort, gId);
                //save groupId
                saveGroupId(gId, groupPeerList.size());
                frontGroupMapService.newFrontGroup(front.getFrontId(), gId);
                //save new peers
                savePeerList(frontIp, frontPort, gId, groupPeerList);
                //remove invalid peers
                removeInvalidPeer(gId, groupPeerList);
                //refresh: add sealer and observer no matter validity
                frontService.refreshSealerAndObserverInNodeList(frontIp, frontPort, gId);
                //check node status
                //nodeService.checkNodeStatus(gId);
            }
        }

        //check group status
        checkGroupStatusAndRemoveInvalidGroup(allGroupSet);
        //clear cache
        frontGroupMapCache.clearMapList();

        log.info("end resetGroupList. useTime:{} ",
                Duration.between(startTime, Instant.now()).toMillis());
    }

    /**
     * save new peers.
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
                    ln -> groupId == ln.getGroupId() && nodeId.equals(ln.getNodeId())).count();
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
        allGroup.stream().forEach(group -> removeByGroupId(group.getGroupId()));
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
        for(PeerInfo peerInfo: sealerAndObserverList){
            if(nodeId.equals(peerInfo.getNodeId())){
                return true;
            }
        }
        return false;
    }

    /**
     * check group status.
     */
    private void checkGroupStatusAndRemoveInvalidGroup(Set<Integer> allGroupOnChain) {
        if (CollectionUtils.isEmpty(allGroupOnChain)) {
            return;
        }

        List<TbGroup> allLocalGroup = getGroupList(null);
        if (CollectionUtils.isEmpty(allLocalGroup)) {
            return;
        }

        for (TbGroup localGroup : allLocalGroup) {
            int localGroupId = localGroup.getGroupId();
            long count = allGroupOnChain.stream().filter(id -> id == localGroupId).count();
            try {
                if (count > 0) {
                    log.warn("group is valid, localGroupId:{}", localGroupId);
                    //update NORMAL
                    updateGroupStatus(localGroupId, DataStatus.NORMAL.getValue());
                    continue;
                }

                if (!NodeMgrTools.isDateTimeInValid(localGroup.getModifyTime(),
                        constants.getGroupInvalidGrayscaleValue())) {
                    log.warn("remove group, localGroup:{}", JSON.toJSONString(localGroup));
                    //remove group
                    removeByGroupId(localGroupId);
                    continue;
                }

                log.warn("group is invalid, localGroupId:{}", localGroupId);
                if (DataStatus.NORMAL.getValue() == localGroup.getGroupStatus()) {
                    //update invalid
                    updateGroupStatus(localGroupId, DataStatus.INVALID.getValue());
                    continue;
                }

            } catch (Exception ex) {
                log.info("fail check group. localGroup:{}", JSON.toJSONString(localGroup));
                continue;
            }

        }
    }


    /**
     * remove by groupId.
     */
    private void removeByGroupId(int groupId) {
        if (groupId == 0) {
            return;
        }
        //remove groupId.
        groupMapper.remove(groupId);
        //remove mapping.
        frontGroupMapService.removeByGroupId(groupId);
        // @Deprecated: not save privateKey anymore
        // remove user and key
        // userService.deleteByGroupId(groupId);
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
    }
}

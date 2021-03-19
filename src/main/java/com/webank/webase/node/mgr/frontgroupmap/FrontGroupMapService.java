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
package com.webank.webase.node.mgr.frontgroupmap;

import static com.webank.webase.node.mgr.group.GroupService.OPERATE_STATUS_GROUP;
import static com.webank.webase.node.mgr.group.GroupService.RUNNING_GROUP;

import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.entity.BaseResponse;
import com.webank.webase.node.mgr.base.enums.GroupStatus;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.front.FrontMapper;
import com.webank.webase.node.mgr.front.entity.TbFront;
import com.webank.webase.node.mgr.frontgroupmap.entity.FrontGroup;
import com.webank.webase.node.mgr.frontgroupmap.entity.MapListParam;
import com.webank.webase.node.mgr.frontgroupmap.entity.TbFrontGroupMap;
import com.webank.webase.node.mgr.frontinterface.FrontInterfaceService;
import com.webank.webase.node.mgr.group.GroupMapper;
import com.webank.webase.node.mgr.group.entity.TbGroup;
import com.webank.webase.node.mgr.node.NodeService;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Service
public class FrontGroupMapService {

    @Autowired
    private FrontGroupMapMapper frontGroupMapMapper;
    @Autowired
    private FrontInterfaceService frontInterface;
    @Autowired
    private FrontGroupMapCache frontGroupMapCache;
    /**
     * to check map sealer or observer
     */
    @Autowired
    private NodeService nodeService;
    @Autowired
    private FrontMapper frontMapper;
    @Autowired
    private GroupMapper groupMapper;

    /**
     * add or update map with group status
     * @param: type: map consensus type
     */
    @Transactional
    public TbFrontGroupMap newFrontGroupWithStatus(Integer frontId, Integer groupId, Integer status) {
        log.debug("start newFrontGroup frontId:{} groupId:{} status:{}", frontId, groupId, status);
        MapListParam param = new MapListParam(frontId, groupId);
        FrontGroup frontGroup = frontGroupMapMapper.queryFrontGroup(param);
        log.debug("start newFrontGroup frontGroup query:{}", frontGroup);

        int consensusType = this.checkFrontGroupType(frontId, groupId);
        log.debug("newFrontGroup consensusType:{}", consensusType);

        // add db
        TbFrontGroupMap tbFrontGroupMap = null;
        int res;
        if (frontGroup == null) {
            tbFrontGroupMap = new TbFrontGroupMap(frontId, groupId, status, consensusType);
            log.info("newFrontGroup tbFrontGroupMap:{}", tbFrontGroupMap);
            res = frontGroupMapMapper.insertSelective(tbFrontGroupMap);
        } else {
            tbFrontGroupMap = new TbFrontGroupMap(frontId, groupId, status, consensusType);
            tbFrontGroupMap.setMapId(frontGroup.getMapId());
            log.info("updateFrontGroup tbFrontGroupMap:{}", tbFrontGroupMap);
            res = frontGroupMapMapper.update(tbFrontGroupMap);
        }
        log.info("end newFrontGroup res:{}", res);

        return tbFrontGroupMap;
    }

    /**
     * add new mapping in visual deploy
     */
    @Transactional
    public TbFrontGroupMap newFrontGroup(Integer frontId, Integer groupId, GroupStatus groupStatus) {
        TbFrontGroupMap tbFrontGroupMap = new TbFrontGroupMap(frontId, groupId, groupStatus.getValue());

        //add db
        frontGroupMapMapper.insertSelective(tbFrontGroupMap);

        return tbFrontGroupMap;
    }


    /**
     * new front group map when refreshing map with group status
     * v1.4.3: add consensus type of front group map
     */
    @Transactional
    public void newFrontGroup(TbFront front, Integer groupId) {
        // check front's all group status
        BaseResponse res = frontInterface.operateGroup(front.getFrontIp(), front.getFrontPort(),
                groupId, OPERATE_STATUS_GROUP);
        log.debug("newFrontGroupWithStatus getGroupStatus frontId{} groupId{} res{}",
                front.getFrontId(), groupId, res);
        // "INEXISTENT"、"STOPPING"、"RUNNING"、"STOPPED"、"DELETED"
        if (res.getCode() == 0) {
            String groupStatus = (String) res.getData();
            if (RUNNING_GROUP.equals(groupStatus)) {
                log.debug("newFrontGroupWithStatus update map's groupStatus NORMAL.");
                newFrontGroupWithStatus(front.getFrontId(), groupId, GroupStatus.NORMAL.getValue());
            } else {
                log.debug("newFrontGroupWithStatus update map's groupStatus MAINTAINING.");
                newFrontGroupWithStatus(front.getFrontId(), groupId, GroupStatus.MAINTAINING.getValue());
            }
        } else {
            log.warn("newFrontGroupWithStatus get group status fail, " +
                    "update front_group_map status fail. res:{}", res);
        }
    }

    /**
     * get map count
     */
    public int getCount(MapListParam param) {
        return frontGroupMapMapper.getCount(param);
    }

    /**
     * remove by groupId
     */
    @Transactional(isolation= Isolation.READ_COMMITTED)
    public void removeByGroupId(int groupId) {
        if (groupId == 0) {
            return;
        }
        // remove by groupId
        frontGroupMapMapper.removeByGroupId(groupId);
    }

    /**
     * remove by frontId
     */
    public void removeByFrontId(int frontId) {
        if (frontId == 0) {
            return;
        }
        //remove by frontId
        frontGroupMapMapper.removeByFrontId(frontId);
    }
    
    /**
     * get group list by frontId
     */
    public List<Integer> getGroupIdListByFrontId(int frontId) {
        if (frontId == 0) {
            return null;
        }
        return frontGroupMapMapper.getGroupIdListByFrontId(frontId);
    }

    /**
     * get map list by groupId
     */
    public List<FrontGroup> listByGroupId(int groupId) {
        if (groupId == 0) {
            return null;
        }
        MapListParam param = new MapListParam();
        param.setGroupId(groupId);
        return getList(param);
    }

    /**
     * get map list
     */
    public List<FrontGroup> getList(MapListParam mapListParam) {
        return frontGroupMapMapper.getList(mapListParam);
    }

    /**
     * remove group that not in tb_front or tb_group
     */
    @Transactional(isolation= Isolation.READ_COMMITTED)
    public void removeInvalidFrontGroupMap() {
        List<FrontGroup> allList = frontGroupMapMapper.getAllList();
        // all front list
        List<TbFront> frontList = frontMapper.getAllList();
        // all group list
        List<TbGroup> groupList = groupMapper.getList(null);
        allList.forEach(map -> {
            int mapId= map.getMapId();
            int frontId = map.getFrontId();
            int groupId = map.getGroupId();
            long frontCount = frontList.stream().filter(f -> frontId == f.getFrontId()).count();
            long groupCount = groupList.stream().filter(g -> groupId == g.getGroupId()).count();
            if (frontCount == 0 || groupCount == 0) {
                log.warn("removeInvalidFrontGroupMap mapId:{} map's group/front is not in table", mapId);
                frontGroupMapMapper.removeByMapId(mapId);
            }
        });
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void updateFrontMapStatus(int frontId, GroupStatus status) {
        // update status
        log.info("Update frontGroupMap:[{}] all group map to status:[{}]", frontId, status);
        frontGroupMapMapper.updateAllGroupsStatus(frontId, status.getValue());
        this.frontGroupMapCache.clearMapList();
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void updateFrontMapStatus(int frontId, int groupId, GroupStatus status) {
        // update status
        log.info("Update frontGroupMap:[{}] group:[{}] map to status:[{}]", frontId, groupId, status);
        frontGroupMapMapper.updateOneGroupStatus(frontId, status.getValue(), groupId);
        this.frontGroupMapCache.clearMapList();
    }

    /**
     * 1- sealer, 2-observer
     * @param frontId
     * @param groupId
     * @return
     */
    private int checkFrontGroupType(int frontId, int groupId) {
        TbFront front = frontMapper.getById(frontId);
        if (front == null) {
            log.error("frontId :{} not exist!", frontId);
            throw new NodeMgrException(ConstantCode.INVALID_FRONT_ID);
        }
        log.debug("getMapSealerOrObserver groupId:{}, nodeId:{}", groupId, front.getNodeId());

        int type = nodeService.checkNodeType(groupId, front.getNodeId());
        if (type == 0) {
            log.error("node block height larger than local! check later! nodeId:{},consensus type:{}", front.getNodeId(), type);
        }
        return type;
    }
}

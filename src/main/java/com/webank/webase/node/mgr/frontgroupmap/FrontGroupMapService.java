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

import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.enums.GroupStatus;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.front.FrontMapper;
import com.webank.webase.node.mgr.front.entity.TbFront;
import com.webank.webase.node.mgr.frontgroupmap.entity.FrontGroup;
import com.webank.webase.node.mgr.frontgroupmap.entity.MapListParam;
import com.webank.webase.node.mgr.frontgroupmap.entity.TbFrontGroupMap;
import com.webank.webase.node.mgr.group.GroupMapper;
import com.webank.webase.node.mgr.group.entity.TbGroup;
import com.webank.webase.node.mgr.node.NodeService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Log4j2
@Service
public class FrontGroupMapService {

    @Autowired
    private FrontGroupMapMapper frontGroupMapMapper;
//    @Autowired
//    private FrontInterfaceService frontInterface;

    @Lazy
    @Autowired
    private FrontGroupMapCache frontGroupMapCache;
    /**
     * to check map sealer or observer
     */
    @Lazy
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
    public TbFrontGroupMap newFrontGroupWithStatus(Integer frontId, String groupId, Integer status) {
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
    public TbFrontGroupMap newFrontGroup(Integer frontId, String groupId, GroupStatus groupStatus) {
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
    public void newFrontGroup(TbFront front, String groupId) {
        newFrontGroupWithStatus(front.getFrontId(), groupId, GroupStatus.NORMAL.getValue());
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
    public void removeByGroupId(String groupId) {
        if (groupId.isEmpty()) {
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
    public List<String> getGroupIdListByFrontId(int frontId) {
        if (frontId == 0) {
            return null;
        }
        return frontGroupMapMapper.getGroupIdListByFrontId(frontId);
    }

    /**
     * get map list by groupId
     */
    public List<FrontGroup> listByGroupId(String groupId) {
        if (groupId.isEmpty()) {
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
            String groupId = map.getGroupId();
            long frontCount = frontList.stream().filter(f -> frontId == f.getFrontId()).count();
            long groupCount = groupList.stream().filter(g -> groupId.equals(g.getGroupId())).count();
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
    public void updateFrontMapStatus(int frontId, String groupId, GroupStatus status) {
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
    private int checkFrontGroupType(int frontId, String groupId) {
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

    public FrontGroup getOneNormalMap(Integer frontId, String groupId) {
        MapListParam param = new MapListParam(frontId, groupId);
        param.setStatus(GroupStatus.NORMAL.getValue());
        log.info("getOneNormalMap param:{}", param);
        List<FrontGroup> list = this.getList(param);
        if (list == null || list.isEmpty()) {
            return null;
        }
        log.info("getOneNormalMap list:{}", list);
        return list.get(0);
    }
}

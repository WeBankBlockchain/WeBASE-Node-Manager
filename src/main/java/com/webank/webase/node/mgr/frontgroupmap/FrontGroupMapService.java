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
package com.webank.webase.node.mgr.frontgroupmap;

import static com.webank.webase.node.mgr.group.GroupService.OPERATE_STATUS_GROUP;
import static com.webank.webase.node.mgr.group.GroupService.RUNNING_GROUP;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webank.webase.node.mgr.base.entity.BaseResponse;
import com.webank.webase.node.mgr.base.enums.GroupStatus;
import com.webank.webase.node.mgr.front.entity.TbFront;
import com.webank.webase.node.mgr.frontgroupmap.entity.FrontGroup;
import com.webank.webase.node.mgr.frontgroupmap.entity.MapListParam;
import com.webank.webase.node.mgr.frontgroupmap.entity.TbFrontGroupMap;
import com.webank.webase.node.mgr.frontinterface.FrontInterfaceService;

import lombok.extern.log4j.Log4j2;

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
     * add new mapping with group status directly
     */
    public TbFrontGroupMap newFrontGroupWithStatus(Integer frontId, Integer groupId, Integer status) {
        log.info("start newFrontGroup frontId:{} groupId:{} status:{}", frontId, groupId, status);
        MapListParam param = new MapListParam(frontId, groupId);
        FrontGroup frontGroup = frontGroupMapMapper.queryFrontGroup(param);
        log.debug("start newFrontGroup frontGroup query:{}", frontGroup);

        // add db
        TbFrontGroupMap tbFrontGroupMap = null;
        Integer res;
        if (frontGroup == null) {
            tbFrontGroupMap = new TbFrontGroupMap(frontId, groupId, status);
            log.debug("newFrontGroup tbFrontGroupMap:{}", tbFrontGroupMap);
            res = frontGroupMapMapper.add(tbFrontGroupMap);
        } else {
            tbFrontGroupMap = new TbFrontGroupMap(frontId, groupId, status);
            tbFrontGroupMap.setMapId(frontGroup.getMapId());
            log.debug("newFrontGroup tbFrontGroupMap:{}", tbFrontGroupMap);
            res = frontGroupMapMapper.update(tbFrontGroupMap);
        }
        log.debug("end newFrontGroup res:{}", res);

        return tbFrontGroupMap;
    }

    /**
     * add new mapping
     */
    public TbFrontGroupMap newFrontGroup(Integer frontId, Integer groupId, GroupStatus groupStatus) {
        TbFrontGroupMap tbFrontGroupMap = new TbFrontGroupMap(frontId, groupId, groupStatus.getValue());

        //add db
        frontGroupMapMapper.add(tbFrontGroupMap);

        return tbFrontGroupMap;
    }


    /**
     * new front group map
     */
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
    public void removeByGroupId(int groupId) {
        if (groupId == 0) {
            return;
        }
        //remove by groupId
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
    public void removeInvalidFrontGroupMap() {
        frontGroupMapMapper.removeInvalidMap();
    }

    @Transactional
    public void updateFrontMapStatus(int frontId, GroupStatus status) {
        // update status
        frontGroupMapMapper.updateStatus(frontId,status.getValue());
        this.frontGroupMapCache.clearMapList();
    }
}

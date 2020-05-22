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
package com.webank.webase.node.mgr.frontgroupmap.entity;


import com.webank.webase.node.mgr.base.enums.GroupStatus;
import com.webank.webase.node.mgr.frontgroupmap.FrontGroupMapService;
import java.util.List;
import java.util.stream.Collectors;

import com.webank.webase.node.mgr.group.GroupService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class FrontGroupMapCache {

    @Autowired
    private FrontGroupMapService mapService;
    @Autowired
    private GroupService groupService;

    private static List<FrontGroup> mapList;


    /**
     * clear mapList.
     */
    public void clearMapList() {
        mapList = null;
    }

    /**
     * reset mapList.
     */
    public List<FrontGroup> resetMapList() {
        mapList = mapService.getList(new MapListParam());
        return mapList;
    }

    /**
     * get mapList.
     */
    public List<FrontGroup> getMapListByGroupId(int groupId) {
        List<FrontGroup> list = getAllMap();
        if (list == null) {
            log.warn("getMapListByGroupId getAllMap is null.");
            return null;
        }
        // filter all FrontGroup which groupStatus is normal
        List<FrontGroup> map = list.stream()
            .filter(m -> groupId == m.getGroupId()
                    && GroupStatus.NORMAL.getValue() == groupService.getGroupStatus(groupId))
            .collect(Collectors.toList());
        return map;
    }

    /**
     * get all mapList.
     */
    public List<FrontGroup> getAllMap() {
        if (mapList == null || mapList.size() == 0) {
            mapList = resetMapList();
        }
        return mapList;
    }
}

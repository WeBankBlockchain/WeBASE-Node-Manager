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
package com.webank.webase.node.mgr.frontgroupmap;


import com.webank.webase.node.mgr.base.enums.ConsensusType;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.webank.webase.node.mgr.base.enums.GroupStatus;
import com.webank.webase.node.mgr.frontgroupmap.entity.FrontGroup;
import com.webank.webase.node.mgr.frontgroupmap.entity.MapListParam;

import lombok.extern.log4j.Log4j2;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Component
public class FrontGroupMapCache {

    @Autowired
    private FrontGroupMapService mapService;

    private static List<FrontGroup> mapList;


    /**
     * clear mapList.
     */
    public void clearMapList() {
        mapList = null;
    }

    /**
     * get map filter by consensus type, sealer first
     * filter by block height
     * @return
     */
    @Transactional(isolation= Isolation.READ_COMMITTED)
    public List<FrontGroup> getSealerOrObserverMap() {
        MapListParam param = new MapListParam();
        param.setType(ConsensusType.SEALER.getValue());
        List<FrontGroup> targetMap = null;
        targetMap = mapService.getList(param);
        log.debug("get sealer map:{} param:{}", targetMap, param);
        if (targetMap == null || targetMap.isEmpty()) {
            param.setType(ConsensusType.OBSERVER.getValue());
            targetMap = mapService.getList(param);
            log.debug("get observer map:{} param:{}", targetMap, param);
        }
        log.debug("getSealerOrObserverMap targetMap:{}", targetMap);
        return targetMap;
    }

    /**
     * reset mapList.
     * 优先选择共识节点
     */
    @Transactional
    public List<FrontGroup> resetMapList() {
        mapList = this.getSealerOrObserverMap();
        return mapList;
    }

    /**
     * get all mapList.
     */
    @Transactional
    public List<FrontGroup> getAllMap() {
        if (mapList == null || mapList.size() == 0) {
            mapList = resetMapList();
        }
        return mapList;
    }
    /**
     * get mapList.
     * filter by group status
     */
    @Transactional
    public List<FrontGroup> getMapListByGroupId(String groupId) {
        List<FrontGroup> list = getAllMap();
        if (list == null) {
            log.warn("getMapListByGroupId getAllMap is null.");
            return null;
        }
        // filter all FrontGroup which groupStatus is normal
        List<FrontGroup> map = list.stream()
            .filter(m -> groupId.equals(m.getGroupId())
                    && m.getStatus() == GroupStatus.NORMAL.getValue())
            .collect(Collectors.toList());
        log.debug("getMapListByGroupId map size:{}", map.size());
        return map;
    }


}

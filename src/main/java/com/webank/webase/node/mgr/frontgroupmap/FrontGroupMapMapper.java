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

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import com.webank.webase.node.mgr.frontgroupmap.entity.FrontGroup;
import com.webank.webase.node.mgr.frontgroupmap.entity.MapListParam;
import com.webank.webase.node.mgr.frontgroupmap.entity.TbFrontGroupMap;

@Repository
public interface FrontGroupMapMapper {

    int add(TbFrontGroupMap tbFrontGroupMap);

    int update(TbFrontGroupMap tbFrontGroupMap);

    FrontGroup queryFrontGroup(MapListParam mapListParam);

    int getCount(MapListParam mapListParam);

    int removeByGroupId(@Param("groupId") Integer groupId);

    int removeByFrontId(@Param("frontId") Integer frontId);

    List<FrontGroup> getList(MapListParam mapListParam);

    void removeInvalidMap();

    @Select({
        "select * from tb_front_group_map where group_id=#{groupId}"
    })
    List<TbFrontGroupMap> selectListByGroupId(@Param("groupId") int groupId);

    @Select({
        "update tb_front_group_map set modify_time = now(),status=#{status} where front_id=#{frontId}"
    })
    void updateStatus(@Param("frontId") int frontId,@Param("status") int status);
}

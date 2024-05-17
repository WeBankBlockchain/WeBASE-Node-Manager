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
package com.webank.webase.node.mgr.group;

import java.util.List;

import com.webank.common.mybatis.annotation.DataColumn;
import com.webank.common.mybatis.annotation.DataPermission;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import com.webank.webase.node.mgr.group.entity.GroupGeneral;
import com.webank.webase.node.mgr.group.entity.StatisticalGroupTransInfo;
import com.webank.webase.node.mgr.group.entity.TbGroup;

/**
 * mapper for table tb_group.
 */
@Repository
public interface GroupMapper {

    /**
     * add group info
     * @deprecated
     */
    @DataPermission({
            @DataColumn(key = "deptName", value = "dept_id"),
            @DataColumn(key = "userName", value = "user_id")
    })
    int save(TbGroup tbGroup);

    /**
     * insert selective
     */
    @DataPermission({
            @DataColumn(key = "deptName", value = "dept_id"),
            @DataColumn(key = "userName", value = "user_id")
    })
    int insertSelective(TbGroup tbGroup);

    /**
     * remove by id.
     */
    @DataPermission({
            @DataColumn(key = "deptName", value = "dept_id"),
            @DataColumn(key = "userName", value = "user_id")
    })
    int remove(@Param("groupId") String groupId);

    /**
     * update status.
     */
    @DataPermission({
            @DataColumn(key = "deptName", value = "dept_id"),
            @DataColumn(key = "userName", value = "user_id")
    })
    int updateStatus(@Param("groupId") String groupId, @Param("groupStatus") Integer groupStatus);

    @DataPermission({
            @DataColumn(key = "deptName", value = "dept_id"),
            @DataColumn(key = "userName", value = "user_id")
    })
    int updateDescription(@Param("groupId") String groupId, @Param("description") String description);


    /**
     * query group count.
     */
    int getCount(@Param("groupId") String groupId, @Param("groupStatus") Integer groupStatus);

    /**
     * get all group.
     */
    @DataPermission({
            @DataColumn(key = "deptName", value = "dept_id"),
            @DataColumn(key = "userName", value = "user_id")
    })
    List<TbGroup> getList(@Param("groupStatus") Integer groupStatus);

    /**
     * get group by group id
     */
    TbGroup getGroupById(@Param("groupId") String groupId);

    /**
     * query the latest statistics trans on all groups.
     */
    List<StatisticalGroupTransInfo> queryLatestStatisticalTrans();

    /**
     * query general info.
     */
    GroupGeneral getGeneral(@Param("groupId") String groupId);

    @DataPermission({
            @DataColumn(key = "deptName", value = "dept_id"),
            @DataColumn(key = "userName", value = "user_id")
    })
    int updateNodeCount(@Param("groupId") String groupId, @Param("nodeCount") int nodeCount);

    @DataPermission({
            @DataColumn(key = "deptName", value = "dept_id"),
            @DataColumn(key = "userName", value = "user_id")
    })
    int deleteByChainId(@Param("chainId") int chainId);

    @Select({
        "select * from tb_group where chain_id=#{chainId}"
    })
    List<TbGroup> selectGroupList(@Param("chainId") int chainId);

    @Select({
            "select * from tb_group where chain_id=#{chainId} and group_id=#{groupId}"
    })
    TbGroup getGroupByChainIdAndGroupId(@Param("chainId") int chainId, @Param("groupId") String groupId);

    @DataPermission({
            @DataColumn(key = "deptName", value = "dept_id"),
            @DataColumn(key = "userName", value = "user_id")
    })
    @Update({
       "update tb_group set group_timestamp=#{timestamp}, node_id_list=#{nodeIdList},modify_time=NOW() where group_id=#{groupId}"
    })
    int updateTimestampNodeList(@Param("groupId") String groupId, @Param("timestamp") long timestamp, @Param("nodeIdList") String nodeIdList);

    @Select("select encrypt_type from tb_group where group_id = #{groupId}")
    int getEncryptType(@Param("groupId") String groupId);
}

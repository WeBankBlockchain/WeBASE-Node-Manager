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
package com.webank.webase.node.mgr.front;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import com.webank.webase.node.mgr.front.entity.FrontParam;
import com.webank.webase.node.mgr.front.entity.TbFront;

@Repository
public interface FrontMapper {

    int add(TbFront tbFront);

    int update(TbFront tbFront);

    int remove(@Param("frontId") int frontId);

    Integer getCount(FrontParam param);

    List<TbFront> getList(FrontParam param);

    TbFront getById(@Param("frontId") int frontId);


    List<TbFront> selectByHostId(@Param("hostId") Integer hostId);
    List<TbFront> selectByAgencyId(@Param("agencyId") Integer agencyId);

    void deleteByAgencyId(@Param("agencyId") Integer agencyId);

    int updateStatus(@Param("frontId") int frontId,
                     @Param("status")int status,
                     @Param("modifyTime")LocalDateTime now);

    @Select({
            "select  max(host_index) from tb_front",
            "where host_id = #{hostId,jdbcType=INTEGER}"
    })
    Integer getNodeMaxIndex(int hostId);

    @Update({
        "update tb_front set client_version=#{newImageTag},image_tag=#{newImageTag},modify_time=#{modifyTime} where chain_id = #{chainId}"
    })
    int updateUpgradingByChainId(@Param("chainId") int chainId,
                                 @Param("newImageTag") String newImageTag,
                                 @Param("modifyTime") LocalDateTime now,
                                 @Param("status")int status);

    TbFront getByNodeId(@Param("nodeId") String nodeId);
}
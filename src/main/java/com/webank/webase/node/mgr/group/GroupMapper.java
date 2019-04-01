/*
 * Copyright 2014-2019  the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webank.webase.node.mgr.group;

import java.math.BigInteger;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * mapper for table tb_group.
 */
@Repository
public interface GroupMapper {

    /**
     * update group info.
     */
    Integer updateNetworkInfo(@Param("groupId") Integer groupId,
        @Param("latestBlock") BigInteger latestBlock);

    /**
     * query group count.
     */
    Integer countOfNetwork(@Param("groupId") Integer groupId);

    /**
     * get all group.
     */
    List<TbGroup> listAllNetwork();

    /**
     * query the latest statistics trans on all groups.
     */
    List<StatisticalGroupTransInfo> queryLatestStatisticalTrans();

    /**
     * query general info.
     */
    GroupGeneral queryNetworkGeneral(@Param("groupId") Integer groupId);

    /**
     * reset all trans count of group.
     */
    Integer resetTransCount(@Param("groupId") Integer groupId);
}

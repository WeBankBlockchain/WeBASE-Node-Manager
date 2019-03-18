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
package com.webank.webase.node.mgr.network;

import java.math.BigInteger;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * mapper for table tb_network.
 */
@Repository
public interface NetworkMapper {

    /**
     * update network info.
     */
    Integer updateNetworkInfo(@Param("networkId") Integer networkId,
        @Param("latestBlock") BigInteger latestBlock);

    /**
     * query network count.
     */
    Integer countOfNetwork(@Param("networkId") Integer networkId);

    /**
     * get all network.
     */
    List<TbNetwork> listAllNetwork();

    /**
     * query the latest statistics trans on all networks.
     */
    List<StatisticalNetworkTransInfo> queryLatestStatisticalTrans();

    /**
     * query general info.
     */
    NetworkGeneral queryNetworkGeneral(@Param("networkId") Integer networkId);

    /**
     * reset all trans count of network.
     */
    Integer resetTransCount(@Param("networkId") Integer networkId);
}

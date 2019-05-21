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
package com.webank.webase.node.mgr.transhash;

import com.webank.webase.node.mgr.block.MinMaxBlock;
import java.math.BigInteger;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * mapper about trans hash.
 */
@Repository
public interface TransHashMapper {

    Integer addTransRow(TbTransHash tbTransHash);

    Integer countOfTransHash(@Param("param") TransListParam param);

    List<TbTransHash> listOfTransHash(@Param("param") TransListParam param);

    List<MinMaxBlock> queryMinMaxBlock();

    Integer deleteSomeTrans(@Param("networkId") Integer networkId,
        @Param("deleteNumber") BigInteger deleteNumber);

    List<TbTransHash> listOfUnStatTransHash(@Param("networkList") List<Integer> networkList);

    List<TbTransHash> listOfUnStatTransHashByJob(
        @Param("shardingTotalCount") Integer shardingTotalCount,
        @Param("shardingItem") Integer shardingItem);

    void updateTransStatFlag(@Param("transHash") String transHash);
}

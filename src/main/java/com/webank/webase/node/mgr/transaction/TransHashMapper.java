/**
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
package com.webank.webase.node.mgr.transaction;

import com.webank.webase.node.mgr.block.entity.MinMaxBlock;
import com.webank.webase.node.mgr.transaction.entity.TbTransHash;
import com.webank.webase.node.mgr.transaction.entity.TransListParam;
import java.math.BigInteger;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * mapper about trans hash.
 */
@Repository
public interface TransHashMapper {

    Integer add(@Param("tableName") String tableName,@Param("trans")TbTransHash tbTransHash);

    Integer getCount(@Param("tableName") String tableName,@Param("param") TransListParam param);

    List<TbTransHash> getList(@Param("tableName") String tableName,@Param("param") TransListParam param);

    List<MinMaxBlock> queryMinMaxBlock(@Param("tableName") String tableName);

    Integer remove(@Param("tableName") String tableName,
        @Param("transRetainMax") BigInteger transRetainMax);

    List<TbTransHash> listOfUnStatTransHash(@Param("tableName") String tableName);

    List<TbTransHash> listOfUnStatTransHashByJob(@Param("tableName") String tableName,
        @Param("shardingTotalCount") Integer shardingTotalCount,
        @Param("shardingItem") Integer shardingItem);

    void updateTransStatFlag(@Param("tableName") String tableName,@Param("transHash") String transHash);
}

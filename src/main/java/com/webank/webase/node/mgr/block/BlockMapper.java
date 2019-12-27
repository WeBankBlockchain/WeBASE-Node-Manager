/**
 * Copyright 2014-2019  the original author or authors.
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
package com.webank.webase.node.mgr.block;

import com.webank.webase.node.mgr.block.entity.BlockListParam;
import com.webank.webase.node.mgr.block.entity.TbBlock;
import java.math.BigInteger;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * Block data interface.
 */
@Repository
public interface BlockMapper {

    /**
     * query latest block number
     */
    BigInteger getLatestBlockNumber(@Param("tableName") String tableName);

    /**
     * Add new block data.
     */
    Integer add(@Param("tableName") String tableName, @Param("block") TbBlock tbBlock);

    /**
     * update sealer.
     */
    Integer update(@Param("tableName") String tableName, @Param("block") TbBlock tbBlock);

    /**
     * query list of block by page.
     */
    List<TbBlock> getList(@Param("tableName") String tableName,
        @Param("param") BlockListParam param);

    /**
     * query block count.
     */
    Integer getCount(@Param("tableName") String tableName, @Param("pkHash") String pkHash,
        @Param("blockNumber") BigInteger blockNumber);

    /**
     * get block count by max minux min
     */
    Integer getBlockCountByMinMax(@Param("tableName") String tableName);
    /**
     * Delete block height.
     */
    Integer remove(@Param("tableName") String tableName,
        @Param("blockRetainMax") BigInteger blockRetainMax);
}

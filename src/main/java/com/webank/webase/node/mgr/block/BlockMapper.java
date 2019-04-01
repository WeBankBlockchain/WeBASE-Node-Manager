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
package com.webank.webase.node.mgr.block;

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
     * query latest block number by groupId.
     */
    BigInteger queryLatestBlockNumber(@Param("groupId") Integer groupId);

    /**
     * Add new block data.
     */
    Integer addBlockRow(TbBlock tbBlock);

    /**
     * query list of block by page.
     */
    List<TbBlock> listOfBlock(@Param("param") BlockListParam param);

    /**
     * query block count.
     */
    Integer countOfBlock(@Param("groupId") Integer groupId, @Param("pkHash") String pkHash,
        @Param("blockNumber") BigInteger blockNumber);

    /**
     * query the min and max block number of tb_block.
     */
    List<MinMaxBlock> queryMinMaxBlock();

    /**
     * Delete block height.
     */
    Integer deleteSomeBlocks(@Param("groupId") Integer groupId,
        @Param("deleteNumber") BigInteger deleteNumber);
}

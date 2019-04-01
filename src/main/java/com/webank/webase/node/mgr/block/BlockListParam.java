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

import com.webank.webase.node.mgr.base.entity.BaseQueryParam;
import java.math.BigInteger;
import java.time.LocalDate;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * param for block sql.
 */
@Data
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class BlockListParam extends BaseQueryParam {

    private String pkHash;
    private BigInteger blockNumber;
    private BigInteger minBlockNumber;
    private LocalDate minDay;
    private String flagSortedByBlock;

    /**
     * init by groupId、start、pageSize、pkHash、blockNumber.
     */
    public BlockListParam(Integer groupId, Integer start, Integer pageSize, String pkHash,
        BigInteger blockNumber,
        String flagSortedByBlock) {
        super(groupId, start, pageSize);
        this.pkHash = pkHash;
        this.blockNumber = blockNumber;
        this.flagSortedByBlock = flagSortedByBlock;
    }

    /**
     * init by groupId、minBlockNumber、minDay、flagSortedByBlock.
     */
    public BlockListParam(Integer groupId, BigInteger minBlockNumber, LocalDate minDay,
        String flagSortedByBlock) {
        super(groupId);
        this.minBlockNumber = minBlockNumber;
        this.minDay = minDay;
        this.flagSortedByBlock = flagSortedByBlock;
    }

}

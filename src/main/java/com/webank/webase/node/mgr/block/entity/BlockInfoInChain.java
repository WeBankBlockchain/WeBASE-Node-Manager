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
package com.webank.webase.node.mgr.block.entity;

import com.webank.webase.node.mgr.transhash.entity.TransactionInfo;
import java.math.BigInteger;
import java.util.List;
import lombok.Data;


@Data
public class BlockInfoInChain {
    private BigInteger number;
    private String hash;
    private String parentHash;
    private int nonce;
    private String logsBloom;
    private String transactionsRoot;
    private String stateRoot;
    private String sealer;
    private int difficulty;
    private int totalDifficulty;
    private List<Object> extraData;
    private int size;
    private Long gasLimit;
    private Long gasUsed;
    private Long timestamp;
    private String gasLimitRaw;
    private String timestampRaw;
    private String gasUsedRaw;
    private String numberRaw;
    private List<TransactionInfo> transactions;
}

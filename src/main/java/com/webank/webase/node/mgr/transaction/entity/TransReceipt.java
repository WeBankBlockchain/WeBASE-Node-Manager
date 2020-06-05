/**
 * Copyright 2014-2020  the original author or authors.
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
package com.webank.webase.node.mgr.transaction.entity;


import java.math.BigInteger;
import java.util.List;
import lombok.Data;

/**
 * transaction receipt entity
 */
@Data
public class TransReceipt {

    private String transactionHash;
    private int transactionIndex;
    private String blockHash;
    private BigInteger blockNumber;
    private int cumulativeGasUsed;
    private int gasUsed;
    private String contractAddress;
    private String status;
    private String from;
    private String to;
    private String output;
    private List<Object> logs;
    private String logsBloom;
    private String blockNumberRaw;
    private String transactionIndexRaw;
    private boolean statusOK;
    private String gasUsedRaw;

    private String root;
    private String message;
    private String input;
    /**
     * list of MerkleProofUnit
     */
    private List<Object> txProof;
    private List<Object> receiptProof;
}

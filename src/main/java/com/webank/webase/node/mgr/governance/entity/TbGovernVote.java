/**
 * Copyright 2014-2020 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.webank.webase.node.mgr.governance.entity;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * entity of committee vote record
 */
@Data
public class TbGovernVote {
    private Integer id;
    private Integer groupId;
    /**
     * blockHeight of ten times BlockLimit at voting time
     */
    private Long timeLimit;
//    private Long enableNum;
    private String fromAddress;
    /**
     * vote type: 1-grantCommittee, 2-revokeCommittee,
     * 3-updateCommitteeWeight, 4-updateThreshold
     */
    private Integer type;
    private String toAddress;
    /**
     * depends on type:
     * @case1: grantCommittee or revokeCommittee, empty
     * @case2: updateCommitteeWeight: example: {weight: 2},
     *         updateThreshold: example: {threshold: 2},
     */
    private String detail;
    private LocalDateTime createTime;
    private LocalDateTime modifyTime;
}

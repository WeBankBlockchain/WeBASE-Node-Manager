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
package com.webank.webase.node.mgr.front.entity;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class TbFront {
    private Integer frontId;
    private String nodeId;
    private String frontIp;
    private Integer frontPort;
    private String agency;
    // node version
    private String clientVersion;
    private LocalDateTime createTime;
    private LocalDateTime modifyTime;
}




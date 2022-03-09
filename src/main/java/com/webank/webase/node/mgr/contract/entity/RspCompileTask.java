/**
 * Copyright 2014-2022 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webank.webase.node.mgr.contract.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 保存编译的任务，单个合约只允许编译一次
 * 编译的任务完成不删除，只要不等于running，group_contractPath_contractName 与合约项目的目录名保持一致
 * @author lining
 */
@Data
public class RspCompileTask {
    private static final long serialVersionUID = 3286516914027062195L;
    private String groupId;

    private String contractName;
    private String contractPath;
    /**
     * compile status: 1-running, 2-success, 3-fail
     */
    private Integer status;
    private String abi;
    private String bin;
    private String description;
    private LocalDateTime createTime;
    private LocalDateTime modifyTime;
}

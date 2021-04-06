/**
 * Copyright 2014-2021  the original author or authors.
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
package com.webank.webase.node.mgr.base.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * executor of node-mgr async
 */
@Data
@Component
@ConfigurationProperties(prefix = ExecutorProperties.EXECUTOR_PREFIX)
public class ExecutorProperties {
    public static final String EXECUTOR_PREFIX = "executor";

    private Integer corePoolSize = 3;
    private Integer maxPoolSize = 5;
    private Integer queueSize = 50;
    private String threadNamePrefix = "node-mgr-async-";

}

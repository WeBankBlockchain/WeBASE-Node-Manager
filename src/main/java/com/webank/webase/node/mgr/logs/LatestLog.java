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
package com.webank.webase.node.mgr.logs;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * result entity of query latestNodeLog.
 */
@Data
public class LatestLog {

    private LocalDateTime logTime;
    private Integer rowNumber;
    private String fileName;

    /**
     * init by logTIme、rowNumber、fileName.
     */
    public LatestLog(LocalDateTime logTime, Integer rowNumber, String fileName) {
        super();
        this.logTime = logTime;
        this.rowNumber = rowNumber;
        this.fileName = fileName;
    }

}

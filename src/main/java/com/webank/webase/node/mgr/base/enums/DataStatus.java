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
package com.webank.webase.node.mgr.base.enums;

import lombok.ToString;

/**
 * Enumeration of data status.
 */
@ToString
public enum DataStatus {
    NORMAL(1), INVALID(2),
    /**
     * used in visual deploy node status(not front status)
     */
    STARTING(3),
    /**
     * node is down, but front is normal; used in manually deploy
     */
    DOWN(4);

    private int value;

    DataStatus(Integer dataStatus) {
        this.value = dataStatus;
    }

    public int getValue() {
        return this.value;
    }

    public static boolean starting(int status){
        return status == STARTING.getValue();
    }
}

/**
 * Copyright 2014-2020 the original author or authors.
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
package com.webank.webase.node.mgr.base.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 *
 */

@Getter
@ToString
@AllArgsConstructor
public enum HostStatusEnum {
    ADDED((byte) 0, "Added."),
    INITIALIZED((byte) 1, "Initialized."),
    FAILED((byte) 2, "Init failed."),
    SUCCESS((byte) 3, "Init success."),
    ;

    private byte id;
    private String description;


    /**
     * @param id
     * @return
     */
    public static HostStatusEnum getById(byte id) {
        for (HostStatusEnum value : HostStatusEnum.values()) {
            if (value.id == id) {
                return value;
            }
        }
        return null;
    }
}

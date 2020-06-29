/**
 * Copyright 2014-2020  the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */


package com.webank.webase.node.mgr.base.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public enum FrontStatusEnum {
    INITIALIZED( 0, "initialized"),
    RUNNING( 1, "running"),
    STOPPED(2, "stopped"),
    UPGRADING(3, "upgrading"),
    ;

    private int id;
    private String description;

    /**
     *
     * @param id
     * @return
     */
    public static FrontStatusEnum getById(int id) {
        for (FrontStatusEnum value : FrontStatusEnum.values()) {
            if (value.id == id) {
                return value;
            }
        }
        return null;
    }

    /**
     *
     * @param id
     * @return
     */
    public static boolean isRunning(int id){
        return id == RUNNING.getId();
    }
}

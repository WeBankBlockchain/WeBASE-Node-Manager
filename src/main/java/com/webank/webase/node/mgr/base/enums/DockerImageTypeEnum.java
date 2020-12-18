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
public enum DockerImageTypeEnum {
    MANUAL((byte) 0, "Download docker image and unzip manually."),
    PULL_OFFICIAL((byte) 1, "Pull image from docker registry."),
    PULL_CDN((byte) 2, "Pull image tar from cdn and load."),
    ;

    private byte id;
    private String description;

    /**
     * @param id
     * @return
     */
    public static DockerImageTypeEnum getById(byte id) {
        for (DockerImageTypeEnum value : DockerImageTypeEnum.values()) {
            if (value.id == id) {
                return value;
            }
        }
        return null;
    }


}


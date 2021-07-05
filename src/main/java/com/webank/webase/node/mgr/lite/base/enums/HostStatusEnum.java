/**
 * Copyright 2014-2021 the original author or authors.
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
package com.webank.webase.node.mgr.lite.base.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;

/**
 * added->check->docker_check->init
 */

@Getter
@ToString
@AllArgsConstructor
@Log4j2
public enum HostStatusEnum {
    ADDED((byte) 0, "added"),
    INITIATING((byte) 1, "host is initiating"),
    INIT_SUCCESS((byte) 2, "host init success"),
    INIT_FAILED((byte) 3, "host init failed"),
    CHECK_SUCCESS((byte) 4, "host check success"),
    CHECK_FAILED((byte) 5, "host check failed"),
    CONFIG_SUCCESS((byte) 6, "host config failed"),
    CONFIG_FAIL((byte) 7, "host config failed")
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

    public static boolean successOrInitiating(byte status){
        HostStatusEnum statusEnum = HostStatusEnum.getById(status);
        if (statusEnum == null) {
            log.error("Host with unknown status:[{}].", status);
            return false;
        }

        // check host status
        switch (statusEnum){
            case CONFIG_FAIL:
            case CONFIG_SUCCESS:
            case INIT_SUCCESS:
            case INITIATING:
                return true;
            default:
                return false;
        }
    }


    public static boolean hostCheckSuccess(byte status){
        HostStatusEnum statusEnum = HostStatusEnum.getById(status);
        if (statusEnum == null) {
            log.error("Host with unknown status:[{}].", status);
            return false;
        }

        // check host status
        switch (statusEnum){
            // if init or init ing means already checked
            // if init failed ,check again available
            case CONFIG_FAIL:
            case CONFIG_SUCCESS:
            case INIT_SUCCESS:
            case INITIATING:
            case CHECK_SUCCESS:
                return true;
            default:
                return false;
        }
    }
}

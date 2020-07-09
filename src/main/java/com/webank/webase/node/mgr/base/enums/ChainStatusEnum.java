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

import com.webank.webase.node.mgr.base.tools.NumberUtil;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;

/**
 *
 */

@Getter
@ToString
@AllArgsConstructor
@Log4j2
public enum ChainStatusEnum {
    INITIALIZED((byte) 0, "initialized"),
    DEPLOYING((byte) 1, "deploying"),
    UPGRADING((byte) 2, "Upgrading"),
    DEPLOY_FAILED((byte) 3, "Deploy failed"),
    UPGRADING_FAILED((byte) 4, "Upgrading failed"),
    RUNNING((byte) 5, "Running"),
    UPDATING((byte) 6, "Updating"),
    ;

    private byte id;
    private String description;

    /**
     * @param id
     * @return
     */
    public static ChainStatusEnum getById(byte id) {
        for (ChainStatusEnum value : ChainStatusEnum.values()) {
            if (value.id == id) {
                return value;
            }
        }
        return null;
    }

    /**
     *
     * @param status
     * @return
     */
    public static boolean successOrDeploying(byte status){
        ChainStatusEnum statusEnum = ChainStatusEnum.getById(status);
        if (statusEnum == null) {
            log.error("Chain with unknown status:[{}].", status);
            return false;
        }

        // check chain status
        switch (statusEnum){
            case DEPLOYING:
            case RUNNING:
                return true;
            default:
                return false;
        }
    }

    /**
     *
     * @param status
     * @return
     */
    public static int progress(byte status){
        ChainStatusEnum statusEnum = ChainStatusEnum.getById(status);
        if (statusEnum == null) {
            log.error("Chain with unknown status:[{}].", status);
            return NumberUtil.PERCENTAGE_FAILED;
        }

        // check chain status
        switch (statusEnum){
            case DEPLOY_FAILED:
            case UPGRADING_FAILED:
                return NumberUtil.PERCENTAGE_FAILED;

            case RUNNING:
                return NumberUtil.PERCENTAGE_FINISH;
            default:
                return NumberUtil.PERCENTAGE_IN_PROGRESS;
        }
    }
}

/**
 * Copyright 2014-2020 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
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
public enum FrontDeployStatusEnum {
    ADDED( 0, "已添加"),
    CHECK_SUCCESS( 1, "检测成功"),
    CHECK_FAIL(2, "检测失败"),
    RUNNING(3, "运行中"),
    ;

    private int id;
    private String description;

    /**
     *
     * @param id
     * @return
     */
    public static FrontDeployStatusEnum getById(int id) {
        for (FrontDeployStatusEnum value : FrontDeployStatusEnum.values()) {
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
    public static boolean isCheckSuccess(int id){
        return id == CHECK_SUCCESS.getId();
    }
}

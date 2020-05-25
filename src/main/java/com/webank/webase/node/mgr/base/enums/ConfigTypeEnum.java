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

public enum ConfigTypeEnum {
    DOCKER_IMAGE_TYPE((short)1,"docker 镜像版本", "Supported tags of docker image FISCO-BCOS and WeBASE-Front."),
    ;

    private short id;
    private String name;
    private String description;

    /**
     * @param id
     * @param name
     * @param description
     */
    ConfigTypeEnum(short id,String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    /**
     *
     * @param id
     * @return
     */
    public static ConfigTypeEnum getById(short id) {
        for (ConfigTypeEnum value : ConfigTypeEnum.values()) {
            if (value.id == id) {
                return value;
            }
        }
        return null;
    }

    public short getId() {
        return id;
    }


    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ConfigTypeEnum{");
        sb.append("id=").append(id);
        sb.append(", name='").append(name).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append('}');
        return sb.toString();
    }
}

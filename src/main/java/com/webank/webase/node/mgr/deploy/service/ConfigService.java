/**
 * Copyright 2014-2021  the original author or authors.
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

package com.webank.webase.node.mgr.deploy.service;

import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.enums.ConfigTypeEnum;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.deploy.entity.TbConfig;
import com.webank.webase.node.mgr.deploy.mapper.TbConfigMapper;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.sdk.model.CryptoType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class ConfigService {

    @Autowired private TbConfigMapper tbConfigMapper;

    /**
     * Select config by type.
     *
     * @param update Update config when value is true.
     * @param type
     * @return
     */
    public List<TbConfig> selectConfigList(boolean update, int type) {
        List<TbConfig> configList = tbConfigMapper.selectByType(type);
        return configList;

    }

    private static List<TbConfig> filterByEncryptType(List<TbConfig> configList, int encryptType) {
        switch (encryptType) {
            case CryptoType.ECDSA_TYPE:
                return configList.stream().filter(config -> !StringUtils.endsWith(config.getConfigValue(), "-gm"))
                        .collect(Collectors.toList());
            case CryptoType.SM_TYPE:
                return configList.stream().filter(config -> StringUtils.endsWith(config.getConfigValue(), "-gm"))
                        .collect(Collectors.toList());
            default:
                break;
        }
        return configList;
    }


    /**
     * Result of listing tags from hub.docker.com.
     */
    static class ImageTag {

        private String layer;
        private String name;

        public String getLayer() {
            return layer;
        }

        public String getName() {
            return name;
        }

        public void setLayer(String layer) {
            this.layer = layer;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    /**
     * check config value in db
     */
    public void checkValueInDb(String configValue) {
        log.info("checkValueInDb configValue:{}", configValue);
        List<TbConfig> configList = tbConfigMapper.selectByConfigValue(configValue);
        log.info("checkValueInDb get config list:{}", configList);
        if (configList == null || configList.isEmpty()) {
            log.error("checkValueInDb configValue:{} not in db!", configValue);
            throw new NodeMgrException(ConstantCode.TAG_ID_PARAM_ERROR);
        }
    }

}


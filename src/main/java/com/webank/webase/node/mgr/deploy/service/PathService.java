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
package com.webank.webase.node.mgr.deploy.service;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.webank.webase.node.mgr.base.properties.ConstantProperties;

import lombok.extern.slf4j.Slf4j;

/**
 *
 */

@Slf4j
@Component
public class PathService {

    @Autowired private ConstantProperties constant;

    /**
     * The file to save ip list config.
     * @param chainName
     * @return
     */
    public String getIpConfigPath(String chainName) {
        return String.format("%s%s-ipconf", getRoot(), chainName);
    }

    /**
     * Root dir of the nodes config.
     * @param chainName
     * @return
     */
    public String getChainRoot(String chainName) {
        return String.format("%s%s-nodes", getRoot(), chainName);
    }

    private String getRoot() {
        if (StringUtils.isBlank(constant.getGenerateNodesRoot())) {
            // return "." by default
            return ".";
        }
        if (constant.getGenerateNodesRoot().trim().endsWith(File.separator)) {
            // ends with separator
            return constant.getGenerateNodesRoot().trim();
        }
        // append a separator
        return String.format("%s%s", constant.getGenerateNodesRoot().trim(), File.separator);

    }


}
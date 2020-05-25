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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.enums.ChainStatusEnum;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.deploy.entity.TbChain;
import com.webank.webase.node.mgr.deploy.mapper.TbChainMapper;

import lombok.extern.slf4j.Slf4j;

/**
 *
 */

@Slf4j
@Component
public class ChainService {

    @Autowired private TbChainMapper tbChainMapper;

    public TbChain insert(String chainName,
                                        String chainDesc,
                                        String version,
                                        byte encryptType,
                                        ChainStatusEnum status) throws NodeMgrException{
        // TODO. params check

        TbChain chain = TbChain.build(chainName, chainDesc, version, encryptType, status);

        if (tbChainMapper.insertSelective(chain) != 1 || chain.getId() <= 0) {
            throw new NodeMgrException(ConstantCode.INSERT_CHAIN_ERROR);
        }
        return chain;
    }
}
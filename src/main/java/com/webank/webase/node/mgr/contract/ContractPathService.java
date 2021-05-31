/**
 * Copyright 2014-2021 the original author or authors.
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

package com.webank.webase.node.mgr.contract;

import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.contract.entity.ContractPathParam;
import com.webank.webase.node.mgr.contract.entity.TbContractPath;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * contract's path in IDE
 */
@Log4j2
@Service
public class ContractPathService {
    @Autowired
    private ContractPathMapper contractPathMapper;

    public List<TbContractPath> listContractPath(Integer groupId, String account) {
        return contractPathMapper.listOfContractPath(groupId, account);
    }

    /**
     * save not exist path
     * 
     * @param groupId
     * @param pathName
     * @return
     */
    public int save(Integer groupId, String pathName, String account, boolean ignoreRepeat) {
        TbContractPath check =
                contractPathMapper.findOne(new ContractPathParam(groupId, pathName, null));
        if (check != null) {
            if (ignoreRepeat) {
                return 0;
            } else {
                log.error("save path, path exists check:{}", check);
                throw new NodeMgrException(ConstantCode.CONTRACT_PATH_IS_EXISTS);
            }
        }
        TbContractPath contractPath = new TbContractPath();
        contractPath.setContractPath(pathName);
        contractPath.setGroupId(groupId);
        contractPath.setAccount(account);
        return contractPathMapper.add(contractPath);
    }

    /**
     * update contract path: update all related contract
     */
    public int updatePath(Integer pathId, String pathName) {
        TbContractPath contractPath = new TbContractPath();
        contractPath.setContractPath(pathName);
        contractPath.setId(pathId);
        return contractPathMapper.update(contractPath);
    }

    public int remove(Integer pathId) {
        return contractPathMapper.remove(pathId);
    }

    public int removeByPathName(ContractPathParam param) {
        log.info("removeByPathName param:{}", param);
        return contractPathMapper.removeByPathName(param);
    }

    public void removeByGroupId(Integer groupId) {
        contractPathMapper.removeByGroupId(groupId);
    }

    public boolean checkPathExist(Integer groupId, String pathName, String account) {
        TbContractPath contractPath =
                contractPathMapper.findOne(new ContractPathParam(groupId, pathName, account));
        if (contractPath != null) {
            return true;
        } else {
            return false;
        }
    }
}

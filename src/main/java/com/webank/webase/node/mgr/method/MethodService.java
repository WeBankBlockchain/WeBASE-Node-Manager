/**
 * Copyright 2014-2019  the original author or authors.
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
package com.webank.webase.node.mgr.method;

import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.constant.MethodIdConstants;
import com.webank.webase.node.mgr.base.enums.ContractType;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.method.entity.Method;
import com.webank.webase.node.mgr.method.entity.NewMethodInputParam;
import com.webank.webase.node.mgr.method.entity.TbMethod;
import java.util.List;
import java.util.Objects;

import com.webank.webase.node.mgr.method.entity.MethodUpdateParam;
import lombok.extern.log4j.Log4j2;
import org.fisco.bcos.web3j.crypto.EncryptType;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class MethodService {

    @Autowired
    private MethodMapper methodMapper;

    /**
     * save method info.
     */
    public void saveMethod(NewMethodInputParam newMethodInputParam) {
        List<Method> methodList = newMethodInputParam.getMethodList();
        TbMethod tbMethod = new TbMethod();
        tbMethod.setGroupId(newMethodInputParam.getGroupId());
        tbMethod.setContractType(ContractType.GENERALCONTRACT.getValue());
        //save each method
        for (Method method : methodList) {
            BeanUtils.copyProperties(method, tbMethod);
            methodMapper.add(tbMethod);
        }
    }

    /**
     * query by methodId.
     */
    public TbMethod getByMethodId(String methodId, Integer groupId) {
        // refresh method id to guomi version
        refreshMethodId2GM();
        TbMethod tbMethod = methodMapper.getMethodById(methodId, null);
        if (Objects.nonNull(tbMethod)) {
            if (ContractType.SYSTEMCONTRACT.getValue() == tbMethod.getContractType().intValue()) {
                return tbMethod;
            } else {
                return methodMapper.getMethodById(methodId, groupId);
            }
        }
        return null;
    }

    /**
     * refresh precompiled contract's methodId in tb_method
     *
     */
    private void refreshMethodId2GM() {
        // is guomi and switched2Gm is false
        if(EncryptType.encryptType == 1 && !MethodIdConstants.switched2Gm) {
            try{
                updateMethodIdForGm(MethodIdConstants.SYSTEM_CONFIG_SET_VALUE_BY_KEY_STANDARD,
                        MethodIdConstants.SYSTEM_CONFIG_SET_VALUE_BY_KEY_GM);
                updateMethodIdForGm(MethodIdConstants.TABLE_FACTORY_CREATE_TABLE_STANDARD,
                        MethodIdConstants.TABLE_FACTORY_CREATE_TABLE_GM);
                updateMethodIdForGm(MethodIdConstants.CRUD_UPDATE_STANDARD,
                        MethodIdConstants.CRUD_UPDATE_GM);
                updateMethodIdForGm(MethodIdConstants.CRUD_SELECT_STANDARD,
                        MethodIdConstants.CRUD_SELECT_GM);
                updateMethodIdForGm(MethodIdConstants.CRUD_REMOVE_STANDARD,
                        MethodIdConstants.CRUD_REMOVE_GM);
                updateMethodIdForGm(MethodIdConstants.CRUD_INSERT_STANDARD,
                        MethodIdConstants.CRUD_INSERT_GM);
                updateMethodIdForGm(MethodIdConstants.CONSENSUS_ADD_OBSERVER_STANDARD,
                        MethodIdConstants.CONSENSUS_ADD_OBSERVER_GM);
                updateMethodIdForGm(MethodIdConstants.CONSENSUS_ADD_SEALER_STANDARD,
                        MethodIdConstants.CONSENSUS_ADD_SEALER_GM);
                updateMethodIdForGm(MethodIdConstants.CONSENSUS_REMOVE_STANDARD,
                        MethodIdConstants.CONSENSUS_REMOVE_GM);
                updateMethodIdForGm(MethodIdConstants.CNS_SELECT_BY_NAME_STANDARD,
                        MethodIdConstants.CNS_SELECT_BY_NAME_GM);
                updateMethodIdForGm(MethodIdConstants.CNS_SELECT_BY_NAME_AND_VERSION_STANDARD,
                        MethodIdConstants.CNS_SELECT_BY_NAME_AND_VERSION_GM);
                updateMethodIdForGm(MethodIdConstants.PERMISSION_INSERT_STANDARD,
                        MethodIdConstants.PERMISSION_INSERT_GM);
                updateMethodIdForGm(MethodIdConstants.PERMISSION_QUERY_BY_NAME_STANDARD,
                        MethodIdConstants.PERMISSION_QUERY_BY_NAME_GM);
                updateMethodIdForGm(MethodIdConstants.PERMISSION_REMOVE_STANDARD,
                        MethodIdConstants.PERMISSION_REMOVE_GM);
                MethodIdConstants.switched2Gm = true;
            }catch (Exception e) {
                log.error("refreshMethodId2GM error: []", e);
                throw new NodeMgrException(ConstantCode.UPDATE_METHOD_ID_GM_ERROR.getCode(),
                        e.getMessage());
            }
        }
    }

    public void updateMethodIdForGm(String oldMethodId, String newMethodId) {
        MethodUpdateParam updateParam = new MethodUpdateParam();
        updateParam.setContractType(ContractType.SYSTEMCONTRACT.getValue());
        updateParam.setMethodId(oldMethodId);
        updateParam.setNewMethodId(newMethodId);
        methodMapper.updateMethodId(updateParam);
    }
    /**
     * delete by groupId.
     */
    public void deleteByGroupId(int groupId){
        if(groupId==0){
            return;
        }
        methodMapper.removeByGroupId(groupId);
    }

}

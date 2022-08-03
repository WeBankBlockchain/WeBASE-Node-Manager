/**
 * Copyright 2014-2021  the original author or authors.
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.enums.ContractType;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.tools.Web3Tools;
import com.webank.webase.node.mgr.method.entity.Method;
import com.webank.webase.node.mgr.method.entity.NewMethodInputParam;
import com.webank.webase.node.mgr.method.entity.TbMethod;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import lombok.extern.log4j.Log4j2;
import org.fisco.bcos.sdk.v3.codec.wrapper.ABIDefinition;
import org.fisco.bcos.sdk.v3.crypto.CryptoSuite;
import org.fisco.bcos.sdk.v3.utils.ObjectMapperFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class MethodService {

    @Autowired
    private MethodMapper methodMapper;
    @Autowired
    private CryptoSuite cryptoSuite;

    /**
     * save method by abi string
     *
     * @param groupId
     * @param abiStr
     * @param type
     */
    public void saveMethod(String groupId, String abiStr, int type) {
        List<Method> methodList;
        try {
            methodList = new ArrayList<>(Web3Tools.getMethodFromAbi(abiStr, cryptoSuite));
        } catch (IOException e) {
            log.error("saveMethod failed:[]", e);
            throw new NodeMgrException(ConstantCode.PARAM_FAIL_ABI_INVALID);
        }
        NewMethodInputParam newMethodInputParam = new NewMethodInputParam();
        newMethodInputParam.setGroupId(groupId);
        newMethodInputParam.setMethodList(methodList);
        this.saveMethod(newMethodInputParam, type);
    }

    /**
     * save method info
     */
    public void saveMethod(NewMethodInputParam newMethodInputParam, Integer type) {
        String groupId = newMethodInputParam.getGroupId();
        List<Method> methodList = newMethodInputParam.getMethodList();
        TbMethod tbMethod = new TbMethod();
        tbMethod.setGroupId(groupId);
        tbMethod.setContractType(type);
        //save each method
        for (Method method : methodList) {
            if (checkMethodExist(method.getMethodId(), groupId)) {
                log.info("methodId of [{}] in group [{}] exist, jump over", method.getMethodId(),
                    groupId);
                continue;
            }
            BeanUtils.copyProperties(method, tbMethod);
            methodMapper.add(tbMethod);
        }
    }

    /**
     * checkMethod whether exist
     *
     * @param methodId
     * @param groupId
     * @return
     */
    private boolean checkMethodExist(String methodId, String groupId) {
        TbMethod check = methodMapper.getMethodById(methodId, groupId);
        if (Objects.nonNull(check)) {
            return true;
        }
        return false;
    }

    /**
     * query by methodId.
     */
    public TbMethod getByMethodId(String methodId, String groupId) {
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
     * delete by groupId.
     */
    public void deleteByGroupId(String groupId) {
        if (groupId.isEmpty()) {
            return;
        }
        methodMapper.removeByGroupId(groupId);
    }

    /**
     * compute MethodId
     */
    public ArrayList<Map<String, String>> computeMethodId(List<ABIDefinition> abiDefinitionList,
        Integer integer) throws JsonProcessingException {
        ArrayList<Map<String, String>> mapList = new ArrayList<>();
        String buildMethodId = "";
        for (ABIDefinition abiDefinition : abiDefinitionList) {
            if ("function".equals(abiDefinition.getType())) {
                Map<String, String> stringMap = new HashMap<>();
                // support guomi sm3
                if (integer == 1) {
                    CryptoSuite c1 = new CryptoSuite(1);
                    buildMethodId = Web3Tools.buildMethodId(abiDefinition, c1);
                } else {
                    CryptoSuite c2 = new CryptoSuite(0);
                    buildMethodId = Web3Tools.buildMethodId(abiDefinition, c2);
                }
                ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
                String abiStr = objectMapper.writeValueAsString(abiDefinition);
                System.out.println(
                    buildMethodId + ": " + abiDefinition.getName() + ": " + abiStr);
                stringMap.put(buildMethodId, abiStr);
                mapList.add(stringMap);
            }
        }
        return mapList;
    }

    /**
     * get MethodId DmlSql
     */
    public ArrayList<String> getMethodIdDmlSql(List<ABIDefinition> abiDefinitionList,
        Integer integer) throws JsonProcessingException {
        ArrayList<String> stringArrayList = new ArrayList<>();
        String buildMethodId = "";
        for (ABIDefinition abiDefinition : abiDefinitionList) {
            if ("function".equals(abiDefinition.getType())) {
                String sql = "";
                // support guomi sm3
                if (integer == 1) {
                    CryptoSuite c1 = new CryptoSuite(1);
                    buildMethodId = Web3Tools.buildMethodId(abiDefinition, c1);
                } else {
                    CryptoSuite c2 = new CryptoSuite(0);
                    buildMethodId = Web3Tools.buildMethodId(abiDefinition, c2);
                }
                ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
                String abiStr = objectMapper.writeValueAsString(abiDefinition);
                System.out.println(
                    buildMethodId + ": " + abiDefinition.getName() + ": " + abiStr);
                sql = "INSERT INTO `tb_method`(`method_id`, `group_id`, `abi_info`, `method_type`, "
                    + "`contract_type`, `create_time`, `modify_time`) VALUES (" + "'"
                    + buildMethodId + "'" + ", 'group'," + "'"
                    + abiStr + "'" + ", 'function', 1, now(), now());";
                stringArrayList.add(sql);
            }
        }
        return stringArrayList;
    }
}

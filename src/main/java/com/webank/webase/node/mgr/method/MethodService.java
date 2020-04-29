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
package com.webank.webase.node.mgr.method;

import com.webank.webase.node.mgr.base.enums.ContractType;
import com.webank.webase.node.mgr.method.entity.Method;
import com.webank.webase.node.mgr.method.entity.NewMethodInputParam;
import com.webank.webase.node.mgr.method.entity.TbMethod;
import java.util.List;
import java.util.Objects;

import lombok.extern.log4j.Log4j2;
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
    public void deleteByGroupId(int groupId){
        if(groupId==0){
            return;
        }
        methodMapper.removeByGroupId(groupId);
    }

}

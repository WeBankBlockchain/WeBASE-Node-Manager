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

package com.webank.webase.node.mgr.contract;

import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.base.tools.JsonTools;
import com.webank.webase.node.mgr.contract.entity.QueryCnsParam;
import com.webank.webase.node.mgr.contract.entity.ReqRegisterCns;
import com.webank.webase.node.mgr.contract.entity.TbCns;
import com.webank.webase.node.mgr.frontinterface.FrontRestTools;
import com.webank.webase.node.mgr.user.UserService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.log4j.Log4j2;
import org.fisco.bcos.web3j.protocol.core.methods.response.AbiDefinition;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * cns service.
 */
@Log4j2
@Service
public class CnsService {

    @Autowired
    private CnsMapper cnsMapper;
    @Autowired
    private UserService userService;
    @Autowired
    private FrontRestTools frontRestTools;

    /**
     * registerCns.
     */
    public void registerCns(ReqRegisterCns inputParam) throws NodeMgrException {
        log.info("start registerCns. inputParam:{}", JsonTools.toJSONString(inputParam));
        int groupId = inputParam.getGroupId();
        String contractName = inputParam.getContractName();
        List<AbiDefinition> abiArray =
                JsonTools.toJavaObjectList(inputParam.getContractAbi(), AbiDefinition.class);
        if (abiArray == null || abiArray.isEmpty()) {
            log.error("fail registerCns. abi is empty");
            throw new NodeMgrException(ConstantCode.CONTRACT_ABI_EMPTY);
        }
        // check version
        QueryCnsParam queryParam = new QueryCnsParam(inputParam.getGroupId(),
                inputParam.getCnsName(), inputParam.getVersion());
        int count = countOfCns(queryParam);
        if (count > 0) {
            log.error("fail registerCns. version already exists.");
            throw new NodeMgrException(ConstantCode.VERSION_ALREADY_EXISTS);
        }

        // get signUserId
        String signUserId =
                userService.getSignUserIdByAddress(groupId, inputParam.getUserAddress());
        Map<String, Object> params = new HashMap<>();
        params.put("groupId", groupId);
        params.put("signUserId", signUserId);
        params.put("contractName", contractName);
        params.put("cnsName", contractName);
        params.put("version", inputParam.getVersion());
        params.put("contractAddress", inputParam.getContractAddress());
        params.put("abiInfo", abiArray);

        // register
        frontRestTools.postForEntity(groupId, FrontRestTools.URI_CONTRACT_REGISTER_CNS, params,
                Object.class);
        // save cns
        TbCns tbCns = new TbCns();
        BeanUtils.copyProperties(inputParam, tbCns);
        cnsMapper.add(tbCns);
    }

    /**
     * get count.
     * 
     * @param queryCnsParam
     * @return
     */
    public int countOfCns(QueryCnsParam queryCnsParam) {
        return cnsMapper.countOfCns(queryCnsParam);
    }

    /**
     * get List.
     * 
     * @param queryCnsParam
     * @return
     */
    public List<TbCns> getList(QueryCnsParam queryCnsParam) {
        return cnsMapper.getList(queryCnsParam);
    }

    /**
     * getCnsByAddress.
     * 
     * @param queryCnsParam
     * @return
     */
    public TbCns getCnsByAddress(QueryCnsParam queryCnsParam) {
        return cnsMapper.getCnsByAddress(queryCnsParam);
    }
}

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
package com.webank.webase.node.mgr.contract;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.webank.webase.node.mgr.base.entity.ConstantCode;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.contract.entity.ContractParam;
import com.webank.webase.node.mgr.contract.entity.DeployIncoming;
import com.webank.webase.node.mgr.contract.entity.TbContract;
import com.webank.webase.node.mgr.contract.entity.Transaction;
import com.webank.webase.node.mgr.frontinterface.FrontInterfaceService;
import com.webank.webase.node.mgr.frontinterface.FrontRestTools;
import com.webank.webase.node.mgr.monitor.MonitorService;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * services for contract data.
 */
@Log4j2
@Service
public class ContractService {

    @Autowired
    private ContractMapper contractMapper;
    @Autowired
    private FrontRestTools frontRestTools;
    @Autowired
    private MonitorService monitorService;
    @Autowired
    private FrontInterfaceService frontInterface;

    /**
     * delete contract by contractId.
     */
/*    public void deleteContract(Integer contractId) throws NodeMgrException {
        log.debug("start deleteContract contractId:{} ", contractId);
        // check contract id
        TbContract tbContract = contractMapper.queryByContractId(contractId);
        if (tbContract == null) {
            throw new NodeMgrException(ConstantCode.INVALID_CONTRACT_ID);
        }

        contractMapper.deleteContract(contractId);

        log.debug("end deleteContract");
    }*/

    /**
     * query contract list.
     */
    public List<TbContract> qureyContractList(ContractParam param) throws NodeMgrException {
        log.debug("start qureyContractList ContractListParam:{}", JSON.toJSONString(param));

        // qurey contract list
        List<TbContract> listOfContract = contractMapper.listOfContract(param);

        log.debug("end qureyContractList listOfContract:{}", JSON.toJSONString(listOfContract));
        return listOfContract;
    }


    /**
     * query count of contract.
     */
    public int countOfContract(ContractParam param) throws NodeMgrException {
        log.debug("start countOfContract ContractListParam:{}", JSON.toJSONString(param));
        try {
            return contractMapper.countOfContract(param);
        } catch (RuntimeException ex) {
            log.error("fail countOfContract", ex);
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }
    }

    /**
     * query contract by contract id.
     */
    public TbContract queryByContractId(Integer contractId) throws NodeMgrException {
        log.debug("start queryContract contractId:{}", contractId);
        try {
            TbContract contractRow = contractMapper.queryByContractId(contractId);
            log.debug("start queryContract contractId:{} contractRow:{}", contractId,
                JSON.toJSONString(contractRow));
            return contractRow;
        } catch (RuntimeException ex) {
            log.error("fail countOfContract", ex);
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }

    }


    /**
     * query DeployIncoming By Address.
     */
    public List<TbContract> queryContractByBin(Integer groupId, String contractBin)
        throws NodeMgrException {
        try {
            List<TbContract> contractRow = contractMapper
                .queryContractByBin(groupId, contractBin);
            log.debug("start queryContractByBin:{}", contractBin, JSON.toJSONString(contractRow));
            return contractRow;
        } catch (RuntimeException ex) {
            log.error("fail queryContractByBin", ex);
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }

    }


    /**
     * deploy contract.
     */
    @Transactional
    public TbContract deployContract(DeployIncoming deployIncoming) throws NodeMgrException {
        log.info("start deployContract. deployIncoming:{}", JSON.toJSONString(deployIncoming));
        Integer groupId = deployIncoming.getGroupId();
        String contractName = deployIncoming.getContractName();
        String contractVersion = deployIncoming.getContractVersion();

        //check contract
        verifyContractNotExist(groupId, contractName, contractVersion);

        // deploy param
        Map<String, Object> params = new HashMap<>();
        params.put("groupId", groupId);
        params.put("userId", deployIncoming.getUserId());
        params.put("contractName", contractName);
        params.put("version", contractVersion);
        params.put("abiInfo", JSONArray.parseArray(deployIncoming.getContractAbi()));
        params.put("bytecodeBin", deployIncoming.getBytecodeBin());
        params.put("funcParam", deployIncoming.getConstructorParams());

        //deploy
        String contractAddress = frontRestTools.postForEntity(groupId,
            FrontRestTools.URI_CONTRACT_DEPLOY, params, String.class);

        //save contract
        TbContract tbContract = new TbContract();
        BeanUtils.copyProperties(deployIncoming, tbContract);
        tbContract.setContractAddress(contractAddress);
        contractMapper.addContractRow(tbContract);

        // update monitor unusual deployIncoming's info
        monitorService
            .updateUnusualContract(groupId, contractName, deployIncoming.getContractBin());

        log.debug("end deployContract. contractId:{} groupId:{} contractAddress:{}",
            tbContract.getContractId(),
            groupId, contractAddress);
        return tbContract;
    }

    /**
     * query contract info.
     */
    public TbContract queryContract(ContractParam queryParam) {
        log.debug("start queryContract. queryParam:{}", JSON.toJSONString(queryParam));
        TbContract tbContract = contractMapper.queryContract(queryParam);
        log.debug("end queryContract. queryParam:{} tbContract:{}", JSON.toJSONString(queryParam),
            JSON.toJSONString(tbContract));
        return tbContract;
    }


    /**
     * send transaction.
     */
    public Object sendTransaction(Transaction param) throws NodeMgrException {
        log.debug("start sendTransaction. param:{}", JSON.toJSONString(param));

        if (param == null) {
            log.info("fail sendTransaction. request param is null");
            throw new NodeMgrException(ConstantCode.INVALID_PARAM_INFO);
        }
        // query param
        ContractParam queryParam = new ContractParam();
        queryParam.setContractName(param.getContractName());
        queryParam.setContractVersion(param.getVersion());

        // query contract row
        TbContract contractRow = queryContract(queryParam);
        if (contractRow == null) {
            log.info("fail sendTransaction. contract had not deploy");
            throw new NodeMgrException(ConstantCode.INVALID_CONTRACT);
        }

        // request send transaction
        Object frontRsp = frontRestTools
            .postForEntity(param.getGroupId(), FrontRestTools.URI_SEND_TRANSACTION, param,
                Object.class);
        log.debug("end sendTransaction. frontRsp:{}", JSON.toJSONString(frontRsp));
        return frontRsp;
    }


    /**
     * get contract code
     */
    public String getContractCode(int groupId, String address, BigInteger blockNumber) {
        return frontInterface.getContractCode(groupId, address, blockNumber);
    }

    /**
     * verify that the contract does not exist.
     */
    private void verifyContractNotExist(int groupId, String name, String version) {
        ContractParam param = new ContractParam(groupId, name, version);
        int count = countOfContract(param);
        if (count > 0) {
            log.warn("contract is exist. groupId:{} name:{} version:{}", groupId, name, version);
            throw new NodeMgrException(ConstantCode.CONTRACT_EXISTS);
        }
    }


}

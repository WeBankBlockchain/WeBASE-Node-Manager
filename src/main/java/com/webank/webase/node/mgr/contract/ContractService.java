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
import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.enums.ContractStatus;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.base.properties.ConstantProperties;
import com.webank.webase.node.mgr.base.tools.Web3Tools;
import com.webank.webase.node.mgr.contract.entity.Contract;
import com.webank.webase.node.mgr.contract.entity.ContractParam;
import com.webank.webase.node.mgr.contract.entity.DeployInputParam;
import com.webank.webase.node.mgr.contract.entity.TbContract;
import com.webank.webase.node.mgr.contract.entity.TransactionInputParam;
import com.webank.webase.node.mgr.front.entity.TransactionParam;
import com.webank.webase.node.mgr.frontinterface.FrontInterfaceService;
import com.webank.webase.node.mgr.frontinterface.FrontRestTools;
import com.webank.webase.node.mgr.frontinterface.entity.PostAbiInfo;
import com.webank.webase.node.mgr.monitor.MonitorService;
import com.webank.webase.node.mgr.user.UserService;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.web3j.protocol.core.methods.response.AbiDefinition;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/**
 * services for contract data.
 */
@Log4j2
@Service
public class  ContractService {

    @Autowired
    private ContractMapper contractMapper;
    @Autowired
    private FrontRestTools frontRestTools;
    @Autowired
    @Lazy
    private MonitorService monitorService;
    @Autowired
    private FrontInterfaceService frontInterface;
    @Autowired
    private UserService userService;
    private static final int CONTRACT_ADDRESS_LENGTH = 42;

    /**
     * add new contract data.
     */
    public TbContract saveContract(Contract contract) throws NodeMgrException {
        log.debug("start addContractInfo Contract:{}", JSON.toJSONString(contract));
        TbContract tbContract;
        if (contract.getContractId() == null) {
            tbContract = newContract(contract);//new
        } else {
            tbContract = updateContract(contract);//update
        }

        if (Objects.nonNull(tbContract) && StringUtils.isNotBlank(tbContract.getContractBin())) {
            // update monitor unusual deployInputParam's info
            monitorService.updateUnusualContract(tbContract.getGroupId(),
                tbContract.getContractName(), tbContract.getContractBin());
        }

        return tbContract;
    }


    /**
     * save new contract.
     */
    private TbContract newContract(Contract contract) {
        //check contract not exist.
        verifyContractNotExist(contract.getGroupId(), contract.getContractName(),
            contract.getContractPath());

        //add to database.
        TbContract tbContract = new TbContract();
        BeanUtils.copyProperties(contract, tbContract);
        contractMapper.add(tbContract);
        return tbContract;
    }


    /**
     * update contract.
     */
    private TbContract updateContract(Contract contract) {
        //check not deploy
        TbContract tbContract = verifyContractNotDeploy(contract.getContractId(),
            contract.getGroupId());
        //check contractName
        verifyContractNameNotExist(contract.getGroupId(), contract.getContractPath(),
            contract.getContractName(), contract.getContractId());
        BeanUtils.copyProperties(contract, tbContract);
        contractMapper.update(tbContract);
        return tbContract;
    }


    /**
     * delete contract by contractId.
     */
    public void deleteContract(Integer contractId, int groupId) throws NodeMgrException {
        log.debug("start deleteContract contractId:{} groupId:{}", contractId, groupId);
        // check contract id
        verifyContractNotDeploy(contractId, groupId);
        //remove
        contractMapper.remove(contractId);
        log.debug("end deleteContract");
    }

    /**
     * query contract list.
     */
    public List<TbContract> qureyContractList(ContractParam param) throws NodeMgrException {
        log.debug("start qureyContractList ContractListParam:{}", JSON.toJSONString(param));

        // query contract list
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
     * query DeployInputParam By Address.
     */
    public List<TbContract> queryContractByBin(Integer groupId, String contractBin)
        throws NodeMgrException {
        try {
            if (StringUtils.isEmpty(contractBin)) {
                return null;
            }
            List<TbContract> contractRow = contractMapper.queryContractByBin(groupId, contractBin);
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
    public TbContract deployContract(DeployInputParam inputParam) throws NodeMgrException {
        log.info("start deployContract. inputParam:{}", JSON.toJSONString(inputParam));
        int groupId = inputParam.getGroupId();
        String contractName = inputParam.getContractName();
        // String version = Instant.now().toEpochMilli() + "";
        //check contract
        verifyContractNotDeploy(inputParam.getContractId(), inputParam.getGroupId());
        //check contractName
        verifyContractNameNotExist(inputParam.getGroupId(), inputParam.getContractPath(),
            inputParam.getContractName(), inputParam.getContractId());

        JSONArray abiArray = JSONArray.parseArray(inputParam.getContractAbi());
        if (abiArray == null || abiArray.isEmpty()) {
            log.info("fail deployContract. abi is empty");
            throw new NodeMgrException(ConstantCode.CONTRACT_ABI_EMPTY);
        }

        // deploy param
        String signUserId = userService.getSignUserIdByAddress(groupId, inputParam.getUser());
        Map<String, Object> params = new HashMap<>();
        params.put("groupId", groupId);
        params.put("signUserId", signUserId);
        params.put("contractName", contractName);
        // params.put("version", version);
        params.put("abiInfo", JSONArray.parseArray(inputParam.getContractAbi()));
        params.put("bytecodeBin", inputParam.getBytecodeBin());
        params.put("funcParam", inputParam.getConstructorParams());

        //deploy
        String contractAddress = frontRestTools.postForEntity(groupId,
            FrontRestTools.URI_CONTRACT_DEPLOY_WITH_SIGN, params, String.class);
        if (StringUtils.isBlank(contractAddress)) {
            log.error("fail deploy, contractAddress is empty");
            throw new NodeMgrException(ConstantCode.CONTRACT_DEPLOY_FAIL);
        }

        //save contract
        TbContract tbContract = new TbContract();
        BeanUtils.copyProperties(inputParam, tbContract);
        tbContract.setContractAddress(contractAddress);
        tbContract.setContractStatus(ContractStatus.DEPLOYED.getValue());
        //tbContract.setContractVersion(version);
        tbContract.setDeployTime(LocalDateTime.now());
        contractMapper.update(tbContract);

        log.debug("end deployContract. contractId:{} groupId:{} contractAddress:{}",
            tbContract.getContractId(), groupId, contractAddress);
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
    public Object sendTransaction(TransactionInputParam param) throws NodeMgrException {
        log.debug("start sendTransaction. param:{}", JSON.toJSONString(param));

        if (Objects.isNull(param)) {
            log.info("fail sendTransaction. request param is null");
            throw new NodeMgrException(ConstantCode.INVALID_PARAM_INFO);
        }

        //check contractId
        TbContract contract = verifyContractIdExist(param.getContractId(), param.getGroupId());
        //send abi to front
        sendAbi(param.getGroupId(), param.getContractId(), param.getContractAddress());
        //check contract deploy
        verifyContractDeploy(param.getContractId(), param.getGroupId());

        // if constant, signUserId is useless
        AbiDefinition funcAbi = Web3Tools.getAbiDefinition(param.getFuncName(), contract.getContractAbi());
        String signUserId = "empty";
        if (!funcAbi.isConstant()) {
            signUserId = userService.getSignUserIdByAddress(param.getGroupId(), param.getUser());
        }

        //send transaction
        TransactionParam transParam = new TransactionParam();
        BeanUtils.copyProperties(param, transParam);
        transParam.setSignUserId(signUserId);
        Object frontRsp = frontRestTools
            .postForEntity(param.getGroupId(), FrontRestTools.URI_SEND_TRANSACTION_WITH_SIGN, transParam,
                Object.class);
        log.debug("end sendTransaction. frontRsp:{}", JSON.toJSONString(frontRsp));
        return frontRsp;
    }


    /**
     * verify that the contract does not exist.
     */
    private void verifyContractNotExist(int groupId, String name, String path) {
        ContractParam param = new ContractParam(groupId, path, name);
        TbContract contract = queryContract(param);
        if (Objects.nonNull(contract)) {
            log.warn("contract is exist. groupId:{} name:{} path:{}", groupId, name, path);
            throw new NodeMgrException(ConstantCode.CONTRACT_EXISTS);
        }
    }

    /**
     * verify that the contract had not deployed.
     */
    private TbContract verifyContractNotDeploy(int contractId, int groupId) {
        TbContract contract = verifyContractIdExist(contractId, groupId);
        if (ContractStatus.DEPLOYED.getValue() == contract.getContractStatus()) {
            log.info("contract had bean deployed contractId:{}", contractId);
            throw new NodeMgrException(ConstantCode.CONTRACT_HAS_BEAN_DEPLOYED);
        }
        return contract;
    }

    /**
     * verify that the contract had bean deployed.
     */
    private TbContract verifyContractDeploy(int contractId, int groupId) {
        TbContract contract = verifyContractIdExist(contractId, groupId);
        if (ContractStatus.DEPLOYED.getValue() != contract.getContractStatus()) {
            log.info("contract had bean deployed contractId:{}", contractId);
            throw new NodeMgrException(ConstantCode.CONTRACT_NOT_DEPLOY);
        }
        return contract;
    }

    /**
     * verify that the contractId is exist.
     */
    private TbContract verifyContractIdExist(int contractId, int groupId) {
        ContractParam param = new ContractParam(contractId, groupId);
        TbContract contract = queryContract(param);
        if (Objects.isNull(contract)) {
            log.info("contractId is invalid. contractId:{}", contractId);
            throw new NodeMgrException(ConstantCode.INVALID_CONTRACT_ID);
        }
        return contract;
    }

    /**
     * contract name can not be repeated.
     */
    private void verifyContractNameNotExist(int groupId, String path, String name, int contractId) {
        ContractParam param = new ContractParam(groupId, path, name);
        TbContract localContract = queryContract(param);
        if (Objects.isNull(localContract)) {
            return;
        }
        if (contractId != localContract.getContractId()) {
            throw new NodeMgrException(ConstantCode.CONTRACT_NAME_REPEAT);
        }
    }


    /**
     * delete by groupId
     */
    public void deleteByGroupId(int groupId) {
        if (groupId == 0) {
            return;
        }
        contractMapper.removeByGroupId(groupId);
    }


    /**
     * send abi.
     */
    public void sendAbi(int groupId, int contractId, String address) {
        log.info("start sendAbi, groupId:{} contractId:{} address:{}", groupId, contractId,
            address);
        TbContract contract = verifyContractIdExist(contractId, groupId);
        String localAddress = contract.getContractAddress();
        String abiInfo = contract.getContractAbi();
        if (StringUtils.isBlank(address)) {
            log.warn("ignore sendAbi. inputAddress is empty");
            return;
        }
        if (StringUtils.isBlank(abiInfo)) {
            log.warn("ignore sendAbi. abiInfo is empty");
            return;
        }
        if (address.equals(localAddress)) {
            log.info("ignore sendAbi. inputAddress:{} localAddress:{}", address, localAddress);
            return;
        }
        if (address.length() != CONTRACT_ADDRESS_LENGTH) {
            log.warn("fail sendAbi. inputAddress:{}", address);
            throw new NodeMgrException(ConstantCode.CONTRACT_ADDRESS_INVALID);
        }
        //send abi
        PostAbiInfo param = new PostAbiInfo();
        param.setGroupId(groupId);
        param.setContractName(contract.getContractName());
        param.setAddress(address);
        param.setAbiInfo(JSONArray.parseArray(abiInfo, AbiDefinition.class));
        param.setContractBin(contract.getContractBin());

        frontInterface.sendAbi(groupId, param);

        //save address
        if (StringUtils.isBlank(contract.getContractAddress())) {
            contract.setContractAddress(address);
            contract.setContractStatus(ContractStatus.DEPLOYED.getValue());
        }

        contract.setDeployTime(LocalDateTime.now());
        contract.setDescription("address add by sendAbi");
        contractMapper.update(contract);
    }
}

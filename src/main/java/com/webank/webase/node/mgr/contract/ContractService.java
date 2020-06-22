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
package com.webank.webase.node.mgr.contract;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.web3j.precompile.permission.PermissionInfo;
import org.fisco.bcos.web3j.protocol.core.methods.response.AbiDefinition;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.webank.webase.node.mgr.abi.AbiService;
import com.webank.webase.node.mgr.abi.entity.AbiInfo;
import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.entity.BasePageResponse;
import com.webank.webase.node.mgr.base.enums.ContractStatus;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.base.tools.JsonTools;
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
import com.webank.webase.node.mgr.precompiled.permission.PermissionManageService;
import com.webank.webase.node.mgr.user.UserService;

import lombok.extern.log4j.Log4j2;

/**
 * services for contract data.
 */
@Log4j2
@Service
public class  ContractService {

    private static final int CONTRACT_ADDRESS_LENGTH = 42;
    private static final String PERMISSION_TYPE_DEPLOY_AND_CREATE = "deployAndCreate";

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
    @Autowired
    private AbiService abiService;
    @Autowired
    private PermissionManageService permissionManageService;

    /**
     * add new contract data.
     */
    public TbContract saveContract(Contract contract) throws NodeMgrException {
        log.debug("start addContractInfo Contract:{}", JsonTools.toJSONString(contract));
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
        log.debug("start qureyContractList ContractListParam:{}", JsonTools.toJSONString(param));

        // query contract list
        List<TbContract> listOfContract = contractMapper.listOfContract(param);

        log.debug("end qureyContractList listOfContract:{}", JsonTools.toJSONString(listOfContract));
        return listOfContract;
    }


    /**
     * query count of contract.
     */
    public int countOfContract(ContractParam param) throws NodeMgrException {
        log.debug("start countOfContract ContractListParam:{}", JsonTools.toJSONString(param));
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
                JsonTools.toJSONString(contractRow));
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
            log.debug("start queryContractByBin:{}", contractBin, JsonTools.toJSONString(contractRow));
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
        log.info("start deployContract. inputParam:{}", JsonTools.toJSONString(inputParam));
        int groupId = inputParam.getGroupId();

        // check deploy permission
        checkDeployPermission(groupId, inputParam.getUser());

        String contractName = inputParam.getContractName();
        //check contract
        verifyContractNotDeploy(inputParam.getContractId(), inputParam.getGroupId());
        //check contractName
        verifyContractNameNotExist(inputParam.getGroupId(), inputParam.getContractPath(),
            inputParam.getContractName(), inputParam.getContractId());

        List<AbiDefinition> abiArray = JsonTools.toJavaObjectList(inputParam.getContractAbi(), AbiDefinition.class);
        if (abiArray == null || abiArray.isEmpty()) {
            log.info("fail deployContract. abi is empty");
            throw new NodeMgrException(ConstantCode.CONTRACT_ABI_EMPTY);
        }

        // deploy param
        // get signUserId
        String signUserId = userService.getSignUserIdByAddress(groupId, inputParam.getUser());

        Map<String, Object> params = new HashMap<>();
        params.put("groupId", groupId);
        params.put("signUserId", signUserId);
        params.put("contractName", contractName);
        // params.put("version", version);
        params.put("abiInfo", abiArray);
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
        log.debug("start queryContract. queryParam:{}", JsonTools.toJSONString(queryParam));
        TbContract tbContract = contractMapper.queryContract(queryParam);
        log.debug("end queryContract. queryParam:{} tbContract:{}", JsonTools.toJSONString(queryParam),
            JsonTools.toJSONString(tbContract));
        return tbContract;
    }


    /**
     * send transaction.
     */
    public Object sendTransaction(TransactionInputParam param) throws NodeMgrException {
        log.debug("start sendTransaction. param:{}", JsonTools.toJSONString(param));

        if (Objects.isNull(param)) {
            log.info("fail sendTransaction. request param is null");
            throw new NodeMgrException(ConstantCode.INVALID_PARAM_INFO);
        }

        // check contractId
        String contractAbiStr = "";
        if (param.getContractId() != null) {
            // get abi by contract
            TbContract contract = verifyContractIdExist(param.getContractId(), param.getGroupId());
            contractAbiStr = contract.getContractAbi();
            //send abi to front
            sendAbi(param.getGroupId(), param.getContractId(), param.getContractAddress());
            //check contract deploy
            verifyContractDeploy(param.getContractId(), param.getGroupId());
        } else {
            // send tx by abi
            // get from db and it's deployed
            AbiInfo abiInfo = abiService.getAbiByGroupIdAndAddress(param.getGroupId(), param.getContractAddress());
            contractAbiStr = abiInfo.getContractAbi();
        }

        // if constant, signUserId is useless
        AbiDefinition funcAbi = Web3Tools.getAbiDefinition(param.getFuncName(), contractAbiStr);
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
        log.debug("end sendTransaction. frontRsp:{}", JsonTools.toJSONString(frontRsp));
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
        param.setAbiInfo(JsonTools.toJavaObjectList(abiInfo, AbiDefinition.class));
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

    /**
     * check user deploy permission
     */
    private void checkDeployPermission(int groupId, String userAddress) {
        // get deploy permission list
        List<PermissionInfo> deployUserList = new ArrayList<>();
        BasePageResponse response = permissionManageService.listPermissionFull(groupId, PERMISSION_TYPE_DEPLOY_AND_CREATE, null);
        if (response.getCode() != 0) {
            log.error("checkDeployPermission get permission list error");
            return;
        } else {
            List listData = (List) response.getData();
            deployUserList = JsonTools.toJavaObjectList(JsonTools.toJSONString(listData), PermissionInfo.class);
        }

        // check user in the list
        if (deployUserList.isEmpty()) {
            return;
        } else {
            long count = 0;
            count = deployUserList.stream().filter( admin -> userAddress.equals(admin.getAddress())).count();
            // if not in the list, permission denied
            if (count == 0) {
                log.error("checkDeployPermission permission denied for user:{}", userAddress);
                throw new NodeMgrException(ConstantCode.PERMISSION_DENIED);
            }
        }
    }

}

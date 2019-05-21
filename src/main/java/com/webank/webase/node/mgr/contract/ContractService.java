/*
 * Copyright 2014-2019  the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webank.webase.node.mgr.contract;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.webank.webase.node.mgr.base.entity.BaseResponse;
import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.enums.ContractStatus;
import com.webank.webase.node.mgr.base.enums.ContractType;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.base.properties.ConstantProperties;
import com.webank.webase.node.mgr.base.tools.NodeMgrTools;
import com.webank.webase.node.mgr.contract.entity.Contract;
import com.webank.webase.node.mgr.contract.entity.ContractParam;
import com.webank.webase.node.mgr.contract.entity.RspSystemProxy;
import com.webank.webase.node.mgr.contract.entity.TbContract;
import com.webank.webase.node.mgr.contract.entity.Transaction;
import com.webank.webase.node.mgr.front.FrontService;
import com.webank.webase.node.mgr.front.TransactionParam;
import com.webank.webase.node.mgr.monitor.MonitorService;
import com.webank.webase.node.mgr.network.NetworkService;
import com.webank.webase.node.mgr.user.UserService;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
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
    private NetworkService networkService;
    @Autowired
    private ContractMapper contractMapper;
    @Autowired
    private FrontService frontService;
    @Autowired
    private UserService userService;
    @Autowired
    private MonitorService monitorService;
    @Autowired
    private ConstantProperties cp;

    /**
     * add new contract data.
     */
    public Integer addContractInfo(Contract contract) throws NodeMgrException {
        log.debug("start addContractInfo Contract:{}", JSON.toJSONString(contract));
        Integer networkId = Optional.ofNullable(contract).map(c -> c.getNetworkId())
            .orElseThrow(() -> new NodeMgrException(ConstantCode.INVALID_PARAM_INFO));

        // check network id
        networkService.checkNetworkId(networkId);

        // check netowrkid銆乧ontractName銆乧ontractVersion
        ContractParam param = new ContractParam(contract.getContractName(),
            contract.getContractVersion());
        Integer contractCount = countOfContract(param);
        if (contractCount != null && contractCount > 0) {
            log.info("contract info already exists");
            throw new NodeMgrException(ConstantCode.CONTRACT_EXISTS);
        }

        // add row
        TbContract tbContract = new TbContract();
        tbContract.setNetworkId(contract.getNetworkId());
        tbContract.setContractName(contract.getContractName());
        tbContract.setContractVersion(contract.getContractVersion());
        tbContract.setContractSource(contract.getContractSource());
        tbContract.setContractStatus(ContractStatus.NOTDEPLOYED.getValue());

        Integer affectRow = 0;
        try {
            affectRow = contractMapper.addContractRow(tbContract);
        } catch (RuntimeException ex) {
            log.error("fail addContractInfo", ex);
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }

        if (affectRow == 0) {
            log.warn("affect 0 rows of tb_contract");
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }

        Integer contractId = tbContract.getContractId();
        log.debug("end addContractInfo contractId:{}", contractId);
        return contractId;
    }

    /**
     * delete contract by contractId.
     */
    public void deleteContract(Integer contractId) throws NodeMgrException {
        log.debug("start deleteContract contractId:{} ", contractId);
        // check contract id
        TbContract tbContract = contractMapper.queryByContractId(contractId);
        if (tbContract == null) {
            throw new NodeMgrException(ConstantCode.INVALID_CONTRACT_ID);
        }

        if (ContractStatus.DEPLOYED.getValue() == tbContract.getContractStatus()) {
            throw new NodeMgrException(ConstantCode.DELETE_DEPLOYED_CONTRACT);
        }

        Integer affectRow = 0;
        try {
            affectRow = contractMapper.deleteContract(contractId);
        } catch (RuntimeException ex) {
            log.error("fail deleteContract. contractId:{}", contractId, ex);
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }

        if (affectRow == 0) {
            log.warn("affect 0 rows of tb_contract");
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }

        log.debug("end deleteContract");
    }

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
     * update contract data.
     */
    @Transactional
    public void updateContract(Contract contract) throws NodeMgrException, JsonProcessingException {
        log.debug("start updateContract contract:{}", JSON.toJSONString(contract));

        Integer contractId = Optional.ofNullable(contract).map(c -> c.getContractId())
            .orElseThrow(() -> new NodeMgrException(ConstantCode.CONTRACT_ID_NULL));
        // check contract id
        checkContractId(contractId);

        TbContract tbContract = convertParam(contract);
        Integer affectRow = 0;
        try {
            affectRow = contractMapper.updateContract(tbContract);
        } catch (RuntimeException ex) {
            log.error("fail updateContract. contractId:{} ", contractId, ex);
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }

        if (affectRow == 0) {
            log.warn("affect 0 rows of tb_contract");
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }

        log.debug("end updateContract");
    }

    /**
     * check contract id.
     */
    public void checkContractId(Integer contractId) throws NodeMgrException {
        log.debug("start checkContractId contractId:{}", contractId);

        if (contractId == null) {
            log.error("fail checkContractId contractId is null");
            throw new NodeMgrException(ConstantCode.CONTRACT_ID_NULL);
        }

        TbContract tbContract = queryByContractId(contractId);
        if (tbContract == null) {
            throw new NodeMgrException(ConstantCode.INVALID_CONTRACT_ID);
        }
        log.debug("end checkContractId");
    }

    /**
     * query count of contract.
     */
    public Integer countOfContract(ContractParam param) throws NodeMgrException {
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
     * convert Contract to TbContract.
     */
    private TbContract convertParam(Contract contractFrom) {
        TbContract contractTo = new TbContract();

        if (contractFrom != null) {
            contractTo.setContractId(contractFrom.getContractId());
            contractTo.setNetworkId(contractFrom.getNetworkId());
            contractTo.setContractName(contractFrom.getContractName());
            contractTo.setContractVersion(contractFrom.getContractVersion());
            contractTo.setContractSource(contractFrom.getContractSource());
            contractTo.setContractStatus(contractFrom.getContractStatus());
            contractTo.setContractAbi(contractFrom.getContractAbi());
            contractTo.setContractBin(contractFrom.getContractBin());
            contractTo.setContractAddress(contractFrom.getContractAddress());
            contractTo.setDescription(contractFrom.getDescription());
            contractTo.setContractBin(contractFrom.getContractBin());
            contractTo.setBytecodeBin(contractFrom.getBytecodeBin());

            String deployTimeStr = contractFrom.getDeployTime();
            if (StringUtils.isNotBlank(deployTimeStr)) {
                LocalDateTime deployTime = NodeMgrTools
                    .string2LocalDateTime(deployTimeStr, NodeMgrTools.DEFAULT_DATE_TIME_FORMAT);
                contractTo.setDeployTime(deployTime);
            }
        }
        return contractTo;
    }

    /**
     * query Contract By Address.
     */
    public List<TbContract> queryContractByBin(Integer networkId, String contractBin)
        throws NodeMgrException {
        try {
            List<TbContract> contractRow = contractMapper
                .queryContractByBin(networkId, contractBin);
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
    public TbContract deployContract(Contract contract) throws NodeMgrException {
        log.info("start deployContract. contract:{}", JSON.toJSONString(contract));
        Integer networkId = contract.getNetworkId();
        Integer contractId = contract.getContractId();
        String contractSource = contract.getContractSource();
        String contractBin = contract.getContractBin();
        String contractAbi = contract.getContractAbi();
        String bytecodeBin = contract.getBytecodeBin();
        // cehck param
        if (StringUtils.isAnyBlank(contractSource, contractBin, contractAbi, bytecodeBin)) {
            throw new NodeMgrException(ConstantCode.CONTRACT_HAS_NOT_COMPILE);
        }

        // get systemUserId
        Integer systemUserId = userService.queryIdOfSystemUser(networkId);

        if (contractId == null) {
            log.info("contract had not save. save contract now");
            contractId = addContractInfo(contract);
        }

        // query contract
        TbContract tbc = contractMapper.queryByContractId(contractId);
        if (tbc != null && tbc.getContractStatus() == ContractStatus.DEPLOYED.getValue()) {
            throw new NodeMgrException(ConstantCode.CONTRACT_HAS_BEAN_DEPLOYED);
        }

        // param
        Map<String, Object> params = new HashMap<>();
        params.put("userId", contract.getUserId());
        params.put("contractName", tbc.getContractName());
        params.put("version", tbc.getContractVersion());
        params.put("abiInfo", JSONArray.parseArray(contractAbi));
        params.put("bytecodeBin", bytecodeBin);
        params.put("funcParam", contract.getConstructorParams());

        tbc.setDeployTime(LocalDateTime.now());
        tbc.setContractSource(contractSource);
        tbc.setContractBin(contractBin);
        tbc.setContractAbi(contractAbi);
        tbc.setBytecodeBin(bytecodeBin);

        BaseResponse rsp = frontService
            .postNodeFront(networkId, FrontService.FRONT_CONTRACT_DEPLOY, params);
        if (rsp.getCode() != 0) {
            // deploy fail
            tbc.setContractStatus(ContractStatus.DEPLOYMENTFAILED.getValue());
            tbc.setDescription(rsp.getMessage());
            contractMapper.updateContract(tbc);
            throw new NodeMgrException(rsp.getCode(), rsp.getMessage());
        } else {
            // deploy success
            String address = (String) rsp.getData();
            log.info("deploy contract success. address:{}", address);
            tbc.setContractStatus(ContractStatus.DEPLOYED.getValue());
            tbc.setContractAddress(address);
            contractMapper.updateContract(tbc);

            // update monitor unusual contract's info
            monitorService.updateUnusualContract(networkId, tbc.getContractName(), contractBin);

            if (!cp.getSupportTransaction()) {
                log.info("current config is not support transaction");
                return tbc;
            }

            // query system contract:contractDetail
            TbContract systemContract = querySystemContract(networkId,
                cp.getSysContractContractdetailName());
            if (systemContract == null) {
                log.error("not found contract:contractdetail");
                new NodeMgrException(ConstantCode.NOT_FOUND_CONTRACTDETAIL);
            }
            String systemContractV = systemContract.getContractVersion();

            // contract func param
            List<Object> funcParam = new LinkedList<>();
            funcParam.add(address);
            funcParam.add(
                tbc.getContractName() + ConstantProperties.NAME_SPRIT + tbc.getContractVersion());
            funcParam.add(contractAbi);
            funcParam.add(contractBin);
            funcParam.add(networkId);
            funcParam.add(tbc.getDeployTime());
            funcParam.add(contractSource);

            // http post param
            TransactionParam postParam = new TransactionParam(systemUserId, systemContractV,
                cp.getSysContractContractdetailName(), "insertContract",
                funcParam);

            // request node front
            frontService.sendTransaction(networkId, postParam);
        }
        log.debug("end deployContract. contractId:{} networkId:{} accress:{}", contractId,
            networkId, tbc.getContractAddress());
        return tbc;
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
     * query system contract.
     */
    public TbContract querySystemContract(Integer networkId, String contractName) {
        log.debug("start querySystemContract. networkId:{} contractName:{}", networkId,
            contractName);
        if (networkId == null) {
            log.info("fail querySystemContract. networkId is null");
            new NodeMgrException(ConstantCode.NETWORK_ID_NULL);
        }
        if (StringUtils.isBlank(contractName)) {
            log.info("fail querySystemContract. contractName is null");
            new NodeMgrException(ConstantCode.CONTRACT_NAME_EMPTY);
        }

        // param
        ContractParam queryParam = new ContractParam();
        queryParam.setNetworkId(networkId);
        queryParam.setContractName(contractName);
        queryParam.setContractType(ContractType.SYSTEMCONTRACT.getValue());

        TbContract contractInfo = contractMapper.queryContract(queryParam);
        log.debug("end querySystemContract");
        return contractInfo;
    }

    /**
     * send transaction.
     */
    public BaseResponse sendTransaction(Transaction param) throws NodeMgrException {
        log.debug("start sendTransaction. param:{}", JSON.toJSONString(param));

        if (!cp.getSupportTransaction()) {
            log.error("current config is not support transaction");
            throw new NodeMgrException(ConstantCode.NOT_SUPPORT_TRANS);
        }

        if (param == null) {
            log.info("fail sendTransaction. request param is null");
            new NodeMgrException(ConstantCode.INVALID_PARAM_INFO);
        }
        // query param
        ContractParam queryParam = new ContractParam();
        queryParam.setNetworkId(param.getNetworkId());
        queryParam.setContractName(param.getContractName());
        queryParam.setContractVersion(param.getVersion());

        // query contract row
        TbContract contractRow = queryContract(queryParam);
        if (contractRow == null) {
            log.info("fail sendTransaction. contract had not deploy");
            new NodeMgrException(ConstantCode.INVALID_CONTRACT);
        }

        if (contractRow.getContractStatus() != ContractStatus.DEPLOYED.getValue()) {
            log.info("fail sendTransaction. contract had not deploy");
            new NodeMgrException(ConstantCode.CONTRACT_HAD_NOT_DEPLOY);
        }

        // request send transaction
        BaseResponse frontRsp = frontService
            .postNodeFront(param.getNetworkId(), FrontService.FRONT_SEND_TRANSACTION, param);
        log.debug("end sendTransaction. frontRsp:{}", JSON.toJSONString(frontRsp));
        return frontRsp;
    }

    /**
     * update system contract.
     */
    public void updateSystemContract(Contract contract) throws NodeMgrException {
        log.debug("start updateSystemContract.");
        Integer networkId = contract.getNetworkId();
        List<RspSystemProxy> rspList = frontService.getSystemProxy(networkId, contract.getUserId());

        for (RspSystemProxy rspSystemProxy : rspList) {
            String systemContractBin = contractMapper
                .querySystemContractBin(networkId, rspSystemProxy.getName());
            if (!StringUtils.isBlank(systemContractBin)) {
                break;
            }
            String address = rspSystemProxy.getAddress();
            String contractBin = frontService
                .getCodeFromFront(networkId, address, BigInteger.valueOf(0));
            contractMapper
                .updateSystemContract(networkId, rspSystemProxy.getName(), contractBin, address);
        }
        log.debug("end updateSystemContract.");
    }
}

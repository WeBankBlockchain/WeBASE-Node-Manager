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
package com.webank.webase.node.mgr.monitor;

import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.web3j.protocol.core.methods.response.AbiDefinition;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.entity.BaseResponse;
import com.webank.webase.node.mgr.base.enums.MonitorUserType;
import com.webank.webase.node.mgr.base.enums.TableName;
import com.webank.webase.node.mgr.base.enums.TransType;
import com.webank.webase.node.mgr.base.enums.TransUnusualType;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.base.properties.ConstantProperties;
import com.webank.webase.node.mgr.base.tools.JsonTools;
import com.webank.webase.node.mgr.base.tools.NodeMgrTools;
import com.webank.webase.node.mgr.base.tools.Web3Tools;
import com.webank.webase.node.mgr.contract.ContractService;
import com.webank.webase.node.mgr.contract.entity.ContractParam;
import com.webank.webase.node.mgr.contract.entity.TbContract;
import com.webank.webase.node.mgr.frontinterface.FrontInterfaceService;
import com.webank.webase.node.mgr.method.MethodService;
import com.webank.webase.node.mgr.method.entity.TbMethod;
import com.webank.webase.node.mgr.monitor.entity.ContractMonitorResult;
import com.webank.webase.node.mgr.monitor.entity.UserMonitorResult;
import com.webank.webase.node.mgr.transaction.TransHashService;
import com.webank.webase.node.mgr.transaction.entity.TbTransHash;
import com.webank.webase.node.mgr.user.UserService;

import lombok.extern.log4j.Log4j2;

/**
 * MonitorService.
 */
@Log4j2
@Service
public class MonitorService {

    @Autowired
    private MonitorMapper monitorMapper;
    @Autowired
    @Lazy
    private ContractService contractService;
    @Autowired
    @Lazy
    private UserService userService;
    @Autowired
    private TransHashService transHashService;
    @Autowired
    private FrontInterfaceService frontInterfacee;
    @Autowired
    private MonitorTransactionService monitorTransactionService;
    @Autowired
    private MethodService methodService;
    @Autowired
    private ConstantProperties cProperties;


    /**
     * monitor every group.
     */
    @Async(value = "mgrAsyncExecutor")
    public void transMonitorByGroupId(CountDownLatch latch, int groupId) {
        try {
            Instant startTimem = Instant.now();//start time
            Long useTimeSum = 0L;
            LocalDateTime start = LocalDateTime.now(); //createTime of monitor info
            LocalDateTime createTime = start;
            do {
                List<TbTransHash> transHashList = transHashService
                    .qureyUnStatTransHashList(groupId);
                log.info("=== groupId:{} transHashList:{}", groupId, transHashList.size());
                if (transHashList.size() == 0) {
                    log.debug("transMonitorByGroupId jump over. transHashList is empty");
                    return;
                }

                if (checkUnusualMax(groupId)) {
                    return;
                }

                //monitor
                for (TbTransHash trans : transHashList) {
                    if (createTime.getDayOfYear() != trans.getBlockTimestamp().getDayOfYear()
                        || start == createTime) {
                        log.info(
                            "============== createTime:{} blockTimestamp:{}",
                            createTime,
                            trans.getBlockTimestamp());
                        log.info(
                            "============== createData:{} blockTimestampData:{}",
                            createTime.getDayOfYear(),
                            trans.getBlockTimestamp().getDayOfYear());
                        createTime = trans.getBlockTimestamp();
                    }
                    monitorTransHash(groupId, trans, createTime);
                }

                //monitor useTime
                useTimeSum = Duration.between(startTimem, Instant.now()).getSeconds();
                log.debug("monitor groupId:{} useTimeSum:{}s maxTime:{}s", groupId, useTimeSum,
                    cProperties.getTransMonitorTaskFixedRate());
            } while (useTimeSum < cProperties.getTransMonitorTaskFixedRate());
            log.info("=== end monitor. groupId:{} allUseTime:{}s", groupId, useTimeSum);
        } catch (Exception ex) {
            log.error("fail transMonitorByGroupId, group:{}", groupId, ex);
        } finally {
            if (Objects.nonNull(latch)) {
                // finish one group, count down
                latch.countDown();
            }
        }
    }

    /**
     * check unusualUserCount or unusualContractCount is max.
     */
    private boolean checkUnusualMax(int groupId) {
        int unusualUserCount = this.countOfUnusualUser(groupId, null);
        int unusualContractCount = this.countOfUnusualContract(groupId, null);
        int unusualMaxCount = cProperties.getMonitorUnusualMaxCount();
        if (unusualUserCount >= unusualMaxCount
            || unusualContractCount >= unusualMaxCount) {
            log.error(
                "monitorHandle jump over. unusualUserCount:{} unusualContractCount:{} monitorUnusualMaxCount:{}",
                unusualUserCount, unusualContractCount, unusualMaxCount);
            return true;
        }
        return false;
    }


    public void updateUnusualUser(Integer groupId, String userName, String address) {
        log.info("start updateUnusualUser address:{}", address);
        monitorMapper.updateUnusualUser(TableName.MONITOR.getTableName(groupId), userName, address);
    }

    /**
     * Remove trans monitor info.
     */
    public Integer delete(Integer groupId, Integer monitorInfoRetainMax) {
        String tableName = TableName.MONITOR.getTableName(groupId);
        Integer affectRow = monitorMapper.deleteAndRetainMax(tableName, monitorInfoRetainMax);
        return affectRow;
    }

    /**
     * update unusual contract.
     */
    public void updateUnusualContract(Integer groupId, String contractName, String contractBin)
        throws NodeMgrException {
        try {
            log.info("start updateUnusualContract groupId:{} contractName:{} contractBin:{}",
                groupId, contractName, contractBin);
            String tableName = TableName.MONITOR.getTableName(groupId);
            contractBin = removeBinFirstAndLast(contractBin);
            String subContractBin = subContractBinForName(contractBin);
            String txHash = monitorMapper.queryUnusualTxhash(tableName, subContractBin);
            if (StringUtils.isBlank(txHash)) {
                return;
            }
            ChainTransInfo trans = frontInterfacee.getTransInfoByHash(groupId, txHash);
            if (trans == null) {
                return;
            }
            ContractMonitorResult contractResult = monitorContract(groupId, txHash, trans.getTo(),
                trans.getInput(), trans.getBlockNumber());

            //update monitor into
            monitorMapper.updateUnusualContract(tableName, contractName, subContractBin,
                contractResult.getInterfaceName(), contractResult.getTransUnusualType());
        } catch (Exception ex) {
            log.error("fail updateUnusualContract", ex);
        }
    }


    /**
     * query monitor user list.
     */
    public List<TbMonitor> qureyMonitorUserList(Integer groupId) throws NodeMgrException {

        List<TbMonitor> monitorUserList = monitorMapper
            .monitorUserList(TableName.MONITOR.getTableName(groupId));

        log.debug("end qureyMonitorUserList monitorUserList:{}",
            JsonTools.toJSONString(monitorUserList));
        return monitorUserList;
    }

    /**
     * query monitor interface list.
     */
    public List<TbMonitor> qureyMonitorInterfaceList(Integer groupId, String userName)
        throws NodeMgrException {

        List<TbMonitor> monitorInterfaceList = monitorMapper
            .monitorInterfaceList(TableName.MONITOR.getTableName(groupId), userName);

        log.debug("end qureyMonitorInterfaceList monitorInterfaceList:{}",
            JsonTools.toJSONString(monitorInterfaceList));
        return monitorInterfaceList;
    }

    /**
     * query monitor trans list.
     */
    public BaseResponse qureyMonitorTransList(Integer groupId, String userName, String startDate,
        String endDate, String interfaceName)
        throws NodeMgrException {
        BaseResponse response = new BaseResponse(ConstantCode.SUCCESS);

        //param
        String tableName = TableName.MONITOR.getTableName(groupId);
        List<String> nameList = Arrays
            .asList("tableName", "groupId", "userName", "startDate", "endDate", "interfaceName");
        List<Object> valueList = Arrays
            .asList(tableName, groupId, userName, startDate, endDate, interfaceName);
        Map<String, Object> param = NodeMgrTools.buidMap(nameList, valueList);

        Integer count = monitorMapper.countOfMonitorTrans(param);
        List<PageTransInfo> transInfoList = monitorMapper.qureyTransCountList(param);

        MonitorTrans monitorTrans = new MonitorTrans(groupId, userName, interfaceName, count,
            transInfoList);
        response.setData(monitorTrans);
        return response;
    }

    /**
     * query count of unusual user.
     */
    public Integer countOfUnusualUser(Integer groupId, String userName) {
        return monitorMapper.countOfUnusualUser(TableName.MONITOR.getTableName(groupId), userName);
    }

    /**
     * query unusual user list.
     */
    public List<UnusualUserInfo> qureyUnusualUserList(Integer groupId, String userName,
        Integer pageNumber, Integer pageSize)
        throws NodeMgrException {
        log.debug("start qureyUnusualUserList groupId:{} userName:{} pageNumber:{} pageSize:{}",
            groupId, userName, pageNumber,
            pageSize);

        Integer start = Optional.ofNullable(pageNumber).map(page -> (page - 1) * pageSize)
            .orElse(null);
        String tableName = TableName.MONITOR.getTableName(groupId);
        List<String> nameList = Arrays
            .asList("tableName", "groupId", "userName", "start", "pageSize");
        List<Object> valueList = Arrays.asList(tableName, groupId, userName, start, pageSize);
        Map<String, Object> param = NodeMgrTools.buidMap(nameList, valueList);

        List<UnusualUserInfo> listOfUnusualUser = monitorMapper.listOfUnusualUser(param);

        log.debug("end qureyUnusualUserList listOfUnusualUser:{}",
            JsonTools.toJSONString(listOfUnusualUser));
        return listOfUnusualUser;
    }

    /**
     * query count of unusual contract.
     */
    public Integer countOfUnusualContract(Integer groupId, String contractAddress) {
        return monitorMapper
            .countOfUnusualContract(TableName.MONITOR.getTableName(groupId), contractAddress);
    }

    /**
     * query unusual contract list.
     */
    public List<UnusualContractInfo> qureyUnusualContractList(Integer groupId,
        String contractAddress, Integer pageNumber, Integer pageSize)
        throws NodeMgrException {
        log.debug(
            "start qureyUnusualContractList groupId:{} userName:{} pageNumber:{} pageSize:{}",
            groupId, contractAddress, pageNumber, pageSize);

        String tableName = TableName.MONITOR.getTableName(groupId);
        Integer start = Optional.ofNullable(pageNumber).map(page -> (page - 1) * pageSize)
            .orElse(null);

        List<String> nameList = Arrays
            .asList("tableName", "groupId", "contractAddress", "start", "pageSize");
        List<Object> valueList = Arrays
            .asList(tableName, groupId, contractAddress, start, pageSize);
        Map<String, Object> param = NodeMgrTools.buidMap(nameList, valueList);

        List<UnusualContractInfo> listOfUnusualContract = monitorMapper
            .listOfUnusualContract(param);

        log.debug("end qureyUnusualContractList listOfUnusualContract:{}",
            JsonTools.toJSONString(listOfUnusualContract));
        return listOfUnusualContract;
    }

    /**
     * monitor TransHash.
     */
    public void monitorTransHash(int groupId, TbTransHash trans, LocalDateTime createTime) {

        try {
            ChainTransInfo chanTrans = frontInterfacee
                .getTransInfoByHash(groupId, trans.getTransHash());
            if (Objects.isNull(chanTrans)) {
                log.error("monitor jump over,invalid hash. groupId:{} hash:{}", groupId,
                    trans.getTransHash());
                return;
            }

            // monitor user
            UserMonitorResult userResult = monitorUser(groupId, trans.getTransFrom());
            //monitor contract
            ContractMonitorResult contractRes = monitorContract(groupId, trans.getTransHash(),
                chanTrans.getTo(), chanTrans.getInput(), trans.getBlockNumber());

            TbMonitor tbMonitor = new TbMonitor();
            BeanUtils.copyProperties(userResult, tbMonitor);
            BeanUtils.copyProperties(contractRes, tbMonitor);
            tbMonitor.setTransHashs(trans.getTransHash());
            tbMonitor.setTransHashLastest(trans.getTransHash());
            tbMonitor.setTransCount(1);
            tbMonitor.setCreateTime(createTime);
            tbMonitor.setModifyTime(trans.getBlockTimestamp());
            //refresh transaction audit
            monitorTransactionService.dataAddAndUpdate(groupId, tbMonitor);
        } catch (Exception ex) {
            log.error("transaction:{} analysis fail...", trans.getTransHash(), ex);
            return;
        } finally {
            try {
                Thread.sleep(cProperties.getAnalysisSleepTime());
            } catch (InterruptedException e) {
                log.error("thread sleep fail", e);
                Thread.currentThread().interrupt();
            }
        }
    }


    /**
     * monitor contract.
     */
    private ContractMonitorResult monitorContract(int groupId, String transHash, String transTo,
        String transInput, BigInteger blockNumber) {
        String contractAddress, contractName, interfaceName = "", contractBin;
        int transType = TransType.DEPLOY.getValue();
        int transUnusualType = TransUnusualType.NORMAL.getValue();

        if (isDeploy(transTo)) {
            contractAddress = frontInterfacee.getAddressByHash(groupId, transHash);
            if (ConstantProperties.ADDRESS_DEPLOY.equals(contractAddress)) {
                contractBin = StringUtils.removeStart(transInput, "0x");
                
                ContractParam param = new ContractParam();
                param.setGroupId(groupId);
                param.setPartOfBytecodeBin(contractBin);
                TbContract tbContract = contractService.queryContract(param);
                
                if (Objects.nonNull(tbContract)) {
                    contractName = tbContract.getContractName();
                } else {
                    contractName = getNameFromContractBin(groupId, contractBin);
                    transUnusualType = TransUnusualType.CONTRACT.getValue();
                }
            } else {
                contractBin = frontInterfacee.getCodeFromFront(groupId, contractAddress, blockNumber);
                contractBin = removeBinFirstAndLast(contractBin);
                
                List<TbContract> contractRow = contractService.queryContractByBin(groupId, contractBin);
                if (contractRow != null && contractRow.size() > 0) {
                    contractName = contractRow.get(0).getContractName();
                } else {
                    contractName = getNameFromContractBin(groupId, contractBin);
                    transUnusualType = TransUnusualType.CONTRACT.getValue();
                }
            }
            interfaceName = contractName;
        } else {    // function call
            transType = TransType.CALL.getValue();
            String methodId = transInput.substring(0, 10);
            contractAddress = transTo;
            contractBin = frontInterfacee.getCodeFromFront(groupId, contractAddress, blockNumber);
            contractBin = removeBinFirstAndLast(contractBin);

            List<TbContract> contractRow = contractService.queryContractByBin(groupId, contractBin);
            if (contractRow != null && contractRow.size() > 0) {
                contractName = contractRow.get(0).getContractName();
                interfaceName = getInterfaceName(methodId, contractRow.get(0).getContractAbi());
                if (StringUtils.isBlank(interfaceName)) {
                    interfaceName = transInput.substring(0, 10);
                    transUnusualType = TransUnusualType.FUNCTION.getValue();
                }
            } else {
                contractName = getNameFromContractBin(groupId, contractBin);
                TbMethod tbMethod = methodService.getByMethodId(methodId, groupId);
                if (Objects.nonNull(tbMethod)) {
                    interfaceName = getInterfaceName(methodId, "[" + tbMethod.getAbiInfo() + "]");
                    log.info("monitor methodId:{} interfaceName:{}", methodId, interfaceName);
                }
                if (StringUtils.isBlank(interfaceName)) {
                    interfaceName = transInput.substring(0, 10);
                    transUnusualType = TransUnusualType.CONTRACT.getValue();
                }
            }
        }

        transUnusualType =
            cProperties.getIsMonitorIgnoreContract() ? TransUnusualType.NORMAL.getValue()
                : transUnusualType;
        ContractMonitorResult contractResult = new ContractMonitorResult();
        contractResult.setContractName(contractName);
        contractResult.setContractAddress(contractAddress);
        contractResult.setInterfaceName(interfaceName);
        contractResult.setTransType(transType);
        contractResult.setTransUnusualType(transUnusualType);
        return contractResult;
    }


    /**
     * monitor user.
     */
    private UserMonitorResult monitorUser(int groupId, String userAddress) {
        String userName = userService.queryUserNameByAddress(groupId, userAddress);

//        if (StringUtils.isBlank(userName)) {
//            userName = getSystemUserName(userAddress);
//        }

        int userType = MonitorUserType.NORMAL.getValue();
        if (StringUtils.isBlank(userName)) {
            userName = userAddress;
            userType =
                cProperties.getIsMonitorIgnoreUser() ? MonitorUserType.NORMAL.getValue()
                    : MonitorUserType.ABNORMAL.getValue();
        }

        UserMonitorResult monitorResult = new UserMonitorResult();
        monitorResult.setUserName(userName);
        monitorResult.setUserType(userType);
        return monitorResult;
    }


    /**
     * get systemUser name.
     */
//    private String getSystemUserName(String address) {
//        if (StringUtils.isBlank(address)) {
//            return null;
//        }
//        TbUser user = userService.getSystemUser();
//        return Optional.ofNullable(user).filter(u -> address.equals(u.getAddress()))
//            .map(u1 -> u1.getUserName()).orElse(null);
//    }




    /**
     * get interface name.
     */
    private String getInterfaceName(String methodId, String contractAbi) {
        if (StringUtils.isAnyBlank(methodId, contractAbi)) {
            log.warn("fail getInterfaceName. methodId:{} contractAbi:{}", methodId, contractAbi);
            return null;
        }

        String interfaceName = null;
        try {
            List<AbiDefinition> abiList = Web3Tools.loadContractDefinition(contractAbi);
            for (AbiDefinition abiDefinition : abiList) {
                if ("function".equals(abiDefinition.getType())) {
                    // support guomi sm3
                    String buildMethodId = Web3Tools.buildMethodId(abiDefinition);
                    if (methodId.equals(buildMethodId)) {
                        interfaceName = abiDefinition.getName();
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            log.error("fail getInterfaceName", ex);
        }
        return interfaceName;
    }

    /**
     * remove "0x" and last 68 character.
     */
    private String removeBinFirstAndLast(String contractBin) {
        if (StringUtils.isBlank(contractBin)) {
            return null;
        }
        if (contractBin.startsWith("0x")) {
            contractBin = StringUtils.removeStart(contractBin, "0x");
        }
        if (contractBin.length() > 68) {
            contractBin = contractBin.substring(0, contractBin.length() - 68);
        }
        return contractBin;
    }

    /**
     * check the address is deploy.
     */
    private boolean isDeploy(String address) {
        if (StringUtils.isBlank(address)) {
            return false;
        }
        return ConstantProperties.ADDRESS_DEPLOY.equals(address);
    }

    /**
     * get contractName from contractBin.
     */
    private String getNameFromContractBin(int groupId, String contractBin) {
        List<TbContract> contractList = contractService.queryContractByBin(groupId, contractBin);
        if (contractList != null && contractList.size() > 0) {
            return contractList.get(0).getContractName();
        }
        return subContractBinForName(contractBin);
    }

    /**
     * substring contractBin for contractName.
     */
    private String subContractBinForName(String contractBin) {
        String contractName = ConstantProperties.CONTRACT_NAME_ZERO;
        if (StringUtils.isNotBlank(contractBin) && contractBin.length() > 10) {
            contractName = contractBin.substring(contractBin.length() - 10);
        }
        return contractName;
    }
}

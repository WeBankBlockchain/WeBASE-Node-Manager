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
package com.webank.webase.node.mgr.monitor;

import com.alibaba.fastjson.JSON;
import com.webank.webase.node.mgr.base.entity.BaseResponse;
import com.webank.webase.node.mgr.base.entity.ConstantCode;
import com.webank.webase.node.mgr.base.enums.MonitorUserType;
import com.webank.webase.node.mgr.base.enums.TableName;
import com.webank.webase.node.mgr.base.enums.TransType;
import com.webank.webase.node.mgr.base.enums.TransUnusualType;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.base.properties.ConstantProperties;
import com.webank.webase.node.mgr.base.tools.NodeMgrTools;
import com.webank.webase.node.mgr.base.tools.Web3Tools;
import com.webank.webase.node.mgr.contract.ContractService;
import com.webank.webase.node.mgr.contract.entity.TbContract;
import com.webank.webase.node.mgr.frontinterface.FrontInterfaceService;
import com.webank.webase.node.mgr.transaction.TransHashService;
import com.webank.webase.node.mgr.transaction.entity.TbTransHash;
import com.webank.webase.node.mgr.user.UserService;
import com.webank.webase.node.mgr.user.entity.TbUser;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.bcos.web3j.protocol.core.methods.response.AbiDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * MonitorService.
 */
@Log4j2
@Service
public class MonitorService {

    @Autowired
    MonitorService monitorService;
    @Autowired
    private MonitorMapper monitorMapper;
    @Autowired
    private ContractService contractService;
    @Autowired
    private UserService userService;
    @Autowired
    private TransHashService transHashService;
    @Autowired
    private FrontInterfaceService frontInterfacee;

    public void addRow(int groupId, TbMonitor tbMonitor) {
        monitorMapper.add(TableName.MONITOR.getTableName(groupId), tbMonitor);
    }

    public void updateRow(int groupId, TbMonitor tbMonitor) {
        monitorMapper.update(TableName.MONITOR.getTableName(groupId), tbMonitor);
    }

    public void updateUnusualUser(Integer groupId, String userName, String address) {
        log.info("start updateUnusualUser address:{}", address);
        monitorMapper.updateUnusualUser(TableName.MONITOR.getTableName(groupId), userName, address);
    }

    /**
     * update unusual contract.
     */
    public void updateUnusualContract(Integer groupId, String contractName, String contractBin)
        throws NodeMgrException {
        try {
            log.info("start updateUnusualContract groupId:{} contractName:{} contractBin:{}",
                groupId, contractName, contractBin);
            String txHash = monitorMapper
                .queryUnusualTxhash(TableName.MONITOR.getTableName(groupId),
                    contractBin.substring(contractBin.length() - 10));
            if (StringUtils.isBlank(txHash)) {
                return;
            }
            ChainTransInfo chainTransInfo = frontInterfacee
                .getTransInfoByHash(groupId, txHash);
            if (chainTransInfo == null) {
                return;
            }
            String interfaceName = "";
            int transUnusualType = 0;
            // contract deploy
            if (StringUtils.isBlank(chainTransInfo.getTo())) {
                List<TbContract> contractRow = contractService
                    .queryContractByBin(groupId, contractBin.substring(2));
                if (contractRow != null && contractRow.size() > 0) {
                    interfaceName = contractRow.get(0).getContractName();
                } else {
                    interfaceName = chainTransInfo.getInput().substring(0, 10);
                    transUnusualType = 1;
                }
            } else {    // function call
                String methodId = chainTransInfo.getInput().substring(0, 10);
                List<TbContract> contractRow = contractService
                    .queryContractByBin(groupId, contractBin.substring(2));
                if (contractRow != null && contractRow.size() > 0) {
                    List<AbiDefinition> abiList = Web3Tools
                        .loadContractDefinition(contractRow.get(0).getContractAbi());
                    for (AbiDefinition abiDefinition : abiList) {
                        if ("function".equals(abiDefinition.getType())) {
                            String buildMethodId = Web3Tools.buildMethodId(abiDefinition);
                            if (methodId.equals(buildMethodId)) {
                                interfaceName = abiDefinition.getName();
                                break;
                            }
                        }
                    }
                    if (StringUtils.isBlank(interfaceName)) {
                        interfaceName = chainTransInfo.getInput().substring(0, 10);
                        transUnusualType = 2;
                    }
                } else {
                    interfaceName = chainTransInfo.getInput().substring(0, 10);
                    transUnusualType = 1;
                }
            }
            monitorMapper
                .updateUnusualContract(TableName.MONITOR.getTableName(groupId), contractName,
                    contractBin.substring(contractBin.length() - 10), interfaceName,
                    transUnusualType);
        } catch (Exception ex) {
            log.error("fail updateUnusualContract", ex);
            throw new NodeMgrException(ConstantCode.SYSTEM_EXCEPTION);
        }
    }

    /**
     * query monitor info.
     */
    public TbMonitor queryTbMonitor(int groupId, TbMonitor tbMonitor) {
        return monitorMapper.queryTbMonitor(TableName.MONITOR.getTableName(groupId), tbMonitor);
    }

    /**
     * query monitor user list.
     */
    public List<TbMonitor> qureyMonitorUserList(Integer groupId) throws NodeMgrException {

        List<TbMonitor> monitorUserList = monitorMapper
            .monitorUserList(TableName.MONITOR.getTableName(groupId));

        log.debug("end qureyMonitorUserList monitorUserList:{}",
            JSON.toJSONString(monitorUserList));
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
            JSON.toJSONString(monitorInterfaceList));
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
            JSON.toJSONString(listOfUnusualUser));
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
            JSON.toJSONString(listOfUnusualContract));
        return listOfUnusualContract;
    }

    /**
     * add trans monitor info.
     */
    public void insertTransMonitorInfo(int groupId, List<TbTransHash> transList) {
        Instant startTime = Instant.now();
        log.info("start insertTransMonitorInfo startTime:{}", startTime.toEpochMilli());

        LocalDateTime createTime = transList.get(0).getBlockTimestamp();
        // query untreated TxHash
        for (TbTransHash trans : transList) {
            try {
                if (createTime.getDayOfYear() != trans.getBlockTimestamp().getDayOfYear()) {
                    createTime = trans.getBlockTimestamp();
                }
                String contractName = "";
                String interfaceName = "";
                String contractAddress = "";

                int userType = MonitorUserType.NORMAL.getValue();
                int transType = TransType.DEPLOY.getValue();
                int transUnusualType = TransUnusualType.NORMAL.getValue();

                ChainTransInfo chainTransInfo = frontInterfacee
                    .getTransInfoByHash(groupId, trans.getTransHash());
                if (chainTransInfo == null) {
                    continue;
                }

                String userName = userService
                    .queryUserNameByAddress(groupId, chainTransInfo.getFrom());
                if (StringUtils.isBlank(userName)) {
                    //system user
                    userName = getSystemUserName(chainTransInfo.getFrom());
                }
                if (StringUtils.isBlank(userName)) {
                    userName = chainTransInfo.getFrom();
                    userType = MonitorUserType.ABNORMAL.getValue();
                }

                String contractBin = "";
                // contract deploy
                if (StringUtils.isBlank(chainTransInfo.getTo())) {
                    contractAddress = frontInterfacee
                        .getAddressByHash(groupId, trans.getTransHash());
                    contractBin = frontInterfacee.getCodeFromFront(groupId, contractAddress,
                        trans.getBlockNumber());
                    if (contractBin.startsWith("0x")) {
                        contractBin = StringUtils.removeStart(contractBin, "0x");
                    }
                    List<TbContract> contractRow = contractService
                        .queryContractByBin(groupId, contractBin);
                    if (contractRow != null && contractRow.size() > 0) {
                        contractName = contractRow.get(0).getContractName();
                        interfaceName = contractRow.get(0).getContractName();
                    } else {
                        if (contractBin.length() < 10) {
                            contractName = ConstantProperties.CONTRACT_NAME_ZERO;
                        } else {
                            contractName = contractBin.substring(contractBin.length() - 10);
                        }
                        interfaceName = chainTransInfo.getInput().substring(0, 10);
                        transUnusualType = TransUnusualType.CONTRACT.getValue();
                    }
                } else {    // function call
                    String methodId = chainTransInfo.getInput().substring(0, 10);
                    contractAddress = chainTransInfo.getTo();
                    contractBin = frontInterfacee
                        .getCodeFromFront(groupId, contractAddress, trans.getBlockNumber());
                    if (contractBin.startsWith("0x")) {
                        contractBin = StringUtils.removeStart(contractBin, "0x");
                    }
                    transType = TransType.CALL.getValue();

                    List<TbContract> contractRow = contractService
                        .queryContractByBin(groupId, contractBin);
                    if (contractRow != null && contractRow.size() > 0) {
                        contractName = contractRow.get(0).getContractName();
                        List<AbiDefinition> abiList = Web3Tools
                            .loadContractDefinition(contractRow.get(0).getContractAbi());
                        for (AbiDefinition abiDefinition : abiList) {
                            if ("function".equals(abiDefinition.getType())) {
                                String buildMethodId = Web3Tools.buildMethodId(abiDefinition);
                                if (methodId.equals(buildMethodId)) {
                                    interfaceName = abiDefinition.getName();
                                    break;
                                }
                            }
                        }
                        if (StringUtils.isBlank(interfaceName)) {
                            interfaceName = chainTransInfo.getInput().substring(0, 10);
                            transUnusualType = TransUnusualType.FUNCTION.getValue();
                        }
                    } else {
                        if (contractBin.length() < 10) {
                            contractName = ConstantProperties.CONTRACT_NAME_ZERO;
                        } else {
                            contractName = contractBin.substring(contractBin.length() - 10);
                        }
                        interfaceName = chainTransInfo.getInput().substring(0, 10);
                        transUnusualType = TransUnusualType.CONTRACT.getValue();
                    }
                }

                TbMonitor tbMonitor = new TbMonitor();
                tbMonitor.setUserName(userName);
                tbMonitor.setUserType(userType);
                tbMonitor.setContractName(contractName);
                tbMonitor.setContractAddress(contractAddress);
                tbMonitor.setInterfaceName(interfaceName);
                tbMonitor.setTransType(transType);
                tbMonitor.setTransUnusualType(transUnusualType);
                tbMonitor.setTransHashs(trans.getTransHash());
                tbMonitor.setTransHashLastest(trans.getTransHash());
                tbMonitor.setTransCount(1);
                tbMonitor.setCreateTime(createTime);
                tbMonitor.setModifyTime(trans.getBlockTimestamp());

                monitorService.dataAddAndUpdate(groupId, tbMonitor);
            } catch (Exception ex) {
                log.error("transaction:{} analysis fail...", trans.getTransHash(),
                    ex);
            }
        }
        log.info("end insertTransMonitorInfo useTime:{}",
            Duration.between(startTime, Instant.now()).toMillis());
    }

    /**
     * get systemUser name.
     */
    private String getSystemUserName(String address) {
        if (StringUtils.isBlank(address)) {
            return null;
        }
        TbUser user = userService.getSystemUser();
        return Optional.ofNullable(user).filter(u -> address.equals(u.getAddress()))
            .map(u1 -> u1.getUserName()).orElse(null);
    }

    /**
     * insert and update.
     */
    @Transactional
    public void dataAddAndUpdate(int groupId, TbMonitor tbMonitor) {
        TbMonitor dbInfo = monitorService.queryTbMonitor(groupId, tbMonitor);
        if (dbInfo == null) {
            monitorService.addRow(groupId, tbMonitor);
        } else {
            String[] txHashsArr = dbInfo.getTransHashs().split(",");
            if (txHashsArr.length < 5) {
                StringBuilder sb = new StringBuilder(dbInfo.getTransHashs()).append(",")
                    .append(tbMonitor.getTransHashLastest());
                tbMonitor.setTransHashs(sb.toString());
            } else {
                tbMonitor.setTransHashs(dbInfo.getTransHashs());
            }
            monitorService.updateRow(groupId, tbMonitor);
        }

        transHashService.updateTransStatFlag(groupId, tbMonitor.getTransHashLastest());
    }
}

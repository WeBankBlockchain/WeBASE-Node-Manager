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
package com.webank.webase.node.mgr.monitor;

import com.alibaba.fastjson.JSON;
import com.webank.webase.node.mgr.base.entity.BaseResponse;
import com.webank.webase.node.mgr.base.entity.ConstantCode;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.base.properties.ConstantProperties;
import com.webank.webase.node.mgr.base.tools.Web3Tools;
import com.webank.webase.node.mgr.contract.ContractService;
import com.webank.webase.node.mgr.contract.TbContract;
import com.webank.webase.node.mgr.front.FrontService;
import com.webank.webase.node.mgr.transhash.TbTransHash;
import com.webank.webase.node.mgr.transhash.TransHashService;
import com.webank.webase.node.mgr.user.UserService;
import com.webank.webase.node.mgr.web3.Web3Service;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.web3j.protocol.core.methods.response.AbiDefinition;
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
    private FrontService frontService;
    @Autowired
    private ContractService contractService;
    @Autowired
    private UserService userService;
    @Autowired
    private TransHashService transHashService;
    @Autowired
    private Web3Service web3Service;

    public void addRow(TbMonitor tbMonitor) {
        monitorMapper.addRow(tbMonitor);
    }

    public void updateRow(TbMonitor tbMonitor) {
        monitorMapper.updateRow(tbMonitor);
    }

    public void updateUnusualUser(Integer groupId, String userName, String address) {
        log.info("start updateUnusualUser address:{}", address);
        monitorMapper.updateUnusualUser(groupId, userName, address);
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
                .queryUnusualTxhash(groupId, contractBin.substring(contractBin.length() - 10));
            if (StringUtils.isBlank(txHash)) {
                return;
            }
            ChainTransInfo chainTransInfo = web3Service.getTransInfoFromFrontByHash(groupId, txHash);
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
            monitorMapper.updateUnusualContract(groupId, contractName,
                contractBin.substring(contractBin.length() - 10), interfaceName, transUnusualType);
        } catch (Exception ex) {
            log.error("fail updateUnusualContract", ex);
            throw new NodeMgrException(ConstantCode.SYSTEM_EXCEPTION);
        }
    }

    /**
     * query monitor info.
     */
    public TbMonitor queryTbMonitor(TbMonitor tbMonitor) {
        return monitorMapper.queryTbMonitor(tbMonitor);
    }

    /**
     * query monitor user list.
     */
    public List<TbMonitor> qureyMonitorUserList(Integer groupId) throws NodeMgrException {

        List<TbMonitor> monitorUserList = monitorMapper.monitorUserList(groupId);

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
            .monitorInterfaceList(groupId, userName);

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

        Integer count = countOfMonitorTrans(groupId, userName, startDate, endDate, interfaceName);
        List<PageTransInfo> transInfoList = qureyTransCountList(groupId, userName, startDate,
            endDate, interfaceName);

        MonitorTrans monitorTrans = new MonitorTrans(groupId, userName, interfaceName, count,
            transInfoList);
        response.setData(monitorTrans);
        return response;
    }

    /**
     * query count of monitor trans.
     */
    public Integer countOfMonitorTrans(Integer groupId, String userName, String startDate,
        String endDate, String interfaceName) throws NodeMgrException {

        Map<String, Object> queryParam = new HashMap<>();
        queryParam.put("groupId", groupId);
        queryParam.put("userName", userName);
        queryParam.put("startDate", startDate);
        queryParam.put("endDate", endDate);
        queryParam.put("interfaceName", interfaceName);

        Integer count = monitorMapper.countOfMonitorTrans(queryParam);
        log.debug("end countOfMonitorTrans groupId:{} userName:{} count:{}", groupId, userName,
            count);
        return count;
    }

    /**
     * query trans count list.
     */
    public List<PageTransInfo> qureyTransCountList(Integer groupId, String userName,
        String startDate, String endDate, String interfaceName) throws NodeMgrException {

        Map<String, Object> queryParam = new HashMap<>();
        queryParam.put("groupId", groupId);
        queryParam.put("userName", userName);
        queryParam.put("startDate", startDate);
        queryParam.put("endDate", endDate);
        queryParam.put("interfaceName", interfaceName);

        List<PageTransInfo> list = monitorMapper.qureyTransCountList(queryParam);
        log.debug("end qureyMonitorTransList groupId:{} userName:{} list:{}", groupId, userName,
            list.toString());
        return list;
    }

    /**
     * query count of unusual user.
     */
    public Integer countOfUnusualUser(Integer groupId, String userName) {
        return monitorMapper.countOfUnusualUser(groupId, userName);
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
        Map<String, Object> queryParam = new HashMap<>();
        queryParam.put("groupId", groupId);
        queryParam.put("userName", userName);
        queryParam.put("start", start);
        queryParam.put("pageSize", pageSize);

        List<UnusualUserInfo> listOfUnusualUser = monitorMapper.listOfUnusualUser(queryParam);

        log.debug("end qureyUnusualUserList listOfUnusualUser:{}",
            JSON.toJSONString(listOfUnusualUser));
        return listOfUnusualUser;
    }

    /**
     * query count of unusual contract.
     */
    public Integer countOfUnusualContract(Integer groupId, String contractAddress) {
        return monitorMapper.countOfUnusualContract(groupId, contractAddress);
    }

    /**
     * query unusual contract list.
     */
    public List<UnusualContractInfo> qureyUnusualContractList(Integer groupId,
        String contractAddress, Integer pageNumber, Integer pageSize)
        throws NodeMgrException {
        log.debug(
            "start qureyUnusualContractList groupId:{} userName:{} pageNumber:{} pageSize:{}",
            groupId, contractAddress, pageNumber,
            pageSize);

        Integer start = Optional.ofNullable(pageNumber).map(page -> (page - 1) * pageSize)
            .orElse(null);
        Map<String, Object> queryParam = new HashMap<>();
        queryParam.put("groupId", groupId);
        queryParam.put("contractAddress", contractAddress);
        queryParam.put("start", start);
        queryParam.put("pageSize", pageSize);

        List<UnusualContractInfo> listOfUnusualContract = monitorMapper
            .listOfUnusualContract(queryParam);

        log.debug("end qureyUnusualContractList listOfUnusualContract:{}",
            JSON.toJSONString(listOfUnusualContract));
        return listOfUnusualContract;
    }

    /**
     * add trans monitor info.
     */
    public void insertTransMonitorInfo(List<TbTransHash> transHashList) {
        Instant startTime = Instant.now();
        log.info("start insertTransMonitorInfo startTime:{}", startTime.toEpochMilli());

        LocalDateTime createTime = transHashList.get(0).getBlockTimestamp();
        // query untreated TxHash
        for (int i = 0; i < transHashList.size(); i++) {
            try {
                if (createTime.getDayOfYear() != transHashList.get(i).getBlockTimestamp()
                    .getDayOfYear()) {
                    createTime = transHashList.get(i).getBlockTimestamp();
                }
                // userType(0:normal, 1:abnormal)
                int userType = 0;
                String contractName = "";
                String interfaceName = "";
                String contractAddress = "";
                // transType(0:contract deploy, 1:function call)
                int transType = 0;
                // transUnusualType(0:normal, 1:abnormal contract, 2:abnormal function)
                int transUnusualType = 0;

                ChainTransInfo chainTransInfo = web3Service
                    .getTransInfoFromFrontByHash(transHashList.get(i).getGroupId(),
                        transHashList.get(i).getTransHash());
                if (chainTransInfo == null) {
                    continue;
                }

                String userName = userService
                    .queryUserNameByAddress(transHashList.get(i).getGroupId(),
                        chainTransInfo.getFrom());
                if (StringUtils.isBlank(userName)) {
                    userName = chainTransInfo.getFrom();
                    userType = 1;
                }

                String contractBin = "";
                // contract deploy
                if (StringUtils.isBlank(chainTransInfo.getTo())) {
                    contractAddress = web3Service
                        .getAddressFromFrontByHash(transHashList.get(i).getGroupId(),
                            transHashList.get(i).getTransHash());
                    contractBin = web3Service
                        .getCodeFromFront(transHashList.get(i).getGroupId(), contractAddress,
                            transHashList.get(i).getBlockNumber());
                    List<TbContract> contractRow = contractService
                        .queryContractByBin(transHashList.get(i).getGroupId(), contractBin);
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
                        transUnusualType = 1;
                    }
                } else {    // function call
                    String methodId = chainTransInfo.getInput().substring(0, 10);
                    contractAddress = chainTransInfo.getTo();
                    contractBin = web3Service
                        .getCodeFromFront(transHashList.get(i).getGroupId(), contractAddress,
                            transHashList.get(i).getBlockNumber());
                    transType = 1;

                    List<TbContract> contractRow = contractService
                        .queryContractByBin(transHashList.get(i).getGroupId(), contractBin);
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
                            transUnusualType = 2;
                        }
                    } else {
                        if (contractBin.length() < 10) {
                            contractName = ConstantProperties.CONTRACT_NAME_ZERO;
                        } else {
                            contractName = contractBin.substring(contractBin.length() - 10);
                        }
                        interfaceName = chainTransInfo.getInput().substring(0, 10);
                        transUnusualType = 1;
                    }
                }

                TbMonitor tbMonitor = new TbMonitor();
                tbMonitor.setUserName(userName);
                tbMonitor.setUserType(userType);
                tbMonitor.setGroupId(transHashList.get(i).getGroupId());
                tbMonitor.setContractName(contractName);
                tbMonitor.setContractAddress(contractAddress);
                tbMonitor.setInterfaceName(interfaceName);
                tbMonitor.setTransType(transType);
                tbMonitor.setTransUnusualType(transUnusualType);
                tbMonitor.setTransHashs(transHashList.get(i).getTransHash());
                tbMonitor.setTransHashLastest(transHashList.get(i).getTransHash());
                tbMonitor.setTransCount(1);
                tbMonitor.setCreateTime(createTime);
                tbMonitor.setModifyTime(transHashList.get(i).getBlockTimestamp());

                monitorService.dataAddAndUpdate(tbMonitor);
            } catch (Exception ex) {
                log.error("transhash:{} analysis fail...", transHashList.get(i).getTransHash(), ex);
            }
        }
        log.info("end insertTransMonitorInfo useTime:{}",
            Duration.between(startTime, Instant.now()).toMillis());
    }

    /**
     * insert and update.
     */
    @Transactional
    public void dataAddAndUpdate(TbMonitor tbMonitor) {
        TbMonitor dbInfo = monitorService.queryTbMonitor(tbMonitor);
        if (dbInfo == null) {
            monitorService.addRow(tbMonitor);
        } else {
            String[] txHashsArr = dbInfo.getTransHashs().split(",");
            if (txHashsArr.length < 5) {
                StringBuilder sb = new StringBuilder(dbInfo.getTransHashs()).append(",")
                    .append(tbMonitor.getTransHashLastest());
                tbMonitor.setTransHashs(sb.toString());
            } else {
                tbMonitor.setTransHashs(dbInfo.getTransHashs());
            }
            monitorService.updateRow(tbMonitor);
        }

        transHashService.updateTransStatFlag(tbMonitor.getTransHashLastest());
    }
}

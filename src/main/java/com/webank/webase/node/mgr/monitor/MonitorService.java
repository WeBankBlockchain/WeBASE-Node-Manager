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
import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.enums.MonitorUserType;
import com.webank.webase.node.mgr.base.enums.TransType;
import com.webank.webase.node.mgr.base.enums.TransUnusualType;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.base.properties.ConstantProperties;
import com.webank.webase.node.mgr.base.tools.Web3Tools;
import com.webank.webase.node.mgr.contract.ContractService;
import com.webank.webase.node.mgr.contract.entity.TbContract;
import com.webank.webase.node.mgr.front.FrontService;
import com.webank.webase.node.mgr.monitor.entity.ContractMonitorResult;
import com.webank.webase.node.mgr.monitor.entity.UserMonitorResult;
import com.webank.webase.node.mgr.transhash.TbTransHash;
import com.webank.webase.node.mgr.transhash.TransHashService;
import com.webank.webase.node.mgr.user.TbUser;
import com.webank.webase.node.mgr.user.UserService;
import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.bcos.web3j.protocol.core.methods.response.AbiDefinition;
import org.springframework.beans.BeanUtils;
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
    private ConstantProperties cProperties;

    public void addRow(TbMonitor tbMonitor) {
        monitorMapper.addRow(tbMonitor);
    }

    public void updateRow(TbMonitor tbMonitor) {
        monitorMapper.updateRow(tbMonitor);
    }

    public void updateUnusualUser(Integer networkId, String userName, String address) {
        log.info("start updateUnusualUser address:{}", address);
        monitorMapper.updateUnusualUser(networkId, userName, address);
    }


    /**
     * Remove trans monitor info.
     */
    public Integer deleteAndRetainMax(Integer networkId, Integer monitorInfoRetainMax) {
        Integer affectRow = monitorMapper.deleteAndRetainMax(networkId, monitorInfoRetainMax);
        return affectRow;
    }

    /**
     * update unusual contract.
     */
    public void updateUnusualContract(Integer networkId, String contractName, String contractBin)
        throws NodeMgrException {
        try {
            log.info("start updateUnusualContract networkId:{} contractName:{} contractBin:{}",
                networkId, contractName, contractBin);

            contractBin = removeBinFirstAndLast(contractBin);
            String subContractBin = subContractBinForName(contractBin);
            String txHash = monitorMapper.queryUnusualTxhash(networkId, subContractBin);
            if (StringUtils.isBlank(txHash)) {
                return;
            }

            ChainTransInfo trans = frontService.getTransInfoFromFrontByHash(networkId, txHash);
            if (trans == null) {
                return;
            }
            ContractMonitorResult contractResult = monitorContract(networkId, txHash, trans.getTo(),
                trans.getInput(), trans.getBlockNumber());

            //update monitor into
            monitorMapper.updateUnusualContract(networkId, contractName, subContractBin,
                contractResult.getInterfaceName(), contractResult.getTransUnusualType());
        } catch (Exception ex) {
            log.error("fail updateUnusualContract", ex);
           // throw new NodeMgrException(ConstantCode.SYSTEM_EXCEPTION);
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
    public List<TbMonitor> qureyMonitorUserList(Integer networkId) throws NodeMgrException {

        List<TbMonitor> monitorUserList = monitorMapper.monitorUserList(networkId);

        log.debug("end qureyMonitorUserList monitorUserList:{}",
            JSON.toJSONString(monitorUserList));
        return monitorUserList;
    }

    /**
     * query monitor interface list.
     */
    public List<TbMonitor> qureyMonitorInterfaceList(Integer networkId, String userName)
        throws NodeMgrException {

        List<TbMonitor> monitorInterfaceList = monitorMapper
            .monitorInterfaceList(networkId, userName);

        log.debug("end qureyMonitorInterfaceList monitorInterfaceList:{}",
            JSON.toJSONString(monitorInterfaceList));
        return monitorInterfaceList;
    }

    /**
     * query monitor trans list.
     */
    public BaseResponse qureyMonitorTransList(Integer networkId, String userName, String startDate,
        String endDate, String interfaceName)
        throws NodeMgrException {
        BaseResponse response = new BaseResponse(ConstantCode.SUCCESS);

        Integer count = countOfMonitorTrans(networkId, userName, startDate, endDate, interfaceName);
        List<PageTransInfo> transInfoList = qureyTransCountList(networkId, userName, startDate,
            endDate, interfaceName);

        MonitorTrans monitorTrans = new MonitorTrans(networkId, userName, interfaceName, count,
            transInfoList);
        response.setData(monitorTrans);
        return response;
    }

    /**
     * query count of monitor trans.
     */
    public Integer countOfMonitorTrans(Integer networkId, String userName, String startDate,
        String endDate, String interfaceName) throws NodeMgrException {

        Map<String, Object> queryParam = new HashMap<>();
        queryParam.put("networkId", networkId);
        queryParam.put("userName", userName);
        queryParam.put("startDate", startDate);
        queryParam.put("endDate", endDate);
        queryParam.put("interfaceName", interfaceName);

        Integer count = monitorMapper.countOfMonitorTrans(queryParam);
        log.debug("end countOfMonitorTrans networkId:{} userName:{} count:{}", networkId, userName,
            count);
        return count;
    }

    /**
     * query trans count list.
     */
    public List<PageTransInfo> qureyTransCountList(Integer networkId, String userName,
        String startDate, String endDate, String interfaceName) throws NodeMgrException {

        Map<String, Object> queryParam = new HashMap<>();
        queryParam.put("networkId", networkId);
        queryParam.put("userName", userName);
        queryParam.put("startDate", startDate);
        queryParam.put("endDate", endDate);
        queryParam.put("interfaceName", interfaceName);

        List<PageTransInfo> list = monitorMapper.qureyTransCountList(queryParam);
        log.debug("end qureyMonitorTransList networkId:{} userName:{} list:{}", networkId, userName,
            list.toString());
        return list;
    }

    /**
     * query count of unusual user.
     */
    public Integer countOfUnusualUser(Integer networkId, String userName) {
        return monitorMapper.countOfUnusualUser(networkId, userName);
    }

    /**
     * query unusual user list.
     */
    public List<UnusualUserInfo> qureyUnusualUserList(Integer networkId, String userName,
        Integer pageNumber, Integer pageSize)
        throws NodeMgrException {
        log.debug("start qureyUnusualUserList networkId:{} userName:{} pageNumber:{} pageSize:{}",
            networkId, userName, pageNumber,
            pageSize);

        Integer start = Optional.ofNullable(pageNumber).map(page -> (page - 1) * pageSize)
            .orElse(null);
        Map<String, Object> queryParam = new HashMap<>();
        queryParam.put("networkId", networkId);
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
    public Integer countOfUnusualContract(Integer networkId, String contractAddress) {
        return monitorMapper.countOfUnusualContract(networkId, contractAddress);
    }

    /**
     * query unusual contract list.
     */
    public List<UnusualContractInfo> qureyUnusualContractList(Integer networkId,
        String contractAddress, Integer pageNumber, Integer pageSize)
        throws NodeMgrException {
        log.debug(
            "start qureyUnusualContractList networkId:{} userName:{} pageNumber:{} pageSize:{}",
            networkId, contractAddress, pageNumber,
            pageSize);

        Integer start = Optional.ofNullable(pageNumber).map(page -> (page - 1) * pageSize)
            .orElse(null);
        Map<String, Object> queryParam = new HashMap<>();
        queryParam.put("networkId", networkId);
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
    public void insertTransMonitorInfo(List<TbTransHash> transList) {
        Instant startTime = Instant.now();
        log.info("start insertTransMonitorInfo startTime:{}", startTime.toEpochMilli());

        LocalDateTime createTime = transList.get(0).getBlockTimestamp();
        // query untreated TxHash
        for (TbTransHash trans : transList) {
            try {
                if (createTime.getDayOfYear() != trans.getBlockTimestamp().getDayOfYear()) {
                    createTime = trans.getBlockTimestamp();
                }

                ChainTransInfo chanTrans = frontService
                    .getTransInfoFromFrontByHash(trans.getNetworkId(), trans.getTransHash());
                if (chanTrans == null) {
                    continue;
                }

                int networkId = trans.getNetworkId();
                UserMonitorResult userResult = monitorUser(networkId, chanTrans.getFrom());
                //monitor contract
                ContractMonitorResult contractRes = monitorContract(networkId, trans.getTransHash(),
                    chanTrans.getTo(), chanTrans.getInput(), trans.getBlockNumber());

                TbMonitor tbMonitor = new TbMonitor();
                BeanUtils.copyProperties(userResult, tbMonitor);
                BeanUtils.copyProperties(contractRes, tbMonitor);
                tbMonitor.setNetworkId(networkId);
                tbMonitor.setTransHashs(trans.getTransHash());
                tbMonitor.setTransHashLastest(trans.getTransHash());
                tbMonitor.setTransCount(1);
                tbMonitor.setCreateTime(createTime);
                tbMonitor.setModifyTime(trans.getBlockTimestamp());

                monitorService.dataAddAndUpdate(tbMonitor);
            } catch (Exception ex) {
                log.error("transhash:{} analysis fail...", trans.getTransHash(), ex);
            }
        }
        log.info("end insertTransMonitorInfo useTime:{}",
            Duration.between(startTime, Instant.now()).toMillis());
    }


    /**
     * monitor user.
     */
    private UserMonitorResult monitorUser(int networkId, String userAddress) {
        String userName = userService.queryUserNameByAddress(networkId, userAddress);

        if (StringUtils.isBlank(userName)) {
            userName = getSystemUserName(networkId, userAddress);
        }

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
     * monitor contract.
     */
    private ContractMonitorResult monitorContract(int networkId, String transHash, String transTo,
        String transInput, BigInteger blockNumber) {
        String contractAddress, contractName, interfaceName, contractBin;
        int transType = TransType.DEPLOY.getValue();
        int transUnusualType = TransUnusualType.NORMAL.getValue();

        if (StringUtils.isBlank(transTo)) {
            contractAddress = frontService.getAddressFromFrontByHash(networkId, transHash);
            contractBin = frontService.getCodeFromFront(networkId, contractAddress, blockNumber);
            contractBin = removeBinFirstAndLast(contractBin);
            contractName = getNameFromContractBin(networkId, contractBin);
            interfaceName = transInput.substring(0, 10);
        } else {    // function call
            transType = TransType.CALL.getValue();
            String methodId = transInput.substring(0, 10);
            contractAddress = transTo;
            contractBin = frontService.getCodeFromFront(networkId, contractAddress, blockNumber);
            contractBin = removeBinFirstAndLast(contractBin);

            List<TbContract> contractRow = contractService
                .queryContractByBin(networkId, contractBin);
            if (contractRow != null && contractRow.size() > 0) {
                contractName = contractRow.get(0).getContractName();
                interfaceName = getInterfaceName(methodId, contractRow.get(0).getContractAbi());
                if (StringUtils.isBlank(interfaceName)) {
                    interfaceName = transInput.substring(0, 10);
                    transUnusualType = TransUnusualType.FUNCTION.getValue();
                }
            } else {
                contractName = getNameFromContractBin(networkId, contractBin);
                interfaceName = transInput.substring(0, 10);
                transUnusualType = TransUnusualType.CONTRACT.getValue();
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
     * get systemUser name.
     */
    private String getSystemUserName(int networkId, String address) {
        if (StringUtils.isBlank(address)) {
            return null;
        }
        return userService.queryUserNameByAddress(networkId, address);
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

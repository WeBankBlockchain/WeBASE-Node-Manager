/**
 * Copyright 2014-2021 the original author or authors.
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

package com.webank.webase.node.mgr.external;

import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.config.properties.ConstantProperties;
import com.webank.webase.node.mgr.tools.NodeMgrTools;
import com.webank.webase.node.mgr.contract.ContractService;
import com.webank.webase.node.mgr.contract.abi.AbiService;
import com.webank.webase.node.mgr.contract.abi.entity.AbiInfo;
import com.webank.webase.node.mgr.contract.entity.ContractParam;
import com.webank.webase.node.mgr.contract.entity.TbContract;
import com.webank.webase.node.mgr.external.entity.RspAllExtContract;
import com.webank.webase.node.mgr.external.entity.TbExternalContract;
import com.webank.webase.node.mgr.external.mapper.TbExternalContractMapper;
import com.webank.webase.node.mgr.front.frontinterface.FrontInterfaceService;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.sdk.v3.model.TransactionReceipt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Service
public class ExtContractService {

    @Autowired
    private TbExternalContractMapper extContractMapper;
    @Autowired
    private ContractService contractService;
    @Autowired
    private AbiService abiService;
    @Autowired
    private FrontInterfaceService frontInterfaceService;

    /**
     *
     * @param groupId
     * @param txHash
     * @return
     */
    @Async(value = "mgrAsyncExecutor")
    @Transactional
    public void asyncSaveContract(String groupId, String txHash, String timestampStr) {
        log.debug("start asyncSaveContract groupId:{}, txHash:{}, timestampStr:{}",
            groupId, txHash, timestampStr);

        TransactionReceipt txReceipt = frontInterfaceService.getTransReceipt(groupId, txHash);
        // if send transaction to call contract, receipt's contract address is all zero,
        // receipt's to is contract address
        // todo 3.0 is null
        String contractAddress;
        if (txReceipt.getTo() == null) {
            contractAddress = "";
        } else {
            contractAddress = txReceipt.getTo();
        }

        // if receipt's to is all zero, deploy transaction
        if (StringUtils.isNotBlank(txReceipt.getContractAddress())) {
            log.debug("deploy contract tx :{}", txReceipt.getContractAddress());
            contractAddress = txReceipt.getContractAddress();
        }
        // ignore precompiled contract address
        if (contractAddress.startsWith(ConstantProperties.ADDRESS_PRECOMPILED)
        || contractAddress.startsWith(ConstantProperties.ADDRESS_PRECOMPILED_NO_PREFIX)
        || StringUtils.isBlank(contractAddress)) {
            log.debug("ignore precompiled contract:{}", contractAddress);
            return;
        }
        // save ext contract
        saveContractOnChain(groupId, contractAddress, txHash,
            txReceipt.getFrom(), timestampStr);
    }

    /**
     * save contract on chain
     */
    @Transactional
    public int saveContractOnChain(String groupId, String contractAddress, String txHash,
        String deployAddress, String timestamp) {
        log.debug("saveContractOnChain groupId:{} contractAddress:{}", groupId, contractAddress);
        if (checkAddressExist(groupId, contractAddress)) {
            log.info("checkAddressExist groupId:{} contractAddress:{}", groupId, contractAddress);
            return 0;
        }
        TbExternalContract tbContract = new TbExternalContract();
        // check tb_contract's address
        ContractParam queryParam = new ContractParam();
        queryParam.setGroupId(groupId);
        queryParam.setContractAddress(contractAddress);
        TbContract existedContract = contractService.queryContract(queryParam);
        if (Objects.nonNull(existedContract)) {
            log.debug("saveContractOnChain exist tb_contract "
                + "contractAddress:{} address:{}", groupId, contractAddress);
            // set related contract name
            tbContract.setContractName(existedContract.getContractName());
            tbContract.setContractAbi(existedContract.getContractAbi());
        }
        // check tb_abi's address
        AbiInfo existedTbAbi = abiService.getAbi(groupId, contractAddress);
        if (Objects.nonNull(existedTbAbi)) {
            log.debug("saveContractOnChain exist tb_contract "
                + "contractAddress:{} address:{}", groupId, contractAddress);
            // set related contract name
            tbContract.setContractName(existedTbAbi.getContractName());
            tbContract.setContractAbi(existedTbAbi.getContractAbi());
        }
        tbContract.setGroupId(groupId);
        tbContract.setContractAddress(contractAddress);
        tbContract.setDeployTxHash(txHash);
        tbContract.setDeployTime(NodeMgrTools.timestamp2Date(Long.parseLong(timestamp)));
        // todo 链上的回执没返回from
        tbContract.setDeployAddress(StringUtils.isBlank(deployAddress) ? "" : deployAddress);
        Date now = new Date();
        tbContract.setCreateTime(now);
        tbContract.setModifyTime(now);
        int insertRes = extContractMapper.insertSelective(tbContract);
        log.info("saveContractOnChain groupId:{} contractAddress:{},deployAddress:{} insertRes:{}",
            groupId, contractAddress, deployAddress, insertRes);
        return insertRes;
    }

    private boolean checkAddressExist(String groupId, String contractAddress) {
        int count = extContractMapper.countOfExtContract(groupId, contractAddress);
        if (count > 0) {
            log.info("saveContractOnChain exists tb_external_contract"
                + " groupId:{} address:{}", groupId, contractAddress);
            return true;
        }
        return false;
    }

    public List<TbExternalContract> listExtContract(ContractParam param) {
        log.debug("listExtContract param:{}", param);
        return extContractMapper.listExtContract(param);
    }

    public int countExtContract(ContractParam param) {
        log.debug("countExtContract param:{}", param);
        return extContractMapper.countExtContract(param);
    }
    public int updateContractInfo(int contractId, String contractName, String abi, String description) {
        TbExternalContract update = extContractMapper.selectByPrimaryKey(contractId);
        if (update == null) {
            log.error("updateContractInfo id not exist!");
            throw new NodeMgrException(ConstantCode.INVALID_CONTRACT_ID);
        }
        update.setContractName(contractName);
        update.setContractAbi(abi);
        update.setDescription(description);
        return extContractMapper.updateByPrimaryKeySelective(update);
    }

    public void deleteByGroupId(String groupId) {
        int affected = extContractMapper.deleteByGroupId(groupId);
        log.warn("deleteByGroupId:{} affected:{}", groupId, affected);
    }

    public List<RspAllExtContract> getAllExtContractLeftJoinAbi(ContractParam param, boolean requiredBin) {
        log.info("getAllExtContractLeftJoinAbi param:{}", param);
        List<RspAllExtContract> contractList = extContractMapper.listContractJoinTbAbi(param);

        if (requiredBin) {
            contractList.forEach(c -> c.setContractBin(""));
        }
        return contractList;
    }

    public TbExternalContract getByAddress(String groupId, String contractAddress) {
        log.debug("getByAddress groupId:{}, contractAddress:{}", groupId, contractAddress);
        TbExternalContract externalContract = extContractMapper.getByGroupIdAndAddress(groupId, contractAddress);
        log.debug("getByAddress externalContract:{}", externalContract);
        return externalContract;
    }
}

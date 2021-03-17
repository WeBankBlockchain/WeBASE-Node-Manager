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
import com.webank.webase.node.mgr.base.tools.NodeMgrTools;
import com.webank.webase.node.mgr.contract.ContractService;
import com.webank.webase.node.mgr.contract.entity.ContractParam;
import com.webank.webase.node.mgr.contract.entity.TbContract;
import com.webank.webase.node.mgr.external.entity.RspAllExtContract;
import com.webank.webase.node.mgr.external.entity.TbExternalContract;
import com.webank.webase.node.mgr.external.mapper.TbExternalContractMapper;
import com.webank.webase.node.mgr.frontinterface.FrontInterfaceService;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import lombok.extern.log4j.Log4j2;
import org.fisco.bcos.sdk.abi.datatypes.Address;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

@Log4j2
@Service
public class ExtContractService {

    @Autowired
    private TbExternalContractMapper extContractMapper;
    @Autowired
    private ContractService contractService;
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
    public void asyncSaveContract(int groupId, String txHash, String timestampStr) {
        log.debug("start asyncSaveContract groupId:{}, txHash:{}, timestampStr:{}",
            groupId, txHash, timestampStr);

        TransactionReceipt txReceipt = frontInterfaceService.getTransReceipt(groupId, txHash);
        if (!Address.DEFAULT.getValue().equals(txReceipt.getTo())) {
            return;
        }
        // save
        saveContractOnChain(groupId, txReceipt.getContractAddress(), txHash,
            txReceipt.getFrom(), timestampStr);
    }

    /**
     * save contract on chain
     */
    @Transactional
    public int saveContractOnChain(int groupId, String contractAddress, String txHash,
        String deployAddress, String timestamp) {
        //log.info("saveContractOnChain groupId:{} contractAddress:{}", groupId, contractAddress);
        if (checkAddressExist(groupId, contractAddress)) {
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
        tbContract.setGroupId(groupId);
        tbContract.setContractAddress(contractAddress);
        tbContract.setDeployTxHash(txHash);
        tbContract.setDeployTime(NodeMgrTools.timestamp2Date(Long.parseLong(timestamp)));
        tbContract.setDeployAddress(deployAddress);
        Date now = new Date();
        tbContract.setCreateTime(now);
        tbContract.setModifyTime(now);
        int insertRes = extContractMapper.insertSelective(tbContract);
        log.info("saveContractOnChain groupId:{} contractAddress:{}, insertRes:{}",
            groupId, contractAddress, insertRes);
        return insertRes;
    }

    private boolean checkAddressExist(int groupId, String contractAddress) {
        int count = extContractMapper.countOfExtContract(groupId, contractAddress);
        if (count > 0) {
            log.debug("saveContractOnChain exists tb_external_contract" 
                + " groupId:{} address:{}", groupId, contractAddress);
            return true;
        }
        return false;
    }

    public List<TbExternalContract> listExtContract(ContractParam param) {
        return extContractMapper.listExtContract(param);
    }

    public int countExtContract(ContractParam param) {
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

    public void deleteByGroupId(int groupId) {
        int affected = extContractMapper.deleteByGroupId(groupId);
        log.warn("deleteByGroupId:{} affected:{}", groupId, affected);
    }

    public List<RspAllExtContract> getAllExtContractLeftJoinAbi(ContractParam param) {
        log.info("getAllExtContractLeftJoinAbi param:{}", param);
        return extContractMapper.listContractJoinTbAbi(param);
    }
}

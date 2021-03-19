/**
 * Copyright 2014-2021 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.webase.node.mgr.contract.abi;

import com.webank.webase.node.mgr.base.tools.JsonTools;
import com.webank.webase.node.mgr.contract.abi.entity.AbiInfo;
import com.webank.webase.node.mgr.contract.abi.entity.ReqAbiListParam;
import com.webank.webase.node.mgr.contract.abi.entity.ReqImportAbi;
import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.base.tools.NodeMgrTools;
import com.webank.webase.node.mgr.contract.ContractService;
import com.webank.webase.node.mgr.contract.abi.entity.RspAllContract;
import com.webank.webase.node.mgr.contract.entity.RspContractNoAbi;
import com.webank.webase.node.mgr.contract.entity.TbContract;
import com.webank.webase.node.mgr.frontinterface.FrontInterfaceService;
import java.util.ArrayList;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Log4j2
@Service
public class AbiService {

	@Autowired
	AbiMapper abiMapper;
	@Autowired
	FrontInterfaceService frontInterfaceService;
	@Autowired
	ContractService contractService;

	public List<AbiInfo> getListByGroupId(ReqAbiListParam param) {
		List<AbiInfo> abiList = abiMapper.listOfAbi(param);
		return abiList;
	}

	public void saveAbi(ReqImportAbi param) {
		if (Objects.isNull(param.getAbiId())) {
			insertAbiInfo(param);
		} else {
			updateAbiInfo(param);
		}
	}

	@Transactional
	public void insertAbiInfo(ReqImportAbi param) {
		int groupId = param.getGroupId();
		String account = param.getAccount();
		String contractName = param.getContractName();
		String contractAddress = param.getContractAddress();
		String contractAbiStr;
		try {
			contractAbiStr = JsonTools.toJSONString(param.getContractAbi());
		} catch (Exception e) {
			log.warn("abi parse string error:{}", param.getContractAbi());
			throw new NodeMgrException(ConstantCode.PARAM_FAIL_ABI_INVALID);
		}
		// check address
		String contractBin = getAddressRuntimeBin(groupId, contractAddress);
		// check name and address of abi not exist
		checkAbiExist(groupId, account, contractAddress);

		AbiInfo saveAbi = new AbiInfo();
		BeanUtils.copyProperties(param, saveAbi);
		saveAbi.setContractAbi(contractAbiStr);
		saveAbi.setContractBin(contractBin);
		LocalDateTime now = LocalDateTime.now();
		saveAbi.setCreateTime(now);
		saveAbi.setModifyTime(now);
		abiMapper.add(saveAbi);
	}

	@Transactional
	public void updateAbiInfo(ReqImportAbi param) {
		Integer abiId = param.getAbiId();
		// check id exists
		checkAbiIdExist(abiId);
		// update
		AbiInfo updateAbi = new AbiInfo();
		BeanUtils.copyProperties(param, updateAbi);
		String contractAbiStr;
		try {
			contractAbiStr = JsonTools.toJSONString(param.getContractAbi());
		} catch (Exception e) {
			log.warn("abi parse string error:{}", param.getContractAbi());
			throw new NodeMgrException(ConstantCode.PARAM_FAIL_ABI_INVALID);
		}
		// check address
		String contractBin = getAddressRuntimeBin(param.getGroupId(), param.getContractAddress());
		updateAbi.setContractAbi(contractAbiStr);
		updateAbi.setContractBin(contractBin);
		updateAbi.setModifyTime(LocalDateTime.now());
		abiMapper.update(updateAbi);
	}

	public void delete(Integer id) {
		checkAbiIdExist(id);
		abiMapper.deleteByAbiId(id);
	}

	private void checkAbiExist(int groupId, String account, String address) {
		AbiInfo checkAbiAddressExist = abiMapper.queryByGroupIdAndAddress(groupId, account, address);
		if (Objects.nonNull(checkAbiAddressExist)) {
			throw new NodeMgrException(ConstantCode.CONTRACT_ADDRESS_ALREADY_EXISTS);
		}
	}

	public AbiInfo getAbiById(Integer abiId) {
		return abiMapper.queryByAbiId(abiId);
	}

	private void checkAbiIdExist(Integer abiId) {
		AbiInfo checkAbiId = getAbiById(abiId);
		if (Objects.isNull(checkAbiId)) {
			throw new NodeMgrException(ConstantCode.ABI_INFO_NOT_EXISTS);
		}
	}

	public AbiInfo getAbiByGroupIdAndAddress(Integer groupId, String contractAddress) {
		AbiInfo abiInfo = abiMapper.queryByGroupIdAndAddress(groupId, null, contractAddress);
		if (Objects.isNull(abiInfo)) {
			throw new NodeMgrException(ConstantCode.ABI_INFO_NOT_EXISTS);
		}
		return abiInfo;
	}

	/**
	 * check address is valid.
	 * @return address's runtime bin
	 */
	public String getAddressRuntimeBin(int groupId, String contractAddress) {
		if (StringUtils.isBlank(contractAddress)) {
			log.error("fail getAddressRuntimeBin. contractAddress is empty");
			throw new NodeMgrException(ConstantCode.CONTRACT_ADDRESS_NULL);
		}
		String binOnChain;
		try {
			binOnChain = frontInterfaceService.getCodeFromFront(groupId, contractAddress, BigInteger.ZERO);
		} catch (Exception e) {
			log.error("fail getAddressRuntimeBin.", e);
			throw new NodeMgrException(ConstantCode.CONTRACT_ADDRESS_INVALID);
		}
		log.info("getAddressRuntimeBin address:{} binOnChain:{}", contractAddress, binOnChain);
		String runtimeBin = NodeMgrTools.removeFirstStr(binOnChain, "0x");
		if (StringUtils.isBlank(runtimeBin)) {
			log.error("fail getAddressRuntimeBin. runtimeBin is null, address:{}", contractAddress);
			throw new NodeMgrException(ConstantCode.CONTRACT_NOT_DEPLOY);
		}
		return runtimeBin;
	}

	public int countOfAbi(ReqAbiListParam param) {
		log.debug("start countOfAbi ");
		try {
			int count = abiMapper.countOfAbi(param);
			log.debug("end countOfAbi count:{}", count);
			return count;
		}catch (Exception e) {
			log.error("countOfAbi error exception:[]", e);
			throw new NodeMgrException(ConstantCode.DB_EXCEPTION.getCode(),
					e.getMessage());
		}
	}

	/**
	 * query contract list.
	 */
	public List<RspContractNoAbi> listByGroupIdNoAbi(int groupId) throws NodeMgrException {
		log.debug("start listByGroupIdNoAbi groupId:{}", groupId);
		ReqAbiListParam param = new ReqAbiListParam();
		param.setGroupId(groupId);
		List<RspContractNoAbi> resultList = new ArrayList<>();
		// query contract list
		List<AbiInfo> listOfAbi = abiMapper.listOfAbi(param);
		listOfAbi.forEach(c -> {
			RspContractNoAbi rsp = new RspContractNoAbi();
			BeanUtils.copyProperties(c, rsp);
			resultList.add(rsp);
		});
		log.debug("end listByGroupIdNoAbi resultList:{}", JsonTools.toJSONString(resultList));
		return resultList;
	}

	public void deleteAbiByGroupId(int groupId) {
		log.info("deleteAbiByGroupId groupId:{}", groupId);
		abiMapper.deleteByGroupId(groupId);
	}

	public int countOfAbiByGroupId(int groupId) {
		log.debug("start countOfAbiByGroupId groupId:{}", groupId);
		ReqAbiListParam param = new ReqAbiListParam();
		param.setGroupId(groupId);
		return countOfAbi(param);
	}

	public void saveAbiFromContractId(int contractId, String contractAddress) {
		TbContract tbContract = contractService.queryByContractId(contractId);

		int groupId = tbContract.getGroupId();
		String account = tbContract.getAccount();
		// concat contract name with address
		String contractName = tbContract.getContractName();
		// check name and address of abi not exist
		checkAbiExist(groupId, account, contractAddress);
		String contractBin = tbContract.getContractBin();
		String contractAbiStr = tbContract.getContractAbi();
		log.info("saveAbiFromContractId of re-deploying contractId:{},contractAddress:{}",
			contractId, contractAddress);

		AbiInfo saveAbi = new AbiInfo();
		saveAbi.setGroupId(groupId);
		saveAbi.setContractAddress(contractAddress);
		saveAbi.setContractName(contractName);
		saveAbi.setContractAbi(contractAbiStr);
		saveAbi.setContractBin(contractBin);
		LocalDateTime now = LocalDateTime.now();
		saveAbi.setCreateTime(now);
		saveAbi.setModifyTime(now);
		saveAbi.setAccount(account);
		abiMapper.add(saveAbi);
	}


	/**
	 * select contract from tb_abi(union tb_contract)
	 */
	public List<RspAllContract> listAllContract(ReqAbiListParam param) {
		log.debug("listAllContract param:{}", param);
		return abiMapper.listAllContract(param);
	}

}

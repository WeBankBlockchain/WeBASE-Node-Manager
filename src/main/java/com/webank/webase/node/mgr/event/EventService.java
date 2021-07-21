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

package com.webank.webase.node.mgr.event;

import com.webank.webase.node.mgr.contract.ContractService;
import com.webank.webase.node.mgr.contract.abi.AbiService;
import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.entity.BasePageResponse;
import com.webank.webase.node.mgr.base.enums.ContractStatus;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.contract.entity.ContractParam;
import com.webank.webase.node.mgr.contract.entity.RspContractNoAbi;
import com.webank.webase.node.mgr.event.entity.ContractEventInfo;
import com.webank.webase.node.mgr.event.entity.NewBlockEventInfo;
import com.webank.webase.node.mgr.event.entity.ReqEventLogList;
import com.webank.webase.node.mgr.event.entity.RspContractInfo;
import com.webank.webase.node.mgr.front.FrontService;
import com.webank.webase.node.mgr.front.entity.FrontParam;
import com.webank.webase.node.mgr.front.entity.TbFront;
import com.webank.webase.node.mgr.front.frontinterface.FrontInterfaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class EventService {
	@Autowired
	private FrontInterfaceService frontInterfaceService;
	@Autowired
	private FrontService frontService;
	@Autowired
	private ContractService contractService;
	@Autowired
	private AbiService abiService;
	private static final String TYPE_CONTRACT = "contract";
	private static final String TYPE_ABI_INFO = "abi";

	public List<NewBlockEventInfo> getNewBlockEventInfoList(int groupId) {
		//get all front
		List<TbFront> frontList = frontService.getFrontList(new FrontParam());
		List<NewBlockEventInfo> eventList = new ArrayList<>();
		frontList.forEach(frontInfo -> eventList.addAll(
				frontInterfaceService.getNewBlockEventInfo(
						frontInfo.getFrontIp(), frontInfo.getFrontPort(), groupId)));
		return eventList;
	}


	public List<ContractEventInfo> getContractEventInfoList(int groupId) {
		//get all front
		List<TbFront> frontList = frontService.getFrontList(new FrontParam());
		List<ContractEventInfo> eventList = new ArrayList<>();
		frontList.forEach(frontInfo -> eventList.addAll(
				frontInterfaceService.getContractEventInfo(
						frontInfo.getFrontIp(), frontInfo.getFrontPort(), groupId)));
		return eventList;
	}

	/**
	 * 同步获取event log列表
	 */
	public BasePageResponse getEventLogList(ReqEventLogList param) {
		return frontInterfaceService.getEventLogList(param);
	}

	/**
	 * list contract info from contract & abi
	 */
	public List<RspContractInfo> listContractInfoBoth(int groupId) {
		// find contract list
		ContractParam contractParam = new ContractParam();
		contractParam.setGroupId(groupId);
		contractParam.setContractStatus(ContractStatus.DEPLOYED.getValue());
		List<RspContractNoAbi> contractList = contractService.queryContractListNoAbi(contractParam);
		// find abi list
		List<RspContractNoAbi> abiInfoList = abiService.listByGroupIdNoAbi(groupId);
		// add abi info and contract info in result list
		List<RspContractInfo> resultList = new ArrayList<>();
		contractList.forEach(c -> resultList.add(new RspContractInfo(TYPE_CONTRACT, c.getContractAddress(), c.getContractName())));
		abiInfoList.forEach(c -> resultList.add(new RspContractInfo(TYPE_ABI_INFO, c.getContractAddress(), c.getContractName())));

		return resultList;
	}


	public Object getAbiByAddressFromBoth(int groupId, String type, String contractAddress) {
		if (TYPE_CONTRACT.equals(type)) {
			ContractParam param = new ContractParam();
			param.setGroupId(groupId);
			param.setContractAddress(contractAddress);
			return contractService.queryContract(param);
		} else if (TYPE_ABI_INFO.equals(type)) {
			return abiService.getAbiByGroupIdAndAddress(groupId, contractAddress);
		} else {
			throw new NodeMgrException(ConstantCode.PARAM_EXCEPTION);
		}
	}


}

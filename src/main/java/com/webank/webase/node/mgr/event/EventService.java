/**
 * Copyright 2014-2019 the original author or authors.
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

import com.webank.webase.node.mgr.event.entity.ContractEventInfo;
import com.webank.webase.node.mgr.event.entity.NewBlockEventInfo;
import com.webank.webase.node.mgr.front.FrontService;
import com.webank.webase.node.mgr.front.entity.FrontParam;
import com.webank.webase.node.mgr.front.entity.TbFront;
import com.webank.webase.node.mgr.frontinterface.FrontInterfaceService;
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


}

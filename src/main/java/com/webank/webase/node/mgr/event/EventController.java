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

import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.entity.BasePageResponse;
import com.webank.webase.node.mgr.base.tools.pagetools.List2Page;
import com.webank.webase.node.mgr.event.entity.ContractEventInfo;
import com.webank.webase.node.mgr.event.entity.NewBlockEventInfo;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.Instant;
import java.util.List;


/**
 * event register info controller
 * @author marsli
 */
@Log4j2
@RestController
@RequestMapping("event")
public class EventController {

	@Autowired
	private EventService eventService;

	/**
	 * get new block event register info
	 */
	@GetMapping(value = {"newBlockEvent/list/{groupId}/{pageNumber}/{pageSize}"})
	public BasePageResponse getNewBlockEventInfo(@PathVariable("groupId") Integer groupId,
												 @PathVariable("pageNumber") Integer pageNumber,
												 @PathVariable("pageSize") Integer pageSize) {
		Instant startTime = Instant.now();
		log.debug("start getNewBlockEventInfo. startTime:{},groupId:{}", groupId, startTime.toEpochMilli());
		List<NewBlockEventInfo> resList;
		if (pageNumber < 1) {
			return new BasePageResponse(ConstantCode.INVALID_PARAM_INFO, null, 0);
		}
		resList = eventService.getNewBlockEventInfoList(groupId);
		List2Page list2Page = new List2Page(resList, pageSize, pageNumber);
		log.info("end getNewBlockEventInfo useTime:{} resList:{}",
				Duration.between(startTime, Instant.now()).toMillis(), resList);
		return new BasePageResponse(ConstantCode.SUCCESS, list2Page.getPagedList(), resList.size());
	}

	/**
	 * get contract event register info
	 */
	@GetMapping(value = {"contractEvent/list/{groupId}/{pageNumber}/{pageSize}"})
	public BasePageResponse getContractEventInfo(@PathVariable("groupId") Integer groupId,
												 @PathVariable("pageNumber") Integer pageNumber,
												 @PathVariable("pageSize") Integer pageSize) {
		Instant startTime = Instant.now();
		log.debug("start getContractEventInfo. startTime:{},groupId:{}", groupId, startTime.toEpochMilli());
		List<ContractEventInfo> resList;
		if (pageNumber < 1) {
			return new BasePageResponse(ConstantCode.INVALID_PARAM_INFO, null, 0);
		}
		resList = eventService.getContractEventInfoList(groupId);
		List2Page list2Page = new List2Page(resList, pageSize, pageNumber);
		log.info("end getContractEventInfo useTime:{} resList:{}",
				Duration.between(startTime, Instant.now()).toMillis(), resList);
		return new BasePageResponse(ConstantCode.SUCCESS, list2Page.getPagedList(), resList.size());
	}
}

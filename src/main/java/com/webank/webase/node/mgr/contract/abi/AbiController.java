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

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.qctc.common.log.annotation.Log;
import com.qctc.common.log.enums.BusinessType;
import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.controller.BaseController;
import com.webank.webase.node.mgr.base.entity.BasePageResponse;
import com.webank.webase.node.mgr.base.entity.BaseResponse;
import com.webank.webase.node.mgr.base.enums.SqlSortType;
import com.webank.webase.node.mgr.config.properties.ConstantProperties;
import com.webank.webase.node.mgr.contract.abi.entity.AbiInfo;
import com.webank.webase.node.mgr.contract.abi.entity.ReqAbiListParam;
import com.webank.webase.node.mgr.contract.abi.entity.ReqImportAbi;
import com.webank.webase.node.mgr.contract.abi.entity.RspAllContract;
import com.webank.webase.node.mgr.tools.JsonTools;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import javax.validation.Valid;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * abi import controller
 */
@Tag(name="合约abi接口")
@Log4j2
@RestController
@RequestMapping("abi")
public class AbiController extends BaseController {
	@Autowired
	AbiService abiService;

	@SaCheckPermission("bcos3:contract:List")
	@GetMapping("/list/{groupId}/{pageNumber}/{pageSize}")
	public Object listAbi(
			@PathVariable("groupId") String groupId,
			@PathVariable("pageNumber") Integer pageNumber,
			@PathVariable("pageSize") Integer pageSize,
            @RequestParam(value = "account", required = false) String account) {
		Instant startTime = Instant.now();
		if (pageNumber < 1 || pageSize <= 0) {
			return new BaseResponse(ConstantCode.PARAM_EXCEPTION);
		}
		log.info("start listAbi. startTime:{},groupId:{},pageNumber:{},pageSize:{}",
				startTime.toEpochMilli(), groupId, pageNumber, pageSize);

		Integer start = Optional.ofNullable(pageNumber).map(page -> (page - 1) * pageSize)
				.orElse(0);
		ReqAbiListParam param = new ReqAbiListParam(start, pageSize,
				SqlSortType.DESC.getValue());
		param.setGroupId(groupId);
		param.setAccount(account);
		// total count
		int count = abiService.countOfAbi(param);
		List<AbiInfo> resList = abiService.getListByGroupId(param);

		log.info("end listAbi. useTime:{}, resList:{}",
				Duration.between(startTime, Instant.now()).toMillis(), resList);
		return new BasePageResponse(ConstantCode.SUCCESS, resList, count);
	}

	@SaCheckPermission("bcos3:contract:List")
	@GetMapping("/list/all/{groupId}/{pageNumber}/{pageSize}")
	public BasePageResponse listAllContractIncludeAbi(
			@PathVariable("groupId") String groupId,
			@PathVariable("pageNumber") Integer pageNumber,
			@PathVariable("pageSize") Integer pageSize,
            @RequestParam(value = "account", required = false) String account,
			@RequestParam(value = "contractName", required = false) String contractName,
			@RequestParam(value = "contractAddress", required = false) String contractAddress) {
		Instant startTime = Instant.now();
		if (pageNumber < 1 || pageSize <= 0) {
			return new BasePageResponse(ConstantCode.PARAM_EXCEPTION);
		}
		log.info("start listAllContractIncludeAbi. startTime:{},groupId:{},pageNumber:{},pageSize:{}",
				startTime.toEpochMilli(), groupId, pageNumber, pageSize);

		Integer start = Optional.ofNullable(pageNumber).map(page -> (page - 1) * pageSize)
				.orElse(0);
		ReqAbiListParam param = new ReqAbiListParam(start, pageSize,
				SqlSortType.DESC.getValue());
		param.setGroupId(groupId);
		param.setAccount(account);
		param.setContractAddress(contractAddress);
		param.setContractName(contractName);
		// total count
		int count = abiService.countOfAbi(param);
		List<RspAllContract> resList = abiService.listAllContract(param);

		log.info("end listAllContractIncludeAbi. useTime:{}, resList size:{}",
				Duration.between(startTime, Instant.now()).toMillis(), resList.size());
		return new BasePageResponse(ConstantCode.SUCCESS, resList, count);
	}

	@SaCheckPermission("bcos3:contract:List")
	@GetMapping("/{abiId}")
	public Object getAbiById(@PathVariable("abiId") Integer abiId) {
		Instant startTime = Instant.now();
		log.info("start getAbiById. startTime:{} abiId:{}",
				startTime.toEpochMilli(), abiId);
		AbiInfo res = abiService.getAbiById(abiId);
		log.info("end getAbiById. useTime:{}, res:{}",
				Duration.between(startTime, Instant.now()).toMillis(), res);
		return new BaseResponse(ConstantCode.SUCCESS, res);
	}

	@Log(title = "BCOS3/合约管理/合约列表", businessType = BusinessType.INSERT)
	@SaCheckPermission("bcos3:contract:addAbi")
	@PostMapping("")
	public Object saveAbi(@Valid @RequestBody ReqImportAbi param, BindingResult result) {
		checkBindResult(result);
		Instant startTime = Instant.now();
		log.info("start saveAbi. startTime:{} ReqImportAbi:{}",
				startTime.toEpochMilli(), JsonTools.toJSONString(param));
		abiService.saveAbi(param);
		log.info("end saveAbi. useTime:{}",
				Duration.between(startTime, Instant.now()).toMillis());
		return new BaseResponse(ConstantCode.SUCCESS);
	}

	/**
	 * @param param abiId is not empty to update
	 * @return
	 */
	@Log(title = "BCOS3/合约管理/合约列表", businessType = BusinessType.UPDATE)
	@SaCheckPermission("bcos3:contract:updateAbi")
	@PutMapping("")
	public Object updateAbi(@RequestBody ReqImportAbi param, BindingResult result) {
		checkBindResult(result);
		Instant startTime = Instant.now();
		log.info("start updateAbi. startTime:{} ReqImportAbi:{}",
				startTime.toEpochMilli(), JsonTools.toJSONString(param));
		if(param.getAbiId() == null) {
			return new BaseResponse(ConstantCode.PARAM_FAIL_ABI_ID_EMPTY);
		}
		abiService.saveAbi(param);
		AbiInfo res = abiService.getAbiById(param.getAbiId());
		log.info("end updateAbi. useTime:{}, res:{}",
				Duration.between(startTime, Instant.now()).toMillis(), res);
		return new BaseResponse(ConstantCode.SUCCESS, res);
	}

	@Log(title = "BCOS3/合约管理/合约列表", businessType = BusinessType.DELETE)
	@SaCheckPermission("bcos3:contract:deleteAbi")
	@DeleteMapping("/{abiId}")
	public BaseResponse deleteAbi(@PathVariable("abiId") Integer abiId) {
		log.debug("start deleteAbi. abiId:{}", abiId);
		abiService.delete(abiId);
		log.debug("end deleteAbi");
		return new BaseResponse(ConstantCode.SUCCESS);
	}
}

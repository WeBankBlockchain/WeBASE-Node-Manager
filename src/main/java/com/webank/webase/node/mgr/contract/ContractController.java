/**
 * Copyright 2014-2020  the original author or authors.
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
package com.webank.webase.node.mgr.contract;

import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.controller.BaseController;
import com.webank.webase.node.mgr.base.entity.BasePageResponse;
import com.webank.webase.node.mgr.base.entity.BaseResponse;
import com.webank.webase.node.mgr.base.enums.ContractStatus;
import com.webank.webase.node.mgr.base.enums.SqlSortType;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.base.properties.ConstantProperties;
import com.webank.webase.node.mgr.base.tools.JsonTools;
import com.webank.webase.node.mgr.contract.entity.Contract;
import com.webank.webase.node.mgr.contract.entity.ContractParam;
import com.webank.webase.node.mgr.contract.entity.ContractPathParam;
import com.webank.webase.node.mgr.contract.entity.DeployInputParam;
import com.webank.webase.node.mgr.contract.entity.QueryByBinParam;
import com.webank.webase.node.mgr.contract.entity.QueryContractParam;
import com.webank.webase.node.mgr.contract.entity.ReqListContract;
import com.webank.webase.node.mgr.contract.entity.RspContractNoAbi;
import com.webank.webase.node.mgr.contract.entity.TbContract;
import com.webank.webase.node.mgr.contract.entity.TbContractPath;
import com.webank.webase.node.mgr.contract.entity.TransactionInputParam;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import javax.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.fisco.bcos.web3j.abi.datatypes.Address;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@RequestMapping("contract")
public class ContractController extends BaseController {

    @Autowired
    private ContractService contractService;
    @Autowired
    private ContractPathService contractPathService;

    /**
     * add new contract info.
     */
    @PostMapping(value = "/save")
    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN_OR_DEVELOPER)
    public BaseResponse saveContract(@RequestBody @Valid Contract contract, BindingResult result)
        throws NodeMgrException {
        checkBindResult(result);
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start saveContract startTime:{} contract:{}", startTime.toEpochMilli(),
            JsonTools.toJSONString(contract));

        // default path /
        if ("".equals(contract.getContractPath())) {
            contract.setContractPath("/");
        }
        // add contract row
        TbContract tbContract = contractService.saveContract(contract);

        baseResponse.setData(tbContract);

        log.info("end saveContract useTime:{} result:{}",
            Duration.between(startTime, Instant.now()).toMillis(), JsonTools.toJSONString(baseResponse));
        return baseResponse;
    }


    /**
     * delete contract by id.
     */
    @DeleteMapping(value = "/{groupId}/{contractId}")
    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN_OR_DEVELOPER)
    public BaseResponse deleteContract(@PathVariable("groupId") Integer groupId,
        @PathVariable("contractId") Integer contractId)
        throws NodeMgrException, Exception {
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start deleteContract startTime:{} contractId:{} groupId:{}",
            startTime.toEpochMilli(),
            contractId, groupId);

        contractService.deleteContract(contractId, groupId);

        log.info("end deleteContract useTime:{} result:{}",
            Duration.between(startTime, Instant.now()).toMillis(), JsonTools.toJSONString(baseResponse));
        return baseResponse;
    }


    /**
     * qurey contract info list.
     */
    @PostMapping(value = "/contractList")
    public BasePageResponse queryContractList(@RequestBody QueryContractParam inputParam)
        throws NodeMgrException {
        BasePageResponse pagesponse = new BasePageResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start contractList. startTime:{} inputParam:{}",
            startTime.toEpochMilli(), JsonTools.toJSONString(inputParam));

        //param
        ContractParam queryParam = new ContractParam();
        BeanUtils.copyProperties(inputParam, queryParam);

        int count = contractService.countOfContract(queryParam);
        if (count > 0) {
            Integer start = Optional.ofNullable(inputParam.getPageNumber())
                .map(page -> (page - 1) * inputParam.getPageSize()).orElse(0);
            queryParam.setStart(start);
            queryParam.setFlagSortedByTime(SqlSortType.DESC.getValue());
            // query list
            List<TbContract> listOfContract = contractService.qureyContractList(queryParam);

            pagesponse.setData(listOfContract);
            pagesponse.setTotalCount(count);
        }

        log.info("end contractList. useTime:{} result count:{}",
            Duration.between(startTime, Instant.now()).toMillis(), count);
        return pagesponse;
    }

    /**
     * query by contract id.
     */
    @GetMapping(value = "/{contractId}")
    public BaseResponse queryContract(@PathVariable("contractId") Integer contractId)
        throws NodeMgrException, Exception {
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start queryContract startTime:{} contractId:{}", startTime.toEpochMilli(),
            contractId);

        TbContract contractRow = contractService.queryByContractId(contractId);
        baseResponse.setData(contractRow);

        log.info("end queryContract useTime:{} result:{}",
            Duration.between(startTime, Instant.now()).toMillis(), JsonTools.toJSONString(baseResponse));
        return baseResponse;
    }

    /**
     * deploy deployInputParam.
     */
    @PostMapping(value = "/deploy")
    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN_OR_DEVELOPER)
    public BaseResponse deployContract(@RequestBody @Valid DeployInputParam deployInputParam,
        BindingResult result) throws NodeMgrException {
        checkBindResult(result);
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start queryContract startTime:{} deployInputParam:{}", startTime.toEpochMilli(),
            JsonTools.toJSONString(deployInputParam));

        TbContract tbContract = contractService.deployContract(deployInputParam);
        baseResponse.setData(tbContract);

        log.info("end deployContract useTime:{} result:{}",
            Duration.between(startTime, Instant.now()).toMillis(), JsonTools.toJSONString(baseResponse));

        return baseResponse;
    }

    /**
     * send transaction.
     */
    @PostMapping(value = "/transaction")
    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN_OR_DEVELOPER)
    public BaseResponse sendTransaction(@RequestBody @Valid TransactionInputParam param,
        BindingResult result) throws NodeMgrException {
        checkBindResult(result);
        // 0x0000000000000000000000000000000000000000 address is invalid
        if (Address.DEFAULT.toString().equals(param.getContractAddress())) {
            throw new NodeMgrException(ConstantCode.CONTRACT_ADDRESS_INVALID);
        }
        Instant startTime = Instant.now();
        log.info("start sendTransaction startTime:{} param:{}", startTime.toEpochMilli(),
            JsonTools.toJSONString(param));
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        Object transRsp = contractService.sendTransaction(param);
        baseResponse.setData(transRsp);
        log.info("end sendTransaction useTime:{} result:{}",
            Duration.between(startTime, Instant.now()).toMillis(), JsonTools.toJSONString(baseResponse));

        return baseResponse;
    }


    /**
     * get by partOfBytecodeBin.
     */
    @PostMapping(value = "/findByPartOfBytecodeBin")
    public BaseResponse getByPartOfByecodebin(@RequestBody @Valid QueryByBinParam queryParam,
        BindingResult result) {
        checkBindResult(result);
        Instant startTime = Instant.now();
        log.info("start getByPartOfByecodebin startTime:{} groupId:{} queryParam:{}",
            startTime.toEpochMilli(), JsonTools.toJSONString(queryParam));
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        ContractParam param = new ContractParam();
        BeanUtils.copyProperties(queryParam, param);
        TbContract tbContract = contractService.queryContract(param);
        baseResponse.setData(tbContract);
        log.info("end getByPartOfByecodebin useTime:{} result:{}",
            Duration.between(startTime, Instant.now()).toMillis(), JsonTools.toJSONString(baseResponse));
        return baseResponse;
    }


    /**
     * qurey contract info list by groupId without abi/bin
     */
    @GetMapping(value = "/contractList/all/light")
    public BasePageResponse queryContractListNoAbi(@RequestParam Integer groupId,
        @RequestParam Integer contractStatus)
        throws NodeMgrException {
        BasePageResponse pagesponse = new BasePageResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start queryContractListNoAbi. startTime:{} groupId:{}",
            startTime.toEpochMilli(), groupId);

        //param
        ContractParam queryParam = new ContractParam();
        queryParam.setGroupId(groupId);
        queryParam.setContractStatus(contractStatus);

        int count = contractService.countOfContract(queryParam);
        if (count > 0) {
            // query list
            List<RspContractNoAbi> listOfContract = contractService.qureyContractListNoAbi(queryParam);
            pagesponse.setData(listOfContract);
            pagesponse.setTotalCount(count);
        }

        log.info("end queryContractListNoAbi. useTime:{} result count:{}",
            Duration.between(startTime, Instant.now()).toMillis(), count);
        return pagesponse;
    }


    /**
     * add contract path
     */
    @PostMapping(value = "/contractPath")
    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN_OR_DEVELOPER)
    public BaseResponse addContractPath(@Valid @RequestBody ContractPathParam param) {
        BaseResponse response = new BaseResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start addContractPath. startTime:{} param:{}",
            startTime.toEpochMilli(), param);

        String contractPath = param.getContractPath();
        if ("".equals(contractPath)) {
            contractPath = "/";
        }
        int result = contractPathService.save(param.getGroupId(), contractPath);
        response.setData(result);

        log.info("end addContractPath. useTime:{} add result:{}",
            Duration.between(startTime, Instant.now()).toMillis(), result);
        return response;
    }


    /**
     * qurey contract info list.
     */
    @PostMapping(value = "/contractPath/list/{groupId}")
    public BasePageResponse queryContractPathList(@PathVariable("groupId") Integer groupId) {
        BasePageResponse pagesponse = new BasePageResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start queryContractPathList. startTime:{} groupId:{}",
            startTime.toEpochMilli(), groupId);

        List<TbContractPath> result = contractService.queryContractPathList(groupId);
        pagesponse.setData(result);
        pagesponse.setTotalCount(result.size());

        log.info("end queryContractPathList. useTime:{} result:{}",
            Duration.between(startTime, Instant.now()).toMillis(), JsonTools.toJSONString(pagesponse));
        return pagesponse;
    }

    /**
     * delete contract by id.
     * only admin batch delete contract
     */
    @DeleteMapping(value = "/batch/path")
    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN)
    public BaseResponse deleteContractByPath(@Valid @RequestBody ContractPathParam param) {
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start deleteContractByPath startTime:{} ContractPathParam:{}",
            startTime.toEpochMilli(), param);

        contractService.deleteByContractPath(param);

        log.info("end deleteContractByPath useTime:{}",
            Duration.between(startTime, Instant.now()).toMillis());
        return baseResponse;
    }

    /**
     * qurey contract info list by multi path
     */
    @PostMapping(value = "/contractList/multiPath")
    public BasePageResponse listContractByMultiPath(@RequestBody ReqListContract inputParam)
        throws NodeMgrException {
        BasePageResponse pagesponse = new BasePageResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start listContractByMultiPath. startTime:{} inputParam:{}",
            startTime.toEpochMilli(), JsonTools.toJSONString(inputParam));
        List<TbContract> contractList = contractService.qureyContractListMultiPath(inputParam);
        pagesponse.setTotalCount(contractList.size());
        pagesponse.setData(contractList);
        log.info("end listContractByMultiPath. useTime:{} result count:{}",
            Duration.between(startTime, Instant.now()).toMillis(), contractList.size());
        return pagesponse;
    }

}

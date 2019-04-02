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
package com.webank.webase.node.mgr.contract;

import com.alibaba.fastjson.JSON;
import com.webank.webase.node.mgr.base.entity.BasePageResponse;
import com.webank.webase.node.mgr.base.entity.BaseResponse;
import com.webank.webase.node.mgr.base.entity.ConstantCode;
import com.webank.webase.node.mgr.base.enums.ShareType;
import com.webank.webase.node.mgr.base.enums.SqlSortType;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.contract.entity.Contract;
import com.webank.webase.node.mgr.contract.entity.ContractParam;
import com.webank.webase.node.mgr.contract.entity.QueryContractParam;
import com.webank.webase.node.mgr.contract.entity.TbContract;
import com.webank.webase.node.mgr.contract.entity.Transaction;
import com.webank.webase.node.mgr.scheduler.SharedChainInfoTask;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@RequestMapping("contract")
public class ContractController {

    @Autowired
    private ContractService contractService;
    @Autowired
    private SharedChainInfoTask sharedChainInfoTask;

    /**
     * add new contract info.
     */
    @PostMapping(value = "/contractInfo")
    public BaseResponse addCotractInfo(@RequestBody Contract contract) throws NodeMgrException {
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start addCotractInfo startTime:{} contract:{}", startTime.toEpochMilli(),
            JSON.toJSONString(contract));

        // add contract row
        Integer contractId = contractService.addContractInfo(contract);

        // query the record of a new row
        TbContract contractRow = contractService.queryByContractId(contractId);
        baseResponse.setData(contractRow);

        log.info("end addCotractInfo useTime:{} result:{}",
            Duration.between(startTime, Instant.now()).toMillis(), JSON.toJSONString(baseResponse));
        return baseResponse;
    }

    /**
     * update contract info.
     */
    @PutMapping(value = "/contractInfo")
    public BaseResponse updateContractInfo(@RequestBody Contract contract)
        throws NodeMgrException, Exception {
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start updateContractInfo startTime:{} contract:{}", startTime.toEpochMilli(),
            JSON.toJSONString(contract));

        // update contract row
        contractService.updateContract(contract);
        // query the record of a new row
        TbContract contractRow = contractService.queryByContractId(contract.getContractId());
        baseResponse.setData(contractRow);

        log.info("end updateContractInfo useTime:{} result:{}",
            Duration.between(startTime, Instant.now()).toMillis(), JSON.toJSONString(baseResponse));
        return baseResponse;
    }

    /**
     * qurey contract info list.
     */
    @PostMapping(value = "/contractList")
    public BasePageResponse queryContractList(@RequestBody QueryContractParam queryParam) throws NodeMgrException {
        BasePageResponse pagesponse = new BasePageResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start contractList. startTime:{} queryParam:{}",
            startTime.toEpochMilli(), JSON.toJSONString(queryParam));
        Integer networkId = queryParam.getNetworkId();
        Integer pageNumber = queryParam.getPageNumber();
        Integer pageSize = queryParam.getPageSize();


        // share from chain
        sharedChainInfoTask.asyncShareFromChain(networkId, ShareType.CONTRACT);

        ContractParam param = new ContractParam();
        param.setNetworkId(networkId);
        // param.setContractType(ContractType.GENERALCONTRACT.getValue());

        Integer count = contractService.countOfContract(param);
        if (count != null && count > 0) {
            Integer start = Optional.ofNullable(pageNumber).map(page -> (page - 1) * pageSize)
                .orElse(0);
            param.setPageSize(pageSize);
            param.setStart(start);
            param.setFlagSortedByTime(SqlSortType.DESC.getValue());
            // query list
            List<TbContract> listOfContract = contractService.qureyContractList(param);

            pagesponse.setData(listOfContract);
            pagesponse.setTotalCount(count);
        }

        log.info("end contractList. useTime:{} result:{}",
            Duration.between(startTime, Instant.now()).toMillis(), JSON.toJSONString(pagesponse));
        return pagesponse;
    }

    /**
     * delete contract by id.
     */
    @DeleteMapping(value = "/{contractId}")
    public BaseResponse deleteContract(@PathVariable("contractId") Integer contractId)
        throws NodeMgrException, Exception {
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start deleteContract startTime:{} contractId:{}", startTime.toEpochMilli(),
            contractId);

        contractService.deleteContract(contractId);

        log.info("end deleteContract useTime:{} result:{}",
            Duration.between(startTime, Instant.now()).toMillis(), JSON.toJSONString(baseResponse));
        return baseResponse;
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
            Duration.between(startTime, Instant.now()).toMillis(), JSON.toJSONString(baseResponse));
        return baseResponse;
    }

    /**
     * deploy contract.
     */
    @PostMapping(value = "/deploy")
    public BaseResponse deployContract(@RequestBody Contract contract) throws NodeMgrException {
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start queryContract startTime:{} contract:{}", startTime.toEpochMilli(),
            JSON.toJSONString(contract));

        TbContract tbContract = contractService.deployContract(contract);
        baseResponse.setData(tbContract);

        log.info("end deployContract useTime:{} result:{}",
            Duration.between(startTime, Instant.now()).toMillis(), JSON.toJSONString(baseResponse));

        return baseResponse;
    }

    /**
     * send transaction.
     */
    @PostMapping(value = "/transaction")
    public BaseResponse sendTransaction(@RequestBody Transaction param) throws NodeMgrException {
        Instant startTime = Instant.now();
        log.info("start sendTransaction startTime:{} param:{}", startTime.toEpochMilli(),
            JSON.toJSONString(param));

        BaseResponse transRsp = contractService.sendTransaction(param);

        log.info("end sendTransaction useTime:{} result:{}",
            Duration.between(startTime, Instant.now()).toMillis(), JSON.toJSONString(transRsp));

        return transRsp;
    }
}

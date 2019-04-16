/**
 * Copyright 2014-2019  the original author or authors.
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

import com.alibaba.fastjson.JSON;
import com.webank.webase.node.mgr.base.entity.BasePageResponse;
import com.webank.webase.node.mgr.base.entity.BaseResponse;
import com.webank.webase.node.mgr.base.entity.ConstantCode;
import com.webank.webase.node.mgr.base.enums.SqlSortType;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.contract.entity.ContractParam;
import com.webank.webase.node.mgr.contract.entity.DeployIncoming;
import com.webank.webase.node.mgr.contract.entity.QueryContractParam;
import com.webank.webase.node.mgr.contract.entity.TbContract;
import com.webank.webase.node.mgr.contract.entity.Transaction;
import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@RequestMapping("contract")
public class ContractController {

    @Autowired
    private ContractService contractService;
    /*

     */
/**
 * add new contract info.
 *//*

    @PostMapping(value = "/contractInfo")
    public BaseResponse addCotractInfo(@RequestBody DeployIncoming contract) throws NodeMgrException {
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

    */
/**
 * update contract info.
 *//*

    @PutMapping(value = "/contractInfo")
    public BaseResponse updateContractInfo(@RequestBody DeployIncoming contract)
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

    */


    /**
     * qurey contract info list.
     */
    @PostMapping(value = "/contractList")
    public BasePageResponse queryContractList(@RequestBody QueryContractParam inputParam)
        throws NodeMgrException {
        BasePageResponse pagesponse = new BasePageResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start contractList. startTime:{} inputParam:{}",
            startTime.toEpochMilli(), JSON.toJSONString(inputParam));

        //param
        ContractParam queryParam = new ContractParam();
        BeanUtils.copyProperties(inputParam, queryParam);

        int count = contractService.countOfContract(queryParam);
        if (count > 0) {
            Integer start = Optional.ofNullable(inputParam.getPageNumber())
                .map(page -> (page - 1) * inputParam.getPageNumber()).orElse(0);
            queryParam.setStart(start);
            queryParam.setFlagSortedByTime(SqlSortType.DESC.getValue());
            // query list
            List<TbContract> listOfContract = contractService.qureyContractList(queryParam);

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
/*    @DeleteMapping(value = "/{contractId}")
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
    }*/

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
     * deploy deployIncoming.
     */
    @PostMapping(value = "/deploy")
    public BaseResponse deployContract(@RequestBody DeployIncoming deployIncoming)
        throws NodeMgrException {
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start queryContract startTime:{} deployIncoming:{}", startTime.toEpochMilli(),
            JSON.toJSONString(deployIncoming));

        TbContract tbContract = contractService.deployContract(deployIncoming);
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
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        Object transRsp = contractService.sendTransaction(param);
        baseResponse.setData(transRsp);
        log.info("end sendTransaction useTime:{} result:{}",
            Duration.between(startTime, Instant.now()).toMillis(), JSON.toJSONString(baseResponse));

        return baseResponse;
    }

    /**
     * get contract code.
     */
    @GetMapping("/code/{groupId}/{address}/{blockNumber}")
    public BaseResponse getContractCode(@PathVariable("groupId") Integer groupId,
        @PathVariable("address") String address,
        @PathVariable("blockNumber") BigInteger blockNumber) throws NodeMgrException {
        Instant startTime = Instant.now();
        log.info("start getContractCode startTime:{} groupId:{} address:{} blockNumber:{}",
            startTime.toEpochMilli(), groupId, address, blockNumber);
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        String contractCode = contractService.getContractCode(groupId, address, blockNumber);
        baseResponse.setData(contractCode);
        log.info("end getContractCode useTime:{} result:{}",
            Duration.between(startTime, Instant.now()).toMillis(), JSON.toJSONString(baseResponse));
        return baseResponse;
    }

    /**
     * get by partOfBytecodeBin.
     */
    @GetMapping(value = "/findByPartOfBytecodeBin/{groupId}/{partOfBytecodeBin}")
    public BaseResponse getByPartOfByecodebin(@PathVariable("groupId") Integer groupId,
        @PathVariable("partOfBytecodeBin") String partOfBytecodeBin) {
        Instant startTime = Instant.now();
        log.info("start getByPartOfByecodebin startTime:{} groupId:{} partOfBytecodeBin:{}",
            startTime.toEpochMilli(), groupId, partOfBytecodeBin);
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        ContractParam param = new ContractParam();
        param.setGroupId(groupId);
        param.setPartOfByecodebin(partOfBytecodeBin);
        TbContract tbContract = contractService.queryContract(param);
        baseResponse.setData(tbContract);
        log.info("end getByPartOfByecodebin useTime:{} result:{}",
            Duration.between(startTime, Instant.now()).toMillis(), JSON.toJSONString(baseResponse));
        return baseResponse;
    }
}

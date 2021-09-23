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

import com.webank.webase.node.mgr.base.annotation.CurrentAccount;
import com.webank.webase.node.mgr.base.annotation.entity.CurrentAccountInfo;
import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.controller.BaseController;
import com.webank.webase.node.mgr.base.entity.BasePageResponse;
import com.webank.webase.node.mgr.base.entity.BaseResponse;
import com.webank.webase.node.mgr.base.enums.RoleType;
import com.webank.webase.node.mgr.base.enums.SqlSortType;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.tools.JsonTools;
import com.webank.webase.node.mgr.contract.entity.ContractParam;
import com.webank.webase.node.mgr.external.entity.RspAllExtAccount;
import com.webank.webase.node.mgr.external.entity.RspAllExtContract;
import com.webank.webase.node.mgr.external.entity.TbExternalAccount;
import com.webank.webase.node.mgr.external.entity.TbExternalContract;
import com.webank.webase.node.mgr.user.entity.UserParam;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@RequestMapping(value = "external")
public class ExternalController extends BaseController {

    @Autowired
    private ExtAccountService extAccountService;
    @Autowired
    private ExtContractService extContractService;
    
    /**
     * query external account info list.
     */
    @GetMapping(value = "account/list/{groupId}/{pageNumber}/{pageSize}")
    public BasePageResponse extAccountList(@PathVariable("groupId") Integer groupId,
        @PathVariable("pageNumber") Integer pageNumber,
        @PathVariable("pageSize") Integer pageSize)
        throws NodeMgrException {
        BasePageResponse pageResponse = new BasePageResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start extAccountList startTime:{} groupId:{} pageNumber:{} pageSize:{}",
            startTime.toEpochMilli(), groupId, pageNumber, pageSize);

        UserParam param = new UserParam();
        param.setGroupId(groupId);
        param.setPageSize(pageSize);

        int count = extAccountService.countExtAccount(param);
        if (count > 0) {
            Integer start =
                Optional.ofNullable(pageNumber).map(page -> (page - 1) * pageSize).orElse(null);
            param.setStart(start);
            param.setPageSize(pageSize);

            List<TbExternalAccount> listOfUser = extAccountService.listExtAccount(param);
            pageResponse.setData(listOfUser);
            pageResponse.setTotalCount(count);
        }

        log.info("end extAccountList useTime:{} result:{}",
            Duration.between(startTime, Instant.now()).toMillis(),
            JsonTools.toJSONString(pageResponse));
        return pageResponse;
    }

    /**
     * query contract info list by groupId without abi/bin
     */
    @GetMapping(value = "/contract/list/{groupId}/{pageNumber}/{pageSize}")
    public BasePageResponse extContractList(@PathVariable("groupId") Integer groupId,
        @PathVariable("pageNumber") Integer pageNumber,
        @PathVariable("pageSize") Integer pageSize) throws NodeMgrException {
        BasePageResponse pageResponse = new BasePageResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start extContractList. startTime:{} groupId:{}", startTime.toEpochMilli(),
            groupId);
        ContractParam param = new ContractParam();
        param.setGroupId(groupId);
        param.setPageSize(pageSize);
        int count = extContractService.countExtContract(param);
        
        if (count > 0) {
            Integer start =
                Optional.ofNullable(pageNumber).map(page -> (page - 1) * pageSize).orElse(null);
            param.setStart(start);
            param.setFlagSortedByTime(SqlSortType.DESC.getValue());
            // query list
            List<TbExternalContract> listOfContract = extContractService.listExtContract(param);

            pageResponse.setData(listOfContract);
            pageResponse.setTotalCount(count);
        }

        log.info("end extContractList. useTime:{} result count:{}",
            Duration.between(startTime, Instant.now()).toMillis(), count);
        return pageResponse;
    }

    /**
     * query external account info list.
     * left join tb_user的接口
     */
    @GetMapping(value = "account/list/all/{groupId}/{pageNumber}/{pageSize}")
    public BasePageResponse listExtUserListJoin(@PathVariable("groupId") Integer groupId,
        @PathVariable("pageNumber") Integer pageNumber,
        @PathVariable("pageSize") Integer pageSize,
        @RequestParam(value = "type", defaultValue = "1") Integer type,
        @RequestParam(value = "commParam", required = false) String commParam,
        @CurrentAccount CurrentAccountInfo currentAccountInfo) throws NodeMgrException {
        BasePageResponse pageResponse = new BasePageResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start listExtUserListJoin startTime:{} groupId:{} pageNumber:{} pageSize:{},type:{},commParam:{}",
            startTime.toEpochMilli(), groupId, pageNumber, pageSize, type, commParam);

        String account = RoleType.DEVELOPER.getValue().intValue() == currentAccountInfo.getRoleId().intValue() 
                ? currentAccountInfo.getAccount() : null;
        UserParam param = new UserParam();
        param.setGroupId(groupId);
        param.setPageSize(pageSize);
        param.setAccount(account);
        // search address or username
        param.setCommParam(commParam);
        // type: 1-all, 2-normal
        param.setType(type);

        int count = extAccountService.countExtAccount(param);
        if (count > 0) {
            Integer start =
                Optional.ofNullable(pageNumber).map(page -> (page - 1) * pageSize).orElse(null);
            param.setStart(start);
            param.setPageSize(pageSize);

            List<RspAllExtAccount> listOfUser = extAccountService.getAllExtAccountLeftJoinUser(param);
            pageResponse.setData(listOfUser);
            pageResponse.setTotalCount(count);
        }

        log.info("end listExtUserListJoin useTime:{} result:{}",
            Duration.between(startTime, Instant.now()).toMillis(),
            JsonTools.toJSONString(pageResponse));
        return pageResponse;
    }

    /**
     * query contract info list by groupId without abi/bin
     * left join tb_abi的接口（本地全量）
     */
    @GetMapping(value = "/contract/list/all/{groupId}/{pageNumber}/{pageSize}")
    public BasePageResponse listExtContractListJoin(@PathVariable("groupId") Integer groupId,
        @PathVariable("pageNumber") Integer pageNumber,
        @PathVariable("pageSize") Integer pageSize,
        @RequestParam(value = "type", defaultValue = "1") Integer type,
        @RequestParam(value = "contractAddress", required = false) String contractAddress,
        @RequestParam(value = "contractName", required = false) String contractName,
        @RequestParam(value = "requiredBin", defaultValue = "true", required = false) Boolean requiredBin,
        @CurrentAccount CurrentAccountInfo currentAccountInfo) throws NodeMgrException {
        BasePageResponse pageResponse = new BasePageResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start listExtContractListJoin. startTime:{} groupId:{}", startTime.toEpochMilli(),
            groupId);
        
        String account = RoleType.DEVELOPER.getValue().intValue() == currentAccountInfo.getRoleId().intValue() 
                ? currentAccountInfo.getAccount() : null;
        ContractParam param = new ContractParam();
        param.setGroupId(groupId);
        param.setPageSize(pageSize);
        param.setAccount(account);
        param.setContractAddress(contractAddress);
        param.setContractName(contractName);
        // type: 1-all, 2-normal
        param.setContractType(type);
        int count = extContractService.countExtContract(param);
        if (count > 0) {
            Integer start =
                Optional.ofNullable(pageNumber).map(page -> (page - 1) * pageSize).orElse(null);
            param.setStart(start);
            param.setFlagSortedByTime(SqlSortType.DESC.getValue());
            // query list
            List<RspAllExtContract> listOfContract = extContractService.getAllExtContractLeftJoinAbi(param, requiredBin);

            pageResponse.setData(listOfContract);
            pageResponse.setTotalCount(count);
        }

        log.info("end listExtContractListJoin. useTime:{} result count:{}",
            Duration.between(startTime, Instant.now()).toMillis(), count);
        return pageResponse;
    }

    /**
     * get deploy address
     */
    @GetMapping("deployAddress/{groupId}/{contractAddress}")
    public BaseResponse queryDeployAddress(@PathVariable("groupId") Integer groupId,
        @PathVariable("contractAddress") String contractAddress) {
        Instant startTime = Instant.now();
        log.info("start queryDeployAddress. startTime:{} groupId:{},contractAddress:{}",
            startTime.toEpochMilli(), groupId, contractAddress);
        TbExternalContract externalContract = extContractService.getByAddress(groupId, contractAddress);
        String deployAddress = externalContract.getDeployAddress();
        log.info("end queryDeployAddress. useTime:{} deployAddress:{}",
            Duration.between(startTime, Instant.now()).toMillis(), deployAddress);
        return new BaseResponse(ConstantCode.SUCCESS, deployAddress);
    }
}

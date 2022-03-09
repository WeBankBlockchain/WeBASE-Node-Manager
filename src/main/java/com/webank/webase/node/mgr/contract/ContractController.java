/**
 * Copyright 2014-2021 the original author or authors.
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

import com.webank.webase.node.mgr.base.annotation.CurrentAccount;
import com.webank.webase.node.mgr.base.annotation.entity.CurrentAccountInfo;
import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.controller.BaseController;
import com.webank.webase.node.mgr.base.entity.BasePageResponse;
import com.webank.webase.node.mgr.base.entity.BaseResponse;
import com.webank.webase.node.mgr.base.enums.RoleType;
import com.webank.webase.node.mgr.base.enums.SqlSortType;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.config.properties.ConstantProperties;
import com.webank.webase.node.mgr.contract.entity.*;
import com.webank.webase.node.mgr.external.entity.TbExternalContract;
import com.webank.webase.node.mgr.tools.JsonTools;
import com.webank.webase.node.mgr.user.entity.TbUser;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import javax.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.sdk.codec.datatypes.Address;
import org.fisco.bcos.sdk.utils.AddressUtils;
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
    @Autowired
    private CnsService cnsService;

    /**
     * add new contract info.
     */
    @PostMapping(value = "/save")
    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN_OR_DEVELOPER)
    public BaseResponse saveContract(@RequestBody @Valid Contract contract,
            @CurrentAccount CurrentAccountInfo currentAccountInfo, BindingResult result) throws NodeMgrException {
        checkBindResult(result);
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start saveContract startTime:{} contract:{}", startTime.toEpochMilli(),
                JsonTools.toJSONString(contract));
        // default path "/"
        if ("".equals(contract.getContractPath())) {
            contract.setContractPath("/");
        }
        // add contract row
        contract.setAccount(currentAccountInfo.getAccount());
        TbContract tbContract = contractService.saveContract(contract);

        baseResponse.setData(tbContract);

        log.info("end saveContract useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JsonTools.toJSONString(baseResponse));
        return baseResponse;
    }


    /**
     * delete contract by id.
     */
    @DeleteMapping(value = "/{groupId}/{contractId}")
    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN_OR_DEVELOPER)
    public BaseResponse deleteContract(@PathVariable("groupId") String groupId,
            @PathVariable("contractId") Integer contractId) throws NodeMgrException, Exception {
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start deleteContract startTime:{} contractId:{} groupId:{}",
                startTime.toEpochMilli(), contractId, groupId);

        contractService.deleteContract(contractId, groupId);

        log.info("end deleteContract useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JsonTools.toJSONString(baseResponse));
        return baseResponse;
    }


    /**
     * query contract info list.
     */
    @PostMapping(value = "/contractList")
    public BasePageResponse queryContractList(@RequestBody QueryContractParam inputParam, 
            @CurrentAccount CurrentAccountInfo currentAccountInfo) throws NodeMgrException {
        BasePageResponse pageResponse = new BasePageResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start contractList. startTime:{} inputParam:{}", startTime.toEpochMilli(),
                JsonTools.toJSONString(inputParam));

        // param
        ContractParam queryParam = new ContractParam();
        BeanUtils.copyProperties(inputParam, queryParam);
        String account = RoleType.DEVELOPER.getValue().intValue() == currentAccountInfo.getRoleId().intValue() 
                ? currentAccountInfo.getAccount() : null;
        queryParam.setAccount(account);

        int count = contractService.countOfContract(queryParam);
        if (count > 0) {
            Integer start = Optional.ofNullable(inputParam.getPageNumber())
                    .map(page -> (page - 1) * inputParam.getPageSize()).orElse(0);
            queryParam.setStart(start);
            queryParam.setFlagSortedByTime(SqlSortType.DESC.getValue());
            // query list
            List<TbContract> listOfContract = contractService.queryContractList(queryParam);

            pageResponse.setData(listOfContract);
            pageResponse.setTotalCount(count);
        }

        log.info("end contractList. useTime:{} result count:{}",
                Duration.between(startTime, Instant.now()).toMillis(), count);
        return pageResponse;
    }

    /**
     * query by contract id.
     */
    @GetMapping(value = "/{contractId}")
    public BaseResponse queryContract(@PathVariable("contractId") Integer contractId) {
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start queryContract startTime:{} contractId:{}", startTime.toEpochMilli(),
                contractId);

        TbContract contractRow = contractService.queryByContractId(contractId);
        baseResponse.setData(contractRow);

        log.info("end queryContract useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JsonTools.toJSONString(baseResponse));
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
                Duration.between(startTime, Instant.now()).toMillis(),
                JsonTools.toJSONString(baseResponse));

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
        Instant startTime = Instant.now();
        log.info("start sendTransaction startTime:{} param:{}", startTime.toEpochMilli(),
            JsonTools.toJSONString(param));
        // 0x0000000000000000000000000000000000000000 address is invalid
        if (Address.DEFAULT.toString().equals(param.getContractAddress())) {
            throw new NodeMgrException(ConstantCode.CONTRACT_ADDRESS_INVALID);
        }
        // check version
        if (param.isUseCns()) {
            if (StringUtils.isBlank(param.getVersion())) {
                throw new NodeMgrException(ConstantCode.VERSION_CANNOT_EMPTY);
            }
            if (StringUtils.isBlank(param.getCnsName())) {
                throw new NodeMgrException(ConstantCode.CNS_NAME_CANNOT_EMPTY);
            }
        }

        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        Object transRsp = contractService.sendTransaction(param);
        baseResponse.setData(transRsp);
        log.info("end sendTransaction useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JsonTools.toJSONString(baseResponse));

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
        log.info("start getByPartOfByecodebin startTime:{} queryParam:{}",
                startTime.toEpochMilli(), JsonTools.toJSONString(queryParam));
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        Object res = contractService.queryContractOrAbiByBin(queryParam);
        baseResponse.setData(res);
        log.info("end getByPartOfByecodebin useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JsonTools.toJSONString(baseResponse));
        return baseResponse;
    }


    /**
     * query contract info list by groupId without abi/bin
     */
    @GetMapping(value = "/contractList/all/light")
    public BasePageResponse queryContractListNoAbi(@RequestParam String groupId,
            @RequestParam Integer contractStatus) throws NodeMgrException {
        BasePageResponse pageResponse = new BasePageResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start queryContractListNoAbi. startTime:{} groupId:{}", startTime.toEpochMilli(),
                groupId);

        // param
        ContractParam queryParam = new ContractParam();
        queryParam.setGroupId(groupId);
        queryParam.setContractStatus(contractStatus);

        int count = contractService.countOfContract(queryParam);
        if (count > 0) {
            // query list
            List<RspContractNoAbi> listOfContract =
                    contractService.queryContractListNoAbi(queryParam);
            pageResponse.setData(listOfContract);
            pageResponse.setTotalCount(count);
        }

        log.info("end queryContractListNoAbi. useTime:{} result count:{}",
                Duration.between(startTime, Instant.now()).toMillis(), count);
        return pageResponse;
    }


    /**
     * add contract path
     */
    @PostMapping(value = "/contractPath")
    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN_OR_DEVELOPER)
    public BaseResponse addContractPath(@Valid @RequestBody ContractPathParam param,
            @CurrentAccount CurrentAccountInfo currentAccountInfo) {
        BaseResponse response = new BaseResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start addContractPath. startTime:{} param:{}", startTime.toEpochMilli(), param);

        String contractPath = param.getContractPath();
        if ("".equals(contractPath)) {
            contractPath = "/";
        }
        int result = contractPathService.save(param.getGroupId(), contractPath, currentAccountInfo.getAccount(), false);
        response.setData(result);

        log.info("end addContractPath. useTime:{} add result:{}",
                Duration.between(startTime, Instant.now()).toMillis(), result);
        return response;
    }


    /**
     * query contract info list.
     */
    @PostMapping(value = "/contractPath/list/{groupId}")
    public BasePageResponse queryContractPathList(@PathVariable("groupId") String groupId,
            @CurrentAccount CurrentAccountInfo currentAccountInfo) {
        BasePageResponse pageResponse = new BasePageResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start queryContractPathList. startTime:{} groupId:{}", startTime.toEpochMilli(),
                groupId);
        String account = RoleType.DEVELOPER.getValue().intValue() == currentAccountInfo.getRoleId().intValue() 
                ? currentAccountInfo.getAccount() : null;
        List<TbContractPath> result = contractService.queryContractPathList(groupId, account);
        pageResponse.setData(result);
        pageResponse.setTotalCount(result.size());

        log.info("end queryContractPathList. useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JsonTools.toJSONString(pageResponse));
        return pageResponse;
    }

    /**
     * delete contract by path. only admin batch delete contract
     */
    @DeleteMapping(value = "/batch/path")
    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN_OR_DEVELOPER)
    public BaseResponse deleteContractByPath(@Valid @RequestBody ContractPathParam param,
            @CurrentAccount CurrentAccountInfo currentAccountInfo) {
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start deleteContractByPath startTime:{} ContractPathParam:{}",
                startTime.toEpochMilli(), param);
        
        contractService.deleteByContractPath(param, currentAccountInfo);

        log.info("end deleteContractByPath useTime:{}",
                Duration.between(startTime, Instant.now()).toMillis());
        return baseResponse;
    }

    /**
     * query contract info list by multi path
     */
    @PostMapping(value = "/contractList/multiPath")
    public BasePageResponse listContractByMultiPath(@RequestBody ReqListContract inputParam,
            @CurrentAccount CurrentAccountInfo currentAccountInfo) throws NodeMgrException {
        BasePageResponse pageResponse = new BasePageResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start listContractByMultiPath. startTime:{} inputParam:{}",
                startTime.toEpochMilli(), JsonTools.toJSONString(inputParam));

        String account = RoleType.DEVELOPER.getValue().intValue() == currentAccountInfo.getRoleId()
                .intValue() ? currentAccountInfo.getAccount() : null;
        inputParam.setAccount(account);
        List<TbContract> contractList = contractService.queryContractListMultiPath(inputParam);
        pageResponse.setTotalCount(contractList.size());
        pageResponse.setData(contractList);
        log.info("end listContractByMultiPath. useTime:{} result count:{}",
                Duration.between(startTime, Instant.now()).toMillis(), contractList.size());
        return pageResponse;
    }
//
//    /**
//     * registerCns.
//     */
//    @PostMapping(value = "/registerCns")
//    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN_OR_DEVELOPER)
//    public BaseResponse registerCns(@RequestBody @Valid ReqRegisterCns reqRegisterCns,
//            BindingResult result) throws NodeMgrException {
//        checkBindResult(result);
//        Instant startTime = Instant.now();
//        log.info("start registerCns startTime:{} reqRegisterCns:{}", startTime.toEpochMilli(),
//                JsonTools.toJSONString(reqRegisterCns));
//
//        cnsService.registerCns(reqRegisterCns);
//
//        log.info("end registerCns useTime:{}",
//                Duration.between(startTime, Instant.now()).toMillis());
//
//        return new BaseResponse(ConstantCode.SUCCESS);
//    }

//    /**
//     * query cns info
//     */
//    @PostMapping(value = "/findCns")
//    public BaseResponse findCnsByAddress(@RequestBody @Valid ReqQueryCns reqQueryCns,
//            BindingResult result) throws NodeMgrException {
//        checkBindResult(result);
//        BaseResponse pageResponse = new BaseResponse(ConstantCode.SUCCESS);
//        Instant startTime = Instant.now();
//        log.info("start findCnsByAddress startTime:{} reqQueryCns:{}", startTime.toEpochMilli(),
//                JsonTools.toJSONString(reqQueryCns));
//        TbCns tbCns = cnsService.getCnsByAddress(
//                new QueryCnsParam(reqQueryCns.getGroupId(), reqQueryCns.getContractAddress()));
//        pageResponse.setData(tbCns);
//        log.info("end findCnsByAddress. useTime:{}",
//                Duration.between(startTime, Instant.now()).toMillis());
//        return pageResponse;
//    }
//
//    /**
//     * query cns info list
//     */
//    @PostMapping(value = "/findCnsList")
//    public BasePageResponse findCnsList(@RequestBody @Valid ReqQueryCnsList inputParam,
//            BindingResult result) throws NodeMgrException {
//        checkBindResult(result);
//        BasePageResponse pageResponse = new BasePageResponse(ConstantCode.SUCCESS);
//        Instant startTime = Instant.now();
//        log.info("start findCnsList startTime:{} reqQueryCns:{}", startTime.toEpochMilli(),
//                JsonTools.toJSONString(inputParam));
//
//        // param
//        QueryCnsParam queryParam = new QueryCnsParam();
//        BeanUtils.copyProperties(inputParam, queryParam);
//
//        int count = cnsService.countOfCns(queryParam);
//        if (count > 0) {
//            Integer start = Optional.ofNullable(inputParam.getPageNumber())
//                    .map(page -> (page - 1) * inputParam.getPageSize()).orElse(0);
//            queryParam.setStart(start);
//            queryParam.setFlagSortedByTime(SqlSortType.DESC.getValue());
//            // query list
//            List<TbCns> listOfCns = cnsService.getList(queryParam);
//            pageResponse.setData(listOfCns);
//            pageResponse.setTotalCount(count);
//        }
//
//        log.info("end findCnsList. useTime:{}",
//                Duration.between(startTime, Instant.now()).toMillis());
//        return pageResponse;
//    }

    @PostMapping(value = "/copy")
    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN_OR_DEVELOPER)
    public BaseResponse copyContracts(@RequestBody @Valid ReqCopyContracts req,
        @CurrentAccount CurrentAccountInfo currentAccountInfo, BindingResult result) {
        Instant startTime = Instant.now();
        log.info("copyContracts start. startTime:{}  req:{}", startTime.toEpochMilli(),
                JsonTools.toJSONString(req));
        checkBindResult(result);
        req.setAccount(currentAccountInfo.getAccount());
        contractService.copyContracts(req);
        log.info("end copyContracts. useTime:{}",
                Duration.between(startTime, Instant.now()).toMillis());
        return new BaseResponse(ConstantCode.SUCCESS, req.getContractItems().size());
    }



    /**
     * get deploy address or permission admin user address list
     * which has private key in webase
     */
    @GetMapping("listManager/{groupId}/{contractAddress}")
    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN_OR_DEVELOPER)
    public BaseResponse queryContractManagerList(@PathVariable("groupId") String groupId,
        @PathVariable("contractAddress") String contractAddress) {
        Instant startTime = Instant.now();
        log.info("start queryDeployAddress. startTime:{} groupId:{},contractAddress:{}",
            startTime.toEpochMilli(), groupId, contractAddress);
        if (!AddressUtils.isValidAddress(contractAddress)) {
            throw new NodeMgrException(ConstantCode.CONTRACT_ADDRESS_INVALID);
        }
        List<TbUser> managerList = contractService.getContractManager(groupId, contractAddress);
        log.info("end queryDeployAddress. useTime:{} managerList:{}",
            Duration.between(startTime, Instant.now()).toMillis(), managerList);
        return new BaseResponse(ConstantCode.SUCCESS, managerList);
    }


    /**
     * compile liquid
     */
    @PostMapping(value = "/liquid/compile")
    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN_OR_DEVELOPER)
    public BaseResponse compileLiquid(@RequestBody @Valid ReqCompileLiquid param,
                                       BindingResult result) throws NodeMgrException {
        checkBindResult(result);
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start compileLiquid startTime:{} param:{}", startTime.toEpochMilli(),
            JsonTools.toJSONString(param));
        if (param.getContractId() == null) {
            throw new NodeMgrException(ConstantCode.INVALID_CONTRACT_ID);
        }
        RspCompileTask rspCompileTask = contractService.compileLiquidContract(param);
        baseResponse.setData(rspCompileTask);
        log.info("end compileLiquid useTime:{} result:{}",
            Duration.between(startTime, Instant.now()).toMillis(),
            JsonTools.toJSONString(baseResponse));

        return baseResponse;
    }


    /**
     * compile liquid
     */
    @PostMapping(value = "/liquid/compile/check")
    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN_OR_DEVELOPER)
    public BaseResponse checkCompileLiquid(@RequestBody @Valid ReqCompileLiquid param,
                                      BindingResult result) throws NodeMgrException {
        checkBindResult(result);
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start checkCompileLiquid startTime:{} param:{}", startTime.toEpochMilli(),
            JsonTools.toJSONString(param));
        if (param.getContractId() == null) {
            throw new NodeMgrException(ConstantCode.INVALID_CONTRACT_ID);
        }
        RspCompileTask rspCompileTask = contractService.checkCompileLiquid(param);
        baseResponse.setData(rspCompileTask);
        log.info("end checkCompileLiquid useTime:{} result:{}",
            Duration.between(startTime, Instant.now()).toMillis(),
            JsonTools.toJSONString(baseResponse));

        return baseResponse;
    }
}

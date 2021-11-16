///**
// * Copyright 2014-2021 the original author or authors.
// * <p>
// * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
// * in compliance with the License. You may obtain a copy of the License at
// * <p>
// * http://www.apache.org/licenses/LICENSE-2.0
// * <p>
// * Unless required by applicable law or agreed to in writing, software distributed under the License
// * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
// * or implied. See the License for the specific language governing permissions and limitations under
// * the License.
// */
//
//package com.webank.webase.node.mgr.precompiled.permission;
//
//import com.webank.webase.node.mgr.base.code.ConstantCode;
//import com.webank.webase.node.mgr.base.controller.BaseController;
//import com.webank.webase.node.mgr.base.entity.BasePageResponse;
//import com.webank.webase.node.mgr.base.entity.BaseResponse;
//import com.webank.webase.node.mgr.base.enums.RequestType;
//import com.webank.webase.node.mgr.base.exception.NodeMgrException;
//import com.webank.webase.node.mgr.config.properties.ConstantProperties;
//import com.webank.webase.node.mgr.tools.JsonTools;
//import com.webank.webase.node.mgr.tools.pagetools.List2Page;
//import com.webank.webase.node.mgr.precompiled.entity.ChainGovernanceHandle;
//import com.webank.webase.node.mgr.precompiled.entity.AddressStatusHandle;
//import java.time.Duration;
//import java.time.Instant;
//import java.util.List;
//import java.util.Map;
//import javax.validation.Valid;
//import lombok.extern.log4j.Log4j2;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.validation.BindingResult;
//import org.springframework.web.bind.annotation.DeleteMapping;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.PutMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//
///**
// * permission manage above FISCO-BCOS v2.5.0
// */
//@Log4j2
//@RestController
//@RequestMapping("governance")
//public class ChainGovernController extends BaseController {
//    @Autowired
//    private ChainGovernService chainGovernService;
//
//    /**
//     * get permission manager paged list
//     * 透传front的BaseResponse
//     */
//    @GetMapping("committee/list")
//    public BasePageResponse listCommittee(
//        @RequestParam(defaultValue = "1") Integer groupId,
//        @RequestParam(defaultValue = "10") Integer pageSize,
//        @RequestParam(defaultValue = "1") Integer pageNumber) {
//
//        Instant startTime = Instant.now();
//        log.info("start listCommittee startTime:{}", startTime.toEpochMilli());
//        List resList = chainGovernService.listCommittee(groupId);
//        int totalCount = resList.size();
//        List2Page list2Page = new List2Page(resList, pageSize, pageNumber);
//        List finalList = list2Page.getPagedList();
//        log.info("end listPermissionManager useTime:{} finalList:{}",
//            Duration.between(startTime, Instant.now()).toMillis(), JsonTools.toJSONString(finalList));
//        return new BasePageResponse(ConstantCode.SUCCESS, finalList, totalCount);
//    }
//
////    @GetMapping("committee/list/sorted")
////    public BasePageResponse listCommitteeWithWeight(
////        @RequestParam(defaultValue = "1") Integer groupId,
////        @RequestParam(defaultValue = "10") Integer pageSize,
////        @RequestParam(defaultValue = "1") Integer pageNumber) {
////
////        Instant startTime = Instant.now();
////        log.info("start listCommitteeWithWeight startTime:{}", startTime.toEpochMilli());
////        List<RspCommitteeInfo> resList = chainGovernService.listCommitteeWithWeight(groupId);
////        int totalCount = resList.size();
////        List2Page list2Page = new List2Page(resList, pageSize, pageNumber);
////        List<RspCommitteeInfo> finalList = list2Page.getPagedList();
////        log.info("end listCommitteeWithWeight useTime:{} finalList:{}",
////            Duration.between(startTime, Instant.now()).toMillis(), JsonTools.toJSONString(finalList));
////        return new BasePageResponse(ConstantCode.SUCCESS, finalList, totalCount);
////    }
//
//
//    @PostMapping("committee")
//    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN)
//    public BaseResponse grantCommittee(@Valid @RequestBody ChainGovernanceHandle governanceHandle,
//        BindingResult result) throws NodeMgrException {
//        checkBindResult(result);
//        Instant startTime = Instant.now();
//        log.info("start grantCommittee startTime:{} governanceHandle:{}", startTime.toEpochMilli(),
//            JsonTools.toJSONString(governanceHandle));
//
//        Object res = chainGovernService.handleCommittee(governanceHandle, RequestType.POST);
//
//        log.info("end grantCommittee useTime:{} result:{}",
//            Duration.between(startTime, Instant.now()).toMillis(), JsonTools.toJSONString(res));
//
//        return new BaseResponse(ConstantCode.SUCCESS, res);
//    }
//
//    @DeleteMapping("committee")
//    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN)
//    public BaseResponse revokeCommittee(@Valid @RequestBody ChainGovernanceHandle governanceHandle,
//        BindingResult result) throws NodeMgrException {
//        checkBindResult(result);
//        Instant startTime = Instant.now();
//        log.info("start revokeCommittee startTime:{} governanceHandle:{}", startTime.toEpochMilli(),
//            JsonTools.toJSONString(governanceHandle));
//
//        Object res = chainGovernService.handleCommittee(governanceHandle, RequestType.DELETE);
//
//        log.info("end revokeCommittee useTime:{} result:{}",
//            Duration.between(startTime, Instant.now()).toMillis(), JsonTools.toJSONString(res));
//
//        return new BaseResponse(ConstantCode.SUCCESS, res);
//    }
//
//    @GetMapping("committee/weight")
//    public BaseResponse getCommitteeWeight(@RequestParam Integer groupId, @RequestParam String address) {
//
//        Instant startTime = Instant.now();
//        log.info("start getCommitteeWeight startTime:{}", startTime.toEpochMilli());
//        Integer res = chainGovernService.getCommitteeWeight(groupId, address);
//
//        log.info("end getCommitteeWeight useTime:{} result:{}",
//            Duration.between(startTime, Instant.now()).toMillis(), JsonTools.toJSONString(res));
//        return new BaseResponse(ConstantCode.SUCCESS, res);
//    }
//
//    @PostMapping("committee/weight/list")
//    public BaseResponse listCommitteeWeight(@Valid @RequestBody AddressStatusHandle addressStatusHandle,
//        BindingResult result) throws NodeMgrException {
//        checkBindResult(result);
//        Instant startTime = Instant.now();
//        log.info("start listCommitteeWeight startTime:{} addressStatusHandle:{}", startTime.toEpochMilli(),
//            JsonTools.toJSONString(addressStatusHandle));
//
//        Map<String, Object> res = chainGovernService.listCommitteeWeight(addressStatusHandle);
//
//        log.info("end listCommitteeWeight useTime:{} result:{}",
//            Duration.between(startTime, Instant.now()).toMillis(), JsonTools.toJSONString(res));
//
//        return new BaseResponse(ConstantCode.SUCCESS, res);
//    }
//
//    @PutMapping("committee/weight")
//    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN)
//    public BaseResponse updateCommitteeWeight(@Valid @RequestBody ChainGovernanceHandle governanceHandle,
//        BindingResult result) throws NodeMgrException {
//        checkBindResult(result);
//        Instant startTime = Instant.now();
//        log.info("start updateCommitteeWeight startTime:{} governanceHandle:{}", startTime.toEpochMilli(),
//            JsonTools.toJSONString(governanceHandle));
//
//        Object res = chainGovernService.updateCommitteeWeight(governanceHandle);
//
//        log.info("end updateCommitteeWeight useTime:{} result:{}",
//            Duration.between(startTime, Instant.now()).toMillis(), JsonTools.toJSONString(res));
//
//        return new BaseResponse(ConstantCode.SUCCESS, res);
//    }
//
//    @GetMapping("threshold")
//    public BaseResponse getThreshold(@RequestParam Integer groupId) {
//
//        Instant startTime = Instant.now();
//        log.info("start getThreshold startTime:{}", startTime.toEpochMilli());
//        Object res = chainGovernService.getThreshold(groupId);
//
//        log.info("end getThreshold useTime:{} result:{}",
//            Duration.between(startTime, Instant.now()).toMillis(), JsonTools.toJSONString(res));
//        return new BaseResponse(ConstantCode.SUCCESS, res);
//    }
//
//    @PutMapping("threshold")
//    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN)
//    public BaseResponse updateThreshold(@Valid @RequestBody ChainGovernanceHandle governanceHandle,
//        BindingResult result) throws NodeMgrException {
//        checkBindResult(result);
//        Instant startTime = Instant.now();
//        log.info("start updateThreshold startTime:{} governanceHandle:{}", startTime.toEpochMilli(),
//            JsonTools.toJSONString(governanceHandle));
//
//        Object res = chainGovernService.updateThreshold(governanceHandle);
//
//        log.info("end updateThreshold useTime:{} result:{}",
//            Duration.between(startTime, Instant.now()).toMillis(), JsonTools.toJSONString(res));
//
//        return new BaseResponse(ConstantCode.SUCCESS, res);
//    }
//
//
//    @GetMapping("operator/list")
//    public BasePageResponse listOperator(
//        @RequestParam(defaultValue = "1") Integer groupId,
//        @RequestParam(defaultValue = "10") Integer pageSize,
//        @RequestParam(defaultValue = "1") Integer pageNumber) {
//
//        Instant startTime = Instant.now();
//        log.info("start listOperator startTime:{}", startTime.toEpochMilli());
//        List resList = chainGovernService.listOperator(groupId);
//        int totalCount = resList.size();
//        List2Page list2Page = new List2Page(resList, pageSize, pageNumber);
//        List finalList = list2Page.getPagedList();
//        log.info("end listOperator useTime:{} finalList:{}",
//            Duration.between(startTime, Instant.now()).toMillis(), JsonTools.toJSONString(finalList));
//        return new BasePageResponse(ConstantCode.SUCCESS, finalList, totalCount);
//    }
//
//    @PostMapping("operator")
//    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN)
//    public BaseResponse grantOperator(@Valid @RequestBody ChainGovernanceHandle governanceHandle,
//        BindingResult result) throws NodeMgrException {
//        checkBindResult(result);
//        Instant startTime = Instant.now();
//        log.info("start grantOperator startTime:{} governanceHandle:{}", startTime.toEpochMilli(),
//            JsonTools.toJSONString(governanceHandle));
//
//        BaseResponse res = chainGovernService.handleOperator(governanceHandle, RequestType.POST);
//
//        log.info("end grantOperator useTime:{} result:{}",
//            Duration.between(startTime, Instant.now()).toMillis(), JsonTools.toJSONString(res));
//
//        return res;
//    }
//
//    @DeleteMapping("operator")
//    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN)
//    public BaseResponse revokeOperator(@Valid @RequestBody ChainGovernanceHandle governanceHandle,
//        BindingResult result) throws NodeMgrException {
//        checkBindResult(result);
//        Instant startTime = Instant.now();
//        log.info("start revokeOperator startTime:{} governanceHandle:{}", startTime.toEpochMilli(),
//            JsonTools.toJSONString(governanceHandle));
//
//        BaseResponse res = chainGovernService.handleOperator(governanceHandle, RequestType.DELETE);
//
//        log.info("end revokeOperator useTime:{} result:{}",
//            Duration.between(startTime, Instant.now()).toMillis(), JsonTools.toJSONString(res));
//
//        return res;
//    }
//
//    @GetMapping("account/status")
//    public BaseResponse getAccountStatus(@RequestParam Integer groupId, @RequestParam String address) {
//
//        Instant startTime = Instant.now();
//        log.info("start getAccountStatus startTime:{}", startTime.toEpochMilli());
//        Object res = chainGovernService.getAccountStatus(groupId, address);
//
//        log.info("end getAccountStatus useTime:{} result:{}",
//            Duration.between(startTime, Instant.now()).toMillis(), JsonTools.toJSONString(res));
//        return new BaseResponse(ConstantCode.SUCCESS, res);
//    }
//
//    @PostMapping("account/status/list")
//    public BaseResponse listAccountStatus(@RequestBody AddressStatusHandle addressStatusHandle) {
//
//        Instant startTime = Instant.now();
//        log.info("start getAccountStatus startTime:{}", startTime.toEpochMilli());
//        Map<String, String> res = chainGovernService.listAccountStatus(addressStatusHandle);
//
//        log.info("end getAccountStatus useTime:{} result:{}",
//            Duration.between(startTime, Instant.now()).toMillis(), JsonTools.toJSONString(res));
//        return new BaseResponse(ConstantCode.SUCCESS, res);
//    }
//
//    @PostMapping("account")
//    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN)
//    public BaseResponse freezeAccount(@Valid @RequestBody ChainGovernanceHandle governanceHandle,
//        BindingResult result) throws NodeMgrException {
//        checkBindResult(result);
//        Instant startTime = Instant.now();
//        log.info("start freezeAccount startTime:{} governanceHandle:{}", startTime.toEpochMilli(),
//            JsonTools.toJSONString(governanceHandle));
//
//        BaseResponse res = chainGovernService.handleAccountStatus(governanceHandle, RequestType.POST);
//
//        log.info("end freezeAccount useTime:{} result:{}",
//            Duration.between(startTime, Instant.now()).toMillis(), JsonTools.toJSONString(res));
//
//        return res;
//    }
//
//    @DeleteMapping("account")
//    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN)
//    public BaseResponse unfreezeAccount(@Valid @RequestBody ChainGovernanceHandle governanceHandle,
//        BindingResult result) throws NodeMgrException {
//        checkBindResult(result);
//        Instant startTime = Instant.now();
//        log.info("start unfreezeAccount startTime:{} governanceHandle:{}", startTime.toEpochMilli(),
//            JsonTools.toJSONString(governanceHandle));
//
//        BaseResponse res = chainGovernService.handleAccountStatus(governanceHandle, RequestType.DELETE);
//
//        log.info("end unfreezeAccount useTime:{} result:{}",
//            Duration.between(startTime, Instant.now()).toMillis(), JsonTools.toJSONString(res));
//
//        return res;
//    }
//
//
//}

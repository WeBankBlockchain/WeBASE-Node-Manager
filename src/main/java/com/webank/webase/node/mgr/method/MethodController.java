/**
 * Copyright 2014-2021  the original author or authors.
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
package com.webank.webase.node.mgr.method;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.qctc.common.log.annotation.Log;
import com.qctc.common.log.enums.BusinessType;
import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.controller.BaseController;
import com.webank.webase.node.mgr.base.entity.BaseResponse;
import com.webank.webase.node.mgr.base.enums.ContractType;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.config.properties.ConstantProperties;
import com.webank.webase.node.mgr.tools.JsonTools;
import com.webank.webase.node.mgr.method.entity.NewMethodInputParam;
import com.webank.webase.node.mgr.method.entity.TbMethod;
import com.webank.webase.node.mgr.tools.Web3Tools;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;
import org.fisco.bcos.sdk.v3.codec.wrapper.ABIDefinition;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name="合约方法管理")
@Log4j2
@RestController
@RequestMapping("method")
public class MethodController extends BaseController {

    @Autowired
    private MethodService methodService;

    /**
     * add method info.
     */
    @Log(title = "BCOS3/合约管理/合约方法", businessType = BusinessType.INSERT)
    @SaCheckPermission("bcos3:contract:addMethod")
    @PostMapping(value = "/add")
    public BaseResponse addMethod(@RequestBody @Valid NewMethodInputParam newMethodInputParam,
        BindingResult result) throws NodeMgrException {
        checkBindResult(result);
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start addMethod. startTime:{} newMethodInputParam:{}",
            startTime.toEpochMilli(), JsonTools.toJSONString(newMethodInputParam));

        methodService.saveMethod(newMethodInputParam, ContractType.GENERALCONTRACT.getValue());

        log.info("end addMethod. useTime:{} result:{}",
            Duration.between(startTime, Instant.now()).toMillis(),
            JsonTools.toJSONString(baseResponse));
        return baseResponse;
    }

    /**
     * query by methodId.
     */
    @SaCheckPermission("bcos3:contract:getMethod")
    @GetMapping(value = "findById/{groupId}/{methodId}")
    public BaseResponse getBymethodId(@PathVariable("groupId") String groupId,
        @PathVariable("methodId") String methodId) {
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start addMethodInfo. startTime:{} groupId:{} methodId:{}",
            startTime.toEpochMilli(), groupId, methodId);

        TbMethod tbMethod = methodService.getByMethodId(methodId, groupId);
        baseResponse.setData(tbMethod);

        log.info("end addMethodInfo. useTime:{} result:{}",
            Duration.between(startTime, Instant.now()).toMillis(),
            JsonTools.toJSONString(baseResponse));
        return baseResponse;
    }

    /**Below API For Developer**/
//    /**
//     * compute methodId.
//     */
//    @GetMapping(value = "computeMethodId")
//    public BaseResponse computeMethodId(String abiInfoStr, Integer integer)
//        throws IOException {
//        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
//        Instant startTime = Instant.now();
//        log.info("start computeMethodId. startTime:{}",
//            startTime.toEpochMilli());
//        List<ABIDefinition> abiDefinitionList = Web3Tools.loadContractDefinition(abiInfoStr);
//        List<Map<String, String>> resMethod = methodService.computeMethodId(abiDefinitionList,
//            integer);
//        baseResponse.setData(resMethod);
//
//        log.info("end computeMethodId. useTime:{} result:{}",
//            Duration.between(startTime, Instant.now()).toMillis(),
//            JsonTools.toJSONString(baseResponse));
//        return baseResponse;
//    }
//
//    /**
//     * get methodId dmlSql.
//     */
//    @GetMapping(value = "getMethodIdDmlSql")
//    public BaseResponse getMethodIdDmlSql(String abiInfoStr, Integer integer)
//        throws IOException {
//        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
//        Instant startTime = Instant.now();
//        log.info("start getMethodIdDmlSql. startTime:{}",
//            startTime.toEpochMilli());
//        List<ABIDefinition> abiDefinitionList = Web3Tools.loadContractDefinition(abiInfoStr);
//        ArrayList<String> resMethod = methodService.getMethodIdDmlSql(abiDefinitionList,
//            integer);
//        baseResponse.setData(resMethod);
//
//        log.info("end getMethodIdDmlSql. useTime:{} result:{}",
//            Duration.between(startTime, Instant.now()).toMillis(),
//            JsonTools.toJSONString(baseResponse));
//        return baseResponse;
//    }
}
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
package com.webank.webase.node.mgr.method;

import com.webank.webase.node.mgr.base.tools.JsonTools;
import com.webank.webase.node.mgr.base.controller.BaseController;
import com.webank.webase.node.mgr.base.entity.BaseResponse;
import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.base.properties.ConstantProperties;
import com.webank.webase.node.mgr.method.entity.NewMethodInputParam;
import com.webank.webase.node.mgr.method.entity.TbMethod;
import java.time.Duration;
import java.time.Instant;
import javax.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@RequestMapping("method")
public class MethodController extends BaseController {

    @Autowired
    private MethodService methodService;

    /**
     * add method info.
     */
    @PostMapping(value = "/add")
    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN)
    public BaseResponse addMethod(@RequestBody @Valid NewMethodInputParam newMethodInputParam,
        BindingResult result) throws NodeMgrException {
        checkBindResult(result);
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start addMethod. startTime:{} newMethodInputParam:{}",
            startTime.toEpochMilli(), JsonTools.toJSONString(newMethodInputParam));

        methodService.saveMethod(newMethodInputParam);

        log.info("end addMethod. useTime:{} result:{}",
            Duration.between(startTime, Instant.now()).toMillis(), JsonTools.toJSONString(baseResponse));
        return baseResponse;
    }

    /**
     * query by methodId.
     */
    @GetMapping(value = "findById/{groupId}/{methodId}")
    public BaseResponse getBymethodId(@PathVariable("groupId") Integer groupId,
        @PathVariable("methodId") String methodId) {
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start addMethodInfo. startTime:{} groupId:{} methodId:{}",
            startTime.toEpochMilli(), groupId, methodId);

        TbMethod tbMethod = methodService.getByMethodId(methodId, groupId);
        baseResponse.setData(tbMethod);

        log.info("end addMethodInfo. useTime:{} result:{}",
            Duration.between(startTime, Instant.now()).toMillis(), JsonTools.toJSONString(baseResponse));
        return baseResponse;
    }
}

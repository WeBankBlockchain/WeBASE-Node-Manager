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
package com.webank.webase.node.mgr.base.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.webase.node.mgr.base.entity.BaseResponse;
import com.webank.webase.node.mgr.base.entity.ConstantCode;
import com.webank.webase.node.mgr.base.entity.RetCode;
import java.util.Optional;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * catch an handler exception.
 */
@ControllerAdvice
@Log4j2
public class ExceptionsHandler {

    @Autowired
    ObjectMapper mapper;


    /**
     * catch：NodeMgrException.
     */
    @ResponseBody
    @ExceptionHandler(value = NodeMgrException.class)
    public BaseResponse myExceptionHandler(NodeMgrException nodeMgrException) throws Exception {
        log.warn("catch business exception", nodeMgrException);
        RetCode retCode = Optional.ofNullable(nodeMgrException).map(NodeMgrException::getRetCode)
            .orElse(ConstantCode.SYSTEM_EXCEPTION);

        BaseResponse bre = new BaseResponse(retCode);
        log.warn("business exception return:{}", mapper.writeValueAsString(bre));
        return bre;
    }


    /**
     * catch：Exception.
     */
    @ResponseBody
    @ExceptionHandler(value = Exception.class)
    public BaseResponse exceptionHandler(Exception exc) {
        log.info("catch exception", exc);
        // 默认系统异常
        RetCode retCode = ConstantCode.SYSTEM_EXCEPTION;

        BaseResponse bre = new BaseResponse(retCode);
        try {
            log.warn("system exception return:{}", mapper.writeValueAsString(bre));
        } catch (JsonProcessingException ex) {
            log.warn("system exception", ex);
        }

        return bre;
    }
}

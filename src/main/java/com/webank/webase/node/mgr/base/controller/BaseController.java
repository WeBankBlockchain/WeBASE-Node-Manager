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
package com.webank.webase.node.mgr.base.controller;

import com.alibaba.fastjson.JSON;
import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.exception.ParamException;
import java.util.Enumeration;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;

@Log4j2
public class BaseController {

    @Autowired
    protected HttpServletRequest request;


    /**
     * check param valid result.
     */
    protected void checkBindResult(BindingResult result) {
        if (result.hasErrors()) {
            log.error("param exception. error:{}", JSON.toJSONString(result.getAllErrors()));
            String errFieldStr = result.getAllErrors().stream()
                .map(obj -> JSON.parseObject(JSON.toJSONString(obj)))
                .map(err -> err.getString("field"))
                .collect(Collectors.joining(","));
            StringUtils.removeEnd(errFieldStr, ",");
            String message = "These fields do not match:" + errFieldStr;
            throw new ParamException(ConstantCode.PARAM_EXCEPTION.getCode(), message);
        }
    }

    /**
     * set session value.
     */
    protected void setSessionAttribute(String key, Object obj) {
        request.getSession().setAttribute(key, obj);
    }

    /**
     * get attribute from session.
     */
    protected Object getSessionAttribute(String key) {
        Object sessionValue = null;
        if (StringUtils.isNoneBlank(key)) {
            sessionValue = request.getSession().getAttribute(key);
        }
        return sessionValue;
    }

    /**
     * clear all session attribute.
     */
    protected void clearSession() {
        HttpSession session = request.getSession(true);
        Enumeration<?> enumeration = session.getAttributeNames();
        while (enumeration.hasMoreElements()) {
            String sessionName = (String) enumeration.nextElement();
            session.removeAttribute(sessionName);
        }
    }

    /**
     * clear by sessionName.
     */
    protected void clearSession(String clearName) {
        if (StringUtils.isNoneBlank(clearName)) {
            HttpSession session = request.getSession(true);
            Enumeration<?> enumeration = session.getAttributeNames();
            while (enumeration.hasMoreElements()) {
                String sessionName = (String) enumeration.nextElement();
                if (clearName.equals(sessionName)) {
                    session.removeAttribute(sessionName);
                }
            }
        } else {
            log.warn("fail clearSession，clearName empty");
        }
    }

}

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
package com.webank.webase.node.mgr.base.filter;

import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.base.properties.ConstantProperties;
import com.webank.webase.node.mgr.base.tools.HttpRequestTools;
import com.webank.webase.node.mgr.base.tools.JwtUtils;
import com.webank.webase.node.mgr.base.tools.NodeMgrTools;
import io.jsonwebtoken.Claims;
import java.io.IOException;
import java.util.Optional;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;


/**
 * validate code before login.
 */
@Log4j2
@Component
@Order(-1001)
@WebFilter(filterName = "validateCodeFilter", urlPatterns = "/*")
public class ValidateCodeFilter implements Filter {

    @Autowired
    private ConstantProperties constants;
    private static final String LOGIN_URI = "/account/login";
    private static final String LOGIN_METHOD = "post";

    /**
     * do filter.
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse rsp = (HttpServletResponse) response;
        String uri = HttpRequestTools.getUri(req);
        //is login
        if (LOGIN_URI.equalsIgnoreCase(uri) && LOGIN_METHOD.equalsIgnoreCase(req.getMethod())) {
            try {
                validateCode(req);
            } catch (NodeMgrException ex) {
                NodeMgrTools.responseRetCodeException(rsp, ex.getRetCode());
                return;
            }
        }
        chain.doFilter(request, response);
    }


    /**
     * validate code.
     */
    private void validateCode(HttpServletRequest request) {
        String tokenInHeard = request.getHeader("token");
        String codeInRequest = request.getParameter("checkCode");
        log.info("validateCode. tokenInHeard:{} codeInRequest:{}", tokenInHeard, codeInRequest);

        if (StringUtils.isBlank(codeInRequest)) {
            throw new NodeMgrException(ConstantCode.CHECK_CODE_NULL);
        }
        if (StringUtils.isBlank(tokenInHeard)) {
            throw new NodeMgrException(ConstantCode.INVALID_CHECK_CODE);
        }
        String realCheckCode;
        try {
            Claims claims = JwtUtils.parseJWT(tokenInHeard, constants.getJwtSecret());
            realCheckCode = Optional.ofNullable(claims).map(c -> c.getSubject())
                .orElseThrow(() -> new Exception(ConstantCode.INVALID_CHECK_CODE.getMsg()));
        } catch (Exception ex) {
            log.warn("fail validateCode.", ex);
            throw new NodeMgrException(ConstantCode.INVALID_CHECK_CODE);
        }

        if (StringUtils.isBlank(realCheckCode)) {
            log.warn("fail validateCode. realCheckCode is null");
            throw new NodeMgrException(ConstantCode.INVALID_CHECK_CODE);
        }
        if (!codeInRequest.equalsIgnoreCase(realCheckCode)) {
            log.warn("fail validateCode. realCheckCode:{} codeInRequest:{}", realCheckCode,
                codeInRequest);
        }
    }


}

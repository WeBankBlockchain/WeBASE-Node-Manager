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
package com.webank.webase.node.mgr.config.security.filter;

import com.webank.webase.node.mgr.account.token.TokenService;
import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.config.properties.ConstantProperties;
import com.webank.webase.node.mgr.tools.HttpRequestTools;
import com.webank.webase.node.mgr.tools.JsonTools;
import com.webank.webase.node.mgr.tools.NodeMgrTools;
import java.io.IOException;
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
 * verify code before login.
 */
@Log4j2
@Component
@Order(-999)
@WebFilter(filterName = "mailCheckCodeFilter", urlPatterns = "/*")
public class MailCheckCodeFilter implements Filter {
    @Autowired
    private TokenService tokenService;
    @Autowired
    private ConstantProperties constantProperties;

    private static final String REGISTER_URI = "/account/register";
    private static final String POST_METHOD = "post";

    /**
     * do filter.
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse rsp = (HttpServletResponse) response;
        String uri = HttpRequestTools.getUri(req);

        String tokenInHeard = req.getHeader("token");
        String codeInRequest = req.getParameter("checkCode");
        log.debug("validateMailCode. tokenInHeard:{} codeInRequest:{}", tokenInHeard, codeInRequest);

        // register接口，查验邮箱的验证码；如果启用了邮箱确认码
        if (constantProperties.getEnableRegisterMailCheck() &&
            REGISTER_URI.equalsIgnoreCase(uri) && POST_METHOD.equalsIgnoreCase(req.getMethod())) {
            try {
                validateMailCode(req);
            } catch (NodeMgrException ex) {
                NodeMgrTools.responseRetCodeException(rsp, ex.getRetCode());
                return;
            } finally {
                //remove token
                tokenService.deleteToken(req.getHeader("token"), null);
            }
        }
        chain.doFilter(request, response);
    }


    /**
     * verify code.
     */
    private void validateMailCode(HttpServletRequest request) {
        String tokenInHeard = request.getHeader("token");
        String codeInRequest = request.getParameter("checkCode");
        log.debug("validateMailCode. tokenInHeard:{} codeInRequest:{}", tokenInHeard, codeInRequest);

        if (StringUtils.isBlank(codeInRequest)) {
            throw new NodeMgrException(ConstantCode.CHECK_CODE_NULL);
        }
        if (StringUtils.isBlank(tokenInHeard)) {
            throw new NodeMgrException(ConstantCode.INVALID_CHECK_CODE);
        }
        String code = tokenService.getValueFromToken(tokenInHeard);
        if (!codeInRequest.equalsIgnoreCase(code)) {
            log.warn("fail validateMailCode. realCheckCode:{} codeInRequest:{}", code,
                codeInRequest);
            throw new NodeMgrException(ConstantCode.INVALID_CHECK_CODE);
        }
    }


}

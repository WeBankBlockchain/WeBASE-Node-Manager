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
package com.webank.webase.node.mgr.security;

import com.alibaba.fastjson.JSON;
import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.entity.BaseResponse;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.base.tools.NodeMgrTools;
import com.webank.webase.node.mgr.token.TokenService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@Log4j2
@Component
public class JsonLogoutSuccessHandler implements LogoutSuccessHandler {
    @Autowired
    private TokenService tokenService;
    private String TOKEN_HEADER_NAME = "Authorization";
    private String TOKEN_START = "Token";

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {

        //get token
        String token;
        try {
            token = this.getToken(request);
        } catch (NodeMgrException ex) {
            NodeMgrTools.responseString(response, JSON.toJSONString(ex.getRetCode()));
            return;
        }
        //remove token
        tokenService.deleteToken(token, null);

        log.debug("logout success");
        NodeMgrTools.responseString(response, JSON.toJSONString(ConstantCode.SUCCESS));
    }

    /**
     * get token.
     */
    private String getToken(HttpServletRequest request) {
        String header = request.getHeader(TOKEN_HEADER_NAME);
        if (StringUtils.isBlank(header)) {
            log.error("not found token");
            throw new NodeMgrException(ConstantCode.INVALID_TOKEN);
        }

        String token = StringUtils.removeStart(header, TOKEN_START).trim();
        if (StringUtils.isBlank(token)) {
            log.error("token is empty");
            throw new NodeMgrException(ConstantCode.INVALID_TOKEN);
        }
        return token;
    }

}

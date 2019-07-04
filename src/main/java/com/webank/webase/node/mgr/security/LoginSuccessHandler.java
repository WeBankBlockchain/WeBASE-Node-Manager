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
import com.webank.webase.node.mgr.account.AccountService;
import com.webank.webase.node.mgr.account.TbAccountInfo;
import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.entity.BaseResponse;
import com.webank.webase.node.mgr.base.properties.ConstantProperties;
import com.webank.webase.node.mgr.token.TokenService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Log4j2
@Component("loginSuccessHandler")
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private AccountService accountService;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private ConstantProperties properties;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {
        log.debug("login success");

        //   Object obj = authentication.getPrincipal();

        // JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(obj));
        //    String accountName = jsonObject.getString("username");
        String accountName = authentication.getName();
        String token = tokenService.createToken(accountName, LocalDateTime.now().plusSeconds(properties.getAuthTokenMaxAge()));

        // response account info
        TbAccountInfo accountInfo = accountService.queryByAccount(accountName);
        Map<String, Object> rsp = new HashMap<>();
        rsp.put("roleName", accountInfo.getRoleName());
        rsp.put("account", accountName);
        rsp.put("accountStatus", accountInfo.getAccountStatus());
        rsp.put("token", token);

        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        baseResponse.setData(rsp);

        String backStr = JSON.toJSONString(baseResponse);
        log.debug("login backInfo:{}", backStr);

        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(backStr);
    }

}

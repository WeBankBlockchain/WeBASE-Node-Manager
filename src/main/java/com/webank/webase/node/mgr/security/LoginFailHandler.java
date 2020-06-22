/**
 * Copyright 2014-2020 the original author or authors.
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
package com.webank.webase.node.mgr.security;

import com.webank.webase.node.mgr.base.tools.JsonTools;
import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.code.RetCode;
import com.webank.webase.node.mgr.base.entity.BaseResponse;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@Log4j2
@Component("loginFailHandler")
public class LoginFailHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException ex) throws IOException {
        log.info("login fail", ex);
        String errorMsg = ex.getMessage();
        RetCode retCode = ConstantCode.PASSWORD_ERROR; // default password fail
        if (errorMsg.contains("code")) {
            retCode = JsonTools.toJavaObject(errorMsg, RetCode.class);
        }

        BaseResponse baseResponse = new BaseResponse(retCode);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(JsonTools.toJSONString(baseResponse));
    }
}

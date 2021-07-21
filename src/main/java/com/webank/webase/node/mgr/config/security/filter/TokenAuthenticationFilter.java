/**
 * Copyright 2014-2021 the original author or authors.
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
package com.webank.webase.node.mgr.base.config.security.filter;

import com.webank.webase.node.mgr.base.config.security.customizeAuth.TokenAuthenticationToken;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;

/**
 * filter request with token
 */
public class TokenAuthenticationFilter extends AbstractAuthenticationFilter {
    public TokenAuthenticationFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager);
    }

    @Override
    protected String getHeaderPrefix() {
        return "Token ";
    }

    @Override
    protected AbstractAuthenticationToken buildAuthentication(String header) {
        String[] tokens = header.split(" ");
        return new TokenAuthenticationToken(tokens[1]);
    }
}
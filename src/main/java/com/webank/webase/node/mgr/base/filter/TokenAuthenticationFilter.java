package com.webank.webase.node.mgr.base.filter;

import com.webank.webase.node.mgr.security.customizeAuth.TokenAuthenticationToken;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;

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
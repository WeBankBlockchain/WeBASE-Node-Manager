package com.webank.webase.node.mgr.security.customizeAuth;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class TokenAuthenticationToken extends AbstractAuthenticationToken {
    private static final long serialVersionUID = 6062547771857401517L;

    private final String principal;

    public TokenAuthenticationToken(String token) {
        super(null);
        this.principal = token;
    }

    public TokenAuthenticationToken(String principal, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }
}
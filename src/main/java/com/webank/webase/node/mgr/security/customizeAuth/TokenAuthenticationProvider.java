package com.webank.webase.node.mgr.security.customizeAuth;

import com.webank.webase.node.mgr.account.AccountService;
import com.webank.webase.node.mgr.account.TbAccountInfo;
import com.webank.webase.node.mgr.base.properties.ConstantProperties;
import com.webank.webase.node.mgr.token.TokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

public class TokenAuthenticationProvider implements AuthenticationProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(TokenAuthenticationProvider.class);

    @Autowired
    private TokenService tokenService;
    @Autowired
    private ConstantProperties properties;

    @Autowired
    private AccountService accountService;
  /*  private AuthenticationManager authenticationManager;

    public TokenAuthenticationProvider(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }*/

    @Override
    @Transactional(noRollbackFor = BadCredentialsException.class)
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        AbstractAuthenticationToken result = null;
        String token = authentication.getName();
        String account = tokenService.getValueFromToken(token);
        tokenService.updateExpireTime(token, LocalDateTime.now().plusSeconds(properties.getAuthTokenMaxAge()));

        if (null == account) {
            throw new BadCredentialsException("Invalid token");
        }
        result = buildAuthentication(account);
        result.setDetails(authentication.getDetails());
        return result;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return TokenAuthenticationToken.class.isAssignableFrom(authentication);
    }

//    private TokenAuthenticationToken buildAuthentication(User user) {
//        return new TokenAuthenticationToken(user.getId(), buildAuthorities(user));
//    }

/*    private Collection<SimpleGrantedAuthority> buildAuthorities(TbAccountInfo user) {
        final Collection<SimpleGrantedAuthority> simpleGrantedAuthorities = new ArrayList<>();
        List<String> authorities = getUserAuthorities(user);
        for (String authority : authorities) {
            simpleGrantedAuthorities.add(new SimpleGrantedAuthority(authority));
        }
        return simpleGrantedAuthorities;
    }*/

    private AbstractAuthenticationToken buildAuthentication(String account) {
        TbAccountInfo tbAccountInfo = accountService.queryByAccount(account);
        LOGGER.info(tbAccountInfo + "****" + tbAccountInfo.getAccount());
        return new TokenAuthenticationToken(tbAccountInfo.getAccount(), null);
    }


//    private List<String> getUserAuthorities(TbAccountInfo user) {
//
//        return Arrays.asList(user.getRoleName());
//    }
}
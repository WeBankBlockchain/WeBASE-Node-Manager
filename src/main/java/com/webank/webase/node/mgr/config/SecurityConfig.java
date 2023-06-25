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
package com.webank.webase.node.mgr.config;

import com.webank.webase.node.mgr.config.properties.ConstantProperties;
import com.webank.webase.node.mgr.config.security.AccountDetailsService;
import com.webank.webase.node.mgr.config.security.JsonAccessDeniedHandler;
import com.webank.webase.node.mgr.config.security.JsonLogoutSuccessHandler;
import com.webank.webase.node.mgr.config.security.LoginFailHandler;
import com.webank.webase.node.mgr.config.security.customizeAuth.TokenAuthenticationProvider;
import com.webank.webase.node.mgr.config.security.filter.TokenAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.webank.webase.node.mgr.config.security.JsonAuthenticationEntryPoint;

/**
 * security config.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private AccountDetailsService userDetailService;
    @Qualifier(value = "loginSuccessHandler")
    @Autowired
    private AuthenticationSuccessHandler loginSuccessHandler;
    @Qualifier(value = "loginFailHandler")
    @Autowired
    private LoginFailHandler loginfailHandler;
    @Autowired
    private JsonAuthenticationEntryPoint jsonAuthenticationEntryPoint;
    @Autowired
    private JsonAccessDeniedHandler jsonAccessDeniedHandler;
    @Autowired
    private JsonLogoutSuccessHandler jsonLogoutSuccessHandler;
    @Autowired
    private ConstantProperties constants;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // 无权访问 JSON 格式的数据
        http.exceptionHandling().accessDeniedHandler(jsonAccessDeniedHandler);

//        // login page
        http.formLogin().loginPage("/login")
            .loginProcessingUrl("/account/login") // login request uri
            .usernameParameter("account").passwordParameter("accountPwd").permitAll()
            .successHandler(loginSuccessHandler) // if login success
            .failureHandler(loginfailHandler) // if login fail
            .and().authorizeRequests()
            .antMatchers(constants.getPermitUrlArray())
            .permitAll()
            .anyRequest().authenticated()
            .and().csrf()
            .disable() // close csrf
            .addFilterBefore(new TokenAuthenticationFilter(authenticationManager()), BasicAuthenticationFilter.class)
            .httpBasic().authenticationEntryPoint(jsonAuthenticationEntryPoint).and().logout()
            .logoutUrl("/account/logout")
            .logoutSuccessHandler(jsonLogoutSuccessHandler)
            .permitAll();
    }


    @Override
    public void configure(WebSecurity web) throws Exception {
        super.configure(web);
        if (!constants.getIsUseSecurity()) {
            web.ignoring().antMatchers("/**");
        }
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(userAuthenticationProvider());
        auth.authenticationProvider(tokenAuthenticationProvider());
    }

    @Bean("bCryptPasswordEncoder")
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public AuthenticationProvider tokenAuthenticationProvider() {
        return new TokenAuthenticationProvider();
    }
    
    @Bean
    public DaoAuthenticationProvider userAuthenticationProvider(){
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(userDetailService);
        daoAuthenticationProvider.setHideUserNotFoundExceptions(false);
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        return daoAuthenticationProvider;
    }
}

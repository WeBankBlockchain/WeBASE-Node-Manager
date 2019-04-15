/**
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
package com.webank.webase.node.mgr.base.config;

import com.webank.webase.node.mgr.base.enums.RoleType;
import com.webank.webase.node.mgr.base.properties.ConstantProperties;
import com.webank.webase.node.mgr.security.AccountDetailsService;
import com.webank.webase.node.mgr.security.JsonAccessDeniedHandler;
import com.webank.webase.node.mgr.security.JsonAuthenticationEntryPoint;
import com.webank.webase.node.mgr.security.JsonLogoutSuccessHandler;
import com.webank.webase.node.mgr.security.LoginFailHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

/**
 * security config.
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
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

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        String adminRolePermit = String.format("hasRole('%1s')", RoleType.ADMIN.getValue());
        http.exceptionHandling().accessDeniedHandler(jsonAccessDeniedHandler); // 无权访问 JSON 格式的数据

        http.formLogin().loginPage("/login") // login page
            .loginProcessingUrl("/account/login") // login request uri
            .usernameParameter("account").passwordParameter("accountPwd").permitAll()
            .successHandler(loginSuccessHandler) // if login success
            .failureHandler(loginfailHandler) // if login fail
            .and().authorizeRequests()
            .antMatchers("/js/**", "/account/login", "/login", "/report/**", "/user/privateKey/**")
            .permitAll()
            .antMatchers(HttpMethod.POST, "/account/accountInfo").access(adminRolePermit)
            .antMatchers(HttpMethod.PUT, "/account/accountInfo").access(adminRolePermit)
            .antMatchers(HttpMethod.DELETE, "/account/**").access(adminRolePermit)
            .antMatchers("/account/accountList/**").access(adminRolePermit)
            .antMatchers("/role/roleList").access(adminRolePermit)
            .anyRequest().authenticated().and().csrf()
            .disable() // close csrf
            .httpBasic().authenticationEntryPoint(jsonAuthenticationEntryPoint).and().logout()
            .logoutUrl("/account/logout")
            .deleteCookies(ConstantProperties.COOKIE_JSESSIONID,
                ConstantProperties.COOKIE_MGR_ACCOUNT)
            .logoutSuccessHandler(jsonLogoutSuccessHandler)
            .permitAll();

            //http.authorizeRequests().anyRequest().permitAll();

    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        super.configure(web);
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailService).passwordEncoder(passwordEncoder());
    }

    @Bean("bCryptPasswordEncoder")
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

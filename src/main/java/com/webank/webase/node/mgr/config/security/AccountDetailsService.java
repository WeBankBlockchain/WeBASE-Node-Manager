///**
// * Copyright 2014-2021  the original author or authors.
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *      http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package com.webank.webase.node.mgr.config.security;
//
//import com.webank.webase.node.mgr.account.AccountService;
//import com.webank.webase.node.mgr.account.entity.TbAccountInfo;
//import com.webank.webase.node.mgr.base.code.ConstantCode;
//import com.webank.webase.node.mgr.base.exception.NodeMgrException;
//import com.webank.webase.node.mgr.tools.JsonTools;
//import java.util.ArrayList;
//import java.util.List;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Lazy;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.userdetails.User;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.stereotype.Service;
//
///**
// * config acccount and role.
// * /login
// */
//@Service
//public class AccountDetailsService implements UserDetailsService {
//
//    @Autowired
//    @Lazy
//    private AccountService accountService;
//
//    @Override
//    public UserDetails loadUserByUsername(String account) throws UsernameNotFoundException {
//        // query account
//        TbAccountInfo accountRow = null;
//        try {
//            accountRow = accountService.queryByAccount(account);
//            // todo 记录登录失败次数  是否在这里调用login函数
////            int loginFailTime = accountRow.getLoginFailTime() + 1;
////            log.info("fail login. pwd error,loginFailTime:{}", loginFailTime);
////            accountRow.setLoginFailTime(loginFailTime);
////            accountMapper.updateAccountRow(accountRow);
//            accountService.validateAccount(accountRow);
//        } catch (Exception e) {
//            if (e instanceof NodeMgrException) {
//                throw new UsernameNotFoundException(JsonTools.toJSONString(((NodeMgrException) e).getRetCode()));
//            } else {
//                throw new UsernameNotFoundException(JsonTools.toJSONString(ConstantCode.DB_EXCEPTION));
//            }
//        }
//        if (null == accountRow) {
//            throw new UsernameNotFoundException(JsonTools.toJSONString(ConstantCode.INVALID_ACCOUNT_NAME));
//        }
//
//        // add role
//        List<GrantedAuthority> list = new ArrayList<GrantedAuthority>();
//        list.add(new SimpleGrantedAuthority("ROLE_" + accountRow.getRoleName()));
//
//        User authUser = new User(account, accountRow.getAccountPwd(), list);
//        return authUser;
//    }
//}

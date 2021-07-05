/**
 * Copyright 2014-2021 the original author or authors.
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
package com.webank.webase.node.mgr.lite.config.filter;

import com.webank.webase.node.mgr.lite.account.AccountService;
import com.webank.webase.node.mgr.lite.account.entity.TbAccountInfo;
import com.webank.webase.node.mgr.lite.base.annotation.entity.CurrentAccountInfo;
import com.webank.webase.node.mgr.lite.base.enums.RoleType;
import com.webank.webase.node.mgr.lite.config.properties.ConstantProperties;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * validate account info.
 */
@Component
public class AccountFilter implements HandlerInterceptor {

    @Autowired
    private AccountService accountService;
    @Autowired
    private ConstantProperties constants;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
            Object handler) throws Exception {
        // 获取账户信息
        String account = accountService.getCurrentAccount(request);
        // if disabled security
        if (!constants.getIsUseSecurity()) {
            // 设置账户信息
            CurrentAccountInfo currentAccountInfo = new CurrentAccountInfo();
            currentAccountInfo.setAccount(account);
            currentAccountInfo.setRoleId(RoleType.ADMIN.getValue());
            request.setAttribute("currentAccountInfo", currentAccountInfo);
            return true;
        } else {
            TbAccountInfo accountRow = accountService.queryByAccount(account);
            // 设置账户信息
            CurrentAccountInfo currentAccountInfo = new CurrentAccountInfo();
            currentAccountInfo.setAccount(account);
            currentAccountInfo.setRoleId(accountRow.getRoleId());
            request.setAttribute("currentAccountInfo", currentAccountInfo);
            return true;
        }
    }

}

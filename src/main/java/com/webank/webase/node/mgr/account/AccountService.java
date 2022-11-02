/**
 * Copyright 2014-2021  the original author or authors.
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
package com.webank.webase.node.mgr.account;

import com.webank.webase.node.mgr.account.entity.ReqDeveloperRegister;
import com.webank.webase.node.mgr.account.entity.RspDeveloper;
import com.webank.webase.node.mgr.base.enums.RoleType;
import com.webank.webase.node.mgr.config.properties.ConstantProperties;
import com.webank.webase.node.mgr.tools.JsonTools;
import com.webank.webase.node.mgr.tools.NodeMgrTools;
import com.webank.webase.node.mgr.account.entity.AccountInfo;
import com.webank.webase.node.mgr.account.entity.AccountListParam;
import com.webank.webase.node.mgr.account.entity.LoginInfo;
import com.webank.webase.node.mgr.account.entity.TbAccountInfo;
import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.enums.AccountStatus;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.account.role.RoleService;
import com.webank.webase.node.mgr.account.token.TokenService;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * services for account data.
 */
@Log4j2
@Service
public class AccountService {

    @Autowired
    private AccountMapper accountMapper;
    @Autowired
    private RoleService roleService;
    @Qualifier(value = "bCryptPasswordEncoder")
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private ConstantProperties constants;
    private static final String ADMIN_TOKEN_VALUE = "admin";
    @Autowired
    private MessageService messageService;

    /**
     * login.
     */
    public TbAccountInfo login(LoginInfo loginInfo) throws NodeMgrException {
        log.info("start login. loginInfo:{}", JsonTools.toJSONString(loginInfo));
        String accountStr = loginInfo.getAccount();
        String passwordStr = loginInfo.getAccountPwd();

        // check account
        accountExist(accountStr);

        // check pwd
        if (StringUtils.isBlank(passwordStr)) {
            log.info("fail login. passwordStr is null");
            throw new NodeMgrException(ConstantCode.PASSWORD_ERROR);
        }
        // encode by bCryptPasswordEncoder
        TbAccountInfo accountRow = accountMapper.queryByAccount(accountStr);
        if (!passwordEncoder.matches(passwordStr, accountRow.getAccountPwd())) {
            // reset login fail time
            int loginFailTime = accountRow.getLoginFailTime() + 1;
            log.info("fail login. pwd error,loginFailTime:{}", loginFailTime);
            accountRow.setLoginFailTime(loginFailTime);
            accountMapper.updateAccountRow(accountRow);
            throw new NodeMgrException(ConstantCode.PASSWORD_ERROR);
        }

        return accountRow;
    }

    /**
     * add account row.
     */
    public void addAccountRow(AccountInfo accountInfo) throws NodeMgrException {
        String accountStr = accountInfo.getAccount();
        Integer roleId = accountInfo.getRoleId();
        String email = accountInfo.getEmail();
        // check account
        accountNotExist(accountStr);
        // check role id
        roleService.roleIdExist(roleId);
        // encode password
        String encryptStr = passwordEncoder.encode(accountInfo.getAccountPwd());
        // add account row
        TbAccountInfo rowInfo = new TbAccountInfo(accountStr, encryptStr, roleId, null, email);
        Integer affectRow = accountMapper.addAccountRow(rowInfo);

        // check result
        checkDbAffectRow(affectRow);

        log.debug("end addAccountRow. affectRow:{}", affectRow);
    }

    /**
     * update account info.
     */
    public void updateAccountRow(String currentAccount, AccountInfo accountInfo)
        throws NodeMgrException {
        String accountStr = accountInfo.getAccount();
        // check account
        accountExist(accountStr);

        // query by account
        TbAccountInfo accountRow = accountMapper.queryByAccount(accountStr);

        // encode password
        if (StringUtils.isNoneBlank(accountInfo.getAccountPwd())) {
            String encryptStr = passwordEncoder.encode(accountInfo.getAccountPwd());
            accountRow.setAccountPwd(encryptStr);
            // the current user is admin
            if (!currentAccount.equals(accountStr)) {
                accountRow.setAccountStatus(AccountStatus.UNMODIFIEDPWD.getValue());
            }
        }
        accountRow.setRoleId(accountInfo.getRoleId());
        accountRow.setEmail(accountInfo.getEmail());

        // update account info
        Integer affectRow = accountMapper.updateAccountRow(accountRow);

        // check result
        checkDbAffectRow(affectRow);

        log.debug("end updateAccountRow. affectRow:{}", affectRow);
    }

    /**
     * update password.
     */
    public void updatePassword(String targetAccount, String oldAccountPwd, String newAccountPwd)
        throws NodeMgrException {
        log.debug("start updatePassword. targetAccount:{} oldAccountPwd:{} newAccountPwd:{}",
            targetAccount, oldAccountPwd, newAccountPwd);

        // query target account info
        TbAccountInfo targetRow = accountMapper.queryByAccount(targetAccount);
        if (targetRow == null) {
            log.warn("fail updatePassword. not found target row. targetAccount:{}",
                targetAccount);
            throw new NodeMgrException(ConstantCode.ACCOUNT_NOT_EXISTS);
        }

        if (StringUtils.equals(oldAccountPwd, newAccountPwd)) {
            log.warn("fail updatePassword. the new pwd cannot be same as old ");
            throw new NodeMgrException(ConstantCode.NOW_PWD_EQUALS_OLD);
        }

        // check old password
        if (!passwordEncoder.matches(oldAccountPwd, targetRow.getAccountPwd())) {
            throw new NodeMgrException(ConstantCode.PASSWORD_ERROR);
        }

        // update password
        targetRow.setAccountPwd(passwordEncoder.encode(newAccountPwd));
        targetRow.setAccountStatus(AccountStatus.NORMAL.getValue());
        Integer affectRow = accountMapper.updateAccountRow(targetRow);

        // check result
        checkDbAffectRow(affectRow);

        log.debug("end updatePassword. affectRow:{}", affectRow);

    }

    /**
     * query account info by accountName.
     */
    public TbAccountInfo queryByAccount(String accountStr) {
        log.debug("start queryByAccount. accountStr:{} ", accountStr);
        TbAccountInfo accountRow = accountMapper.queryByAccount(accountStr);
        log.debug("end queryByAccount. accountRow:{} ", JsonTools.toJSONString(accountRow));
        return accountRow;
    }

    /**
     * query count of account.
     */
    public int countOfAccount(String account) {
        Integer accountCount = accountMapper.countOfAccount(account);
        int count = accountCount == null ? 0 : accountCount.intValue();
        return count;
    }

    /**
     * query account list.
     */
    public List<TbAccountInfo> listOfAccount(AccountListParam param) {
        log.debug("start listOfAccount. param:{} ", JsonTools.toJSONString(param));
        List<TbAccountInfo> list = accountMapper.listOfAccount(param);
        log.debug("end listOfAccount. list:{} ", JsonTools.toJSONString(list));
        return list;
    }

    /**
     * delete account info.
     */
    public void deleteAccountRow(String account) throws NodeMgrException {
        // check account
        accountExist(account);

        // delete account row
        Integer affectRow = accountMapper.deleteAccountRow(account);

        // check result
        checkDbAffectRow(affectRow);

        log.debug("end deleteAccountRow. affectRow:{} ", affectRow);

    }


    /**
     * boolean account is exist.
     */
    public void accountExist(String account) throws NodeMgrException {
        if (StringUtils.isBlank(account)) {
            throw new NodeMgrException(ConstantCode.ACCOUNT_NAME_EMPTY);
        }
        int count = countOfAccount(account);
        if (count == 0) {
            throw new NodeMgrException(ConstantCode.ACCOUNT_NOT_EXISTS);
        }
    }

    /**
     * boolean account is not exit.
     */
    public void accountNotExist(String account) throws NodeMgrException {
        if (StringUtils.isBlank(account)) {
            throw new NodeMgrException(ConstantCode.ACCOUNT_NAME_EMPTY);
        }
        int count = countOfAccount(account);
        if (count > 0) {
            throw new NodeMgrException(ConstantCode.ACCOUNT_EXISTS);
        }
    }

    /**
     * check db affect row.
     */
    private void checkDbAffectRow(Integer affectRow) throws NodeMgrException {
        if (affectRow == 0) {
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }
    }
    
    /**
     * get current account.
     * whether use security:
     * @case1: get account info from request's header, such as account of admin001
     * @case2: get account from token, use token to get account, such as 0x001 to get its account of admin001
     */
    public String getCurrentAccount(HttpServletRequest request) {
        if (!constants.getIsUseSecurity()) {
            String accountDefault = NodeMgrTools.getAccount(request);
            if (StringUtils.isNotBlank(accountDefault)) {
                log.debug("getCurrentAccount default [{}]", accountDefault);
                return accountDefault;
            } else {
                // default return admin
                log.debug("getCurrentAccount not found account, default admin");
                return ADMIN_TOKEN_VALUE;
            }
        }
        String token = NodeMgrTools.getToken(request);
        return tokenService.getValueFromToken(token);
    }

    /**
     * register.
     */
    @Transactional
    public RspDeveloper register(ReqDeveloperRegister param) throws NodeMgrException {
        log.info("start exec method [register]. param:{}", JsonTools.objToString(param));

        String accountStr = param.getAccount();
        Integer roleId = param.getRoleId();
        String email = param.getEmail();
        // check account
        accountNotExist(accountStr);
        // check role id
        if (!roleId.equals(RoleType.DEVELOPER.getValue())) {
            log.error("only support developer register");
            throw new NodeMgrException(ConstantCode.INVALID_ROLE_ID);
        }
        roleService.roleIdExist(roleId);
        // encode password
        String encryptStr = passwordEncoder.encode(param.getAccountPwd());

        TbAccountInfo tbDeveloper = new TbAccountInfo(accountStr, encryptStr, roleId, "new register user", email);
        tbDeveloper.setRoleId(RoleType.DEVELOPER.getValue());
        tbDeveloper.setAccountStatus(AccountStatus.FROZEN.getValue());
        tbDeveloper.setExpireTime(LocalDateTime.now().plusYears(1L));

        tbDeveloper.setEmail(param.getEmail());
        tbDeveloper.setCompanyName(param.getCompanyName());
        tbDeveloper.setContactAddress(param.getContactAddress());
        tbDeveloper.setIdCardNumber(param.getIdCardNumber());
        tbDeveloper.setRealName(param.getRealName());
        tbDeveloper.setMobile(param.getMobile());

        //save developer
        Integer affectRow = accountMapper.registerAccount(tbDeveloper);


        // todo send email or mobile message
//        messageService.sendMail();

        log.info("success exec method [register] row:{}", affectRow);
        TbAccountInfo tbAccountInfo = queryByAccount(tbDeveloper.getAccount());
        RspDeveloper rspDeveloper = new RspDeveloper();
        BeanUtils.copyProperties(tbAccountInfo, rspDeveloper);
        return rspDeveloper;
    }


    /**
     * @param accountStr
     */
    public void freeze(String currentAccount, String accountStr, String description) {
        log.info("start exec method [freeze]. accountStr:{} description:{}", accountStr, description);
        TbAccountInfo developer = queryByAccount(accountStr);
        if (Objects.isNull(developer)) {
            log.warn("start exec method [freeze]. not found record by id:{}", accountStr);
            throw new NodeMgrException(ConstantCode.INVALID_ACCOUNT_NAME);
        }
        developer.setAccountStatus(AccountStatus.FROZEN.getValue());
        developer.setDescription(description);
        updateAccountInfo(currentAccount, developer);
    }

    /**
     * @param accountStr
     */
    public void unfreeze(String currentAccount, String accountStr, String description) {
        log.info("start exec method [freeze]. accountStr:{} description:{}", accountStr, description);
        TbAccountInfo developer = queryByAccount(accountStr);
        if (Objects.isNull(developer)) {
            log.warn("start exec method [freeze]. not found record by id:{}", accountStr);
            throw new NodeMgrException(ConstantCode.INVALID_ACCOUNT_NAME);
        }
        developer.setAccountStatus(AccountStatus.NORMAL.getValue());
        developer.setDescription(description);
        updateAccountInfo(currentAccount, developer);
    }

    /**
     * 注销用户
     * @param accountStr
     */
    public void cancel(String currentAccount, String accountStr) {
        log.info("start exec method [freeze]. accountStr:{}", accountStr);
        TbAccountInfo developer = queryByAccount(accountStr);
        if (Objects.isNull(developer)) {
            log.warn("start exec method [freeze]. not found record by id:{}", accountStr);
            throw new NodeMgrException(ConstantCode.INVALID_ACCOUNT_NAME);
        }
        developer.setAccountStatus(AccountStatus.FROZEN.getValue());
        updateAccountInfo(currentAccount, developer);
    }

    /**
     * update account info. 更新详细信息、状态等
     */
    private void updateAccountInfo(String currentAccount, TbAccountInfo accountInfo)
        throws NodeMgrException {

        // todo check currentAccount is self or manager

        // update account info
        Integer affectRow = accountMapper.updateAccountRow(accountInfo);

        // check result
        checkDbAffectRow(affectRow);

        log.debug("end updateAccountRow. affectRow:{}", affectRow);
    }

}

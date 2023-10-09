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
import com.webank.webase.node.mgr.account.entity.ReqUpdateInfo;
import com.webank.webase.node.mgr.account.entity.RspDeveloper;
import com.webank.webase.node.mgr.base.enums.RoleType;
import com.webank.webase.node.mgr.config.properties.ConstantProperties;
import com.webank.webase.node.mgr.tools.AesUtils;
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
import com.webank.webase.node.mgr.user.UserService;
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
import org.springframework.context.annotation.Lazy;
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
    @Lazy
    private PasswordEncoder passwordEncoder;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private ConstantProperties constants;
    private static final String ADMIN_TOKEN_VALUE = "admin";
    @Autowired
    private MessageService messageService;

    @Lazy
    @Autowired
    private UserService userService;

    /**
     * login.
     * 检测密码、检测账号状态（非冻结）
     * login逻辑在AccountDetailService中
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
        TbAccountInfo accountRow = this.queryByAccount(accountStr);
        validateAccount(accountRow);

        if (!passwordEncoder.matches(passwordStr, accountRow.getAccountPwd())) {
            // reset login fail time
            int loginFailTime = accountRow.getLoginFailTime() + 1;
            log.info("fail login. pwd error,loginFailTime:{}", loginFailTime);
            accountRow.setLoginFailTime(loginFailTime);
            this.updateAccountRowEncrypted(accountRow);
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
        // 默认一年后过期
        rowInfo.setExpireTime(LocalDateTime.now().plusYears(1L));

        // 加密身份证和电话
        this.encryptAccountInfo(rowInfo);

        Integer affectRow = accountMapper.addAccountRow(rowInfo);

        // check result
        checkDbAffectRow(affectRow);

        log.debug("end addAccountRow. affectRow:{}", affectRow);
    }

    /**
     * update account info.
     * 检查curentAccount
     */
    public void updateAccountVo(String currentAccount, ReqUpdateInfo accountInfo)
        throws NodeMgrException {
        String accountStr = accountInfo.getAccount();

        // check currentAccount is self or manager
        if (!haveAccess2Update(currentAccount, accountStr)) {
            log.error("end updateAccountVo. denied update status:{}|{}", currentAccount, accountStr);
            throw new NodeMgrException(ConstantCode.UPDATE_ACCOUNT_STATUS_DENIED);
        }

        // 需要校验是否修改了，否则不修改
        // 电话和身份证号前端传来包含*号，意味着未修改
        String mobile = null;
        String idCardNumber = null;
        if (accountInfo.getMobile() != null && !accountInfo.getMobile().contains("*")) {
            mobile = accountInfo.getMobile();
        }
        if (accountInfo.getIdCardNumber() != null && !accountInfo.getIdCardNumber().contains("*")) {
            idCardNumber = accountInfo.getIdCardNumber();
        }
        log.info("updateAccountVo mobile:{}, idCardNumber:{}", mobile, idCardNumber);

        // check account
        accountExist(accountStr);

        // query by account
        // skip valid
        TbAccountInfo accountRow = this.queryByAccount(accountStr);

        // encode password
        if (StringUtils.isNoneBlank(accountInfo.getAccountPwd())) {
            String encryptStr = passwordEncoder.encode(accountInfo.getAccountPwd());
            accountRow.setAccountPwd(encryptStr);
            // the current user is admin
            if (!currentAccount.equals(accountStr)) {
                accountRow.setAccountStatus(AccountStatus.UNMODIFIEDPWD.getValue());
            }
        }
        if (accountInfo.getRoleId() != null) {
            accountRow.setRoleId(accountInfo.getRoleId());
        }
        // db的非空邮箱不可修改
        if (StringUtils.isNotBlank(accountInfo.getEmail())) {
            log.warn("db's email is not empty, not modify as:{}|{}", accountRow.getEmail(), accountInfo.getEmail());
            accountRow.setEmail(accountInfo.getEmail());
        }
        accountRow.setContactAddress(accountInfo.getContactAddress());
        accountRow.setCompanyName(accountInfo.getCompanyName());

        // status 只能在freeze或者cancel修改

        // check mobile if exist
        if (StringUtils.isNotBlank(mobile) && !mobile.equals(accountRow.getMobile())) {
            mobileNotExist(mobile);
            accountRow.setMobile(mobile);
        }

        accountRow.setRealName(accountInfo.getRealName());
        accountRow.setIdCardNumber(idCardNumber);
        accountRow.setDescription(accountInfo.getDescription());

        if (accountInfo.getExpandYear() != null) {
            LocalDateTime newExpiredTime;
            if (accountRow.getExpireTime() == null) {
                newExpiredTime = LocalDateTime.now().plusYears(accountInfo.getExpandYear());
            } else {
                newExpiredTime = accountRow.getExpireTime().plusYears(accountInfo.getExpandYear());
            }
            log.info("updateAccountVo newExpiredTime {}", newExpiredTime);
            accountRow.setExpireTime(newExpiredTime);
        }

        // update account info
        Integer affectRow = this.updateAccountRowEncrypted(accountRow);

        // check result
        checkDbAffectRow(affectRow);

        log.info("end updateAccountVo. affectRow:{}", affectRow);
    }

    /**
     * 包含加密
     * @param tbAccountInfo
     * @return
     */
    private Integer updateAccountRowEncrypted(TbAccountInfo tbAccountInfo) {
        // 加密身份证和电话
        this.encryptAccountInfo(tbAccountInfo);
        // update account info
        Integer affectRow = accountMapper.updateAccountRow(tbAccountInfo);
        log.info("end updateAccountRow. affectRow:{}", affectRow);
        return affectRow;
    }

    /**
     * update password.
     */
    public void updatePassword(String targetAccount, String oldAccountPwd, String newAccountPwd)
        throws NodeMgrException {
        log.debug("start updatePassword. targetAccount:{} oldAccountPwd:{} newAccountPwd:{}",
            targetAccount, oldAccountPwd, newAccountPwd);

        // query target account info
        TbAccountInfo targetRow = this.queryByAccount(targetAccount);
        if (targetRow == null) {
            log.warn("fail updatePassword. not found target row. targetAccount:{}",
                targetAccount);
            throw new NodeMgrException(ConstantCode.ACCOUNT_NOT_EXISTS);
        }
        validateAccount(targetRow);

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
        Integer affectRow = this.updateAccountRowEncrypted(targetRow);

        // check result
        checkDbAffectRow(affectRow);

        log.debug("end updatePassword. affectRow:{}", affectRow);

    }

    public TbAccountInfo queryAccountDetail(String currentAccount, String accountStr) {
        // check currentAccount is self or manager
        if (!haveAccess2Update(currentAccount, accountStr)) {
            log.error("end queryAccountDetail. denied update status:{}|{}", currentAccount, accountStr);
            throw new NodeMgrException(ConstantCode.UPDATE_ACCOUNT_STATUS_DENIED);
        }

        TbAccountInfo tbAccountInfo = queryByAccount(accountStr);
        if (tbAccountInfo == null) {
            throw new NodeMgrException(ConstantCode.ACCOUNT_NOT_EXISTS);
        }
        validateAccount(tbAccountInfo);
        log.debug("end queryAccountDetail. accountRow:{} ", JsonTools.toJSONString(tbAccountInfo));
        return tbAccountInfo;
    }

    /**
     * query account info by accountName.
     * 返回解密后的所有信息
     */
    public TbAccountInfo queryByAccount(String accountStr) {
        log.debug("start queryByAccount. accountStr:{} ", accountStr);
        TbAccountInfo accountRow = accountMapper.queryByAccount(accountStr);
        this.decryptAccountInfo(accountRow);
        log.debug("end queryByAccount. accountRow:{} ", JsonTools.toJSONString(accountRow));
        return accountRow;
    }

    public void validateAccount(TbAccountInfo tbAccountInfo) {
        log.info("validateAccount {}", tbAccountInfo);
        if (AccountStatus.FROZEN.getValue() == tbAccountInfo.getAccountStatus()
            || AccountStatus.CANCEL.getValue() == tbAccountInfo.getAccountStatus()) {
            log.error("account is invalid status {}|{}", tbAccountInfo.getAccountStatus(),
                tbAccountInfo.getAccount());
            throw new NodeMgrException(ConstantCode.ACCOUNT_DISABLED);
        }
        if (tbAccountInfo.getExpireTime() != null && LocalDateTime.now().isAfter(tbAccountInfo.getExpireTime())) {
            log.error("account is beyond expired time {}", tbAccountInfo.getAccount());
            throw new NodeMgrException(ConstantCode.ACCOUNT_DISABLED);
        }
    }

    /**
     * query count of account.
     */
    public int countOfAccount(String account) {
        Integer accountCount = accountMapper.countOfAccount(account);
        int count = accountCount == null ? 0 : accountCount;
        return count;
    }

    /**
     * query count of account.
     */
    public int countOfMobile(String mobile) {
        Integer accountCount = accountMapper.countOfMobile(mobile);
        int count = accountCount == null ? 0 : accountCount;
        return count;
    }

    /**
     * query count of account.
     */
    public int countOfAccountAvailable(String account) {
        Integer accountCount = accountMapper.countOfAccountAvailable(account);
        int count = accountCount == null ? 0 : accountCount;
        return count;
    }

    /**
     * query account list.
     */
    public List<TbAccountInfo> listOfAccount(AccountListParam param) {
        log.debug("start listOfAccount. param:{} ", JsonTools.toJSONString(param));
        List<TbAccountInfo> list = accountMapper.listOfAccount(param);
        list.forEach(this::decryptAccountInfo);
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

    private void mobileExist(String mobile){
        if (StringUtils.isNotBlank(mobile)) {
            int count = this.countOfMobile(mobile);
            if (count == 0) {
                throw new NodeMgrException(ConstantCode.ACCOUNT_MOBILE_NOT_EXISTS);
            }
        } else {
            throw new NodeMgrException(ConstantCode.ACCOUNT_MOBILE_IS_EMPTY);
        }
    }


    private void mobileNotExist(String mobile){
        if (StringUtils.isNotBlank(mobile)) {
            int count = this.countOfMobile(mobile);
            if (count > 0) {
                throw new NodeMgrException(ConstantCode.ACCOUNT_MOBILE_EXISTS);
            }
        } else {
            throw new NodeMgrException(ConstantCode.ACCOUNT_MOBILE_IS_EMPTY);
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
    public TbAccountInfo register(ReqDeveloperRegister param) throws NodeMgrException {
        log.info("start exec method [register]. param:{}", JsonTools.objToString(param));

        String accountStr = param.getAccount();
        Integer roleId = param.getRoleId();
        String email = param.getEmail();
        String mobile = param.getMobile() == null ? "" : String.valueOf(param.getMobile());

        // check account
        accountNotExist(accountStr);
        // check mobile unique and must not empty
        mobileNotExist(mobile);

        // check role id
        if (!roleId.equals(RoleType.DEVELOPER.getValue()) &&
            !roleId.equals(RoleType.VISITOR.getValue())) {
            log.error("only support developer/visitor register");
            throw new NodeMgrException(ConstantCode.INVALID_ROLE_ID_REGISTER);
        }
        roleService.roleIdExist(roleId);
        // encode password
        String encryptStr = passwordEncoder.encode(param.getAccountPwd());

        TbAccountInfo tbDeveloper = new TbAccountInfo(accountStr, encryptStr, roleId, "new register user", email);
        tbDeveloper.setRoleId(roleId);
        tbDeveloper.setAccountStatus(AccountStatus.FROZEN.getValue());
        tbDeveloper.setExpireTime(LocalDateTime.now().plusYears(1L));

        tbDeveloper.setEmail(param.getEmail());
        tbDeveloper.setCompanyName(param.getCompanyName());
        tbDeveloper.setContactAddress(param.getContactAddress());
        tbDeveloper.setIdCardNumber(param.getIdCardNumber());
        tbDeveloper.setRealName(param.getRealName());
        tbDeveloper.setMobile(mobile);

        // 加密身份证和电话
        this.encryptAccountInfo(tbDeveloper);

        //save developer
        Integer affectRow = accountMapper.registerAccount(tbDeveloper);


        log.info("success exec method [register] row:{}", affectRow);
        TbAccountInfo tbAccountInfo = this.queryByAccount(tbDeveloper.getAccount());

        return tbAccountInfo;
    }


    /**
     * @param accountStr
     */
    public TbAccountInfo freeze(String currentAccount, String accountStr, String description) {
        log.info("start exec method [freeze]. accountStr:{} description:{}", accountStr, description);
        TbAccountInfo developer = this.queryByAccount(accountStr);
        if (Objects.isNull(developer)) {
            log.warn("start exec method [freeze]. not found record by id:{}", accountStr);
            throw new NodeMgrException(ConstantCode.INVALID_ACCOUNT_NAME);
        }
        developer.setAccountStatus(AccountStatus.FROZEN.getValue());
        developer.setDescription(description);
        updateAccountStatus(currentAccount, developer);


        return developer;
    }

    /**
     * @param accountStr
     */
    public TbAccountInfo unfreeze(String currentAccount, String accountStr, String description) {
        log.info("start exec method [freeze]. accountStr:{} description:{}", accountStr, description);
        TbAccountInfo developer = this.queryByAccount(accountStr);
        if (Objects.isNull(developer)) {
            log.warn("start exec method [freeze]. not found record by id:{}", accountStr);
            throw new NodeMgrException(ConstantCode.INVALID_ACCOUNT_NAME);
        }
        developer.setAccountStatus(AccountStatus.NORMAL.getValue());
        developer.setDescription(description);
        updateAccountStatus(currentAccount, developer);


        return developer;
    }

    /**
     * 注销用户
     * @param accountStr
     */
    public void cancel(String currentAccount, String accountStr) {
        log.info("start exec method [freeze]. accountStr:{}", accountStr);
        TbAccountInfo developer = this.queryByAccount(accountStr);
        if (Objects.isNull(developer)) {
            log.warn("start exec method [freeze]. not found record by id:{}", accountStr);
            throw new NodeMgrException(ConstantCode.INVALID_ACCOUNT_NAME);
        }
        developer.setAccountStatus(AccountStatus.CANCEL.getValue());
        updateAccountStatus(currentAccount, developer);

        // todo 获取链上管理员地址，发起冻结操作
        // 直接在本地db suspend这个私钥
        int result = userService.suspendUserByAccountInfo(accountStr);
        log.info("suspend user private key in local, result:{}", result);

    }

    /**
     * update account info. 更新详细信息、状态等
     * 自己修改或者管理员修改
     */
    private void updateAccountStatus(String currentAccount, TbAccountInfo accountInfo)
        throws NodeMgrException {

        // check currentAccount is self or manager
        if (!haveAccess2Update(currentAccount, accountInfo.getAccount())) {
            log.error("end updateAccountStatus. denied update status:{}|{}", currentAccount, accountInfo.getAccount());
            throw new NodeMgrException(ConstantCode.UPDATE_ACCOUNT_STATUS_DENIED);
        }
        // update account info
        Integer affectRow = this.updateAccountRowEncrypted(accountInfo);
        // check result
        checkDbAffectRow(affectRow);

        log.info("end updateAccountStatus. affectRow:{}", affectRow);
    }

    public String loadPrivacyDoc() {
        String privacyFilePath = "templates/privacy_doc.txt";
        String privacyDoc = NodeMgrTools.loadFileContent(privacyFilePath);
        if (StringUtils.isBlank(privacyDoc)) {
            throw new NodeMgrException(ConstantCode.GET_PRIVACY_DOC_FAILED);
        }
        String result = NodeMgrTools.encodedBase64Str(privacyDoc);
        return result;
    }

    private void encryptAccountInfo(TbAccountInfo tbAccountInfo) {
        if (StringUtils.isNotBlank(tbAccountInfo.getIdCardNumber())) {
            String encrypted = AesUtils.encrypt(tbAccountInfo.getIdCardNumber(), constants.getAccountInfoAesKey());
            log.debug("getIdCardNumber:{}, encrypted {}", tbAccountInfo.getIdCardNumber(), encrypted);
            tbAccountInfo.setIdCardNumber(encrypted);
        }
        if (StringUtils.isNotBlank(tbAccountInfo.getRealName())) {
            String encrypted = AesUtils.encrypt(tbAccountInfo.getRealName(), constants.getAccountInfoAesKey());
            log.debug("getRealName:{}, encrypted {}", tbAccountInfo.getRealName(), encrypted);
            tbAccountInfo.setRealName(encrypted);
        }
    }

    private void decryptAccountInfo(TbAccountInfo tbAccountInfo) {
        if (StringUtils.isNotBlank(tbAccountInfo.getIdCardNumber())) {
            String rawContent = AesUtils.decrypt(tbAccountInfo.getIdCardNumber(), constants.getAccountInfoAesKey());
            log.debug("getIdCardNumber:{}, encrypted {}", tbAccountInfo.getIdCardNumber(), rawContent);
            tbAccountInfo.setIdCardNumber(rawContent);
        }
        if (StringUtils.isNotBlank(tbAccountInfo.getRealName())) {
            String rawContent = AesUtils.decrypt(tbAccountInfo.getRealName(), constants.getAccountInfoAesKey());
            log.debug("getRealName:{}, rawContent {}", tbAccountInfo.getRealName(), rawContent);
            tbAccountInfo.setRealName(rawContent);
        }
    }

    public static void hideAccountInfo(TbAccountInfo tbAccountInfo) {
        if (StringUtils.isNotBlank(tbAccountInfo.getIdCardNumber())) {
            String hided = tbAccountInfo.getIdCardNumber().replaceAll("(\\d{4})\\d{10}(\\w{4})", "$1*****$2");
            log.info("hideAccountInfo id card hided {}", hided);
            tbAccountInfo.setIdCardNumber(hided);
        }
        if (StringUtils.isNotBlank(tbAccountInfo.getMobile())) {
            String hided = tbAccountInfo.getMobile().replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
            log.info("hideAccountInfo mobile hided {}", hided);
            tbAccountInfo.setMobile(hided);
        }
        if (StringUtils.isNotBlank(tbAccountInfo.getRealName())) {
            String hided;
            if (NodeMgrTools.isLetterDigit(tbAccountInfo.getRealName())) {
                hided = tbAccountInfo.getRealName().substring(0,2) + "*";
            } else {
                hided = tbAccountInfo.getRealName().substring(0,1) + "*";
            }
            log.info("hideAccountInfo real name hided {}", hided);
            tbAccountInfo.setRealName(hided);
        }
        tbAccountInfo.setAccountPwd(null);
    }

    private boolean haveAccess2Update(String currentAccount, String accountTarget) {
        // check currentAccount is self or manager
        TbAccountInfo checkAdmin = this.queryByAccount(currentAccount);
        return currentAccount.equals(accountTarget) || checkAdmin.getRoleId().equals(RoleType.ADMIN.getValue());
    }
}

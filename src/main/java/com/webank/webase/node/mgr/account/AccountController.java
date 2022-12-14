
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
package com.webank.webase.node.mgr.account;

import com.webank.webase.node.mgr.account.entity.AccountInfo;
import com.webank.webase.node.mgr.account.entity.AccountListParam;
import com.webank.webase.node.mgr.account.entity.ImageToken;
import com.webank.webase.node.mgr.account.entity.PasswordInfo;
import com.webank.webase.node.mgr.account.entity.ReqCancel;
import com.webank.webase.node.mgr.account.entity.ReqDeveloperRegister;
import com.webank.webase.node.mgr.account.entity.ReqFreeze;
import com.webank.webase.node.mgr.account.entity.ReqSendMail;
import com.webank.webase.node.mgr.account.entity.ReqUpdateInfo;
import com.webank.webase.node.mgr.account.entity.RspDeveloper;
import com.webank.webase.node.mgr.account.entity.TbAccountInfo;
import com.webank.webase.node.mgr.account.token.TokenService;
import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.controller.BaseController;
import com.webank.webase.node.mgr.base.entity.BasePageResponse;
import com.webank.webase.node.mgr.base.entity.BaseResponse;
import com.webank.webase.node.mgr.base.enums.SqlSortType;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.config.properties.ConstantProperties;
import com.webank.webase.node.mgr.tools.JsonTools;
import com.webank.webase.node.mgr.tools.NodeMgrTools;
import com.webank.webase.node.mgr.tools.TokenImgGenerator;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@RequestMapping(value = "account")
public class AccountController extends BaseController {

    @Autowired
    private AccountService accountService;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private ConstantProperties constants;
    @Autowired
    private MessageService messageService;

    private static final int PICTURE_CHECK_CODE_CHAR_NUMBER = 4;

    /**
     * get verify code when login
     */
    @GetMapping(value = "pictureCheckCode")
    public BaseResponse getPictureCheckCode() throws Exception {
        Instant startTime = Instant.now();
        log.info("start getPictureCheckCode startTime:{}", startTime);
        // random code
        String checkCode;
        if (constants.getEnableVerificationCode()) {
            checkCode = NodeMgrTools.randomString(PICTURE_CHECK_CODE_CHAR_NUMBER);
        } else {
            checkCode = constants.getVerificationCodeValue();
            log.debug("getPictureCheckCode: already disabled check code, and default value is {}", checkCode);
        }

        String token = tokenService.createToken(checkCode, 2);
        log.info("new checkCode:" + checkCode);

        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        try {
            // 得到图形验证码并返回给页面
            String base64Image = TokenImgGenerator.getBase64Image(checkCode);
            ImageToken tokenData = new ImageToken();
            tokenData.setToken(token);
            tokenData.setBase64Image(base64Image);
            baseResponse.setData(tokenData);
            log.info("end getPictureCheckCode useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(), JsonTools.toJSONString(baseResponse));
            return baseResponse;
        } catch (IOException e) {
            log.error("fail getPictureCheckCode:[]", e);
            throw new NodeMgrException(ConstantCode.CREATE_CHECK_CODE_FAIL);
        }
    }


    /**
     * add account info.
     */
    @PostMapping(value = "/accountInfo")
    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN)
    public BaseResponse addAccountInfo(@RequestBody @Valid AccountInfo info, BindingResult result)
        throws NodeMgrException {
        checkBindResult(result);
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start addAccountInfo. startTime:{}", startTime.toEpochMilli());

        // add account row
        accountService.addAccountRow(info);

        // query row
        TbAccountInfo tbAccount = accountService.queryByAccount(info.getAccount());
        AccountService.hideAccountInfo(tbAccount);
        baseResponse.setData(tbAccount);

        log.info("end addAccountInfo useTime:{} result:{}",
            Duration.between(startTime, Instant.now()).toMillis(), JsonTools.toJSONString(baseResponse));
        return baseResponse;
    }

    /**
     * query account info.
     */
    @GetMapping(value = "/accountInfo")
    public BaseResponse queryAccountDetail(HttpServletRequest request,
        @RequestParam(value = "account", required = false) String account) throws NodeMgrException {
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start queryAccountDetail. startTime:{}", startTime.toEpochMilli());

        // current
        String currentAccount = accountService.getCurrentAccount(request);
        if (StringUtils.isBlank(account)) {
            account = currentAccount;
        }
        // add account row
        TbAccountInfo tbAccount = accountService.queryAccountDetail(currentAccount, account);

//        AccountService.hideAccountInfo(tbAccount);  获取单个，不做隐藏

        RspDeveloper rspDeveloper = new RspDeveloper();
        BeanUtils.copyProperties(tbAccount, rspDeveloper);
        baseResponse.setData(rspDeveloper);

        log.info("end queryAccountDetail useTime:{} result:{}",
            Duration.between(startTime, Instant.now()).toMillis(), JsonTools.toJSONString(baseResponse));
        return baseResponse;
    }

    /**
     * update account info.
     */
    @PutMapping(value = "/accountInfo")
    public BaseResponse updateAccountInfo(@RequestBody @Valid ReqUpdateInfo info, HttpServletRequest request,
        BindingResult result) throws NodeMgrException {
        checkBindResult(result);
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start updateAccountInfo startTime:{}, info:{}", startTime.toEpochMilli(), info);

        // current
        String currentAccount = accountService.getCurrentAccount(request);

        // update account row
        accountService.updateAccountVo(currentAccount, info);

        // query row
        TbAccountInfo tbAccount = accountService.queryByAccount(info.getAccount());
        AccountService.hideAccountInfo(tbAccount);

        baseResponse.setData(tbAccount);

        log.info("end updateAccountInfo useTime:{} result:{}",
            Duration.between(startTime, Instant.now()).toMillis(),
            JsonTools.toJSONString(baseResponse));
        return baseResponse;
    }

    /**
     * query account list.
     */
    @GetMapping(value = "/accountList/{pageNumber}/{pageSize}")
    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN)
    public BasePageResponse queryAccountList(@PathVariable("pageNumber") Integer pageNumber,
        @PathVariable("pageSize") Integer pageSize,
        @RequestParam(value = "account", required = false) String account) throws NodeMgrException {
        BasePageResponse pageResponse = new BasePageResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start queryAccountList.  startTime:{} pageNumber:{} pageSize:{}",
            startTime.toEpochMilli(), pageNumber, pageSize);

        int count = accountService.countOfAccountAvailable(account);
        if (count > 0) {
            Integer start = Optional.ofNullable(pageNumber).map(page -> (page - 1) * pageSize)
                .orElse(0);
            AccountListParam param = new AccountListParam(start, pageSize, account,
                SqlSortType.DESC.getValue());
            List<TbAccountInfo> listOfAccount = accountService.listOfAccount(param);
            listOfAccount.forEach(AccountService::hideAccountInfo);
            pageResponse.setData(listOfAccount);
            pageResponse.setTotalCount(count);
        }

        log.info("end queryAccountList useTime:{} result:{}",
            Duration.between(startTime, Instant.now()).toMillis(), JsonTools.toJSONString(pageResponse));
        return pageResponse;
    }

    /**
     * delete contract by id.
     */
    @DeleteMapping(value = "/{account}")
    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN)
    public BaseResponse deleteAccount(HttpServletRequest request, @PathVariable("account") String account)
        throws NodeMgrException {
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();

//        accountService.deleteAccountRow(account);
        String currentAccount = accountService.getCurrentAccount(request);

        accountService.cancel(currentAccount, account);

        log.info("end deleteAccount. useTime:{} result:{}",
            Duration.between(startTime, Instant.now()).toMillis(), JsonTools.toJSONString(baseResponse));
        return baseResponse;
    }

    /**
     * update password.
     */
    @PutMapping(value = "/passwordUpdate")
    public BaseResponse updatePassword(@RequestBody @Valid PasswordInfo info, HttpServletRequest request, 
            BindingResult result) throws NodeMgrException {
        checkBindResult(result);
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();

        String targetAccount = accountService.getCurrentAccount(request);

        // update account row
        accountService
            .updatePassword(targetAccount, info.getOldAccountPwd(), info.getNewAccountPwd());

        log.info("end updatePassword useTime:{} result:{}",
            Duration.between(startTime, Instant.now()).toMillis(), JsonTools.toJSONString(baseResponse));
        return baseResponse;
    }


    @GetMapping(value = "/mail/enable")
    public BaseResponse ifEnableMailCheckCode() {
        log.info("start exec method [ifEnableMailCheckCode]. ");
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        baseResponse.setData(constants.getEnableRegisterMailCheck());

        log.info("success exec method [ifEnableMailCheckCode]. result:{}", JsonTools.objToString(baseResponse));
        return baseResponse;
    }

    @PostMapping(value = "/mail")
    public BaseResponse sendCheckCodeMail(@RequestBody @Valid ReqSendMail param, BindingResult result) {
        log.info("start exec method [sendCheckCodeMail]. param:{}", JsonTools.objToString(param));
        checkBindResult(result);
        Instant startTime = Instant.now();
        log.info("start getPictureCheckCode startTime:{}", startTime);
        // random code
        String checkCode;
        if (constants.getEnableVerificationCode()) {
            checkCode = NodeMgrTools.randomString(PICTURE_CHECK_CODE_CHAR_NUMBER);
        } else {
            checkCode = constants.getVerificationCodeValue();
            log.info("getPictureCheckCode: already disabled check code, and default value is {}", checkCode);
        }

        String token = tokenService.createToken(checkCode, 2);
        log.info("new checkCode:" + checkCode);

        messageService.sendMail(param.getMailAddress(), checkCode);

        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        ImageToken tokenData = new ImageToken();
        tokenData.setToken(token);
        baseResponse.setData(tokenData);
        log.info("end getPictureCheckCode useTime:{} result:{}",
            Duration.between(startTime, Instant.now()).toMillis(), JsonTools.toJSONString(baseResponse));
        log.info("success exec method [sendCheckCodeMail]. result:{}", JsonTools.objToString(baseResponse));
        return baseResponse;
    }


    /**
     * developer register
     * 未登录也可以调用
     * @param param
     * @param result
     * @return
     */
    @PostMapping(value = "/register")
    public BaseResponse register(@RequestBody @Valid ReqDeveloperRegister param, BindingResult result) {
        log.info("start exec method [register]. param:{}", JsonTools.objToString(param));
        checkBindResult(result);
        TbAccountInfo tbAccountInfo = accountService.register(param);

        AccountService.hideAccountInfo(tbAccountInfo);
        RspDeveloper rspDeveloper = new RspDeveloper();
        BeanUtils.copyProperties(tbAccountInfo, rspDeveloper);

        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        baseResponse.setData(rspDeveloper);
        log.info("success exec method [register]. result:{}", JsonTools.objToString(baseResponse));
        return baseResponse;
    }



    @PatchMapping(value = "freeze")
    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN)
    public BaseResponse freeze(@RequestBody @Valid ReqFreeze param, HttpServletRequest request, BindingResult result) {
        log.info("start exec method [freeze]. param:{}", JsonTools.objToString(param));
        checkBindResult(result);

        // current
        String currentAccount = accountService.getCurrentAccount(request);

        TbAccountInfo tbAccountInfo = accountService.freeze(currentAccount, param.getAccount(), param.getDescription());

        AccountService.hideAccountInfo(tbAccountInfo);
        RspDeveloper rspDeveloper = new RspDeveloper();
        BeanUtils.copyProperties(tbAccountInfo, rspDeveloper);
        log.info("success exec method [freeze] rspDeveloper:{}", rspDeveloper);
        return new BaseResponse(ConstantCode.SUCCESS, rspDeveloper);
    }


    @PatchMapping(value = "unFreeze")
    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN)
    public BaseResponse unfreeze(@RequestBody @Valid ReqFreeze param, HttpServletRequest request, BindingResult result) {
        log.info("start exec method [unfreeze]. param:{}", JsonTools.objToString(param));
        checkBindResult(result);

        // current
        String currentAccount = accountService.getCurrentAccount(request);

        TbAccountInfo tbAccountInfo = accountService.unfreeze(currentAccount, param.getAccount(), param.getDescription());

        AccountService.hideAccountInfo(tbAccountInfo);
        RspDeveloper rspDeveloper = new RspDeveloper();
        BeanUtils.copyProperties(tbAccountInfo, rspDeveloper);

        log.info("success exec method [unfreeze] rspDeveloper{}", rspDeveloper);
        return new BaseResponse(ConstantCode.SUCCESS, rspDeveloper);
    }

    /**
     * 注销用户
     * @param param
     * @param request
     * @param result
     * @return
     */
    @DeleteMapping(value = "cancel")
    public BaseResponse cancel(@RequestBody @Valid ReqCancel param, HttpServletRequest request, BindingResult result) {
        log.info("start exec method [cancel]. param:{}", JsonTools.objToString(param));
        checkBindResult(result);

        // current
        String currentAccount = accountService.getCurrentAccount(request);

        accountService.cancel(currentAccount, param.getAccount());
        log.info("success exec method [cancel]");
        return new BaseResponse(ConstantCode.SUCCESS);
    }

    @GetMapping(value = "/privacy")
    public BaseResponse getPrivacyDoc() {
        log.info("start exec method [getPrivacyDoc]. ");

        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        String doc = accountService.loadPrivacyDoc();
        baseResponse.setData(doc);

        log.info("success exec method [getPrivacyDoc]. result:{}", JsonTools.objToString(baseResponse));
        return baseResponse;
    }

}


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
package com.webank.webase.node.mgr.account;

import com.alibaba.fastjson.JSON;
import com.webank.webase.node.mgr.base.controller.BaseController;
import com.webank.webase.node.mgr.base.entity.BasePageResponse;
import com.webank.webase.node.mgr.base.entity.BaseResponse;
import com.webank.webase.node.mgr.base.entity.ConstantCode;
import com.webank.webase.node.mgr.base.enums.SqlSortType;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.base.properties.ConstantProperties;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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


    /**
     * add account info.
     */
    @PostMapping(value = "/accountInfo")
    public BaseResponse addAccountInfo(@RequestBody AccountInfo info) throws NodeMgrException {
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start addAccountInfo. startTime:{} accountInfo:{}", startTime.toEpochMilli(),
            JSON.toJSONString(info));

        // add account row
        accountService.addAccountRow(info);

        // query row
        TbAccountInfo tbAccount = accountService.queryByAccount(info.getAccount());
        tbAccount.setAccountPwd(null);
        baseResponse.setData(tbAccount);

        log.info("end addAccountInfo useTime:{} result:{}",
            Duration.between(startTime, Instant.now()).toMillis(), JSON.toJSONString(baseResponse));
        return baseResponse;
    }

    /**
     * update account info.
     */
    @PutMapping(value = "/accountInfo")
    public BaseResponse updateAccountInfo(@RequestBody AccountInfo info) throws NodeMgrException {
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start updateAccountInfo startTime:{} accountInfo:{}", startTime.toEpochMilli(),
            JSON.toJSONString(info));

        // current
        String currentAccount = (String) getSessionAttribute(
            ConstantProperties.SESSION_MGR_ACCOUNT);

        // update account row
        accountService.updateAccountRow(currentAccount, info);

        // query row
        TbAccountInfo tbAccount = accountService.queryByAccount(info.getAccount());
        tbAccount.setAccountPwd(null);
        baseResponse.setData(tbAccount);

        log.info("end updateAccountInfo useTime:{} result:{}",
            Duration.between(startTime, Instant.now()).toMillis(),
            JSON.toJSONString(baseResponse));
        return baseResponse;
    }

    /**
     * query account list.
     */
    @GetMapping(value = "/accountList/{pageNumber}/{pageSize}")
    public BasePageResponse queryAccountList(@PathVariable("pageNumber") Integer pageNumber,
        @PathVariable("pageSize") Integer pageSize,
        @RequestParam(value = "account", required = false) String account) throws NodeMgrException {
        BasePageResponse pagesponse = new BasePageResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start queryAccountList.  startTime:{} pageNumber:{} pageSize:{} account:{} ",
            startTime.toEpochMilli(), pageNumber, pageSize,
            account);

        int count = accountService.countOfAccount(account);
        if (count > 0) {
            Integer start = Optional.ofNullable(pageNumber).map(page -> (page - 1) * pageSize)
                .orElse(0);
            AccountListParam param = new AccountListParam(start, pageSize, account,
                SqlSortType.DESC.getValue());
            List<TbAccountInfo> listOfAccount = accountService.listOfAccount(param);
            listOfAccount.stream().forEach(accountData -> accountData.setAccountPwd(null));
            pagesponse.setData(listOfAccount);
            pagesponse.setTotalCount(count);
        }

        log.info("end queryAccountList useTime:{} result:{}",
            Duration.between(startTime, Instant.now()).toMillis(), JSON.toJSONString(pagesponse));
        return pagesponse;
    }

    /**
     * delete contract by id.
     */
    @DeleteMapping(value = "/{account}")
    public BaseResponse deleteAccount(@PathVariable("account") String account)
        throws NodeMgrException {
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start deleteAccount. startTime:{} account:{}", startTime.toEpochMilli(), account);

        accountService.deleteAccountRow(account);

        log.info("end deleteAccount. useTime:{} result:{}",
            Duration.between(startTime, Instant.now()).toMillis(), JSON.toJSONString(baseResponse));
        return baseResponse;
    }

    /**
     * update password.
     */
    @PutMapping(value = "/passwordUpdate")
    public BaseResponse updatePassword(@RequestBody PasswordInfo info) throws NodeMgrException {
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start updatePassword startTime:{} passwordInfo:{}", startTime.toEpochMilli(),
            JSON.toJSONString(info));

        String targetAccount = (String) getSessionAttribute(ConstantProperties.SESSION_MGR_ACCOUNT);

        // update account row
        accountService
            .updatePassword(targetAccount, info.getOldAccountPwd(), info.getNewAccountPwd());

        log.info("end updatePassword useTime:{} result:{}",
            Duration.between(startTime, Instant.now()).toMillis(), JSON.toJSONString(baseResponse));
        return baseResponse;
    }
}

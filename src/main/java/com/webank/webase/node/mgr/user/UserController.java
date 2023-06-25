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
package com.webank.webase.node.mgr.user;

import com.webank.webase.node.mgr.base.annotation.CurrentAccount;
import com.webank.webase.node.mgr.base.annotation.entity.CurrentAccountInfo;
import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.controller.BaseController;
import com.webank.webase.node.mgr.base.entity.BasePageResponse;
import com.webank.webase.node.mgr.base.entity.BaseResponse;
import com.webank.webase.node.mgr.base.enums.CheckUserExist;
import com.webank.webase.node.mgr.base.enums.RoleType;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.config.properties.ConstantProperties;
import com.webank.webase.node.mgr.tools.HttpRequestTools;
import com.webank.webase.node.mgr.tools.JsonTools;
import com.webank.webase.node.mgr.tools.NodeMgrTools;
import com.webank.webase.node.mgr.tools.PemUtils;
import com.webank.webase.node.mgr.cert.entity.FileContentHandle;
import com.webank.webase.node.mgr.user.entity.BindUserInputParam;
import com.webank.webase.node.mgr.user.entity.KeyPair;
import com.webank.webase.node.mgr.user.entity.NewUserInputParam;
import com.webank.webase.node.mgr.user.entity.ReqBindPrivateKey;
import com.webank.webase.node.mgr.user.entity.ReqExport;
import com.webank.webase.node.mgr.user.entity.ReqImportPem;
import com.webank.webase.node.mgr.user.entity.ReqImportPrivateKey;
import com.webank.webase.node.mgr.user.entity.TbUser;
import com.webank.webase.node.mgr.user.entity.UpdateUserInputParam;
import com.webank.webase.node.mgr.user.entity.UserParam;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import javax.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Key pair manage
 */
@Log4j2
@RestController
@RequestMapping("user")
public class UserController extends BaseController {

    @Autowired
    private UserService userService;

    /**
     * add new user info.
     */
    @PostMapping(value = "/userInfo")
    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN_OR_DEVELOPER)
    public BaseResponse addUserInfo(@RequestBody @Valid NewUserInputParam user, 
            @CurrentAccount CurrentAccountInfo currentAccountInfo, BindingResult result) throws NodeMgrException {
        checkBindResult(result);
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start addUserInfo startTime:{},currentAccount:{},NewUserInputParam:{}",
            startTime.toEpochMilli(), currentAccountInfo, user);
        // add user row
        TbUser userRow = userService.addUserInfoLocal(user.getGroupId(), user.getUserName(),
                currentAccountInfo.getAccount(), user.getDescription(), user.getUserType(), null);
        baseResponse.setData(userRow);

        log.info("end addUserInfo useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JsonTools.toJSONString(baseResponse));
        return baseResponse;
    }

    /**
     * bind user info. (add public key user, different from bind private key)
     */
    @PostMapping(value = "/bind")
    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN_OR_DEVELOPER)
    public BaseResponse bindUserInfo(@RequestBody @Valid BindUserInputParam user,
            @CurrentAccount CurrentAccountInfo currentAccountInfo, BindingResult result) throws NodeMgrException {
        checkBindResult(result);
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start bindUserInfo startTime:{},currentAccount:{},BindUserInputParam:{}",
            startTime.toEpochMilli(), currentAccountInfo, user);
        // query user row
        TbUser userRow = userService.bindUserInfo(user, currentAccountInfo.getAccount(), CheckUserExist.TURE.getValue());
        baseResponse.setData(userRow);

        log.info("end bindUserInfo useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JsonTools.toJSONString(baseResponse));
        return baseResponse;
    }

    /**
     * update user info of description
     */
    @PutMapping(value = "/userInfo")
    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN_OR_DEVELOPER)
    public BaseResponse updateUserInfo(@RequestBody @Valid UpdateUserInputParam user,
            BindingResult result) throws NodeMgrException {
        checkBindResult(result);
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start updateUserInfo startTime:{} User:{}", startTime.toEpochMilli(),
                JsonTools.toJSONString(user));

        // update user row
        userService.updateUser(user);
        // query user row
        TbUser userRow = userService.queryByUserId(user.getUserId());
        baseResponse.setData(userRow);

        log.info("end updateUserInfo useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JsonTools.toJSONString(baseResponse));
        return baseResponse;
    }

    /**
     * query user info list.
     */
    @GetMapping(value = "/userList/{groupId}/{pageNumber}/{pageSize}")
    public BasePageResponse userList(@PathVariable("groupId") String groupId,
            @PathVariable("pageNumber") Integer pageNumber,
            @PathVariable("pageSize") Integer pageSize,
            @RequestParam(value = "userParam", required = false) String commParam,
            @CurrentAccount CurrentAccountInfo currentAccountInfo)
            throws NodeMgrException {
        BasePageResponse pageResponse = new BasePageResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start userList startTime:{},currentAccountInfo:{} groupId:{} pageNumber:{} pageSize:{} commParam:{}",
                startTime.toEpochMilli(), currentAccountInfo, groupId, pageNumber, pageSize, commParam);

        String account = RoleType.DEVELOPER.getValue().intValue() == currentAccountInfo.getRoleId().intValue() 
                ? currentAccountInfo.getAccount() : null;
        UserParam param = new UserParam();
        param.setGroupId(groupId);
        param.setAccount(account);
        param.setCommParam(commParam);
        param.setPageSize(pageSize);

        Integer count = userService.countOfUser(param);
        if (count != null && count > 0) {
            Integer start =
                    Optional.ofNullable(pageNumber).map(page -> (page - 1) * pageSize).orElse(null);
            param.setStart(start);
            param.setPageSize(pageSize);

            List<TbUser> listOfUser = userService.queryUserList(param);
            pageResponse.setData(listOfUser);
            pageResponse.setTotalCount(count);
        }

        log.info("end userList useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JsonTools.toJSONString(pageResponse));
        return pageResponse;
    }

    @PostMapping("/import")
    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN_OR_DEVELOPER)
    public BaseResponse importPrivateKey(@Valid @RequestBody ReqImportPrivateKey reqImport,
            @CurrentAccount CurrentAccountInfo currentAccountInfo, BindingResult result) {
        checkBindResult(result);
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start importPrivateKey startTime:{},currentAccount:{},reqImport:{}",
            startTime.toEpochMilli(), currentAccountInfo, reqImport);
        // encoded by web in base64
        String privateKeyEncoded = reqImport.getPrivateKey();
        // add user row
        TbUser userRow = userService.addUserInfoLocal(reqImport.getGroupId(), reqImport.getUserName(),
                currentAccountInfo.getAccount(), reqImport.getDescription(), reqImport.getUserType(),
                privateKeyEncoded);
        baseResponse.setData(userRow);

        log.info("end importPrivateKey useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JsonTools.toJSONString(baseResponse));
        return baseResponse;
    }

    @PostMapping("/importPem")
    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN_OR_DEVELOPER)
    public BaseResponse importPemPrivateKey(@Valid @RequestBody ReqImportPem reqImportPem,
            @CurrentAccount CurrentAccountInfo currentAccountInfo, BindingResult result) {
        checkBindResult(result);
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start importPemPrivateKey startTime:{},currentAccount:{}",
            startTime.toEpochMilli(), currentAccountInfo);
        String pemContent = reqImportPem.getPemContent();
        if (!pemContent.startsWith(PemUtils.crtContentHeadNoLF)) {
            throw new NodeMgrException(ConstantCode.PEM_FORMAT_ERROR);
        }
        // import
        reqImportPem.setAccount(currentAccountInfo.getAccount());
        TbUser userRow = userService.importPem(reqImportPem, CheckUserExist.TURE.getValue());
        baseResponse.setData(userRow);

        log.info("end importPemPrivateKey useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JsonTools.toJSONString(baseResponse));
        return baseResponse;
    }

    @PostMapping("/importP12")
    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN_OR_DEVELOPER)
    public BaseResponse importP12PrivateKey(@RequestParam MultipartFile p12File,
            @RequestParam(required = false, defaultValue = "") String p12Password,
            @RequestParam String groupId, @RequestParam String userName,
            @RequestParam(required = false) String description,
            @CurrentAccount CurrentAccountInfo currentAccountInfo) {
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start importP12PrivateKey startTime:{},currentAccount:{}",
            startTime.toEpochMilli(), currentAccountInfo);
        if (!NodeMgrTools.notContainsChinese(p12Password)) {
            throw new NodeMgrException(ConstantCode.P12_PASSWORD_NOT_CHINESE);
        }
        if (p12File.getSize() == 0) {
            throw new NodeMgrException(ConstantCode.P12_FILE_ERROR);
        }
        TbUser userRow = userService.importKeyStoreFromP12(p12File, p12Password, groupId, userName,
                currentAccountInfo.getAccount(), description, CheckUserExist.TURE.getValue());
        baseResponse.setData(userRow);

        log.info("end importPemPrivateKey useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JsonTools.toJSONString(baseResponse));
        return new BaseResponse(ConstantCode.SUCCESS);
    }

    @PostMapping(value = "/exportPem")
    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN_OR_DEVELOPER)
    public ResponseEntity<InputStreamResource> exportPemUserFromSign(@RequestBody ReqExport param,
        @CurrentAccount CurrentAccountInfo currentAccount) throws NodeMgrException {
        Instant startTime = Instant.now();
        log.info("start exportPemUserFromSign startTime:{} param:{},currentAccount:{}",
            startTime.toEpochMilli(), param, currentAccount);
        String groupId = param.getGroupId();
        String signUserId = param.getSignUserId();
        String account = currentAccount.getAccount();
        Integer roleId = currentAccount.getRoleId();
        FileContentHandle fileContentHandle = userService.exportPemFromSign(groupId, signUserId, account, roleId);

        log.info("end exportPemUserFromSign useTime:{} fileContentHandle:{}",
            Duration.between(startTime, Instant.now()).toMillis(),
            JsonTools.toJSONString(fileContentHandle));
        return ResponseEntity.ok().headers(HttpRequestTools.headers(fileContentHandle.getFileName()))
            .body(new InputStreamResource(fileContentHandle.getInputStream()));
    }

    @PostMapping(value = "/exportP12")
    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN_OR_DEVELOPER)
    public ResponseEntity<InputStreamResource> exportP12UserFromSign(@RequestBody ReqExport param,
        @CurrentAccount CurrentAccountInfo currentAccount) throws NodeMgrException {
        Instant startTime = Instant.now();
        log.info("start exportP12UserFromSign startTime:{} param:{},currentAccount:{}",
            startTime.toEpochMilli(), param, currentAccount);
        String groupId = param.getGroupId();
        String signUserId = param.getSignUserId();
        String p12PasswordEncoded = param.getP12Password();
        if (!NodeMgrTools.notContainsChinese(p12PasswordEncoded)) {
            throw new NodeMgrException(ConstantCode.P12_PASSWORD_NOT_CHINESE);
        }
        // account info
        String account = currentAccount.getAccount();
        Integer roleId = currentAccount.getRoleId();
        FileContentHandle fileContentHandle = userService.exportP12FromSign(groupId, signUserId,
            p12PasswordEncoded, account, roleId);

        log.info("end exportP12UserFromSign useTime:{} result:{}",
            Duration.between(startTime, Instant.now()).toMillis(),
            JsonTools.toJSONString(fileContentHandle));
        return ResponseEntity.ok().headers(HttpRequestTools.headers(fileContentHandle.getFileName()))
            .body(new InputStreamResource(fileContentHandle.getInputStream()));
    }


    /**
     * not check account, cuz this is used for app integration
     * @param userId
     * @return
     * @throws NodeMgrException
     */
    @PostMapping(value = "/export/{userId}")
    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN_OR_DEVELOPER)
    public BaseResponse exportRawUserFromSign(@PathVariable("userId") Integer userId)
        throws NodeMgrException {
        Instant startTime = Instant.now();
        log.info("start exportRawUserFromSign startTime:{} userId:{}", startTime.toEpochMilli(), userId);

        TbUser tbUser = userService.queryUserDetail(userId);
        log.info("end exportRawUserFromSign useTime:{} result:{}",
            Duration.between(startTime, Instant.now()).toMillis(),
            tbUser);
        return new BaseResponse(ConstantCode.SUCCESS, tbUser);
    }

    @PostMapping("/bind/privateKey")
    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN_OR_DEVELOPER)
    public BaseResponse bindPrivateKey(@Valid @RequestBody ReqBindPrivateKey reqBind,
        @CurrentAccount CurrentAccountInfo currentAccountInfo, BindingResult result) {
        checkBindResult(result);
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start bindPrivateKey startTime:{} userId:{},currentAccount:{}",
            startTime.toEpochMilli(), reqBind.getUserId(), currentAccountInfo);

        if (StringUtils.isBlank(reqBind.getPrivateKey())) {
            throw new NodeMgrException(ConstantCode.PARAM_EXCEPTION);
        }
        // add user row
        TbUser tbUser = userService.updateUser(reqBind, currentAccountInfo);
        baseResponse.setData(tbUser);

        log.info("end bindPrivateKey useTime:{} result:{}",
            Duration.between(startTime, Instant.now()).toMillis(),
            JsonTools.toJSONString(baseResponse));
        return baseResponse;
    }

    @PostMapping("/bind/privateKey/pem")
    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN_OR_DEVELOPER)
    public BaseResponse bindPrivateKeyByPem(@Valid @RequestBody ReqBindPrivateKey reqBindPem,
        @CurrentAccount CurrentAccountInfo currentAccountInfo, BindingResult result) {
        checkBindResult(result);
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start bindPrivateKeyByPem startTime:{} userId:{},currentAccount:{}",
            startTime.toEpochMilli(), reqBindPem.getUserId(), currentAccountInfo);
        String pemContent = reqBindPem.getPemContent();
        if (StringUtils.isBlank(pemContent)) {
            throw new NodeMgrException(ConstantCode.PARAM_EXCEPTION);
        }
        if (!pemContent.startsWith(PemUtils.crtContentHeadNoLF)) {
            throw new NodeMgrException(ConstantCode.PEM_FORMAT_ERROR);
        }
        // add user row
        TbUser tbUser = userService.updateUserByPem(reqBindPem.getGroupId(), reqBindPem.getUserId(),
            pemContent, currentAccountInfo);
        baseResponse.setData(tbUser);

        log.info("end bindPrivateKey useTime:{} result:{}",
            Duration.between(startTime, Instant.now()).toMillis(),
            JsonTools.toJSONString(baseResponse));
        return baseResponse;
    }

    @PostMapping("/bind/privateKey/p12")
    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN_OR_DEVELOPER)
    public BaseResponse bindPrivateKeyByP12(@RequestParam MultipartFile p12File,
        @RequestParam(required = false, defaultValue = "") String p12Password,
        @RequestParam String groupId,
        @RequestParam Integer userId,
        @CurrentAccount CurrentAccountInfo currentAccountInfo) {
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start bindPrivateKeyByP12 startTime:{},currentAccount:{}",
            startTime.toEpochMilli(), currentAccountInfo);
        if (!NodeMgrTools.notContainsChinese(p12Password)) {
            throw new NodeMgrException(ConstantCode.P12_PASSWORD_NOT_CHINESE);
        }
        if (p12File.getSize() == 0) {
            throw new NodeMgrException(ConstantCode.P12_FILE_ERROR);
        }
        // add user row
        TbUser tbUser = userService.updateUserByP12(groupId, userId, p12File, p12Password,
            currentAccountInfo);
        baseResponse.setData(tbUser);

        log.info("end bindPrivateKey useTime:{} result:{}",
            Duration.between(startTime, Instant.now()).toMillis(),
            JsonTools.toJSONString(baseResponse));
        return baseResponse;
    }

    /**
     * update user info of description
     */
    @DeleteMapping(value = "/{groupId}/{address}")
    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN_OR_DEVELOPER)
    public BaseResponse suspendUser(@PathVariable("groupId") String groupId,
        @PathVariable("address") String address) throws NodeMgrException {
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start suspendUser startTime:{} User:{}|{}", startTime.toEpochMilli(),
            groupId, address);

        // update user row
        int res = userService.suspendUserByAddress(groupId, address);
        baseResponse.setData(res);

        log.info("end suspendUser useTime:{} result:{}",
            Duration.between(startTime, Instant.now()).toMillis(),
            JsonTools.toJSONString(baseResponse));
        return baseResponse;
    }
}

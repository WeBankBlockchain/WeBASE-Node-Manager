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

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.qctc.common.satoken.utils.LoginHelper;
import com.qctc.system.api.model.LoginUser;
import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.controller.BaseController;
import com.webank.webase.node.mgr.base.entity.BasePageResponse;
import com.webank.webase.node.mgr.base.entity.BaseResponse;
import com.webank.webase.node.mgr.base.enums.CheckUserExist;
import com.webank.webase.node.mgr.base.enums.GlobalRoleType;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.cert.entity.FileContentHandle;
import com.webank.webase.node.mgr.tools.HttpRequestTools;
import com.webank.webase.node.mgr.tools.JsonTools;
import com.webank.webase.node.mgr.tools.NodeMgrTools;
import com.webank.webase.node.mgr.tools.PemUtils;
import com.webank.webase.node.mgr.user.entity.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Key pair manage
 */
@Tag(name="链上用户管理")
@Log4j2
@RestController
@RequestMapping("user")
public class UserController extends BaseController {

    @Autowired
    private UserService userService;

    /**
     * add new user info.
     */
    @SaCheckPermission("bcos3:privateKeyManagement:userOperate")
    @PostMapping(value = "/userInfo")
    public BaseResponse addUserInfo(@RequestBody @Valid NewUserInputParam user,
             BindingResult result) throws NodeMgrException {
        checkBindResult(result);
        LoginUser curLoginUser = LoginHelper.getLoginUser();
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start addUserInfo startTime:{},currentAccount:{},NewUserInputParam:{}",
            startTime.toEpochMilli(), curLoginUser, user);
        // add user row
        TbUser userRow = userService.addUserInfoLocal(user.getGroupId(), user.getUserName(),
                curLoginUser.getUsername(), user.getDescription(), user.getUserType(), null);
        baseResponse.setData(userRow);

        log.info("end addUserInfo useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JsonTools.toJSONString(baseResponse));
        return baseResponse;
    }

    /**
     * bind user info. (add public key user, different from bind private key)
     */
    @SaCheckPermission("bcos3:privateKeyManagement:userOperate")
    @PostMapping(value = "/bind")
    public BaseResponse bindUserInfo(@RequestBody @Valid BindUserInputParam user, BindingResult result) throws NodeMgrException {
        checkBindResult(result);
        LoginUser curLoginUser = LoginHelper.getLoginUser();
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start bindUserInfo startTime:{},currentAccount:{},BindUserInputParam:{}",
            startTime.toEpochMilli(), curLoginUser, user);
        // query user row
        TbUser userRow = userService.bindUserInfo(user, curLoginUser.getUsername(), CheckUserExist.TURE.getValue());
        baseResponse.setData(userRow);

        log.info("end bindUserInfo useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JsonTools.toJSONString(baseResponse));
        return baseResponse;
    }

    /**
     * update user info of description
     */
    @SaCheckPermission("bcos3:privateKeyManagement:userOperate")
    @PutMapping(value = "/userInfo")
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
    @SaCheckPermission("bcos3:privateKeyManagement")
    @GetMapping(value = "/userList/{groupId}/{pageNumber}/{pageSize}")
    public BasePageResponse userList(@PathVariable("groupId") String groupId,
            @PathVariable("pageNumber") Integer pageNumber,
            @PathVariable("pageSize") Integer pageSize,
            @RequestParam(value = "userParam", required = false) String commParam)
            throws NodeMgrException {
        LoginUser curLoginUser = LoginHelper.getLoginUser();
        BasePageResponse pageResponse = new BasePageResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start userList startTime:{},currentAccountInfo:{} groupId:{} pageNumber:{} pageSize:{} commParam:{}",
                startTime.toEpochMilli(), curLoginUser, groupId, pageNumber, pageSize, commParam);

        String account = curLoginUser.getRolePermission().contains(GlobalRoleType.DEVELOPER.getValue())
                ? curLoginUser.getUsername() : null;
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

    @SaCheckPermission("bcos3:privateKeyManagement:userOperate")
    @PostMapping("/import")
    public BaseResponse importPrivateKey(@Valid @RequestBody ReqImportPrivateKey reqImport, BindingResult result) {
        checkBindResult(result);
        LoginUser curLoginUser = LoginHelper.getLoginUser();
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start importPrivateKey startTime:{},currentAccount:{},reqImport:{}",
            startTime.toEpochMilli(), curLoginUser, reqImport);
        // encoded by web in base64
        String privateKeyEncoded = reqImport.getPrivateKey();
        // add user row
        TbUser userRow = userService.addUserInfoLocal(reqImport.getGroupId(), reqImport.getUserName(),
                curLoginUser.getUsername(), reqImport.getDescription(), reqImport.getUserType(),
                privateKeyEncoded);
        baseResponse.setData(userRow);

        log.info("end importPrivateKey useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JsonTools.toJSONString(baseResponse));
        return baseResponse;
    }

    @SaCheckPermission("bcos3:privateKeyManagement:userOperate")
    @PostMapping("/importPem")
    public BaseResponse importPemPrivateKey(@Valid @RequestBody ReqImportPem reqImportPem, BindingResult result) {
        checkBindResult(result);
        LoginUser curLoginUser = LoginHelper.getLoginUser();
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start importPemPrivateKey startTime:{},currentAccount:{}",
            startTime.toEpochMilli(), curLoginUser);
        String pemContent = reqImportPem.getPemContent();
        if (!pemContent.startsWith(PemUtils.crtContentHeadNoLF)) {
            throw new NodeMgrException(ConstantCode.PEM_FORMAT_ERROR);
        }
        // import
        reqImportPem.setAccount(curLoginUser.getUsername());
        TbUser userRow = userService.importPem(reqImportPem, CheckUserExist.TURE.getValue());
        baseResponse.setData(userRow);

        log.info("end importPemPrivateKey useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JsonTools.toJSONString(baseResponse));
        return baseResponse;
    }

    @SaCheckPermission("bcos3:privateKeyManagement:userOperate")
    @PostMapping("/importP12")
    public BaseResponse importP12PrivateKey(@RequestParam MultipartFile p12File,
            @RequestParam(required = false, defaultValue = "") String p12Password,
            @RequestParam String groupId, @RequestParam String userName,
            @RequestParam(required = false) String description) {
        LoginUser curLoginUser = LoginHelper.getLoginUser();
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start importP12PrivateKey startTime:{},currentAccount:{}",
            startTime.toEpochMilli(), curLoginUser);
        if (!NodeMgrTools.notContainsChinese(p12Password)) {
            throw new NodeMgrException(ConstantCode.P12_PASSWORD_NOT_CHINESE);
        }
        if (p12File.getSize() == 0) {
            throw new NodeMgrException(ConstantCode.P12_FILE_ERROR);
        }
        TbUser userRow = userService.importKeyStoreFromP12(p12File, p12Password, groupId, userName,
                curLoginUser.getUsername(), description, CheckUserExist.TURE.getValue());
        baseResponse.setData(userRow);

        log.info("end importPemPrivateKey useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JsonTools.toJSONString(baseResponse));
        return new BaseResponse(ConstantCode.SUCCESS);
    }

    @SaCheckPermission("bcos3:privateKeyManagement:userOperate")
    @PostMapping(value = "/exportPem")
    public ResponseEntity<InputStreamResource> exportPemUserFromSign(@RequestBody ReqExport param) throws NodeMgrException {
        Instant startTime = Instant.now();
        LoginUser curLoginUser = LoginHelper.getLoginUser();
        log.info("start exportPemUserFromSign startTime:{} param:{},currentAccount:{}",
            startTime.toEpochMilli(), param, curLoginUser);
        String groupId = param.getGroupId();
        String signUserId = param.getSignUserId();
//        String account = currentAccount.getAccount();
//        Integer roleId = currentAccount.getRoleId();
        FileContentHandle fileContentHandle = userService.exportPemFromSign(groupId, signUserId, curLoginUser);

        log.info("end exportPemUserFromSign useTime:{} fileContentHandle:{}",
            Duration.between(startTime, Instant.now()).toMillis(),
            JsonTools.toJSONString(fileContentHandle));
        return ResponseEntity.ok().headers(HttpRequestTools.headers(fileContentHandle.getFileName()))
            .body(new InputStreamResource(fileContentHandle.getInputStream()));
    }

    @SaCheckPermission("bcos3:privateKeyManagement:userOperate")
    @PostMapping(value = "/exportP12")
    public ResponseEntity<InputStreamResource> exportP12UserFromSign(@RequestBody ReqExport param) throws NodeMgrException {
        LoginUser curLoginUser = LoginHelper.getLoginUser();
        Instant startTime = Instant.now();
        log.info("start exportP12UserFromSign startTime:{} param:{},currentAccount:{}",
            startTime.toEpochMilli(), param, curLoginUser);
        String groupId = param.getGroupId();
        String signUserId = param.getSignUserId();
        String p12PasswordEncoded = param.getP12Password();
        if (!NodeMgrTools.notContainsChinese(p12PasswordEncoded)) {
            throw new NodeMgrException(ConstantCode.P12_PASSWORD_NOT_CHINESE);
        }
        // account info
//        String account = currentAccount.getAccount();
//        Integer roleId = currentAccount.getRoleId();
        FileContentHandle fileContentHandle = userService.exportP12FromSign(groupId, signUserId,
            p12PasswordEncoded, curLoginUser);

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
    @SaCheckPermission("bcos3:privateKeyManagement:userOperate")
    @PostMapping(value = "/export/{userId}")
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

    @SaCheckPermission("bcos3:privateKeyManagement:userOperate")
    @PostMapping("/bind/privateKey")
    public BaseResponse bindPrivateKey(@Valid @RequestBody ReqBindPrivateKey reqBind, BindingResult result) {
        checkBindResult(result);
        LoginUser curLoginUser = LoginHelper.getLoginUser();
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start bindPrivateKey startTime:{} userId:{},currentAccount:{}",
            startTime.toEpochMilli(), reqBind.getUserId(), curLoginUser);

        if (StringUtils.isBlank(reqBind.getPrivateKey())) {
            throw new NodeMgrException(ConstantCode.PARAM_EXCEPTION);
        }
        // add user row
        TbUser tbUser = userService.updateUser(reqBind, curLoginUser);
        baseResponse.setData(tbUser);

        log.info("end bindPrivateKey useTime:{} result:{}",
            Duration.between(startTime, Instant.now()).toMillis(),
            JsonTools.toJSONString(baseResponse));
        return baseResponse;
    }

    @SaCheckPermission("bcos3:privateKeyManagement:userOperate")
    @PostMapping("/bind/privateKey/pem")
    public BaseResponse bindPrivateKeyByPem(@Valid @RequestBody ReqBindPrivateKey reqBindPem, BindingResult result) {
        checkBindResult(result);
        LoginUser curLoginUser = LoginHelper.getLoginUser();
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start bindPrivateKeyByPem startTime:{} userId:{},currentAccount:{}",
            startTime.toEpochMilli(), reqBindPem.getUserId(), curLoginUser);
        String pemContent = reqBindPem.getPemContent();
        if (StringUtils.isBlank(pemContent)) {
            throw new NodeMgrException(ConstantCode.PARAM_EXCEPTION);
        }
        if (!pemContent.startsWith(PemUtils.crtContentHeadNoLF)) {
            throw new NodeMgrException(ConstantCode.PEM_FORMAT_ERROR);
        }
        // add user row
        TbUser tbUser = userService.updateUserByPem(reqBindPem.getGroupId(), reqBindPem.getUserId(),
            pemContent);
        baseResponse.setData(tbUser);

        log.info("end bindPrivateKey useTime:{} result:{}",
            Duration.between(startTime, Instant.now()).toMillis(),
            JsonTools.toJSONString(baseResponse));
        return baseResponse;
    }

    @SaCheckPermission("bcos3:privateKeyManagement:userOperate")
    @PostMapping("/bind/privateKey/p12")
    public BaseResponse bindPrivateKeyByP12(@RequestParam MultipartFile p12File,
        @RequestParam(required = false, defaultValue = "") String p12Password,
        @RequestParam String groupId,
        @RequestParam Integer userId) {
        LoginUser curLoginUser= LoginHelper.getLoginUser();
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start bindPrivateKeyByP12 startTime:{},currentAccount:{}",
            startTime.toEpochMilli(), curLoginUser);
        if (!NodeMgrTools.notContainsChinese(p12Password)) {
            throw new NodeMgrException(ConstantCode.P12_PASSWORD_NOT_CHINESE);
        }
        if (p12File.getSize() == 0) {
            throw new NodeMgrException(ConstantCode.P12_FILE_ERROR);
        }
        // add user row
        TbUser tbUser = userService.updateUserByP12(groupId, userId, p12File, p12Password);
        baseResponse.setData(tbUser);

        log.info("end bindPrivateKey useTime:{} result:{}",
            Duration.between(startTime, Instant.now()).toMillis(),
            JsonTools.toJSONString(baseResponse));
        return baseResponse;
    }

    /**
     * update user info of description
     */
    @SaCheckPermission("bcos3:privateKeyManagement:userOperate")
    @DeleteMapping(value = "/{groupId}/{address}")
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

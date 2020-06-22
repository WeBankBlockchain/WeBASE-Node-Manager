/**
 * Copyright 2014-2020  the original author or authors.
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

import com.webank.webase.node.mgr.base.tools.JsonTools;
import com.webank.webase.node.mgr.base.controller.BaseController;
import com.webank.webase.node.mgr.base.entity.BasePageResponse;
import com.webank.webase.node.mgr.base.entity.BaseResponse;
import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.base.properties.ConstantProperties;
import com.webank.webase.node.mgr.base.tools.NodeMgrTools;
import com.webank.webase.node.mgr.base.tools.PemUtils;
import com.webank.webase.node.mgr.user.entity.*;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import javax.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
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
    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN)
    public BaseResponse addUserInfo(@RequestBody @Valid NewUserInputParam user,
        BindingResult result) throws NodeMgrException {
        checkBindResult(result);
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();

        // add user row
        Integer userId = userService.addUserInfo(user.getGroupId(),
                user.getUserName(), user.getDescription(), user.getUserType(), null);

        // query user row
        TbUser userRow = userService.queryByUserId(userId);
        baseResponse.setData(userRow);

        log.info("end addUserInfo useTime:{} result:{}",
            Duration.between(startTime, Instant.now()).toMillis(), JsonTools.toJSONString(baseResponse));
        return baseResponse;
    }

    /**
     * bind user info.
     */
    @PostMapping(value = "/bind")
    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN)
    public BaseResponse bindUserInfo(@RequestBody @Valid BindUserInputParam user,
        BindingResult result) throws NodeMgrException {
        checkBindResult(result);
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();

        // add user row
        Integer userId = userService.bindUserInfo(user);

        // query user row
        TbUser userRow = userService.queryByUserId(userId);
        baseResponse.setData(userRow);

        log.info("end bindUserInfo useTime:{} result:{}",
            Duration.between(startTime, Instant.now()).toMillis(), JsonTools.toJSONString(baseResponse));
        return baseResponse;
    }

    /**
     * update user info.
     */
    @PutMapping(value = "/userInfo")
    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN)
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
            Duration.between(startTime, Instant.now()).toMillis(), JsonTools.toJSONString(baseResponse));
        return baseResponse;
    }

    /**
     * qurey user info list.
     */
    @GetMapping(value = "/userList/{groupId}/{pageNumber}/{pageSize}")
    public BasePageResponse userList(@PathVariable("groupId") Integer groupId,
        @PathVariable("pageNumber") Integer pageNumber,
        @PathVariable("pageSize") Integer pageSize,
        @RequestParam(value = "userParam", required = false) String commParam)
        throws NodeMgrException {
        BasePageResponse pagesponse = new BasePageResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start userList startTime:{} groupId:{} pageNumber:{} pageSize:{} commParam:{}",
            startTime.toEpochMilli(), groupId, pageNumber, pageSize,
            commParam);

        UserParam param = new UserParam();
        param.setGroupId(groupId);
        param.setCommParam(commParam);
        param.setPageSize(pageSize);

        Integer count = userService.countOfUser(param);
        if (count != null && count > 0) {
            Integer start = Optional.ofNullable(pageNumber).map(page -> (page - 1) * pageSize)
                .orElse(null);
            param.setStart(start);
            param.setPageSize(pageSize);

            List<TbUser> listOfUser = userService.qureyUserList(param);
            pagesponse.setData(listOfUser);
            pagesponse.setTotalCount(count);
        }

        log.info("end userList useTime:{} result:{}",
            Duration.between(startTime, Instant.now()).toMillis(), JsonTools.toJSONString(pagesponse));
        return pagesponse;
    }

    @PostMapping("/import")
    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN)
    public BaseResponse importPrivateKey(@Valid @RequestBody ReqImportPrivateKey reqImport, BindingResult result) {
        checkBindResult(result);
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();

        // encoded by web in base64
        String privateKeyEncoded = reqImport.getPrivateKey();
        // add user row
        Integer userId = userService.addUserInfo(reqImport.getGroupId(), reqImport.getUserName(),
                reqImport.getDescription(), reqImport.getUserType(), privateKeyEncoded);

        // query user row
        TbUser userRow = userService.queryByUserId(userId);
        baseResponse.setData(userRow);

        log.info("end importPrivateKey useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(), JsonTools.toJSONString(baseResponse));
        return baseResponse;
    }

    @PostMapping("/importPem")
    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN)
    public BaseResponse importPemPrivateKey(@Valid @RequestBody ReqImportPem reqImportPem, BindingResult result) {
        checkBindResult(result);
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();

        String pemContent = reqImportPem.getPemContent();
        if(!pemContent.startsWith(PemUtils.crtContentHead)) {
            throw new NodeMgrException(ConstantCode.PEM_FORMAT_ERROR);
        }
        // import
        Integer userId = userService.importPem(reqImportPem);
        // query user row
        TbUser userRow = userService.queryByUserId(userId);
        baseResponse.setData(userRow);

        log.info("end importPemPrivateKey useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(), JsonTools.toJSONString(baseResponse));
        return baseResponse;
    }

    @PostMapping("/importP12")
    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN)
    public BaseResponse importP12PrivateKey(@RequestParam MultipartFile p12File,
                                            @RequestParam(required = false, defaultValue = "") String p12Password,
                                            @RequestParam Integer groupId, @RequestParam String userName,
                                            @RequestParam(required = false) String description) {
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();

        if (!NodeMgrTools.notContainsChinese(p12Password)) {
            throw new NodeMgrException(ConstantCode.P12_PASSWORD_NOT_CHINESE);
        }
        if (p12File.getSize() == 0) {
            throw new NodeMgrException(ConstantCode.P12_FILE_ERROR);
        }
        Integer userId = userService.importKeyStoreFromP12(p12File, p12Password,
                groupId, userName, description);
        // query user row
        TbUser userRow = userService.queryByUserId(userId);
        baseResponse.setData(userRow);

        log.info("end importPemPrivateKey useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(), JsonTools.toJSONString(baseResponse));
        return new BaseResponse(ConstantCode.SUCCESS);
    }

}

/**
 * Copyright 2014-2021 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.webank.webase.node.mgr.appintegration.api;

import com.webank.webase.node.mgr.account.AccountService;
import com.webank.webase.node.mgr.account.entity.AccountInfo;
import com.webank.webase.node.mgr.account.entity.AccountListParam;
import com.webank.webase.node.mgr.account.entity.TbAccountInfo;
import com.webank.webase.node.mgr.appintegration.AppIntegrationService;
import com.webank.webase.node.mgr.appintegration.contractstore.ContractStoreService;
import com.webank.webase.node.mgr.appintegration.contractstore.entity.ReqContractAddressSave;
import com.webank.webase.node.mgr.appintegration.contractstore.entity.ReqContractSourceSave;
import com.webank.webase.node.mgr.appintegration.entity.AppRegisterInfo;
import com.webank.webase.node.mgr.appintegration.entity.BasicInfo;
import com.webank.webase.node.mgr.appintegration.entity.UpdatePasswordInfo;
import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.controller.BaseController;
import com.webank.webase.node.mgr.base.entity.BasePageResponse;
import com.webank.webase.node.mgr.base.entity.BaseResponse;
import com.webank.webase.node.mgr.base.enums.CheckUserExist;
import com.webank.webase.node.mgr.base.enums.GroupStatus;
import com.webank.webase.node.mgr.base.enums.ReturnPrivateKey;
import com.webank.webase.node.mgr.base.enums.SqlSortType;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.base.properties.VersionProperties;
import com.webank.webase.node.mgr.base.tools.JsonTools;
import com.webank.webase.node.mgr.base.tools.NodeMgrTools;
import com.webank.webase.node.mgr.base.tools.PemUtils;
import com.webank.webase.node.mgr.contract.ContractService;
import com.webank.webase.node.mgr.front.FrontService;
import com.webank.webase.node.mgr.front.entity.FrontParam;
import com.webank.webase.node.mgr.front.entity.TbFront;
import com.webank.webase.node.mgr.frontgroupmap.FrontGroupMapService;
import com.webank.webase.node.mgr.frontinterface.FrontInterfaceService;
import com.webank.webase.node.mgr.group.GroupService;
import com.webank.webase.node.mgr.group.entity.TbGroup;
import com.webank.webase.node.mgr.node.NodeService;
import com.webank.webase.node.mgr.node.entity.NodeParam;
import com.webank.webase.node.mgr.node.entity.TbNode;
import com.webank.webase.node.mgr.role.RoleService;
import com.webank.webase.node.mgr.table.TableService;
import com.webank.webase.node.mgr.user.UserService;
import com.webank.webase.node.mgr.user.entity.BindUserInputParam;
import com.webank.webase.node.mgr.user.entity.NewUserInputParam;
import com.webank.webase.node.mgr.user.entity.ReqImportPem;
import com.webank.webase.node.mgr.user.entity.ReqImportPrivateKey;
import com.webank.webase.node.mgr.user.entity.TbUser;
import com.webank.webase.node.mgr.user.entity.UserParam;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import javax.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * application integration api.
 */
@Log4j2
@RestController
@RequestMapping(value = "api")
public class AppIntegrationApi extends BaseController {

    @Autowired
    private CryptoSuite cryptoSuite;
    @Autowired
    private AppIntegrationService appIntegrationService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private RoleService roleService;
    @Autowired
    private GroupService groupService;
    @Autowired
    private NodeService nodeService;
    @Autowired
    private UserService userService;
    @Autowired
    private ContractStoreService contractStoreService;
    @Autowired
    private ContractService contractService;
    @Autowired
    private TableService tableService;
    @Autowired
    private FrontService frontService;
    @Autowired
    private FrontGroupMapService frontGroupMapService;
    @Autowired
    private FrontInterfaceService frontInterfaceService;
    @Autowired
    private VersionProperties versionProperties;

    /**
     * app register.
     */
    @PostMapping("/appRegister")
    public BaseResponse appRegister(@RequestParam(required = true) String appKey,
            @RequestBody @Valid AppRegisterInfo appRegisterInfo, BindingResult result) {
        checkBindResult(result);
        Instant startTime = Instant.now();
        log.info("start appRegister startTime:{} appKey:{} appRegisterInfo:{}",
                startTime.toEpochMilli(), appKey, JsonTools.toJSONString(appRegisterInfo));
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        appIntegrationService.appRegister(appKey, appRegisterInfo);
        log.info("end appRegister useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JsonTools.toJSONString(baseResponse));
        return baseResponse;
    }

    /**
     * query account list.
     */
    @GetMapping(value = "/accountList")
    public BasePageResponse queryAccountList(@RequestParam(required = true) Integer pageNumber,
            @RequestParam(required = true) Integer pageSize,
            @RequestParam(required = false) String account) throws NodeMgrException {
        BasePageResponse pageResponse = new BasePageResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start queryAccountList.  startTime:{} pageNumber:{} pageSize:{}",
                startTime.toEpochMilli(), pageNumber, pageSize);

        int count = accountService.countOfAccount(account);
        if (count > 0) {
            Integer start =
                    Optional.ofNullable(pageNumber).map(page -> (page - 1) * pageSize).orElse(0);
            AccountListParam param =
                    new AccountListParam(start, pageSize, account, SqlSortType.DESC.getValue());
            List<TbAccountInfo> listOfAccount = accountService.listOfAccount(param);
            listOfAccount.stream().forEach(accountData -> accountData.setAccountPwd(null));
            pageResponse.setData(listOfAccount);
            pageResponse.setTotalCount(count);
        }

        log.info("end queryAccountList useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JsonTools.toJSONString(pageResponse));
        return pageResponse;
    }

    /**
     * query role list.
     */
    @GetMapping(value = "/roleList")
    public BasePageResponse queryRoleList() throws NodeMgrException {
        Instant startTime = Instant.now();
        log.info("start queryRoleList.", startTime.toEpochMilli());

        // query
        BasePageResponse pageResponse = roleService.queryRoleList(null, null, null, null);

        log.info("end queryRoleList useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JsonTools.toJSONString(pageResponse));
        return pageResponse;
    }

    /**
     * add account info.
     */
    @PostMapping(value = "/accountAdd")
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
        tbAccount.setAccountPwd(null);
        baseResponse.setData(tbAccount);

        log.info("end addAccountInfo useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JsonTools.toJSONString(baseResponse));
        return baseResponse;
    }

    /**
     * update password.
     */
    @PostMapping(value = "/passwordUpdate")
    public BaseResponse updatePassword(@RequestBody @Valid UpdatePasswordInfo info,
            BindingResult result) throws NodeMgrException {
        checkBindResult(result);
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();

        // update account row
        accountService.updatePassword(info.getAccount(), info.getOldAccountPwd(),
                info.getNewAccountPwd());

        log.info("end updatePassword useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JsonTools.toJSONString(baseResponse));
        return baseResponse;
    }

    /**
     * get base info.
     */
    @GetMapping("basicInfo")
    public BaseResponse getBasicInfo() {
        BasicInfo basicInfo = new BasicInfo();
        basicInfo.setEncryptType(cryptoSuite.cryptoTypeConfig);
        basicInfo.setSslCryptoType(frontInterfaceService.getSSLCryptoType());
        basicInfo.setFiscoBcosVersion(frontInterfaceService.getClientVersion().getVersion());
        basicInfo.setWebaseVersion(versionProperties.getVersion());
        log.info("getBasicInfo:{}", JsonTools.toJSONString(basicInfo));
        return new BaseResponse(ConstantCode.SUCCESS, basicInfo);
    }

    /**
     * get encrypt type.
     */
    @Deprecated
    @GetMapping("encrypt")
    public BaseResponse getEncryptType() {
        int encrypt = cryptoSuite.cryptoTypeConfig;
        log.info("getEncryptType:{}", encrypt);
        return new BaseResponse(ConstantCode.SUCCESS, encrypt);
    }

    /**
     * query group list.
     */
    @GetMapping("/groupList")
    public BasePageResponse getGroupList(
            @RequestParam(required = false, defaultValue = "1") Integer groupStatus)
            throws NodeMgrException {
        BasePageResponse pageResponse = new BasePageResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start getGroupList startTime:{}", startTime.toEpochMilli());

        if (groupStatus > GroupStatus.CONFLICT_LOCAL_DATA.getValue()
                || groupStatus < GroupStatus.NORMAL.getValue()) {
            return new BasePageResponse(ConstantCode.INVALID_PARAM_INFO);
        }

        // get group list
        int count = groupService.countOfGroup(null, groupStatus);
        if (count > 0) {
            List<TbGroup> groupList = groupService.getGroupList(groupStatus);
            pageResponse.setTotalCount(count);
            pageResponse.setData(groupList);
        }

        log.info("end getGroupList useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JsonTools.toJSONString(pageResponse));
        return pageResponse;
    }

    /**
     * query node info list.
     */
    @GetMapping(value = "/nodeList")
    public BasePageResponse queryNodeList(@RequestParam(required = true) Integer pageNumber,
            @RequestParam(required = true) Integer pageSize,
            @RequestParam(required = false, defaultValue = "") Integer groupId,
            @RequestParam(required = false) String nodeId) throws NodeMgrException {
        BasePageResponse pageResponse = new BasePageResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start queryNodeList startTime:{}", startTime.toEpochMilli());

        // param
        NodeParam queryParam = new NodeParam();
        queryParam.setGroupId(groupId);
        queryParam.setPageSize(pageSize);
        queryParam.setNodeId(nodeId);

        // check node status before query
        try {
            nodeService.checkAndUpdateNodeStatus(groupId);
        } catch (Exception e) {
            log.error("queryNodeList checkAndUpdateNodeStatus groupId:{}, error: []", groupId, e);
        }
        Integer count = nodeService.countOfNode(queryParam);
        if (count != null && count > 0) {
            Integer start =
                    Optional.ofNullable(pageNumber).map(page -> (page - 1) * pageSize).orElse(null);
            queryParam.setStart(start);

            List<TbNode> listOfnode = nodeService.queryNodeList(queryParam);
            pageResponse.setData(listOfnode);
            pageResponse.setTotalCount(count);

        }

        log.info("end queryNodeList useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JsonTools.toJSONString(pageResponse));
        return pageResponse;
    }

    /**
     * get node info
     */
    @GetMapping(value = "/nodeInfo")
    public BaseResponse getNodeInfo(@RequestParam(required = true) Integer groupId,
            @RequestParam(required = true) String nodeId) throws NodeMgrException {

        Instant startTime = Instant.now();
        log.info("start addNodeInfo startTime:{} groupId:{}", startTime.toEpochMilli(), groupId);

        // param
        NodeParam param = new NodeParam();
        param.setGroupId(groupId);
        param.setNodeId(nodeId);

        // query node row
        TbNode tbNode = nodeService.queryNodeInfo(param);

        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        baseResponse.setData(tbNode);

        log.info("end addNodeInfo useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JsonTools.toJSONString(baseResponse));
        return baseResponse;
    }

    /**
     * get front and node info list
     */
    @GetMapping(value = "/frontNodeList")
    public BasePageResponse queryFrontList(@RequestParam(required = false) Integer groupId,
            @RequestParam(required = false) String nodeId) throws NodeMgrException {
        BasePageResponse pageResponse = new BasePageResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start queryFrontList startTime:{} groupId:{},nodeId:{}", startTime.toEpochMilli(),
                groupId, nodeId);

        // param
        FrontParam param = new FrontParam();
        param.setGroupId(groupId);
        param.setNodeId(nodeId);

        // query front info
        int count = frontService.getFrontCount(param);
        pageResponse.setTotalCount(count);
        if (count > 0) {
            List<TbFront> list = frontService.getFrontList(param);
            list.forEach(front -> front.setGroupList(
                    frontGroupMapService.getGroupIdListByFrontId(front.getFrontId())));
            pageResponse.setData(list);
        }

        log.info("end queryFrontList useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JsonTools.toJSONString(pageResponse));
        return pageResponse;
    }

    /**
     * get sdk cert
     */
    @GetMapping(value = "/sdkCert")
    public BaseResponse getSdkCert() throws NodeMgrException {

        Instant startTime = Instant.now();
        log.info("start getSdkCert startTime:{}", startTime.toEpochMilli());

        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        baseResponse.setData(frontInterfaceService.getSdkCertInfo());

        log.info("end getSdkCert useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JsonTools.toJSONString(baseResponse));
        return baseResponse;
    }

    /**
     * add new user info.
     */
    @PostMapping(value = "/newUser")
    public BaseResponse newUser(@RequestBody @Valid NewUserInputParam user, BindingResult result)
            throws NodeMgrException {
        checkBindResult(result);
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();

        // add user row
        TbUser userRow = userService.addUserInfo(user.getGroupId(), user.getUserName(),
                user.getAccount(), user.getDescription(), user.getUserType(), null,
                ReturnPrivateKey.TURE.getValue(), CheckUserExist.TURE.getValue());
        baseResponse.setData(userRow);

        log.info("end newUser useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JsonTools.toJSONString(baseResponse));
        return baseResponse;
    }

    /**
     * import publicKey.
     */
    @PostMapping(value = "/importPublicKey")
    public BaseResponse importPublicKey(@RequestBody @Valid BindUserInputParam user,
            BindingResult result) throws NodeMgrException {
        checkBindResult(result);
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();

        // query user row
        TbUser userRow = userService.bindUserInfo(user, user.getAccount(), CheckUserExist.FALSE.getValue());
        baseResponse.setData(userRow);

        log.info("end importPublicKey useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JsonTools.toJSONString(baseResponse));
        return baseResponse;
    }

    /**
     * query user info list.
     */
    @GetMapping(value = "/userList")
    public BasePageResponse userList(@RequestParam(required = true) Integer groupId,
            @RequestParam(required = true) Integer pageNumber,
            @RequestParam(required = true) Integer pageSize,
            @RequestParam(required = false) String account,
            @RequestParam(required = false) String userParam,
            @RequestParam(required = false, defaultValue = "") Integer hasPrivateKey)
            throws NodeMgrException {
        BasePageResponse pageResponse = new BasePageResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start userList startTime:{} groupId:{} pageNumber:{} pageSize:{} userParam:{}",
                startTime.toEpochMilli(), groupId, pageNumber, pageSize, userParam);

        UserParam param = new UserParam();
        param.setGroupId(groupId);
        param.setAccount(account);
        param.setCommParam(userParam);
        param.setPageSize(pageSize);
        param.setHasPk(hasPrivateKey);

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

    /**
     * query user info.
     */
    @GetMapping(value = "/userInfo")
    public BaseResponse userInfo(@RequestParam(required = true) int userId)
            throws NodeMgrException {
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start userInfo startTime:{} userId:{}", startTime.toEpochMilli(), userId);

        TbUser user = userService.queryUserDetail(userId);
        baseResponse.setData(user);

        log.info("end userInfo useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JsonTools.toJSONString(baseResponse));
        return baseResponse;
    }

    @PostMapping("/importPrivateKey")
    public BaseResponse importPrivateKey(@Valid @RequestBody ReqImportPrivateKey reqImport,
            BindingResult result) {
        checkBindResult(result);
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();

        // encoded by web in base64
        String privateKeyEncoded = reqImport.getPrivateKey();
        // add user row
        TbUser userRow = userService.addUserInfo(reqImport.getGroupId(), reqImport.getUserName(),
                reqImport.getAccount(), reqImport.getDescription(), reqImport.getUserType(),
                privateKeyEncoded, ReturnPrivateKey.FALSE.getValue(),
                CheckUserExist.FALSE.getValue());
        baseResponse.setData(userRow);

        log.info("end importPrivateKey useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JsonTools.toJSONString(baseResponse));
        return baseResponse;
    }

    @PostMapping("/importPem")
    public BaseResponse importPemPrivateKey(@Valid @RequestBody ReqImportPem reqImportPem,
            BindingResult result) {
        checkBindResult(result);
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();

        String pemContent = reqImportPem.getPemContent();
        if (!pemContent.startsWith(PemUtils.crtContentHeadNoLF)) {
            throw new NodeMgrException(ConstantCode.PEM_FORMAT_ERROR);
        }
        // import
        TbUser userRow = userService.importPem(reqImportPem, CheckUserExist.FALSE.getValue());
        baseResponse.setData(userRow);

        log.info("end importPemPrivateKey useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JsonTools.toJSONString(baseResponse));
        return baseResponse;
    }

    @PostMapping("/importP12")
    public BaseResponse importP12PrivateKey(@RequestParam MultipartFile p12File,
            @RequestParam(required = false, defaultValue = "") String p12Password,
            @RequestParam Integer groupId, @RequestParam String userName,
            @RequestParam String account, @RequestParam(required = false) String description) {
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();

        if (!NodeMgrTools.notContainsChinese(p12Password)) {
            throw new NodeMgrException(ConstantCode.P12_PASSWORD_NOT_CHINESE);
        }
        if (p12File.getSize() == 0) {
            throw new NodeMgrException(ConstantCode.P12_FILE_ERROR);
        }
        TbUser userRow = userService.importKeyStoreFromP12(p12File, p12Password, groupId, userName,
                account, description, CheckUserExist.FALSE.getValue());
        baseResponse.setData(userRow);

        log.info("end importPemPrivateKey useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JsonTools.toJSONString(baseResponse));
        return baseResponse;
    }

    /**
     * save contract source.
     */
    @PostMapping(value = "/contractSourceSave")
    public BaseResponse contractSourceSave(@RequestParam(required = true) String appKey,
            @RequestBody @Valid ReqContractSourceSave reqContractSourceSave, BindingResult result)
            throws NodeMgrException {
        checkBindResult(result);
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start contractSourceSave startTime:{} reqContractSourceSave:{}",
                startTime.toEpochMilli(), JsonTools.toJSONString(reqContractSourceSave));

        // save contract source
        contractStoreService.saveContractSource(appKey, reqContractSourceSave);

        log.info("end reqContractSourceSave useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JsonTools.toJSONString(baseResponse));
        return baseResponse;
    }

    /**
     * save contract address.
     */
    @PostMapping(value = "/contractAddressSave")
    public BaseResponse contractAddressSave(@RequestParam(required = true) String appKey,
            @RequestBody @Valid ReqContractAddressSave reqContractAddressSave, BindingResult result)
            throws NodeMgrException, IOException {
        checkBindResult(result);
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start contractAddressSave startTime:{} reqContractAddressSave:{}",
                startTime.toEpochMilli(), JsonTools.toJSONString(reqContractAddressSave));

        // save contract address
        contractService.appContractSave(appKey, reqContractAddressSave);

        log.info("end contractAddressSave useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JsonTools.toJSONString(baseResponse));
        return baseResponse;
    }

    /**
     * get db info.
     */
    @GetMapping("dbInfo")
    public BaseResponse getDbInfo() {
        log.info("getDbInfo.");
        return new BaseResponse(ConstantCode.SUCCESS, tableService.getDbInfo());
    }
}

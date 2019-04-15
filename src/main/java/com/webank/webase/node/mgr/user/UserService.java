/**
 * Copyright 2014-2019  the original author or authors.
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

import com.alibaba.fastjson.JSON;
import com.webank.webase.node.mgr.base.entity.ConstantCode;
import com.webank.webase.node.mgr.base.enums.HasPk;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.base.properties.ConstantProperties;
import com.webank.webase.node.mgr.base.tools.AesTools;
import com.webank.webase.node.mgr.base.tools.Web3Tools;
import com.webank.webase.node.mgr.frontinterface.FrontRestTools;
import com.webank.webase.node.mgr.group.GroupService;
import com.webank.webase.node.mgr.monitor.MonitorService;
import java.util.List;
import java.util.Optional;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * services for user data.
 */
@Log4j2
@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private GroupService groupService;
    @Autowired
    private AesTools aesTools;
    @Autowired
    private FrontRestTools frontRestTools;
    @Autowired
    private MonitorService monitorService;

    /**
     * add new user data.
     */
    @Transactional
    public Integer addUserInfo(User user) throws NodeMgrException {
        log.debug("start addUserInfo User:{}", JSON.toJSONString(user));

        Integer groupId = user.getGroupId();

        // check group id
        groupService.checkgroupId(groupId);

        // check userName
        TbUser userRow = queryByName(user.getUserName());
        if (userRow != null) {
            log.warn("fail addUserIndo. user info already exists");
            throw new NodeMgrException(ConstantCode.USER_EXISTS);
        }

        KeyPair keyPair = frontRestTools
            .getForEntity(groupId, FrontRestTools.URI_KEY_PAIR, KeyPair.class);
        String privateKey = Optional.ofNullable(keyPair).map(k -> k.getPrivateKey()).orElse(null);
        String publicKey = Optional.ofNullable(keyPair).map(k -> k.getPublicKey()).orElse(null);
        String address = Optional.ofNullable(keyPair).map(k -> k.getAddress()).orElse(null);

        if (StringUtils.isAnyBlank(privateKey, publicKey, address)) {
            log.warn("get key pair fail. privateKey:{} publicKey:{} address:{}", privateKey,
                publicKey, address);
            throw new NodeMgrException(ConstantCode.SYSTEM_EXCEPTION);
        }

        // add row
        TbUser newUserRow = new TbUser(HasPk.HAS.getValue(), user.getUserType(), user.getUserName(),
            groupId, address, publicKey,
            user.getDescription());
        Integer affectRow = userMapper.addUserRow(newUserRow);
        if (affectRow == 0) {
            log.warn("affect 0 rows of tb_user");
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }

        Integer userId = newUserRow.getUserId();

        // add user_key_mapping info
        String aesPrivateKey = aesTools.aesEncrypt(privateKey);
        TbUserKeyMap newMapRow = new TbUserKeyMap(userId, aesPrivateKey);
        Integer affectMapRow = userMapper.addUserKeyMapRow(newMapRow);
        if (affectMapRow == 0) {
            log.warn("affect 0 rows of tb_user_key_map");
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }
        // update monitor unusual user's info
        monitorService.updateUnusualUser(groupId, user.getUserName(), address);

        log.debug("end addNodeInfo userId:{}", userId);
        return userId;
    }

    /**
     * bind user info.
     */
    @Transactional
    public Integer bindUserInfo(User user) throws NodeMgrException {
        log.debug("start bindUserInfo User:{}", JSON.toJSONString(user));

        String publicKey = user.getPublicKey();
        if (StringUtils.isBlank(publicKey)) {
            log.info("fail bindUserInfo. publicKey cannot be empty");
            throw new NodeMgrException(ConstantCode.PUBLICKEY_NULL);
        }

        if (publicKey.length() != ConstantProperties.PUBLICKEY_LENGTH
            && publicKey.length() != ConstantProperties.ADDRESS_LENGTH) {
            log.info("fail bindUserInfo. publicKey length error");
            throw new NodeMgrException(ConstantCode.PUBLICKEY_LENGTH_ERROR);
        }

        // check group id
        groupService.checkgroupId(user.getGroupId());

        // check userName
        TbUser userRow = queryByName(user.getUserName());
        if (userRow != null) {
            log.warn("fail bindUserInfo. userName is already exists");
            throw new NodeMgrException(ConstantCode.USER_EXISTS);
        }
        String address = publicKey;
        if (publicKey.length() == ConstantProperties.PUBLICKEY_LENGTH) {
            address = Web3Tools.getAddressByPublicKey(publicKey);
        }

        // check address
        TbUser addressRow = queryByAddress(address);
        if (addressRow != null) {
            log.warn("fail bindUserInfo. address is already exists");
            throw new NodeMgrException(ConstantCode.USER_EXISTS);
        }

        // get org id
        //   TbOrganization orgRow = organizationService
        //       .queryOrganization(user.getGroupId(), OrgType.CURRENT.getValue());
        //  Integer orgId = Optional.ofNullable(orgRow).map(org -> org.getOrgId())
        //      .orElseThrow(() -> new NodeMgrException(ConstantCode.CURRENT_ORG_NOT_EXISTS));

        // add row
        TbUser newUserRow = new TbUser(HasPk.NONE.getValue(), user.getUserType(),
            user.getUserName(), user.getGroupId(), address, publicKey, user.getDescription());
        Integer affectRow = userMapper.addUserRow(newUserRow);
        if (affectRow == 0) {
            log.warn("bindUserInfo affect 0 rows of tb_user");
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }

        Integer userId = newUserRow.getUserId();

        // update monitor unusual user's info
        monitorService.updateUnusualUser(user.getGroupId(), user.getUserName(), address);

        log.debug("end bindUserInfo userId:{}", userId);
        return userId;
    }

    /**
     * query count of user.
     */
    public Integer countOfUser(UserParam userParam) throws NodeMgrException {
        log.debug("start countOfUser. userParam:{}", JSON.toJSONString(userParam));

        try {
            Integer count = userMapper.countOfUser(userParam);
            log.debug("end countOfUser userParam:{} count:{}", JSON.toJSONString(userParam), count);
            return count;
        } catch (RuntimeException ex) {
            log.error("fail countOfUser userParam:{}", JSON.toJSONString(userParam), ex);
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }
    }

    /**
     * query count of user.
     */
    private Integer countOfUser(Integer userId) throws NodeMgrException {
        UserParam userParam = new UserParam();
        userParam.setUserId(userId);
        return countOfUser(userParam);
    }

    /**
     * query user list by page.
     */
    public List<TbUser> qureyUserList(UserParam userParam) throws NodeMgrException {
        log.debug("start qureyUserList userParam:{}", JSON.toJSONString(userParam));
        // query user list
        List<TbUser> listOfUser = userMapper.listOfUser(userParam);
        log.debug("end qureyUserList listOfUser:{}", JSON.toJSONString(listOfUser));
        return listOfUser;
    }

    /**
     * query user row.
     */
    public TbUser queryUser(Integer userId, Integer groupId, String userName, String address)
        throws NodeMgrException {
        log.debug("start queryUser userId:{} groupId:{} userName:{} address:{}", userId,
            groupId, userName, address);
        try {
            TbUser userRow = userMapper.queryUser(userId, groupId, userName, address);
            log.debug(
                "end queryUser userId:{} groupId:{} userName:{}  address:{} TbUser:{}",
                userId, groupId, userName, address, JSON.toJSONString(userRow));
            return userRow;
        } catch (RuntimeException ex) {
            log.error("fail queryUser userId:{} groupId:{} userName:{}  address:{}",
                userId, groupId, userName, address, ex);
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }
    }

    /**
     * query by groupId、userName.
     */
    public TbUser queryUser(Integer groupId, String userName)
        throws NodeMgrException {
        return queryUser(null, groupId, userName, null);
    }

    /**
     * query by groupId.
     */
    public TbUser queryBygroupId(Integer groupId) throws NodeMgrException {
        return queryUser(null, groupId, null, null);
    }

    /**
     * query by userName.
     */
    public TbUser queryByName(String userName) throws NodeMgrException {
        return queryUser(null, null, userName, null);
    }

    /**
     * query by address.
     */
    public TbUser queryByAddress(String address) throws NodeMgrException {
        return queryUser(null, null, null, address);
    }

    /**
     * query by userId.
     */
    public TbUser queryByUserId(Integer userId) throws NodeMgrException {
        return queryUser(userId, null, null, null);
    }

    /**
     * update user info.
     */
    public void updateUser(User user) throws NodeMgrException {
        Integer userId = Optional.ofNullable(user).map(u -> u.getUserId()).orElse(null);
        String description = Optional.ofNullable(user).map(u -> u.getDescription()).orElse(null);
        TbUser tbUser = queryByUserId(userId);
        tbUser.setDescription(description);
        updateUser(tbUser);
    }

    /**
     * update user info.
     */
    public void updateUser(TbUser user) throws NodeMgrException {
        log.debug("start updateUser user", JSON.toJSONString(user));
        Integer userId = Optional.ofNullable(user).map(u -> u.getUserId()).orElse(null);
        String description = Optional.ofNullable(user).map(u -> u.getDescription()).orElse(null);
        if (userId == null) {
            log.warn("fail updateUser. user id is null");
            throw new NodeMgrException(ConstantCode.USER_ID_NULL);
        }
        TbUser tbUser = queryByUserId(userId);
        tbUser.setDescription(description);

        try {
            Integer affectRow = userMapper.updateUser(tbUser);
            if (affectRow == 0) {
                log.warn("affect 0 rows of tb_user");
                throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
            }
        } catch (RuntimeException ex) {
            log.error("fail updateUser  userId:{} description:{}", userId, description, ex);
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }

        log.debug("end updateOrtanization");
    }

    /**
     * get private key.
     */
    public PrivateKeyInfo getPrivateKey(Integer userId) throws NodeMgrException {
        log.debug("start getPrivateKey userId:{} ", userId);
        // check user id
        checkUserId(userId);

        PrivateKeyInfo privateKeyInfoInfo = userMapper.queryPrivateKey(userId);
        privateKeyInfoInfo.setPrivateKey(aesTools.aesDecrypt(privateKeyInfoInfo.getPrivateKey()));
        log.debug("end getPrivateKey,privateKeyInfoInfo:{}", JSON.toJSONString(privateKeyInfoInfo));
        return privateKeyInfoInfo;
    }

    /**
     * check usder id.
     */
    public void checkUserId(Integer userId) throws NodeMgrException {
        log.debug("start checkUserId userId:{}", userId);

        if (userId == null) {
            log.error("fail checkUserId userId is null");
            throw new NodeMgrException(ConstantCode.USER_ID_NULL);
        }

        Integer userCount = countOfUser(userId);
        log.debug("checkUserId userId:{} userCount:{}", userId, userCount);
        if (userCount == null || userCount == 0) {
            throw new NodeMgrException(ConstantCode.INVALID_USER_ID);
        }
        log.debug("end checkUserId");
    }

    /**
     * get user name by address.
     */
    public String queryUserNameByAddress(Integer groupId, String address)
        throws NodeMgrException {
        log.debug("queryUserNameByAddress address:{} ", address);
        String userName = userMapper.queryUserNameByAddress(groupId, address);
        log.debug("end queryUserNameByAddress");
        return userName;
    }
}
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
import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.enums.HasPk;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.base.properties.ConstantProperties;
import com.webank.webase.node.mgr.base.tools.Web3Tools;
import com.webank.webase.node.mgr.frontinterface.FrontRestTools;
import com.webank.webase.node.mgr.group.GroupService;
import com.webank.webase.node.mgr.monitor.MonitorService;
import com.webank.webase.node.mgr.user.entity.BindUserInputParam;
import com.webank.webase.node.mgr.user.entity.KeyPair;
import com.webank.webase.node.mgr.user.entity.NewUserInputParam;
import com.webank.webase.node.mgr.user.entity.TbUser;
import com.webank.webase.node.mgr.user.entity.UpdateUserInputParam;
import com.webank.webase.node.mgr.user.entity.UserParam;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
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
    private FrontRestTools frontRestTools;
    @Lazy
    @Autowired
    private MonitorService monitorService;

    /**
     * add new user data.
     */
    @Transactional
    public Integer addUserInfo(NewUserInputParam user) throws NodeMgrException {
        log.debug("start addUserInfo User:{}", JSON.toJSONString(user));

        Integer groupId = user.getGroupId();

        // check group id
        groupService.checkGroupId(groupId);

        // check userName
        TbUser userRow = queryByName(user.getGroupId(), user.getUserName());
        if (userRow != null) {
            log.warn("fail addUserInfo. user info already exists");
            throw new NodeMgrException(ConstantCode.USER_EXISTS);
        }
        // add user by webase-front->webase-sign
        String signUserId = UUID.randomUUID().toString().replaceAll("-","");
        // group id as appId
        String appId = user.getGroupId().toString();
        String keyUri = String.format(FrontRestTools.URI_KEY_PAIR, user.getUserName(), signUserId, appId);
        KeyPair keyPair = frontRestTools
            .getForEntity(groupId, keyUri, KeyPair.class);
        String publicKey = Optional.ofNullable(keyPair).map(k -> k.getPublicKey()).orElse(null);
        String address = Optional.ofNullable(keyPair).map(k -> k.getAddress()).orElse(null);

        if (StringUtils.isAnyBlank(publicKey, address)) {
            log.warn("get key pair fail. publicKey:{} address:{}", publicKey, address);
            throw new NodeMgrException(ConstantCode.SYSTEM_EXCEPTION_GET_PRIVATE_KEY_FAIL);
        }

        // add row
        TbUser newUserRow = new TbUser(HasPk.HAS.getValue(), user.getUserType(), user.getUserName(),
            groupId, address, publicKey,
            user.getDescription());
        newUserRow.setSignUserId(signUserId);
        newUserRow.setAppId(appId);
        Integer affectRow = userMapper.addUserRow(newUserRow);
        if (affectRow == 0) {
            log.warn("affect 0 rows of tb_user");
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }

        // update monitor unusual user's info
        monitorService.updateUnusualUser(groupId, user.getUserName(), address);

        Integer userId = newUserRow.getUserId();
        log.debug("end addNodeInfo userId:{}", userId);
        return userId;
    }

    /**
     * bind user info.
     */
    @Transactional
    public Integer bindUserInfo(BindUserInputParam user) throws NodeMgrException {
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
        groupService.checkGroupId(user.getGroupId());

        // check userName
        TbUser userRow = queryByName(user.getGroupId(), user.getUserName());
        if (Objects.nonNull(userRow)) {
            log.warn("fail bindUserInfo. userName is already exists");
            throw new NodeMgrException(ConstantCode.USER_EXISTS);
        }
        String address = publicKey;
        if (publicKey.length() == ConstantProperties.PUBLICKEY_LENGTH) {
            address = Web3Tools.getAddressByPublicKey(publicKey);
        }

        // check address
        TbUser addressRow = queryUser(null, user.getGroupId(), null, address);
        if (Objects.nonNull(addressRow)) {
            log.warn("fail bindUserInfo. address is already exists");
            throw new NodeMgrException(ConstantCode.USER_EXISTS);
        }

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
     * query by userName.
     */
    public TbUser queryByName(int groupId, String userName) throws NodeMgrException {
        return queryUser(null, groupId, userName, null);
    }


    /**
     * query by userId.
     */
    public TbUser queryByUserId(Integer userId) throws NodeMgrException {
        return queryUser(userId, null, null, null);
    }

    /**
     * query by group id and address.
     */
    public String getSignUserIdByAddress(int groupId, String address) throws NodeMgrException {
        TbUser user = queryUser(null, groupId, null, address);
        return user.getSignUserId();
    }

    /**
     * update user info.
     */
    public void updateUser(UpdateUserInputParam user) throws NodeMgrException {
        TbUser tbUser = queryByUserId(user.getUserId());
        tbUser.setDescription(user.getDescription());
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
     * get user name by address.
     */
    public String queryUserNameByAddress(Integer groupId, String address)
        throws NodeMgrException {
        log.debug("queryUserNameByAddress address:{} ", address);
        String userName = userMapper.queryUserNameByAddress(groupId, address);
        log.debug("end queryUserNameByAddress");
        return userName;
    }

    public void deleteByAddress(String address) throws NodeMgrException{
        log.debug("deleteByAddress address:{} ", address);
        userMapper.deleteByAddress(address);
        log.debug("end deleteByAddress");
    }
}
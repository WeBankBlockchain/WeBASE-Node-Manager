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
package com.webank.webase.node.mgr.user;

import com.webank.webase.node.mgr.user.entity.PrivateKeyInfo;
import com.webank.webase.node.mgr.user.entity.TbUser;
import com.webank.webase.node.mgr.user.entity.TbUserKeyMap;
import com.webank.webase.node.mgr.user.entity.UserParam;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * user data interface.
 */
@Repository
public interface UserMapper {

    /**
     * Add new user data.
     */
    Integer addUserRow(TbUser tbUser);

    /**
     * Query the number of user according to some conditions.
     */
    Integer countOfUser(UserParam userParam);

    /**
     * Query user list according to some conditions.
     */
    List<TbUser> listOfUser(UserParam userParam);

    /**
     * Query user list according to some conditions.
     */
    TbUser queryUser(@Param("userId") Integer userId, @Param("groupId") Integer groupId,
        @Param("userName") String userName, @Param("address") String address);

    /**
     * update user row.
     */
    Integer updateUser(TbUser tbuser);

    /**
     * add new user_key_map data.
     */
    Integer addUserKeyMapRow(TbUserKeyMap tbUserKeyMap);

    /**
     * query private key by user id.
     */
    PrivateKeyInfo queryPrivateKey(Integer userId);

    /**
     * query max chainIndex of user.
     */
    String queryUserNameByAddress(@Param("groupId") Integer groupId,
        @Param("address") String address);

    /**
     * query system User.
     */
    TbUser querySystemUser();
}

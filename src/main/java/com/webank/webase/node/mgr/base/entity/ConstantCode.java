/*
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
package com.webank.webase.node.mgr.base.entity;

/**
 * A-BB-CCC A:error level. <br/>
 * 1:system exception <br/>
 * 2:business exception <br/>
 * B:project number <br/>
 * webase-node-mgr:02 <br/>
 * C: error code <br/>
 */
public class ConstantCode {

    /* return success */
    public static final RetCode SUCCESS = RetCode.mark(0, "success");

    /* system exception */
    public static final RetCode SYSTEM_EXCEPTION = RetCode.mark(102000, "system exception");

    /**
     * Business exception.
     */
    public static final RetCode INVALID_NODE_INFO = RetCode.mark(202000, "invalid node info");

    public static final RetCode DB_EXCEPTION = RetCode.mark(202001, "database exception");

    public static final RetCode ORG_EXISTS = RetCode.mark(202002, "organization already exists");

    public static final RetCode ORG_NOT_EXISTS = RetCode.mark(202003, "organization not exists");

    public static final RetCode NODE_EXISTS = RetCode.mark(202004, "node already exists");

    public static final RetCode GROUP_ID_NULL = RetCode
        .mark(202005, "group id cannot be empty");

    public static final RetCode INVALID_GROUP_ID = RetCode.mark(202006, "invalid group id");

    public static final RetCode ORG_ID_NULL = RetCode
        .mark(202007, "organization id cannot be empty");

    public static final RetCode INVALID_ORG_ID = RetCode.mark(202008, "invalid organization id");

    public static final RetCode NET_ORG_MAP_EXISTS = RetCode
        .mark(202009, "group_organization_mapping already exists");

    public static final RetCode NET_ORG_MAP_NOT_EXISTS = RetCode
        .mark(202010, "group_organization_mapping not exists");

    public static final RetCode CURRENT_ORG_NOT_EXISTS = RetCode
        .mark(202011, "did not find the current organization");

    public static final RetCode USER_ID_NULL = RetCode.mark(202012, "user id cannot be empty");

    public static final RetCode INVALID_USER_ID = RetCode.mark(202013, "invalid user id");

    public static final RetCode USER_EXISTS = RetCode.mark(202014, "user already exists");

    public static final RetCode CONTRACT_EXISTS = RetCode.mark(202015, "contract already exists");

    public static final RetCode CONTRACT_ID_NULL = RetCode
        .mark(202016, "contract id cannot be empty");

    public static final RetCode INVALID_CONTRACT_ID = RetCode.mark(202017, "invalid contract id");

    public static final RetCode INVALID_PARAM_INFO = RetCode.mark(202018, "invalid param info");

    public static final RetCode NODE_NOT_EXISTS = RetCode.mark(202019, "did not find node info");

    public static final RetCode CURRENT_ORG_EXISTS = RetCode
        .mark(202020, "current organization already exists");

    public static final RetCode INVALID_ORG_TYPE = RetCode
        .mark(202021, "invalid organization type");

    public static final RetCode DELETE_DEPLOYED_CONTRACT = RetCode
        .mark(202022, "unable to delete deployed contract");

    public static final RetCode NODE_IP_EMPTY = RetCode.mark(202023, "node ip cannot be empty");

    public static final RetCode NODE_P2P_PORT_EMPTY = RetCode
        .mark(202024, "node p2p port cannot be empty");

    public static final RetCode NODE_LOG_NOT_EXISTS = RetCode.mark(202025, "did not find node log");

    public static final RetCode ACCOUNT_EXISTS = RetCode
        .mark(202026, "account info already exists");

    public static final RetCode ACCOUNT_NOT_EXISTS = RetCode
        .mark(202027, "account info not exists");

    public static final RetCode ACCOUNT_NAME_EMPTY = RetCode.mark(202028, "account name empty");

    public static final RetCode INVALID_ACCOUNT_NAME = RetCode.mark(202029, "invalid account name");

    public static final RetCode PASSWORD_ERROR = RetCode.mark(202030, "password error");

    public static final RetCode ROLE_ID_EMPTY = RetCode.mark(202031, "role id cannot be empty");

    public static final RetCode INVALID_ROLE_ID = RetCode.mark(202032, "invalid role id");

    public static final RetCode INVALID_ATTR = RetCode.mark(202033, "invalid attr");

    public static final RetCode LOGIN_FAIL = RetCode.mark(202034, "login fail");

    public static final RetCode CONTRACT_HAS_BEAN_DEPLOYED = RetCode
        .mark(202035, "contract has been deployed");

    public static final RetCode PUBLICKEY_NULL = RetCode.mark(202036, "publicKey cannot be empty");

    public static final RetCode USER_DOES_NOT_EXISTS = RetCode.mark(202037, "user does not exist");

    public static final RetCode INTERFACE_DOES_NOT_EXISTS = RetCode
        .mark(202038, "interface does not exist");

    public static final RetCode NOT_SAVE_BLOCK = RetCode
        .mark(202039, "do not save this block height");

    public static final RetCode CONTRACT_DEPLOY_FAIL = RetCode
        .mark(202040, "contract deploy not success");

    public static final RetCode INVALID_USER_INDEX = RetCode.mark(202041, "invalid user index");

    public static final RetCode INVALID_CONTRACT_INDEX = RetCode
        .mark(202042, "invalid contract index");

    public static final RetCode NOT_FOUND_CONTRACTDETAIL = RetCode
        .mark(202043, "did not found system contract:contractDetail");

    public static final RetCode NOT_FOUND_USERCONTRACT = RetCode
        .mark(202044, "did not found system contract:user");

    public static final RetCode NOW_PWD_EQUALS_OLD = RetCode
        .mark(202045, "the new password cannot be same as old");

    public static final RetCode CONTRACT_HAS_NOT_COMPILE = RetCode
        .mark(202046, "contract has not compiled");

    public static final RetCode NOT_FOUND_NODECONTRACT = RetCode
        .mark(202047, "did not found system contract:node");

    public static final RetCode INVALID_NODE_INDEX = RetCode.mark(202048, "invalid node index");

    public static final RetCode CONTRACT_NAME_EMPTY = RetCode
        .mark(202049, "contract name is empty");

    public static final RetCode PUBLICKEY_LENGTH_ERROR = RetCode
        .mark(202050, "publicKey's length is 130,address's length is 42");

    public static final RetCode INVALID_NODE_IP = RetCode.mark(202051, "invalid node ip");

    public static final RetCode CURRENT_NODE_NOT_EXISTS = RetCode
        .mark(202052, "did not find current node info");

    public static final RetCode NOT_FOUND_SYSTEM_USER = RetCode
        .mark(202053, "system user has not been initialized yet");

    public static final RetCode CONTRACT_HAD_NOT_DEPLOY = RetCode
        .mark(202054, "contract had not deploy");

    public static final RetCode INVALID_CONTRACT = RetCode.mark(202055, "invalid contract");

    public static final RetCode INVALID_NODE_TYPE = RetCode.mark(202056, "invalid node type");

    public static final RetCode IP_PORT_EMPTY = RetCode.mark(202057, " ip or frontPort is empty");

    public static final RetCode NOT_SUPPORT_TRANS = RetCode.mark(202058, "not support transaction");

    public static final RetCode INVALID_NODE_ID = RetCode.mark(202059, "invalid node id");

    /* auth */
    public static final RetCode USER_NOT_LOGGED_IN = RetCode.mark(302000, "user not logged in");
    public static final RetCode ACCESS_DENIED = RetCode.mark(302001, "access denied");

}

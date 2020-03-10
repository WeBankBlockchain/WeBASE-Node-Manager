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
package com.webank.webase.node.mgr.base.code;

/**
 * A-BB-CCC A:error level. <br/>
 * 1:system exception <br/>
 * 2:business exception <br/>
 * B:project number <br/>
 * WeBASE-Node-Manager:02 <br/>
 * C: error code <br/>
 */
public class ConstantCode {

    /* return success */
    public static final RetCode SUCCESS = RetCode.mark(0, "success");

    /* system exception */
    public static final RetCode SYSTEM_EXCEPTION = RetCode.mark(102000, "system exception");
    public static final RetCode SYSTEM_EXCEPTION_GET_PRIVATE_KEY_FAIL = RetCode.mark(102000, "system exception: please check front");

    /**
     * Business exception.
     */
    public static final RetCode INVALID_FRONT_ID = RetCode.mark(202000, "invalid front id");

    public static final RetCode DB_EXCEPTION = RetCode.mark(202001, "database exception");

    public static final RetCode FRONT_LIST_NOT_FOUNT = RetCode.mark(202002, "not found any front");

    public static final RetCode INVALID_FRONT_IP = RetCode.mark(202003, "not support this ip");

    public static final RetCode FRONT_EXISTS = RetCode.mark(202004, "front already exists");

    public static final RetCode GROUP_ID_NULL = RetCode
        .mark(202005, "group id cannot be empty");

    public static final RetCode INVALID_GROUP_ID = RetCode.mark(202006, "invalid group id");

    public static final RetCode CHECK_CODE_NULL = RetCode.mark(202007, "checkCode is null");

    public static final RetCode INVALID_CHECK_CODE = RetCode.mark(202008, "invalid checkCode");

    public static final RetCode SAVE_FRONT_FAIL = RetCode.mark(202009, "save front fail");

    public static final RetCode REQUEST_FRONT_FAIL = RetCode.mark(202010,"request front fail");

    public static final RetCode CONTRACT_ABI_EMPTY = RetCode.mark(202011, "abiInfo cannot be empty");

    public static final RetCode USER_ID_NULL = RetCode.mark(202012, "user id cannot be empty");

    public static final RetCode INVALID_USER = RetCode.mark(202013, "invalid user");

    public static final RetCode USER_EXISTS = RetCode.mark(202014, "user already exists");

    public static final RetCode CONTRACT_EXISTS = RetCode.mark(202015, "contract already exists");

    public static final RetCode INVALID_CONTRACT_ID = RetCode.mark(202017, "invalid contract id");

    public static final RetCode INVALID_PARAM_INFO = RetCode.mark(202018, "invalid param info");

    public static final RetCode CONTRACT_NAME_REPEAT = RetCode.mark(202019, "contract name cannot be repeated");

    public static final RetCode CONTRACT_NOT_DEPLOY = RetCode.mark(202023, "contract has not deploy");

    public static final RetCode ACCOUNT_EXISTS = RetCode
        .mark(202026, "account info already exists");

    public static final RetCode ACCOUNT_NOT_EXISTS = RetCode
        .mark(202027, "account info not exists");

    public static final RetCode ACCOUNT_NAME_EMPTY = RetCode.mark(202028, "account name empty");

    public static final RetCode INVALID_ACCOUNT_NAME = RetCode.mark(202029, "invalid account name");

    public static final RetCode PASSWORD_ERROR = RetCode.mark(202030, "password error");

    public static final RetCode ROLE_ID_EMPTY = RetCode.mark(202031, "role id cannot be empty");

    public static final RetCode INVALID_ROLE_ID = RetCode.mark(202032, "invalid role id");

    public static final RetCode CONTRACT_ADDRESS_INVALID = RetCode.mark(202033, "invalid contract address");

    public static final RetCode LOGIN_FAIL = RetCode.mark(202034, "login fail");

    public static final RetCode CONTRACT_HAS_BEAN_DEPLOYED = RetCode
        .mark(202035, "contract has been deployed");

    public static final RetCode PUBLICKEY_NULL = RetCode.mark(202036, "publicKey cannot be empty");

    public static final RetCode CONTRACT_DEPLOY_FAIL = RetCode
        .mark(202040, "contract deploy not success");

    public static final RetCode NOW_PWD_EQUALS_OLD = RetCode
        .mark(202045, "the new password cannot be same as old");

    public static final RetCode PUBLICKEY_LENGTH_ERROR = RetCode
        .mark(202050, "publicKey's length is 130,address's length is 42");

    public static final RetCode SERVER_CONNECT_FAIL = RetCode
        .mark(202051, "wrong host or port");
    public static final RetCode INVALID_TOKEN = RetCode.mark(202052, "invalid token");
    public static final RetCode TOKEN_EXPIRE = RetCode.mark(202053, "token expire");

    // 证书管理
    public static final RetCode CERT_ERROR = RetCode.mark(202060, "cert handle error");
    public static final RetCode FAIL_SAVE_CERT_ERROR = RetCode.mark(202061, "store cert error");
    public static final RetCode CERT_FORMAT_ERROR = RetCode.mark(202062,
            "cert format error, must start with -----BEGIN CERTIFICATE-----\\n, end with end");
    public static final RetCode SAVING_FRONT_CERT_ERROR = RetCode.mark(202063, "saving front's cert error");

    // 邮件告警错误
    public static final RetCode MAIL_SERVER_CONFIG_ERROR = RetCode.mark(202070, "Mail server config error.");
    public static final RetCode MAIL_SERVER_CONFIG__PARAM_EMPTY = RetCode.mark(202071,
            "Mail server config param empty/not match.");
    public static final RetCode MAIL_SERVER_CONFIG_ERROR_NO_DATA_IN_DB = RetCode.mark(202072,
            "Mail server config error, db's server config is empty");
    public static final RetCode ALERT_RULE_ERROR = RetCode.mark(202076, "Alert rule error.");
    public static final RetCode ALERT_RULE_PARAM_EMPTY = RetCode.mark(202077, "Alert rule param not match.");
    public static final RetCode SEND_MAIL_ERROR = RetCode.mark(202080,
            "Send mail error, please check mail server configuration.");
    public static final RetCode SEND_MAIL_ERROR_FOR_SERVER_IS_OFF = RetCode.mark(202081,
            "Send mail error, please enable mail server before send.");
    public static final RetCode ALERT_LOG_ERROR = RetCode.mark(202086, "Alert log error.");
    public static final RetCode ALERT_LOG_PARAM_EMPTY = RetCode.mark(202087,
            "Alert log param: status/logId is empty.");

    /* guomi exception */
    public static final RetCode UPDATE_METHOD_ID_GM_ERROR = RetCode.mark(202090, "Update guomi methodId error");
    public static final RetCode ENCRYPT_TYPE_NOT_MATCH = RetCode.mark(202091,
            "Front's encrypt type not matches with nodemgr");


    /* auth */
    public static final RetCode USER_NOT_LOGGED_IN = RetCode.mark(302000, "user not logged in");
    public static final RetCode ACCESS_DENIED = RetCode.mark(302001, "access denied");

    /* param exception */
    public static final RetCode PARAM_EXCEPTION = RetCode.mark(402000, "param exception");

}

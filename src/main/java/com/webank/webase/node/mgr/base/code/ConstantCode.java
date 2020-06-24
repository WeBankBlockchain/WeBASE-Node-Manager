/**
 * Copyright 2014-2020  the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
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

    /* front error code */
    public static final RetCode PERMISSION_DENIED = RetCode.mark(201202, "permission denied");
    public static final RetCode CERT_FILE_NOT_FOUND = RetCode.mark(201231, "Cert file not found, please check cert path in config");
    public static final RetCode PEM_FORMAT_ERROR = RetCode.mark(201232, "Pem file format error, must surrounded by -----XXXXX PRIVATE KEY-----");
    public static final RetCode PEM_CONTENT_ERROR = RetCode.mark(201234, "Pem file content error");
    public static final RetCode P12_PASSWORD_NOT_CHINESE = RetCode.mark(201235, "p12's password cannot be chinese");
    public static final RetCode P12_PASSWORD_ERROR = RetCode.mark(201236, "p12's password not match");
    public static final RetCode P12_FILE_ERROR = RetCode.mark(201237, "P12 file content error");

    /**
     * Business exception.
     */
    public static final RetCode INVALID_FRONT_ID = RetCode.mark(202000, "invalid front id");

    public static final RetCode DB_EXCEPTION = RetCode.mark(202001, "database exception");

    public static final RetCode FRONT_LIST_NOT_FOUNT = RetCode.mark(202002, "not found any front for this group");

    public static final RetCode INVALID_FRONT_IP = RetCode.mark(202003, "not support this ip");

    public static final RetCode FRONT_EXISTS = RetCode.mark(202004, "front already exists");

    public static final RetCode GROUP_ID_NULL = RetCode
            .mark(202005, "group id cannot be empty");

    public static final RetCode INVALID_GROUP_ID = RetCode.mark(202006, "invalid group id");

    public static final RetCode CHECK_CODE_NULL = RetCode.mark(202007, "checkCode is null");

    public static final RetCode INVALID_CHECK_CODE = RetCode.mark(202008, "invalid checkCode");

    public static final RetCode SAVE_FRONT_FAIL = RetCode.mark(202009, "save front fail");

    public static final RetCode REQUEST_FRONT_FAIL = RetCode.mark(202010, "request front fail");

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

    public static final RetCode AVAILABLE_FRONT_URL_IS_NULL = RetCode.mark(202054, "Available front url is empty, check front status");

    // 证书管理
    public static final RetCode CERT_ERROR = RetCode.mark(202060, "cert handle error");
    public static final RetCode FAIL_SAVE_CERT_ERROR = RetCode.mark(202061, "store cert error");
    public static final RetCode CERT_FORMAT_ERROR = RetCode.mark(202062,
            "cert format error, must start with -----BEGIN CERTIFICATE-----\\n, end with end");
    public static final RetCode SAVING_FRONT_CERT_ERROR = RetCode.mark(202063, "saving front's cert error");

    // 邮件告警错误
    public static final RetCode MAIL_SERVER_CONFIG_ERROR = RetCode.mark(202070, "Mail server config error.");
    public static final RetCode MAIL_SERVER_CONFIG_PARAM_EMPTY = RetCode.mark(202071,
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

    // abi import
    public static final RetCode CONTRACT_ADDRESS_ALREADY_EXISTS = RetCode.mark(202096, "contract address already exists");
    public static final RetCode ABI_INFO_NOT_EXISTS = RetCode.mark(202097, "abi info of this id not exists");
    public static final RetCode PARAM_FAIL_ABI_INVALID = RetCode.mark(202098, "Contract abi invalid, please check abi");
    public static final RetCode PARAM_FAIL_ABI_ID_EMPTY = RetCode.mark(202099, "Abi Id cannot be empty");
    public static final RetCode CONTRACT_ADDRESS_NULL = RetCode.mark(202100, "contractAddress is null");

    public static final RetCode USER_NOT_EXIST = RetCode.mark(202110, "User's signUserId not exist");
    /* Json parse error */
    public static final RetCode FAIL_PARSE_JSON = RetCode.mark(202111, "Fail to parse json");

    /* dynamic group manage */
    public static final RetCode GROUP_ID_EXISTS = RetCode.mark(202300, "group id already exists");
    public static final RetCode NODE_NOT_EXISTS = RetCode.mark(202301, "node's front not exists");


    // add in v1.4.0
    public static final RetCode NO_DOCKER_TAG_UPDATE_URL_ERROR = RetCode.mark(205001, "No docker image tag update url.");
    public static final RetCode UPDATE_DOCKER_TAG_ERROR = RetCode.mark(205002, "Update docker tag from registry error.");
    public static final RetCode UNKNOWN_CONFIG_TYPE_ERROR = RetCode.mark(205003, "Unknown config type.");
    public static final RetCode SAVE_IP_CONFIG_FILE_ERROR = RetCode.mark(205004, "Save IP config error.");
    public static final RetCode TAG_ID_PARAM_ERROR = RetCode.mark(205005, "Tag id param error.");
    public static final RetCode IP_CONF_PARAM_NULL_ERROR = RetCode.mark(205006, "ipconf null.");
    public static final RetCode CHAIN_NAME_EXISTS_ERROR = RetCode.mark(205007, "Chain name exists.");
    public static final RetCode INSERT_CHAIN_ERROR = RetCode.mark(205008, "Insert new chain failed.");
    public static final RetCode NO_CONFIG_FILE_ERROR = RetCode.mark(205009, "No ipconf file.");
    public static final RetCode EXEC_BUILD_CHAIN_ERROR = RetCode.mark(205010, "Exec build chain script failed.");
    public static final RetCode IP_CONFIG_LINE_ERROR = RetCode.mark(205011, "ipconf line error.");
    public static final RetCode IP_NUM_ERROR = RetCode.mark(205012, "IP and num config error.");
    public static final RetCode AGENCY_NAME_CONFIG_ERROR = RetCode.mark(205013, "Agency name config error.");
    public static final RetCode GROUPS_CONFIG_ERROR = RetCode.mark(205014, "Groups config error.");
    public static final RetCode HOST_CONNECT_ERROR = RetCode.mark(205015, "Connect to host error.");
    public static final RetCode INSERT_AGENCY_ERROR = RetCode.mark(205016, "Insert new agency failed.");
    public static final RetCode INSERT_GROUP_ERROR = RetCode.mark(205017, "Insert new group failed.");
    public static final RetCode INSERT_HOST_ERROR = RetCode.mark(205018, "Insert new host failed.");
    public static final RetCode INSERT_FRONT_ERROR = RetCode.mark(205019, "Insert new front failed.");
    public static final RetCode INSERT_NODE_ERROR = RetCode.mark(205020, "Insert new node failed.");
    public static final RetCode INSERT_FRONT_GROUP_ERROR = RetCode.mark(205021, "Insert new front node group failed.");
    public static final RetCode PARSE_HOST_INDEX_ERROR = RetCode.mark(205022, "Parse host index from node directory failed.");
    public static final RetCode HOST_ONLY_BELONGS_ONE_AGENCY_ERROR = RetCode.mark(205023, "A host only belongs to one agency.");
    public static final RetCode DEPLOY_WITH_UNKNOWN_EXCEPTION_ERROR = RetCode.mark(205024, "Unexpected exception occurred when deploy.");
    public static final RetCode UNSUPPORTED_PASSWORD_SSH_ERROR = RetCode.mark(205025, "SSH password login not supported yet.");
    public static final RetCode CHAIN_WITH_NO_AGENCY_ERROR = RetCode.mark(205026, "Chain has no agency.");
    public static final RetCode CHAIN_NAME_NOT_EXISTS_ERROR = RetCode.mark(205027, "Chain name not exists.");
    public static final RetCode IP_FORMAT_ERROR = RetCode.mark(205028, "IP format error.");
    public static final RetCode AGENCY_NAME_EMPTY_ERROR = RetCode.mark(205029, "Agency name is null when host ip is new.");
    public static final RetCode AGENCY_NAME_EXISTS_ERROR = RetCode.mark(205030, "Agency name exists when host ip is new.");
    public static final RetCode ADD_NODE_WITH_UNKNOWN_EXCEPTION_ERROR = RetCode.mark(205031, "Unexpected exception occurred when add new node.");
    public static final RetCode CHAIN_CERT_NOT_EXISTS_ERROR = RetCode.mark(205032, "Chain cert directory not exists.");
    public static final RetCode EXEC_GEN_AGENCY_ERROR = RetCode.mark(205033, "Exec generate agency script failed.");
    public static final RetCode HOST_WITH_NO_AGENCY_ERROR = RetCode.mark(205034, "Host's agency is null.");
    public static final RetCode NODES_NUM_ERROR = RetCode.mark(205035, "Num should be positive integer and less then 200.");
    public static final RetCode EXEC_GEN_SDK_ERROR = RetCode.mark(205036, "Exec generate node script to generate sdk dir failed.");
    public static final RetCode EXEC_GEN_NODE_ERROR = RetCode.mark(205037, "Exec generate node script to generate node dir failed.");
    public static final RetCode COPY_SDK_FILES_ERROR = RetCode.mark(205038, "Copy sdk config files error.");
    public static final RetCode SEND_SDK_FILES_ERROR = RetCode.mark(205039, "Send sdk config files error.");
    public static final RetCode SEND_NODE_FILES_ERROR = RetCode.mark(205040, "Send node config files error.");
    public static final RetCode COPY_GROUP_FILES_ERROR = RetCode.mark(205041, "Copy original group config files error.");
    public static final RetCode DELETE_OLD_AGENCY_DIR_ERROR = RetCode.mark(205042, "Delete old agency config files error.");
    public static final RetCode DELETE_OLD_SDK_DIR_ERROR = RetCode.mark(205043, "Delete old sdk of host config files error.");
    public static final RetCode DELETE_OLD_NODE_DIR_ERROR = RetCode.mark(205044, "Delete old node config files error.");
    public static final RetCode NODE_ID_NOT_EXISTS_ERROR = RetCode.mark(205045, "Nodeid not exists.");
    public static final RetCode STOP_NODE_ERROR = RetCode.mark(205046, "Stop node failed.");
    public static final RetCode START_NODE_ERROR = RetCode.mark(205047, "Start node failed.");
    public static final RetCode UPGRADE_WITH_SAME_TAG_ERROR = RetCode.mark(205048, "New image tag and current are the same.");
    public static final RetCode UPDATE_CHAIN_WITH_NEW_VERSION_ERROR = RetCode.mark(205049, "Update chain version error.");
    public static final RetCode NODE_IN_GROUP_ERROR = RetCode.mark(205050, "Node still in group, remove before deleting.");
    public static final RetCode READ_NODE_CONFIG_ERROR = RetCode.mark(205051, "Read node config error.");
    public static final RetCode DELETE_NODE_DIR_ERROR = RetCode.mark(205052, "Delete node config files error.");
    public static final RetCode NODE_RUNNING_ERROR = RetCode.mark(205053, "Node is running.");
    public static final RetCode UPDATE_RELATED_NODE_ERROR = RetCode.mark(205054, "Update related nodes error.");
    public static final RetCode DELETE_CHAIN_ERROR = RetCode.mark(205055, "Delete chain error.");
    public static final RetCode NODE_NEED_REMOVE_FROM_GROUP_ERROR = RetCode.mark(205056, "Node is sealer or observer, remove from group first.");
    public static final RetCode LIST_HOST_NODE_DIR_ERROR = RetCode.mark(205057, "List node dirs of host error.");
    public static final RetCode GENERATE_FRONT_YML_ERROR = RetCode.mark(205058, "Generate front application.yml file failed.");
    public static final RetCode EXEC_HOST_INIT_SCRIPT_ERROR = RetCode.mark(205059, "Exec host init script failed.");
    public static final RetCode TRANSFER_FILES_ERROR = RetCode.mark(205060, "Transfer files error.");
    public static final RetCode DOCKER_OPERATION_ERROR = RetCode.mark(205061, "Docker option error.");

    /* auth */
    public static final RetCode USER_NOT_LOGGED_IN = RetCode.mark(302000, "user not logged in");
    public static final RetCode ACCESS_DENIED = RetCode.mark(302001, "access denied");

    /* param exception */
    public static final RetCode PARAM_EXCEPTION = RetCode.mark(402000, "param exception");
}


-- ----------------------------
-- 1、init tb_account_info data   admin/Abcd1234 // 国密版 sm3.hash(Abcd123)
-- ----------------------------
INSERT INTO tb_account_info (account,account_pwd,role_id,create_time,modify_time)values('admin', '$2a$10$F/aEB1iEx/FvVh0fMn6L/uyy.PkpTy8Kd9EdbqLGo7Bw7eCivpq.m',100000,now(),now());


-- ----------------------------
-- 2、init tb_role data
-- ----------------------------
INSERT INTO `tb_role` (role_name,role_name_zh,create_time,modify_time)VALUES ('admin', '管理员', now(), now());
INSERT INTO `tb_role` (role_name,role_name_zh,create_time,modify_time)VALUES ('visitor', '普通用户', now(), now());


-- ----------------------------
-- 3、init tb_user data  国密版
-- ----------------------------
-- INSERT INTO tb_user(user_name, public_key, user_status, user_type, address, has_pk, create_time, modify_time) VALUES ( 'systemUser', '0xc5d877bff9923af55f248fb48b8907dc7d00cac3ba19b4259aebefe325510af7bd0a75e9a8e8234aa7aa58bc70510ee4bef02201a86006196da4e771c47b71b4', 1, 2, '0xf1585b8d0e08a0a00fff662e24d67ba95a438256', 1, Now(), Now());



-- ----------------------------
-- 4、init tb_user_key_mapping data  国密版
-- ----------------------------
-- INSERT INTO tb_user_key_mapping(user_id, private_key, map_status, create_time, modify_time) VALUES (700001, 'SzK9KCjpyVCW0T9K9r/MSlmcpkeckYKVn/D1X7fzzp18MM7yHhUHQugTxKXVJJY5XWOb4zZ79IXMBu77zmXsr0mCRnATZTUqFfWLX6tUBIw=', 1, now(), now());


-- ----------------------------
-- 5、init tb_method (repeated methodId is removed, ex: remove(string)) methodId 国密版
-- ----------------------------
-- (system config info 0x1000) setValueByKey
INSERT INTO `tb_method`(`method_id`, `group_id`, `abi_info`, `method_type`, `contract_type`, `create_time`, `modify_time`) VALUES ('0x0749b518', 0, '{\"constant\":false,\"inputs\":[{\"name\":\"key\",\"type\":\"string\"},{\"name\":\"value\",\"type\":\"string\"}],\"name\":\"setValueByKey\",\"outputs\":[{\"name\":\"\",\"type\":\"int256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"}', 'function', 1, '2019-09-02 16:32:30', '2019-09-02 16:32:30');
-- (table factory 0x1001) createTable
INSERT INTO `tb_method`(`method_id`, `group_id`, `abi_info`, `method_type`, `contract_type`, `create_time`, `modify_time`) VALUES ('0xc92a7801', 0, '{\"constant\":false,\"inputs\":[{\"name\":\"tableName\",\"type\":\"string\"},{\"name\":\"key\",\"type\":\"string\"},{\"name\":\"valueField\",\"type\":\"string\"}],\"name\":\"createTable\",\"outputs\":[{\"name\":\"\",\"type\":\"int256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"}', 'function', 1, '2019-09-02 16:32:30', '2019-09-02 16:32:30');
-- (crud info 0x1002) update select remove insert(same as cns's insert)
INSERT INTO `tb_method`(`method_id`, `group_id`, `abi_info`, `method_type`, `contract_type`, `create_time`, `modify_time`) VALUES ('0x10bd675b', 0, '{\"constant\":false,\"inputs\":[{\"name\":\"tableName\",\"type\":\"string\"},{\"name\":\"key\",\"type\":\"string\"},{\"name\":\"entry\",\"type\":\"string\"},{\"name\":\"condition\",\"type\":\"string\"},{\"name\":\"optional\",\"type\":\"string\"}],\"name\":\"update\",\"outputs\":[{\"name\":\"\",\"type\":\"int256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"}', 'function', 1, '2019-09-02 16:32:30', '2019-09-02 16:32:30');
INSERT INTO `tb_method`(`method_id`, `group_id`, `abi_info`, `method_type`, `contract_type`, `create_time`, `modify_time`) VALUES ('0x7388111f', 0, '{\"constant\":true,\"inputs\":[{\"name\":\"tableName\",\"type\":\"string\"},{\"name\":\"key\",\"type\":\"string\"},{\"name\":\"condition\",\"type\":\"string\"},{\"name\":\"optional\",\"type\":\"string\"}],\"name\":\"select\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"}', 'function', 1, '2019-09-02 16:32:30', '2019-09-02 16:32:30');
INSERT INTO `tb_method`(`method_id`, `group_id`, `abi_info`, `method_type`, `contract_type`, `create_time`, `modify_time`) VALUES ('0x81b81824', 0, '{\"constant\":false,\"inputs\":[{\"name\":\"tableName\",\"type\":\"string\"},{\"name\":\"key\",\"type\":\"string\"},{\"name\":\"condition\",\"type\":\"string\"},{\"name\":\"optional\",\"type\":\"string\"}],\"name\":\"remove\",\"outputs\":[{\"name\":\"\",\"type\":\"int256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"}', 'function', 1, '2019-09-02 16:32:30', '2019-09-02 16:32:30');
INSERT INTO `tb_method`(`method_id`, `group_id`, `abi_info`, `method_type`, `contract_type`, `create_time`, `modify_time`) VALUES ('0xb8eaa08d', 0, '{\"constant\":false,\"inputs\":[{\"name\":\"name\",\"type\":\"string\"},{\"name\":\"version\",\"type\":\"string\"},{\"name\":\"addr\",\"type\":\"string\"},{\"name\":\"abi\",\"type\":\"string\"}],\"name\":\"insert\",\"outputs\":[{\"name\":\"\",\"type\":\"int256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"}', 'function', 1, '2019-06-17 20:32:30', '2019-06-17 20:32:30');
-- (consensus info node manage 0x1003) addObserver addSealer remove
INSERT INTO `tb_method`(`method_id`, `group_id`, `abi_info`, `method_type`, `contract_type`, `create_time`, `modify_time`) VALUES ('0x25e85d16', 0, '{\"constant\":false,\"inputs\":[{\"name\":\"nodeID\",\"type\":\"string\"}],\"name\":\"addObserver\",\"outputs\":[{\"name\":\"\",\"type\":\"int256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"}', 'function', 1, '2019-09-02 16:32:30', '2019-09-02 16:32:30');
INSERT INTO `tb_method`(`method_id`, `group_id`, `abi_info`, `method_type`, `contract_type`, `create_time`, `modify_time`) VALUES ('0xdf434acc', 0, '{\"constant\":false,\"inputs\":[{\"name\":\"nodeID\",\"type\":\"string\"}],\"name\":\"addSealer\",\"outputs\":[{\"name\":\"\",\"type\":\"int256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"}', 'function', 1, '2019-09-02 16:32:30', '2019-09-02 16:32:30');
INSERT INTO `tb_method`(`method_id`, `group_id`, `abi_info`, `method_type`, `contract_type`, `create_time`, `modify_time`) VALUES ('0x86b733f9', 0, '{\"constant\":false,\"inputs\":[{\"name\":\"nodeID\",\"type\":\"string\"}],\"name\":\"remove\",\"outputs\":[{\"name\":\"\",\"type\":\"int256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"}', 'function', 1, '2019-09-02 16:32:30', '2019-09-02 16:32:30');
-- (cns info 0x1004) selectByName selectByNameAndVersion // insert(ignored, same as crud's insert method: insert(string,string,string,string)
INSERT INTO `tb_method`(`method_id`, `group_id`, `abi_info`, `method_type`, `contract_type`, `create_time`, `modify_time`) VALUES ('0x078af4af', 0, '{\"constant\":true,\"inputs\":[{\"name\":\"name\",\"type\":\"string\"}],\"name\":\"selectByName\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"}', 'function', 1, '2019-06-17 20:32:30', '2019-06-17 20:32:30');
INSERT INTO `tb_method`(`method_id`, `group_id`, `abi_info`, `method_type`, `contract_type`, `create_time`, `modify_time`) VALUES ('0xec72a422', 0, '{\"constant\":true,\"inputs\":[{\"name\":\"name\",\"type\":\"string\"},{\"name\":\"version\",\"type\":\"string\"}],\"name\":\"selectByNameAndVersion\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"}', 'function', 1, '2019-06-17 20:32:30', '2019-06-17 20:32:30');
-- INSERT INTO `tb_method`(`method_id`, `group_id`, `abi_info`, `method_type`, `contract_type`, `create_time`, `modify_time`) VALUES ('0xb8eaa08d', 0, '{\"constant\":false,\"inputs\":[{\"name\":\"name\",\"type\":\"string\"},{\"name\":\"version\",\"type\":\"string\"},{\"name\":\"addr\",\"type\":\"string\"},{\"name\":\"abi\",\"type\":\"string\"}],\"name\":\"insert\",\"outputs\":[{\"name\":\"\",\"type\":\"int256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"}', 'function', 1, '2019-06-17 20:32:30', '2019-06-17 20:32:30');
-- (permission manage 0x1005) insert queryByName remove
INSERT INTO `tb_method`(`method_id`, `group_id`, `abi_info`, `method_type`, `contract_type`, `create_time`, `modify_time`) VALUES ('0xce0a9fb9', 0, '{\"constant\":false,\"inputs\":[{\"name\":\"table_name\",\"type\":\"string\"},{\"name\":\"addr\",\"type\":\"string\"}],\"name\":\"insert\",\"outputs\":[{\"name\":\"\",\"type\":\"int256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"}', 'function', 1, '2019-09-02 16:32:30', '2019-09-02 16:32:30');
INSERT INTO `tb_method`(`method_id`, `group_id`, `abi_info`, `method_type`, `contract_type`, `create_time`, `modify_time`) VALUES ('0xbbec3f91', 0, '{\"constant\":true,\"inputs\":[{\"name\":\"table_name\",\"type\":\"string\"}],\"name\":\"queryByName\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"}', 'function', 1, '2019-09-02 16:32:30', '2019-09-02 16:32:30');
INSERT INTO `tb_method`(`method_id`, `group_id`, `abi_info`, `method_type`, `contract_type`, `create_time`, `modify_time`) VALUES ('0x85d23afc', 0, '{\"constant\":false,\"inputs\":[{\"name\":\"table_name\",\"type\":\"string\"},{\"name\":\"addr\",\"type\":\"string\"}],\"name\":\"remove\",\"outputs\":[{\"name\":\"\",\"type\":\"int256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"}', 'function', 1, '2019-09-02 16:32:30', '2019-09-02 16:32:30');

-- ----------------------------
-- 6、init tb_alert_rule
-- ----------------------------
-- add node status alert rule template
INSERT INTO `tb_alert_rule`(`rule_name`,`enable`,`alert_type`,`alert_level`,`alert_interval_seconds`,`alert_content`,`content_param_list`,`create_time`,`modify_time`) VALUES ('节点异常告警/Node Exception', 0, 1, 1, 3600, '{nodeId}节点异常，请到“节点管理”页面查看具体信息 / Node: {nodeIdEn} node status exception，please check out in \"Node Management\"', '[\"{nodeId}\", \"{nodeIdEn}\"]', '2019-10-29 20:02:30', '2019-10-29 20:02:30');
-- add audit alert rule template
INSERT INTO `tb_alert_rule`(`rule_name`,`enable`,`alert_type`,`alert_level`,`alert_interval_seconds`,`alert_content`,`content_param_list`,`create_time`,`modify_time`) VALUES ('审计异常告警/Audit Exception', 0, 2, 1, 3600, '审计异常：{auditType}，请到“交易审计”页面查看具体信息 / Audit alert: {auditTypeEn}，please check out in \"Transaction Audit\"', '[\"{auditType}\", \"{auditTypeEn}\"]', '2019-10-29 20:02:30', '2019-10-29 20:02:30');
-- add cert alert rule template
INSERT INTO `tb_alert_rule`(`rule_name`,`enable`,`alert_type`,`alert_level`,`alert_interval_seconds`,`alert_content`,`content_param_list`,`create_time`,`modify_time`) VALUES ('证书有效期告警/Cert Validity Exception', 0, 3, 1, 3600, '证书将在{time}过期，请到“证书管理”页面查看具体信息 / Cert validity exception：invalid at {timeEn}，please check out in \"Cert Management\"', '[\"{time}\", \"{timeEn}\"]', '2019-10-29 20:02:30', '2019-10-29 20:02:30');

-- ----------------------------
-- 7、init tb_mail_server_config
-- ----------------------------
-- add mail_server_config template
INSERT INTO `tb_mail_server_config`(`server_name`,`host`,`port`,`username`,`password`,`protocol`,`default_encoding`,`create_time`,`modify_time`,`authentication`,`starttls_enable`,`starttls_required`,`socket_factory_port`,`socket_factory_class`,`socket_factory_fallback`,`enable`) VALUES ('Default config', 'smtp.qq.com', '25', 'yourmail@qq.com', 'yourpassword','smtp', 'UTF-8','2019-10-29 20:02:30', '2019-10-29 20:02:30', 1, 1, 0, 465, 'javax.net.ssl.SSLSocketFactory', 0, 0);

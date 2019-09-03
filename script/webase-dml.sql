
-- ----------------------------
-- 1、init tb_account_info data   admin/Abcd1234
-- ----------------------------
INSERT INTO tb_account_info (account,account_pwd,role_id,create_time,modify_time)values('admin', '$2a$10$F/aEB1iEx/FvVh0fMn6L/uyy.PkpTy8Kd9EdbqLGo7Bw7eCivpq.m',100000,now(),now());



-- ----------------------------
-- 2、init tb_role data
-- ----------------------------
INSERT INTO `tb_role` (role_name,role_name_zh,create_time,modify_time)VALUES ('admin', '管理员', now(), now());
INSERT INTO `tb_role` (role_name,role_name_zh,create_time,modify_time)VALUES ('visitor', '普通用户', now(), now());


-- ----------------------------
-- 3、init tb_user data
-- ----------------------------
INSERT INTO tb_user(user_name, public_key, user_status, user_type, address, has_pk, create_time, modify_time) VALUES ( 'systemUser', '0xc5d877bff9923af55f248fb48b8907dc7d00cac3ba19b4259aebefe325510af7bd0a75e9a8e8234aa7aa58bc70510ee4bef02201a86006196da4e771c47b71b4', 1, 2, '0xf1585b8d0e08a0a00fff662e24d67ba95a438256', 1, Now(), Now());



-- ----------------------------
-- 4、init tb_user_key_mapping data
-- ----------------------------
INSERT INTO tb_user_key_mapping(user_id, private_key, map_status, create_time, modify_time) VALUES (700001, 'SzK9KCjpyVCW0T9K9r/MSlmcpkeckYKVn/D1X7fzzp18MM7yHhUHQugTxKXVJJY5XWOb4zZ79IXMBu77zmXsr0mCRnATZTUqFfWLX6tUBIw=', 1, now(), now());




-- ----------------------------
-- 5、init tb_method
-- ----------------------------
-- (system config info 0x1000) setValueByKey
INSERT INTO `tb_method`(`method_id`, `group_id`, `abi_info`, `method_type`, `contract_type`, `create_time`, `modify_time`) VALUES ('0xbd291aef', 0, '{\"constant\":false,\"inputs\":[{\"name\":\"key\",\"type\":\"string\"},{\"name\":\"value\",\"type\":\"string\"}],\"name\":\"setValueByKey\",\"outputs\":[{\"name\":\"\",\"type\":\"int256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"}', 'function', 1, '2019-09-02 16:32:30', '2019-09-02 16:32:30');
-- (table factory 0x1001) createTable
INSERT INTO `tb_method`(`method_id`, `group_id`, `abi_info`, `method_type`, `contract_type`, `create_time`, `modify_time`) VALUES ('0x56004b6a', 0, '{\"constant\":false,\"inputs\":[{\"name\":\"tableName\",\"type\":\"string\"},{\"name\":\"key\",\"type\":\"string\"},{\"name\":\"valueField\",\"type\":\"string\"}],\"name\":\"createTable\",\"outputs\":[{\"name\":\"\",\"type\":\"int256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"}', 'function', 1, '2019-09-02 16:32:30', '2019-09-02 16:32:30');
-- (crud info 0x1002) update select insert remove
INSERT INTO `tb_method`(`method_id`, `group_id`, `abi_info`, `method_type`, `contract_type`, `create_time`, `modify_time`) VALUES ('0x2dca76c1', 0, '{\"constant\":false,\"inputs\":[{\"name\":\"tableName\",\"type\":\"string\"},{\"name\":\"key\",\"type\":\"string\"},{\"name\":\"entry\",\"type\":\"string\"},{\"name\":\"condition\",\"type\":\"string\"},{\"name\":\"optional\",\"type\":\"string\"}],\"name\":\"update\",\"outputs\":[{\"name\":\"\",\"type\":\"int256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"}', 'function', 1, '2019-09-02 16:32:30', '2019-09-02 16:32:30');
INSERT INTO `tb_method`(`method_id`, `group_id`, `abi_info`, `method_type`, `contract_type`, `create_time`, `modify_time`) VALUES ('0x983c6c4f', 0, '{\"constant\":true,\"inputs\":[{\"name\":\"tableName\",\"type\":\"string\"},{\"name\":\"key\",\"type\":\"string\"},{\"name\":\"condition\",\"type\":\"string\"},{\"name\":\"optional\",\"type\":\"string\"}],\"name\":\"select\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"}', 'function', 1, '2019-09-02 16:32:30', '2019-09-02 16:32:30');
INSERT INTO `tb_method`(`method_id`, `group_id`, `abi_info`, `method_type`, `contract_type`, `create_time`, `modify_time`) VALUES ('0xa72a1e65', 0, '{\"constant\":false,\"inputs\":[{\"name\":\"tableName\",\"type\":\"string\"},{\"name\":\"key\",\"type\":\"string\"},{\"name\":\"condition\",\"type\":\"string\"},{\"name\":\"optional\",\"type\":\"string\"}],\"name\":\"remove\",\"outputs\":[{\"name\":\"\",\"type\":\"int256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"}', 'function', 1, '2019-09-02 16:32:30', '2019-09-02 16:32:30');
-- (consensus info node manage 0x1003) addObserver addSealer remove
INSERT INTO `tb_method`(`method_id`, `group_id`, `abi_info`, `method_type`, `contract_type`, `create_time`, `modify_time`) VALUES ('0x2800efc0', 0, '{\"constant\":false,\"inputs\":[{\"name\":\"nodeID\",\"type\":\"string\"}],\"name\":\"addObserver\",\"outputs\":[{\"name\":\"\",\"type\":\"int256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"}', 'function', 1, '2019-09-02 16:32:30', '2019-09-02 16:32:30');
INSERT INTO `tb_method`(`method_id`, `group_id`, `abi_info`, `method_type`, `contract_type`, `create_time`, `modify_time`) VALUES ('0x89152d1f', 0, '{\"constant\":false,\"inputs\":[{\"name\":\"nodeID\",\"type\":\"string\"}],\"name\":\"addSealer\",\"outputs\":[{\"name\":\"\",\"type\":\"int256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"}', 'function', 1, '2019-09-02 16:32:30', '2019-09-02 16:32:30');
INSERT INTO `tb_method`(`method_id`, `group_id`, `abi_info`, `method_type`, `contract_type`, `create_time`, `modify_time`) VALUES ('0x80599e4b', 0, '{\"constant\":false,\"inputs\":[{\"name\":\"nodeID\",\"type\":\"string\"}],\"name\":\"remove\",\"outputs\":[{\"name\":\"\",\"type\":\"int256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"}', 'function', 1, '2019-09-02 16:32:30', '2019-09-02 16:32:30');
-- (cns info 0x1004) selectByName selectByNameAndVersion insert
INSERT INTO `tb_method`(`method_id`, `group_id`, `abi_info`, `method_type`, `contract_type`, `create_time`, `modify_time`) VALUES ('0x819a3d62', 0, '{\"constant\":true,\"inputs\":[{\"name\":\"name\",\"type\":\"string\"}],\"name\":\"selectByName\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"}', 'function', 1, '2019-06-17 20:32:30', '2019-06-17 20:32:30');
INSERT INTO `tb_method`(`method_id`, `group_id`, `abi_info`, `method_type`, `contract_type`, `create_time`, `modify_time`) VALUES ('0x897f0251', 0, '{\"constant\":true,\"inputs\":[{\"name\":\"name\",\"type\":\"string\"},{\"name\":\"version\",\"type\":\"string\"}],\"name\":\"selectByNameAndVersion\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"}', 'function', 1, '2019-06-17 20:32:30', '2019-06-17 20:32:30');
INSERT INTO `tb_method`(`method_id`, `group_id`, `abi_info`, `method_type`, `contract_type`, `create_time`, `modify_time`) VALUES ('0xa216464b', 0, '{\"constant\":false,\"inputs\":[{\"name\":\"name\",\"type\":\"string\"},{\"name\":\"version\",\"type\":\"string\"},{\"name\":\"addr\",\"type\":\"string\"},{\"name\":\"abi\",\"type\":\"string\"}],\"name\":\"insert\",\"outputs\":[{\"name\":\"\",\"type\":\"int256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"}', 'function', 1, '2019-06-17 20:32:30', '2019-06-17 20:32:30');
-- (permission manage 0x1005) insert query remove
INSERT INTO `tb_method`(`method_id`, `group_id`, `abi_info`, `method_type`, `contract_type`, `create_time`, `modify_time`) VALUES ('0x06e63ff8', 0, '{\"constant\":false,\"inputs\":[{\"name\":\"table_name\",\"type\":\"string\"},{\"name\":\"addr\",\"type\":\"string\"}],\"name\":\"insert\",\"outputs\":[{\"name\":\"\",\"type\":\"int256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"}', 'function', 1, '2019-09-02 16:32:30', '2019-09-02 16:32:30');
INSERT INTO `tb_method`(`method_id`, `group_id`, `abi_info`, `method_type`, `contract_type`, `create_time`, `modify_time`) VALUES ('0x20586031', 0, '{\"constant\":true,\"inputs\":[{\"name\":\"table_name\",\"type\":\"string\"}],\"name\":\"queryByName\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"}', 'function', 1, '2019-09-02 16:32:30', '2019-09-02 16:32:30');
INSERT INTO `tb_method`(`method_id`, `group_id`, `abi_info`, `method_type`, `contract_type`, `create_time`, `modify_time`) VALUES ('0x44590a7e', 0, '{\"constant\":false,\"inputs\":[{\"name\":\"table_name\",\"type\":\"string\"},{\"name\":\"addr\",\"type\":\"string\"}],\"name\":\"remove\",\"outputs\":[{\"name\":\"\",\"type\":\"int256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"}', 'function', 1, '2019-09-02 16:32:30', '2019-09-02 16:32:30');















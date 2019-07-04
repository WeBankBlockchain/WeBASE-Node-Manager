/*
Navicat MySQL Data Transfer

Source Server         : localhost
Source Server Version : 50723
Source Host           : localhost:3306
Source Database       : fisco-bcos

Target Server Type    : MYSQL
Target Server Version : 50723
File Encoding         : 65001

Date: 2018-12-03 17:32:52
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for `tb_block`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `tb_block` (
  `pk_hash` varchar(128) NOT NULL COMMENT '块hash值',
  `network_id` int(11) NOT NULL COMMENT '所属网络',
  `block_number` int(11) NOT NULL COMMENT '高度',
  `miner` varchar(256) NOT NULL COMMENT '矿工',
  `block_timestamp` datetime NOT NULL,
  `trans_count` bigint(20) DEFAULT '0' COMMENT '块包含的交易数',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `modify_time` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`pk_hash`),
  KEY `index_number` (`block_number`),
  KEY `index_networkId` (`network_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='区块信息表';


-- ----------------------------
-- Table structure for `tb_contract`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `tb_contract` (
  `contract_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '合约编号',
  `contract_name` varchar(120) DEFAULT NULL COMMENT '合约名称',
  `network_id` int(11) NOT NULL COMMENT '所属网络',
  `contract_source` text COMMENT '合约源码',
  `contract_status` int(1) DEFAULT '1' COMMENT '部署状态（1：未部署，2：部署成功，3：部署失败）',
  `contract_abi` text COMMENT '编译合约生成的abi文件内容',
  `contract_bin` text COMMENT '合约binary',
  `bytecodeBin` text COMMENT '合约bin',
  `contract_address` varchar(64) DEFAULT NULL COMMENT '合约地址',
  `deploy_time` datetime DEFAULT NULL COMMENT '部署时间',
  `contract_version` varchar(24) DEFAULT NULL COMMENT '合约版本号',
  `contract_type` tinyint(4) DEFAULT '0' COMMENT '合约类型(0-普通合约，1-系统合约)',
  `chain_index` int(8) DEFAULT NULL COMMENT '保存在链上的索引位置',
  `description` text COMMENT '描述',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `modify_time` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`contract_id`),
  UNIQUE KEY `UK_NetworkId_Name_ContractVersion` (`network_id`,`contract_name`,`contract_version`)
) ENGINE=InnoDB AUTO_INCREMENT=200001 DEFAULT CHARSET=utf8 COMMENT='合约表';





-- ----------------------------
-- Table structure for `tb_network`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `tb_network` (
  `network_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '网络编号',
  `network_name` varchar(120) DEFAULT NULL COMMENT '网络名称',
  `network_status` int(1) DEFAULT '1' COMMENT '状态（1-正常 2-停用）',
  `latest_block` int(11) DEFAULT NULL COMMENT '最新块高',
  `trans_count` bigint(20) DEFAULT '0' COMMENT '交易量',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `modify_time` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`network_id`),
  UNIQUE KEY `unique_name` (`network_name`)
) ENGINE=InnoDB AUTO_INCREMENT=300001 DEFAULT CHARSET=utf8 COMMENT='网络表';



-- ----------------------------

-- ----------------------------
-- Table structure for `tb_network_org_map`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `tb_network_org_map` (
  `map_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '编号',
  `network_id` int(11) NOT NULL COMMENT '网络编号',
  `org_id` int(11) NOT NULL COMMENT '组织编号',
  `map_status` int(1) NOT NULL DEFAULT '1' COMMENT '状态（1-正常 2-停用）',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `modify_time` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`map_id`),
  UNIQUE KEY `unique_net_org` (`network_id`,`org_id`)
) ENGINE=InnoDB AUTO_INCREMENT=400001 DEFAULT CHARSET=utf8 COMMENT='网络和组织的映射关系表';




-- ----------------------------
-- Table structure for `tb_node`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `tb_node` (
  `node_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '节点编号',
  `node_name` varchar(120) NOT NULL COMMENT '节点名称',
  `network_id` int(11) NOT NULL COMMENT '所属网络',
  `org_id` int(11) NOT NULL COMMENT '所属组织',
  `node_ip` varchar(16) NOT NULL COMMENT '节点ip',
  `p2p_port` int(11) NOT NULL COMMENT '节点p2p端口',
  `rpc_port` int(11) NOT NULL,
  `channel_port` int(11) NOT NULL COMMENT '链上链下端口',
  `front_port` int(11) DEFAULT NULL COMMENT '节点前置服务端口',
  `block_number` int(11) DEFAULT '0' COMMENT '节点块高',
  `pbft_view` int(11) DEFAULT NULL COMMENT 'pbft_view',
  `node_active` int(1) NOT NULL DEFAULT '2' COMMENT '节点存活标识(1存活，2不存活)',
  `node_type` int(1) DEFAULT '1' COMMENT '节点类型（1-本组织的节点 2-其他节点） 默认本节点（即当前节点管理服务对应的节点）',
  `chain_index` int(8) DEFAULT NULL COMMENT '保存在链上的索引位置',
  `description` text COMMENT '描述',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `modify_time` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`node_id`),
  UNIQUE KEY `unique_name` (`network_id`,`node_name`),
  UNIQUE KEY `unique_node_base` (`node_ip`,`p2p_port`,`rpc_port`)
) ENGINE=InnoDB AUTO_INCREMENT=500001 DEFAULT CHARSET=utf8 COMMENT='节点表';




-- ----------------------------
-- Table structure for `tb_organization`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `tb_organization` (
  `org_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '组织编号',
  `org_name` varchar(120) DEFAULT NULL COMMENT '组织名称',
  `org_type` int(1) DEFAULT '2' COMMENT '组织类型（1-本组织 2-其他组织） 默认其他组织',
  `org_status` int(1) DEFAULT '1' COMMENT '状态（1-正常 2-停用）',
  `description` varchar(250) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `modify_time` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`org_id`),
  UNIQUE KEY `unique_name` (`org_name`)
) ENGINE=InnoDB AUTO_INCREMENT=600001 DEFAULT CHARSET=utf8 COMMENT='组织表';





-- ----------------------------

-- ----------------------------
-- Table structure for `tb_trans_daily`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `tb_trans_daily` (
  `network_id` int(11) NOT NULL COMMENT '所属网络',
  `trans_day` date NOT NULL COMMENT '日期',
  `trans_count` int(11) DEFAULT '0' COMMENT '交易数量',
  `block_number` int(11) DEFAULT '0' COMMENT '当前统计到的块高',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `modify_time` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`network_id`,`trans_day`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='每日交易数据表';




-- ----------------------------
-- Table structure for `tb_user`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `tb_user` (
  `user_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '用户编号',
  `user_name` varchar(64) NOT NULL COMMENT '用户名',
  `network_id` int(11) NOT NULL COMMENT '所属网络',
  `org_id` int(11) NOT NULL COMMENT '所属组织',
  `public_key` varchar(250) NOT NULL COMMENT '公钥',
  `user_status` int(1) NOT NULL DEFAULT '1' COMMENT '状态（1-正常 2-停用）',
  `user_type` int(1) NOT NULL DEFAULT '1' COMMENT '用户类型（1-普通用户 2-系统用户）',
  `address` varchar(64) DEFAULT NULL COMMENT '在链上位置的hash',
  `chain_index` int(8) DEFAULT NULL COMMENT '保存在链上的索引位置',
  `has_pk` int(1) DEFAULT 1 COMMENT '是否拥有私钥信息(1-拥有，2-不拥有)',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `modify_time` datetime DEFAULT NULL COMMENT '修改时间',
  `description` varchar(250) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `unique_name` (`user_name`)
) ENGINE=InnoDB AUTO_INCREMENT=700001 DEFAULT CHARSET=utf8 COMMENT='用户信息表';




-- ----------------------------
-- Table structure for `tb_user_key_mapping`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `tb_user_key_mapping` (
  `map_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '编号',
  `user_id` int(11) NOT NULL COMMENT '用户编号',
  `private_key` text NOT NULL COMMENT '私钥',
  `map_status` int(1) NOT NULL DEFAULT '1' COMMENT '状态（1-正常 2-停用）',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `modify_time` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`map_id`),
  UNIQUE KEY `unique_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=800001 DEFAULT CHARSET=utf8 COMMENT='用户私钥映射表';



-- ----------------------------
-- Table structure for `tb_account_info`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `tb_account_info` (
  `account` varchar(50) NOT NULL COMMENT '系统账号',
  `account_pwd` varchar(250) NOT NULL COMMENT '登录密码',
  `role_id` int(11) NOT NULL COMMENT '所属角色编号',
  `login_fail_time` int(2) NOT NULL DEFAULT '0' COMMENT '登录失败次数,默认0，登录成功归0',
  `account_status` int(1) NOT NULL DEFAULT '1' COMMENT '状态（1-未更新密码 2-正常） 默认1',
  `description` text COMMENT '备注',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `modify_time` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`account`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='系统账号信息表';






-- ----------------------------
-- Table structure for `tb_role`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `tb_role` (
  `role_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '角色编号',
  `role_name` varchar(120) DEFAULT NULL COMMENT '角色英文名称',
  `role_name_zh` varchar(120) DEFAULT NULL COMMENT '角色中文名称',
  `role_status` int(1) DEFAULT '1' COMMENT '状态（1-正常2-无效） 默认1',
  `description` text COMMENT '备注',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `modify_time` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`role_id`),
  UNIQUE KEY `UK_role_Name` (`role_name`)
) ENGINE=InnoDB AUTO_INCREMENT=100000 DEFAULT CHARSET=utf8 COMMENT='角色信息表';



-- ----------------------------
-- Table structure for `tb_trans_hash`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `tb_trans_hash` (
  `trans_hash` varchar(128) NOT NULL COMMENT '交易hash',
  `network_id` int(11) NOT NULL COMMENT '所属网络编号',
  `block_number` bigint(25) NOT NULL COMMENT '所属区块',
  `block_timestamp` datetime NOT NULL COMMENT '出块时间',
  `statistics_flag` int(1) DEFAULT '1' COMMENT '交易发生时间（1-未统计，2-已统计）',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `modify_time` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`trans_hash`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='交易hash信息表';


-- ----------------------------
-- Table structure for tb_user_transaction_monitor
-- ----------------------------
CREATE TABLE IF NOT EXISTS `tb_user_transaction_monitor` (
  `user_name` varchar(128) NOT NULL COMMENT '用户名称',
  `user_type` tinyint(4) DEFAULT '0' COMMENT '用户类型(0-正常，1-异常)',
  `network_id` int(11) NOT NULL COMMENT '所属网络',
  `contract_name` varchar(128) NOT NULL COMMENT '合约名称',
  `contract_address` varchar(64) COMMENT '合约地址',
  `interface_name` varchar(32) COMMENT '合约接口名',
  `trans_type` tinyint(4) DEFAULT '0' COMMENT '交易类型(0-合约部署，1-接口调用)',
  `trans_unusual_type` tinyint(4) DEFAULT '0' COMMENT '交易异常类型 (0-正常，1-异常合约，2-异常接口)',
  `trans_count` int(11) NOT NULL COMMENT '交易量',
  `trans_hashs` varchar(1024) COMMENT '交易hashs(最多5个)',
  `trans_hash_lastest` varchar(128) COMMENT '最新交易hash',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `modify_time` datetime DEFAULT NULL COMMENT '修改时间',
    INDEX idx_un (user_name),
    INDEX idx_ni (network_id),
    INDEX idx_cn (contract_name),
    INDEX idx_ct (create_time),
    INDEX idx_mt (modify_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用户交易监管统计表'
PARTITION BY RANGE (TO_DAYS(create_time)) (
    PARTITION p1 VALUES LESS THAN (TO_DAYS('2019-07-01')),
    PARTITION p2 VALUES LESS THAN (TO_DAYS('2020-01-01')),
    PARTITION p3 VALUES LESS THAN (TO_DAYS('2020-07-01')),
    PARTITION p4 VALUES LESS THAN (TO_DAYS('2021-01-01')),
    PARTITION p5 VALUES LESS THAN (TO_DAYS('2021-07-01')),
    PARTITION p6 VALUES LESS THAN (TO_DAYS('2022-01-01')),
    PARTITION p7 VALUES LESS THAN (TO_DAYS('2022-07-01')),
    PARTITION p8 VALUES LESS THAN (TO_DAYS('2023-01-01')),
    PARTITION p9 VALUES LESS THAN (TO_DAYS('2023-07-01')),
    PARTITION p99 VALUES LESS THAN (MAXVALUE)
);


CREATE TABLE IF NOT EXISTS tb_token (
  token varchar(120) NOT NULL PRIMARY KEY COMMENT 'token',
  value varchar(50) NOT NULL COMMENT '与token相关的值（如：用户编号，图形验证码值）',
  expire_time timestamp NOT NULL COMMENT '失效时间',
  create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='token信息表';
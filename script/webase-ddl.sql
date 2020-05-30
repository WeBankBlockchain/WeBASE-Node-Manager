SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for tb_account_info
-- ----------------------------
CREATE TABLE `tb_account_info` (
  `account` varchar(50) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT '系统账号',
  `account_pwd` varchar(250) NOT NULL COMMENT '登录密码',
  `role_id` int(11) NOT NULL COMMENT '所属角色编号',
  `login_fail_time` int(2) NOT NULL DEFAULT '0' COMMENT '登录失败次数,默认0，登录成功归0',
  `account_status` int(1) NOT NULL DEFAULT '1' COMMENT '状态（1-未更新密码 2-正常） 默认1',
  `description` text COMMENT '备注',
  `email` varchar(40) DEFAULT NULL COMMENT '用户邮箱',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `modify_time` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`account`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='系统账号信息表';

-- ----------------------------
-- Table structure for tb_agency
-- ----------------------------
CREATE TABLE `tb_agency` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增长 ID',
  `agency_name` varchar(64) NOT NULL COMMENT '机构名称',
  `agency_desc` varchar(1024) DEFAULT '' COMMENT '机构描述信息',
  `chain_id` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '所属链 ID',
  `chain_name` varchar(64) DEFAULT '' COMMENT '所属链名称，冗余字段',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `modify_time` datetime NOT NULL COMMENT '最近一次更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_chain_id_agency_name` (`chain_id`,`agency_name`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for tb_alert_log
-- ----------------------------
CREATE TABLE `tb_alert_log` (
  `log_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '告警日志的编号',
  `alert_type` tinyint(4) NOT NULL COMMENT '告警日志的类型, 1-节点, 2-审计, 3-证书',
  `alert_level` tinyint(4) NOT NULL COMMENT '告警日志的告警等级：1-high, 2-middle, 3-low',
  `alert_content` text NOT NULL COMMENT '告警日志的内容',
  `description` text COMMENT '告警日志的描述',
  `status` tinyint(4) NOT NULL DEFAULT '0' COMMENT '告警日志的状态：0-未处理，1-已处理',
  `create_time` datetime DEFAULT NULL COMMENT '告警日志的创建时间',
  `modify_time` datetime DEFAULT NULL COMMENT '告警日志的修改时间',
  PRIMARY KEY (`log_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='告警日志表';

-- ----------------------------
-- Table structure for tb_alert_rule
-- ----------------------------
CREATE TABLE `tb_alert_rule` (
  `rule_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '告警规则的ID',
  `rule_name` varchar(50) NOT NULL COMMENT '告警规则的命名',
  `enable` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否启用规则, 0:false, 1:true',
  `alert_type` tinyint(4) NOT NULL COMMENT '告警规则的类型, 1-节点, 2-审计, 3-证书',
  `alert_level` tinyint(4) NOT NULL COMMENT '告警规则的级别, 1-高, 2-中, 3-低',
  `alert_interval_seconds` bigint(20) NOT NULL COMMENT '告警规则的间隔时间(s)',
  `alert_content` text NOT NULL COMMENT '告警邮件的内容',
  `content_param_list` text NOT NULL COMMENT '告警邮件内容中的可替代参数，如nodeId',
  `description` varchar(50) DEFAULT NULL COMMENT '告警规则的描述',
  `is_all_user` tinyint(4) DEFAULT '0' COMMENT '是否选中所有用户, 0:false, 1:true',
  `user_list` text COMMENT '告警规则作用的用户列表',
  `create_time` datetime DEFAULT NULL COMMENT '告警规则的创建时间',
  `modify_time` datetime DEFAULT NULL COMMENT '告警规则的修改时间',
  `less_than` varchar(40) DEFAULT NULL COMMENT '告警规则：小于某个值',
  `less_and_equal` varchar(40) DEFAULT NULL COMMENT '告警规则：小于等于某个值',
  `larger_than` varchar(40) DEFAULT NULL COMMENT '告警规则：大于某个值',
  `larger_and_equal` varchar(40) DEFAULT NULL COMMENT '告警规则：大于等于某个值',
  `equal` varchar(40) DEFAULT NULL COMMENT '告警规则：等于某个值',
  `last_alert_time` datetime DEFAULT NULL COMMENT '上次告警的时间，与Interval间隔共同作用',
  PRIMARY KEY (`rule_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='告警规则表';

-- ----------------------------
-- Table structure for tb_cert
-- ----------------------------
CREATE TABLE `tb_cert` (
  `finger_print` varchar(120) NOT NULL COMMENT '证书的指纹(唯一标记）',
  `cert_name` varchar(60) NOT NULL COMMENT '证书id',
  `content` text NOT NULL COMMENT 'cert(crt证书）的内容',
  `cert_type` varchar(20) NOT NULL COMMENT '证书类型',
  `public_key` varchar(150) DEFAULT NULL COMMENT '节点证书的公钥/编号(nodeid)',
  `address` varchar(50) DEFAULT NULL COMMENT '节点证书的节点地址',
  `father` varchar(120) NOT NULL COMMENT '父证书对应地址(fingerprint)',
  `validity_from` datetime NOT NULL COMMENT '有效期开始',
  `validity_to` datetime NOT NULL COMMENT '有效期截止',
  `modify_time` datetime DEFAULT NULL COMMENT '修改时间',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`finger_print`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='证书信息表';

-- ----------------------------
-- Table structure for tb_chain
-- ----------------------------
CREATE TABLE `tb_chain` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增长 ID',
  `chain_name` varchar(64) NOT NULL COMMENT '链名称',
  `chain_desc` varchar(1024) DEFAULT NULL COMMENT '链描述信息',
  `version` varchar(64) NOT NULL DEFAULT '' COMMENT '创建链时选择的镜像版本',
  `encrypt_type` tinyint(8) unsigned NOT NULL DEFAULT '1' COMMENT '加密类型：1，标密；2，国密；默认 1 ',
  `chain_status` tinyint(8) unsigned NOT NULL DEFAULT '0' COMMENT '链状态：0，初始化；1，部署中；2，部署失败；3，部署成功等等',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `modify_time` datetime NOT NULL COMMENT '最近一次更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_chain_name` (`chain_name`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for tb_config
-- ----------------------------
CREATE TABLE `tb_config` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增长 ID',
  `config_name` varchar(64) NOT NULL COMMENT '配置名称',
  `config_type` smallint(6) NOT NULL DEFAULT '0' COMMENT '配置类型',
  `config_value` varchar(512) NOT NULL DEFAULT '' COMMENT '配置值',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `modify_time` datetime NOT NULL COMMENT '最近一次更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unq_type_value` (`config_type`,`config_value`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for tb_contract
-- ----------------------------
CREATE TABLE `tb_contract` (
  `contract_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '合约编号',
  `contract_path` varchar(24) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT '合约所在目录',
  `contract_name` varchar(120) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT '合约名称',
  `contract_version` varchar(120) DEFAULT NULL COMMENT '合约版本',
  `group_id` int(11) NOT NULL COMMENT '所属群组编号',
  `contract_source` text COMMENT '合约源码',
  `contract_abi` text COMMENT '编译合约生成的abi文件内容',
  `contract_bin` text COMMENT '合约binary',
  `bytecodeBin` text COMMENT '合约bin',
  `contract_address` varchar(64) DEFAULT NULL COMMENT '合约地址',
  `deploy_time` datetime DEFAULT NULL COMMENT '部署时间',
  `contract_status` int(1) DEFAULT '1' COMMENT '部署状态（1：未部署，2：部署成功，3：部署失败）',
  `contract_type` tinyint(4) DEFAULT '0' COMMENT '合约类型(0-普通合约，1-系统合约)',
  `description` text COMMENT '描述',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `modify_time` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`contract_id`),
  UNIQUE KEY `uk_group_path_name` (`group_id`,`contract_path`,`contract_name`)
) ENGINE=InnoDB AUTO_INCREMENT=200001 DEFAULT CHARSET=utf8 COMMENT='合约表';

-- ----------------------------
-- Table structure for tb_front
-- ----------------------------
CREATE TABLE `tb_front` (
  `front_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '前置服务编号',
  `node_id` varchar(250) NOT NULL COMMENT '节点编号',
  `front_ip` varchar(16) NOT NULL COMMENT '前置服务ip',
  `front_port` int(11) NOT NULL COMMENT '前置服务端口',
  `agency` varchar(32) NOT NULL COMMENT '所属机构名称',
  `client_version` varchar(32) NOT NULL COMMENT '节点版本（国密/非国密）',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `modify_time` datetime NOT NULL COMMENT '修改时间',
  `run_type` tinyint(8) unsigned DEFAULT '0' COMMENT '运行方式：0，命令行；1，Docker',
  `agency_id` int(10) unsigned DEFAULT '0' COMMENT '所属机构 ID',
  `agency_name` varchar(64) DEFAULT '' COMMENT '所属机构名称，冗余字段, 跟 agency 字段相同',
  `host_id` int(10) unsigned DEFAULT '0' COMMENT '所属主机',
  `host_index` smallint(6) DEFAULT '0' COMMENT '一台主机可能有多个节点。表示在主机中的编号，从 0 开始编号',
  `image_tag` varchar(64) DEFAULT '' COMMENT '运行的镜像版本标签',
  `container_name` varchar(255) DEFAULT '' COMMENT 'Docker 启动的容器名称',
  `jsonrpc_port` smallint(6) DEFAULT '8545' COMMENT 'jsonrpc 端口',
  `p2p_port` smallint(6) DEFAULT '30303' COMMENT 'p2p 端口',
  `channel_port` smallint(6) DEFAULT '20200' COMMENT 'channel 端口',
  `status` tinyint(8) DEFAULT '2' COMMENT '容器状态：0，未创建；1，停止；2，启动；',
  PRIMARY KEY (`front_id`),
  UNIQUE KEY `unique_node_id` (`node_id`),
  UNIQUE KEY `unique_agency_id_host_id_front_port` (`agency_id`,`front_ip`,`front_port`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='前置服务信息表'

-- ----------------------------
-- Table structure for tb_front_group_map
-- ----------------------------
CREATE TABLE `tb_front_group_map` (
  `map_id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '编号',
  `front_id` int(11) NOT NULL COMMENT '前置服务编号',
  `group_id` int(11) NOT NULL COMMENT '群组编号',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `modify_time` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`map_id`),
  UNIQUE KEY `unique_front_group` (`front_id`,`group_id`)
) ENGINE=InnoDB AUTO_INCREMENT=600001 DEFAULT CHARSET=utf8 COMMENT='前置群组映射表';

-- ----------------------------
-- Table structure for tb_group
-- ----------------------------
CREATE TABLE `tb_group` (
  `group_id` int(11) UNSIGNED NOT NULL COMMENT '群组ID',
  `group_name` varchar(64) NOT NULL COMMENT '群组名字',

  `chain_id` int(10) unsigned NULL DEFAULT '0' COMMENT '所属链 ID',
  `chain_name` varchar(64) DEFAULT '' COMMENT '所属链名称，冗余字段',

  `group_status` int(1) UNSIGNED DEFAULT '1' COMMENT '状态（1-正常 2-异常）',
  `node_count` int(11) UNSIGNED DEFAULT '0' COMMENT '群组下节点数',
  `group_desc` varchar(1024) DEFAULT NULL COMMENT '群组描述',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `modify_time` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`group_id`),
  UNIQUE KEY `unique_chain_id_group_id` (`chain_id`,`group_id`),
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='群组信息表';

-- ----------------------------
-- Table structure for tb_host
-- ----------------------------
CREATE TABLE `tb_host` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增长 ID',
  `agency_id` int(10) unsigned NOT NULL DEFAULT '1' COMMENT '所属机构 ID',
  `agency_name` varchar(64) DEFAULT NULL COMMENT '所属机构名称，冗余字段',
  `ip` varchar(16) NOT NULL COMMENT '主机IP',
  `ssh_user` varchar(64) NOT NULL DEFAULT 'root' COMMENT 'SSH 登录账号',
  `ssh_port` smallint(5) unsigned NOT NULL DEFAULT '22' COMMENT 'SSH 端口',
  `root_dir` varchar(255) NOT NULL DEFAULT '/opt/fisco-bcos' COMMENT '主机存放节点配置文件的根目录，可能存放多个节点配置',
  `docker_port` smallint(5) unsigned NOT NULL DEFAULT '2375' COMMENT 'Docker demon 的端口',
  `status` tinyint(8) unsigned NOT NULL DEFAULT '0' COMMENT '主机状态：0，新建；1，初始化；2，运行等等',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `modify_time` datetime NOT NULL COMMENT '最近一次更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unq_agency_id,ip` (`agency_id`,`ip`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for tb_mail_server_config
-- ----------------------------
CREATE TABLE `tb_mail_server_config` (
  `server_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '邮件服务器配置的ID',
  `server_name` varchar(40) NOT NULL COMMENT '邮件服务器配置的命名',
  `host` varchar(30) NOT NULL COMMENT '邮件服务器的主机',
  `port` int(10) NOT NULL DEFAULT '25' COMMENT '邮件服务器的端口',
  `username` varchar(40) NOT NULL COMMENT '邮件服务器的邮箱地址',
  `password` varchar(40) NOT NULL COMMENT '邮件服务器的邮箱授权码',
  `protocol` varchar(10) NOT NULL COMMENT '邮件服务器的协议',
  `default_encoding` varchar(10) NOT NULL DEFAULT 'UTF-8' COMMENT '邮件服务器的默认编码(UTF-8)',
  `create_time` datetime DEFAULT NULL COMMENT '邮件服务器配置的创建时间',
  `modify_time` datetime DEFAULT NULL COMMENT '邮件服务器配置的修改时间',
  `authentication` tinyint(4) NOT NULL DEFAULT '1' COMMENT '是否开启验证, 0:false, 1:true',
  `starttls_enable` tinyint(4) NOT NULL DEFAULT '1' COMMENT '如支持，是否优先选用STARTTLS, 0:false, 1:true',
  `starttls_required` tinyint(4) DEFAULT '0' COMMENT '是否必须使用STARTTLS, 0:false, 1:true',
  `socket_factory_port` int(10) DEFAULT '465' COMMENT 'SSL的端口',
  `socket_factory_class` varchar(150) DEFAULT 'javax.net.ssl.SSLSocketFactory' COMMENT 'SSL选用的JAVA类',
  `socket_factory_fallback` tinyint(4) DEFAULT '0' COMMENT '是否启用SSL的fallback, 0:false, 1:true',
  `enable` tinyint(4) NOT NULL DEFAULT '0' COMMENT '邮件服务器是否已配置完成，0初始，1完成',
  `connection_timeout` int(10) NOT NULL DEFAULT '5000' COMMENT '邮件服务器的连接超时值',
  `timeout` int(10) NOT NULL DEFAULT '5000' COMMENT '邮件服务器的通用超时值',
  `write_timeout` int(10) NOT NULL DEFAULT '5000' COMMENT '邮件服务器的写超时值',
  PRIMARY KEY (`server_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='邮件服务器配置表';

-- ----------------------------
-- Table structure for tb_method
-- ----------------------------
CREATE TABLE `tb_method` (
  `method_id` varchar(128) NOT NULL COMMENT '方法id',
  `group_id` int(11) NOT NULL COMMENT '所属群组编号',
  `abi_info` text COMMENT 'abi信息',
  `method_type` varchar(32) DEFAULT NULL COMMENT '方法类型',
  `contract_type` tinyint(4) DEFAULT '0' COMMENT '合约类型(0-普通合约，1-系统合约)',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `modify_time` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`method_id`,`group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='方法解析信息表';

-- ----------------------------
-- Table structure for tb_node
-- ----------------------------
CREATE TABLE `tb_node` (
  `node_id` varchar(250) NOT NULL COMMENT '节点编号',
  `group_id` int(11) NOT NULL COMMENT '所属群组编号',
  `node_name` varchar(120) NOT NULL COMMENT '节点名称',
  `node_ip` varchar(16) DEFAULT NULL COMMENT '节点ip',
  `p2p_port` int(11) DEFAULT NULL COMMENT '节点p2p端口',
  `block_number` bigint(20) DEFAULT '0' COMMENT '节点块高',
  `pbft_view` bigint(20) DEFAULT '0' COMMENT 'pbft_view',
  `node_active` int(1) NOT NULL DEFAULT '2' COMMENT '节点存活标识(1存活，2不存活)',
  `description` text COMMENT '描述',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `modify_time` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`node_id`,`group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='节点表';

-- ----------------------------
-- Table structure for tb_role
-- ----------------------------
CREATE TABLE `tb_role` (
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
-- Table structure for tb_token
-- ----------------------------
CREATE TABLE `tb_token` (
  `token` varchar(120) NOT NULL COMMENT 'token',
  `value` varchar(50) NOT NULL COMMENT '与token相关的值（如：用户编号，图形验证码值）',
  `expire_time` datetime DEFAULT NULL COMMENT '失效时间',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`token`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='token信息表';

-- ----------------------------
-- Table structure for tb_trans_daily
-- ----------------------------
CREATE TABLE `tb_trans_daily` (
  `group_id` int(11) NOT NULL COMMENT '所属群组编号',
  `trans_day` date NOT NULL COMMENT '日期',
  `trans_count` int(11) DEFAULT '0' COMMENT '交易数量',
  `block_number` int(11) DEFAULT '0' COMMENT '当前统计到的块高',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `modify_time` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`group_id`,`trans_day`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='每日交易数据表';

-- ----------------------------
-- Table structure for tb_user
-- ----------------------------
CREATE TABLE `tb_user` (
  `user_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '用户编号',
  `user_name` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT '用户名',
  `group_id` int(11) DEFAULT NULL COMMENT '所属群组编号',
  `public_key` varchar(250) NOT NULL COMMENT '公钥',
  `user_status` int(1) NOT NULL DEFAULT '1' COMMENT '状态（1-正常 2-停用）',
  `user_type` int(1) NOT NULL DEFAULT '1' COMMENT '用户类型（1-普通用户 2-系统用户）',
  `address` varchar(64) DEFAULT NULL COMMENT '在链上位置的hash',
  `sign_user_id` varchar(64) DEFAULT NULL COMMENT '签名服务中的user的业务id',
  `app_id` varchar(64) DEFAULT NULL COMMENT '区块链应用的编号',
  `has_pk` int(1) DEFAULT '1' COMMENT '是否拥有私钥信息(1-拥有，2-不拥有)',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `modify_time` datetime DEFAULT NULL COMMENT '修改时间',
  `description` varchar(250) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `unique_name` (`group_id`,`user_name`),
  UNIQUE KEY `unique_uuid` (`sign_user_id`),
  KEY `index_address` (`address`)
) ENGINE=InnoDB AUTO_INCREMENT=700001 DEFAULT CHARSET=utf8 COMMENT='用户信息表';

SET FOREIGN_KEY_CHECKS = 1;


CREATE TABLE IF NOT EXISTS tb_stat (
  id int(11) NOT NULL AUTO_INCREMENT COMMENT '统计编号',
  group_id int(11) NOT NULL COMMENT '所属群组编号',
  block_cycle double DEFAULT '0' COMMENT '出块周期（秒）',
  tps int(11) DEFAULT '0' COMMENT '每秒交易量',
  block_number int(11) DEFAULT '0' COMMENT '当前统计到的块高度',
  block_size int(11) DEFAULT '0' COMMENT '块大小(交易数量)',
  stat_timestamp varchar(64) COMMENT '记录时间戳',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  modify_time datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_block (group_id,block_number),
  KEY index_group (group_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='区块统计数据表';

-- ----------------------------
-- Table structure for tb_external_account 链上外部账户
-- ----------------------------
CREATE TABLE IF NOT EXISTS tb_external_account (
  id int(11) NOT NULL AUTO_INCREMENT COMMENT '外部账户编号',
  group_id int(11) DEFAULT NULL COMMENT '所属群组编号',
  address varchar(64) DEFAULT NULL COMMENT '在链上位置的hash',
  public_key varchar(250) DEFAULT NULL COMMENT '公钥',
  sign_user_id varchar(64) DEFAULT NULL COMMENT '签名服务中的user的业务id',
  has_pk int(1) DEFAULT 1 COMMENT '是否拥有私钥信息(1-拥有，2-不拥有)',
  user_name varchar(64) binary DEFAULT NULL COMMENT '用户名',
  user_status int(1) DEFAULT NULL DEFAULT '1' COMMENT '状态（1-正常 2-停用）',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  modify_time datetime DEFAULT NULL COMMENT '修改时间',
  description varchar(250) DEFAULT NULL COMMENT '备注',
  app_id varchar(64) DEFAULT NULL COMMENT '区块链应用的编号',
  PRIMARY KEY (id),
  UNIQUE KEY unique_name (group_id,user_name),
  UNIQUE KEY unique_address (group_id,address),
  KEY index_address (address),
  UNIQUE KEY unique_uuid (sign_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='外部账户信息表';


-- ----------------------------
-- Table structure for tb_external_contract 链上外部合约
-- ----------------------------
CREATE TABLE IF NOT EXISTS tb_external_contract (
  id int(11) NOT NULL AUTO_INCREMENT COMMENT '外部合约编号',
  group_id int(11) NOT NULL COMMENT '所属群组编号',
  contract_address varchar(64) NOT NULL COMMENT '合约地址',
  deploy_address varchar(64) NOT NULL COMMENT '合约部署者地址',
  deploy_tx_hash varchar(120) NOT NULL COMMENT '合约部署的交易哈希',
  deploy_time datetime NOT NULL COMMENT '部署时间',
  contract_bin mediumtext DEFAULT NULL COMMENT '合约链上binary',
  contract_status int(1) DEFAULT '1' COMMENT '部署状态（1：未部署，2：部署成功，3：部署失败）',
  contract_type tinyint(4) DEFAULT '0' COMMENT '合约类型(0-普通合约，1-系统合约)',
  contract_name varchar(120) binary DEFAULT NULL COMMENT '合约名称',
  contract_version varchar(120) DEFAULT NULL COMMENT '合约版本',
  contract_abi mediumtext COMMENT '编译合约生成的abi文件内容',
  bytecode_bin mediumtext COMMENT '合约bin',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  modify_time datetime DEFAULT NULL COMMENT '修改时间',
  description text COMMENT '描述',
  PRIMARY KEY (id),
  UNIQUE KEY uk_group_path_name (group_id,contract_address)
) ENGINE=InnoDB AUTO_INCREMENT=800001 DEFAULT CHARSET=utf8 COMMENT='外部合约表';

-- rm unique key of contract_name on table tb_abi
ALTER TABLE tb_abi DROP INDEX unique_name;

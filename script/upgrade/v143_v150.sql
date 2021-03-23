SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE IF NOT EXISTS tb_app_info (
  id int(11) NOT NULL AUTO_INCREMENT COMMENT '自增编号',
  app_name varchar(128) NOT NULL COMMENT '应用名',
  app_key varchar(16) NOT NULL COMMENT '应用Key',
  app_secret varchar(32) DEFAULT NULL COMMENT '应用密码',
  app_type int(1) NOT NULL DEFAULT '2' COMMENT '应用类型(1模板，2新建)',
  app_status int(1) NOT NULL DEFAULT '2' COMMENT '应用状态(1存活，2不存活)',
  app_doc_link varchar(256) DEFAULT NULL COMMENT '应用文档链接',
  app_link varchar(256) DEFAULT NULL COMMENT '应用链接',
  app_ip varchar(16) DEFAULT NULL COMMENT '应用ip',
  app_port int(11) DEFAULT NULL COMMENT '应用端口',
  app_icon mediumtext DEFAULT NULL COMMENT '应用图标',
  app_desc varchar(1024) DEFAULT NULL COMMENT '应用描述',
  app_detail text DEFAULT NULL COMMENT '应用详情',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  modify_time datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_key (app_key),
  KEY uk_name (app_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='应用信息表';

-- ----------------------------
-- Table structure for tb_contract_store
-- ----------------------------
CREATE TABLE IF NOT EXISTS tb_contract_store (
  id int(11) NOT NULL AUTO_INCREMENT COMMENT '自增编号',
  app_key varchar(16) NOT NULL COMMENT '所属应用Key',
  contract_name varchar(120) binary NOT NULL COMMENT '合约名称',
  contract_version varchar(120) NOT NULL COMMENT '合约版本',
  contract_source mediumtext COMMENT '合约源码',
  contract_abi mediumtext COMMENT '编译合约生成的abi文件内容',
  bytecode_bin mediumtext COMMENT '合约bytecodeBin',
  account varchar(50) binary DEFAULT 'admin' COMMENT '关联账号',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  modify_time datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_version (app_key,contract_name,contract_version)
) ENGINE=InnoDB AUTO_INCREMENT=300001 DEFAULT CHARSET=utf8 COMMENT='应用合约仓库';

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

-- 修改表 --
ALTER TABLE tb_abi DROP INDEX unique_name;
ALTER TABLE tb_contract MODIFY COLUMN contract_path varchar(128) binary NOT NULL COMMENT '合约所在目录';
ALTER TABLE tb_cns MODIFY COLUMN contract_path varchar(128) binary NOT NULL COMMENT '合约所在目录';
ALTER TABLE tb_contract_path MODIFY COLUMN contract_path varchar(128) binary NOT NULL COMMENT '合约所在目录';


-- 插入默认数据 --
-- if begin end, else begin end
INSERT INTO `tb_app_info` (`app_name`, `app_key`, `app_type`, `app_doc_link`, `app_desc`, `app_detail`, `create_time`, `modify_time`) VALUES ('WeId-temp', 'app00001', 1, 'WeIdentity是一套分布式多中心的技术解决方案，可承载实体对象（人或者物）的现实身份与链上身份的可信映射、以及实现实体对象之间安全的访问授权与数据交换。WeIdentity由微众银行自主研发并完全开源，秉承公众联盟链整合资源、交换价值、服务公众的理念，致力于成为链接多个垂直行业领域的分布式商业基础设施，促进泛行业、跨机构、跨地域间的身份认证和数据合作。', 'WeIdentity目前主要包含两大模块：WeIdentity DID以及WeIdentity Credential。分布式身份标识 (WeIdentity DID)传统方式中，用户的注册和身份管理完全依赖于单一中心的注册机构；随着分布式账本技术（例如区块链）的出现，分布式多中心的身份注册、标识和管理成为可能。 WeIdentity DID模块在FISCO-BCOS区块链底层平台上实现了一套符合W3C DID规范的分布式多中心的身份标识协议，使实体（人或物）的现实身份实现了链上的身份标识；同时，WeIdentity DID给与Entity（人或者物）直接拥有和控制自己身份ID的能力。可验证数字凭证 (WeIdentity Credential)现实世界中存在着各种各样用于描述实体身份、实体间关系的数据，如身份证、行驶证、存款证明、处方、毕业证、房产证、信用报告等。WeIdentity Credential提供了一整套基于W3C VC规范的解决方案，旨在对这一类数据进行标准化、电子化，生成可验证、可交换的「凭证」（Credential），支持对凭证的属性进行选择性披露，及生成链上存证（Evidence）', now(), now());


SET FOREIGN_KEY_CHECKS = 1;

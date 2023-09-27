-- SET NAMES utf8mb4;
-- SET FOREIGN_KEY_CHECKS = 0;

-- TODO varchar(128) binary 对应 bytea 需要修改默认配置,无法单独配置长度
-- bytea 类型支持两种用于输入和输出的格式：“十六进制”格式和瀚高数据库的历史的“转义”格式。在输入时这两种格式总是会被接受，输出格式则取决于配置参数 bytea_output，
-- 其默认值为十六进制。“十六进制”格式将二进制数据编码为每个字节 2 个十六进制位，最高有效位在前，整个串以序列 \x 开头（用以和转义格式区分）。
-- 作为输入，十六进制位可以是大写也可以是小写，在位对之间可以有空白。


-- ----------------------------
-- Table structure for tb_group
-- ----------------------------
CREATE TABLE IF NOT EXISTS tb_group
(
    group_id        int         NOT NULL,
    group_name      varchar(64) NOT NULL,
    group_status    smallint      DEFAULT '1',
    node_count      int           DEFAULT '0',
    description     varchar(1024) DEFAULT NULL,
    group_type      int,
    group_timestamp varchar(64),
    node_id_list    text,
    create_time     time          DEFAULT NULL,
    modify_time     time          DEFAULT NULL,
    chain_id        int           DEFAULT '0',
    chain_name      varchar(64)   DEFAULT '',
    PRIMARY KEY (group_id),
    UNIQUE (chain_id, group_id)
);
CREATE UNIQUE INDEX unique_chain_id_group_id ON tb_group (chain_id, group_id);

-- ----------------------------
-- Table structure for tb_front
-- ----------------------------
CREATE TABLE IF NOT EXISTS tb_front
(
    front_id        bigserial    NOT NULL,
    node_id         varchar(250) NOT NULL,
    front_ip        varchar(16)  NOT NULL,
    front_port      int          NOT NULL,
    agency          varchar(32)  NOT NULL,
    client_version  varchar(32)  NOT NULL,
    support_version varchar(32)  DEFAULT NULL,
    front_version   varchar(32)  DEFAULT NULL,
    sign_version    varchar(32)  DEFAULT NULL,
    status          int          DEFAULT 1,
    create_time     time         NOT NULL,
    modify_time     time         NOT NULL,
    run_type        smallint     DEFAULT '0',
    agency_id       int          DEFAULT '0',
    agency_name     varchar(64)  DEFAULT '',
    host_id         int          DEFAULT '0',
    host_index      int          DEFAULT '0',
    image_tag       varchar(64)  DEFAULT '',
    container_name  varchar(255) DEFAULT '',
    jsonrpc_port    int          DEFAULT '8545',
    p2p_port        int          DEFAULT '30303',
    channel_port    int          DEFAULT '20200',
    chain_id        int          DEFAULT '0',
    chain_name      varchar(64)  DEFAULT '',
    PRIMARY key (front_id),
    UNIQUE (node_id),
    UNIQUE (front_ip, front_port),
    UNIQUE (agency_id, front_ip, front_port)
);
CREATE UNIQUE INDEX unique_node_id ON tb_front (node_id);
CREATE UNIQUE INDEX unique_ip_port ON tb_front (front_ip, front_port);
CREATE UNIQUE INDEX unique_agency_id_host_id_front_port ON tb_front (agency_id, front_ip, front_port);

-- ----------------------------
-- Table structure for tb_front_group_map
-- ----------------------------
CREATE TABLE IF NOT EXISTS tb_front_group_map
(
    map_id      bigserial          NOT NULL,
    front_id    int                NOT NULL,
    group_id    int                NOT NULL,
    create_time time     DEFAULT NULL,
    modify_time time     DEFAULT NULL,
    status      int      DEFAULT 1 NOT NULL,
    type        smallint DEFAULT 1,
    PRIMARY KEY (map_id),
    UNIQUE (front_id, group_id)
);
CREATE UNIQUE INDEX unique_front_group ON tb_front_group_map (front_id, group_id);


-- ----------------------------
-- Table structure for tb_node
-- ----------------------------
CREATE TABLE IF NOT EXISTS tb_node
(
    node_id      varchar(250) NOT NULL,
    group_id     int          NOT NULL,
    node_name    varchar(255) NOT NULL,
    node_ip      varchar(16)           DEFAULT NULL,
    p2p_port     int                   DEFAULT NULL,
    block_number bigint                DEFAULT '0',
    pbft_view    bigint                DEFAULT '0',
    node_active  smallint     NOT NULL DEFAULT '2',
    description  text                  DEFAULT NULL,
    city         varchar(64)           DEFAULT NULL,
    agency       varchar(250)          DEFAULT NULL,
    create_time  time                  DEFAULT NULL,
    modify_time  time                  DEFAULT NULL,
    PRIMARY KEY (node_id, group_id)
);



-- ----------------------------
-- Table structure for tb_contract
-- ----------------------------
CREATE TABLE IF NOT EXISTS tb_contract
(
    contract_id      bigserial NOT NULL,
    contract_path    bytea     NOT NULL,
    contract_name    bytea     NOT NULL,
    contract_version varchar(120) DEFAULT NULL,
    account          bytea        DEFAULT 'admin',
    group_id         int       NOT NULL,
    contract_source  text,
    contract_abi     text,
    contract_bin     text,
    bytecode_bin     text,
    contract_address varchar(64)  DEFAULT NULL,
    deploy_time      time         DEFAULT NULL,
    contract_status  smallint     DEFAULT '1',
    contract_type    smallint     DEFAULT '0',
    description      text,
    create_time      time         DEFAULT NULL,
    modify_time      time         DEFAULT NULL,
    deploy_address   varchar(64)  DEFAULT NULL,
    deploy_user_name varchar(64)  DEFAULT NULL,
    PRIMARY KEY (contract_id),
    UNIQUE (group_id, contract_path, contract_name, account)
);
CREATE UNIQUE INDEX uk_group_path_name ON tb_contract (group_id, contract_path, contract_name, account);



-- ----------------------------
-- Table structure for tb_method
-- ----------------------------
CREATE TABLE IF NOT EXISTS tb_method
(
    method_id     varchar(128),
    group_id      int NOT NULL,
    abi_info      text,
    method_type   varchar(32),
    contract_type smallint DEFAULT '0',
    create_time   time     DEFAULT NULL,
    modify_time   time     DEFAULT NULL,
    PRIMARY KEY (method_id, group_id)
);



-- ----------------------------
-- Table structure for tb_trans_daily
-- ----------------------------
CREATE TABLE IF NOT EXISTS tb_trans_daily
(
    group_id     int  NOT NULL,
    trans_day    date NOT NULL,
    trans_count  int  DEFAULT '0',
    block_number int  DEFAULT '0',
    create_time  time DEFAULT NULL,
    modify_time  time DEFAULT NULL,
    PRIMARY KEY (group_id, trans_day)
);



-- ----------------------------
-- Table structure for tb_user
-- ----------------------------
CREATE TABLE IF NOT EXISTS tb_user
(
    user_id      bigserial    NOT NULL,
    user_name    bytea        NOT NULL,
    account      bytea                 DEFAULT 'admin',
    group_id     int                   DEFAULT NULL,
    public_key   varchar(250) NOT NULL,
    user_status  smallint     NOT NULL DEFAULT '1',
    user_type    smallint     NOT NULL DEFAULT '1',
    address      varchar(64)           DEFAULT NULL,
    sign_user_id varchar(64)           DEFAULT NULL,
    app_id       varchar(64)           DEFAULT NULL,
    has_pk       smallint              DEFAULT 1,
    create_time  time                  DEFAULT NULL,
    modify_time  time                  DEFAULT NULL,
    description  varchar(250)          DEFAULT NULL,
    PRIMARY KEY (user_id),
    UNIQUE (group_id, user_name, account),
    UNIQUE (group_id, address),
    UNIQUE (sign_user_id)
);
CREATE UNIQUE INDEX unique_name ON tb_user (group_id, user_name, account);
CREATE UNIQUE INDEX unique_address ON tb_user (group_id, address);
CREATE INDEX index_address ON tb_user (address);
CREATE UNIQUE INDEX unique_uuid ON tb_user (sign_user_id);

-- ----------------------------
-- Table structure for tb_account_info
-- ----------------------------
CREATE TABLE IF NOT EXISTS tb_account_info
(
    account         bytea        NOT NULL,
    account_pwd     varchar(250) NOT NULL,
    role_id         int          NOT NULL,
    login_fail_time int          NOT NULL DEFAULT '0',
    account_status  smallint     NOT NULL DEFAULT '1',
    description     text,
    email           varchar(40)           DEFAULT NULL,
    create_time     time                  DEFAULT NULL,
    modify_time     time                  DEFAULT NULL,
    PRIMARY KEY (account)
);



-- ----------------------------
-- Table structure for tb_role
-- ----------------------------
CREATE TABLE IF NOT EXISTS tb_role
(
    role_id      bigserial NOT NULL,
    role_name    varchar(120) DEFAULT NULL,
    role_name_zh varchar(120) DEFAULT NULL,
    role_status  smallint     DEFAULT '1',
    description  text,
    create_time  time         DEFAULT NULL,
    modify_time  time         DEFAULT NULL,
    PRIMARY KEY (role_id),
    UNIQUE (role_name)
);
CREATE UNIQUE INDEX UK_role_Name ON tb_role (role_name);



-- ----------------------------
-- Table structure for tb_token
-- ----------------------------
CREATE TABLE IF NOT EXISTS tb_token
(
    token       varchar(120) NOT NULL PRIMARY KEY,
    value       varchar(50)  NOT NULL,
    expire_time time DEFAULT NULL,
    create_time time DEFAULT NULL
);


-- ----------------------------
-- Table structure for tb_cert
-- ----------------------------
CREATE TABLE IF NOT EXISTS tb_cert
(
    finger_print  varchar(120) NOT NULL,
    cert_name     varchar(60)  NOT NULL,
    content       text         NOT NULL,
    cert_type     varchar(20)  NOT NULL,
    public_key    varchar(150) DEFAULT NULL,
    address       varchar(50)  DEFAULT NULL,
    father        varchar(120) NOT NULL,
    validity_from time         NOT NULL,
    validity_to   time         NOT NULL,
    modify_time   time         DEFAULT NULL,
    create_time   time         DEFAULT NULL,
    PRIMARY KEY (finger_print)
);

-- ----------------------------
-- Table structure for tb_alert_rule
-- ----------------------------
CREATE TABLE IF NOT EXISTS tb_alert_rule
(
    rule_id                bigserial             NOT NULL,
    rule_name              varchar(50)           NOT NULL,
    enable                 smallint    DEFAULT 0 NOT NULL,
    alert_type             smallint              NOT NULL,
    alert_level            smallint              NOT NULL,
    alert_interval_seconds bigint                NOT NULL,
    alert_content          text                  NOT NULL,
    content_param_list     text                  NOT NULL,
    description            varchar(50) DEFAULT NULL,
    is_all_user            smallint    DEFAULT 0,
    user_list              text        DEFAULT NULL,
    create_time            time        DEFAULT NULL,
    modify_time            time        DEFAULT NULL,
    less_than              varchar(40) DEFAULT NULL,
    less_and_equal         varchar(40) DEFAULT NULL,
    larger_than            varchar(40) DEFAULT NULL,
    larger_and_equal       varchar(40) DEFAULT NULL,
    equal                  varchar(40) DEFAULT NULL,
    last_alert_time        time        DEFAULT NULL,
    PRIMARY KEY (rule_id)
);

-- ----------------------------
-- Table structure for tb_mail_server_config
-- ----------------------------
CREATE TABLE IF NOT EXISTS tb_mail_server_config
(
    server_id               bigserial                    NOT NULL,
    server_name             varchar(40)                  NOT NULL,
    host                    varchar(30)                  NOT NULL,
    port                    int          DEFAULT '25'    NOT NULL,
    username                varchar(40)                  NOT NULL,
    password                varchar(40)                  NOT NULL,
    protocol                varchar(10)                  NOT NULL,
    default_encoding        varchar(10)  DEFAULT 'UTF-8' NOT NULL,
    create_time             time         DEFAULT NULL,
    modify_time             time         DEFAULT NULL,
    authentication          smallint     DEFAULT 1       NOT NULL,
    starttls_enable         smallint     DEFAULT 1       NOT NULL,
    starttls_required       smallint     DEFAULT 0,
    socket_factory_port     int          DEFAULT 465,
    socket_factory_class    varchar(150) DEFAULT 'javax.net.ssl.SSLSocketFactory',
    socket_factory_fallback smallint     DEFAULT 0,
    enable                  smallint     DEFAULT 0       NOT NULL,
    connection_timeout      int          DEFAULT 5000    NOT NULL,
    timeout                 int          DEFAULT 5000    NOT NULL,
    write_timeout           int          DEFAULT 5000    NOT NULL,
    PRIMARY KEY (server_id)
);


-- ----------------------------
-- Table structure for tb_alert_log
-- ----------------------------
CREATE TABLE IF NOT EXISTS tb_alert_log
(
    log_id        bigserial NOT NULL,
    alert_type    smallint  NOT NULL,
    alert_level   smallint  NOT NULL,
    alert_content text      NOT NULL,
    description   text               DEFAULT NULL,
    status        smallint  NOT NULL DEFAULT '0',
    create_time   time               DEFAULT NULL,
    modify_time   time               DEFAULT NULL,
    PRIMARY KEY (log_id)
);

-- ----------------------------
-- Table structure for tb_abi, unrelated with tb_contract
-- ----------------------------
CREATE TABLE IF NOT EXISTS tb_abi
(
    abi_id           bigserial    NOT NULL,
    account          bytea DEFAULT 'admin',
    group_id         int          NOT NULL,
    contract_name    varchar(120) NOT NULL,
    contract_address varchar(64)  NOT NULL,
    contract_abi     text         NOT NULL,
    contract_bin     text         NOT NULL,
    create_time      time  DEFAULT NULL,
    modify_time      time  DEFAULT NULL,
    PRIMARY KEY (abi_id),
    UNIQUE (group_id, account, contract_address)
);
-- TODO UNIQUE 会自动生成索引,瀚高的索引是多表唯一的.
-- 所以这里和 tb_external_account 表的索引重复,无法创建
-- UNIQUE自动创建的索引为
-- tb_abi_group_id_account_contract_address_key
-- CREATE UNIQUE INDEX unique_address ON tb_abi (group_id, account, contract_address);

-- ----------------------------
-- Table structure for tb_agency
-- ----------------------------
CREATE TABLE IF NOT EXISTS tb_agency
(
    id          bigserial   NOT NULL,
    agency_name varchar(64) NOT NULL,
    agency_desc varchar(1024)        DEFAULT '',
    chain_id    int         NOT NULL DEFAULT '0',
    chain_name  varchar(64)          DEFAULT '',
    create_time time        NOT NULL,
    modify_time time        NOT NULL,
    PRIMARY KEY (id),
    UNIQUE (chain_id, agency_name)
);
CREATE UNIQUE INDEX uniq_chain_id_agency_name ON tb_agency (chain_id, agency_name);
-- TODO 此处不支持 USING BTREE
-- UNIQUE KEY `uniq_chain_id_agency_name` (`chain_id`,`agency_name`) USING BTREE

-- ----------------------------
-- Table structure for tb_chain
-- ----------------------------
CREATE TABLE IF NOT EXISTS tb_chain
(
    id               bigserial    NOT NULL,
    chain_name       varchar(64)  NOT NULL,
    chain_desc       varchar(1024)         DEFAULT NULL,
    version          varchar(64)  NOT NULL DEFAULT '',
    encrypt_type     smallint     NOT NULL DEFAULT '1',
    chain_status     smallint     NOT NULL DEFAULT '0',
    webase_sign_addr varchar(255) NOT NULL DEFAULT '127.0.0.1:5004',
    create_time      time         NOT NULL,
    modify_time      time         NOT NULL,
    run_type         smallint              DEFAULT '0',
    PRIMARY KEY (id),
    UNIQUE (chain_name)
);
CREATE UNIQUE INDEX uniq_chain_name ON tb_chain (chain_name);
-- TODO 此处不支持 USING BTREE
-- UNIQUE KEY `uniq_chain_name` (`chain_name`) USING BTREE

-- ----------------------------
-- Table structure for tb_config
-- ----------------------------
CREATE TABLE IF NOT EXISTS tb_config
(
    id           bigserial    NOT NULL,
    config_name  varchar(64)  NOT NULL,
    config_type  smallint     NOT NULL DEFAULT '0',
    config_value varchar(512) NOT NULL DEFAULT '',
    create_time  time         NOT NULL,
    modify_time  time         NOT NULL,
    PRIMARY KEY (id)
);



-- ----------------------------
-- Table structure for tb_host
-- ----------------------------
CREATE TABLE IF NOT EXISTS tb_host
(
    id          bigserial    NOT NULL,
    ip          varchar(16)  NOT NULL,
    root_dir    varchar(255) NOT NULL DEFAULT '/opt/fisco-bcos',
    status      smallint     NOT NULL DEFAULT '0',
    remark      text                  DEFAULT NULL,
    create_time time         NOT NULL,
    modify_time time         NOT NULL,
    PRIMARY KEY (id),
    UNIQUE (ip)
);
CREATE UNIQUE INDEX unique_ip ON tb_host (ip);
-- TODO 此处不支持 USING BTREE
-- UNIQUE KEY ` unique_ip ` (` ip `) USING BTREE

-- ----------------------------
-- Table structure for tb_govern_vote
-- ----------------------------
CREATE TABLE IF NOT EXISTS tb_govern_vote
(
    id           bigserial   NOT NULL,
    group_id     int         NOT NULL,
    time_limit   bigint      DEFAULT NULL,
    from_address varchar(64) NOT NULL,
    type         smallint    NOT NULL,
    to_address   varchar(64) DEFAULT NULL,
    detail       varchar(64) DEFAULT NULL,
    create_time  time        NOT NULL,
    modify_time  time        NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS tb_contract_path
(
    id            bigserial NOT NULL,
    contract_path bytea     NOT NULL,
    group_id      int       NOT NULL,
    account       bytea DEFAULT 'admin',
    create_time   time  DEFAULT NULL,
    modify_time   time  DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE (group_id, contract_path)
);
-- TODO 同理忽略此处单独命名索引
-- CREATE UNIQUE INDEX uk_group_path_name ON tb_contract_path (group_id, contract_path);


CREATE TABLE IF NOT EXISTS tb_cns
(
    id               bigserial    NOT NULL,
    group_id         int          NOT NULL,
    contract_path    bytea DEFAULT NULL,
    contract_name    bytea        NOT NULL,
    cns_name         bytea        NOT NULL,
    version          varchar(120) NOT NULL,
    contract_address varchar(64)  NOT NULL,
    contract_abi     text         NOT NULL,
    create_time      time  DEFAULT NULL,
    modify_time      time  DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE (group_id, cns_name, version)
);
CREATE UNIQUE INDEX uk_version ON tb_cns (group_id, cns_name, version);


CREATE TABLE IF NOT EXISTS tb_stat
(
    id             bigserial NOT NULL,
    group_id       int       NOT NULL,
    block_cycle    double precision DEFAULT '0',
    tps            int              DEFAULT '0',
    block_number   int              DEFAULT '0',
    block_size     int              DEFAULT '0',
    stat_timestamp varchar(64),
    create_time    time             DEFAULT NULL,
    modify_time    time             DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE (group_id, block_number)
);
CREATE UNIQUE INDEX uk_block ON tb_stat (group_id, block_number);
CREATE INDEX index_group ON tb_stat (group_id);

-- ----------------------------
-- Table structure for tb_external_account 链上外部账户
-- ----------------------------
CREATE TABLE IF NOT EXISTS tb_external_account
(
    id           bigserial NOT NULL,
    group_id     int          DEFAULT NULL,
    address      varchar(64)  DEFAULT NULL,
    public_key   varchar(250) DEFAULT NULL,
    sign_user_id varchar(64)  DEFAULT NULL,
    has_pk       int          DEFAULT 1,
    user_name    bytea        DEFAULT NULL,
    user_status  smallint     DEFAULT '1',
    create_time  time         DEFAULT NULL,
    modify_time  time         DEFAULT NULL,
    description  varchar(250) DEFAULT NULL,
    app_id       varchar(64)  DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE (group_id, user_name),
    UNIQUE (group_id, address),
    UNIQUE (sign_user_id)
);
-- TODO 同理忽略此处单独命名索引
-- CREATE UNIQUE INDEX unique_name ON tb_external_account (group_id, user_name);
-- CREATE UNIQUE INDEX unique_address ON tb_external_account (group_id, address);
-- CREATE UNIQUE INDEX unique_uuid ON tb_external_account (sign_user_id);
-- CREATE INDEX index_address ON tb_external_account (address);

-- ----------------------------
-- Table structure for tb_external_contract 链上外部合约
-- ----------------------------
CREATE TABLE IF NOT EXISTS tb_external_contract
(
    id               bigserial    NOT NULL,
    group_id         int          NOT NULL,
    contract_address varchar(64)  NOT NULL,
    deploy_address   varchar(64)  NOT NULL,
    deploy_tx_hash   varchar(120) NOT NULL,
    deploy_time      time         NOT NULL,
    contract_bin     text         DEFAULT NULL,
    contract_status  smallint     DEFAULT '1',
    contract_type    smallint     DEFAULT '0',
    contract_name    bytea        DEFAULT NULL,
    contract_version varchar(120) DEFAULT NULL,
    contract_abi     text,
    bytecode_bin     text,
    create_time      time         DEFAULT NULL,
    modify_time      time         DEFAULT NULL,
    description      text,
    PRIMARY KEY (id),
    UNIQUE (group_id, contract_address)
);
-- TODO 同理忽略此处单独命名索引
-- CREATE UNIQUE INDEX uk_group_path_name ON tb_external_contract (group_id, contract_address);

-- ----------------------------
-- Table structure for tb_app_info
-- ----------------------------
CREATE TABLE IF NOT EXISTS tb_app_info
(
    id           bigserial    NOT NULL,
    app_name     varchar(128) NOT NULL,
    app_key      varchar(16)  NOT NULL,
    app_secret   varchar(32)           DEFAULT NULL,
    app_type     int          NOT NULL DEFAULT '2',
    app_status   smallint     NOT NULL DEFAULT '2',
    app_doc_link varchar(256)          DEFAULT NULL,
    app_link     varchar(256)          DEFAULT NULL,
    app_ip       varchar(16)           DEFAULT NULL,
    app_port     int                   DEFAULT NULL,
    app_icon     text                  DEFAULT NULL,
    app_desc     varchar(1024)         DEFAULT NULL,
    app_detail   text                  DEFAULT NULL,
    create_time  time                  DEFAULT NULL,
    modify_time  time                  DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE (app_key)
);
CREATE UNIQUE INDEX uk_key ON tb_app_info (app_key);
CREATE INDEX uk_name ON tb_app_info (app_name);


-- ----------------------------
-- Table structure for tb_contract_store
-- contract from application manage
-- ----------------------------
CREATE TABLE IF NOT EXISTS tb_contract_store
(
    id               bigserial    NOT NULL,
    app_key          varchar(16)  NOT NULL,
    contract_name    bytea        NOT NULL,
    contract_version varchar(120) NOT NULL,
    contract_source  text,
    contract_abi     text,
    bytecode_bin     text,
    account          bytea DEFAULT 'admin',
    create_time      time  DEFAULT NULL,
    modify_time      time  DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE (app_key, contract_name, contract_version)
);
-- TODO 同理忽略此处单独命名索引
-- CREATE UNIQUE INDEX uk_version ON tb_contract_store (app_key, contract_name, contract_version);

-- ----------------------------
-- Table structure for tb_contract_store
-- ----------------------------
CREATE TABLE IF NOT EXISTS tb_warehouse
(
    id                  int   NOT NULL,
    warehouse_name      bytea NOT NULL,
    warehouse_name_en   bytea NOT NULL,
    type                int   NOT NULL,
    warehouse_icon      text,
    description         text,
    description_en      text,
    warehouse_detail    text,
    warehouse_detail_en text,
    create_time         time DEFAULT NULL,
    modify_time         time DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE (warehouse_name)
);
-- TODO 同理忽略此处单独命名索引
-- CREATE UNIQUE INDEX uk_name ON tb_warehouse (warehouse_name);

CREATE TABLE IF NOT EXISTS tb_contract_folder
(
    id               int   NOT NULL,
    folder_name      bytea NOT NULL,
    description      text,
    description_en   text,
    folder_detail    text,
    folder_detail_en text,
    create_time      time DEFAULT NULL,
    modify_time      time DEFAULT NULL,
    warehouse_id     int   NOT NULL,
    PRIMARY KEY (id),
    UNIQUE (warehouse_id, folder_name)
);
-- TODO 同理忽略此处单独命名索引
-- CREATE UNIQUE INDEX uk_name ON tb_contract_folder (warehouse_id, folder_name);

CREATE TABLE IF NOT EXISTS tb_contract_item
(
    id                 int   NOT NULL,
    contract_name      bytea NOT NULL,
    contract_source    text,
    description        text,
    description_en     text,
    create_time        time DEFAULT NULL,
    modify_time        time DEFAULT NULL,
    warehouse_id       int   NOT NULL,
    contract_folder_id int   NOT NULL,
    PRIMARY KEY (id),
    UNIQUE (warehouse_id, contract_folder_id, contract_name)
);
-- TODO 同理忽略此处单独命名索引
-- CREATE UNIQUE INDEX uk_name ON tb_contract_item (warehouse_id, contract_folder_id, contract_name);


-- SET FOREIGN_KEY_CHECKS = 1;

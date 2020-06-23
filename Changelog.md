### v1.3.2(2020-06-17)

**Fix**
- 移除Fastjson，替换为Jackson 2.11.0; web3sdk升级为2.4.1
- 升级依赖包：spring: 5.1.15; log4j: 2.13.3; slf4j: 1.7.30; netty-all: 4.1.44+; guava: 28.2;

**兼容性**
- 支持FISCO-BCOS v2.4.x 版本
- WeBASE-Web v1.3.1+
- WeBASE-Front v1.3.1+
- WeBASE-Sign v1.3.1+
- WeBASE-Transaction v1.3.0+

详细了解,请阅读[**技术文档**](https://webasedoc.readthedocs.io/zh_CN/latest/)。

### v1.3.1(2020-06-01)

**Add**
- 新增动态管理群组接口，支持群组脏数据提醒，手动删除群组数据等
- 新增导入已部署合约的ABI接口，进行合约调用
- 新增导入.p12/.pem/.txt格式私钥接口，支持导入控制台与前置的私钥
- 新增节点前置状态，可查看前置运行状态

**Fix**
- 修复部署合约权限不足导致合约地址为0x0
- 移除自动删除无效群组数据的逻辑
- 修复公钥用户签名报空问题

**兼容性**
- 支持FISCO-BCOS v2.4.x 版本
- WeBASE-Web v1.3.1+
- WeBASE-Front v1.3.1+
- WeBASE-Sign v1.3.1+
- WeBASE-Transaction v1.3.0+

详细了解,请阅读[**技术文档**](https://webasedoc.readthedocs.io/zh_CN/latest/)。


### v1.3.0(2020-04-29)

**Add**
- 新增链上事件通知的GET接口
- 不再保存私钥在数据库中，私钥与交易签名由节点前置通过**WeBASE-Sign**进行私钥创建与交易签名
- 私钥接口中，不再返回私钥`privateKey`，仅返回`signUserId`、`address`等
- 预编译合约接口由WeBASE-Sign签名后调用
- `tb_user`表新增`signUserId`和`appId`字段，移除表`tb_user_key_map`
- 签名服务的`userId`改为`signUserId`, 接口中的`useAes`默认为true，不再需要传入值

**Fix**
- 升级依赖包log4j, fastjson, jackson，移除Jwt
- 统一HTTP请求为UTF-8
- 优化启动脚本

**兼容性**
- 支持FISCO-BCOS v2.0.0-rc1 版本
- 支持FISCO-BCOS v2.0.0-rc2 版本
- 支持FISCO-BCOS v2.0.0-rc3 版本
- 支持FISCO-BCOS v2.0.0 - 2.4.x 版本
- WeBASE-Web v1.2.2+
- WeBASE-Front v1.2.2+
- WeBASE-Sign v1.2.2+
- WeBASE-Transaction v1.2.2+

详细了解,请阅读[**技术文档**](https://webasedoc.readthedocs.io/zh_CN/latest/)。

### v1.2.4 (2020-04-14)

**Fix**
- bugifx: 升级fastjson v1.2.67
- bugifx: 请求体headers中token字段由“Authorization”改成“AuthorizationToken”
- bugifx: Precompiled预编译相关接口中的useAes默认为false，改为默认true
- bugifx: 发交易接口在前后端增加contractAbi字段，修复合约重载函数与CNS获取abi失败的问题

**兼容性**
- 支持FISCO-BCOS v2.0.0-rc1 版本
- 支持FISCO-BCOS v2.0.0-rc2 版本
- 支持FISCO-BCOS v2.0.0-rc3 版本
- 支持FISCO-BCOS v2.0.0 及以上版本
- WeBASE-Web v1.2.2+
- WeBASE-Node-Manager v1.2.2+
- WeBASE-Sign v1.2.2+
- WeBASE-Transaction v1.2.2+

详细了解,请阅读[**技术文档**](https://webasedoc.readthedocs.io/zh_CN/latest/)。

### v1.2.2 (2020-01-02)

**Add**
- 支持国密
  - 支持sm2, sm3
  - sql脚本增加国密版（交易审计支持国密）
  - 新增`/encrypt`接口判断国密
  - 证书管理支持国密证书
- 邮件告警支持中英文
- 动态生成的交易数据表`trans_hash_xx`增加自增的`trans_number`字段

**Fix**
- bugfix：precompiled api加入`useAes`字段，默认为false
- 优化：web3sdk升级至v2.2.0
- 优化：删除sql脚本默认数据中的systemUser
- bugfix: 修复触发告警间隔时间不生效问题
- bugifx: 修复start.sh启动时间过长的问题
- bugfix：通过`block_number`, `trans_number`获取数据表中区块数与交易数，修复数据量过大时，getCount耗时过长问题

**兼容性**
- 支持FISCO-BCOS v2.0.0-rc1 版本
- 支持FISCO-BCOS v2.0.0-rc2 版本
- 支持FISCO-BCOS v2.0.0-rc3 版本
- 支持FISCO-BCOS v2.0.0 及以上版本
- WeBASE-Web v1.2.2
- WeBASE-Front v1.2.2
- WeBASE-Sign v1.2.2
- WeBASE-Transaction v1.2.2

详细了解,请阅读[**技术文档**](https://webasedoc.readthedocs.io/zh_CN/latest/)。


### v1.2.1 (2019-11-22)

**Add**
- 邮件服务接口
- 邮件告警类型接口
- 告警日志接口
- 证书管理增加SDK证书

**Fix**
- 优化：sh脚本支持secp256k1
- 优化：升级fastjson为1.2.60, gradle-wrapper为v6.0.1
- 优化：precompiled节点列表接口增加离线的共识/观察节点

**兼容性**
- 支持FISCO-BCOS v2.0.0-rc1 版本
- 支持FISCO-BCOS v2.0.0-rc2 版本
- 支持FISCO-BCOS v2.0.0-rc3 版本
- 支持FISCO-BCOS v2.0.0 版本
- WeBASE-Front v1.2.1
- WeBASE-Node-Manager v1.2.1

详细了解,请阅读[**技术文档**](https://webasedoc.readthedocs.io/zh_CN/latest/)。


### v1.2.0 (2019-10-29)

**Add**
- 证书管理接口
- 权限管理功能接口

**兼容性**
- 支持FISCO-BCOS v2.0.0-rc1 版本
- 支持FISCO-BCOS v2.0.0-rc2 版本
- 支持FISCO-BCOS v2.0.0-rc3 版本
- 支持FISCO-BCOS v2.0.0 版本
- WeBASE-Front v1.2.0
- WeBASE-Node-Manager v1.2.0

详细了解,请阅读[**技术文档**](https://webasedoc.readthedocs.io/zh_CN/latest/)。


### v1.1.0 (2019-09-09)

**Add**
- 查询cns
- 节点共识状态管理
- 配置修改
- CRUD

**Fix**
- 优化：引导用户导入已部署的合约
- bugfix：概览页面节点正常，节点管理界面节点异常
- 优化：默认>=20个就不审计了。此时需要提示用户
- 优化：引导用户导入外部用户
- 批量upload合约

**兼容性**
- 支持FISCO-BCOS v2.0.0-rc1 版本
- 支持FISCO-BCOS v2.0.0-rc2 版本
- 支持FISCO-BCOS v2.0.0-rc3 版本
- 支持FISCO-BCOS v2.0.0 版本
- WeBASE-Web v1.1.0
- WeBASE-Front v1.1.0

详细了解,请阅读[**技术文档**](https://webasedoc.readthedocs.io/zh_CN/latest/)。



### v1.0.0

(2019-06-27)

WeBASE-Node-Manager（微众区块链中间件平台-节点管理子系统），与WeBASE-Web配套使用，属于WeBASE-Web的后台服务。

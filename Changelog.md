### v3.0.2(2023-03-31)

**Fix**
- 优化合约IDE交易的参数编码失败问题：发起交易与部署合约的地方参数`funcParam`从`List<Object>`改为`List<String>`

**兼容性**
- 支持FISCO-BCOS v3.0.0 及以上版本
- WeBASE-Web v3.0.2
- WeBASE-Front v3.0.2

详细了解,请阅读[**技术文档**](https://webasedoc.readthedocs.io/zh_CN/lab/)。

### v3.0.1(2022-12-14)

**Add**
- 新增登录账户注册、冻结、有效期与删除等功能
- 应用管理适配FISCO BCOS 3.0, 可使用webase-app-sdk进行应用管理
- 支持SSL，与WeBASE-Web管理台使用HTTPS

**兼容性**
- 支持FISCO-BCOS v3.0.0 及以上版本
- WeBASE-Web v3.0.1
- WeBASE-Front v3.0.0

详细了解,请阅读[**技术文档**](https://webasedoc.readthedocs.io/zh_CN/lab/)。

### v3.0.0(2022-08-24)

**Add**
- 支持FISCO BCOS v3.0.0

**Fix**
- 支持基于BFS的CNS

**兼容性**
- 支持FISCO-BCOS v3.0.0 及以上版本
- WeBASE-Front v3.0.0
- WeBASE-Web v3.0.0

详细了解,请阅读[**技术文档**](https://webasedoc.readthedocs.io/zh_CN/lab/)。


### lab-rc2(2022-05-07)

**Add**
- 支持Liquid合约功能
- 支持BFS、CNS、权限管理、Event查看与事件订阅等功能

**兼容性**
- 支持FISCO-BCOS v3.0.0-rc2 及以上版本
- WeBASE-Web lab-rc2
- WeBASE-Front lab-rc2
- WeBASE-Sign lab-rc1

详细了解,请阅读[**技术文档**](https://webasedoc.readthedocs.io/zh_CN/lab/)。

### lab-rc1(2021-12-09)

**Add**
- 适配兼容FISCO BCOS v3.0.0


**兼容性**
- 支持FISCO-BCOS v3.0.0 及以上版本
- WeBASE-Web lab-rc1
- WeBASE-Front lab-rc1
- WeBASE-Sign lab-rc1

详细了解,请阅读[**技术文档**](https://webasedoc.readthedocs.io/zh_CN/lab/)。



### v1.5.3(2021-09-27)

**Add**
- 新增节点管理服务Docker镜像，`webasepro/webase-node-mgr:v1.5.3`
- 合约仓库新增代理合约模板、溯源合约模板
- 新增公钥用户绑定私钥功能
- 新增导出私钥功能，导出格式为带密码的P12文件
- 新增节点备注城市、IP与机构功能，新增群组备注群组信息功能，用于监控大屏展示

**Fix**
- 优化冻结/解冻合约获取合约管理员获取
- 优化观察节点同步区块状态显示
- 修复游离节点设为共识节点失败问题
- 修复重复编译合约提示“合约已存在”问题
- 修复对v2.6.0节点的兼容问题


**兼容性**
- 支持FISCO-BCOS v2.4.x 及以上版本
- WeBASE-Web v1.5.3+
- WeBASE-Front v1.5.1+
- WeBASE-Sign v1.5.0+
详细了解,请阅读[**技术文档**](https://webasedoc.readthedocs.io/zh_CN/latest/)。

### v1.5.2(2021-07-16)

**Add**
- 优化合约Java项目导出并支持多用户、节点检测
- 合约仓库新增资产合约模板
- 支持开发者导出私钥、支持合约IDE绑定合约地址

**Fix**
- 优化禁用后台鉴权，增加Swagger，方便服务端联调
- 修复异常合约列表排序
- 修复获取区块头接口

**兼容性**
- 支持FISCO-BCOS v2.4.x 及以上版本
- WeBASE-Web v1.5.2+
- WeBASE-Front v1.5.1+
- WeBASE-Sign v1.5.0+
详细了解,请阅读[**技术文档**](https://webasedoc.readthedocs.io/zh_CN/latest/)。

### v1.5.1(2021-05-28)

**Add**
- 支持导出合约的Java工程脚手架
- 新增合约仓库接口
- 新增全量用户/合约通过地址搜索功能

**Fix**
- 优化开发者模式下鉴权，修复开发者模式下删除合约目录问题
- 优化应用管理中合约同步速度过慢问题
- 修复交易审计中异常数据总数错误问题
- 修复导出SDK证书目录问题

**兼容性**
- 支持FISCO-BCOS v2.4.x 及以上版本
- WeBASE-Web v1.5.1+
- WeBASE-Front v1.5.1+
- WeBASE-Sign v1.5.0+
详细了解,请阅读[**技术文档**](https://webasedoc.readthedocs.io/zh_CN/latest/)。

### v1.5.0(2021-04-06)

**Add**
- 新增应用管理，支持WeIdentity模板和自定义应用接入
- 节点监控新增链上TPS、出块周期、块大小的统计
- 新增链上全量合约统计、新增链上全量私钥统计
- 支持导出Pem/P12/WeID或明文私钥、支持导出前置的SDK证书
- Web3SDK切换到JavaSDK

**Fix**
- 支持合约的重复编译、部署等操作
- 修复合约列表搜索合约问题
- 优化设置共识节点的配置文件检测

**兼容性**
- 支持FISCO-BCOS v2.4.x 及以上版本
- WeBASE-Web v1.5.0+
- WeBASE-Front v1.5.0+
- WeBASE-Sign v1.5.0+
详细了解,请阅读[**技术文档**](https://webasedoc.readthedocs.io/zh_CN/latest/)。

### v1.4.3(2021-01-27)

**Add**
- 拆分可视化部署步骤为：添加主机、初始化主机、部署节点
- 可视化部署支持同机部署节点
- 可视化部署支持自动拉取镜像
- 可视化部署使用ansible，并完善各个步骤的检测脚本
- 支持注册CNS合约

**Fix**
- 修复私钥管理address唯一限制
- 修复一些已知的bug

**兼容性**
- 支持FISCO-BCOS v2.4.x 版本及以上版本
- WeBASE-Web v1.4.3+
- WeBASE-Front v1.4.3+
- WeBASE-Sign v1.4.0+

详细了解,请阅读[**技术文档**](https://webasedoc.readthedocs.io/zh_CN/latest/)。


### v1.4.2(2020-11-19)

**Add**
- 新增EventLog查询功能

**Fix**
- 优化合约IDE合约加载
- 修复创建群组/加入群组异常
- 修复节点共识类型问题

**兼容性**
- 支持FISCO-BCOS v2.4.x 版本及以上版本
- WeBASE-Web v1.4.0+
- WeBASE-Front v1.4.2+
- WeBASE-Sign v1.4.0+

详细了解,请阅读[**技术文档**](https://webasedoc.readthedocs.io/zh_CN/latest/)。

### v1.4.1(2020-09-18)

**Add**
- 新增ChainGovernance接口，包含链委员与运维管理、合约冻结功能
- 新增getBlockHeader接口
- 新增开发者模式，区别与普通用户与管理员用户

**Fix**
- 修复bouncy-castle版本兼容问题，统一为1.60版本
- 优化调用节点前置策略，不再随机选择前置访问，优先访问最新的可用前置

**兼容性**
- 支持FISCO-BCOS v2.4.x 版本及以上版本
- WeBASE-Web v1.4.0+
- WeBASE-Front v1.4.1+
- WeBASE-Sign v1.4.0+

详细了解,请阅读[**技术文档**](https://webasedoc.readthedocs.io/zh_CN/latest/)。

### v1.4.0(2020-08-06)

**Add**
- 在原有先部署链，后添加前置方式的基础上，新增可视化部署 FISCO-BCOS 底层节点功能；
- 可视化部署链后，节点的管理操作，包括：新增，启动，停止；
- 可视化部署链后，重置链后重新部署的功能；
- `/front/find` 接口增加字段：节点版本号（`clientVersion`）和节点最高支持版本号（`supportVersion`）；

**Fix**
- 增加返回 FISCO-BCOS 和 WeBASE-Front Version 版本接口
- 修改`tb_contract`表中合约ABI, BIN字段为`mediumtext`
- 支持ChainGovernance/ContractLifeCycle预编译合约的交易解析

**兼容性**
- 支持FISCO-BCOS v2.4.x 版本（推荐）
- 支持FISCO-BCOS v2.5.x 版本
- WeBASE-Web v1.4.0+
- WeBASE-Front v1.4.0+
- WeBASE-Sign v1.4.0+

详细了解,请阅读[**技术文档**](https://webasedoc.readthedocs.io/zh_CN/latest/)。



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

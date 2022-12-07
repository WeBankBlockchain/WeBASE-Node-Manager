# WeBASE管理台账号文档

## 账户注册说明

账户注册包含两个方式：
- 通过管理员直接在“账号管理”页面中，点击“新增账号”按钮进行添加账户；
- 通过登录页面下方的“注册”按钮，填写表单后提交注册开发者账户或普通用户账户的申请；
    - 注册后的账号需要管理员在“账号管理”中对新注册的用户进行“解冻”后，用户才能登录该账号；

注册时需要填入包含账号名、密码、邮箱、邮箱验证码、手机号等必填信息信息，还包含身份证号、真名、联系地址、备注等信息。
- 其中填写邮箱后，会发送随机的4位数字验证码到接收邮箱。只有填入了正确的验证码才能完成注册。验证码将在5min后失效
- 账号默认的有效期是一年，管理员可以通过账号列表中的“修改信息”按钮，延长账号的有效期；

### 账号管理

普通用户、开发者用户、管理员用户：
- 通过点击页面右上角的账户名，在下拉菜单中点击“修改信息”，即可修改个人相关信息；

管理员：
- 在“账号管理”页面中，点击“新增账号”按钮进行添加账户；
- 在“账号管理”页面，查看各个账号的信息，身份证、手机号、姓名的信息会进行脱敏展示；
- 在“账号管理”页面，点击列表中的“修改信息”按钮，可以获取用户的信息详情并进行修改操作。如账户续期、账户手机号修改等；
- 在“账号管理”页面，点击列表中的“冻结/解冻”按钮，可以将账户进行冻结或解冻的操作。新注册的普通账户、开发者默认处于冻结状态；需要管理员解冻后才能正常使用；

## 账户信息加密说明

### 信息加密

用户注册后，密码会多层不可逆加密保存到数据库。而个人信息中身份证、手机号、真名等信息会使用AES+自定义秘钥进行加密。

密码
- 前端通过密码进行哈希操作传到后台
- 后台接收哈希后，通过SpringSecurityCrypto中的BCryptPasswordEncoder，对该密码哈希进行不可逆加密

个人信息：
- 身份证、真名会通过对称加密保存到db；
- 手机号、身份证、真名在前端展示时会做脱敏展示。

### 登录及访问口令鉴权

登录时，用户需要输入随机的验证码进行人机确认后才能输入账号密码进行登录。
- 登录的验证码通过一个随机因子生成随机4位数字，并生成对应的数字图片，将图片返回到前端登录页面。

密码加密传输到后台，后台通过SpringSecurity判断密码是否与用户的密码匹配。
- 确认匹配且验证码正确则登录成功，返回用户token，该token为用户访问后台的口令

用户访问页面需要用到口令token，每次访问会刷新token并刷新有效期。token在不使用的情况下，会在30分钟后失效，届时则需要重新登录。

### 个人隐私协议

在resource中提供privacy.txt，其中为个人隐私协议，需要用户在注册账户时确认并勾选同意隐私协议

## 接口及其分类

接口文档位于：https://webasedoc.readthedocs.io/zh_CN/latest/docs/WeBASE-Node-Manager/interface.html

权限分为普通账户权限、开发者权限、管理员权限三种，管理员可以访问所有接口，开发者与普通账户仅能访问部分接口

**普通用户权限**
- get /account/pictureCheckCode
- put /passwordUpdate
- get /log/list/{pageNumber}/{pageSize}
- get /mailServer/config/{serverId}
- get /mailServer/config/list
- get /alert/{ruleId}
- get /alert/list
- get /block/blockList/{groupId}/{pageNumber}/{pageSize}
- get /block/blockByNumber/{groupId}/{blockNumber}
- get /block/search/{groupId}/{input}
- get /cert/list
- get /cert/sdk/{frontId}
- get /cert/sdk/zip/{frontId}
- get /cert/
- get /config/version
- get /config/ipPort
- get /config/isDeployedModifyEnable
- get /config/list
- get /config/auth
- get /abi/list/{groupId}/{pageNumber}/{pageSize}
- get /abi/list/all/{groupId}/{pageNumber}/{pageSize}
- get /abi/{abiId}
- get /warehouse/list
- get /warehouse/
- get /warehouse/folder
- get /warehouse/item
- get /warehouse/folder/list
- get /warehouse/item/list
- get /contract/contractList
- get /contract/contractId
- post /contract/findByPartOfBytecodeBin
- get /contract/contractList/all/light
- post /contract/contractPath/list/{groupId}
- post /contract/contractList/multiPath
- post /contract/findCns
- post /contract/findCnsList
- get /contract/liquid/check/{frontId}
- get /chain/monitorInfo/{frontId}
- get /event/newBlockEvent/list/{groupId}/{pageNumber}/{pageSize}
- get /event/contractEvent/list/{groupId}/{pageNumber}/{pageSize}
- get /event/contractInfo/{groupId}/{type}/{contractAddress}
- get /event/listAddress/{groupId}
- get /external/account/list/{groupId}/{pageNumber}/{pageSize}
- get /external/contract/list/{groupId}/{pageNumber}/{pageSize}
- get /external/account/list/all/{groupId}/{pageNumber}/{pageSize}
- get /external/contract/list/all/{groupId}/{pageNumber}/{pageSize}
- get /external/deployAddress/{groupId}/{contractAddress}
- get /front/refresh
- get /front/find
- delete /front/{frontId}
- get /front/isWasm/{frontId}/{groupId}
- get /group/encrypt/{groupId}
- get /group/general/{groupId}
- get /group/all
- get /group/{"/all/invalidIncluded/{pageNumber}/{pageSize}","/all/invalidIncluded"}
- get /group/all/{groupStatus}
- get /group/transDaily/{groupId}
- get /group/update
- delete /group/{groupId}
- get /group/detail/{groupId}
- get /method/findById/{groupId}/{methodId}
- get /monitor/userList/{groupId}
- get /monitor/interfaceList/{groupId}
- get /monitor/transList/{groupId}
- get /monitor/unusualUserList/{groupId}/{pageNumber}/{pageSize}
- get /monitor/unusualContractList/{groupId}/{pageNumber}/{pageSize}
- get /node/nodeList/{groupId}/{pageNumber}/{pageSize}
- get /node/nodeInfo/{groupId}
- get /node/nodeInfo/{groupId}/{nodeId}
- put /node/description
- get /node/city/list
- get /stat/
- get /transaction/transList/{groupId}/{pageNumber}/{pageSize}
- get /transaction/transactionReceipt/{groupId}/{transHash}
- get /transaction/transInfo/{groupId}/{transHash}
- post /transaction/signMessageHash
- get /user/userList/{groupId}/{pageNumber}/{pageSize}


**开发者权限**
- post /abi/
- put /abi/
- delete /abi/{abiId}
- post /contract/save
- delete /contract/{groupId}/{contractId}
- post /contract/deploy
- post /contract/transaction
- post /contract/contractPath
- delete /contract/batch/path
- post /contract/registerCns
- post /contract/copy
- get /contract/listManager/{groupId}/{contractAddress}
- post /contract/liquid/compile
- post /contract/liquid/compile/check
- post /event/eventLogs/list
- get /front/refresh/status
- get /front/groupInfo
- post /method/add
- post /user/userInfo
- post /user/bind
- put /user/userInfo
- post /user/import
- post /user/importPem
- post /user/importP12
- post /user/exportPem
- post /user/exportP12
- post /user/export/{userId}
- post /user/bind/privateKey
- post /user/bind/privateKey/pem
- post /user/bind/privateKey/p12

**管理员权限**
- get /role/roleList?{pageNumber}&{pageSize}&{roleId}&{roleName}
- post /account/accountInfo
- put /account/accountInfo
- get /account/accountList/{pageNumber}/{pageSize}
- delete /account/{account}
- put /log/
- put /mailServer/config
- post /alert/mail/test/{toMailAddress}
- put /alert/
- put /alert/toggle
- post /cert/
- delete /cert/
- post /front/new
- put /group/description


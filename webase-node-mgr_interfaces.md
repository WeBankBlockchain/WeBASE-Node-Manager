区块链平台节点管理接口设计

**修改记录**

| **更新时间** | **变更内容**                                                  | **变更理由** | **变更后版本** |
|--------------|---------------------------------------------------------------|--------------|----------------|
| 20181120     | V1.0.0 创建文档                                               |              |                |
| 20181123     | 合约信息增删改查                                              |              | v1.0.1         |
| 20181126     | 添加“修改组织信息功能                                         |              | V1.0.3         |
| 20181202     | 1、所有接口出入参为小写首字母                                 |              | V1.0.5         |
| 20181202     | 接口出入参变更                                                |              | v1.0.6         |
| 20181212     | 私钥接口中返回单个字段的，不封装，直接放data返回              |              | V1.0.7         |
| 20181217     | 1、新增report接口：获取最新已上报的错误日志、节点错误日志上报 | 0.3版本设计  | v0.3.0         |
| 20190304     | report接口整合为两个接口                                      |              | v0.4.0         |

1.  用户信息增改查

2.  私钥查询接口变更

3.  添加“删除组织信息功能

4.  列表查询返回体变更

5.  新增接口：新增帐号、修改帐号、删除帐号、查询帐号列表、更新当前密码、查询区块列表、新增合约、修改合约、删除合约、
    部署合约、发交易

**节点数据上报接口**

### 2.1 获取浏览器当前块高

#### 2.1.1 传输协议规范

a)、网络传输协议：使用HTTP协议

b)、请求地址：/report /blockChainInfo/{attr}/{nodeIp}/{nodeP2PPort}

c)、请求方式：GET

d)、返回格式：json

#### 2.1.2 参数信息详情

| 序号 | 输入参数    | 类型   | 可为空 | 备注                       |
|------|-------------|--------|--------|----------------------------|
| 1    | hostIp      | String | 否     | 节点ip                     |
| 2    | nodeP2PPort | int    | 否     | 节点p2p端口                |
| 3    | attr        | String | 否     | 接口标志：latest_block     |
| 序号 | 输出参数    | 类型   |        | 备注                       |
| 1    | code        | Int    | 否     | 返回码，0：成功 其它：失败 |
| 2    | message     | String | 否     | 描述                       |
| 3    | data        | object | 是     | 返回信息实体               |
| 3.1  | latestBlock | Int    | 是     | 当前块高                   |

#### 2.1.3 入参事例

http://localhost:8080/fisco-bcos-node-mgr/report/blockChainInfo/latest_block/127.0.0.1/30303

#### 2.1.4 出参示例

**a)、成功：**

｛

“code“:0,

”message”: ”success” ,

“data”:{“latestBlock”:23}

｝

**b)、失败：**

｛

“code“: 202000,

”message”: ” invalid node info,

“data”:{}

｝

### 2.2 区块信息上报

#### 2.2.1 传输协议规范

a)、网络传输协议：使用HTTP协议

b)、请求地址：/report/blockChainInfo

c)、请求方式：POST

d)、请求头：Content-type: application/json

e)、返回格式：JSON

#### 2.2.2 参数信息详情

| 序号        | 输入参数       | 类型           | 可为空 | 备注                       |
|-------------|----------------|----------------|--------|----------------------------|
| 1           | metricDataList | List           | 否     |                            |
| 1.1         |                | object         | 否     |                            |
| 1.1.1       | hostIp         | String         | 否     | 节点Ip                     |
| 1.1.2       | nodeP2PPort    | Int            | 否     | 节点端口                   |
| 1.1.3       | attr           | String         | 否     | 接口标志：block_info       |
| 1.1.4       | metricValue    | list           | 否     |                            |
| 1.1.4.1     |                | object         | 否     |                            |
| 1.1.4.1.1   | id             | Integer        | 是     |                            |
| 1.1.4.1.2   | jsonrpc        | String         | 是     |                            |
| 1.1.4.1.3   | result         | object         | 否     |                            |
| 1.1.4.1.3.1 | hash           | String         | 否     | 区块hash                   |
| 1.1.4.1.3.2 | minerNodeId    | String         | 否     | 矿工                       |
| 1.1.4.1.3.3 | number         | String         | 否     | 块高                       |
| 1.1.4.1.3.4 | timestamp      | String         | 否     | 区块时间戳                 |
| 1.1.4.1.3.5 | transactions   | List\<String\> | 否     | 块内交易hash列表           |
| 序号        | 输出参数       | 类型           |        | 备注                       |
| 1           | code           | Int            | 否     | 返回码，0：成功 其它：失败 |
| 2           | message        | String         | 否     | 描述                       |

#### 2.2.3 入参事例

<http://localhost:8080/fisco-bcos-node-mgr/report/blockInfo>

{

"metricDataList": [

{

"nodeP2PPort": "30306",

"metricValue": [

{

"jsonrpc": "2.0",

"id": 3424,

"result": {

"hash": "0xc1662d849a140dbdeae9a30d76214c9728c55f03c27a8ad7434d0947db392699",

"timestamp": "0x167345b4585",

"transactions": {"fasdfasf"，"hrtherther"},

"number": "0x402",

"minerNodeId":
"578176ff550fceae06b947996d25bb4bac13200437d5d5e5589154ada18106d0d22b1ecc99f14aaa657dbfaf2cdf67da23cdd59483283bd946b89a8a339753cf",

}

}

],

"hostIp": "127.0.0.1",

"attr": "block_info"

}

]

#### 2.2.4 出参示例

**a)、成功：**

｛

“code“:0,

”message”: ”success”

｝

**b)、失败：**

｛

“code“: 202000,

”message”: ” invalid node info“,

｝

### 2.3 获取最新已上报的错误日志

#### 2.3.1 传输协议规范

a)、网络传输协议：使用HTTP协议

b)、请求地址：/report /blockChainInfo/{attr}/{nodeIp}/{nodeP2PPort}

c)、请求方式：GET

d)、返回格式：json

#### 2.3.2 参数信息详情

| 序号 | 输入参数    | 类型          | 可为空 | 备注                       |
|------|-------------|---------------|--------|----------------------------|
| 1    | hostIp      | String        | 否     | 节点ip                     |
| 2    | nodeP2PPort | int           | 否     | 节点p2p端口                |
| 3    | attr        | String        | 否     | 接口标志：latest_node_log  |
| 序号 | 输出参数    | 类型          |        | 备注                       |
| 1    | code        | Int           | 否     | 返回码，0：成功 其它：失败 |
| 2    | message     | String        | 否     | 描述                       |
| 3    | data        | object        | 否     | 返回信息实体               |
| 3.1  | logTime     | LocalDateTime | 否     | 日志时间                   |
| 3.2  | rowNumber   | Int           | 否     | 日志在文件中的行数         |
| 3.3  | fileName    | String        | 否     | 文件名                     |

#### 2.3.3 入参事例

http://localhost:8080/fisco-bcos-node-mgr/report/blockChainInfo/latest_node_log
/127.0.0.1/30303

#### 2.3.4 出参示例

**a)、成功：**

｛

“code“:0,

”message”: ”success” ,

“data”: {

"rowNumber": 11419,

"logTime": "2018-11-21 15:06:38",

"fileName": "testLogFile"

}

｝

**b)、失败：**

｛

“code“: 202000,

”message”: ” invalid node info,

“data”:{}

｝

### 2.4 节点错误日志上报

#### 2.4.1 传输协议规范

a)、网络传输协议：使用HTTP协议

b)、请求地址：/report/latestErrorLog

c)、请求方式：POST

d)、请求头：Content-type: application/json

e)、返回格式：JSON

#### 2.4.2 参数信息详情

| 序号      | 输入参数       | 类型   | 可为空 | 备注                       |
|-----------|----------------|--------|--------|----------------------------|
| 1         | metricDataList | List   | 否     |                            |
| 1.1       |                | object | 否     |                            |
| 1.1.1     | hostIp         | String | 否     | 节点Ip                     |
| 1.1.2     | nodeP2PPort    | Int    | 否     | 节点端口                   |
| 1.1.3     | attr           | String | 否     | 属性：node_log             |
| 1.1.4     | metricValue    | list   | 否     |                            |
| 1.1.4.1   |                | object | 否     |                            |
| 1.1.4.1.1 | logTime        | String | 否     | 日志生成时间               |
| 1.1.4.1.2 | rowNumber      | Int    | 否     | 记录在日志文件中的行号     |
| 1.1.4.1.3 | logMsg         | String | 否     | 错误日志内容               |
| 1.1.4.1.4 | fileName       | String | 否     | 文件名                     |
| 序号      | 输出参数       | 类型   |        | 备注                       |
| 1         | code           | Int    | 否     | 返回码，0：成功 其它：失败 |
| 2         | message        | String | 否     | 描述                       |

#### 2.4.3 入参事例

<http://localhost:8080/fisco-bcos-node-mgr/report/blockInfo>

{

"metricDataList": [

{

"nodeP2PPort": "30306",

"metricValue": [

{

"rowNumber": 11419,

"logTime": "2018-11-21 15:06:38",

"logMsg": "dfasdfasfasf",

"fileName": "testLogFile"

},

{

"rowNumber": 11420,

"logTime": "2018-11-21 15:06:38",

"logMsg": "dfasdfasfasf",

"fileName": "testLogFile"

}

}

],

"hostIp": "127.0.0.1",

"attr": "node_log"

}

]

#### 2.4.4 出参示例

**a)、成功：**

｛

“code“:0,

”message”: ”success”

｝

**b)、失败：**

｛

“code“: 202000,

”message”: ” invalid node info“,

｝

**交易信息模块**

3.1 查询交易信息列表
--------------------

### 3.1.1 传输协议规范

a)、网络传输协议：使用HTTP协议

b)、请求地址：

/transList/{networkId}/{pageNumber}/{pageSize}?transactionHash={transactionHash}&blockNumber={blockNumber}

c)、请求方式：GET

d)、返回格式：JSON

### 3.1.2 参数信息详情

| 序号  | 输入参数        | 类型          | 可为空 | 备注                       |
|-------|-----------------|---------------|--------|----------------------------|
| 1     | networkId       | int           | 否     | 所属网络编号               |
| 2     | transactionHash | String        | 是     | 交易hash                   |
| 3     | blockNumber     | BigInteger    | 是     | 块高                       |
| 4     | pageSize        | int           | 否     | 每页记录数                 |
| 5     | pageNumber      | int           | 否     | 当前页码                   |
| 序号  | 输出参数        | 类型          |        | 备注                       |
| 1     | code            | Int           | 否     | 返回码，0：成功 其它：失败 |
| 2     | message         | String        | 否     | 描述                       |
| 3     | totalCount      | Int           | 否     | 总记录数                   |
| 4     | data            | List          | 否     | 交易信息列表               |
| 4.1   |                 | Object        |        | 交易信息对象               |
| 4.1.1 | transHash       | String        | 否     | 交易hash                   |
| 4.1.2 | networkId       | Int           | 否     | 所属网络编号               |
| 4.1.3 | blockNumber     | BigInteger    | 否     | 块高                       |
| 4.1.4 | blockTimestamp  | LocalDateTime | 否     | 出块时间戳                 |
| 4.1.5 | statisticsFlag  | Int           | 否     | 是否已经统计               |
| 4.1.6 | createTime      | LocalDateTime | 否     | 落库时间                   |
| 4.1.7 | modifyTime      | LocalDateTime | 否     | 修改时间                   |

### 3.1.3 入参事例

http://127.0.0.1:3001/mgr/webase-node-mgr/transaction/transList/300001/1/10?transactionHash=0x303daa78ebe9e6f5a6d9761a8eab4bf5a0ed0b06c28764488e4716de42e1df01

### 3.1.4 出参示例

**a)、成功：**

{

"code": 0,

"message": "success",

"data": [

{

"transHash":
"0x303daa78ebe9e6f5a6d9761a8eab4bf5a0ed0b06c28764488e4716de42e1df01",

"networkId": 300001,

"blockNumber": 133,

"blockTimestamp": "2019-03-15 09:36:13",

"statisticsFlag": 1,

"createTime": "2019-03-15 09:36:17",

"modifyTime": "2019-03-15 09:36:17"

}

],

"totalCount": 1

}

**b)、失败：**

｛

“code“: 102000:,

”message”: "system exception",

“data”:{}

｝

**帐号管理模块**

4.1 新增帐号
------------

### 4.1.1 传输协议规范

a)、网络传输协议：使用HTTP协议

b)、请求地址：/account/accountInfo

c)、请求方式：post

d)、请求头：Content-type: application/json

e)、返回格式：JSON

### 4.1.2 参数信息详情

| 序号 | 输入参数      | 类型          | 可为空 | 备注                       |
|------|---------------|---------------|--------|----------------------------|
| 1    | account       | String        | 否     | 帐号名称                   |
| 2    | accountPwd    | String        | 否     | 登录密码（sha256）         |
| 3    | roleId        | int           | 否     | 所属角色                   |
| 序号 | 输出参数      | 类型          |        | 备注                       |
| 1    | code          | Int           | 否     | 返回码，0：成功 其它：失败 |
| 2    | message       | String        | 否     | 描述                       |
| 3    | data          | object        | 否     | 返回信息实体               |
| 3.1  | account       | String        | 否     | 帐号                       |
| 3.2  | roleId        | Integer       | 否     | 所属角色                   |
| 3.3  | roleName      | String        | 否     | 角色名称                   |
| 3.4  | roleNameZh    | String        | 否     | 角色中文名                 |
| 3.5  | loginFailTime | Integer       | 是     | 登录失败次数               |
| 3.6  | accountStatus | Integer       | 否     | 帐号状态                   |
| 3.7  | description   | String        | 是     | 备注                       |
| 3.8  | createTime    | LocalDateTime | 否     | 创建时间                   |
| 3.9  | modifyTime    | LocalDateTime | 否     | 修改时间                   |

### 4.1.3 入参事例

[http://localhost:8080/fisco-bcos-node-mgr/account/accountInfo
](http://localhost:8080/fisco-bcos-node-mgr/account/accountInfo%20)

{

"account": "testAccount",

"accountPwd":
"3f21a8490cef2bfb60a9702e9d2ddb7a805c9bd1a263557dfd51a7d0e9dfa93e",

"roleId": 100001

}

### 4.1.4 出参示例

**a)、成功：**

{

"code": 0,

"message": "success",

"data": {

"account": "testAccount",

"roleId": 100001,

"roleName": "visitor",

"roleNameZh": "访客",

"loginFailTime": 0,

"accountStatus": 1,

"description": null,

"createTime": "2019-03-04 15:11:44",

"modifyTime": "2019-03-04 15:11:44"

}

}

**b)、失败：**

｛

“code“: 102000:,

”message”: " system exception",

“data”:{}

｝

4.2 修改帐号
------------

### 4.2.1 传输协议规范

a)、网络传输协议：使用HTTP协议

b)、请求地址：/account/accountInfo

c)、请求方式：PUT

d)、请求头：Content-type: application/json

e)、返回格式：JSON

### 4.2.2 参数信息详情

| 序号 | 输入参数      | 类型          | 可为空 | 备注                       |
|------|---------------|---------------|--------|----------------------------|
| 1    | account       | String        | 否     | 帐号名称                   |
| 2    | accountPwd    | String        | 否     | 登录密码（sha256）         |
| 3    | roleId        | int           | 否     | 所属角色                   |
| 序号 | 输出参数      | 类型          |        | 备注                       |
| 1    | code          | Int           | 否     | 返回码，0：成功 其它：失败 |
| 2    | message       | String        | 否     | 描述                       |
| 3    | data          | object        | 否     | 返回信息实体               |
| 3.1  | account       | String        | 否     | 帐号                       |
| 3.2  | roleId        | Integer       | 否     | 所属角色                   |
| 3.3  | roleName      | String        | 否     | 角色名称                   |
| 3.4  | roleNameZh    | String        | 否     | 角色中文名                 |
| 3.5  | loginFailTime | Integer       | 是     | 登录失败次数               |
| 3.6  | accountStatus | Integer       | 否     | 帐号状态                   |
| 3.7  | description   | String        | 是     | 备注                       |
| 3.8  | createTime    | LocalDateTime | 否     | 创建时间                   |
| 3.9  | modifyTime    | LocalDateTime | 否     | 修改时间                   |

### 4.2.3 入参事例

[http://localhost:8080/fisco-bcos-node-mgr/account/accountInfo
](http://localhost:8080/fisco-bcos-node-mgr/account/accountInfo%20)

{

"account": "testAccount",

"accountPwd":
"82ca84cf0d2ae423c09a214cee2bd5a7ac65c230c07d1859b9c43b30c3a9fc80",

"roleId": 100001

}

### 4.2.4 出参示例

**a)、成功：**

{

"code": 0,

"message": "success",

"data": {

"account": "testAccount",

"roleId": 100001,

"roleName": "visitor",

"roleNameZh": "访客",

"loginFailTime": 0,

"accountStatus": 1,

"description": null,

"createTime": "2019-03-04 15:11:44",

"modifyTime": "2019-03-04 15:11:44"

}

}

**b)、失败：**

｛

“code“: 102000:,

”message”: " system exception",

“data”:{}

｝

4.3 删除帐号
------------

### 4.3.1 传输协议规范 

a)、网络传输协议：使用HTTP协议

b)、请求地址：/account/{account}

c)、请求方式：DELETE

d)、返回格式：JSON

### 4.3.2 参数信息详情

| 序号 | 输入参数 | 类型   | 可为空 | 备注                       |
|------|----------|--------|--------|----------------------------|
| 1    | account  | String | 否     | 帐号名称                   |
| 序号 | 输出参数 | 类型   |        | 备注                       |
| 1    | code     | Int    | 否     | 返回码，0：成功 其它：失败 |
| 2    | message  | String | 否     | 描述                       |
| 3    | data     | object | 是     | 返回信息实体（空）         |

### 4.3.3 入参事例

<http://localhost:8080/fisco-bcos-node-mgr/account/testAccount>

### 4.3.4 出参示例

**a)、成功：**

{

"code": 0,

"data": {},

"message": "Success"

}

**b)、失败：**

｛

“code“: 102000:,

”message”: " system exception",

“data”:{}

｝

4.4 查询帐号列表
----------------

### 4.4.1 传输协议规范

a)、网络传输协议：使用HTTP协议

b)、请求地址: /account/accountList/{pageNumber}/{pageSize}?account={account}

c)、请求方式：GET

d)、返回格式：JSON

### 4.4.2 参数信息详情

| 序号  | 输入参数      | 类型          | 可为空 | 备注                       |
|-------|---------------|---------------|--------|----------------------------|
| 1     | pageSize      | Int           | 否     | 每页记录数                 |
| 2     | pageNumber    | Int           | 否     | 当前页码                   |
| 3     | account       | String        | 是     | 帐号                       |
|       | 输出参数      | 类型          |        | 备注                       |
| 1     | code          | Int           | 否     | 返回码，0：成功 其它：失败 |
| 2     | message       | String        | 否     | 描述                       |
| 3     | totalCount    | Int           | 否     | 总记录数                   |
| 4     | data          | List          | 是     | 节点列表                   |
| 4.1   |               | Object        |        | 节点信息对象               |
| 4.1.1 | account       | String        | 否     | 帐号                       |
| 4.1.2 | roleId        | Integer       | 否     | 所属角色                   |
| 4.1.3 | roleName      | String        | 否     | 角色名称                   |
| 4.1.4 | roleNameZh    | String        | 否     | 角色中文名                 |
| 4.1.5 | loginFailTime | Integer       | 是     | 登录失败次数               |
| 4.1.6 | accountStatus | Integer       | 否     | 帐号状态                   |
| 4.1.7 | description   | String        | 是     | 备注                       |
| 4.1.8 | createTime    | LocalDateTime | 否     | 创建时间                   |
| 4.1.9 | modifyTime    | LocalDateTime | 否     | 修改时间                   |

### 4.4.3 入参事例

http://localhost:8080/fisco-bcos-node-mgr/fisco-bcos-node-mgr/account/accountList/1/10?account=

### 4.4.4 出参示例

**a)、成功：**

{

"code": 0,

"message": "success",

"data": [

{

"account": "testAccount",

"roleId": 100001,

"roleName": "visitor",

"roleNameZh": "访客",

"loginFailTime": 0,

"accountStatus": 1,

"description": null,

"createTime": "2019-03-04 15:11:44",

"modifyTime": "2019-03-04 15:18:47"

},

{

"account": "admin",

"roleId": 100000,

"roleName": "admin",

"roleNameZh": "管理员",

"loginFailTime": 0,

"accountStatus": 2,

"description": null,

"createTime": "2019-02-14 17:33:50",

"modifyTime": "2019-02-14 17:45:53"

}

],

"totalCount": 2

}

**b)、失败：**

｛

“code“: 102000:,

”message”: "system exception",

“data”:{}

｝

4.5 更新当前密码
----------------

### 4.5.1 传输协议规范

a)、网络传输协议：使用HTTP协议

b)、请求地址：/account/passwordUpdate

c)、请求方式：put

d)、请求头：Content-type: application/json

e)、返回格式：JSON

### 4.5.2 参数信息详情

| 序号 | 输入参数      | 类型   | 可为空 | 备注                       |
|------|---------------|--------|--------|----------------------------|
| 1    | oldAccountPwd | String | 否     | 旧密码（sha256）           |
| 2    | newAccountPwd | String | 否     | 新密码（sha256）           |
| 序号 | 输出参数      | 类型   |        | 备注                       |
| 1    | code          | Int    | 否     | 返回码，0：成功 其它：失败 |
| 2    | message       | String | 否     | 描述                       |

### 4.5.3 入参事例

http://localhost:8080/fisco-bcos-node-mgr/account/passwordUpdate

{

"oldAccountPwd":
"dfdfgdg490cef2bfb60a9702erd2ddb7a805c9bd1arrrewefd51a7d0etttfa93e ",

"newAccountPwd":
"3f21a8490cef2bfb60a9702e9d2ddb7a805c9bd1a263557dfd51a7d0e9dfa93e"

}

### 4.5.4 出参示例

**a)、成功：**

{

"code": 0,

"message": "success"

}

**b)、失败：**

｛

“code“: 102000:,

”message”: " system exception",

“data”:{}

｝

5 区块管理模块
==============

### 5.1 查询区块列表

#### 5.1.1 传输协议规范

a)、网络传输协议：使用HTTP协议

b)、请求地址:

/block/blockList/{networkId}/{pageNumber}/{pageSize}}?pkHash={pkHash}&
blockNumber={ blockNumber}

c)、请求方式：GET

d)、返回格式：JSON

#### 5.1.2 参数信息详情

| 序号  | 输入参数       | 类型          | 可为空 | 备注                       |
|-------|----------------|---------------|--------|----------------------------|
| 1     | networkId      | Int           | 否     | 当前所属链                 |
| 2     | pageSize       | Int           | 否     | 每页记录数                 |
| 3     | pageNumber     | Int           | 否     | 当前页码                   |
| 4     | pkHash         | String        | 是     | 区块hash                   |
| 5     | blockNumber    | BigInteger    | 是     | 块高                       |
|       | 输出参数       | 类型          |        | 备注                       |
| 1     | code           | Int           | 否     | 返回码，0：成功 其它：失败 |
| 2     | message        | String        | 否     | 描述                       |
| 3     | totalCount     | Int           | 否     | 总记录数                   |
| 4     | data           | List          | 是     | 区块列表                   |
| 4.1   |                | Object        |        | 区块信息对象               |
| 4.1.1 | pkHash         | String        | 否     | 块hash                     |
| 4.1.2 | networkId      | int           | 否     | 所属网络编号               |
| 4.1.3 | blockNumber    | BigInteger    | 否     | 块高                       |
| 4.1.4 | miner          | String        | 否     | 矿工                       |
| 4.1.5 | blockTimestamp | LocalDateTime | 否     | 出块时间                   |
| 4.1.6 | transCount     | int           | 否     | 交易数                     |
| 4.1.7 | createTime     | LocalDateTime | 否     | 创建时间                   |
| 4.1.8 | modifyTime     | LocalDateTime | 否     | 修改时间                   |

#### 5.1.3 入参事例

http://127.0.0.1:5002/mgr/fisco-bcos-node-mgr/block/blockList/300001/1/10?pkHash=

#### 5.1.4 出参示例

**a)、成功：**

{

"code": 0,

"message": "success",

"data": [

{

"pkHash": "0xe6438646633542e26d053f75931d74a258a607464207e1343344c100da89e661",

"networkId": 300001,

"blockNumber": 1442,

"miner": "",

"blockTimestamp": "2019-02-27 19:18:23",

"transCount": 1,

"createTime": "2019-03-04 10:29:07",

"modifyTime": "2019-03-04 10:29:07"

},

{

"pkHash": "0x2e036eba6d1581a280712276e06517987c7be40f0f252fca34303eef157d8c3d",

"networkId": 300001,

"blockNumber": 1441,

"miner": "",

"blockTimestamp": "2019-02-27 19:18:22",

"transCount": 1,

"createTime": "2019-03-04 10:29:07",

"modifyTime": "2019-03-04 10:29:07"

}

],

"totalCount": 2

}

**b)、失败：**

｛

“code“: 102000,

”message”: "system exception",

“data”:{}

｝

**6 合约管理模块**

### 6.1 新增合约

#### 6.1.1 传输协议规范 

a)、网络传输协议：使用HTTP协议

b)、请求地址：/contract/contractInfo

c)、请求方式：POST

d)、请求头：Content-type: application/json

e)、返回格式：JSON

#### 6.1.2 参数信息详情

| 序号 | 输入参数        | 类型          | 可为空 | 备注                                            |
|------|-----------------|---------------|--------|-------------------------------------------------|
| 1    | networkId       | int           | 否     | 所属网络编号                                    |
| 2    | contractName    | string        | 否     | 合约名称                                        |
| 3    | contractVersion | String        | 否     | 合约版本                                        |
| 4    | contractSource  | String        | 否     | 源码（base64）                                  |
| 序号 | 输出参数        | 类型          |        | 备注                                            |
| 1    | code            | Int           | 否     | 返回码，0：成功 其它：失败                      |
| 2    | message         | String        | 否     | 描述                                            |
| 3    | data            | object        |        | 返回信息实体（成功时不为空）                    |
| 3.1  | contractId      | int           | 否     | 节点编号                                        |
| 3.2  | contractName    | String        | 否     | 合约名称                                        |
| 3.3  | networkId       | Int           | 否     | 所属网络编号                                    |
| 3.4  | chainIndex      | Int           | 是     | 链上索引                                        |
| 3.5  | contractType    | Int           | 否     | 合约类型(0-普通合约，1-系统合约)                |
| 3.6  | contractSource  | String        | 否     | 合约源码                                        |
| 3.7  | contractStatus  | Int           | 否     | 部署状态（1：未部署，2：部署成功，3：部署失败） |
| 3.8  | contractAbi     | String        | 是     | 编译合约生成的abi文件内容                       |
| 3.9  | contractBin     | String        | 是     | 合约binary                                      |
| 3.10 | bytecodeBin     | String        | 是     | 合约bin                                         |
| 3.11 | contractAddress | String        | 是     | 合约地址                                        |
| 3.12 | deployTime      | LocalDateTime | 是     | 部署时间                                        |
| 3.13 | contractVersion | String        | 否     | 合约版本                                        |
| 3.14 | description     | String        | 是     | 备注                                            |
| 3.15 | createTime      | LocalDateTime | 否     | 创建时间                                        |
| 3.16 | modifyTime      | LocalDateTime | 是     | 修改时间                                        |

#### 6.1.3 入参事例

<http://localhost:8080/fisco-bcos-node-mgr/contract>*/ contractInfo*

{

"networkId": "300001",

"contractName": "Helllo",

"contractVersion": "v1.0",

"contractSource": "cHJhZ21hIHNvbGlkaXR5IF4wLjQuMjsN

}

#### 6.1.4 出参示例

**a)、成功：**

{

"code": 0,

"message": "success",

"data": {

"contractId": 200035,

"contractName": "Helllo",

"networkId": 300001,

"chainIndex": null,

"contractType": 0,

"contractSource": "cHJhZ21hIHNvbGlkaXR5IF4wLjQuMjsN",

"contractStatus": 1,

"contractAbi": null,

"contractBin": null,

"bytecodeBin": null,

"contractAddress": null,

"deployTime": null,

"contractVersion": "v1.0",

"description": null,

"createTime": "2019-03-11 10:11:59",

"modifyTime": "2019-03-11 10:11:59"

}

}

**b)、失败：**

｛

“code“: 102000:,

”message”: " system exception",

“data”:{}

｝

### 6.2 修改合约

#### 6.2.1 传输协议规范 

a)、网络传输协议：使用HTTP协议

b)、请求地址：/contract/contractInfo

c)、请求方式：PUT

d)、请求头：Content-type: application/json

e)、返回格式：JSON

#### 6.2.2 参数信息详情

| 序号 | 输入参数        | 类型          | 可为空 | 备注                                            |
|------|-----------------|---------------|--------|-------------------------------------------------|
| 1    | contractId      | int           | 否     | 节点编号                                        |
| 2    | contractName    | String        | 否     | 合约名称                                        |
| 3    | networkId       | Int           | 否     | 所属网络编号                                    |
| 4    | chainIndex      | Int           | 是     | 链上索引                                        |
| 5    | contractType    | Int           | 否     | 合约类型(0-普通合约，1-系统合约)                |
| 6    | contractSource  | String        | 否     | 合约源码                                        |
| 7    | contractStatus  | Int           | 否     | 部署状态（1：未部署，2：部署成功，3：部署失败） |
| 8    | contractAbi     | String        | 是     | 编译合约生成的abi文件内容                       |
| 9    | contractBin     | String        | 是     | 合约binary                                      |
| 10   | bytecodeBin     | String        | 是     | 合约bin                                         |
| 11   | contractAddress | String        | 是     | 合约地址                                        |
| 12   | deployTime      | LocalDateTime | 是     | 部署时间                                        |
| 13   | contractVersion | String        | 否     | 合约版本                                        |
| 14   | description     | String        | 是     | 备注                                            |
| 序号 | 输出参数        | 类型          |        | 备注                                            |
| 1    | code            | Int           | 否     | 返回码，0：成功 其它：失败                      |
| 2    | message         | String        | 否     | 描述                                            |
| 3    | data            | object        | 是     | 返回信息实体（空）                              |

#### 6.2.3 入参事例

[http://localhost:8080/fisco-bcos-node-mgr/contract/contractInfo
](http://localhost:8080/fisco-bcos-node-mgr/contract/contractInfo%20)

{

"networkId": "300001",

"contractId": 200035,

"contractBin": "60606040526000357c010000000",

"bytecodeBin": "6060604052341561000c57fe",

"contractSource": "cHJhZ21hIHNvbGlkaXR5IF4wLjQuMjsNCmNvbQ==",

"contractAbi":
"[{\\"constant\\":false,\\"inputs\\":[{\\"name\\":\\"n\\",\\"type\\":\\"string\\"}],\\"name\\":\\"set\\",\\"outputs\\":[],\\"payable\\":false,\\"type\\":\\"function\\"}\\"}]"

}

#### 6.2.4 出参示例

**a)、成功：**

{

"code": 0,

"message": "success",

"data": {

"contractId": 200035,

"contractName": "Helllo",

"networkId": 300001,

"chainIndex": null,

"contractType": 0,

"contractSource": "cHJhZ21hIHNvbGgICAgfQ0KfQ==",

"contractStatus": 1,

"contractAbi":
"[{\\"constant\\":false,\\"inputs\\":[{\\"name\\":\\"n\\",\\"type\\":\\"string\\"}],\\"name\\":\\"set\\",\\"outputs\\":[],\\"payable\\":false,\\"type\\":\\"function\\"}]",

"contractBin": "606060405260004a9354c32393ae5c9bfee50029",

"bytecodeBin": "6060604052341561000c57fe5b6040516103dd3803806103dd829",

"contractAddress": null,

"deployTime": null,

"contractVersion": "v1.0",

"description": null,

"createTime": "2019-03-11 10:11:59",

"modifyTime": "2019-03-11 10:28:06"

}

}

**b)、失败：**

｛

“code“: 102000:,

”message”: " system exception",

“data”:{}

｝

### 6.3 删除合约

#### 6.3.1 传输协议规范 

a)、网络传输协议：使用HTTP协议

b)、请求地址：/contract/{contractId}

c)、请求方式：DELETE

d)、请求头：Content-type: application/json

e)、返回格式：JSON

#### 6.3.2 参数信息详情

| 序号 | 输入参数   | 类型   | 可为空 | 备注                       |
|------|------------|--------|--------|----------------------------|
| 1    | contractId | int    | 否     | 合约编号名称               |
| 序号 | 输出参数   | 类型   |        | 备注                       |
| 1    | code       | Int    | 否     | 返回码，0：成功 其它：失败 |
| 2    | message    | String | 否     | 描述                       |
| 3    | data       | object | 是     | 返回信息实体（空）         |

#### 6.3.3 入参事例

<http://localhost:8080/fisco-bcos-node-mgr/contract>*/{contractId}*

#### 6.3.4 出参示例

**a)、成功：**

{

"code": 0,

"data": {},

"message": "Success"

}

**b)、失败：**

｛

“code“: 102000:,

”message”: " system exception",

“data”:{}

### 6.4 查询合约列表

#### 6.4.1 传输协议规范

a)、网络传输协议：使用HTTP协议

b)、请求地址： /contract/contractList/{networkId}/{pageNumber}/{pageSize}

c)、请求方式：GET

d)、返回格式：JSON

#### 6.4.2 参数信息详情

| 序号   | 输入参数        | 类型          | 可为空 | 备注                                            |
|--------|-----------------|---------------|--------|-------------------------------------------------|
| 1      | networkId       | int           | 否     | 网络id                                          |
| 2      | pageSize        | int           | 否     | 每页记录数                                      |
| 3      | pageNumber      | int           | 否     | 当前页码                                        |
|        |                 |               |        |                                                 |
| 序号   | 输出参数        | 类型          | 可为空 | 备注                                            |
| 1      | code            | Int           | 否     | 返回码，0：成功 其它：失败                      |
| 2      | message         | String        | 否     | 描述                                            |
| 3      | totalCount      | Int           | 否     | 总记录数                                        |
| 4      | data            | List          | 是     | 列表                                            |
| 5.1    |                 | Oject         |        | 返回信息实体                                    |
| 5.1.1  | contractId      | int           | 否     | 节点编号                                        |
| 5.1.2  | contractName    | String        | 否     | 合约名称                                        |
| 5.1.3  | networkId       | Int           | 否     | 所属网络编号                                    |
| 5.1.4  | chainIndex      | Int           | 是     | 链上索引                                        |
| 5.1.5  | contractType    | Int           | 否     | 合约类型(0-普通合约，1-系统合约)                |
| 5.1.6  | contractSource  | String        | 否     | 合约源码                                        |
| 5.1.7  | contractStatus  | Int           | 否     | 部署状态（1：未部署，2：部署成功，3：部署失败） |
| 5.1.8  | contractAbi     | String        | 是     | 编译合约生成的abi文件内容                       |
| 5.1.9  | contractBin     | String        | 是     | 合约binary                                      |
| 5.1.10 | bytecodeBin     | String        | 是     | 合约bin                                         |
| 5.1.11 | contractAddress | String        | 是     | 合约地址                                        |
| 5.1.12 | deployTime      | LocalDateTime | 是     | 部署时间                                        |
| 5.1.13 | contractVersion | String        | 否     | 合约版本                                        |
| 5.1.14 | description     | String        | 是     | 备注                                            |
| 5.1.15 | createTime      | LocalDateTime | 否     | 创建时间                                        |
| 5.1.16 | modifyTime      | LocalDateTime | 是     | 修改时间                                        |

#### 6.4.3 入参事例

*http://localhost:8080/fisco-bcos-node-mgr/*contract/contractList/300001/1/15

#### 6.4.4 出参示例

**a)、成功：**

{

"code": 0,

"message": "success",

"data": [

{

"contractId": 200034,

"contractName": "Hello3",

"networkId": 300001,

"chainIndex": null,

"contractType": 0,

"contractSource": "cHJhZ21hIHNvbQ0KfQ==",

"contractStatus": 1,

"contractAbi":
"[{\\"constant\\":false,\\"inputs\\":[{\\"name\\":\\"n\\",\\"type\\":\\"string\\"}],\\"name\\":\\"set\\",\\"outputs\\":[],\\"payable\\":false,\\"type\\":\\"function\\"},{\\"constant\\":\\"}]",

"contractBin": "60606040526945a9521ffdcb8fe5825c208260d0029",

"bytecodeBin": "6060604052341561000c57fe5b06103dd83029",

"contractAddress": null,

"deployTime": null,

"contractVersion": "1",

"description": null,

"createTime": "2019-02-21 14:47:14",

"modifyTime": "2019-03-12 11:02:03"

}

],

"totalCount": 1

}

**b)、失败：**

｛

“code“: 102000:,

”message”: " system exception",

“data”:{}

｝

### 6.5 查询合约信息（未使用）

#### 6.5.1 传输协议规范

a)、网络传输协议：使用HTTP协议

b)、请求地址： /contract/{contractId}

c)、请求方式：GET

d)、返回格式：JSON

#### 6.5.2 参数信息详情

| 序号 | 输入参数        | 类型          | 可为空 | 备注                                            |
|------|-----------------|---------------|--------|-------------------------------------------------|
| 1    | contractId      | int           | 否     | 合约编号                                        |
| 序号 | 输出参数        | 类型          | 可为空 | 备注                                            |
| 1    | code            | Int           | 否     | 返回码，0：成功 其它：失败                      |
| 2    | message         | String        | 否     | 描述                                            |
| 3    |                 | Oject         |        | 返回信息实体                                    |
| 3.1  | contractId      | int           | 否     | 节点编号                                        |
| 3.2  | contractName    | String        | 否     | 合约名称                                        |
| 3.3  | networkId       | Int           | 否     | 所属网络编号                                    |
| 3.4  | chainIndex      | Int           | 是     | 链上索引                                        |
| 3.5  | contractType    | Int           | 否     | 合约类型(0-普通合约，1-系统合约)                |
| 3.6  | contractSource  | String        | 否     | 合约源码                                        |
| 3.7  | contractStatus  | Int           | 否     | 部署状态（1：未部署，2：部署成功，3：部署失败） |
| 3.8  | contractAbi     | String        | 是     | 编译合约生成的abi文件内容                       |
| 3.9  | contractBin     | String        | 是     | 合约binary                                      |
| 3.10 | bytecodeBin     | String        | 是     | 合约bin                                         |
| 3.11 | contractAddress | String        | 是     | 合约地址                                        |
| 3.12 | deployTime      | LocalDateTime | 是     | 部署时间                                        |
| 3.13 | contractVersion | String        | 否     | 合约版本                                        |
| 3.14 | description     | String        | 是     | 备注                                            |
| 3.15 | createTime      | LocalDateTime | 否     | 创建时间                                        |
| 3.16 | modifyTime      | LocalDateTime | 是     | 修改时间                                        |

#### 6.5.3 入参事例

*http://localhost:8080/fisco-bcos-node-mgr/contract/200001*

#### 6.5.4 出参示例

**a)、成功：**

{

"code": 0,

"message": "success",

"data": {

"contractId": 200001,

"contractName": "33",

"networkId": 300001,

"contractSource": "efsdfde",

"contractStatus": 1,

"contractAbi": "sdfsd",

"contractAddress": "vcde",

"deployTime": null,

"contractVersion": "33",

"description": "vcde",

"createTime": "2018-12-02 16:09:57",

"modifyTime": "2018-12-02 16:22:25"

}

}

**b)、失败：**

｛

“code“: 102000:,

”message”: " system exception",

“data”:{}

｝

### 6.6 部署合约

#### 6.6.1 传输协议规范 

a)、网络传输协议：使用HTTP协议

b)、请求地址：/contract/deploy

c)、请求方式：POST

d)、请求头：Content-type: application/json

e)、返回格式：JSON

#### 6.6.2 参数信息详情

| 序号 | 输入参数          | 类型           | 可为空 | 备注                       |
|------|-------------------|----------------|--------|----------------------------|
| 1    | contractId        | int            | 否     | 节点编号                   |
| 2    | networkId         | Int            | 否     | 所属网络编号               |
| 3    | contractSource    | String         | 否     | 合约源码                   |
| 4    | contractAbi       | String         | 否     | 编译合约生成的abi文件内容  |
| 5    | contractBin       | String         | 否     | 合约binary                 |
| 6    | bytecodeBin       | String         | 否     | 合约bin                    |
| 7    | description       | String         | 是     | 备注                       |
| 8    | userId            | String         | 否     | 私钥用户编号               |
| 9    | constructorParams | List\<Object\> | 是     | 构造函数入参               |
| 序号 | 输出参数          | 类型           |        | 备注                       |
| 1    | code              | Int            | 否     | 返回码，0：成功 其它：失败 |
| 2    | message           | String         | 否     | 描述                       |
| 3    | data              | object         | 是     | 返回信息实体（空）         |

#### 6.6.3 入参事例

*http://localhost:8080/fisco-bcos-node-mgr/contract/deploy*

{

"networkId": "300001",

"contractBin": "60606040526000357c01e08980029",

"bytecodeBin": null,

"contractAbi":
"[{\\"constant\\":false,\\"inputs\\":[{\\"name\\":\\"n\\",\\"type\\":\\"bytes\\"}],\\"name\\":\\"set\\",\\"outputs\\":[],\\"payable\\":false,\\"type\\":\\"function\\"}]",

"contractSource": "cHJhZ21hIHudCByZXR1Sk7DQogICAgfQ0KfQ==",

"userId": 700001,

"contractId": 200033

}

#### 6.6.4 出参示例

**a)、成功：**

{

"code": 0,

"message": "success",

"data": {

"contractId": 200035,

"contractName": "Helllo",

"networkId": 300001,

"chainIndex": null,

"contractType": 0,

"contractSource": "cHJhZ21hIHNvbGgICAgfQ0KfQ==",

"contractStatus": 1,

"contractAbi":
"[{\\"constant\\":false,\\"inputs\\":[{\\"name\\":\\"n\\",\\"type\\":\\"string\\"}],\\"name\\":\\"set\\",\\"outputs\\":[],\\"payable\\":false,\\"type\\":\\"function\\"}]",

"contractBin": "606060405260004a9354c32393ae5c9bfee50029",

"bytecodeBin": "6060604052341561000c57fe5b6040516103dd3803806103dd829",

"contractAddress": null,

"deployTime": null,

"contractVersion": "v1.0",

"description": null,

"createTime": "2019-03-11 10:11:59",

"modifyTime": "2019-03-11 10:28:06"

}

}

**b)、失败：**

｛

“code“: 102000:,

”message”: " system exception",

“data”:{}

｝

### 6.7 发送交易

#### 6.7.1 传输协议规范 

a)、网络传输协议：使用HTTP协议

b)、请求地址：/contract/transaction

c)、请求方式：POST

d)、请求头：Content-type: application/json

e)、返回格式：JSON

#### 6.7.2 参数信息详情

| 序号 | 输入参数     | 类型           | 可为空 | 备注                       |
|------|--------------|----------------|--------|----------------------------|
| 1    | networkId    | Int            | 否     | 所属网络编号               |
| 2    | abiInfo      | List\<Object\> | 否     | 合约编译的abi              |
| 3    | userId       | Integer        | 否     | 私钥用户编号               |
| 4    | version      | String         | 否     | 合约版本                   |
| 5    | contractName | String         | 否     | 合约名称                   |
| 6    | funcName     | String         | 否     | 合约方法名                 |
| 7    | funcParam    | List\<Object\> | 是     | 合约方法入参               |
| 序号 | 输出参数     | 类型           |        | 备注                       |
| 1    | code         | Int            | 否     | 返回码，0：成功 其它：失败 |
| 2    | message      | String         | 否     | 描述                       |
| 3    | data         | object         | 是     | 返回信息实体（空）         |

#### 6.7.3 入参事例

*http://localhost:8080/fisco-bcos-node-mgr/contract/deploy*

{

"networkId": "300001",

"contractBin": "6060604052600f8dee08980029",

"bytecodeBin": null,

"contractAbi":
"[{\\"constant\\":false,\\"inputs\\":[{\\"name\\":\\"n\\",\\"type\\":\\"bytes\\"}],\\"name\\":\\"set\\",\\"outputs\\":[],\\"payable\\":false,\\"type\\":\\"function\\"}]",

"contractSource": "cHJhZ21hIHNvbGlkaXR5IF4wLjQuMjfQ==",

"userId": 700001,

"contractId": 200033

}

#### 6.7.4 出参示例

**a)、成功：**

{

"code": 0,

"message": "success",

"data": {}

}

**b)、失败：**

{

"code": 202046,

"message": "contract has not compiled",

"data": null

}

**7 区块管理模块**

### 7.1 查询区块列表

#### 7.1.1 传输协议规范

a)、网络传输协议：使用HTTP协议

b)、请求地址： /block/blockList/{networkId}/{pageNumber}/{pageSize}?
pkHash=hashVal?blockNumber=blockValue1

c)、请求方式：GET

d)、返回格式：JSON

#### 7.1.2 参数信息详情

| 序号  | 输入参数       | 类型   | 可为空 | 备注         |
|-------|----------------|--------|--------|--------------|
| 1     | networkId      | int    | 否     | 网络id       |
| 2     | pageSize       | int    | 否     | 每页记录数   |
| 3     | pageNumber     | int    | 否     | 当前页码     |
| 4     | pkHash         | String | 是     | 区块hash     |
| 5     | blockNumber    | Int    | 是     | 块高         |
| 序号  | 输出参数       | 类型   |        | 备注         |
| 1     | code           | int    | 否     | 返回码       |
| 2     | message        | String | 否     | 描述信息     |
| 3     | totalCount     | int    | 否     | 总记录数     |
| 4     | data           | Array  | 否     | 返回信息列表 |
| 4.1   |                | Oject  |        | 返回信息实体 |
| 4.1.1 | blockNumber    | int    | 否     | 块高         |
| 4.1.2 | pkHash         | String | 否     | 区块hash     |
| 4.1.3 | blockTimestamp | String | 否     | 生成时间     |
| 4.1.4 | transCount     | int    | 否     | 块包含交易数 |
| 4.1.5 | networkId      | int    | 否     | 网络id       |
| 4.1.6 | miner          | String | 否     | 矿工         |
| 4.1.7 | createTime     | String | 否     | 创建时间     |
| 4.1.8 | modifyTime     | String | 否     | 修改时间     |

#### 7.1.3 入参事例

*http://localhost:8080/fisco-bcos-node-mgr/block/blockList/300001/1/4*

#### 7.1.4 出参示例

**a)、成功：**

{

"code": 0,

"message": "success",

"data": [

{

"pkHash": "0x22a6535dcdef14cf1e4b8427fe4c7d585faaff76c9257bf184f701e04583fc28",

"networkId": 300001,

"blockNumber": 747309,

"miner": "b3b574d098120fa1e63a3a947cf",

"blockTimestamp": "2019-03-11 14:43:43",

"transCount": 1,

"createTime": "2019-03-11 14:43:45",

"modifyTime": "2019-03-11 14:43:45"

}

],

"totalCount": 1

}

**b)、失败：**

｛

“code“: 102000:,

”message”: " system exception",

“data”:{}

｝

**8 服务器监控相关**

### 8.1 获取节点监控信息

#### 8.1.1 传输协议规范

a)、网络传输协议：使用HTTP协议

b)、请求地址： /chain/mointorInfo/{nodeId}?beginDate={beginDate}&
endDate={endDate}& contrastBeginDate={contrastBeginDate}&
contrastEndDate={contrastEndDate}& gap={gap}

c)、请求方式：GET

d)、返回格式：JSON

#### 8.1.2 参数信息详情

| 序号      | 输入参数          | 类型            | 可为空 | 备注                                                           |
|-----------|-------------------|-----------------|--------|----------------------------------------------------------------|
| 1         | nodeId            | int             | 否     | 网络id                                                         |
| 2         | beginDate         | LocalDateTime   | 是     | 显示时间（开始） yyyy-MM-dd'T'HH:mm:ss.SSS 2019-03-13T00:00:00 |
| 3         | endDate           | LocalDateTime   | 是     | 显示时间（结束）                                               |
| 4         | contrastBeginDate | LocalDateTime   | 是     | 对比时间（开始）                                               |
| 5         | contrastEndDate   | LocalDateTime   | 是     | 对比时间（结束）                                               |
| 6         | gap               | Int             | 是     | 数据粒度                                                       |
| 序号      | 输出参数          | 类型            |        | 备注                                                           |
| 1         | code              | int             | 否     | 返回码                                                         |
| 2         | message           | String          | 否     | 描述信息                                                       |
| 3         | data              | Array           | 否     | 返回信息列表                                                   |
| 3.1       |                   | Oject           |        | 返回信息实体                                                   |
| 3.1.1     | metricType        | String          | 否     | 测量类型：blockHeight、pbftView                                |
| 3.1.2     | data              | Oject           | 否     |                                                                |
| 3.1.2.1   | lineDataList      | Oject           | 否     |                                                                |
| 3.1.2.1.1 | timestampList     | List\<String\>  | 否     | 时间戳列表                                                     |
| 3.1.2.1.2 | valueList         | List\<Integer\> | 否     | 值列表                                                         |
| 3.1.2.2   | contrastDataList  | Oject           | 否     |                                                                |
| 3.1.2.2.1 | timestampList     | List\<String\>  | 否     | 时间戳列表                                                     |
| 3.1.2.2.2 | valueList         | List\<Integer\> | 否     | 值列表                                                         |

#### 8.1.3 入参事例

http://127.0.0.1:4001/mgr/webcaf-node-mgr/chain/mointorInfo/500001?gap=60&beginDate=2019-03-13T00:00:00&endDate=2019-03-13T14:34:22&contrastBeginDate=2019-03-13T00:00:00&contrastEndDate=2019-03-13T14:34:22

#### 8.1.4 出参示例

**a)、成功：**

{

"code": 0,

"message": "success",

"data": [

{

"metricType": "blockHeight",

"data": {

"lineDataList": {

"timestampList": [

1552406401042,

1552406701001

],

"valueList": [

747309,

747309

]

},

"contrastDataList": {

"timestampList": [

1552320005000,

1552320301001

],

"valueList": [

null,

747309

]

}

}

},

{

"metricType": "pbftView",

"data": {

"lineDataList": {

"timestampList": null,

"valueList": [

118457,

157604

]

},

"contrastDataList": {

"timestampList": null,

"valueList": [

null,

33298

]

}

}

}

]

}

**b)、失败：**

｛

“code“: 102000:,

”message”: " system exception",

“data”:{}

｝

### 8.2 获取服务器监控信息

#### 8.2.1 传输协议规范

a)、网络传输协议：使用HTTP协议

b)、请求地址：

performance/ratio/{nodeId}?gap={gap}&beginDate={beginDate}&endDate={endDate}&contrastBeginDate={contrastBeginDate}&contrastEndDate={contrastEndDate}

c)、请求方式：GET

d)、返回格式：JSON

#### 8.2.2 参数信息详情

| 序号      | 输入参数          | 类型            | 可为空 | 备注                                                           |
|-----------|-------------------|-----------------|--------|----------------------------------------------------------------|
| 1         | nodeId            | int             | 否     | 网络id                                                         |
| 2         | beginDate         | LocalDateTime   | 是     | 显示时间（开始） yyyy-MM-dd'T'HH:mm:ss.SSS 2019-03-13T00:00:00 |
| 3         | endDate           | LocalDateTime   | 是     | 显示时间（结束）                                               |
| 4         | contrastBeginDate | LocalDateTime   | 是     | 对比时间（开始）                                               |
| 5         | contrastEndDate   | LocalDateTime   | 是     | 对比时间（结束）                                               |
| 6         | gap               | Int             | 是     | 数据粒度                                                       |
| 序号      | 输出参数          | 类型            |        | 备注                                                           |
| 1         | code              | int             | 否     | 返回码                                                         |
| 2         | message           | String          | 否     | 描述信息                                                       |
| 3         | data              | Array           | 否     | 返回信息列表                                                   |
| 3.1       |                   | Oject           |        | 返回信息实体                                                   |
| 3.1.1     | metricType        | String          | 否     | 测量类型: cpu、memory、disk、txbps、rxbps                      |
| 3.1.2     | data              | Oject           | 否     |                                                                |
| 3.1.2.1   | lineDataList      | Oject           | 否     |                                                                |
| 3.1.2.1.1 | timestampList     | List\<String\>  | 否     | 时间戳列表                                                     |
| 3.1.2.1.2 | valueList         | List\<Integer\> | 否     | 值列表                                                         |
| 3.1.2.2   | contrastDataList  | Oject           | 否     |                                                                |
| 3.1.2.2.1 | timestampList     | List\<String\>  | 否     | 时间戳列表                                                     |
| 3.1.2.2.2 | valueList         | List\<Integer\> | 否     | 值列表                                                         |

#### 8.2.3 入参事例

http://127.0.0.1:3001/mgr/webase-node-mgr/performance/ratio/500001?gap=1&beginDate=2019-03-15T00:00:00&endDate=2019-03-15T15:26:55&contrastBeginDate=2019-03-15T00:00:00&contrastEndDate=2019-03-15T15:26:55

#### 8.2.4 出参示例

**a)、成功：**

{

"code": 0,

"message": "success",

"data": [

{

"metricType": "txbps",

"data": {

"lineDataList": {

"timestampList": [

1552406401042,

1552406701001

],

"valueList": [

12.24,

54.48

]

},

"contrastDataList": {

"timestampList": [

1552320005000,

1552320301001

],

"valueList": [

22.24,

24.48

]

}

}

},

{

"metricType": "cpu",

"data": {

"lineDataList": {

"timestampList": null,

"valueList": [

118457,

157604

]

},

"contrastDataList": {

"timestampList": null,

"valueList": [

null,

33298

]

}

}

}

]

}

**b)、失败：**

｛

“code“: 102000:,

”message”: " system exception",

“data”:{}

｝

**9 节点日志模块**

### 9.1 获取节点日志列表

#### 9.1.1 传输协议规范

a)、网络传输协议：使用HTTP协议

b)、请求地址：
/nodeLog/nodeLogList/{networkId}/{nodeId}/{pageNumber}/{pageSize}?startTime={startTime}&endTime={endTime}

c)、请求方式：GET

d)、返回格式：JSON

#### 9.1.2 参数信息详情

| 序号  | 输入参数   | 类型          | 可为空 | 备注                       |
|-------|------------|---------------|--------|----------------------------|
| 1     | networkId  | int           | 否     | 所属网络编号               |
| 2     | nodeId     | int           | 否     | 节点id                     |
| 3     | pageNumber | int           | 否     | 页码                       |
| 4     | pageSize   | int           | 否     | 每页记录数                 |
| 5     | startTime  | LocalDateTime | 是     | 开始时间                   |
| 6     | endTime    | LocalDateTime | 是     | 结束时间                   |
| 序号  | 输出参数   | 类型          |        | 备注                       |
| 1     | code       | Int           | 否     | 返回码，0：成功 其它：失败 |
| 2     | message    | String        | 否     | 描述                       |
| 3     | totalCount | Int           | 否     | 总记录数                   |
| 4     | data       | List          | 是     | 日志列表                   |
| 4.1   |            | Object        |        | 日志信息对象               |
| 4.1.1 | logId      | Int           | 否     | 日志编号                   |
| 4.1.2 | nodeId     | Int           | 否     | 所属节点                   |
| 4.1.3 | fileName   | String        | 否     | 文件名                     |
| 4.1.4 | logTime    | LocalDateTime | 否     | 日志发生时间               |
| 4.1.5 | rowNumber  | Int           | 否     | 日志在文件中的行数         |
| 4.1.6 | logMsg     | String        | 否     | 日志内容                   |
| 4.1.7 | logStatus  | Int           | 否     | 日志状态                   |
| 4.1.8 | createTime | LocalDateTime | 否     | 插入数据库表时间           |
| 4.1.9 | modifyTime | LocalDateTime | 否     | 记录更改时间               |

#### 9.1.3 入参事例

http://127.0.0.1:4001/mgr/webcaf-node-mgr/nodeLog/nodeLogList/300001/500001/1/10?startTime=2019-03-12+00:00:00&endTime=2019-03-13+00:00:00

#### 9.1.4 出参示例

**a)、成功：**

{

"code": 0,

"message": "success",

"data": [

{

"logId":123,

"nodeId":237,

"fileName":"sdfsfs",

"logTime":"2019-03-13 00:00:00",

"rowNumber":237,

"logMsg":"3233esfsadfawefa",

"logStatus":1,

"createTime":"2019-03-13 00:00:00",

"modifyTime":"2019-03-13 00:00:00"

}

]，

"totalCount": 1

}

**b)、失败：**

｛

“code“: 102000:,

”message”: " system exception",

“data”:{}｝

**10 审计相关模块**

### 10.1 获取用户交易监管信息列表

#### 10.1.1 传输协议规范

a)、网络传输协议：使用HTTP协议

b)、请求地址： /monitor/userList/{networkId}

c)、请求方式：GET

d)、返回格式：JSON

#### 10.1.2 参数信息详情

| 序号   | 输入参数         | 类型          | 可为空 | 备注                                          |
|--------|------------------|---------------|--------|-----------------------------------------------|
| 1      | networkId        | int           | 否     | 所属网络编号                                  |
| 序号   | 输出参数         | 类型          |        | 备注                                          |
| 1      | code             | Int           | 否     | 返回码，0：成功 其它：失败                    |
| 2      | message          | String        | 否     | 描述                                          |
| 3      | totalCount       | Int           | 否     | 总记录数                                      |
| 4      | data             | List          | 是     | 信息列表                                      |
| 4.1    |                  | Object        | 是     | 监管信息对象                                  |
| 4.1.1  | userName         | String        | 是     | 用户名称                                      |
| 4.1.2  | userType         | Int           | 是     | 用户类型(0-正常，1-异常)                      |
| 4.1.3  | networkId        | Int           | 是     | 所属网络                                      |
| 4.1.4  | contractName     | String        | 是     | 合约名称                                      |
| 4.1.5  | contractAddress  | String        | 是     | 合约地址                                      |
| 4.1.6  | interfaceName    | String        | 是     | 合约接口名                                    |
| 4.1.7  | transType        | Int           | 是     | 交易类型(0-合约部署，1-接口调用)              |
| 4.1.8  | transUnusualType | Int           | 是     | 交易异常类型 (0-正常，1-异常合约，2-异常接口) |
| 4.1.9  | transCount       | Int           | 是     | 交易量                                        |
| 4.1.10 | transHashs       | String        | 是     | 交易hashs(最多5个)                            |
| 4.1.11 | transHashLastest | String        | 是     | 最新交易hash                                  |
| 4.1.12 | createTime       | LocalDateTime | 是     | 落库时间                                      |
| 4.1.13 | modifyTime       | LocalDateTime | 是     | 修改时间                                      |

#### 10.1.3 入参事例

http://127.0.0.1:3001/mgr/webase-node-mgr/monitor/userList/300001

#### 10.1.4 出参示例

**a)、成功：**

{

"code": 0,

"message": "success",

"data": [

{

"userName": "SYSTEMUSER",

"userType": 0,

"networkId": null,

"contractName": null,

"contractAddress": null,

"interfaceName": null,

"transType": null,

"transUnusualType": null,

"transCount": null,

"transHashs": null,

"transHashLastest": null,

"createTime": null,

"modifyTime": null

},

{

"userName": "asdf",

"userType": 0,

"networkId": null,

"contractName": null,

"contractAddress": null,

"interfaceName": null,

"transType": null,

"transUnusualType": null,

"transCount": null,

"transHashs": null,

"transHashLastest": null,

"createTime": null,

"modifyTime": null

}

]

}

**b)、失败：**

｛

“code“: 102000:,

”message”: " system exception",

“data”:{}

｝

### 10.2 获取合约方法监管信息列表

#### 10.2.1 传输协议规范

a)、网络传输协议：使用HTTP协议

b)、请求地址： /monitor/interfaceList/{networkId}?userName={userName}

c)、请求方式：GET

d)、返回格式：JSON

#### 10.2.2 参数信息详情

| 序号   | 输入参数         | 类型          | 可为空 | 备注                                          |
|--------|------------------|---------------|--------|-----------------------------------------------|
| 1      | networkId        | int           | 否     | 所属网络编号                                  |
| 2      | userName         | String        | 是     | 用户名                                        |
| 序号   | 输出参数         | 类型          |        | 备注                                          |
| 1      | code             | Int           | 否     | 返回码，0：成功 其它：失败                    |
| 2      | message          | String        | 否     | 描述                                          |
| 3      | totalCount       | Int           | 否     | 总记录数                                      |
| 4      | data             | List          | 是     | 信息列表                                      |
| 4.1    |                  | Object        | 是     | 监管信息对象                                  |
| 4.1.1  | userName         | String        | 是     | 用户名称                                      |
| 4.1.2  | userType         | Int           | 是     | 用户类型(0-正常，1-异常)                      |
| 4.1.3  | networkId        | Int           | 是     | 所属网络                                      |
| 4.1.4  | contractName     | String        | 是     | 合约名称                                      |
| 4.1.5  | contractAddress  | String        | 是     | 合约地址                                      |
| 4.1.6  | interfaceName    | String        | 是     | 合约接口名                                    |
| 4.1.7  | transType        | Int           | 是     | 交易类型(0-合约部署，1-接口调用)              |
| 4.1.8  | transUnusualType | Int           | 是     | 交易异常类型 (0-正常，1-异常合约，2-异常接口) |
| 4.1.9  | transCount       | Int           | 是     | 交易量                                        |
| 4.1.10 | transHashs       | String        | 是     | 交易hashs(最多5个)                            |
| 4.1.11 | transHashLastest | String        | 是     | 最新交易hash                                  |
| 4.1.12 | createTime       | LocalDateTime | 是     | 落库时间                                      |
| 4.1.13 | modifyTime       | LocalDateTime | 是     | 修改时间                                      |

#### 10.2.3 入参事例

http://127.0.0.1:3001/mgr/webase-node-mgr/monitor/interfaceList/300001

#### 10.2.4 出参示例

**a)、成功：**

{

"code": 0,

"message": "success",

"data": [

{

"userName": "SYSTEMUSER",

"userType": 0,

"networkId": null,

"contractName": null,

"contractAddress": null,

"interfaceName": null,

"transType": null,

"transUnusualType": null,

"transCount": null,

"transHashs": null,

"transHashLastest": null,

"createTime": null,

"modifyTime": null

},

{

"userName": "asdf",

"userType": 0,

"networkId": null,

"contractName": null,

"contractAddress": null,

"interfaceName": null,

"transType": null,

"transUnusualType": null,

"transCount": null,

"transHashs": null,

"transHashLastest": null,

"createTime": null,

"modifyTime": null

}

]

}

**b)、失败：**

｛

“code“: 102000:,

”message”: " system exception",

“data”:{}

｝

### 10.3 获取交易hash监管信息列表

#### 10.3.1 传输协议规范

a)、网络传输协议：使用HTTP协议

b)、请求地址： /monitor/interfaceList/{networkId}

c)、请求方式：GET

d)、返回格式：JSON

#### 10.3.2 参数信息详情

| 序号    | 输入参数      | 类型           | 可为空 | 备注                       |
|---------|---------------|----------------|--------|----------------------------|
| 1       | networkId     | int            | 否     | 所属网络编号               |
| 2       | userName      | String         | 是     | 用户名                     |
| 3       | startDate     | String         |        | 开始时间                   |
| 4       | endDate       | String         |        | 结束时间                   |
| 5       | interfaceName | String         |        | 接口名称                   |
| 序号    | 输出参数      | 类型           |        | 备注                       |
| 1       | code          | Int            | 否     | 返回码，0：成功 其它：失败 |
| 2       | message       | String         | 否     | 描述                       |
| 3       | data          | Object         | 否     | 返回结果实体               |
| 3.1     | networkId     | Int            | 否     | 所属网络编号               |
| 3.2     | userName      | String         | 否     | 用户名                     |
| 3.3     | interfaceName | String         | 否     | 接口名                     |
| 3.4     | totalCount    | Int            | 否     | 总记录数                   |
| 3.5     | transInfoList | List\<Object\> | 是     | 交易信息列表               |
| 3.5.1   |               | Object         | 是     | 交易信息实体               |
| 3.5.1.1 | transCount    | Int            | 是     | 交易记录数                 |
| 3.5.1.2 | time          | LcalDateTime   | 是     | 时间                       |

#### 10.3.3 入参事例

http://127.0.0.1:3001/mgr/webase-node-mgr/monitor/transList/300001?userName=0x5d97f8d41638a7b1b669b70b307bab6d49df8e2c&interfaceName=0x4ed3885e

#### 10.3.4 出参示例

**a)、成功：**

"code": 0,

"message": "success",

"data": {

"networkId": 300001,

"userName": "0x5d97f8d41638a7b1b669b70b307bab6d49df8e2c",

"interfaceName": "0x4ed3885e",

"totalCount": 1,

"transInfoList": [

{

"transCount": 1,

"time": "2019-03-13 15:41:56"

}

]

}

}

**b)、失败：**

｛

“code“: 102000:,

”message”: " system exception",

“data”:{}

｝

### 10.4 获取异常用户信息列表

#### 10.4.1 传输协议规范

a)、网络传输协议：使用HTTP协议

b)、请求地址：

/unusualUserList/{networkId}/{pageNumber}/{pageSize}?userName={userName}

c)、请求方式：GET

d)、返回格式：JSON

#### 10.4.2 参数信息详情

| 序号  | 输入参数   | 类型          | 可为空 | 备注                       |
|-------|------------|---------------|--------|----------------------------|
| 1     | networkId  | int           | 否     | 所属网络编号               |
| 2     | userName   | String        | 是     | 用户名                     |
| 3     | pageNumber | int           | 否     | 当前页码                   |
| 4     | pageSize   | int           | 否     | 页面大小                   |
| 序号  | 输出参数   | 类型          |        | 备注                       |
| 1     | code       | Int           | 否     | 返回码，0：成功 其它：失败 |
| 2     | message    | String        | 否     | 描述                       |
| 3     | totalCount | Int           | 否     | 总记录数                   |
| 4     | data       | List          | 否     | 返回信息列表               |
| 4.1   |            | object        | 是     | 返回信息实体               |
| 4.1.1 | userName   | String        | 是     | 用户名                     |
| 4.1.2 | transCount | int           | 是     | 交易数                     |
| 4.1.3 | hashs      | String        | 是     | 交易hash                   |
| 4.1.4 | time       | LocalDateTime | 是     | 时间                       |

#### 10.4.3 入参事例

http://127.0.0.1:3001/mgr/webase-node-mgr/monitor/unusualUserList/300001/1/10?userName=

#### 10.4.4 出参示例

**a)、成功：**

{

"code": 0,

"message": "success",

"data": [

{

"userName": "0x08b52f85638a925929cf62a3ac77c67415012c24",

"transCount": 1,

"hashs": "0x43b50faa3f007c22cf5dd710c3561c5cde516e01a55b5b4acffd7d94cf61fc57",

"time": "2019-03-13 22:28:29"

}

],

"totalCount": 1

}

**b)、失败：**

｛

“code“: 102000:,

”message”: " system exception",

“data”:{}

｝

### 10.5 获取异常合约信息列表

#### 10.5.1 传输协议规范

a)、网络传输协议：使用HTTP协议

b)、请求地址：

/unusualContractList/{networkId}/{pageNumber}/{pageSize}?contractAddress={contractAddress}

c)、请求方式：GET

d)、返回格式：JSON

#### 10.5.2 参数信息详情

| 序号  | 输入参数        | 类型          | 可为空 | 备注                       |
|-------|-----------------|---------------|--------|----------------------------|
| 1     | networkId       | int           | 否     | 所属网络编号               |
| 2     | contractAddress | String        | 是     | 合约地址                   |
| 3     | pageNumber      | int           | 否     | 当前页码                   |
| 4     | pageSize        | int           | 否     | 页面大小                   |
| 序号  | 输出参数        | 类型          |        | 备注                       |
| 1     | code            | Int           | 否     | 返回码，0：成功 其它：失败 |
| 2     | message         | String        | 否     | 描述                       |
| 3     | totalCount      | Int           | 否     | 总记录数                   |
| 4     | data            | List          | 否     | 返回信息列表               |
| 4.1   |                 | object        | 是     | 返回信息实体               |
| 4.1.1 | contractName    | String        | 是     | 合约名称                   |
| 4.1.2 | contractAddress | String        | 是     | 合约地址                   |
| 4.1.3 | transCount      | int           | 是     | 交易数                     |
| 4.1.4 | hashs           | String        | 是     | 交易hash                   |
| 4.1.5 | time            | LocalDateTime | 是     | 时间                       |

#### 10.5.3 入参事例

http://127.0.0.1:3001/mgr/webase-node-mgr/monitor/unusualContractList/300001/1/10?contractAddress=

#### 10.5.4 出参示例

**a)、成功：**

{

"code": 0,

"message": "success",

"data": [

{

"contractName": "0x00000000",

"contractAddress": "0x0000000000000000000000000000000000000000",

"transCount": 3,

"hashs": "0xc87e306db85740895369cc2a849984fe544a6e9b0ecdbd2d898fc0756a02a4ce",

"time": "2019-03-13 15:41:56"

}

],

"totalCount":

}

**b)、失败：**

｛

“code“: 102000:,

”message”: " system exception",

“data”:{}

｝

**11 网络信息相关模块**

### 11.1 获取网络概况

#### 11.1.1 传输协议规范

a)、网络传输协议：使用HTTP协议

b)、请求地址： /network/general/{networkId}

c)、请求方式：GET

d)、返回格式：JSON

#### 11.1.2 参数信息详情

| 序号 | 输入参数         | 类型   | 可为空 | 备注                       |
|------|------------------|--------|--------|----------------------------|
| 1    | networkId        | int    | 否     | 网络id                     |
| 序号 | 输出参数         | 类型   |        | 备注                       |
| 1    | code             | Int    | 否     | 返回码，0：成功 其它：失败 |
| 2    | message          | String | 否     | 描述                       |
| 3    | data             | object | 否     | 返回信息实体               |
| 3.1  | networkId        | int    | 否     | 网络id                     |
| 3.2  | orgCount         | int    | 否     | 组织数量                   |
| 3.3  | nodeCount        | int    | 否     | 节点数量                   |
| 3.4  | contractCount    | int    | 否     | 已部署智能合约数量         |
| 3.5  | transactionCount | int    | 否     | 交易数量                   |
| 3.6  | latestBlock      | int    | 否     | 当前块高                   |

#### 11.1.3 入参事例

http://localhost:8080/fisco-bcos-node-mgr/network/300001

#### 11.1.4 出参示例

**a)、成功：**

{

"code": 0,

"data": {

"latestBlock": 7156,

"contractCount": 0,

"networkId": "300001",

"nodeCount": 2,

"orgCount": 23,

"transactionCount": 7131

},

"message": "Success"

}

**b)、失败：**

｛

“code“: 102000:,

”message”: " system exception",

“data”:{}

｝

### 11.2 获取所有网络列表

#### 11.2.1 传输协议规范

a)、网络传输协议：使用HTTP协议

b)、请求地址： /network/all

c)、请求方式：GET

d)、返回格式：JSON

#### 11.2.2 参数信息详情

| 序号  | 输入参数      | 类型          | 可为空 | 备注                       |
|-------|---------------|---------------|--------|----------------------------|
|       |               |               |        |                            |
| 序号  | 输出参数      | 类型          |        | 备注                       |
| 1     | code          | Int           | 否     | 返回码，0：成功 其它：失败 |
| 2     | message       | String        | 否     | 描述                       |
| 3     | totalCount    | Int           | 否     | 总记录数                   |
| 4     | data          | List          | 否     | 组织列表                   |
| 4.1   |               | Object        |        | 组织信息对象               |
| 4.1.1 | networkId     | int           | 否     | 网络编号                   |
| 4.1.2 | networkName   | String        | 否     | 网络名称                   |
| 4.1.3 | networkStatus | int           | 否     | 状态（1-正常 2-停用）      |
| 4.1.4 | latestBlock   | BigInteger    | 否     | 最新块高                   |
| 4.1.5 | transCount    | BigInteger    | 否     | 交易量                     |
| 4.1.6 | createTime    | LocalDateTime | 否     | 落库时间                   |
| 4.1.7 | modifyTime    | LocalDateTime | 否     | 修改时间                   |

#### 11.2.3 入参事例

http://127.0.0.1:3001/mgr/webase-node-mgr/network/all

#### 11.2.4 出参示例

**a)、成功：**

{

"code": 0,

"message": "success",

"data": [

{

"networkId": 300001,

"networkName": "network1",

"networkStatus": 1,

"latestBlock": 133,

"transCount": 133,

"createTime": "2019-02-14 17:33:50",

"modifyTime": "2019-03-15 09:36:17"

}

],

"totalCount": 1

}

**b)、失败：**

｛

“code“: 102000:,

”message”: " system exception",

“data”:{}

｝

### 11.3 查询每日交易数据

#### 11.3.1 传输协议规范

a)、网络传输协议：使用HTTP协议

b)、请求地址：/network/transDaily/{networkId}

c)、请求方式：GET

d)、返回格式：JSON

#### 11.3.2 参数信息详情

| 序号 | 输入参数   | 类型   | 可为空 | 备注                       |
|------|------------|--------|--------|----------------------------|
| 1    | networkId  | int    | 否     | 网络id                     |
| 序号 | 输出参数   | 类型   |        | 备注                       |
| 1    | code       | Int    | 否     | 返回码，0：成功 其它：失败 |
| 2    | message    | String | 否     | 描述                       |
| 3    | data       | list   | 否     | 返回信息列表               |
| 3.1  |            | object |        | 返回信息实体               |
| 4.1  | day        | string | 否     | 日期YYYY-MM-DD             |
| 4.2  | networkId  | int    | 否     | 网络编号                   |
| 4.3  | transCount | int    | 否     | 交易数量                   |

#### 11.3.3 入参事例

http://localhost:8080/fisco-bcos-node-mgr/network/transDaily/300001

#### 11.3.4 出参示例

**a)、成功：**

{

"code": 0,

"data": [

{

"day": "2018-11-21",

"networkId": "300001",

" transCount": 12561

},{

"day": "2018-11-22",

"networkId": "300001",

"transCount": 1251

}

]

,

"message": "Success"

}

**b)、失败：**

｛

“code“: 102000:,

”message”: " system exception",

“data”:{}

｝

**12 节点管理模块**

### 12.1 查询节点列表

#### 12.1.1 传输协议规范

a)、网络传输协议：使用HTTP协议

b)、请求地址:

/node/nodeList/{networkId}/{pageNumber}/{pageSize}?nodeName={nodeName}

c)、请求方式：GET

d)、返回格式：JSON

#### 12.1.2 参数信息详情

| 序号   | 输入参数    | 类型          | 可为空 | 备注                                       |
|--------|-------------|---------------|--------|--------------------------------------------|
| 1      | networkId   | int           | 否     | 网络id                                     |
| 2      | pageSize    | Int           | 否     | 每页记录数                                 |
| 3      | pageNumber  | Int           | 否     | 当前页码                                   |
| 4      | nodeName    | String        | 是     | 节点名称                                   |
|        | 输出参数    | 类型          |        | 备注                                       |
| 1      | code        | Int           | 否     | 返回码，0：成功 其它：失败                 |
| 2      | message     | String        | 否     | 描述                                       |
| 3      | totalCount  | Int           | 否     | 总记录数                                   |
| 4      | data        | List          | 是     | 节点列表                                   |
| 4.1    |             | Object        |        | 节点信息对象                               |
| 4.1.1  | nodeId      | int           | 否     | 节点编号                                   |
| 4.1.2  | nodeName    | string        | 否     | 节点名称                                   |
| 4.1.3  | networkId   | int           | 否     | 所属网络编号                               |
| 4.1.4  | orgId       | int           | 否     | 所属组织编号                               |
| 4.1.5  | orgName     |               |        |                                            |
| 4.1.6  | nodeActive  | int           | 否     | 状态                                       |
| 4.1.7  | nodeIp      | string        | 否     | 节点ip                                     |
| 4.1.8  | P2pPort     | int           | 否     | 节点p2p端口                                |
| 4.1.9  | rpcPort     | int           | 否     | 节点rpc端口                                |
| 4.1.10 | channelPort | int           | 否     | 链上链下端口                               |
| 4.1.11 | frontPort   | int           | 否     | 前置端口                                   |
| 4.1.12 | chainIndex  | int           | 是     | 链上索引                                   |
| 4.1.13 | nodeType    | int           | 否     | 节点类型（1-本组织 2-其他节点） 默认本节点 |
| 4.1.14 | description | String        | 否     | 备注                                       |
| 4.1.15 | blockNumber | BigInteger    | 否     | 节点块高                                   |
| 4.1.16 | pbftView    | BigInteger    | 否     | Pbft view                                  |
| 4.1.17 | createTime  | LocalDateTime | 否     | 落库时间                                   |
| 4.1.18 | modifyTime  | LocalDateTime | 否     | 修改时间                                   |

#### 12.1.3 入参事例

http://127.0.0.1:3001/mgr/webase-node-mgr/node/nodeList/300001/1/10?nodeName=

#### 12.1.4 出参示例

**a)、成功：**

{

"code": 0,

"message": "success",

"data": [

{

"nodeId": 500001,

"nodeName": "127.0.0.1_10303",

"networkId": 300001,

"orgId": 600001,

"orgName": "WeBank",

"nodeIp": "127.0.0.1",

"p2pPort": 10303,

"rpcPort": 1545,

"channelPort": 1821,

"frontPort": 8181,

"chainIndex": 0,

"nodeType": 1,

"description": null,

"blockNumber": 133,

"pbftView": 5852,

"nodeActive": 1,

"createTime": "2019-02-14 17:47:00",

"modifyTime": "2019-03-15 11:14:29"

}

],

"totalCount": 1

}

**b)、失败：**

｛

“code“: 102000:,

”message”: "system exception",

“data”:{}

｝

### 12.2 查询节点信息(未使用)

#### 12.2.1 传输协议规范

a)、网络传输协议：使用HTTP协议

b)、请求地址: /node/nodeInfo/{networkId}?nodeType={nodeType}

c)、请求方式：GET

d)、返回格式：JSON

#### 12.2.2 参数信息详情

| 序号 | 输入参数    | 类型          | 可为空 | 备注                                       |
|------|-------------|---------------|--------|--------------------------------------------|
| 1    | networkId   | int           | 否     | 网络id                                     |
| 2    | nodeType    | Int           | 是     | 节点类型（1-本组织 2-其他节点）            |
|      | 输出参数    | 类型          |        | 备注                                       |
| 1    | code        | Int           | 否     | 返回码，0：成功 其它：失败                 |
| 2    | message     | String        | 否     | 描述                                       |
| 3    |             | Object        |        | 节点信息对象                               |
| 3.1  | nodeId      | int           | 否     | 节点编号                                   |
| 3.2  | nodeName    | string        | 否     | 节点名称                                   |
| 3.3  | networkId   | int           | 否     | 所属网络编号                               |
| 3.4  | orgId       | int           | 否     | 所属组织编号                               |
| 3.5  | orgName     |               |        |                                            |
| 3.6  | nodeActive  | int           | 否     | 状态                                       |
| 3.7  | nodeIp      | string        | 否     | 节点ip                                     |
| 3.8  | P2pPort     | int           | 否     | 节点p2p端口                                |
| 3.9  | rpcPort     | int           | 否     | 节点rpc端口                                |
| 3.10 | channelPort | int           | 否     | 链上链下端口                               |
| 3.11 | frontPort   | int           | 否     | 前置端口                                   |
| 3.12 | chainIndex  | int           | 是     | 链上索引                                   |
| 3.13 | nodeType    | int           | 否     | 节点类型（1-本组织 2-其他节点） 默认本节点 |
| 3.14 | description | String        | 否     | 备注                                       |
| 3.15 | blockNumber | BigInteger    | 否     | 节点块高                                   |
| 3.16 | pbftView    | BigInteger    | 否     | Pbft view                                  |
| 3.17 | createTime  | LocalDateTime | 否     | 落库时间                                   |
| 3.18 | modifyTime  | LocalDateTime | 否     | 修改时间                                   |

#### 12.2.3 入参事例

http://127.0.0.1:3001/mgr/webase-node-mgr/node/nodeInfo/{networkId}?nodeType=1

#### 12.2.4 出参示例

**a)、成功：**

{

"code": 0,

"message": "success",

"data":

{

"nodeId": 500001,

"nodeName": "127.0.0.1_10303",

"networkId": 300001,

"orgId": 600001,

"orgName": "WeBank",

"nodeIp": "127.0.0.1",

"p2pPort": 10303,

"rpcPort": 1545,

"channelPort": 1821,

"frontPort": 8181,

"chainIndex": 0,

"nodeType": 1,

"description": null,

"blockNumber": 133,

"pbftView": 5852,

"nodeActive": 1,

"createTime": "2019-02-14 17:47:00",

"modifyTime": "2019-03-15 11:14:29"

}

}

**b)、失败：**

｛

“code“: 102000:,

”message”: "system exception",

“data”:{}

｝

### 12.3 新增节点

#### 12.3.1 传输协议规范 

a)、网络传输协议：使用HTTP协议

b)、请求地址： /node/nodeInfo

c)、请求方式：POST

d)、请求头：Content-type: application/json

e)、返回格式：JSON

#### 12.3.2 参数信息详情

| 序号 | 输入参数    | 类型          | 可为空 | 备注                                       |
|------|-------------|---------------|--------|--------------------------------------------|
| 1    | networkId   | int           | 否     | 所属网络编号                               |
| 2    | nodeIp      | string        | 否     | 节点ip                                     |
| 3    | nodeType    | int           | 否     | 节点类型（1本节点，2其他节点）             |
| 4    | frontPort   | int           | 否     | 前置服务端口                               |
| 序号 | 输出参数    | 类型          |        | 备注                                       |
| 1    | code        | Int           | 否     | 返回码，0：成功 其它：失败                 |
| 2    | message     | String        | 否     | 描述                                       |
| 3    |             | Object        |        | 节点信息对象                               |
| 3.1  | nodeId      | int           | 否     | 节点编号                                   |
| 3.2  | nodeName    | string        | 否     | 节点名称                                   |
| 3.3  | networkId   | int           | 否     | 所属网络编号                               |
| 3.4  | orgId       | int           | 否     | 所属组织编号                               |
| 3.5  | orgName     |               |        |                                            |
| 3.6  | nodeActive  | int           | 否     | 状态                                       |
| 3.7  | nodeIp      | string        | 否     | 节点ip                                     |
| 3.8  | P2pPort     | int           | 否     | 节点p2p端口                                |
| 3.9  | rpcPort     | int           | 否     | 节点rpc端口                                |
| 3.10 | channelPort | int           | 否     | 链上链下端口                               |
| 3.11 | frontPort   | int           | 否     | 前置端口                                   |
| 3.12 | chainIndex  | int           | 是     | 链上索引                                   |
| 3.13 | nodeType    | int           | 否     | 节点类型（1-本组织 2-其他节点） 默认本节点 |
| 3.14 | description | String        | 否     | 备注                                       |
| 3.15 | blockNumber | BigInteger    | 否     | 节点块高                                   |
| 3.16 | pbftView    | BigInteger    | 否     | Pbft view                                  |
| 3.17 | createTime  | LocalDateTime | 否     | 落库时间                                   |
| 3.18 | modifyTime  | LocalDateTime | 否     | 修改时间                                   |

#### 12.3.3 入参事例

<http://localhost:8080/fisco-bcos-node-mgr/node>/nodeInfo

{

"networkId": "300001",

"nodeIp": "127.0.0.1",

"nodeType": "1",

"frontPort": "8081"

}

#### 12.3.4 出参示例

**a)、成功：**

{

"code": 0,

"message": "success",

"data":

{

"nodeId": 500001,

"nodeName": "127.0.0.1_10303",

"networkId": 300001,

"orgId": 600001,

"orgName": "WeBank",

"nodeIp": "127.0.0.1",

"p2pPort": 10303,

"rpcPort": 1545,

"channelPort": 1821,

"frontPort": 8181,

"chainIndex": 0,

"nodeType": 1,

"description": null,

"blockNumber": 133,

"pbftView": 5852,

"nodeActive": 1,

"createTime": "2019-02-14 17:47:00",

"modifyTime": "2019-03-15 11:14:29"

}

}

**b)、失败：**

｛

“code“: 102000:,

”message”: " system exception",

“data”:{}

｝

### 12.4 删除节点信息（未使用）

#### 12.4.1 传输协议规范 

a)、网络传输协议：使用HTTP协议

b)、请求地址： /node/nodeInfo/{nodeId}

c)、请求方式：DELETE

d)、请求头：Content-type: application/json

e)、返回格式：JSON

#### 12.4.2 参数信息详情

| 序号 | 输入参数 | 类型   | 可为空 | 备注                       |
|------|----------|--------|--------|----------------------------|
| 1    | nodeId   | int    | 否     | 节点编号                   |
| 序号 | 输出参数 | 类型   |        | 备注                       |
| 1    | code     | Int    | 否     | 返回码，0：成功 其它：失败 |
| 2    | message  | String | 否     | 描述                       |
| 3    | data     | object | 是     | 返回信息实体（空）         |

#### 12.4.3 入参事例

*http://localhost:8080/fisco-bcos-node-mgr/node/nodeInfo/2541*

#### 12.4.4 出参示例

**a)、成功：**

{

"code": 0,

"data": {

"orgId": 10200024

},

"message": "Success"

}

**b)、失败：**

｛

“code“: 102000:,

”message”: " system exception",

“data”:{}

｝

13 角色管理模块
===============

### 13.1 查询角色列表

### 13.1.1 传输协议规范

a)、网络传输协议：使用HTTP协议

b)、请求地址：role/roleList

c)、请求方式：GET

d)、返回格式：JSON

### 13.1.2 参数信息详情

| 序号  | 输入参数    | 类型          | 可为空 | 备注                       |
|-------|-------------|---------------|--------|----------------------------|
| 1     | roleId      | int           | 否     | 角色id                     |
| 2     | roleName    | String        | 是     | 角色名称                   |
| 3     | pageSize    | int           | 否     | 每页记录数                 |
| 4     | pageNumber  | int           | 否     | 当前页码                   |
| 序号  | 输出参数    | 类型          |        | 备注                       |
| 1     | code        | Int           | 否     | 返回码，0：成功 其它：失败 |
| 2     | message     | String        | 否     | 描述                       |
| 3     | totalCount  | Int           | 否     | 总记录数                   |
| 4     | data        | List          | 否     | 组织列表                   |
| 4.1   |             | Object        |        | 组织信息对象               |
| 4.1.1 | roleId      | Int           | 否     | 角色编号                   |
| 4.1.2 | roleName    | String        | 否     | 角色名称                   |
| 4.1.3 | roleNameZh  | String        | 否     | 角色中文名称               |
| 4.1.4 | roleStatus  | Int           | 否     | 状态（1-正常2-无效） 默认1 |
| 4.1.5 | description | String        | 否     | 备注                       |
| 4.1.6 | createTime  | LocalDateTime | 否     | 创建时间                   |
| 4.1.7 | modifyTime  | LocalDateTime | 否     | 修改时间                   |

### 13.1.3 入参事例

http://127.0.0.1:3001/mgr/webase-node-mgr/role/roleList?networkId=300001&pageNumber=&pageSize=&roleId=&roleName=

### 13.1.4 出参示例

**a)、成功：**

{

"code": 0,

"message": "success",

"data": [

{

"roleId": 100000,

"roleName": "admin",

"roleNameZh": "管理员",

"roleStatus": 1,

"description": null,

"createTime": "2019-02-14 17:33:50",

"modifyTime": "2019-02-14 17:33:50"

},

{

"roleId": 100001,

"roleName": "visitor",

"roleNameZh": "访客",

"roleStatus": 1,

"description": null,

"createTime": "2019-02-14 17:33:50",

"modifyTime": "2019-02-14 17:33:50"

}

],

"totalCount": 2

}

**b)、失败：**

｛

“code“: 102000:,

”message”: "system exception",

“data”:{}

｝

14 用户管理模块
===============

### 14.1 新增私钥用户

#### 14.1.1 传输协议规范 

a)、网络传输协议：使用HTTP协议

b)、请求地址： /user/userInfo

c)、请求方式：POST

d)、请求头：Content-type: application/json

e)、返回格式：JSON

#### 14.1.2 参数信息详情

| 序号 | 输入参数    | 类型          | 可为空 | 备注                               |
|------|-------------|---------------|--------|------------------------------------|
| 1    | userName    | string        | 否     | 用户名称                           |
| 2    | description | string        | 是     | 备注                               |
| 3    | networkId   | Int           | 否     | 所属网络                           |
| 序号 | 输出参数    | 类型          |        | 备注                               |
| 1    | code        | Int           | 否     | 返回码，0：成功 其它：失败         |
| 2    | message     | String        | 否     | 描述                               |
| 3    | data        | object        | 是     | 返回信息实体（成功时不为空）       |
| 3.1  | userId      | int           | 否     | 用户编号                           |
|      | userName    | string        | 否     | 用户名称                           |
|      | networkId   | int           | 否     | 所属网络编号                       |
|      | orgId       | int           | 否     | 所属组织编号                       |
|      | description | String        | 是     | 备注                               |
|      | userStatus  | int           | 否     | 状态（1-正常 2-停用） 默认1        |
|      | publicKey   | String        | 否     | 公钥信息                           |
|      | chainIndex  | int           | 是     | 保存在链上的索引位置               |
|      | userType    | int           | 否     | 用户类型（1-普通用户 2-系统用户）  |
|      | address     | String        | 是     | 在链上位置的hash                   |
|      | hasPk       | Int           | 否     | 是否拥有私钥信息(1-拥有，2-不拥有) |
|      | createTime  | LocalDateTime | 否     | 创建时间                           |
|      | modifyTime  | LocalDateTime | 否     | 修改时间                           |

#### 14.1.3 入参事例

<http://localhost:8080/fisco-bcos-node-mgr/user/userInfo>

{

"networkId": "300001",

"description": "密钥拥有者",

"userName": "user1"

}

#### 14.1.4 出参示例

**a)、成功：**

{

"code": 0,

"message": "success",

"data": {

"userId": 700007,

"userName": "asdfvw",

"networkId": 300001,

"orgId": 600001,

"publicKey":
"0x4189fdacff55fb99172e015e1adb96dc77b0c62e3b8fead7b392ac3634eab01bd3dffdae1619b1a41cc360777bee6682fcc975238aabf144fbf610a3057fd4b5",

"userStatus": 1,

"chainIndex": null,

"userType": 1,

"address": "0x40ec3c20b5178401ae14ad8ce9c9f94fa5ebb86a",

"hasPk": 1,

"description": "sda",

"createTime": "2019-03-15 18:00:27",

"modifyTime": "2019-03-15 18:00:27"

}

}

**b)、失败：**

｛

“code“: 102000:,

”message”: " system exception",

“data”:{}

｝

### 14.2 绑定公钥用户

#### 14.2.1 传输协议规范 

a)、网络传输协议：使用HTTP协议

b)、请求地址： /user/bind

c)、请求方式：POST

d)、请求头：Content-type: application/json

e)、返回格式：JSON

#### 14.2.2 参数信息详情

| 序号 | 输入参数    | 类型          | 可为空 | 备注                               |
|------|-------------|---------------|--------|------------------------------------|
| 1    | userName    | string        | 否     | 用户名称                           |
| 2    | description | string        | 是     | 备注                               |
| 3    | networkId   | Int           | 否     | 所属网络                           |
| 4    |             |               |        |                                    |
| 序号 | 输出参数    | 类型          |        | 备注                               |
| 1    | code        | Int           | 否     | 返回码，0：成功 其它：失败         |
| 2    | message     | String        | 否     | 描述                               |
| 3    | data        | object        | 是     | 返回信息实体（成功时不为空）       |
| 3.1  | userId      | int           | 否     | 用户编号                           |
| 3.2  | userName    | string        | 否     | 用户名称                           |
| 3.3  | networkId   | int           | 否     | 所属网络编号                       |
| 3.4  | orgId       | int           | 否     | 所属组织编号                       |
| 3.5  | description | String        | 是     | 备注                               |
| 3.6  | userStatus  | int           | 否     | 状态（1-正常 2-停用） 默认1        |
| 3.7  | publicKey   | String        | 否     | 公钥信息                           |
| 3.8  | chainIndex  | int           | 是     | 保存在链上的索引位置               |
| 3.9  | userType    | int           | 否     | 用户类型（1-普通用户 2-系统用户）  |
| 3.10 | address     | String        | 是     | 在链上位置的hash                   |
| 3.11 | hasPk       | Int           | 否     | 是否拥有私钥信息(1-拥有，2-不拥有) |
| 3.12 | createTime  | LocalDateTime | 否     | 创建时间                           |
| 3.13 | modifyTime  | LocalDateTime | 否     | 修改时间                           |

#### 14.2.3 入参事例

<http://localhost:8080/fisco-bcos-node-mgr/user/userInfo>

{

"userName": "sdfasd",

"publicKey":
"0x4189fdacff55fb99172e015e1adb96dc77b0c62e3b8fead7b392ac3634eab01bd3dffdae1619b1a41cc360777bee6682fcc9752d8aabf144fbf610a3057fd4b5",

"networkId": "300001",

"description": "sdfa"

}

#### 14.2.4 出参示例

**a)、成功：**

{

"code": 0,

"message": "success",

"data": {

"userId": 700007,

"userName": "asdfvw",

"networkId": 300001,

"orgId": 600001,

"publicKey":
"0x4189fdacff55fb99172e015e1adb96dc77b0c62e3b8fead7b392ac3634eab01bd3dffdae1619b1a41cc360777bee6682fcc9752d8aabf144fbf610a3057fd4b5",

"userStatus": 1,

"chainIndex": null,

"userType": 1,

"address": "0x40ec3c20b5178401ae14ad8ce9c9f94fa5ebb86a",

"hasPk": 1,

"description": "sda",

"createTime": "2019-03-15 18:00:27",

"modifyTime": "2019-03-15 18:00:27"

}

}

**b)、失败：**

｛

“code“: 102000:,

”message”: " system exception",

“data”:{}

｝

### 14.3 修改用户备注

#### 14.3.1 传输协议规范

a)、网络传输协议：使用HTTP协议

b)、请求地址：/user/userInfo

c)、请求方式：PUT

d)、请求头：Content-type: application/json

e)、返回格式：JSON

#### 14.3.2 参数信息详情

| 序号 | 输入参数    | 类型          | 可为空 | 备注                               |
|------|-------------|---------------|--------|------------------------------------|
| 1    | userId      | int           | 否     | 用户编号                           |
| 2    | description | String        | 是     | 备注                               |
| 序号 | 输出参数    | 类型          |        | 备注                               |
| 1    | code        | Int           | 否     | 返回码，0：成功 其它：失败         |
| 2    | message     | String        | 否     | 描述                               |
| 3    | data        | object        | 是     | 返回信息实体（成功时不为空）       |
| 3.1  | userId      | int           | 否     | 用户编号                           |
| 3.2  | userName    | string        | 否     | 用户名称                           |
| 3.3  | networkId   | int           | 否     | 所属网络编号                       |
| 3.4  | orgId       | int           | 否     | 所属组织编号                       |
| 3.5  | description | String        | 是     | 备注                               |
| 3.6  | userStatus  | int           | 否     | 状态（1-正常 2-停用） 默认1        |
| 3.7  | publicKey   | String        | 否     | 公钥信息                           |
| 3.8  | chainIndex  | int           | 是     | 保存在链上的索引位置               |
| 3.9  | userType    | int           | 否     | 用户类型（1-普通用户 2-系统用户）  |
| 3.10 | address     | String        | 是     | 在链上位置的hash                   |
| 3.11 | hasPk       | Int           | 否     | 是否拥有私钥信息(1-拥有，2-不拥有) |
| 3.12 | createTime  | LocalDateTime | 否     | 创建时间                           |
| 3.13 | modifyTime  | LocalDateTime | 否     | 修改时间                           |

#### 14.3.3 入参事例

[http://localhost:8080/fisco-bcos-node-mgr/user/](http://localhost:8080/fisco-bcos-node-mgr/user/privateKey/4585)

{

"userId": "400001",

"description": "newDescription"

}

#### 14.3.4 出参示例

**a)、成功：**

{

"code": 0,

"message": "success",

"data": {

"userId": 700007,

"userName": "asdfvw",

"networkId": 300001,

"orgId": 600001,

"publicKey":
"0x4189fdacff55fb99172e015e1adb96dc77b0c62e3b8fead7b392ac3634eab01bd3dffdae1619b1a41cc360777bee6682fcc9752d8aabf144fbf610a3057fd4b5",

"userStatus": 1,

"chainIndex": null,

"userType": 1,

"address": "0x40ec3c20b5178401ae14ad8ce9c9f94fa5ebb86a",

"hasPk": 1,

"description": "newDescription",

"createTime": "2019-03-15 18:00:27",

"modifyTime": "2019-03-15 18:00:27"

}

}

**b)、失败：**

｛

“code“:2001,

”message”: ”invalid userid”,

“data”:{}

｝

### 14.4 查询私钥

#### 14.4.1 传输协议规范

a)、网络传输协议：使用HTTP协议

b)、请求地址：/user/privateKey/{userId}

c)、请求方式：GET

d)、返回格式：json

#### 14.4.2 参数信息详情

| 序号 | 输入参数   | 类型   | 可为空 | 备注                       |
|------|------------|--------|--------|----------------------------|
| 1    | userId     | int    | 否     | 用户编号                   |
| 序号 | 输出参数   | 类型   |        | 备注                       |
| 1    | code       | Int    | 否     | 返回码，0：成功 其它：失败 |
| 2    | message    | String | 否     | 描述                       |
| 3    | data       | Object | 否     | 返回私钥信息实体           |
| 3.1  | privateKey | String | 否     | 私钥                       |
| 3.2  | address    | String | 否     | 用户链上地址               |

#### 14.4.3 入参事例

<http://localhost:8080/fisco-bcos-node-mgr/user/privateKey/4585>

#### 14.4.4 出参示例

**a)、成功：**

｛

“code“:0,

”message”: ”success” ,

“data”:{

” “privateKey””:123456，

“address”:”asfsafasfasfasfasfas”}

｝

**b)、失败：**

｛

“code“:2001,

”message”: ”invalid userid”,

“data”:{}

｝

### 14.5 查询用户列表

#### 14.5.1 传输协议规范

a)、网络传输协议：使用HTTP协议

b)、请求地址:

/user/userList/{networkId}/{orgId}/{pageNumber}/{pageSize}?userName=nameValue

c)、请求方式：GET

d)、返回格式：JSON

#### 14.5.2 参数信息详情

| 序号   | 输入参数    | 类型          | 可为空 | 备注                               |
|--------|-------------|---------------|--------|------------------------------------|
| 1      | networkId   | int           | 否     | 所属网络id                         |
| 2      | pageSize    | Int           | 否     | 每页记录数                         |
| 3      | pageNumber  | Int           | 否     | 当前页码                           |
| 4      | userParam   | String        | 是     | 查询参数（用户名或公钥地址）       |
|        | 输出参数    | 类型          |        | 备注                               |
| 1      | code        | Int           | 否     | 返回码，0：成功 其它：失败         |
| 2      | message     | String        | 否     | 描述                               |
| 3      | totalCount  | Int           | 否     | 总记录数                           |
| 4      | data        | List          | 是     | 用户列表                           |
| 4.1    |             | Object        |        | 用户信息对象                       |
| 4.1.1  | userId      | int           | 否     | 用户编号                           |
| 4.1.2  | userName    | string        | 否     | 用户名称                           |
| 4.1.3  | networkId   | int           | 否     | 所属网络编号                       |
| 4.1.4  | orgId       | int           | 否     | 所属组织编号                       |
| 4.1.5  | description | String        | 是     | 备注                               |
| 4.1.6  | userStatus  | int           | 否     | 状态（1-正常 2-停用） 默认1        |
| 4.1.7  | publicKey   | String        | 否     | 公钥信息                           |
| 4.1.8  | chainIndex  | int           | 是     | 保存在链上的索引位置               |
| 4.1.9  | userType    | int           | 否     | 用户类型（1-普通用户 2-系统用户）  |
| 4.1.10 | address     | String        | 是     | 在链上位置的hash                   |
| 4.1.11 | hasPk       | Int           | 否     | 是否拥有私钥信息(1-拥有，2-不拥有) |
| 4.1.12 | createTime  | LocalDateTime | 否     | 创建时间                           |
| 4.1.13 | modifyTime  | LocalDateTime | 否     | 修改时间                           |

#### 14.5.3 入参事例

http://127.0.0.1:3001/mgr/webase-node-mgr/user/userList/300001/1/10?userParam=asdfvw

#### 14.5.4 出参示例

**a)、成功：**

{

"code": 0,

"message": "success",

"data": [

{

"userId": 700007,

"userName": "asdfvw",

"networkId": 300001,

"orgId": 600001,

"publicKey":
"0x4189fdacff55fb99172e015e1adb96dc77b0c62e3b8fead7b392ac3634eab01bd3dffdae1619b1a41cc360777bee6682fcc975238aabf144fbf610a3057fd4b5",

"userStatus": 1,

"chainIndex": 3,

"userType": 1,

"address": "0x40ec3c20b5178401ae14ad8ce9c9f94fa5ebb86a",

"hasPk": 1,

"description": "sda",

"createTime": "2019-03-15 18:00:27",

"modifyTime": "2019-03-15 18:00:28"

}

],

"totalCount": 1

}

**b)、失败：**

｛

“code“: 102000:,

”message”: "system exception",

“data”:{}

｝

# 区块链平台节点管理接口设计

## 目录
<!-- TOC -->
- [区块链平台节点管理接口设计](#区块链平台节点管理接口设计)
    - [1 前置管理模块](#1-前置管理模块)
        - [1.1 新增节点前置信息](#11-新增节点前置信息)
        - [1.2 获取所有前置列表](#12-获取所有前置列表)
        - [1.3 删除前置信息](#13-删除前置信息)
    - [2 交易信息模块](#2-交易信息模块)
        - [2.1 查询交易信息列表](#21-查询交易信息列表)
        - [2.2 查询交易回执](#22-查询交易回执)
        - [2.3 根据交易hash查询交易信息](#23-根据交易hash查询交易信息)
    - [3 帐号管理模块](#3-帐号管理模块)
        - [3.1 新增帐号](#31-新增帐号)
        - [3.2 修改帐号](#32-修改帐号)
        - [3.3 删除帐号](#33-删除帐号)
        - [3.4 查询帐号列表](#34-查询帐号列表)
        - [3.5 更新当前密码](#35-更新当前密码)
        - [3.6 获取登录验证码](#36-获取登录验证码)
        - [3.7 登录接口](#37-登录接口)
    - [4 区块管理模块](#4-区块管理模块)
        - [4.1 查询区块列表](#41-查询区块列表)
        - [4.2 根据块高或hash查询区块信息](#42-根据块高或hash查询区块信息)
    - [5 合约管理模块](#5-合约管理模块)
        - [5.1 查询合约列表](#51-查询合约列表)
        - [5.2 查询合约信息](#52-查询合约信息)
        - [5.3 部署合约](#53-部署合约)
        - [5.4 发送交易](#54-发送交易)
        - [5.5 根据包含bytecodeBin的字符串查询合约](#55-根据包含bytecodebin的字符串查询合约)
    - [6 服务器监控相关](#6-服务器监控相关)
        - [6.1 获取节点监控信息](#61-获取节点监控信息)
        - [6.2 获取服务器监控信息](#62-获取服务器监控信息)
    - [7 审计相关模块](#7-审计相关模块)
        - [7.1 获取用户交易监管信息列表](#71-获取用户交易监管信息列表)
        - [7.2 获取合约方法监管信息列表](#72-获取合约方法监管信息列表)
        - [7.3 获取交易hash监管信息列表](#73-获取交易hash监管信息列表)
        - [7.4 获取异常用户信息列表](#74-获取异常用户信息列表)
        - [7.5 获取异常合约信息列表](#75-获取异常合约信息列表)
    - [8 群组信息模块](#8-群组信息模块)
        - [8.1 获取群组概况](#81-获取群组概况)
        - [8.2 获取所有群组列表](#82-获取所有群组列表)
        - [8.3 查询每日交易数据](#83-查询每日交易数据)
    - [9 节点管理模块](#9-节点管理模块)
        - [9.1 查询节点列表](#91-查询节点列表)
        - [9.2 查询节点信息](#92-查询节点信息)
    - [10 角色管理模块](#10-角色管理模块)
    - [11 用户管理模块](#11-用户管理模块)
        - [11.1 新增私钥用户](#111-新增私钥用户)
        - [11.2 绑定公钥用户](#112-绑定公钥用户)
        - [11.3 修改用户备注](#113-修改用户备注)
        - [11.4 查询私钥](#114-查询私钥)
        - [11.5 查询用户列表](#115-查询用户列表)
    - [12 合约方法管理模块](#12-合约方法管理模块)
        - [12.1 新增合约方法](#121-新增合约方法)
        - [12.2 根据方法编号查询](#122-根据方法编号查询)

<!-- /TOC -->





## 1 前置管理模块  
[top](#目录)
### 1.1 新增节点前置信息
[top](#目录)

#### 1.1.1 传输协议规范
* 网络传输协议：使用HTTP协议
* 请求地址： `/front/new`
* 请求方式：POST
* 请求头：Content-type: application/json
* 返回格式：JSON

#### 1.1.2 参数信息详情

| 序号 | 输入参数    | 类型          | 可为空 | 备注                                       |
|------|-------------|---------------|--------|--------------------------------------------|
| 1    | frontIp     | string        | 否     | 前置ip                                     |
| 2    | frontPort   | int           | 否     | 前置服务端口                               |
| 2    | agency      | int           | 否     | 所属机构                               |
| 序号 | 输出参数    | 类型          |        | 备注                                       |
| 1    | code        | Int           | 否     | 返回码，0：成功 其它：失败                 |
| 2    | message     | String        | 否     | 描述                                       |
| 3    |             | Object        |        | 节点信息对象                               |
| 3.1  | frontId     | int           | 否     | 前置编号                        |
| 3.2  | frontIp     | string        | 否     | 前置ip                                    |
| 3.3  | frontPort   | int           | 否     | 前置端口                                   |
| 3.4  | agency      | string        | 否     | 所属机构                                   |
| 3.5  | createTime  | LocalDateTime | 否     | 落库时间                                   |
| 3.6  | modifyTime  | LocalDateTime | 否     | 修改时间                                   |

#### 1.1.3 入参示例
`http://127.0.0.1:8080/WeBASE-Node-Manager/front/new`
```
{
    "frontIp": "127.0.0.1",
    "frontPort": "8081",
    "agency": "abc"
}
```


#### 1.1.4 出参示例
* 成功：
```
{
    "code": 0,
    "message": "success",
    "data": {
        "frontId": 500001,
        "frontIp": "127.0.0.1",
        "frontPort": 8181,
        "agency": "abc",
        "createTime": "2019-02-14 17:47:00",
        "modifyTime": "2019-03-15 11:14:29"
    }
}
```

* 失败：
```
{
    "code": 102000,
    "message": "system exception",
    "data": {}
}
```


### 1.2 获取所有前置列表 
[top](#目录)

#### 1.2.1 传输协议规范
* 网络传输协议：使用HTTP协议
* 请求地址： `/front/find?frontId={frontId}&groupId={groupId}`
* 请求方式：GET
* 返回格式：JSON

#### 1.2.2 参数信息详情

| 序号  | 输入参数      | 类型          | 可为空 | 备注                       |
|-------|---------------|---------------|--------|----------------------------|
| 1     | frontId       | Int           | 是     | 前置编号                  |
| 2     | groupId       | Int           | 是     | 所属群组编号                |
| 序号  | 输出参数      | 类型           |        | 备注                       |
| 1     | code          | Int           | 否     | 返回码，0：成功 其它：失败 |
| 2     | message       | String        | 否     | 描述                       |
| 3     | totalCount    | Int           | 否     | 总记录数                   |
| 4     | data          | List          | 否     | 组织列表                   |
| 4.1   |               | Object        |        | 节点信息对象               |
| 4.1.1 | frontId       | int           | 否     | 前置编号                   |
| 4.1.2 | frontIp       | string        | 否     | 前置ip                     |
| 4.1.3 | frontPort     | int           | 否     | 前置端口                   |
| 4.1.4 | createTime    | LocalDateTime | 否     | 落库时间                   |
| 4.1.5 | modifyTime    | LocalDateTime | 否     | 修改时间                   |
| 4.1.6 | agency        | string        | 否     | 所属机构                   |


#### 1.2.3 入参示例
`http://127.0.0.1:8080/WeBASE-Node-Manager/front/find`

#### 1.2.4 出参示例
* 成功：
```
{
    "code": 0,
    "message": "success",
    "data": [
        {
            "frontId": 500001,
            "frontIp": "10.107.105.18",
            "frontPort": 8081,
            "agency": "aa",
            "createTime": "2019-06-04 20:49:42",
            "modifyTime": "2019-06-04 20:49:42"
        }
    ],
    "totalCount": 1
}
```

* 失败：
```
{
    "code": 102000,
    "message": "system exception",
    "data": {}
}
```



### 1.3 删除前置信息
[top](#目录)

#### 1.3.1 传输协议规范
* 网络传输协议：使用HTTP协议
* 请求地址：`/front/{frontId}`
* 请求方式：DELETE
* 请求头：Content-type: application/json
* 返回格式：JSON

#### 1.3.2 参数信息详情

| 序号 | 输入参数   | 类型   | 可为空 | 备注                       |
|------|------------|--------|--------|----------------------------|
| 1    | frontId    | int    | 否     | 前置编号                   |
| 序号 | 输出参数   | 类型   |        | 备注                       |
| 1    | code       | Int    | 否     | 返回码，0：成功 其它：失败 |
| 2    | message    | String | 否     | 描述                       |
| 3    | data       | object | 是     | 返回信息实体（空）         |

#### 1.3.3 入参示例
`http://127.0.0.1:8080/WeBASE-Node-Manager/front/{frontId}`

#### 1.3.4 出参示例

* 成功：
```
{
    "code": 0,
    "data": {},
    "message": "Success"
}
```


* 失败：
```
{
    "code": 102000,
    "message": "system exception",
    "data": {}
}
```



## 2 交易信息模块
[top](#目录)

### 2.1 查询交易信息列表
[top](#目录)


#### 2.1.1 传输协议规范

* 网络传输协议：使用HTTP协议
* 请求地址：`/transaction/transList/{groupId}/{pageNumber}/{pageSize}?transactionHash={transactionHash}&blockNumber={blockNumber}`
* 请求方式：GET
* 返回格式：JSON

#### 2.1.2 参数信息详情

| 序号  | 输入参数        | 类型          | 可为空 | 备注                       |
|-------|-----------------|---------------|--------|----------------------------|
| 1     | groupId         | int           | 否     | 所属群组编号               |
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
| 4.1.2 | groupId         | Int           | 否     | 所属群组编号               |
| 4.1.3 | blockNumber     | BigInteger    | 否     | 所属块高                   |
| 4.1.4 | statisticsFlag  | Int           | 否     | 是否已经统计               |
| 4.1.5 | createTime      | LocalDateTime | 否     | 落库时间                   |
| 4.1.6 | modifyTime      | LocalDateTime | 否     | 修改时间                   |

#### 2.1.3 入参示例
```
http://127.0.0.1:8080/WeBASE-Node-Manager/transaction/transList/300001/1/10?transactionHash=0x303daa78ebe9e6f5a6d9761a8eab4bf5a0ed0b06c28764488e4716de42e1df01
```

#### 2.1.4 出参示例
* 成功：
```
{
    "code": 0,
    "message": "success",
    "data": [
        {
            "transHash": "0x303daa78ebe9e6f5a6d9761a8eab4bf5a0ed0b06c28764488e4716de42e1df01",
            "groupId": 300001,
            "blockNumber": 133,
            "statisticsFlag": 1,
            "createTime": "2019-03-15 09:36:17",
            "modifyTime": "2019-03-15 09:36:17"
        }
    ],
    "totalCount": 1
}
```


* 失败：
```
{
    "code": 102000,
    "message": "system exception",
    "data": {}
}
```


### 2.2 查询交易回执 
[top](#目录)


#### 2.2.1 传输协议规范

* 网络传输协议：使用HTTP协议
* 请求地址：`/transaction/transactionReceipt/{groupId}/{transHash}`
* 请求方式：GET
* 返回格式：JSON

#### 2.2.2 参数信息详情

| 序号  | 输入参数        | 类型          | 可为空 | 备注                       |
|-------|-----------------|---------------|--------|----------------------------|
| 1     | groupId         | int           | 否     | 所属群组编号               |
| 2     | transHash | String        | 是     | 交易hash                   |
| 序号  | 输出参数        | 类型          |        | 备注                       |
| 1     | code            | Int           | 否     | 返回码，0：成功 其它：失败 |
| 2     | message         | String        | 否     | 描述                       |
| 3     |                 | Object        |        | 交易信息对象               |
| 3.1 | transactionHash       | String        | 否     | 交易hash                   |
| 3.2 | transactionIndex         | Int           | 否     | 在区块中的索引               |
| 3.2 | blockHash         | String           | 否     | 区块hash               |
| 3.3 | blockNumber     | BigInteger    | 否     | 所属块高                   |
| 3.4 | cumulativeGasUsed  | Int           | 否     |                |
| 3.5 | gasUsed      | Int | 否     | 交易消耗的gas                   |
| 3.6 | contractAddress      | String | 否     | 合约地址                   |
| 3.7 | status      | String | 否     | 交易的状态值                   |
| 3.8 | from      | String | 否     | 交易发起者                   |
| 3.9 | to      | String | 否     | 交易目标                   |
| 3.10 | output      | String | 否     | 交易输出内容                   |
| 3.11 | logs      | String | 否     | 日志                   |
| 3.12 | logsBloom      | String | 否     | log的布隆过滤值                   |

#### 2.2.3 入参示例
```
http://127.0.0.1:8080/WeBASE-Node-Manager/transaction/transactionReceipt/1/0xda879949df6b5d75d2d807f036b461e0cebcc1abaccac119c9a282d3941a4818
```

#### 2.2.4 出参示例
* 成功：
```
{
    "code": 0,
    "message": "success",
    "data": {
        "transactionHash": "0xda879949df6b5d75d2d807f036b461e0cebcc1abaccac119c9a282d3941a4818",
        "transactionIndex": 0,
        "blockHash": "0x739853061c6c87ed691c0ee6f938589f7e2e442d42b16f582b353a475359b91d",
        "blockNumber": 4311,
        "cumulativeGasUsed": 0,
        "gasUsed": 32940,
        "contractAddress": "0x0000000000000000000000000000000000000000",
        "status": "0x0",
        "from": "0xe4bc056009daed8253008e03db6f62d93ccfacea",
        "to": "0x522eda3fbe88c07025f1db3f7dc7d9836af95b3f",
        "output": "0x",
        "logs": [],
        "logsBloom": "0x000000000000000000000000000000000000000",
        "blockNumberRaw": "0x10d7",
        "transactionIndexRaw": "0x0",
        "statusOK": true,
        "gasUsedRaw": "0x80ac"
    }
}
```


* 失败：
```
{
    "code": 102000,
    "message": "system exception",
    "data": {}
}
```


### 2.3 根据交易hash查询交易信息 
[top](#目录)


#### 2.3.1 传输协议规范

* 网络传输协议：使用HTTP协议
* 请求地址：`/transaction/transInfo/{groupId}/{transHash}`
* 请求方式：GET
* 返回格式：JSON

#### 2.3.2 参数信息详情

| 序号  | 输入参数        | 类型          | 可为空 | 备注                       |
|-------|-----------------|---------------|--------|----------------------------|
| 1     | groupId         | int           | 否     | 所属群组编号               |
| 2     | transHash       | String        | 是     | 交易hash                   |
| 序号  | 输出参数        | 类型          |        | 备注                       |
| 1     | code            | Int           | 否     | 返回码，0：成功 其它：失败 |
| 2     | message         | String        | 否     | 描述                       |
| 3     |                 | Object        |        | 交易信息对象               |
| 3.1   | hash            | String        | 否     | 交易hash                   |
| 3.2   | transactionIndex         | Int           | 否     | 在区块中的索引               |
| 3.2   | blockHash         | String           | 否     | 区块hash               |
| 3.3   | blockNumber     | BigInteger    | 否     | 所属块高                   |
| 3.4   | cumulativeGasUsed  | Int           | 否     |                |
| 3.5   | gasUsed         | Int | 否     | 交易消耗的gas                   |
| 3.6   | contractAddress      | String | 否     | 合约地址                   |
| 3.7   | status          | String | 否     | 交易的状态值                   |
| 3.8   | from            | String | 否     | 交易发起者                   |
| 3.9   | to              | String | 否     | 交易目标                   |
| 3.10  | output          | String | 否     | 交易输出内容                   |
| 3.11  | logs            | String | 否     | 日志                   |
| 3.12  | logsBloom       | String | 否     | log的布隆过滤值      |
| 3.13  | nonce           | String | 否     |                    |
| 3.14  | value           | String | 否     |                    |
| 3.15  | gasPrice        | long | 否     |                    |
| 3.16  | gas             | long | 否     |                    |
| 3.17  | input           | String | 否     |                    |
| 3.18  | v               | int | 否     |                    |
| 3.19  | nonceRaw        | String | 否     |                    |
| 3.20  | blockNumberRaw  | String | 否     |                    |
| 3.21  | gasPriceRaw     | String | 否     |                    |
| 3.22  | gasRaw          | String | 否     |                    |





#### 2.3.3 入参示例
```
http://127.0.0.1:8080/WeBASE-Node-Manager/transaction/transInfo/1/0xda879949df6b5d75d2d807f036b461e0cebcc1abaccac119c9a282d3941a4818
```

#### 2.3.4 出参示例
* 成功：
```
{
    "code": 0,
    "message": "success",
    "data": {
        "hash": "0xda879949df6b5d75d2d807f036b461e0cebcc1abaccac119c9a282d3941a4818",
        "nonce": "600264747827990445399299219738839026203774909117379671331964756256186263529",
        "blockHash": "0x739853061c6c87ed691c0ee6f938589f7e2e442d42b16f582b353a475359b91d",
        "blockNumber": 4311,
        "transactionIndex": 0,
        "from": "0xe4bc056009daed8253008e03db6f62d93ccfacea",
        "to": "0x522eda3fbe88c07025f1db3f7dc7d9836af95b3f",
        "value": 0,
        "gasPrice": 100000000,
        "gas": 100000000,
        "input": "0x4ed3885e000000000000000",
        "v": 0,
        "nonceRaw": "0x153bce0f26461030fe5189385b9c3e84336b007769a3849524ca3f4af7d67e9",
        "blockNumberRaw": "0x10d7",
        "transactionIndexRaw": "0x0",
        "gasPriceRaw": "0x5f5e100",
        "gasRaw": "0x5f5e100"
    }
}
```


* 失败：
```
{
    "code": 102000,
    "message": "system exception",
    "data": {}
}
```




## 3 帐号管理模块
[top](#目录)

### 3.1 新增帐号
[top](#目录)

#### 3.1.1 传输协议规范

* 网络传输协议：使用HTTP协议
* 请求地址：`/account/accountInfo`
* 请求方式：post
* 请求头：Content-type: application/json
* 返回格式：JSON

#### 3.1.2 参数信息详情

| 序号 | 输入参数      | 类型          | 可为空 | 备注                       |
|------|---------------|---------------|--------|----------------------------|
| 1    | account       | String        | 否     | 帐号名称                   |
| 2    | accountPwd    | String        | 否     | 登录密码（sha256）         |
| 3    | roleId        | int           | 否     | 所属角色                   |
| 序号 | 输出参数      | 类型          |        | 备注                        |
| 1    | code          | Int           | 否     | 返回码，0：成功 其它：失败   |
| 2    | message       | String        | 否     | 描述                      |
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

### 3.1.3 入参示例

`http://127.0.0.1:8080/WeBASE-Node-Manager/account/accountInfo`
```
{
    "account": "testAccount",
    "accountPwd": "3f21a8490cef2bfb60a9702e9d2ddb7a805c9bd1a263557dfd51a7d0e9dfa93e",
    "roleId": 100001
}
```


#### 3.1.4 出参示例

* 成功：
```
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
```


* 失败：
```
{
    "code": 102000,
    "message": "system exception",
    "data": {}
}
```


### 3.2 修改帐号
[top](#目录)

#### 3.2.1 传输协议规范
* 网络传输协议：使用HTTP协议
* 请求地址：`/account/accountInfo`
* 请求方式：PUT
* 请求头：Content-type: application/json
* 返回格式：JSON

#### 3.2.2 参数信息详情

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

#### 3.2.3 入参示例

`http://127.0.0.1:8080/WeBASE-Node-Manager/account/accountInfo`
```
{
    "account": "testAccount",
    "accountPwd": "82ca84cf0d2ae423c09a214cee2bd5a7ac65c230c07d1859b9c43b30c3a9fc80",
    "roleId": 100001
}
```


#### 3.2.4 出参示例

* 成功：
```
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
```

* 失败：
```
{
    "code": 102000,
    "message": "system exception",
    "data": {}
}
```

### 3.3 删除帐号
[top](#目录)

#### 3.3.1 传输协议规范
* 网络传输协议：使用HTTP协议
* 请求地址：`/account/{account}`
* 请求方式：DELETE
* 返回格式：JSON

#### 3.3.2 参数信息详情

| 序号 | 输入参数 | 类型   | 可为空 | 备注                       |
|------|----------|--------|--------|------------------------|
| 1    | account  | String | 否     | 帐号名称                   |
| 序号 | 输出参数 | 类型   |        | 备注                       |
| 1    | code     | Int    | 否     | 返回码，0：成功 其它：失败 |
| 2    | message  | String | 否     | 描述                       |
| 3    | data     | object | 是     | 返回信息实体（空）         |

#### 3.3.3 入参示例
`http://127.0.0.1:8080/WeBASE-Node-Manager/account/testAccount`

#### 3.3.4 出参示例
* 成功：
```
{
    "code": 0,
    "data": {},
    "message": "Success"
}
```

* 失败：
```
{
    "code": 102000,
    "message": "system exception",
    "data": {}
}
```

###  3.4 查询帐号列表
[top](#目录)

#### 3.4.1 传输协议规范
* 网络传输协议：使用HTTP协议
* 请求地址: `/account/accountList/{pageNumber}/{pageSize}?account={account}`
* 请求方式：GET
* 返回格式：JSON

#### 3.4.2 参数信息详情

| 序号  | 输入参数      | 类型          | 可为空 | 备注                       |
|-------|---------------|---------------|--------|----------------------------|
| 1     | pageSize      | Int           | 否     | 每页记录数                 |
| 2     | pageNumber    | Int           | 否     | 当前页码                   |
| 3     | account       | String        | 是     | 帐号                       |
|       | 输出参数      | 类型          |        | 备注                       |
| 1     | code          | Int           | 否     | 返回码，0：成功 其它：失败 |
| 2     | message       | String        | 否     | 描述                       |
| 3     | totalCount    | Int           | 否     | 总记录数                   |
| 4     | data          | List          | 是     | 信息列表                   |
| 4.1   |               | Object        |        | 信息对象               |
| 4.1.1 | account       | String        | 否     | 帐号                       |
| 4.1.2 | roleId        | Integer       | 否     | 所属角色                   |
| 4.1.3 | roleName      | String        | 否     | 角色名称                   |
| 4.1.4 | roleNameZh    | String        | 否     | 角色中文名                 |
| 4.1.5 | loginFailTime | Integer       | 是     | 登录失败次数               |
| 4.1.6 | accountStatus | Integer       | 否     | 帐号状态                   |
| 4.1.7 | description   | String        | 是     | 备注                       |
| 4.1.8 | createTime    | LocalDateTime | 否     | 创建时间                   |
| 4.1.9 | modifyTime    | LocalDateTime | 否     | 修改时间                   |

#### 3.4.3 入参示例
`http://127.0.0.1:8080/WeBASE-Node-Manager/account/accountList/1/10?account=`

#### 3.4.4 出参示例
* 成功：
```
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
```

* 失败：
```
{
    "code": 102000,
    "message": "system exception",
    "data": {}
}
```

### 3.5 更新当前密码 
[top](#目录)

#### 3.5.1 传输协议规范
* 网络传输协议：使用HTTP协议
* 请求地址：`/account/passwordUpdate`
* 请求方式：put
* 请求头：Content-type: application/json
* 返回格式：JSON

#### 3.5.2 参数信息详情

| 序号 | 输入参数      | 类型   | 可为空 | 备注                       |
|------|---------------|--------|--------|----------------------------|
| 1    | oldAccountPwd | String | 否     | 旧密码（sha256）           |
| 2    | newAccountPwd | String | 否     | 新密码（sha256）           |
| 序号 | 输出参数      | 类型   |        | 备注                       |
| 1    | code          | Int    | 否     | 返回码，0：成功 其它：失败 |
| 2    | message       | String | 否     | 描述                       |

### 3.5.3 入参示例
`http://127.0.0.1:8080/WeBASE-Node-Manager/account/passwordUpdate`
```
{
    "oldAccountPwd": "dfdfgdg490cef2bfb60a9702erd2ddb7a805c9bd1arrrewefd51a7d0etttfa93e ",
    "newAccountPwd": "3f21a8490cef2bfb60a9702e9d2ddb7a805c9bd1a263557dfd51a7d0e9dfa93e"
}
```


#### 3.5.4 出参示例
* 成功：
```
{
    "code": 0,
    "message": "success"
}
```

* 失败：
```
{
    "code": 102000,
    "message": "system exception",
    "data": {}
}
```



### 3.6 获取登录验证码 
[top](#目录)

#### 3.6.1 传输协议规范
* 网络传输协议：使用HTTP协议
* 请求地址：`/account/pictureCheckCode`
* 请求方式：get
* 请求头：Content-type: application/json
* 返回格式：JSON

#### 3.6.2 参数信息详情

| 序号 | 输入参数      | 类型   | 可为空 | 备注                       |
|------|---------------|--------|--------|----------------------------|
| 无 |  |  |    |         |
| 序号 | 输出参数      | 类型   |        | 备注                        |
| 1    | code          | Int    | 否     | 返回码，0：成功 其它：失败   |
| 2    | message       | String | 否     | 描述                      |
| 3    | data       | Object | 否     | 图片信息实体                  |
| 3.1  | base64Image   | String | 否     | 图片的base64              |
| 3.2  | token   | String | 否     | token（登录接口需要用到此值）     |



### 3.6.3 入参示例
`http://127.0.0.1:8080/WeBASE-Node-Manager/account/pictureCheckCode`



#### 3.6.4 出参示例
* 成功：
```
{
    "code": 0,
    "message": "success",
    "data": {
        "base64Image": "iVBORw0KGgoAAAANSUhEUgAAAJsAAAA8CAIAAAD+Gl+NAAAC3ElEQVR42u3cO04DMRAG4DkER6CFhoNwCgoq6DgQh0DiVJR0oUCKovWuPU977P2trZK1s/GnGb8g9PN7wbXSRReUtQpEIYoCURSIokAUBaIQTV2+P798r3mfnJbB69nXvmAnjdHm9+8ZgruNjI31mUQrvWPpPpeuH+43n2g9FOJyrOJmiMpQ+4fC0cdVniED6hzj6NjMdv3cZvJHjCpz70DazJY+oh2mBptmH+7um5eo/ff3F+mVENJHVDrMeEUGB1W0mFGgLrhnVI9OL1QdZ58wzTnVIMcA3X3FXTQP6tjJ7dFuBjnm29JgXlGXbazoubcs63K23JgSxu/MQXXpKd1IWS6XQ42b7ZOoH/lIZfp9ff7YXCuJHvX709sj87Jb1kQ5Ichp/XpPySmlra9hvCJAl3vr77qg8kO/h2idU4eaRJTTCXzRo9QtSuOkBuOjckQVriWqC7MIldnX6jD1OfF25NwNUP679X50n+4qljH2rMBJvNYY5QefDrWspZ4xRSxjOqMyh1I+arhoqbVbS4HK5IzebejAKUIlS/AlRL0OJ1OgShcwyvVotKjjLGmD2vSOXptGc3J63iSaBFUUx+oDmVSilf4/nWieMHURLRWsomrUnhvcOVEdRW8tyIIkuj9OtKmVUDSC01NUh9ozr2ZDdU+5jRPvIFHmEsUy3eVMeqP37jtwVk7uyCgkrcU5VtOFcsW1/2F4kCjnCLZxPqqI1Hot/mmMPVKDNgXdRflnMso9I07Aqc/abl+PWMOsispvmaRZVL3ru3vmF7EkdT+NWUq0kr5FO4Vl3U31iB0GL865UAV/Czj8X/tOUoxdhN9hyChqqQ7RpTghClEUiKJAFKIQBSpEF0aFKETX7cRsW1q6JyEoHh0qZABWfDqdnDPi5rGPSqflHLK06OBKiM7O1e0DRH1EIETn2HY4bMxffp31F5MHRmeQq3EW5vPrN2eOTq8h2X0u/d/aH4oBfftm+5EiAAAAAElFTkSuQmCC",
        "token": "eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiIzOGM0NjlhNC1kMTg3LTQyZDQtYWM1YS02OWU0OWM5MjMxNTkiLCJpYXQiOjE1NjAyNDY3MzksInN1YiI6ImU1RnoiLCJleHAiOjE1NjAyNDY3OTl9.FJYRZJSAhFjvO_P4AjMO6bnoOZJiu-AOSdO9ikb-30M"
    }
}
```

* 失败：
```
{
    "code": 102000,
    "message": "system exception",
    "data": {}
}
```


### 3.7 登录接口
[top](#目录)

#### 3.7.1 传输协议规范
* 网络传输协议：使用HTTP协议
* 请求地址：`/account/login?checkCode={checkCode}`
* 请求方式：get
* 请求头：Content-type: application/json;token:{token}
* 返回格式：JSON

#### 3.7.2 参数信息详情

| 序号 | 输入参数      | 类型   | 可为空 | 备注                       |
|------|---------------|--------|--------|----------------------------|
| 1    | checkCode          | String    | 否     | 登录验证码   |
| 2    | account          | String    | 否     | 帐号   |
| 3    | accountPwd          | String    | 否     | 密码   |
| 4    | token          | String    | 否     | 随验证码返回的token   |
| 序号 | 输出参数      | 类型   |        | 备注                        |
| 1    | code          | Int    | 否     | 返回码，0：成功 其它：失败   |
| 2    | message       | String | 否     | 描述                      |
| 3    | data       | Object | 否     | 信息实体                  |
| 3.1  | accountStatus   | int | 否     | 1未修改密码，2正常   |
| 3.2  | roleName   | String | 否     | 所属角色名称     |
| 3.3  | account   | String | 否     | 帐号名称     |



### 3.7.3 入参示例
`http://127.0.0.1:8080/WeBASE-Node-Manager/account/login?checkCode=aege`
```
{
    "account": "admin",
    "accountPwd": "Abcd1234"
}
```


#### 3.7.4 出参示例
* 成功：
```
{
    "code": 0,
    "data": {
        "accountStatus": 2,
        "roleName": "admin",
        "account": "admin"
    },
    "message": "success"
}
```

* 失败：
```
{
    "code": 102000,
    "message": "system exception",
    "data": {}
}
```





## 4 区块管理模块
[top](#目录)

### 4.1 查询区块列表
[top](#目录)

#### 4.1.1 传输协议规范
* 网络传输协议：使用HTTP协议
* 请求地址:`/block/blockList/{groupId}/{pageNumber}/{pageSize}}?pkHash={pkHash}&blockNumber={blockNumber}`
* 请求方式：GET
* 返回格式：JSON

#### 4.1.2 参数信息详情

| 序号  | 输入参数       | 类型          | 可为空 | 备注                       |
|-------|----------------|---------------|--------|----------------------------|
| 1     | groupId        | Int           | 否     | 当前所属链                 |
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
| 4.1.2 | blockNumber    | BigInteger    | 否     | 块高                       |
| 4.1.3 | blockTimestamp | LocalDateTime | 否     | 出块时间                   |
| 4.1.4 | transCount     | int           | 否     | 交易数                     |
| 4.1.5 | sealerIndex    | int           | 否     | 打包节点索引                     |
| 4.1.6 | sealer         | String        | 否     | 打包节点                     |
| 4.1.7 | createTime     | LocalDateTime | 否     | 创建时间                   |
| 4.1.8 | modifyTime     | LocalDateTime | 否     | 修改时间                   |

#### 4.1.3 入参示例
`http://127.0.0.1:8080/WeBASE-Node-Manager/block/blockList/300001/1/10?pkHash=`

#### 4.1.4 出参示例
* 成功：
```
{
    "code": 0,
    "message": "success",
    "data": [
        {
            "pkHash": "0x10fb8485eebffbb2a0b0d4f22d58d5cd54df2ac53f974b6c731c954957f36dd7",
            "blockNumber": 127,
            "blockTimestamp": "2019-06-11 18:11:32",
            "transCount": 1,
            "sealerIndex": 2,
            "sealer": "552398be0eef124c000e632b0b76a48c52b6cfbd547d92c15527c2d1df15fab2bcded48353db22526c3540e4ab2027630722889f20a4a614bb11a7887a85941b",
            "createTime": "2019-06-11 18:11:36",
            "modifyTime": "2019-06-11 18:11:36"
        }
    ],
    "totalCount": 1
}
```

* 失败：
```
{
    "code": 102000,
    "message": "system exception",
    "data": {}
}
```


### 4.2 根据块高或hash查询区块信息
[top](#目录)

#### 4.2.1 传输协议规范
* 网络传输协议：使用HTTP协议
* 请求地址:`/block/blockList/{groupId}/{pageNumber}/{pageSize}}?pkHash={pkHash}&blockNumber={blockNumber}`
* 请求方式：GET
* 返回格式：JSON

#### 4.2.2 参数信息详情

| 序号  | 输入参数       | 类型          | 可为空 | 备注                       |
|-------|----------------|---------------|--------|----------------------------|
| 1     | groupId        | Int           | 否     | 当前所属链                 |
| 2     | pageSize       | Int           | 否     | 每页记录数                 |
| 3     | pageNumber     | Int           | 否     | 当前页码                   |
| 4     | pkHash         | String        | 是     | 区块hash                   |
| 5     | blockNumber    | BigInteger    | 是     | 块高                       |
|       | 输出参数       | 类型          |        | 备注                       |
| 1     | code           | Int           | 否     | 返回码，0：成功 其它：失败 |
| 2     | message        | String        | 否     | 描述                       |
| 3     |                | Object        |        | 区块信息对象               |
| 3.1   | number         | BigInteger    | 否     | 块高                       |
| 3.2   | hash           | String        | 否     | 区块hsah                   |
| 3.3   | parentHash     | String        | 否     | 父块hash                   |
| 3.4   | nonce          | String        | 否     | 随机数                     |
| 3.5   | sealer         | String        | 否     | 打包节点索                 |
| 3.6   | logsBloom      | String        | 否     | log的布隆过滤值            |
| 3.7   | transactionsRoot        | String        | 否     |                    |
| 3.8   | stateRoot        | String        | 否     |                    |
| 3.9   | difficulty        | String        | 否     |                    |
| 3.10   | totalDifficulty        | String        | 否     |                    |
| 3.11   | extraData        | String        | 否     |                    |
| 3.12   | size        | int        | 否     |                    |
| 3.13   | gasLimit        | long        | 否     | 限制gas值                   |
| 3.14   | gasUsed        | long        | 否     | 已使用的gas值                 |
| 3.15   | timestamp        | String        | 否     | 出块时间                   |
| 3.16 | gasLimitRaw        | String        | 否     |                    |
| 3.17   | timestampRaw        | String        | 否     |                    |
| 3.18   | gasUsedRaw        | String        | 否     |                    |
| 3.19   | numberRaw        | String        | 否     |                    |
| 3.20   | transactions        | List        | 否     |                    |
| 3.20.1     |                 | Object        |        | 交易信息对象               |
| 3.20.1.1   | hash            | String        | 否     | 交易hash                   |
| 3.20.1.2   | blockHash         | String           | 否     | 区块hash               |
| 3.20.1.3   | blockNumber     | BigInteger    | 否     | 所属块高                   |
| 3.20.1.4   | cumulativeGasUsed  | Int           | 否     |                |
| 3.20.1.5   | gasUsed         | Int | 否     | 交易消耗的gas                   |
| 3.20.1.6   | contractAddress      | String | 否     | 合约地址                   |
| 3.20.1.7   | status          | String | 否     | 交易的状态值                   |
| 3.20.1.8   | from            | String | 否     | 交易发起者                   |
| 3.20.1.9   | to              | String | 否     | 交易目标                   |
| 3.20.1.10  | output          | String | 否     | 交易输出内容                   |
| 3.20.1.11  | logs            | String | 否     | 日志                   |
| 3.20.1.12  | logsBloom       | String | 否     | log的布隆过滤值                   |
| 3.20.1.13  | nonce           | String | 否     |                    |
| 3.20.1.14  | value           | String | 否     |                    |
| 3.20.1.15  | gasPrice        | long | 否     |                    |
| 3.20.1.16  | gas             | long | 否     |                    |
| 3.20.1.17  | input           | String | 否     |                    |
| 3.20.1.18  | v               | int | 否     |                    |
| 3.20.1.19  | nonceRaw        | String | 否     |                    |
| 3.20.1.20  | blockNumberRaw  | String | 否     |                    |
| 3.20.1.21  | gasPriceRaw     | String | 否     |                    |
| 3.20.1.22  | gasRaw          | String | 否     |                    |
| 3.20.1.23  | transactionIndex         | Int           | 否     | 在区块中的索引               |



#### 4.2.3 入参示例
`http://127.0.0.1:8080/WeBASE-Node-Manager/block/blockByNumber/1/11`

#### 4.2.4 出参示例
* 成功：
```
{
    "code": 0,
    "message": "success",
    "data": {
        "number": 11,
        "hash": "0xeef574a136f1d5031ce7f5d4bbc19fa1a1b5736f38ec5687d43405a572219405",
        "parentHash": "0xca84147e343acb972dc9247727b920b5c081320bbe940f4e2b24363836dca4a1",
        "nonce": "0",
        "sealer": "0x0",
        "logsBloom": "0x000000000000000000000000000000000000",
        "transactionsRoot": "0x68510be0e37b993874c6cb59170b87f01fc9672a162b30df7ea96cb026f3ab27",
        "stateRoot": "0xa6e930f100c2f4a13816e57aede9b63f3b7d51d64148f4412d8a6efcb0fa9c79",
        "difficulty": 0,
        "totalDifficulty": 0,
        "extraData": [],
        "size": 0,
        "gasLimit": 0,
        "gasUsed": 0,
        "timestamp": "1551667286153",
        "gasLimitRaw": "0x0",
        "timestampRaw": "0x1694693d089",
        "gasUsedRaw": "0x0",
        "numberRaw": "0xb",
        "transactions": [
            {
                "hash": "0x30ab22a942a6545cfe46fd725e53311fbcfea655f9c0d1e198b83749f5d7bf9b",
                "nonce": "1224685724047484442779169279180691132123728860283320089873703663086305160417",
                "blockHash": "0xeef574a136f1d5031ce7f5d4bbc19fa1a1b5736f38ec5687d43405a572219405",
                "blockNumber": 11,
                "transactionIndex": 0,
                "from": "0x148947262ec5e21739fe3a931c29e8b84ee34a0f",
                "to": "0xdfb1684019f7f6ea2c41590ac55d29961de5deba",
                "value": 0,
                "gasPrice": 300000000,
                "gas": 300000000,
                "input": "0x66c991390000000000000000000000000000000000000000000000000000000000000004",
                "v": 0,
                "nonceRaw": "0x2b525c633f530fdd935428a58afcfbb533e4dd16f24eda6b6a860b63e6a2ce1",
                "blockNumberRaw": "0xb",
                "transactionIndexRaw": "0x0",
                "gasPriceRaw": "0x11e1a300",
                "gasRaw": "0x11e1a300"
            }
        ]
    }
```

* 失败：
```
{
    "code": 102000,
    "message": "system exception",
    "data": {}
}
```




## 5 合约管理模块  
[top](#目录)

### 5.1 查询合约列表 
[top](#目录)

#### 5.1.1 传输协议规范
* 网络传输协议：使用HTTP协议
* 请求地址： `/contract/contractList/{groupId}/{pageNumber}/{pageSize}`
* 请求方式：POST
* 返回格式：JSON

#### 5.1.2 参数信息详情

| 序号   | 输入参数        | 类型          | 可为空 | 备注                                            |
|--------|-----------------|---------------|--------|-------------------------------------------------|
| 1      | groupId       | int           | 否     | 群组id                                          |
| 2      | contractName       | String           | 否     | 合约名                             |
| 3      | contractAddress    | String           | 否     | 合约地址                               |
| 4      | pageSize        | int           | 否     | 每页记录数                                      |
| 5      | pageNumber      | int           | 否     | 当前页码                                        |
| 6      | contractStatus      | int           | 否     | 1未部署，2已部署                        |
|        |                 |               |        |                                                 |
| 序号   | 输出参数        | 类型          | 可为空 | 备注                                            |
| 1      | code            | Int           | 否     | 返回码，0：成功 其它：失败                      |
| 2      | message         | String        | 否     | 描述                                            |
| 3      | totalCount      | Int           | 否     | 总记录数                                        |
| 4      | data            | List          | 是     | 列表                                            |
| 5.1    |                 | Oject         |        | 返回信息实体                                    |
| 5.1.1  | contractId      | int           | 否     | 合约编号                                        |
| 5.1.2  | contractPath    | String        | 否     | 合约所在目录                              |
| 5.1.3  | contractName    | String        | 否     | 合约名称                                        |
| 5.1.4  | groupId       | Int           | 否     | 所属群组编号                                    |
| 5.1.5  | contractStatus      | int           | 否     | 1未部署，2已部署                        |
| 5.1.6  | contractType    | Int           | 否     | 合约类型(0-普通合约，1-系统合约)                |
| 5.1.7  | contractSource  | String        | 否     | 合约源码                                        |
| 5.1.8  | contractAbi     | String        | 是     | 编译合约生成的abi文件内容                       |
| 5.1.9  | contractBin     | String        | 是     | 合约binary                                      |
| 5.1.10 | bytecodeBin     | String        | 是     | 合约bin                                         |
| 5.1.11 | contractAddress | String        | 是     | 合约地址                                        |
| 5.1.12 | deployTime      | LocalDateTime | 是     | 部署时间                                        |
| 5.1.13 | contractVersion | String        | 否     | 合约版本（会去除该字段）               |
| 5.1.14 | description     | String        | 是     | 备注                                            |
| 5.1.15 | createTime      | LocalDateTime | 否     | 创建时间                                        |
| 5.1.16 | modifyTime      | LocalDateTime | 是     | 修改时间                                        |



#### 5.1.3 入参示例
`http://127.0.0.1:8080/WeBASE-Node-Manager/contract/contractList`
```

```

#### 5.1.4 出参示例

* 成功：
```
{
    "code": 0,
    "message": "success",
    "data": [
        {
            "contractId": 200002,
            "contractPath": "hellos",
            "contractVersion": null,
            "contractName": "hellos",
            "contractStatus": 2,
            "groupId": 1,
            "contractType": 0,
            "contractSource": "cHJhZ21hIHNvbGlkaXgICAJbmFtZSA9IG47CiAgICB9Cn0=",
            "contractAbi": "[\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"constructor\"}]",
            "contractBin": "60806040526004361061004c576000398de7e4ddf5fdc9ccbcfd44565fed695cd960b0029",
            "bytecodeBin": "608060405234801561001057600080004d4c",
            "deployTime": "2019-06-11 18:11:33",
            "description": null,
            "createTime": "2019-06-05 16:40:40",
            "modifyTime": "2019-06-11 18:11:33"
        }
    ],
    "totalCount": 1
}
```

* 失败：
```
{
    "code": 102000,
    "message": "system exception",
    "data": {}
}
```

### 5.2 查询合约信息
[top](#目录)

#### 5.2.1 传输协议规范
* 网络传输协议：使用HTTP协议
* 请求地址： `/contract/{contractId}`
* 请求方式：GET
* 返回格式：JSON

#### 5.2.2 参数信息详情

| 序号 | 输入参数        | 类型          | 可为空 | 备注                                            |
|------|-----------------|---------------|--------|-------------------------------------------------|
| 1    | groupId       | int           | 否     | 群组id                                          |
| 2    | contractId      | int           | 否     | 合约编号                                        |
| 序号 | 输出参数        | 类型          | 可为空 | 备注                                            |
| 1    | code            | Int           | 否     | 返回码，0：成功 其它：失败                      |
| 2    | message         | String        | 否     | 描述                                            |
| 3    |                 | Oject         |        | 返回信息实体                                    |
| 3.1  | contractId      | int           | 否     | 合约编号                                        |
| 3.2  | contractPath    | String        | 否     | 合约所在目录                              |
| 3.3  | contractName    | String        | 否     | 合约名称                                        |
| 3.4  | groupId         | Int           | 否     | 所属群组编号                                    |
| 3.5  | contractStatus  | int           | 否     | 1未部署，2已部署                        |
| 3.6  | contractType    | Int           | 否     | 合约类型(0-普通合约，1-系统合约)                |
| 3.7  | contractSource  | String        | 否     | 合约源码                                        |
| 3.8  | contractAbi     | String        | 是     | 编译合约生成的abi文件内容                       |
| 3.9  | contractBin     | String        | 是     | 合约binary                                      |
| 3.10 | bytecodeBin     | String        | 是     | 合约bin                                         |
| 3.11 | contractAddress | String        | 是     | 合约地址                                        |
| 3.12 | deployTime      | LocalDateTime | 是     | 部署时间                                        |
| 3.13 | contractVersion | String        | 否     | 合约版本（会去除该字段）               |
| 3.14 | description     | String        | 是     | 备注                                            |
| 3.15 | createTime      | LocalDateTime | 否     | 创建时间                                        |
| 3.16 | modifyTime      | LocalDateTime | 是     | 修改时间                                        |

#### 5.2.3 入参示例
`http://127.0.0.1:8080/WeBASE-Node-Manager/contract/200001`

#### 5.2.4 出参示例

* 成功：
```
{
    "code": 0,
    "message": "success",
    "data": {
        "contractId": 200002,
        "contractPath": "hellos",
        "contractVersion": null,
        "contractName": "hellos",
        "contractStatus": 2,
        "groupId": 1,
        "contractType": 0,
        "contractSource": "cHJhZ21hIHNvbGlkaXgICAJbmFtZSA9IG47CiAgICB9Cn0=",
        "contractAbi": "[\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"constructor\"}]",
        "contractBin": "60806040526004361061004c576000398de7e4ddf5fdc9ccbcfd44565fed695cd960b0029",
        "bytecodeBin": "608060405234801561001057600080004d4c",
        "deployTime": "2019-06-11 18:11:33",
        "description": null,
        "createTime": "2019-06-05 16:40:40",
        "modifyTime": "2019-06-11 18:11:33"
    }
}
```


* 失败：
```
{
    "code": 102000,
    "message": "system exception",
    "data": {}
}
```

### 5.3 部署合约
[top](#目录)

#### 5.3.1 传输协议规范
* 网络传输协议：使用HTTP协议
* 请求地址：`/contract/deploy`
* 请求方式：POST
* 请求头：Content-type: application/json
* 返回格式：JSON

#### 5.3.2 参数信息详情

| 序号 | 输入参数          | 类型           | 可为空 | 备注                       |
|------|-------------------|----------------|--------|----------------------------|
| 1    | groupId           | Int            | 否     | 所属群组编号               |
| 2    | contractName      | String         | 否     | 合约名称               |
| 3    | contractSource    | String         | 否     | 合约源码                   |
| 4    | contractAbi       | String         | 否     | 编译合约生成的abi文件内容  |
| 5    | contractBin       | String         | 否     | 合约binary                 |
| 6    | bytecodeBin       | String         | 否     | 合约bin                    |
| 7    | contractId      | String         | 否     | 合约名称               |
| 8    | contractPath      | String         | 否     | 合约所在目录               |
| 9    | user              | String         | 否     | 私钥用户               |
| 10    | constructorParams | List | 是     | 构造函数入参               |
| 序号 | 输出参数          | 类型           |        | 备注                       |
| 1    | code            | Int           | 否     | 返回码，0：成功 其它：失败                      |
| 2    | message         | String        | 否     | 描述                                            |
| 3    |                 | Oject         |        | 返回信息实体                                    |
| 3.1  | contractId      | int           | 否     | 合约编号                                        |
| 3.2  | contractPath    | String        | 否     | 合约所在目录                              |
| 3.3  | contractName    | String        | 否     | 合约名称                                        |
| 3.4  | groupId         | Int           | 否     | 所属群组编号                                    |
| 3.5  | contractStatus  | int           | 否     | 1未部署，2已部署                        |
| 3.6  | contractType    | Int           | 否     | 合约类型(0-普通合约，1-系统合约)                |
| 3.7  | contractSource  | String        | 否     | 合约源码                                        |
| 3.8  | contractAbi     | String        | 是     | 编译合约生成的abi文件内容                       |
| 3.9  | contractBin     | String        | 是     | 合约binary                                      |
| 3.10 | bytecodeBin     | String        | 是     | 合约bin                                         |
| 3.11 | contractAddress | String        | 是     | 合约地址                                        |
| 3.12 | deployTime      | LocalDateTime | 是     | 部署时间                                        |
| 3.13 | contractVersion | String        | 否     | 合约版本（会去除该字段）               |
| 3.14 | description     | String        | 是     | 备注                                            |
| 3.15 | createTime      | LocalDateTime | 否     | 创建时间                                        |
| 3.16 | modifyTime      | LocalDateTime | 是     | 修改时间                                        |




#### 5.3.3 入参示例
`http://127.0.0.1:8080/WeBASE-Node-Manager/contract/deploy`
```
{
    "groupId": "1",
    "contractBin": "60806040526004361061004c576000357c010000002269b80029",
    "bytecodeBin": "60806040523480156100105761146031c79ef057dd274c87bff322ea2269b80029",
    "contractAbi": "[]",
    "contractSource": "cHJhZ21hIHNvbGlkaXR5IF4wLICAJbmFtZSA9IG47CiAgICB9Cn0=",
    "user": 700006,
    "contractName": "HeHe",
    "contractId": 200008,
    "contractPath": "Hi",
    "constructorParams": ["a"]
}
```


#### 5.3.4 出参示例

* 成功：
```
{
    "code": 0,
    "message": "success",
    "data": {
        "contractId": 200008,
        "contractPath": "Hi",
        "contractVersion": null,
        "contractName": "HeHe",
        "contractStatus": 2,
        "groupId": 1,
        "contractType": null,
        "contractSource": "cHJhZ21hIHNvbGlkaXR5IF4wLjQuM0=",
        "contractAbi": "[]",
        "contractBin": "60806040526004361061004c576000357c010274c87bff322ea2269b80029",
        "bytecodeBin": "608060405234801561001057629",
        "contractAddress": "0xa2ea2280b3a08a3ae2e1785dff09561e13915fb2",
        "deployTime": "2019-06-11 18:58:33",
        "description": null,
        "createTime": null,
        "modifyTime": null
    }
}
```


* 失败：
```
{
    "code": 102000,
    "message": "system exception",
    "data": {}
}
```

### 5.4 发送交易
[top](#目录)

#### 5.4.1 传输协议规范
* 网络传输协议：使用HTTP协议
* 请求地址：`/contract/transaction`
* 请求方式：POST
* 请求头：Content-type: application/json
* 返回格式：JSON

#### 5.4.2 参数信息详情

| 序号 | 输入参数     | 类型           | 可为空 | 备注                       |
|------|--------------|----------------|--------|----------------------------|
| 1    | groupId      | Int            | 否     | 所属群组编号               |
| 2    | user       | Integer        | 否     | 私钥用户               |
| 3    | contractName | String         | 否     | 合约名称                   |
| 4    | contractId      | String         | 否     | 合约名称               |
| 5    | funcName     | String         | 否     | 合约方法名                 |
| 6    | contractAddress     | String         | 是     | 合约地址（传合约名和版本时可为空）   |
| 7   | funcParam    | List | 是     | 合约方法入参               |
| 序号 | 输出参数     | 类型           |        | 备注                       |
| 1    | code         | Int            | 否     | 返回码，0：成功 其它：失败 |
| 2    | message      | String         | 否     | 描述                       |
| 3    | data         | object         | 是     | 返回信息实体（空）         |

#### 5.4.3 入参示例
`http://127.0.0.1:8080/WeBASE-Node-Manager/contract/transaction`
```
{
    "groupId": "300001",
    "contractBin": "6060604052600f8dee08980029",
    "bytecodeBin": null,
    "contractAbi": "[{\"constant\":false,\"inputs\":[{\"name\":\"num\",\"type\":\"uint256\"}],\"name\":\"trans\",\"outputs\":[],\"payable\":false,\"type\":\"function\"}]",
    "contractSource": "cHJhZ21hIHNvbGlkaXR5IF4wLjQuMjfQ==",
    "userId": 700001,
    "contractId": 200033
}
```


#### 5.4.4 出参示例
* 成功：
```
{
    "code": 0,
    "message": "success",
    "data": {}
}
```


* 失败：
```
{
    "code": 102000,
    "message": "system exception",
    "data": {}
}
```


### 5.5 根据包含bytecodeBin的字符串查询合约  
[top](#目录)

#### 5.2.1 传输协议规范
* 网络传输协议：使用HTTP协议
* 请求地址： `/contract/findByPartOfBytecodeBin`
* 请求方式：GET
* 返回格式：JSON

#### 5.2.2 参数信息详情

| 序号 | 输入参数        | 类型          | 可为空 | 备注                                            |
|------|-----------------|---------------|--------|-------------------------------------------------|
| 1    | groupId         | int           | 否     | 所属群组编号                                        |
| 2    | partOfBytecodeBin      | String           | 否     | 包含合约bytecodeBin的的字符串         |
| 序号 | 输出参数        | 类型          | 可为空 | 备注                                            |
| 1    | code            | Int           | 否     | 返回码，0：成功 其它：失败                      |
| 2    | message         | String        | 否     | 描述                                            |
| 3    |                 | Oject         |        | 返回信息实体                                    |
| 3.1  | contractId      | int           | 否     | 合约编号                                        |
| 3.2  | contractName    | String        | 否     | 合约名称                                        |
| 3.3  | groupId       | Int           | 否     | 所属群组编号                                      |
| 3.4  | contractType    | Int           | 否     | 合约类型(0-普通合约，1-系统合约)                |
| 3.5  | contractSource  | String        | 否     | 合约源码                                        |
| 3.6  | contractAbi     | String        | 是     | 编译合约生成的abi文件内容                       |
| 3.7  | contractBin     | String        | 是     | 合约binary                                      |
| 3.8 | bytecodeBin     | String        | 是     | 合约bin                                         |
| 3.9 | contractAddress | String        | 是     | 合约地址                                        |
| 3.10 | deployTime      | LocalDateTime | 是     | 部署时间                                        |
| 3.11 | contractVersion | String        | 否     | 合约版本                                        |
| 3.12 | description     | String        | 是     | 备注                                            |
| 3.13 | createTime      | LocalDateTime | 否     | 创建时间                                        |
| 3.14 | modifyTime      | LocalDateTime | 是     | 修改时间                                        |

#### 5.2.3 入参示例
`http://127.0.0.1:8080/WeBASE-Node-Manager/contract/findByPartOfBytecodeBin`
```
{
    "groupId": "300001",
    "partOfBytecodeBin": "abc123455dev"
}
```

#### 5.2.4 出参示例

* 成功：
```
{
    "code": 0,
    "message": "success",
    "data": {
        "contractId": 200002,
        "contractName": "Ok",
        "groupId": 2,
        "chainIndex": null,
        "contractType": 0,
        "contractSource": "cHJhZ21hIDQoNCg0KfQ==",
        "contractAbi": "[]",
        "contractBin": "60606040526000357c01000000000029",
        "bytecodeBin": "123455",
        "contractAddress": "0x19146d3a2f138aacb97ac52dd45dd7ba7cb3e04a",
        "deployTime": null,
        "contractVersion": "v6.0",
        "description": null,
        "createTime": "2019-04-15 21:14:40",
        "modifyTime": "2019-04-15 21:14:40"
    }
}
```


* 失败：
```
{
    "code": 102000,
    "message": "system exception",
    "data": {}
}
```





## 6 服务器监控相关
[top](#目录)

### 6.1 获取节点监控信息  
[top](#目录)

#### 6.1.1 传输协议规范
* 网络传输协议：使用HTTP协议
* 请求地址：`/chain/mointorInfo/{nodeId}?beginDate={beginDate}&endDate={endDate}&contrastBeginDate={contrastBeginDate}&contrastEndDate={contrastEndDate}&gap={gap}`
* 请求方式：GET
* 返回格式：JSON

#### 6.1.2 参数信息详情

| 序号      | 输入参数          | 类型            | 可为空 | 备注                                                           |
|-----------|-------------------|-----------------|--------|----------------------------------------------------------------|
| 1         | nodeId            | int             | 否     | 群组id                                                         |
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

#### 6.1.3 入参示例
`http://127.0.0.1:8080/webcaf-node-mgr/chain/mointorInfo/500001?gap=60&beginDate=2019-03-13T00:00:00&endDate=2019-03-13T14:34:22&contrastBeginDate=2019-03-13T00:00:00&contrastEndDate=2019-03-13T14:34:22`

#### 6.1.4 出参示例

* 成功：
```
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
```


* 失败：
```
{
    "code": 102000,
    "message": "system exception",
    "data": {}
}
```

### 6.2 获取服务器监控信息 
[top](#目录)

#### 6.2.1 传输协议规范
* 网络传输协议：使用HTTP协议
* 请求地址：`performance/ratio/{nodeId}?gap={gap}&beginDate={beginDate}&endDate={endDate}&contrastBeginDate={contrastBeginDate}&contrastEndDate={contrastEndDate}`
* 请求方式：GET
* 返回格式：JSON

#### 6.2.2 参数信息详情

| 序号      | 输入参数          | 类型            | 可为空 | 备注                                                           |
|-----------|-------------------|-----------------|--------|----------------------------------------------------------------|
| 1         | nodeId            | int             | 否     | 群组id                                                         |
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

#### 6.2.3 入参示例
`http://127.0.0.1:8080/WeBASE-Node-Manager/performance/ratio/500001?gap=1&beginDate=2019-03-15T00:00:00&endDate=2019-03-15T15:26:55&contrastBeginDate=2019-03-15T00:00:00&contrastEndDate=2019-03-15T15:26:55`

#### 6.2.4 出参示例
* 成功：
```
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
```


* 失败：
```
{
    "code": 102000,
    "message": "system exception",
    "data": {}
}
```


## 7 审计相关模块
[top](#目录)

### 7.1 获取用户交易监管信息列表
[top](#目录)

#### 7.1.1 传输协议规范
* 网络传输协议：使用HTTP协议
* 请求地址： `/monitor/userList/{groupId}`
* 请求方式：GET
* 返回格式：JSON

#### 10.1.2 参数信息详情

| 序号   | 输入参数         | 类型          | 可为空 | 备注                                          |
|--------|------------------|---------------|--------|-----------------------------------------------|
| 1      | groupId        | int           | 否     | 所属群组编号                                  |
| 序号   | 输出参数         | 类型          |        | 备注                                          |
| 1      | code             | Int           | 否     | 返回码，0：成功 其它：失败                    |
| 2      | message          | String        | 否     | 描述                                          |
| 3      | data             | List          | 是     | 信息列表                                      |
| 3.1    |                  | Object        | 是     | 监管信息对象                                  |
| 3.1.1  | userName         | String        | 是     | 用户名称                                      |
| 3.1.2  | userType         | Int           | 是     | 用户类型(0-正常，1-异常)                      |
| 3.1.3  | groupId        | Int           | 是     | 所属群组                                      |
| 3.1.4  | contractName     | String        | 是     | 合约名称                                      |
| 3.1.5  | contractAddress  | String        | 是     | 合约地址                                      |
| 3.1.6  | interfaceName    | String        | 是     | 合约接口名                                    |
| 3.1.7  | transType        | Int           | 是     | 交易类型(0-合约部署，1-接口调用)              |
| 3.1.8  | transUnusualType | Int           | 是     | 交易异常类型 (0-正常，1-异常合约，2-异常接口) |
| 3.1.9  | transCount       | Int           | 是     | 交易量                                        |
| 3.1.10 | transHashs       | String        | 是     | 交易hashs(最多5个)                            |
| 3.1.11 | transHashLastest | String        | 是     | 最新交易hash                                  |
| 3.1.12 | createTime       | LocalDateTime | 是     | 落库时间                                      |
| 3.1.13 | modifyTime       | LocalDateTime | 是     | 修改时间                                      |

#### 7.1.3 入参示例
`http://127.0.0.1:8080/WeBASE-Node-Manager/monitor/userList/300001`

#### 7.1.4 出参示例
* 成功：
```
{
    "code": 0,
    "message": "success",
    "data": [
        {
            "userName": "SYSTEMUSER",
            "userType": 0,
            "groupId": null,
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
            "groupId": null,
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
```

* 失败：
```
{
    "code": 102000,
    "message": "system exception",
    "data": {}
}
```

### 7.2 获取合约方法监管信息列表
[top](#目录)

#### 7.2.1 传输协议规范
* 网络传输协议：使用HTTP协议
* 请求地址：`/monitor/interfaceList/{groupId}?userName={userName}`
* 请求方式：GET
* 返回格式：JSON

#### 7.2.2 参数信息详情

| 序号   | 输入参数         | 类型          | 可为空 | 备注                                          |
|--------|------------------|---------------|--------|-----------------------------------------------|
| 1      | groupId        | int           | 否     | 所属群组编号                                  |
| 2      | userName         | String        | 是     | 用户名                                        |
| 序号   | 输出参数         | 类型          |        | 备注                                          |
| 1      | code             | Int           | 否     | 返回码，0：成功 其它：失败                    |
| 2      | message          | String        | 否     | 描述                                          |
| 3      | data             | List          | 是     | 信息列表                                      |
| 3.1    |                  | Object        | 是     | 监管信息对象                                  |
| 3.1.1  | userName         | String        | 是     | 用户名称                                      |
| 3.1.2  | userType         | Int           | 是     | 用户类型(0-正常，1-异常)                      |
| 3.1.3  | groupId        | Int           | 是     | 所属群组                                      |
| 3.1.4  | contractName     | String        | 是     | 合约名称                                      |
| 3.1.5  | contractAddress  | String        | 是     | 合约地址                                      |
| 3.1.6  | interfaceName    | String        | 是     | 合约接口名                                    |
| 3.1.7  | transType        | Int           | 是     | 交易类型(0-合约部署，1-接口调用)              |
| 3.1.8  | transUnusualType | Int           | 是     | 交易异常类型 (0-正常，1-异常合约，2-异常接口) |
| 3.1.9  | transCount       | Int           | 是     | 交易量                                        |
| 3.1.10 | transHashs       | String        | 是     | 交易hashs(最多5个)                            |
| 3.1.11 | transHashLastest | String        | 是     | 最新交易hash                                  |
| 3.1.12 | createTime       | LocalDateTime | 是     | 落库时间                                      |
| 3.1.13 | modifyTime       | LocalDateTime | 是     | 修改时间                                      |

#### 7.2.3 入参示例
`http://127.0.0.1:8080/WeBASE-Node-Manager/monitor/interfaceList/300001`

#### 7.2.4 出参示例
* 成功：
```
{
    "code": 0,
    "message": "success",
    "data": [
        {
            "userName": "SYSTEMUSER",
            "userType": 0,
            "groupId": null,
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
            "groupId": null,
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
```


* 失败：
```
{
    "code": 102000,
    "message": "system exception",
    "data": {}
}
```

### 7.3 获取交易hash监管信息列表
[top](#目录)

#### 7.3.1 传输协议规范
* 网络传输协议：使用HTTP协议
* 请求地址： `/monitor/interfaceList/{groupId}`
* 请求方式：GET
* 返回格式：JSON

#### 7.3.2 参数信息详情

| 序号    | 输入参数      | 类型           | 可为空 | 备注                       |
|---------|---------------|----------------|--------|----------------------------|
| 1       | groupId     | int            | 否     | 所属群组编号               |
| 2       | userName      | String         | 是     | 用户名                     |
| 3       | startDate     | String         |        | 开始时间                   |
| 4       | endDate       | String         |        | 结束时间                   |
| 5       | interfaceName | String         |        | 接口名称                   |
| 序号    | 输出参数      | 类型           |        | 备注                       |
| 1       | code          | Int            | 否     | 返回码，0：成功 其它：失败 |
| 2       | message       | String         | 否     | 描述                       |
| 3       | data          | Object         | 否     | 返回结果实体               |
| 3.1     | groupId     | Int            | 否     | 所属群组编号               |
| 3.2     | userName      | String         | 否     | 用户名                     |
| 3.3     | interfaceName | String         | 否     | 接口名                     |
| 3.4     | totalCount    | Int            | 否     | 总记录数                   |
| 3.5     | transInfoList | List\<Object\> | 是     | 交易信息列表               |
| 3.5.1   |               | Object         | 是     | 交易信息实体               |
| 3.5.1.1 | transCount    | Int            | 是     | 交易记录数                 |
| 3.5.1.2 | time          | LcalDateTime   | 是     | 时间                       |

#### 7.3.3 入参示例
`http://127.0.0.1:8080/WeBASE-Node-Manager/monitor/transList/300001?userName=0x5d97f8d41638a7b1b669b70b307bab6d49df8e2c&interfaceName=0x4ed3885e`

#### 7.3.4 出参示例
* 成功：
```
{
    "code": 0,
    "message": "success",
    "data": {
        "groupId": 300001,
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
```


* 失败：
```
{
    "code": 102000,
    "message": "system exception",
    "data": {}
}
```

### 7.4 获取异常用户信息列表
[top](#目录)

#### 7.4.1 传输协议规范
* 网络传输协议：使用HTTP协议
* 请求地址：`/unusualUserList/{groupId}/{pageNumber}/{pageSize}?userName={userName}`
* 请求方式：GET
* 返回格式：JSON

#### 7.4.2 参数信息详情

| 序号  | 输入参数   | 类型          | 可为空 | 备注                       |
|-------|------------|---------------|--------|----------------------------|
| 1     | groupId  | int           | 否     | 所属群组编号               |
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

#### 7.4.3 入参示例
`http://127.0.0.1:8080/WeBASE-Node-Manager/monitor/unusualUserList/300001/1/10?userName=`

#### 7.4.4 出参示例
* 成功：
```
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
```

* 失败：
```
{
    "code": 102000,
    "message": "system exception",
    "data": {}
}
```

### 7.5 获取异常合约信息列表
[top](#目录)

#### 7.5.1 传输协议规范
* 网络传输协议：使用HTTP协议
* 请求地址：`/unusualContractList/{groupId}/{pageNumber}/{pageSize}?contractAddress={contractAddress}`
* 请求方式：GET
* 返回格式：JSON

#### 7.5.2 参数信息详情

| 序号  | 输入参数        | 类型          | 可为空 | 备注                       |
|-------|-----------------|---------------|--------|----------------------------|
| 1     | groupId       | int           | 否     | 所属群组编号               |
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

#### 7.5.3 入参示例
`http://127.0.0.1:8080/WeBASE-Node-Manager/monitor/unusualContractList/300001/1/10?contractAddress=`

#### 7.5.4 出参示例
```

```
* 成功：
```
{
    "code": 0,
    "message": "success",
    "totalCount": 1,
    "data": [
        {
            "contractName": "0x00000000",
            "contractAddress": "0x0000000000000000000000000000000000000000",
            "transCount": 3,
            "hashs": "0xc87e306db85740895369cc2a849984fe544a6e9b0ecdbd2d898fc0756a02a4ce",
            "time": "2019-03-13 15:41:56"
        }
    ]
}
```

* 失败：
```
{
    "code": 102000,
    "message": "system exception",
    "data": {}
}
```

## 8 群组信息模块
[top](#目录)

### 8.1 获取群组概况
[top](#目录)

#### 8.1.1 传输协议规范
* 网络传输协议：使用HTTP协议
* 请求地址： `/group/general/{groupId}`
* 请求方式：GET
* 返回格式：JSON

#### 8.1.2 参数信息详情

| 序号 | 输入参数         | 类型   | 可为空 | 备注                       |
|------|------------------|--------|--------|----------------------------|
| 1    | groupId          | int    | 否     | 群组id                     |
| 序号 | 输出参数         | 类型   |        | 备注                       |
| 1    | code             | Int    | 否     | 返回码，0：成功 其它：失败 |
| 2    | message          | String | 否     | 描述                       |
| 3    | data             | object | 否     | 返回信息实体               |
| 3.1  | groupId          | int    | 否     | 群组id                     |
| 3.2  | nodeCount        | int    | 否     | 节点数量                   |
| 3.3  | contractCount    | int    | 否     | 已部署智能合约数量         |
| 3.4  | transactionCount | int    | 否     | 交易数量                   |
| 3.5  | latestBlock      | int    | 否     | 当前块高                   |

#### 8.1.3 入参示例
`http://127.0.0.1:8080/WeBASE-Node-Manager/group/300001`

#### 8.1.4 出参示例
* 成功：
```
{
    "code": 0,
    "data": {
        "latestBlock": 7156,
        "contractCount": 0,
        "groupId": "300001",
        "nodeCount": 2,
        "transactionCount": 7131
    },
    "message": "Success"
}
```


* 失败：
```
{
    "code": 102000,
    "message": "system exception",
    "data": {}
}
```

### 8.2 获取所有群组列表
[top](#目录)

#### 8.2.1 传输协议规范
* 网络传输协议：使用HTTP协议
* 请求地址： `/group/all`
* 请求方式：GET
* 返回格式：JSON

#### 8.2.2 参数信息详情

| 序号  | 输入参数      | 类型          | 可为空 | 备注                       |
|-------|---------------|---------------|--------|----------------------------|
|       |               |               |        |                            |
| 序号  | 输出参数      | 类型          |        | 备注                       |
| 1     | code          | Int           | 否     | 返回码，0：成功 其它：失败 |
| 2     | message       | String        | 否     | 描述                       |
| 3     | totalCount    | Int           | 否     | 总记录数                   |
| 4     | data          | List          | 否     | 组织列表                   |
| 4.1   |               | Object        |        | 组织信息对象               |
| 4.1.1 | groupId       | int           | 否     | 群组编号                   |
| 4.1.2 | groupName     | String        | 否     | 群组名称                   |
| 4.1.3 | latestBlock   | BigInteger    | 否     | 最新块高                   |
| 4.1.4 | transCount    | BigInteger    | 否     | 交易量                     |
| 4.1.5 | createTime    | LocalDateTime | 否     | 落库时间                   |
| 4.1.6 | modifyTime    | LocalDateTime | 否     | 修改时间                   |

#### 8.2.3 入参示例
`http://127.0.0.1:8080/WeBASE-Node-Manager/group/all`

#### 8.2.4 出参示例
* 成功：
```
{
    "code": 0,
    "message": "success",
    "totalCount": 1,
    "data": [
        {
            "groupId": 300001,
            "groupName": "group1",
            "latestBlock": 133,
            "transCount": 133,
            "createTime": "2019-02-14 17:33:50",
            "modifyTime": "2019-03-15 09:36:17"
        }
    ]
}
```

* 失败：
```
{
    "code": 102000,
    "message": "system exception",
    "data": {}
}
```

### 8.3 查询每日交易数据
[top](#目录)

#### 8.3.1 传输协议规范
* 网络传输协议：使用HTTP协议
* 请求地址：`/group/transDaily/{groupId}`
* 请求方式：GET
* 返回格式：JSON

#### 8.3.2 参数信息详情

| 序号 | 输入参数   | 类型   | 可为空 | 备注                       |
|------|------------|--------|--------|----------------------------|
| 1    | groupId    | int    | 否     | 群组id                     |
| 序号 | 输出参数   | 类型   |        | 备注                       |
| 1    | code       | Int    | 否     | 返回码，0：成功 其它：失败 |
| 2    | message    | String | 否     | 描述                       |
| 3    | data       | list   | 否     | 返回信息列表               |
| 3.1  |            | object |        | 返回信息实体               |
| 4.1  | day        | string | 否     | 日期YYYY-MM-DD             |
| 4.2  | groupId    | int    | 否     | 群组编号                   |
| 4.3  | transCount | int    | 否     | 交易数量                   |

#### 8.3.3 入参示例
`http://127.0.0.1:8080/WeBASE-Node-Manager/group/transDaily/300001`

#### 8.3.4 出参示例

* 成功：
```
{
    "code": 0,
    "data": [
        {
            "day": "2018-11-21",
            "groupId": "300001",
            "transCount": 12561
        },
        {
            "day": "2018-11-22",
            "groupId": "300001",
            "transCount": 1251
        }
    ],
    "message": "Success"
}
```


* 失败：
```
{
    "code": 102000,
    "message": "system exception",
    "data": {}
}
```





## 9 节点管理模块
[top](#目录)

### 9.1 查询节点列表
[top](#目录)

#### 9.1.1 传输协议规范
* 网络传输协议：使用HTTP协议
* 请求地址:`/node/nodeList/{groupId}/{pageNumber}/{pageSize}?nodeName={nodeName}`
* 请求方式：GET
* 返回格式：JSON

#### 9.1.2 参数信息详情

| 序号   | 输入参数    | 类型          | 可为空 | 备注                                       |
|--------|-------------|---------------|--------|--------------------------------------------|
| 1      | groupId   | int           | 否     | 群组id                                     |
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
| 4.1.3  | groupId     | int           | 否     | 所属群组编号                               |
| 4.1.4  | nodeActive  | int           | 否     | 状态                                       |
| 4.1.5  | nodeIp      | string        | 否     | 节点ip                                     |
| 4.1.6  | P2pPort     | int           | 否     | 节点p2p端口                                |
| 4.1.7 | description | String        | 否     | 备注                                       |
| 4.1.8 | blockNumber | BigInteger    | 否     | 节点块高                                   |
| 4.1.9 | pbftView    | BigInteger    | 否     | Pbft view                                  |
| 4.1.10 | createTime  | LocalDateTime | 否     | 落库时间                                   |
| 4.1.11 | modifyTime  | LocalDateTime | 否     | 修改时间                                   |

#### 9.1.3 入参示例
`http://127.0.0.1:8080/WeBASE-Node-Manager/node/nodeList/300001/1/10?nodeName=`

#### 9.1.4 出参示例
* 成功：
```
{
    "code": 0,
    "message": "success",
    "totalCount": 1,
    "data": [
        {
            "nodeId": 500001,
            "nodeName": "127.0.0.1_10303",
            "groupId": 300001,
            "nodeIp": "127.0.0.1",
            "p2pPort": 10303,
            "description": null,
            "blockNumber": 133,
            "pbftView": 5852,
            "nodeActive": 1,
            "createTime": "2019-02-14 17:47:00",
            "modifyTime": "2019-03-15 11:14:29"
        }
    ]
}
```

* 失败：
```
{
    "code": 102000,
    "message": "system exception",
    "data": {}
}
```

### 9.2 查询节点信息
[top](#目录)

#### 9.2.1 传输协议规范
* 网络传输协议：使用HTTP协议
* 请求地址: `/node/nodeInfo/{groupId}?nodeType={nodeType}`
* 请求方式：GET
* 返回格式：JSON

#### 9.2.2 参数信息详情

| 序号 | 输入参数    | 类型          | 可为空 | 备注                                       |
|------|-------------|---------------|--------|--------------------------------------------|
| 1    | groupId     | int           | 否     | 群组id                                     |
|      | 输出参数    | 类型          |        | 备注                                       |
| 1    | code        | Int           | 否     | 返回码，0：成功 其它：失败                 |
| 2    | message     | String        | 否     | 描述                                       |
| 3    |             | Object        |        | 节点信息对象                               |
| 3.1  | nodeId      | int           | 否     | 节点编号                                   |
| 3.2  | nodeName    | string        | 否     | 节点名称                                   |
| 3.3  | groupId     | int           | 否     | 所属群组编号                               |
| 3.4  | nodeActive  | int           | 否     | 状态                                       |
| 3.5  | nodeIp      | string        | 否     | 节点ip                                     |
| 3.6  | P2pPort     | int           | 否     | 节点p2p端口                                |
| 3.7 | description | String        | 否     | 备注                                       |
| 3.8 | blockNumber | BigInteger    | 否     | 节点块高                                   |
| 3.9 | pbftView    | BigInteger    | 否     | Pbft view                                  |
| 3.10 | createTime  | LocalDateTime | 否     | 落库时间                                   |
| 3.11 | modifyTime  | LocalDateTime | 否     | 修改时间                                   |

#### 9.2.3 入参示例
`http://127.0.0.1:8080/WeBASE-Node-Manager/node/nodeInfo/{groupId}

#### 9.2.4 出参示例
* 成功：
```
{
    "code": 0,
    "message": "success",
    "data": {
        "nodeId": 500001,
        "nodeName": "127.0.0.1_10303",
        "groupId": 300001,
        "nodeIp": "127.0.0.1",
        "p2pPort": 10303,
        "description": null,
        "blockNumber": 133,
        "pbftView": 5852,
        "nodeActive": 1,
        "createTime": "2019-02-14 17:47:00",
        "modifyTime": "2019-03-15 11:14:29"
    }
}
```

* 失败：
```
{
    "code": 102000,
    "message": "system exception",
    "data": {}
}
```

## 10 角色管理模块
[top](#目录)

### 10.1 查询角色列表
[top](#目录)

### 10.1.1 传输协议规范
* 网络传输协议：使用HTTP协议
* 请求地址：`role/roleList`
* 请求方式：GET
* 返回格式：JSON

### 10.1.2 参数信息详情

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

### 10.1.3 入参示例
`http://127.0.0.1:8080/WeBASE-Node-Manager/role/roleList?groupId=300001&pageNumber=&pageSize=&roleId=&roleName=`

### 10.1.4 出参示例

* 成功：
```
{
    "code": 0,
    "message": "success",
    "totalCount": 2,
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
    ]
}
```


* 失败：
```
{
    "code": 102000,
    "message": "system exception",
    "data": {}
}
```

## 11 用户管理模块 
[top](#目录)

### 11.1 新增私钥用户
[top](#目录)

#### 11.1.1 传输协议规范
* 网络传输协议：使用HTTP协议
* 请求地址： `/user/userInfo`
* 请求方式：POST
* 请求头：Content-type: application/json
* 返回格式：JSON

#### 11.1.2 参数信息详情

| 序号 | 输入参数    | 类型          | 可为空 | 备注                               |
|------|-------------|---------------|--------|------------------------------------|
| 1    | userName    | string        | 否     | 用户名称                           |
| 2    | description | string        | 是     | 备注                               |
| 3    | groupId     | Int           | 否     | 所属群组                           |
| 序号 | 输出参数    | 类型          |        | 备注                               |
| 1    | code        | Int           | 否     | 返回码，0：成功 其它：失败         |
| 2    | message     | String        | 否     | 描述                               |
| 3    | data        | object        | 是     | 返回信息实体（成功时不为空）       |
| 3.1  | userId      | int           | 否     | 用户编号                           |
| 3.2  | userName    | string        | 否     | 用户名称                           |
| 3.3  | groupId     | int           | 否     | 所属群组编号                       |
| 3.4  | description | String        | 是     | 备注                               |
| 3.5  | userStatus  | int           | 否     | 状态（1-正常 2-停用） 默认1        |
| 3.6  | publicKey   | String        | 否     | 公钥信息                           |
| 3.7  | address     | String        | 是     | 在链上位置的hash                   |
| 3.8  | hasPk       | Int           | 否     | 是否拥有私钥信息(1-拥有，2-不拥有) |
| 3.9  | createTime  | LocalDateTime | 否     | 创建时间                           |
| 3.10 | modifyTime  | LocalDateTime | 否     | 修改时间                           |

#### 11.1.3 入参示例
`v/WeBASE-Node-Manager/user/userInfo`

```
{
    "groupId": "300001",
    "description": "密钥拥有者",
    "userName": "user1"
}
```


#### 11.1.4 出参示例
* 成功：
```
{
    "code": 0,
    "message": "success",
    "data": {
        "userId": 700007,
        "userName": "asdfvw",
        "groupId": 300001,
        "publicKey": "0x4189fdacff55fb99172e015e1adc360777bee6682fcc975238aabf144fbf610a3057fd4b5",
        "userStatus": 1,
        "userType": 1,
        "address": "0x40ec3c20b5178401ae14ad8ce9c9f94fa5ebb86a",
        "hasPk": 1,
        "description": "sda",
        "createTime": "2019-03-15 18:00:27",
        "modifyTime": "2019-03-15 18:00:27"
    }
}
```


* 失败：
```
{
    "code": 102000,
    "message": "system exception",
    "data": {}
}
```

### 11.2 绑定公钥用户
[top](#目录)

#### 11.2.1 传输协议规范
* 网络传输协议：使用HTTP协议
* 请求地址： `/user/bind`
* 请求方式：POST
* 请求头：Content-type: application/json
* 返回格式：JSON

#### 11.2.2 参数信息详情

| 序号 | 输入参数    | 类型          | 可为空 | 备注                               |
|------|-------------|---------------|--------|------------------------------------|
| 1    | userName    | string        | 否     | 用户名称                           |
| 2    | description | string        | 是     | 备注                               |
| 3    | groupId   | Int           | 否     | 所属群组                           |
| 4    |             |               |        |                                    |
| 序号 | 输出参数    | 类型          |        | 备注                               |
| 1    | code        | Int           | 否     | 返回码，0：成功 其它：失败         |
| 2    | message     | String        | 否     | 描述                               |
| 3    | data        | object        | 是     | 返回信息实体（成功时不为空）       |
| 3.1  | userId      | int           | 否     | 用户编号                           |
| 3.2  | userName    | string        | 否     | 用户名称                           |
| 3.3  | groupId     | int           | 否     | 所属群组编号                       |
| 3.4  | description | String        | 是     | 备注                               |
| 3.5  | userStatus  | int           | 否     | 状态（1-正常 2-停用） 默认1        |
| 3.6  | publicKey   | String        | 否     | 公钥信息                           |
| 3.7  | address     | String        | 是     | 在链上位置的hash                   |
| 3.8  | hasPk       | Int           | 否     | 是否拥有私钥信息(1-拥有，2-不拥有) |
| 3.9  | createTime  | LocalDateTime | 否     | 创建时间                           |
| 3.10 | modifyTime  | LocalDateTime | 否     | 修改时间                           |

#### 11.2.3 入参示例
`http://127.0.0.1:8080/WeBASE-Node-Manager/user/userInfo`

```
{
    "userName": "sdfasd",
    "publicKey": "0x4189fdacff55fb99172e015e1adb96dc77b0cae1619b1a41cc360777bee6682fcc9752d8aabf144fbf610a3057fd4b5",
    "groupId": "300001",
    "description": "sdfa"
}
```

#### 11.2.4 出参示例
* 成功：
```
{
    "code": 0,
    "message": "success",
    "data": {
        "userId": 700007,
        "userName": "asdfvw",
        "groupId": 300001,
        "publicKey": "0x4189fdacff55fb99172e015e1adb96dc77b0cae1619b1a41cc360777bee6682fcc9752d8aabf144fbf610a3057fd4b5",
        "userStatus": 1,
        "userType": 1,
        "address": "0x40ec3c20b5178401ae14ad8ce9c9f94fa5ebb86a",
        "hasPk": 1,
        "description": "sda",
        "createTime": "2019-03-15 18:00:27",
        "modifyTime": "2019-03-15 18:00:27"
    }
}
```


* 失败：
```
{
    "code": 102000,
    "message": "system exception",
    "data": {}
}
```

### 11.3 修改用户备注
[top](#目录)

#### 11.3.1 传输协议规范
* 网络传输协议：使用HTTP协议
* 请求地址：`/user/userInfo`
* 请求方式：PUT
* 请求头：Content-type: application/json
* 返回格式：JSON

#### 11.3.2 参数信息详情

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
| 3.3  | groupId     | int           | 否     | 所属群组编号                       |
| 3.4  | description | String        | 是     | 备注                               |
| 3.5  | userStatus  | int           | 否     | 状态（1-正常 2-停用） 默认1        |
| 3.6  | publicKey   | String        | 否     | 公钥信息                           |
| 3.7  | address     | String        | 是     | 在链上位置的hash                   |
| 3.8  | hasPk       | Int           | 否     | 是否拥有私钥信息(1-拥有，2-不拥有) |
| 3.9  | createTime  | LocalDateTime | 否     | 创建时间                           |
| 3.10 | modifyTime  | LocalDateTime | 否     | 修改时间                           |

#### 11.3.3 入参示例
`http://127.0.0.1:8080/WeBASE-Node-Manager/user/userInfo`

```
{
    "userId": "400001",
    "description": "newDescription"
}
```

#### 11.3.4 出参示例

* 成功：
```
{
    "code": 0,
    "message": "success",
    "data": {
        "userId": 400001,
        "userName": "asdfvw",
        "groupId": 300001,
        "publicKey": "0x4189fdacff55fb99172e015e1682fcc9752d8aabf144fbf610a3057fd4b5",
        "userStatus": 1,
        "userType": 1,
        "address": "0x40ec3c20b5178401ae14ad8ce9c9f94fa5ebb86a",
        "hasPk": 1,
        "description": "newDescription",
        "createTime": "2019-03-15 18:00:27",
        "modifyTime": "2019-03-15 18:00:27"
    }
}
```


* 失败：
```
{
    "code": 102000,
    "message": "system exception",
    "data": {}
}
```

### 11.4 查询私钥
[top](#目录)

#### 11.4.1 传输协议规范
* 网络传输协议：使用HTTP协议
* 请求地址：`/user/privateKey/{userId}`
* 请求方式：GET
* 返回格式：json

#### 11.4.2 参数信息详情

| 序号 | 输入参数   | 类型   | 可为空 | 备注                       |
|------|------------|--------|--------|----------------------------|
| 1    | userId     | int    | 否     | 用户编号                   |
| 序号 | 输出参数   | 类型   |        | 备注                       |
| 1    | code       | Int    | 否     | 返回码，0：成功 其它：失败 |
| 2    | message    | String | 否     | 描述                       |
| 3    | data       | Object | 否     | 返回私钥信息实体           |
| 3.1  | privateKey | String | 否     | 私钥                       |
| 3.2  | address    | String | 否     | 用户链上地址               |

#### 11.4.3 入参示例
`http://127.0.0.1:8080/WeBASE-Node-Manager/user/privateKey/4585`

#### 11.4.4 出参示例

* 成功：
```
{
    "code": 0,
    "message": "success",
    "data": {
        "privateKey": 123456,
        "address": "asfsafasfasfasfasfas"
    }
}
```


* 失败：
```
{
    "code": 102000,
    "message": "system exception",
    "data": {}
}
```

### 11.5 查询用户列表
[top](#目录)

#### 11.5.1 传输协议规范
* 网络传输协议：使用HTTP协议
* 请求地址:`/user/userList/{groupId}/{pageNumber}/{pageSize}?userParam={userName}`
* 请求方式：GET
* 返回格式：JSON

#### 11.5.2 参数信息详情

| 序号   | 输入参数    | 类型          | 可为空 | 备注                               |
|--------|-------------|---------------|--------|------------------------------------|
| 1      | groupId   | int           | 否     | 所属群组id                           |
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
| 4.1.3  | groupId     | int           | 否     | 所属群组编号                       |
| 4.1.4  | description | String        | 是     | 备注                               |
| 4.1.5  | userStatus  | int           | 否     | 状态（1-正常 2-停用） 默认1        |
| 4.1.6  | publicKey   | String        | 否     | 公钥信息                           |
| 4.1.7  | address     | String        | 是     | 在链上位置的hash                   |
| 4.1.8  | hasPk       | Int           | 否     | 是否拥有私钥信息(1-拥有，2-不拥有) |
| 4.1.9  | createTime  | LocalDateTime | 否     | 创建时间                           |
| 4.1.10 | modifyTime  | LocalDateTime | 否     | 修改时间                           |

#### 11.5.3 入参示例
`http://127.0.0.1:8080/WeBASE-Node-Manager/user/userList/300001/1/10?userParam=asdfvw`

#### 11.5.4 出参示例

* 成功：
```
{
    "code": 0,
    "message": "success",
    "totalCount": 1,
    "data": [
        {
            "userId": 700007,
            "userName": "asdfvw",
            "groupId": 300001,
            "publicKey": "0x4189fdacff55fb99172e015e1adb96dc71cc360777bee6682fcc975238aabf144fbf610a3057fd4b5",
            "userStatus": 1,
            "userType": 1,
            "address": "0x40ec3c20b5178401ae14ad8ce9c9f94fa5ebb86a",
            "hasPk": 1,
            "description": "sda",
            "createTime": "2019-03-15 18:00:27",
            "modifyTime": "2019-03-15 18:00:28"
        }
    ]
}
```


* 失败：
```
{
    "code": 102000,
    "message": "system exception",
    "data": {}
}
```





## 12 合约方法管理模块 
[top](#目录)

### 12.1 新增合约方法 
[top](#目录)

#### 12.1.1 传输协议规范
* 网络传输协议：使用HTTP协议
* 请求地址： `/method/add`
* 请求方式：POST
* 请求头：Content-type: application/json
* 返回格式：JSON

#### 12.1.2 参数信息详情

| 序号 | 输入参数    | 类型          | 可为空 | 备注                               |
|------|-------------|---------------|--------|------------------------------------|
| 1    | groupId     | Int           | 否     | 所属群组                           |
| 2    | methodList  | List           | 否     | 方法列表                           |
| 2.1  |             | Object           | 否     | 方法实体                           |
| 2.1.1 | abiInfo    | String        | 否     | 合约abi信息                           |
| 2.1.2 | methodId   | String        | 否     | 方法编号                           |
| 2.1.3 | methodType | String        | 否     | 方法类型                           |
| 序号 | 输出参数    | 类型          |        | 备注                               |
| 1    | code        | Int           | 否     | 返回码，0：成功 其它：失败         |
| 2    | message     | String        | 否     | 描述                               |
| 3    | data        | object        | 是     | 返回信息实体（空）       |


#### 12.1.3 入参示例
`http://127.0.0.1:8080/WeBASE-Node-Manager/method/add`

```
{
    "groupId": 2,
    "methodList": [
        {
            "abiInfo": "fsdabiTestfd232222",
            "methodId": "methodIasdfdttttt",
            "methodType": "function"
        }
    ]
}
```


#### 12.1.4 出参示例
* 成功：
```
{
    "code": 0,
    "message": "success",
    "data": null
}
```


* 失败：
```
{
    "code": 102000,
    "message": "system exception",
    "data": {}
}
```




### 12.2 根据方法编号查询
[top](#目录)

#### 12.1.1 传输协议规范
* 网络传输协议：使用HTTP协议
* 请求地址： `/method/findById/{groupId}/{methodId}`
* 请求方式：POST
* 请求头：Content-type: application/json
* 返回格式：JSON

#### 12.1.2 参数信息详情

| 序号 | 输入参数    | 类型          | 可为空 | 备注                               |
|------|-------------|---------------|--------|------------------------------------|
| 1    | groupId     | Int           | 否     | 所属群组                           |
| 2    | methodId    | String        | 否     | 方法编号                           |
| 序号  | 输出参数     | 类型           |       | 备注                               |
| 1    | code        | Int           | 否     | 返回码，0：成功 其它：失败            |
| 2    | message     | String        | 否     | 描述                               |
| 3    | data        | List          | 是     | 返回信息实体                        |


#### 12.1.3 入参示例
`http://127.0.0.1:8080/WeBASE-Node-Manager/method/findById/2/methodIasdfdttttt`


#### 12.1.4 出参示例
* 成功：
```
{
    "code": 0,
    "message": "success",
    "data": {
        "methodId": "methodIasdfdttttt",
        "groupId": 2,
        "abiInfo": "fsdabiTestfd232222",
        "methodType": "function",
        "createTime": "2019-04-16 16:59:27",
        "modifyTime": "2019-04-16 16:59:27"
    }
}
```


* 失败：
```
{
    "code": 102000,
    "message": "system exception",
    "data": {}
}
```


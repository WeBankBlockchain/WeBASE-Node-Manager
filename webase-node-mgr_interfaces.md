# 区块链平台节点管理接口设计

## 1 <span id="1">目录</span>
- [2.交易信息模块](#2)
  - [2.1.查询交易信息列表](#2.1)
- [3.帐号管理模块](#3)
  - [3.1.新增帐号](#3.1)
  - [3.2.修改帐号](#3.2)
  - [3.3.删除帐号](#3.3)
  - [3.4.查询帐号列表](#3.4)
  - [3.5.更改当前密码](#3.5)
- [4.区块管理模块](#4)
  - [4.1.查询区块列表](#4.1)
- [5.合约管理模块](#5)
  - [5.1.新增合约](#5.1)
  - [5.2.修改合约](#5.2)
  - [5.3.删除合约](#5.3)
  - [5.4.查询合约列表](#5.4)
  - [5.5.查询合约信息](#5.5)
  - [5.6.部署合约](#5.6)
  - [5.7.发送交易](#5.7)
- [6.服务器监控相关](#6)
  - [6.1.获取节点监控信息](#6.1)
  - [6.2.获取服务器监控信息](#6.2)
- [7.审计相关模块](#7)
  - [7.1.获取节点日志列表](#7.1)
  - [7.2.获取合约方法监管信息列表](#7.2)
  - [7.3.获取交易hash监管信息列表](#7.3)
  - [7.4.获取异常用户信息列表](#7.4)
  - [7.5.获取异常合约信息列表](#7.5)
- [8.群组信息模块](#8)
  - [8.1.获取群组概况](#8.1)
  - [8.2.获取所有群组列表](#8.2)
  - [8.3.查询每日交易数据](#8.3)
  - [8.4.修改群组名称](#8.4)
- [9.节点管理模块](#9)
  - [9.1.查询节点列表](#9.1)
  - [9.2.查询节点信息](#9.2)
- [10.角色管理模块](#10)
  - [10.1.查询角色列表](#10.1)
- [11.用户管理模块](#11)
  - [11.1.新增私钥用户](#11.1)
  - [11.2.绑定公钥用户](#11.2)
  - [11.3.修改用户备注](#11.3)
  - [11.4.查询私钥](#11.4)
  - [11.5.查询用户列表](#11.5)
- [12.文件管理模块](#12)
  - [12.1.文件上传](#12.1)
- [13.前置管理模块](#13) 
  - [13.1.新增节点前置信息](#13.1)
  - [13.2.获取所有前置列表](#13.2)
  - [13.3.删除前置信息](#13.3)  


## <span id="2">2 交易信息模块</span>  [top](#1)

### <span id="2.1">2.1 查询交易信息列表</span>  [top](#1)


#### 2.1.1 传输协议规范

* 网络传输协议：使用HTTP协议
* 请求地址：`/transList/{groupId}/{pageNumber}/{pageSize}?transactionHash={transactionHash}&blockNumber={blockNumber}`
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
http://127.0.0.1:8080/webase-node-mgr/transaction/transList/300001/1/10?transactionHash=0x303daa78ebe9e6f5a6d9761a8eab4bf5a0ed0b06c28764488e4716de42e1df01
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

## <span id="3">3 帐号管理模块</span>  [top](#1)

### <span id="3.1">3.1 新增帐号</span>  [top](#1)

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

### 3.1.3 入参示例

`http://127.0.0.1:8080/webase-node-mgr/account/accountInfo`
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


### <span id="3.2">3.2 修改帐号</span>  [top](#1)

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

`http://127.0.0.1:8080/webase-node-mgr/account/accountInfo`
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

### <span id="3.3">3.3 删除帐号</span>  [top](#1)

#### 3.3.1 传输协议规范
* 网络传输协议：使用HTTP协议
* 请求地址：`/account/{account}`
* 请求方式：DELETE
* 返回格式：JSON

#### 3.3.2 参数信息详情

| 序号 | 输入参数 | 类型   | 可为空 | 备注                       |
|------|----------|--------|--------|----------------------------|
| 1    | account  | String | 否     | 帐号名称                   |
| 序号 | 输出参数 | 类型   |        | 备注                       |
| 1    | code     | Int    | 否     | 返回码，0：成功 其它：失败 |
| 2    | message  | String | 否     | 描述                       |
| 3    | data     | object | 是     | 返回信息实体（空）         |

#### 3.3.3 入参示例
`http://127.0.0.1:8080/webase-node-mgr/account/testAccount`

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

###  <span id="3.4">3.4 查询帐号列表</span>  [top](#1)

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
`http://127.0.0.1:8080/webase-node-mgr/account/accountList/1/10?account=`

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

### <span id="3.5">3.5 更新当前密码</span>  [top](#1)

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
`http://127.0.0.1:8080/webase-node-mgr/account/passwordUpdate`
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

## <span id="4">4 区块管理模块</span>  [top](#1)

### <span id="4.1">4.1 查询区块列表</span>  [top](#1)

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
| 4.1.2 | groupId        | int           | 否     | 所属群组编号               |
| 4.1.3 | blockNumber    | BigInteger    | 否     | 块高                       |
| 4.1.4 | blockTimestamp | LocalDateTime | 否     | 出块时间                   |
| 4.1.5 | transCount     | int           | 否     | 交易数                     |
| 4.1.6 | createTime     | LocalDateTime | 否     | 创建时间                   |
| 4.1.7 | modifyTime     | LocalDateTime | 否     | 修改时间                   |

#### 4.1.3 入参示例
`http://127.0.0.1:8080/webase-node-mgr/block/blockList/300001/1/10?pkHash=`

#### 4.1.4 出参示例
* 成功：
```
{
    "code": 0,
    "message": "success",
    "totalCount": 2,
    "data": [
        {
            "pkHash": "0xe6438646633542e26d053f75931d74a258a607464207e1343344c100da89e661",
            "groupId": 300001,
            "blockNumber": 1442,
            "blockTimestamp": "2019-02-27 19:18:23",
            "transCount": 1,
            "createTime": "2019-03-04 10:29:07",
            "modifyTime": "2019-03-04 10:29:07"
        },
        {
            "pkHash": "0x2e036eba6d1581a280712276e06517987c7be40f0f252fca34303eef157d8c3d",
            "groupId": 300001,
            "blockNumber": 1441,
            "blockTimestamp": "2019-02-27 19:18:22",
            "transCount": 1,
            "createTime": "2019-03-04 10:29:07",
            "modifyTime": "2019-03-04 10:29:07"
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

## <span id="5">5 合约管理模块</span>  [top](#1)

### <span id="5.1">5.1 新增合约</span>  [top](#1)

#### 5.1.1 传输协议规范
* 网络传输协议：使用HTTP协议
* 请求地址：`/deployIncoming/contractInfo`
* 请求方式：POST
* 请求头：Content-type: application/json
* 返回格式：JSON

#### 5.1.2 参数信息详情

| 序号 | 输入参数        | 类型          | 可为空 | 备注                                            |
|------|-----------------|---------------|--------|-------------------------------------------------|
| 1    | groupId       | int           | 否     | 所属群组编号                                    |
| 2    | contractName    | string        | 否     | 合约名称                                        |
| 3    | contractVersion | String        | 否     | 合约版本                                        |
| 4    | contractSource  | String        | 否     | 源码（base64）                                  |
| 序号 | 输出参数        | 类型          |        | 备注                                            |
| 1    | code            | Int           | 否     | 返回码，0：成功 其它：失败                      |
| 2    | message         | String        | 否     | 描述                                            |
| 3    | data            | object        |        | 返回信息实体（成功时不为空）                    |
| 3.1  | contractId      | int           | 否     | 合约编号                                        |
| 3.2  | contractName    | String        | 否     | 合约名称                                        |
| 3.3  | groupId       | Int           | 否     | 所属群组编号                                      |
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

#### 5.1.3 入参示例

`http://127.0.0.1:8080/webase-node-mgr/deployIncoming`
```
{
    "groupId": "300001",
    "contractName": "Helllo",
    "contractVersion": "v1.0",
    "contractSource": "cHJhZ21hIHNvbGlkaXR5IF4wLjQuMjsN"
}
```


#### 5.1.4 出参示例
* 成功：
```
{
    "code": 0,
    "message": "success",
    "data": {
        "contractId": 200035,
        "contractName": "Helllo",
        "groupId": 300001,
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
```

* 失败：
```
{
    "code": 102000,
    "message": "system exception",
    "data": {}
}
```



### <span id="5.2">5.2 修改合约</span>  [top](#1)

#### 5.2.1 传输协议规范
* 网络传输协议：使用HTTP协议
* 请求地址：`/deployIncoming/contractInfo`
* 请求方式：PUT
* 请求头：Content-type: application/json
* 返回格式：JSON

#### 5.2.2 参数信息详情

| 序号 | 输入参数        | 类型          | 可为空 | 备注                                            |
|------|-----------------|---------------|--------|-------------------------------------------------|
| 1    | contractId      | int           | 否     | 合约编号                                        |
| 2    | contractName    | String        | 否     | 合约名称                                        |
| 3    | groupId       | Int           | 否     | 所属群组编号                                      |
| 4    | contractType    | Int           | 否     | 合约类型(0-普通合约，1-系统合约)                |
| 5    | contractSource  | String        | 否     | 合约源码                                        |
| 6    | contractStatus  | Int           | 否     | 部署状态（1：未部署，2：部署成功，3：部署失败） |
| 7    | contractAbi     | String        | 是     | 编译合约生成的abi文件内容                       |
| 8    | contractBin     | String        | 是     | 合约binary                                      |
| 9    | bytecodeBin     | String        | 是     | 合约bin                                         |
| 10   | contractAddress | String        | 是     | 合约地址                                        |
| 11   | deployTime      | LocalDateTime | 是     | 部署时间                                        |
| 12   | contractVersion | String        | 否     | 合约版本                                        |
| 13   | description     | String        | 是     | 备注                                            |
| 序号 | 输出参数        | 类型          |        | 备注                                            |
| 1    | code            | Int           | 否     | 返回码，0：成功 其它：失败                      |
| 2    | message         | String        | 否     | 描述                                            |
| 3    | data            | object        | 是     | 返回信息实体（空）                              |

#### 5.2.3 入参示例
`http://127.0.0.1:8080/webase-node-mgr/deployIncoming/contractInfo`
```
{
    "groupId": "300001",
    "contractId": 200035,
    "contractBin": "60606040526000357c010000000",
    "bytecodeBin": "6060604052341561000c57fe",
    "contractSource": "cHJhZ21hIHNvbGlkaXR5IF4wLjQuMjsNCmNvbQ==",
    "contractAbi": "[{\"constant\":false,\"inputs\":[{\"name\":\"n\",\"type\":\"string\"}],\"name\":\"set\",\"outputs\":[],\"payable\":false,\"type\":\"function\"}\"}]"
}
```


#### 5.2.4 出参示例
* 成功：
```
{
    "code": 0,
    "message": "success",
    "data": {
        "contractId": 200035,
        "contractName": "Helllo",
        "groupId": 300001,
        "contractType": 0,
        "contractSource": "cHJhZ21hIHNvbGgICAgfQ0KfQ==",
        "contractStatus": 1,
        "contractAbi": "[{\"constant\":false,\"inputs\":[{\"name\":\"n\",\"type\":\"string\"}],\"name\":\"set\",\"outputs\":[],\"payable\":false,\"type\":\"function\"}]",
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
```


* 失败：
```
{
    "code": 102000,
    "message": "system exception",
    "data": {}
}
```

### <span id="5.3">5.3 删除合约</span>  [top](#1)

#### 5.3.1 传输协议规范
* 网络传输协议：使用HTTP协议
* 请求地址：`/deployIncoming/{contractId}`
* 请求方式：DELETE
* 请求头：Content-type: application/json
* 返回格式：JSON

#### 5.3.2 参数信息详情

| 序号 | 输入参数   | 类型   | 可为空 | 备注                       |
|------|------------|--------|--------|----------------------------|
| 1    | contractId | int    | 否     | 合约编号名称               |
| 序号 | 输出参数   | 类型   |        | 备注                       |
| 1    | code       | Int    | 否     | 返回码，0：成功 其它：失败 |
| 2    | message    | String | 否     | 描述                       |
| 3    | data       | object | 是     | 返回信息实体（空）         |

#### 5.3.3 入参示例
`http://127.0.0.1:8080/webase-node-mgr/deployIncoming/{contractId}`

#### 5.3.4 出参示例

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

### <span id="5.4">5.4 查询合约列表</span>  [top](#1)

#### 5.4.1 传输协议规范
* 网络传输协议：使用HTTP协议
* 请求地址： `/deployIncoming/contractList/{groupId}/{pageNumber}/{pageSize}`
* 请求方式：GET
* 返回格式：JSON

#### 5.4.2 参数信息详情

| 序号   | 输入参数        | 类型          | 可为空 | 备注                                            |
|--------|-----------------|---------------|--------|-------------------------------------------------|
| 1      | groupId       | int           | 否     | 群组id                                          |
| 2      | pageSize        | int           | 否     | 每页记录数                                      |
| 3      | pageNumber      | int           | 否     | 当前页码                                        |
|        |                 |               |        |                                                 |
| 序号   | 输出参数        | 类型          | 可为空 | 备注                                            |
| 1      | code            | Int           | 否     | 返回码，0：成功 其它：失败                      |
| 2      | message         | String        | 否     | 描述                                            |
| 3      | totalCount      | Int           | 否     | 总记录数                                        |
| 4      | data            | List          | 是     | 列表                                            |
| 5.1    |                 | Oject         |        | 返回信息实体                                    |
| 5.1.1  | contractId      | int           | 否     | 合约编号                                        |
| 5.1.2  | contractName    | String        | 否     | 合约名称                                        |
| 5.1.3  | groupId       | Int           | 否     | 所属群组编号                                    |
| 5.1.4  | contractType    | Int           | 否     | 合约类型(0-普通合约，1-系统合约)                |
| 5.1.5  | contractSource  | String        | 否     | 合约源码                                        |
| 5.1.6  | contractStatus  | Int           | 否     | 部署状态（1：未部署，2：部署成功，3：部署失败） |
| 5.1.7  | contractAbi     | String        | 是     | 编译合约生成的abi文件内容                       |
| 5.1.8  | contractBin     | String        | 是     | 合约binary                                      |
| 5.1.9 | bytecodeBin     | String        | 是     | 合约bin                                         |
| 5.1.10 | contractAddress | String        | 是     | 合约地址                                        |
| 5.1.11 | deployTime      | LocalDateTime | 是     | 部署时间                                        |
| 5.1.12 | contractVersion | String        | 否     | 合约版本                                        |
| 5.1.13 | description     | String        | 是     | 备注                                            |
| 5.1.14 | createTime      | LocalDateTime | 否     | 创建时间                                        |
| 5.1.15 | modifyTime      | LocalDateTime | 是     | 修改时间                                        |

#### 5.4.3 入参示例
`http://127.0.0.1:8080/webase-node-mgr/*deployIncoming/contractList/300001/1/15`

#### 5.4.4 出参示例

* 成功：
```
{
    "code": 0,
    "message": "success",
    "data": [
        {
            "contractId": 200034,
            "contractName": "Hello3",
            "groupId": 300001,
            "contractType": 0,
            "contractSource": "cHJhZ21hIHNvbQ0KfQ==",
            "contractStatus": 1,
            "contractAbi": "[{\"constant\":false,\"inputs\":[{\"name\":\"n\",\"type\":\"string\"}],\"name\":\"set\",\"outputs\":[],\"payable\":false,\"type\":\"function\"},{\"constant\":\"}]",
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
```

* 失败：
```
{
    "code": 102000,
    "message": "system exception",
    "data": {}
}
```

### <span id="5.5">5.5 查询合约信息</span>  [top](#1)

#### 5.5.1 传输协议规范
* 网络传输协议：使用HTTP协议
* 请求地址： `/deployIncoming/{contractId}`
* 请求方式：GET
* 返回格式：JSON

#### 5.5.2 参数信息详情

| 序号 | 输入参数        | 类型          | 可为空 | 备注                                            |
|------|-----------------|---------------|--------|-------------------------------------------------|
| 1    | contractId      | int           | 否     | 合约编号                                        |
| 序号 | 输出参数        | 类型          | 可为空 | 备注                                            |
| 1    | code            | Int           | 否     | 返回码，0：成功 其它：失败                      |
| 2    | message         | String        | 否     | 描述                                            |
| 3    |                 | Oject         |        | 返回信息实体                                    |
| 3.1  | contractId      | int           | 否     | 合约编号                                        |
| 3.2  | contractName    | String        | 否     | 合约名称                                        |
| 3.3  | groupId       | Int           | 否     | 所属群组编号                                      |
| 3.4  | contractType    | Int           | 否     | 合约类型(0-普通合约，1-系统合约)                |
| 3.5  | contractSource  | String        | 否     | 合约源码                                        |
| 3.6  | contractStatus  | Int           | 否     | 部署状态（1：未部署，2：部署成功，3：部署失败） |
| 3.7  | contractAbi     | String        | 是     | 编译合约生成的abi文件内容                       |
| 3.8  | contractBin     | String        | 是     | 合约binary                                      |
| 3.9 | bytecodeBin     | String        | 是     | 合约bin                                         |
| 3.10 | contractAddress | String        | 是     | 合约地址                                        |
| 3.11 | deployTime      | LocalDateTime | 是     | 部署时间                                        |
| 3.12 | contractVersion | String        | 否     | 合约版本                                        |
| 3.13 | description     | String        | 是     | 备注                                            |
| 3.14 | createTime      | LocalDateTime | 否     | 创建时间                                        |
| 3.15 | modifyTime      | LocalDateTime | 是     | 修改时间                                        |

#### 5.5.3 入参示例
`http://127.0.0.1:8080/webase-node-mgr/deployIncoming/200001`

#### 5.5.4 出参示例

* 成功：
```
{
    "code": 0,
    "message": "success",
    "data": {
        "contractId": 200001,
        "contractName": "33",
        "groupId": 300001,
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
```


* 失败：
```
{
    "code": 102000,
    "message": "system exception",
    "data": {}
}
```

### <span id="5.6">5.6 部署合约</span>  [top](#1)

#### 5.6.1 传输协议规范
* 网络传输协议：使用HTTP协议
* 请求地址：`/deployIncoming/deploy`
* 请求方式：POST
* 请求头：Content-type: application/json
* 返回格式：JSON

#### 5.6.2 参数信息详情

| 序号 | 输入参数          | 类型           | 可为空 | 备注                       |
|------|-------------------|----------------|--------|----------------------------|
| 1    | contractId        | int            | 否     | 合约编号                   |
| 2    | groupId           | Int            | 否     | 所属群组编号               |
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

#### 5.6.3 入参示例
`http://127.0.0.1:8080/webase-node-mgr/deployIncoming/deploy`
```
{
    "groupId": "300001",
    "contractBin": "60606040526000357c01e08980029",
    "bytecodeBin": null,
    "contractAbi": "[{\"constant\":false,\"inputs\":[{\"name\":\"n\",\"type\":\"bytes\"}],\"name\":\"set\",\"outputs\":[],\"payable\":false,\"type\":\"function\"}]",
    "contractSource": "cHJhZ21hIHudCByZXR1Sk7DQogICAgfQ0KfQ==",
    "userId": 700001,
    "contractId": 200033
}
```


#### 5.6.4 出参示例

* 成功：
```
{
    "code": 0,
    "message": "success",
    "data": {
        "contractId": 200035,
        "contractName": "Helllo",
        "groupId": 300001,
        "contractType": 0,
        "contractSource": "cHJhZ21hIHNvbGgICAgfQ0KfQ==",
        "contractStatus": 1,
        "contractAbi": "[{\"constant\":false,\"inputs\":[{\"name\":\"n\",\"type\":\"string\"}],\"name\":\"set\",\"outputs\":[],\"payable\":false,\"type\":\"function\"}]",
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
```


* 失败：
```
{
    "code": 102000,
    "message": "system exception",
    "data": {}
}
```

### <span id="5.7">5.7 发送交易</span>  [top](#1)

#### 5.7.1 传输协议规范
* 网络传输协议：使用HTTP协议
* 请求地址：`/deployIncoming/transaction`
* 请求方式：POST
* 请求头：Content-type: application/json
* 返回格式：JSON

#### 5.7.2 参数信息详情

| 序号 | 输入参数     | 类型           | 可为空 | 备注                       |
|------|--------------|----------------|--------|----------------------------|
| 1    | groupId      | Int            | 否     | 所属群组编号               |
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

#### 5.7.3 入参示例
`http://127.0.0.1:8080/webase-node-mgr/deployIncoming/deploy`
```
{
    "groupId": "300001",
    "contractBin": "6060604052600f8dee08980029",
    "bytecodeBin": null,
    "contractAbi": "[{\"constant\":false,\"inputs\":[{\"name\":\"n\",\"type\":\"bytes\"}],\"name\":\"set\",\"outputs\":[],\"payable\":false,\"type\":\"function\"}]",
    "contractSource": "cHJhZ21hIHNvbGlkaXR5IF4wLjQuMjfQ==",
    "userId": 700001,
    "contractId": 200033
}
```


#### 5.7.4 出参示例
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


## <span id="6">6 服务器监控相关</span>  [top](#1)

### <span id="6.1">6.1 获取节点监控信息</span>  [top](#1)

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

### <span id="6.2">6.2 获取服务器监控信息</span>  [top](#1)

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
`http://127.0.0.1:8080/webase-node-mgr/performance/ratio/500001?gap=1&beginDate=2019-03-15T00:00:00&endDate=2019-03-15T15:26:55&contrastBeginDate=2019-03-15T00:00:00&contrastEndDate=2019-03-15T15:26:55`

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


## <span id="7">7 审计相关模块</span>  [top](#1)

### <span id="7.1">7.1 获取用户交易监管信息列表</span>  [top](#1)

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
`http://127.0.0.1:8080/webase-node-mgr/monitor/userList/300001`

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

### <span id="7.2">7.2 获取合约方法监管信息列表</span>  [top](#1)

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
`http://127.0.0.1:8080/webase-node-mgr/monitor/interfaceList/300001`

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

### <span id="7.3">7.3 获取交易hash监管信息列表</span>  [top](#1)

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
`http://127.0.0.1:8080/webase-node-mgr/monitor/transList/300001?userName=0x5d97f8d41638a7b1b669b70b307bab6d49df8e2c&interfaceName=0x4ed3885e`

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

### <span id="7.4">7.4 获取异常用户信息列表</span>  [top](#1)

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
`http://127.0.0.1:8080/webase-node-mgr/monitor/unusualUserList/300001/1/10?userName=`

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

### <span id="7.5">7.5 获取异常合约信息列表</span>  [top](#1)

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
`http://127.0.0.1:8080/webase-node-mgr/monitor/unusualContractList/300001/1/10?contractAddress=`

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

## <span id="8">8 群组信息模块</span>  [top](#1)

### <span id="8.1">8.1 获取群组概况</span>  [top](#1)

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
`http://127.0.0.1:8080/webase-node-mgr/group/300001`

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

### <span id="8.2">8.2 获取所有群组列表</span>  [top](#1)

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
`http://127.0.0.1:8080/webase-node-mgr/group/all`

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

### <span id="8.3">8.3 查询每日交易数据</span>  [top](#1)

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
`http://127.0.0.1:8080/webase-node-mgr/group/transDaily/300001`

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



### <span id="8.4">8.4 修改群组名称</span>  [top](#1)

#### 8.4.1 传输协议规范
* 网络传输协议：使用HTTP协议
* 请求地址：`/group/update`
* 请求方式：put
* 请求头：Content-type: application/json
* 返回格式：JSON

#### 8.4.2 参数信息详情

| 序号 | 输入参数      | 类型   | 可为空 | 备注                        |
|------|---------------|--------|--------|---------------------------|
| 1    | groupId      | int     | 否     | 群组编号                   |
| 2    | groupName    | String | 否      | 群组新名称                  |
| 序号 | 输出参数      | 类型   |        | 备注                        |
| 1    | code          | Int    | 否     | 返回码，0：成功 其它：失败  |
| 2    | message       | String | 否     | 描述                       |

### 8.4.3 入参示例
`http://127.0.0.1:8080/webase-node-mgr/group/update`
```
{
    "groupId": 12541,
    "groupName": "groupNew"
}
```


#### 8.4.4 出参示例
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


## <span id="9">9 节点管理模块</span>  [top](#1)

### <span id="9.1">9.1 查询节点列表</span>  [top](#1)

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
`http://127.0.0.1:8080/webase-node-mgr/node/nodeList/300001/1/10?nodeName=`

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

### <span id="9.2">9.2 查询节点信息</span>  [top](#1)

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
`http://127.0.0.1:8080/webase-node-mgr/node/nodeInfo/{groupId}

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

## <span id="10">10 角色管理模块</span>  [top](#1)

### <span id="10.1">10.1 查询角色列表</span>  [top](#1)

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
`http://127.0.0.1:8080/webase-node-mgr/role/roleList?groupId=300001&pageNumber=&pageSize=&roleId=&roleName=`

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

## <span id="11">11 用户管理模块</span>  [top](#1)

### <span id="11.1">11.1 新增私钥用户</span>  [top](#1)

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
`v/webase-node-mgr/user/userInfo`

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

### <span id="11.2">11.2 绑定公钥用户</span>  [top](#1)

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
`http://127.0.0.1:8080/webase-node-mgr/user/userInfo`

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

### <span id="11.3">11.3 修改用户备注</span>  [top](#1)

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
`http://127.0.0.1:8080/webase-node-mgr/user/userInfo`

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

### <span id="11.4">11.4 查询私钥</span>  [top](#1)

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
`http://127.0.0.1:8080/webase-node-mgr/user/privateKey/4585`

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

### <span id="11.5">11.5 查询用户列表</span>  [top](#1)

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
`http://127.0.0.1:8080/webase-node-mgr/user/userList/300001/1/10?userParam=asdfvw`

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



## <span id="12">12 文件管理模块</span>  [top](#1)

### <span id="12.1">12.1 文件上传</span>  [top](#1)
* 网络传输协议：使用HTTP协议
* 请求地址：`/file/uploadFile`
* 请求方式：POST
* 请求编码：multipart/form-data
* 返回格式：JSON

#### 12.1.2 参数信息详情

| 序号 | 输入参数        | 类型          | 可为空 | 备注                                            |
|------|-----------------|---------------|--------|-------------------------------------------------|
| 1    | groupId         | int           | 否     | 所属群组id                                      |
| 2    | file            | File          | 否     | 文件  （支持格式：sol、zip ）                   |
| 3    | description     | String        | 是     | 备注                   |
| 2    | type            | int           | 否     | 文件类型 （0-合约）                             |
| 序号 | 输出参数        | 类型          |        | 备注                                            |
| 1    | code            | Int           | 否     | 返回码，0：成功 其它：失败                      |
| 2    | message         | String        | 否     | 描述                                            |
| 3    | data            | object        | 是     |                                                 |

#### 12.1.3 入参示例

`http://127.0.0.1:8080/webase-node-mgr/file/uploadFile`
```
{
    "groupId": "300001",
    "file":  File,
    "description": "testFile",
    "type": 0
}
```


#### 12.1.4 出参示例
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



## <span id="13">13 前置管理模块</span>  [top](#1)
### <span id="13.1">13.1 新增节点前置信息</span>  [top](#1)

#### 13.1.1 传输协议规范
* 网络传输协议：使用HTTP协议
* 请求地址： `/front/new`
* 请求方式：POST
* 请求头：Content-type: application/json
* 返回格式：JSON

#### 13.1.2 参数信息详情

| 序号 | 输入参数    | 类型          | 可为空 | 备注                                       |
|------|-------------|---------------|--------|--------------------------------------------|                          |
| 1    | frontIp     | string        | 否     | 前置ip                                     |
| 2    | frontPort   | int           | 否     | 前置服务端口                               |
| 序号 | 输出参数    | 类型          |        | 备注                                       |
| 1    | code        | Int           | 否     | 返回码，0：成功 其它：失败                 |
| 2    | message     | String        | 否     | 描述                                       |
| 3    |             | Object        |        | 节点信息对象                               |
| 3.1  | frontId     | int           | 否     | 前置编号                                                         |
| 3.2  | frontIp     | string        | 否     | 前置ip                                           |
| 3.3  | frontPort   | int           | 否     | 前置端口                                   |                               |
| 3.4  | createTime  | LocalDateTime | 否     | 落库时间                                   |
| 3.5  | modifyTime  | LocalDateTime | 否     | 修改时间                                   |

#### 13.1.3 入参示例
`http://127.0.0.1:8080/webase-node-mgr/node`
```
{
    "frontIp": "127.0.0.1",
    "frontPort": "8081"
}
```


#### 13.1.4 出参示例
* 成功：
```
{
    "code": 0,
    "message": "success",
    "data": {
        "frontId": 500001,
        "frontIp": "127.0.0.1",
        "frontPort": 8181,
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


### <span id="13.2">13.2 获取所有前置列表</span>  [top](#1)

#### 13.2.1 传输协议规范
* 网络传输协议：使用HTTP协议
* 请求地址： `/front/find?frontId={frontId}`
* 请求方式：GET
* 返回格式：JSON

#### 13.2.2 参数信息详情

| 序号  | 输入参数      | 类型          | 可为空 | 备注                       |
|-------|---------------|---------------|--------|----------------------------|
| 1     | frontId       | Int           | 是     | 前置编号                  |
| 序号  | 输出参数      | 类型           |        | 备注                       |
| 1     | code          | Int           | 否     | 返回码，0：成功 其它：失败 |
| 2     | message       | String        | 否     | 描述                       |
| 3     | totalCount    | Int           | 否     | 总记录数                   |
| 4     | data          | List          | 否     | 组织列表                   |
| 4.1   |               | Object        |        | 节点信息对象               |
| 4.1.1 | frontId       | int           | 否     | 前置编号                   |
| 4.1.2 | frontIp       | string        | 否     | 前置ip                     |
| 4.1.3 | frontPort     | int           | 否     | 前置端口                   |                               |
| 4.1.4 | createTime    | LocalDateTime | 否     | 落库时间                   |
| 4.1.5 | modifyTime    | LocalDateTime | 否     | 修改时间                   |


#### 13.2.3 入参示例
`http://127.0.0.1:8080/webase-node-mgr/front/find`

#### 13.2.4 出参示例
* 成功：
```
{
    "code": 0,
    "message": "success",
    "totalCount": 1,
    "data": [
        {
        "frontId": 500001,
        "frontIp": "127.0.0.1",
        "frontPort": 8181,
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



### <span id="13.3">13.3 删除前置信息</span>  [top](#1)

#### 13.3.1 传输协议规范
* 网络传输协议：使用HTTP协议
* 请求地址：`/front/{frontId}`
* 请求方式：DELETE
* 请求头：Content-type: application/json
* 返回格式：JSON

#### 13.3.2 参数信息详情

| 序号 | 输入参数   | 类型   | 可为空 | 备注                       |
|------|------------|--------|--------|----------------------------|
| 1    | frontId    | int    | 否     | 前置编号                   |
| 序号 | 输出参数   | 类型   |        | 备注                       |
| 1    | code       | Int    | 否     | 返回码，0：成功 其它：失败 |
| 2    | message    | String | 否     | 描述                       |
| 3    | data       | object | 是     | 返回信息实体（空）         |

#### 13.3.3 入参示例
`http://127.0.0.1:8080/webase-node-mgr/front/{frontId}`

#### 13.3.4 出参示例

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
# 节点管理服务说明

# 目录
> * [功能说明](#chapter-1)
> * [前提条件](#chapter-2)
> * [部署说明](#chapter-3)

# 1. <a id="chapter-1"></a>功能说明
节点管理服务是fisco-bcos配套的一个子系统，可以通过本系统维护合约信息、私钥用户、查看区块信息、以及链上异常数据监管。

# 2. <a id="chapter-2"></a>前提条件
2.1. fisco-bcos底层代码已经安装；  
2.2. 前置和fisco-bcos同机部署；  
2.3. 当前用户有sudo权限；  
2.4. mysql5.5或5.6版本  
2.5. java1.8.0_181或更高版本  
2.6. 安装gradle


# 3. <a id="chapter-3"></a>部署说明
## 3.1 拉取代码
执行命令：
```
git clone https://github.com/WeBankFinTech/webase-node-mgr
```
## 3.2 编译代码
在代码的根目录webase-node-mgr执行构建命令：
```
gradle build
```
构建完成后，会在/usr/local/app/webase-node-mgr目录生成已编译的代码。
## 3.3 数据库初始化

### 3.3.1 修改脚本配置
进入数据库脚本目录
```
cd  /usr/local/app/webase-node-mgr/conf/script
```
修改数据库连接信息：
```
修改数据库名称：sed -i "s/fisco-bcos-data/${your_db_name}/g" fisco-bcos.sh
修改数据库用户名：sed -i "s/defaultAccount/${your_db_account}/g" fisco-bcos.sh
修改数据库密码：sed -i "s/defaultPassword/${your_db_password}/g" fisco-bcos.sh
```
例如：将数据库用户名修改为root，则执行：
```
sed -i "s/defaultAccount/root/g" fisco-bcos.sh
```

### 3.3.2 运行数据库脚本
执行命令：sh  fisco-bcos.sh  ${dbIP}  ${dbPort}
如：
```
sh  fisco-bcos.sh  127.0.0.1 3306
```

## 3.4 节点服务的配置及启动
### 3.4.1 服务配置修改
进入到已编译的代码根目录：
```
cd /usr/local/app/webase-node-mgr/conf
```
修改服务配置：
```
修改当前服务端口：sed -i "s/8080/${your_server_port}/g" application.yml
修改数据库IP：sed -i "s/127.0.0.1/${your_db_port}/g" application.yml
修改数据库名称：sed -i "s/fisco-bcos-data/${your_db_name}/g" application.yml
修改数据库用户名：sed -i "s/defaultAccount/${your_db_account}/g" application.yml
修改数据库密码：sed -i "s/defaultPassword/${your_db_password}/g" application.yml
```

### 3.4.2 服务启停
进入到已编译的代码根目录：
```
cd /usr/local/app/webase-node-mgr
```
启动：
```
sh start.sh
```
停止：
```
sh stop.sh
```
状态检查：
```
sh serverStatus.sh
```
## 3.4.3 查看日志
全量日志：
```
/usr/local/app/logs/webase-node-mgr/node-mgr.log
```
错误日志：
```
/usr/local/app/logs/webase-node-mgr/node-mgr-error.log
```

## 3.5 初始化基础合约
### 3.5.1 前提条件
节点正常运行
节点管理服务正常运行
节点前置正常运行

### 3.5.2 修改脚本配置
进入合约脚本目录：
```
cd /usr/local/app/webase-node-mgr/conf/contract
```
修改配置中的前置服务信息：
```
修改前置服务IP：sed -i "s/defaultFrontIp /${your_front_ip}/g"  contract-init.sh
修改前置服务的端口：sed -i "s/defaultFrontPort /${your_front_port}/g"  contract-init.sh
```
### 3.5.3 运行脚本
执行命令：
```
sh contract-init.sh
```
如果脚本中三个合约部署返回结果的code都是0,则表示合约所有合约都部署成功
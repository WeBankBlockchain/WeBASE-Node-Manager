# 节点管理服务说明

# 目录
> * [功能说明](#chapter-1)
> * [前提条件](#chapter-2)
> * [部署说明](#chapter-3)

# <a id="chapter-1">1. 功能说明</a>
节点管理服务是fisco-bcos配套的一个子系统，可以通过本系统维护合约信息、私钥用户、查看区块信息、以及链上异常数据监管。

# <a id="chapter-2">2. 前提条件</a>
| 序号  | 输入参数                                          | 
|-------|---------------------------------------------------|
| 1     | fisco-bcos底层代码已经安装                        |
| 2     | 前置和fisco-bcos同机部署                          |
| 3     | mysql5.5或5.6版本【更高版本需要更改mysql配置，可参考《install_FAQ.md》】    |
| 4     | java1.8.0_181或更高版本                           |
| 5     | 安装gradle4.9以上版本                             |


# <a id="chapter-3">3. 部署说明</a>

## 3.1 注意事项
* 本文档默认服务器为centos，如果在ubuntu搭建，请将下面所有命令中的sh更该为bash,如：bash start.sh
* 在服务搭建的过程中，如碰到问题，请查看本文同目录下《install_FAQ.md》

## 3.2 拉取代码
执行命令：
```shell
git clone https://github.com/WeBankFinTech/webase-node-mgr.git
```
## 3.3 编译代码
进入代码根目录：
```shell
cd webase-node-mgr
```
在代码的根目录webase-node-mgr执行构建命令：
```shell
gradle build -x test
```
构建完成后，会在根目录webase-node-mgr下生成已编译的代码目录dist。


## 3.4 数据库初始化
### 3.4.1 新建数据库
```
#登录mysql:
mysql  -u ${your_db_account}  -p${your_db_password}  例如：mysql  -u root  -p123456
#新建数据库：
CREATE DATABASE IF NOT EXISTS {your_db_name} DEFAULT CHARSET utf8 COLLATE utf8_general_ci;
```

### 3.4.2 修改脚本配置
进入数据库脚本目录
```shell
cd  dist/script
```
修改数据库连接信息：
```shell
修改数据库名称：sed -i "s/fisco-bcos-data/${your_db_name}/g" webase.sh
修改数据库用户名：sed -i "s/defaultAccount/${your_db_account}/g" webase.sh
修改数据库密码：sed -i "s/defaultPassword/${your_db_password}/g" webase.sh
```
例如：将数据库用户名修改为root，则执行：
```shell
sed -i "s/defaultAccount/root/g" webase.sh
```

### 3.4.3 运行数据库脚本
执行命令：sh  webase.sh  ${dbIP}  ${dbPort}
如：
```shell
sh  webase.sh  127.0.0.1 3306
```

## 3.5 节点服务的配置及启动
### 3.5.1 服务配置修改
进入到已编译的代码配置文件目录：
```shell
cd dist/conf
```
修改服务配置：
```shell
修改当前服务（webase-node-mgr）端口：sed -i "s/8080/${your_server_port}/g" application.yml
修改数据库IP：sed -i "s/127.0.0.1/${your_db_ip}/g" application.yml
修改数据库端口：sed -i "s/3306/${your_db_port}/g" application.yml
修改数据库名称：sed -i "s/fisco-bcos-data/${your_db_name}/g" application.yml
修改数据库用户名：sed -i "s/defaultAccount/${your_db_account}/g" application.yml
修改数据库密码：sed -i "s/defaultPassword/${your_db_password}/g" application.yml
```

### 3.5.2 服务启停
进入到已编译的代码根目录：
```
cd dist
```
启动：
```shell
sh start.sh
```
停止：
```shell
sh stop.sh
```
状态检查：
```shell
sh serverStatus.sh
```
## 3.5.3 查看日志
进入到日志目录：
```shell
cd dist/logs
```
全量日志：tail -f node-mgr.log
错误日志：tail -f node-mgr-error.log

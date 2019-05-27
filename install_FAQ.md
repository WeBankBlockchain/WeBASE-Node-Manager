#常见问题解答

### 一般问题
* 问：执行shell脚本报下面错误：
```
[app@VM_96_107_centos deployInputParam]$ bash start.sh
start.sh: line 2: $'\r': command not found
start.sh: line 8: $'\r': command not found
start.sh: line 9: $'\r': command not found
start.sh: line 10: $'\r': command not found
```
答：这是编码问题，在脚本的目录下执行转码命令：
```shell
dos2unix *.sh
```


### 数据库问题
* 问：服务访问数据库抛出异常：
```
[Err] 1055 - Expression #1 of ORDER BY clause is not in GROUP BY clause and contains nonaggregated column 'information_schema.PROFILING.SEQ' which is not functionally dependent on columns in GROUP BY clause; this is incompatible with sql_mode=only_full_group_by
```
答：mysql5.7版本默认开启（only_full_group_by），修改文件/etc/my.cnf,在[mysqlId]这行的后面加上下面的内容：
```
sql_mode=STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION
```

* 问：执行数据库初始化脚本抛出异常：
```
ERROR 2003 (HY000): Can't connect to MySQL server on '10.0.0.52' (110)
```
答：mysql没有开通该帐号的远程访问权限，登录mysql，执行如下命令，其中TestUser改为你的帐号
```
GRANT ALL PRIVILEGES ON *.* TO 'TestUser'@'%' IDENTIFIED BY '此处为TestUser的密码’' WITH GRANT OPTION;
```



### mgr服务搭建问题
* 问：执行构建命令`gradle build -x test`抛出异常：
```
A problem occurred evaluating root project 'webase-node-mgr'.
Could not find method compileOnly() for arguments [[org.projectlombok:lombok:1.18.2]] on root project 'webase-node-mgr'.
```
答：
方法1、已安装的gradle版本过低，升级gradle版本到4.10以上即可。
方法2、直接使用命令：`./gradlew build -x test`


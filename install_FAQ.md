#常见问题解答

### 一般问题
* 问：执行shell脚本报下面错误：
```
[app@VM_96_107_centos contract]$ sh contract-init.sh
contract-init.sh: line 2: $'\r': command not found
contract-init.sh: line 8: $'\r': command not found
contract-init.sh: line 9: $'\r': command not found
contract-init.sh: line 10: $'\r': command not found
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



### mgr服务搭建问题
* 问：执行构建命令`gradle build`抛出异常：
```
A problem occurred evaluating root project 'webase-node-mgr'.
Could not find method annotationProcessor() for arguments [[org.projectlombok:lombok:1.18.2]] on object of type org.gradle.api.internal.artifacts.dsl.dependencies.DefaultDependencyHandler.
```
答：已安装的gradle版本过低，没有annotationProcessor()方法,升级gradle版本到4.9以上即可。


* 问：mgr服务能正常启动，但查看日志有异常：
```
2019-03-26 10:25:49.332 [http-nio-8083-exec-2] WARN  ExceptionsHandler() - business exception return:{"code":202000,"message":"invalid node info","data":null}
2019-03-26 10:25:54.348 [http-nio-8083-exec-1] WARN  ExceptionsHandler() - catch business exception
com.webank.webase.node.mgr.base.exception.NodeMgrException: invalid node info
```
答：服务中有些功能（如上报、和数据同步等）需要用到节点信息，可在webase-web的【 节点管理】页面新增节点信息。

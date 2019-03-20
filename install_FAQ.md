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
```
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
#  服务升级记录

# V0.5.3 升级到 V0.5.4
## 变更内容
取消session保存登录状态，新增tb_token表保存登录token

## 准备工作
1、更新WeBASE-Node-Manager代码到V0.5.4，并重新打包。
2、登录mysql,运行如下脚本：
```ddl
CREATE TABLE IF NOT EXISTS tb_token (
  token varchar(120) NOT NULL PRIMARY KEY COMMENT 'token',
  value varchar(50) NOT NULL COMMENT '与token相关的值（如：用户编号，图形验证码值）',
  expire_time timestamp NOT NULL COMMENT '失效时间',
  create_time timestamp NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='token信息表';
```
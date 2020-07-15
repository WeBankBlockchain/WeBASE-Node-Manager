spring:
  datasource:
    url: jdbc:h2:file:/data/h2/webasefront;DB_CLOSE_ON_EXIT=FALSE
sdk:
  encryptType: [(${encryptType})]
  channelPort: [(${channelPort})]
server:
  port: [(${frontPort})]
constant:
  keyServer: [(${webaseSignAddr})] # webase-sign服务的IP:Port（单个）
  nodePath: /data
  monitorDisk: /data


package com.webank.webase.node.mgr.precntauth.precompiled.crud.entity;

import lombok.Data;

@Data
public class ReqGetTableInfo {

  private String groupId;
  private String tableName;
  private String key;

}

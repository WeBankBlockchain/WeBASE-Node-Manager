package com.webank.webase.node.mgr.precntauth.precompiled.crud.entity;

import java.util.Map;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReqSetTableInfo {

  private String groupId;
  private String tableName;
  private String key;
  private Map<String,String> fieldNameToValue;
  private String signUserId;
  @NotNull
  private String fromAddress;
}

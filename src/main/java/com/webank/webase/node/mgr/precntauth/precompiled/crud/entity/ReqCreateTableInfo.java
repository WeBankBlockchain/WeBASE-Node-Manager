package com.webank.webase.node.mgr.precntauth.precompiled.crud.entity;

import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReqCreateTableInfo {

  private String groupId;
  private String tableName;
  private String keyFieldName;
  private List<String> valueFields;
  private String signUserId;
  @NotNull
  private String fromAddress;

}

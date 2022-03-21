package com.webank.webase.node.mgr.precntauth.precompiled.cns.entity;

import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReqRegisterCnsInfo {

  private String groupId;
  private String contractName;
  private String contractVersion;
  private String contractAddress;
  private String abiData;
  private String signUserId;
  @NotNull
  private String fromAddress;

}

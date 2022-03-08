package com.webank.webase.node.mgr.precntauth.authmanager.admin.entity;

import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReqAclUsrInfo {

  private String groupId;
  private String contractAddr;
  private String func;
  private String userAddress;
  private Boolean isOpen;
  private String signUserId;
  @NotNull
  private String fromAddress;


}

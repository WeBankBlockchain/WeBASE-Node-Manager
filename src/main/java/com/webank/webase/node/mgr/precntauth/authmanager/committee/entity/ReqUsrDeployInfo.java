package com.webank.webase.node.mgr.precntauth.authmanager.committee.entity;

import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReqUsrDeployInfo {

  private String groupId;
  private Boolean openFlag;
  private String userAddress;
  private String signUserId;
  @NotNull
  private String fromAddress;

}

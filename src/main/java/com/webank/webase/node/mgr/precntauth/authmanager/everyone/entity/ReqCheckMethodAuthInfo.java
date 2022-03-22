package com.webank.webase.node.mgr.precntauth.authmanager.everyone.entity;

import lombok.Data;

@Data
public class ReqCheckMethodAuthInfo {

  private String groupId;
  private String contractAddr;
  private String func;
  private String userAddress;

}

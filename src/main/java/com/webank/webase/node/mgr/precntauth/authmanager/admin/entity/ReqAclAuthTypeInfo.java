package com.webank.webase.node.mgr.precntauth.authmanager.admin.entity;

import java.math.BigInteger;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReqAclAuthTypeInfo {

  private String groupId;
  private String contractAddr;
  private String func;
  private BigInteger authType; //type为1时，设置为白名单，type为2时，设置为黑名单。
  private String signUserId;
//  private byte[] func;
  @NotNull
  private String fromAddress;

}

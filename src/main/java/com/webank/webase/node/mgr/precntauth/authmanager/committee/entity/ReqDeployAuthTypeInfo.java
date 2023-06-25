package com.webank.webase.node.mgr.precntauth.authmanager.committee.entity;

import java.math.BigInteger;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReqDeployAuthTypeInfo {

  private String groupId;
  private BigInteger  deployAuthType;
  private String signUserId;
  @NotNull
  private String fromAddress;

}

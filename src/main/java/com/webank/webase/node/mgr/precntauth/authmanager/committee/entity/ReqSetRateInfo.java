package com.webank.webase.node.mgr.precntauth.authmanager.committee.entity;

import java.math.BigInteger;
import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReqSetRateInfo {

  private String groupId;
  private BigInteger participatesRate;
  private BigInteger winRate;
  private String signUserId;
  @NotBlank
  private String fromAddress;


}

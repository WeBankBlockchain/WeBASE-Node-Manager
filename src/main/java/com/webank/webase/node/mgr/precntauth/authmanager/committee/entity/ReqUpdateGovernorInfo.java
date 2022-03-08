package com.webank.webase.node.mgr.precntauth.authmanager.committee.entity;

import java.math.BigInteger;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReqUpdateGovernorInfo {

  @NotNull
  private String groupId;
  @NotBlank
  private String fromAddress;     //请求地址
  private String accountAddress;  //账户地址
  private BigInteger weight;

  //front service will use signUserId
  private String signUserId;


}

package com.webank.webase.node.mgr.precntauth.authmanager.committee.entity;

import lombok.Data;

@Data
public class ReqLoadGovernorInfo {

  private String accountFileFormat;
  private String accountFilePath;
  private String password;

}

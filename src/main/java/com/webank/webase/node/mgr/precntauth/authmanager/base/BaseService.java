package com.webank.webase.node.mgr.precntauth.authmanager.base;

import com.webank.webase.node.mgr.front.frontinterface.FrontRestTools;
import com.webank.webase.node.mgr.tools.HttpRequestTools;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BaseService {

  @Autowired
  private FrontRestTools frontRestTools;

  public boolean queryExecEnvIsWasm(String groupId) {
    Map<String, String> map = new HashMap<>();
    map.put("groupId", groupId);
    String uri = HttpRequestTools.getQueryUri(
        FrontRestTools.RPC_AUTHMANAGER_BASE_ENV, map);
    Boolean frontRsp = frontRestTools.getForEntity(groupId, uri, Boolean.class);
    return frontRsp;
  }

  public boolean queryChainHasAuth(String groupId) {
    Map<String, String> map = new HashMap<>();
    map.put("groupId", groupId);
    String uri = HttpRequestTools.getQueryUri(
        FrontRestTools.RPC_AUTHMANAGER_BASE_AUTH, map);
    Boolean frontRsp = frontRestTools.getForEntity(groupId, uri, Boolean.class);
    return frontRsp;
  }

}

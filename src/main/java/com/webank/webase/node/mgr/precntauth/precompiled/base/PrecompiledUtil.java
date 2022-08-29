package com.webank.webase.node.mgr.precntauth.precompiled.base;

import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.entity.BaseResponse;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.sdk.v3.model.PrecompiledConstant;
import org.fisco.bcos.sdk.v3.model.RetCode;
import org.fisco.bcos.sdk.v3.model.TransactionReceipt;
import org.fisco.bcos.sdk.v3.transaction.codec.decode.ReceiptParser;
import org.fisco.bcos.sdk.v3.transaction.model.exception.ContractException;

@Slf4j
public class PrecompiledUtil {

  public static final String ContractLogFileName = "deploylog.txt";

  // SystemConfig key
  public static final String TxCountLimit = "tx_count_limit";
  public static final String TxGasLimit = "tx_gas_limit";
  // node consensus type
  public static final String NODE_TYPE_SEALER = "sealer";
  public static final String NODE_TYPE_OBSERVER = "observer";
  public static final String NODE_TYPE_REMOVE = "remove";

  public static final int TxGasLimitMin = 10000;
  public static final int TxGasLimitMax = 2147483647;

  public static int SYS_TABLE_KEY_MAX_LENGTH = 58; // 64- "_user_".length
  public static int SYS_TABLE_KEY_FIELD_NAME_MAX_LENGTH = 64;
  public static int SYS_TABLE_VALUE_FIELD_MAX_LENGTH = 1024;
  public static int USER_TABLE_KEY_VALUE_MAX_LENGTH = 255;
  public static int USER_TABLE_FIELD_NAME_MAX_LENGTH = 64;
  public static int USER_TABLE_FIELD_VALUE_MAX_LENGTH = 16 * 1024 * 1024 - 1;


  public static boolean checkVersion(String version) {

    if (StringUtils.isBlank(version)) {
      return false;
    }else if (!version.matches("^[A-Za-z0-9.]+$")) { // check version's character
      return false;
    }else {
      return true;
    }
  }

  public static boolean checkNodeId(String nodeId) {
    if (nodeId.length() != 128) {
      return false;
    }else {
      return true;
    }
  }


}

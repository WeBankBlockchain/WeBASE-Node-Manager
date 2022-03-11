/**
 * Copyright 2014-2022 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package node.mgr.test.contract;

import com.webank.webase.node.mgr.contract.ContractService;
import com.webank.webase.node.mgr.contract.entity.DeployInputParam;
import com.webank.webase.node.mgr.contract.entity.ReqCompileLiquid;
import com.webank.webase.node.mgr.contract.entity.RspCompileTask;
import com.webank.webase.node.mgr.contract.entity.TbContract;
import com.webank.webase.node.mgr.tools.JsonTools;
import node.mgr.test.base.TestBase;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;

public class LiquidContractTest extends TestBase {
  @Autowired
  ContractService contractService;
  private final static int frontId = 2;
  private final static int contractId = 200001;
  private static final String groupId = "group0";
  private static final String account = "admin";
  private static final String contractPath = "/";
  private static final String contractName = "hello";
  private static final String source = "IyFbY2ZnX2F0dHIobm90KGZlYXR1cmUgPSAic3RkIiksIG5vX3N0ZCldCgp1c2UgbGlxdWlkOjpzdG9yYWdlOwp1c2UgbGlxdWlkX2xhbmcgYXMgbGlxdWlkOwoKI1tsaXF1aWQ6OmNvbnRyYWN0XQptb2QgaGVsbG9fd29ybGQgewogICAgdXNlIHN1cGVyOjoqOwoKICAgICNbbGlxdWlkKHN0b3JhZ2UpXQogICAgc3RydWN0IEhlbGxvV29ybGQgewogICAgICAgIG5hbWU6IHN0b3JhZ2U6OlZhbHVlPFN0cmluZz4sCiAgICB9CgogICAgI1tsaXF1aWQobWV0aG9kcyldCiAgICBpbXBsIEhlbGxvV29ybGQgewogICAgICAgIHB1YiBmbiBuZXcoJm11dCBzZWxmKSB7CiAgICAgICAgICAgIHNlbGYubmFtZS5pbml0aWFsaXplKFN0cmluZzo6ZnJvbSgiQWxpY2UiKSk7CiAgICAgICAgfQoKICAgICAgICBwdWIgZm4gZ2V0KCZzZWxmKSAtPiBTdHJpbmcgewogICAgICAgICAgICBzZWxmLm5hbWUuY2xvbmUoKQogICAgICAgIH0KCiAgICAgICAgcHViIGZuIHNldCgmbXV0IHNlbGYsIG5hbWU6IFN0cmluZykgewogICAgICAgICAgICBzZWxmLm5hbWUuc2V0KG5hbWUpCiAgICAgICAgfQogICAgfQoKICAgICNbY2ZnKHRlc3QpXQogICAgbW9kIHRlc3RzIHsKICAgICAgICB1c2Ugc3VwZXI6Oio7CgogICAgICAgICNbdGVzdF0KICAgICAgICBmbiBnZXRfd29ya3MoKSB7CiAgICAgICAgICAgIGxldCBjb250cmFjdCA9IEhlbGxvV29ybGQ6Om5ldygpOwogICAgICAgICAgICBhc3NlcnRfZXEhKGNvbnRyYWN0LmdldCgpLCAiQWxpY2UiKTsKICAgICAgICB9CgogICAgICAgICNbdGVzdF0KICAgICAgICBmbiBzZXRfd29ya3MoKSB7CiAgICAgICAgICAgIGxldCBtdXQgY29udHJhY3QgPSBIZWxsb1dvcmxkOjpuZXcoKTsKCiAgICAgICAgICAgIGxldCBuZXdfbmFtZSA9IFN0cmluZzo6ZnJvbSgiQm9iIik7CiAgICAgICAgICAgIGNvbnRyYWN0LnNldChuZXdfbmFtZS5jbG9uZSgpKTsKICAgICAgICAgICAgYXNzZXJ0X2VxIShjb250cmFjdC5nZXQoKSwgIkJvYiIpOwogICAgICAgIH0KICAgIH0KfQ==";

  private static final String user = "0x5dbeb8597d50f8ef9bc63ba45db3abf2d2da1f8d"; // 2ad78092874441a8b354768bded29057


  @Test
  public void testEnv() {
    contractService.checkFrontLiquidEnv(frontId);
  }



  @Test
  public void testCompile() {
    ReqCompileLiquid param = new ReqCompileLiquid(frontId);
    param.setGroupId(groupId);
    param.setContractId(contractId);
    param.setAccount(account);
    param.setContractSource(source);
    param.setContractName(contractName);
    param.setContractPath(contractPath);
    param.setIsWasm(true);
    TbContract contract = contractService.saveContract(param);
    System.out.println("contractId " + contract.getContractId());
    RspCompileTask response = contractService.compileLiquidContract(param);
    System.out.println("res: " + JsonTools.objToString(response));

  }

  @Test
  public void testGetCompileTask() {
    ReqCompileLiquid param = new ReqCompileLiquid(frontId);
    param.setGroupId(groupId);
    param.setAccount(account);
    param.setContractSource(source);
    param.setContractName(contractName);
    param.setContractPath(contractPath);
    param.setContractId(contractId);
    param.setIsWasm(true);
    RspCompileTask response = contractService.checkCompileLiquid(param);
    System.out.println("res: " + JsonTools.objToString(response));
  }

  @Test
  public void testDeployLiquid() {
    TbContract contract = contractService.queryByContractId(contractId);
    DeployInputParam param = new DeployInputParam();
    param.setGroupId(groupId);
    param.setAccount(account);
    param.setContractId(contractId);
    param.setContractName(contractName);
    param.setContractPath(contractPath);
    param.setContractAbi(contract.getContractAbi());
    param.setBytecodeBin(contract.getBytecodeBin());
    param.setUser(user);
    param.setConstructorParams(new ArrayList<>());
    param.setIsWasm(true);
    param.setContractAddress("/test_4_3");
    TbContract contract1 = contractService.deployContract(param);
    System.out.println("contract address " + contract1.getContractAddress());
  }
}

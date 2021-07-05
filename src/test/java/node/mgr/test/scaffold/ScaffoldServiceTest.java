/**
 * Copyright 2014-2020 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package node.mgr.test.scaffold;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.webase.node.mgr.lite.contract.entity.TbContract;
import com.webank.webase.node.mgr.lite.front.entity.FrontNodeConfig;
import com.webank.webase.node.mgr.lite.contract.scaffold.ScaffoldService;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import node.mgr.test.base.TestBase;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class ScaffoldServiceTest extends TestBase {

    @Autowired
    private ScaffoldService scaffoldService;

    @Test
    public void testGenerate() throws JsonProcessingException {
        FrontNodeConfig nodeConfig = new FrontNodeConfig();
        nodeConfig.setP2pip("127.0.0.1");
        nodeConfig.setChannelPort(25210);
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> sdkMap = mapper.readValue(ScaffoldBuildTest.sdkMapStr, Map.class);
        String groupName = "org.webank";
        String artifact = "demo";
        TbContract contract = new TbContract();
        contract.setContractAddress(ScaffoldBuildTest.contractAddress);
        contract.setContractSource(ScaffoldBuildTest.helloWorldSolBase64Str);
        contract.setContractAbi(ScaffoldBuildTest.abiStr);
        contract.setBytecodeBin(ScaffoldBuildTest.binStr);
        contract.setContractName(ScaffoldBuildTest.contractName);
        List<TbContract> tbContractList = Collections.singletonList(contract);

        scaffoldService.generateProject(nodeConfig, groupName, artifact,
            tbContractList, 1, "", sdkMap);
    }
}

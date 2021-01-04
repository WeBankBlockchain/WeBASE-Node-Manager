/**
 * Copyright 2014-2020 the original author or authors.
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

package node.mgr.test.alert.task;

import com.webank.webase.node.mgr.base.tools.JsonTools;
import com.fasterxml.jackson.databind.JsonNode;
import com.webank.webase.node.mgr.Application;
import com.webank.webase.node.mgr.precompiled.PrecompiledService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.LinkedHashMap;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class ParseToolTest {

    @Autowired
    PrecompiledService precompiledService;

    /**
     * Front's response is {code=0,message=xx,data=[{xx=xx,xx=xx}]}
     * Data type is LinkedHashMap
     */
    @Test
    public void testParseResponseFromFromt() {
        int groupId = 1;
        String nodeId = "17b9cdf9b764b97fd1a93e7153608f7fc40a122b552075eac3d2ddd4f2b32eec080e50ba60045c0840fe5d0f62d7a3070227f89dd6854e35534d27ec7da026f7";
        Object responseFromFront = precompiledService.getNodeListService(groupId, 100, 1);
//        Object responseFromFront = {code=0, message=success, data=[{nodeId=e5e7efc9e8d5bed699313d5a0cd5b024b3c11811d50473b987b9429c2f6379742c88249a7a8ea64ab0e6f2b69fb8bb280454f28471e38621bea8f38be45bc42d, nodeType=sealer}, {nodeId=089846e75c7b18098bb67c77c0b97b3905fc41f5f328fbc52a1580371393fca4a89dda81ab76944942a5e8d307f17f0fb3a139b8333dd933b8e534f2e9a56d9a, nodeType=sealer}, {nodeId=67a9fa9a3994a441c9a77dc24fa85d1e47a88f9a632dbe003ee33cf4a85ba690f874408b447ebb1a316c9c2e18640b5f2840aa1c1aa746cd84eda65ff5743c98, nodeType=sealer}, {nodeId=17b9cdf9b764b97fd1a93e7153608f7fc40a122b552075eac3d2ddd4f2b32eec080e50ba60045c0840fe5d0f62d7a3070227f89dd6854e35534d27ec7da026f7, nodeType=sealer}], totalCount=4};
        System.out.println("==========responseFromFront============");
        System.out.println(responseFromFront.toString());
        LinkedHashMap<String, Object> after = (LinkedHashMap<String, Object>) (responseFromFront);
        System.out.println("==========after========");
        System.out.println(after);
        System.out.println(after.get("data"));
        // transfer to list
        List<LinkedHashMap<String, String>> temp = (List<LinkedHashMap<String, String>>) after.get("data");
//        List<Node> list = (List<Node>) after.get("data"); cast to Node fail
        System.out.println("=========list========");
        System.out.println(temp);
        System.out.println(temp.get(0).get("nodeId"));
        System.out.println(temp.get(0).get("nodeType"));
    }
}

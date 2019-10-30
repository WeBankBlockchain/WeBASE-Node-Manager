/**
 * Copyright 2014-2019 the original author or authors.
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

package node.mgr.test.alert.mail;

import com.alibaba.fastjson.JSON;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class SendMailTest {

    @Test
    public void testProcessEmailContent() {
        // 初始化参数
        // 假设只有一个参数时
        List<String> testParam = new ArrayList<>();
        testParam.add("nodeId");
        String afterTestParam = JSON.toJSONString(testParam);
        System.out.println("=======afterTestParam======");
        System.out.println(afterTestParam);

        // 转回去，可能出错
        List<String> finalParamList = (List<String>) JSON.parse(afterTestParam);

        // 待处理的string
        String alertContent = "您的节点nodeId状态异常";
        String nodeId = "0x000124412312321ABCEF";
        String result = "";
        for(String paramItem: finalParamList) {
            result = alertContent.replace(paramItem, nodeId);
        }
        System.out.println("=======alertContent======");
        System.out.println(alertContent);
        System.out.println("=======result======");
        System.out.println(result);
    }
}

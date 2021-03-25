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

package node.mgr.test.alert.mail;

import com.webank.webase.node.mgr.base.tools.JsonTools;
import com.webank.webase.node.mgr.base.tools.AlertRuleTools;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class AlertContentTest {

    @Test
    public void testAlertTitle() {
        System.out.println(AlertRuleTools.getAlertTypeStrFromEnum(1));
    }

    /**
     * AlertRuleTools.processMailContent:
     * String param cast to List<String>
     * replaceText(String) now support List<String>
     */
    @Test
    public void testAlertParamReplaceList() {
        String alertContent = "{nodeId}节点异常，请到“节点管理”页面查看具体信息。{english}";
        String params = "[\"{nodeId}\",\"{english}\"]";
        List<String> replaceList = new ArrayList<>();
        replaceList.add("0x111");
        replaceList.add("hello ennnglish");
        String res = AlertRuleTools.processMailContent(alertContent, params, replaceList);
        System.out.println(alertContent);
        System.out.println(res);
    }
}

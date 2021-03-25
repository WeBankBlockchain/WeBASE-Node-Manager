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

package node.mgr.test.chaingovernance;

import com.webank.webase.node.mgr.governance.GovernVoteService;
import com.webank.webase.node.mgr.governance.entity.GovernParam;
import node.mgr.test.gm.TestBase;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class ContractStatusServiceTest extends TestBase {

    @Autowired
    private GovernVoteService governVoteService;

    @Test
    public void testListVote() {
        GovernParam param = new GovernParam();
        param.setGroupId(1);
        System.out.println(governVoteService.getVoteList(param));
    }
}

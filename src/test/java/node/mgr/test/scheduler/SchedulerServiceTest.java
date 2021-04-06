/**
 * Copyright 2014-2021  the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package node.mgr.test.scheduler;

import com.webank.webase.node.mgr.scheduler.PullBlockTransTask;
import com.webank.webase.node.mgr.scheduler.ResetGroupListTask;
import com.webank.webase.node.mgr.scheduler.TransMonitorTask;
import node.mgr.test.base.TestBase;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class SchedulerServiceTest extends TestBase {

    @Autowired
    private PullBlockTransTask pullBlockTransTask;
    @Autowired
    private ResetGroupListTask resetGroupListTask;
    @Autowired
    private TransMonitorTask transMonitorTask;

    @Test
    public void pullBlockInfoTaskTest() {
        pullBlockTransTask.pullBlockStart();
    }

    @Test
    public void resetGroupListTest() {
        resetGroupListTask.resetGroupList();
    }

    @Test
    public void transMonitorTest() {
        transMonitorTask.monitorStart();
    }

}

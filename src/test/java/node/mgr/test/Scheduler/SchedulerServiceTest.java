/**
 * Copyright 2014-2019  the original author or authors.
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
package node.mgr.test.Scheduler;

import com.webank.webase.node.mgr.Application;
import com.webank.webase.node.mgr.scheduler.PullBlockInfoTask;
import com.webank.webase.node.mgr.scheduler.ResetGroupListTask;
import com.webank.webase.node.mgr.scheduler.TransMonitorTask;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class SchedulerServiceTest {

    @Autowired
    private PullBlockInfoTask pullBlockInfoTask;
    @Autowired
    private ResetGroupListTask resetGroupListTask;
    @Autowired
    private TransMonitorTask transMonitorTask;

    @Test
    public void pullBlockInfoTaskTest() {
        pullBlockInfoTask.startPull();
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

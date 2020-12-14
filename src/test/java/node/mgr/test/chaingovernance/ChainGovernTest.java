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

import com.webank.webase.node.mgr.precompiled.entity.AddressStatusHandle;
import com.webank.webase.node.mgr.precompiled.entity.ChainGovernanceHandle;
import com.webank.webase.node.mgr.precompiled.permission.ChainGovernService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import node.mgr.test.gm.TestBase;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class ChainGovernTest extends TestBase {

    @Autowired
    private ChainGovernService chainGovernService;
    private static final Integer groupId = 1;
    private static final String adminAddress = "0xd031e61f6dc4dedd7d77f90128ed33caafbed0af";
    private static final String operatorAddress = "0x304852a7cc6511e62c37b6e189850861e41282b0";
    private static final String adminSignUserId = "037bda25bbb34067821860e9d743e9f2";

    @Test
    public void testList() {
        System.out.println("test list all chain governance:");
        System.out.println(chainGovernService.listCommittee(groupId));
        System.out.println(chainGovernService.getThreshold(groupId));
        System.out.println(chainGovernService.getCommitteeWeight(groupId, adminAddress));
    }

    @Test
    public void testUpdateThreshold() {
        ChainGovernanceHandle param = new ChainGovernanceHandle();
        param.setGroupId(groupId);
        param.setFromAddress(adminAddress);
        param.setThreshold(48);
        System.out.println("testUpdateThreshold: ");
        System.out.println(chainGovernService.updateThreshold(param));
    }

    @Test
    public void testQueryList() {
        AddressStatusHandle param = new AddressStatusHandle();
        param.setGroupId(groupId);
        List<String> addressList = new ArrayList<>();
        addressList.add(adminAddress);
        addressList.add(operatorAddress);
        param.setAddressList(addressList);
        System.out.println("query address status list: ");
        System.out.println(chainGovernService.listAccountStatus(param));
    }
}

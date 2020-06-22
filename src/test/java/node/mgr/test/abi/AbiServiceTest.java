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

package node.mgr.test.abi;

import com.webank.webase.node.mgr.base.tools.JsonTools;
import com.webank.webase.node.mgr.Application;
import com.webank.webase.node.mgr.abi.AbiService;
import com.webank.webase.node.mgr.abi.entity.AbiInfo;
import com.webank.webase.node.mgr.abi.entity.ReqAbiListParam;
import com.webank.webase.node.mgr.abi.entity.ReqImportAbi;
import com.webank.webase.node.mgr.base.enums.SqlSortType;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class AbiServiceTest {
	@Autowired
	AbiService abiService;

	@Test
	public void testInsert() {
		int count = abiService.countOfAbi();
		ReqImportAbi abiInsert = new ReqImportAbi();
		abiInsert.setGroupId(1);
		abiInsert.setContractAddress("0xd8e1e0834b38081982f4a080aeae350a6d422915");
		abiInsert.setContractName("Hello");
		String abiStr = "[{\"constant\":true,\"inputs\":[],\"name\":\"get\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256[]\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_ua\",\"type\":\"uint256[]\"}],\"name\":\"set\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"}]";
		abiInsert.setContractAbi(JsonTools.toJavaObjectList(abiStr, Object.class));
		abiService.saveAbi(abiInsert);
		int afterCount = abiService.countOfAbi();
		Assert.assertTrue("insert failed", afterCount > count);
	}

	@Test
	public void testListAbi() {
		ReqAbiListParam param = new ReqAbiListParam(0, 5,
				SqlSortType.DESC.getValue());
		param.setGroupId(1);
		List<AbiInfo> resList = abiService.getListByGroupId(param);
		System.out.println(resList);
		Assert.assertTrue("res is empty", !resList.isEmpty());
	}

	@Test
	public void testDeleteAbi() {
		int count = abiService.countOfAbi();
		abiService.delete(1);
		int afterCount = abiService.countOfAbi();
		Assert.assertTrue("insert failed", afterCount < count);
	}
}

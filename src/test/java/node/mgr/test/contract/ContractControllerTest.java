/**
 * Copyright 2014-2019  the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package node.mgr.test.contract;

import com.alibaba.fastjson.JSON;
import com.webank.webase.node.mgr.Application;
import com.webank.webase.node.mgr.contract.entity.QueryContractParam;
import com.webank.webase.node.mgr.front.entity.FrontInfo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@WebAppConfiguration
public class ContractControllerTest {
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Before
    public void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void testQueryContractList() throws Exception {
        QueryContractParam param = new QueryContractParam();
        param.setGroupId(2);
        param.setContractName("OK");
        param.setContractAddress("0x19146d3a2f138aacb97ac52dd45dd7ba7cb3e04a");
        param.setPageNumber(1);
        param.setPageSize(10);

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.post("/contract/contractList").
            content(JSON.toJSONString(param)).
            contentType(MediaType.APPLICATION_JSON)
        );
        resultActions.
            andExpect(MockMvcResultMatchers.status().isOk()).
            andDo(MockMvcResultHandlers.print());
        System.out.println("response:"+resultActions.andReturn().getResponse().getContentAsString());
    }

    @Test
    public void testFindByPartOfBytecodeBin() throws Exception {
        String bytecodeBin = "6060604052341561000c57fe5b5b6001600060000160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055506402540be4006000600101819055506002600260000160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555060006002600101819055505b5b610443806100c26000396000f30060606040526000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff16806366c99139146100465780636d4ce63c14610066575bfe5b341561004e57fe5b610064600480803590602001909190505061008c565b005b341561006e57fe5b610076610264565b6040518082815260200191505060405180910390f35b806000600101540360006001018190555080600260010160008282540192505081905550600480548060010182816100c49190610272565b916000526020600020906004020160005b608060405190810160405280604060405190810160405280600881526020017f32303137303431330000000000000000000000000000000000000000000000008152508152602001600060000160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001600260000160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200185815250909190915060008201518160000190805190602001906101c49291906102a4565b5060208201518160010160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555060408201518160020160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff160217905550606082015181600301555050505b50565b600060026001015490505b90565b81548183558181151161029f5760040281600402836000526020600020918201910161029e9190610324565b5b505050565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f106102e557805160ff1916838001178555610313565b82800160010185558215610313579182015b828111156103125782518255916020019190600101906102f7565b5b50905061032091906103aa565b5090565b6103a791905b808211156103a357600060008201600061034491906103cf565b6001820160006101000a81549073ffffffffffffffffffffffffffffffffffffffff02191690556002820160006101000a81549073ffffffffffffffffffffffffffffffffffffffff021916905560038201600090555060040161032a565b5090565b90565b6103cc91905b808211156103c85760008160009055506001016103b0565b5090565b90565b50805460018160011615610100020316600290046000825580601f106103f55750610414565b601f01602090049060005260206000209081019061041391906103aa565b5b505600a165627a7a72305820d453cb481a312519166e409e7248d76d8c2672458c08b9500945a4004a1b69020029";
        String partOfBytecodeBin = "test"+bytecodeBin;
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get("/contract/findByPartOfBytecodeBin/2/"+partOfBytecodeBin));
        resultActions.
            andExpect(MockMvcResultMatchers.status().isOk()).
            andDo(MockMvcResultHandlers.print());
        System.out.println("response:"+resultActions.andReturn().getResponse().getContentAsString());
    }
}
/**
 * Copyright 2014-2020  the original author or authors.
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
package node.mgr.test.transaction;

import com.webank.webase.node.mgr.Application;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
public class TransHashControllerTest {
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Before
    public void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void testGetTransList() throws Exception {
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get("/transaction/transList/1/1/15"));
        resultActions.
            andExpect(MockMvcResultMatchers.status().isOk()).
            andDo(MockMvcResultHandlers.print());
        System.out.println("=================================response:"+resultActions.andReturn().getResponse().getContentAsString());
    }

    @Test
    public void testGetTransactionReceipt() throws Exception {
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get("/transaction/transactionReceipt/1/0xda879949df6b5d75d2d807f036b461e0cebcc1abaccac119c9a282d3941a4818"));
        resultActions.
            andExpect(MockMvcResultMatchers.status().isOk()).
            andDo(MockMvcResultHandlers.print());
        System.out.println("=================================response:"+resultActions.andReturn().getResponse().getContentAsString());
    }


    @Test
    public void testGetTransactionByHash() throws Exception {
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get("/transaction/transInfo/1/0xda879949df6b5d75d2d807f036b461e0cebcc1abaccac119c9a282d3941a4818"));
        resultActions.
            andExpect(MockMvcResultMatchers.status().isOk()).
            andDo(MockMvcResultHandlers.print());
        System.out.println("=================================response:"+resultActions.andReturn().getResponse().getContentAsString());
    }
}

/**
 * Copyright 2014-2020  the original author or authors.
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
package node.mgr.test.user;

import com.webank.webase.node.mgr.base.tools.JsonTools;
import com.webank.webase.node.mgr.Application;
import com.webank.webase.node.mgr.frontgroupmap.FrontGroupMapCache;
import com.webank.webase.node.mgr.user.entity.BindUserInputParam;
import com.webank.webase.node.mgr.user.entity.NewUserInputParam;
import com.webank.webase.node.mgr.user.entity.UpdateUserInputParam;

import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.crypto.gm.GenCredential;
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
public class UserControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private FrontGroupMapCache frontGroupMapCache;

    @Before
    public void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void testNewUser() throws Exception {
        NewUserInputParam newUser = new NewUserInputParam();
        newUser.setUserName("cnsUser");
        newUser.setGroupId(1);

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.post("/user/userInfo").
            content(JsonTools.toJSONString(newUser)).
            contentType(MediaType.APPLICATION_JSON_UTF8)
        );
        resultActions.
            andExpect(MockMvcResultMatchers.status().isOk()).
            andDo(MockMvcResultHandlers.print());
        System.out
            .println("response:" + resultActions.andReturn().getResponse().getContentAsString());
    }

    @Test
    public void testUpdateUser() throws Exception {
        UpdateUserInputParam updateUser = new UpdateUserInputParam();
        updateUser.setUserId(700001);
        updateUser.setDescription("testtttttttttttttttttttttttt");

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.put("/user/userInfo").
            content(JsonTools.toJSONString(updateUser)).
            contentType(MediaType.APPLICATION_JSON_UTF8)
        );
        resultActions.
            andExpect(MockMvcResultMatchers.status().isOk()).
            andDo(MockMvcResultHandlers.print());
        System.out
            .println("response:" + resultActions.andReturn().getResponse().getContentAsString());
    }


    @Test
    public void testBindUser() throws Exception {
        BindUserInputParam newUser = new BindUserInputParam();
        newUser.setUserName("testPublic");
        newUser.setGroupId(1);
        newUser.setPublicKey(
            "tettewetrweewtettewetrweewtettewetrweewtettewetrweewtettewetrweewtettewetrweewtettewetrweewtettewetrweewtettewetrweewtettewetrweew");

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.post("/user/bind").
            content(JsonTools.toJSONString(newUser)).
            contentType(MediaType.APPLICATION_JSON_UTF8)
        );
        resultActions.
            andExpect(MockMvcResultMatchers.status().isOk()).
            andDo(MockMvcResultHandlers.print());
        System.out
            .println("response:" + resultActions.andReturn().getResponse().getContentAsString());
    }

    @Test
    public void testGetPrivateKey() throws Exception {
        ResultActions resultActions = mockMvc
            .perform(MockMvcRequestBuilders.get("/user/privateKey/700001"));
        resultActions.
            andExpect(MockMvcResultMatchers.status().isOk()).
            andDo(MockMvcResultHandlers.print());
        System.out.println(
            "======================response:" + resultActions.andReturn().getResponse()
                .getContentAsString());
    }

    @Test
    public void testGetUserList() throws Exception {
        ResultActions resultActions = mockMvc
            .perform(MockMvcRequestBuilders.get("/user/userList/1/1/15"));
        resultActions.
            andExpect(MockMvcResultMatchers.status().isOk()).
            andDo(MockMvcResultHandlers.print());
        System.out.println(
            "======================response:" + resultActions.andReturn().getResponse()
                .getContentAsString());
    }

    @Test
    public void testGenerateKey() throws Exception {
        // guomi use GenCredential
       Credentials credentials = GenCredential.create("3bed914595c159cbce70ec5fb6aff3d6797e0c5ee5a7a9224a21cae8932d84a4");
        System.out.println( credentials.getAddress());
        System.out.println( credentials.getEcKeyPair().getPrivateKey().toString(16));
        System.out.println(  credentials.getEcKeyPair().getPublicKey().toString(16));
    }

}

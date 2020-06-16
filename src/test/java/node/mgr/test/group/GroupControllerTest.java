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
package node.mgr.test.group;

import com.webank.webase.node.mgr.base.tools.JsonTools;
import com.webank.webase.node.mgr.Application;
import com.webank.webase.node.mgr.group.entity.ReqGenerateGroup;
import com.webank.webase.node.mgr.group.entity.ReqGroupStatus;
import com.webank.webase.node.mgr.group.entity.ReqOperateGroup;
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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@WebAppConfiguration
public class GroupControllerTest {
    private MockMvc mockMvc;
    private String targetNodeId = "dd7a2964007d583b719412d86dab9dcf773c61bccab18cb646cd480973de0827cc94fa84f33982285701c8b7a7f465a69e980126a77e8353981049831b550f5c";
    private Integer newGroupId = 2022;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Before
    public void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }
    @Test
    public void testGeneral() throws Exception {

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get("/group/general/1"));
        resultActions.
            andExpect(MockMvcResultMatchers.status().isOk()).
            andDo(MockMvcResultHandlers.print());
        System.out.println("=================================response:"+resultActions.andReturn().getResponse().getContentAsString());
    }

    @Test
    public void testGetAll() throws Exception {

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get("/group/all"));
        resultActions.
                andExpect(MockMvcResultMatchers.status().isOk()).
                andDo(MockMvcResultHandlers.print());
        System.out.println("=================================response:"+resultActions.andReturn().getResponse().getContentAsString());
    }


    @Test
    public void testGetAllInvalidIncluded() throws Exception {

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get("/group/all/invalidIncluded/1/5"));
        resultActions.
                andExpect(MockMvcResultMatchers.status().isOk()).
                andDo(MockMvcResultHandlers.print());
        System.out.println("=================================response:"+resultActions.andReturn().getResponse().getContentAsString());
    }

    @Test
    public void testGetAllInvalid() throws Exception {

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get("/group/all/4"));
        resultActions.
                andExpect(MockMvcResultMatchers.status().isOk()).
                andDo(MockMvcResultHandlers.print());
        System.out.println("=================================response:"+resultActions.andReturn().getResponse().getContentAsString());
    }


    /**
     * dynamic group manage
     */
    @Test
    public void testGenerateSingle() throws Exception {
        List<String> nodeList = new ArrayList<>();
        nodeList.add(targetNodeId);

        ReqGenerateGroup param = new ReqGenerateGroup();
        param.setGenerateGroupId(newGroupId);
        param.setTimestamp(BigInteger.valueOf(new Date().getTime()));
        param.setNodeList(nodeList);
        param.setDescription("test");

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.post("/group/generate/" + targetNodeId).
                content(JsonTools.toJSONString(param)).
                contentType(MediaType.APPLICATION_JSON_UTF8)
        );
        resultActions.
                andExpect(MockMvcResultMatchers.status().isOk()).
                andDo(MockMvcResultHandlers.print());
        System.out.println("response:"+resultActions.andReturn().getResponse().getContentAsString());
    }

    @Test
    public void testGenerate() throws Exception {
        List<String> nodeList = new ArrayList<>();
        nodeList.add(targetNodeId);

        ReqGenerateGroup param = new ReqGenerateGroup();
        param.setGenerateGroupId(newGroupId);
        param.setTimestamp(BigInteger.valueOf(new Date().getTime()));
        param.setNodeList(nodeList);
        param.setDescription("test");

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.post( "/group/generate").
                content(JsonTools.toJSONString(param)).
                contentType(MediaType.APPLICATION_JSON_UTF8)
        );
        resultActions.
                andExpect(MockMvcResultMatchers.status().isOk()).
                andDo(MockMvcResultHandlers.print());
        System.out.println("response:"+resultActions.andReturn().getResponse().getContentAsString());
    }

    @Test
    public void testOperate() throws Exception {
        ReqOperateGroup param = new ReqOperateGroup();
        param.setGenerateGroupId(newGroupId);
        param.setType("start");
//        param.setType("getStatus");
//        param.setType("stop");
//        param.setType("recover");
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .post("/group/operate/" + targetNodeId).
                content(JsonTools.toJSONString(param)).
                contentType(MediaType.APPLICATION_JSON_UTF8)
        );
        resultActions.
                andExpect(MockMvcResultMatchers.status().isOk()).
                andDo(MockMvcResultHandlers.print());
        System.out.println("=================================response:"+resultActions.andReturn().getResponse().getContentAsString());
    }

    @Test
    public void testUpdate() throws Exception {
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get("/group/update"));
        resultActions.
                andExpect(MockMvcResultMatchers.status().isOk()).
                andDo(MockMvcResultHandlers.print());
        System.out.println("=================================response:"+resultActions.andReturn().getResponse().getContentAsString());
    }

    @Test
    public void testGroupStatusList() throws Exception {
        ReqGroupStatus param = new ReqGroupStatus();
        List<Integer> groupIdList = new ArrayList<>();
        groupIdList.add(2020);
        groupIdList.add(3);
        groupIdList.add(1);
        groupIdList.add(2021);
        groupIdList.add(2023);
        param.setGroupIdList(groupIdList);
        List<String> nodeIdList = new ArrayList<>();
        nodeIdList.add(targetNodeId);
        param.setNodeIdList(nodeIdList);
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .post("/group/queryGroupStatus/list")
                .content(JsonTools.toJSONString(param))
                .contentType(MediaType.APPLICATION_JSON_UTF8)
        );
        resultActions.
//                andExpect(MockMvcResultMatchers.status().isOk()).
                andDo(MockMvcResultHandlers.print());
        System.out.println("=================================response:"+resultActions.andReturn().getResponse().getContentAsString());
    }
}

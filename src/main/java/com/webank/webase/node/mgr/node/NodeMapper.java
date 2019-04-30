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
package com.webank.webase.node.mgr.node;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * node data interface.
 */
@Repository
public interface NodeMapper {

    /**
     * Add new node data.
     */
    Integer add(TbNode tbNode);

    /**
     * Query the number of node according to some conditions.
     */
    Integer getCount(NodeParam nodeParam);


    /**
     * Query node list according to some conditions.
     */
    List<TbNode> getList(NodeParam nodeParam);

    /**
     * query tb_node by nodeip and p2pport.
     */
    TbNode queryNodeByIpAndP2pPort(@Param("nodeIp") String nodeIp,
        @Param("p2pPort") Integer p2pPort);

    /**
     * query node info.
     */
    TbNode queryByNodeId(@Param("nodeId") Integer nodeId);

    /**
     * update node info.
     */
    Integer update(TbNode dbNode);

    /**
     * query node info.
     */
    TbNode queryNodeInfo(NodeParam nodeParam);


    /**
     * delete by nodeId and groupId.
     */
    Integer deleteByNodeAndGroup(@Param("nodeId") String nodeId, @Param("groupId") Integer groupId);
    /**
     * delete by groupId.
     */
    Integer deleteByGroupId( @Param("groupId") Integer groupId);
}
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
package com.webank.webase.node.mgr.group;

import com.alibaba.fastjson.JSON;
import com.webank.webase.node.mgr.base.entity.ConstantCode;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.front.FrontService;
import com.webank.webase.node.mgr.node.NodeService;
import com.webank.webase.node.mgr.table.TableService;
import java.math.BigInteger;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * services for group data.
 */
@Log4j2
@Service
public class GroupService {

    @Autowired
    private GroupMapper groupMapper;
    @Autowired
    private FrontService frontService;
    @Autowired
    private NodeService nodeService;
    @Autowired
    private TableService tableService;


    /**
     * save group id
     */
    public void saveGroupId(int groupId, int nodeCount) {
        if (groupId == 0) {
            return;
        }
        //save group id
        String groupName = "group" + groupId;
        TbGroup tbGroup = new TbGroup(groupId, groupName, nodeCount);
        groupMapper.add(tbGroup);

        //create table by group id
        tableService.newTableByGroupId(groupId);
    }

    /**
     * update group latest block number.
     */
    public void updateGroupInfo(Integer groupId, BigInteger latestBlock)
        throws NodeMgrException {
        log.debug("start updateGroupInfo groupId:{} latestBlock:{} ", groupId,
            latestBlock);
        try {
            Integer affectRow = groupMapper.update(groupId, latestBlock);
            if (affectRow == 0) {
                log.info(
                    "fail updateGroupInfo. groupId:{}  latestBlock:{}. affect 0 rows"
                        + " of tb_group",
                    groupId, latestBlock);
                throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
            }
        } catch (RuntimeException ex) {
            log.debug("fail updateGroupInfo groupId:{} latestBlock:{}", groupId,
                latestBlock, ex);
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }
    }

    /**
     * query count of group.
     */
    public Integer countOfGroup(Integer groupId) throws NodeMgrException {
        log.debug("start countOfGroup groupId:{}", groupId);
        try {
            Integer count = groupMapper.getCount(groupId);
            log.debug("end countOfGroup groupId:{} count:{}", groupId, count);
            return count;
        } catch (RuntimeException ex) {
            log.error("fail countOfGroup", ex);
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }
    }

    /**
     * query all group info.
     */
    public List<TbGroup> getAllGroup() throws NodeMgrException {
        log.debug("start getAllGroup");
        // query group count
        int count = countOfGroup(null);
        if (count == 0) {
            return null;
        }

        try {
            List<TbGroup> groupList = groupMapper.getList();
            log.debug("end getAllGroup groupList:{}", JSON.toJSONString(groupList));
            return groupList;
        } catch (RuntimeException ex) {
            log.error("fail getAllGroup", ex);
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }
    }

    /**
     * Check the validity of the groupId.
     */
    public void checkgroupId(Integer groupId) throws NodeMgrException {
        log.debug("start checkgroupId groupId:{}", groupId);

        if (groupId == null) {
            log.error("fail checkgroupId groupId is null");
            throw new NodeMgrException(ConstantCode.GROUP_ID_NULL);
        }

        Integer groupCount = countOfGroup(groupId);
        log.debug("checkgroupId groupId:{} groupCount:{}", groupId, groupCount);
        if (groupCount == null || groupCount == 0) {
            throw new NodeMgrException(ConstantCode.INVALID_GROUP_ID);
        }
        log.debug("end checkgroupId");
    }

    /**
     * query latest statistical trans.
     */
    public List<StatisticalGroupTransInfo> queryLatestStatisticalTrans() throws NodeMgrException {
        log.debug("start queryLatestStatisticalTrans");
        try {
            // qurey list
            List<StatisticalGroupTransInfo> listStatisticalTrans = groupMapper
                .queryLatestStatisticalTrans();
            log.debug("end queryLatestStatisticalTrans listStatisticalTrans:{}",
                JSON.toJSONString(listStatisticalTrans));
            return listStatisticalTrans;
        } catch (RuntimeException ex) {
            log.error("fail queryLatestStatisticalTrans", ex);
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }
    }

    /**
     * query group overview information.
     */
    public GroupGeneral queryGroupGeneral(Integer groupId) throws NodeMgrException {
        log.debug("start queryGroupGeneral groupId:{}", groupId);
        try {
            // qurey general info from tb_group
            GroupGeneral generalInfo = groupMapper.getGeneral(groupId);
            log.debug("end queryGroupGeneral generalInfo:{}",
                JSON.toJSONString(generalInfo));
            return generalInfo;
        } catch (RuntimeException ex) {
            log.error("fail queryGroupGeneral", ex);
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }
    }


}

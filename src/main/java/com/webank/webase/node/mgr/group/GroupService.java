/*
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
package com.webank.webase.node.mgr.group;

import com.alibaba.fastjson.JSON;
import com.webank.webase.node.mgr.base.entity.ConstantCode;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
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

    /**
     * update group latest block number.
     */
    public void updateNetworkInfo(Integer groupId, BigInteger latestBlock)
        throws NodeMgrException {
        log.debug("start updateNetworkInfo groupId:{} latestBlock:{} ", groupId,
            latestBlock);
        try {
            Integer affectRow = groupMapper.updateNetworkInfo(groupId, latestBlock);
            if (affectRow == 0) {
                log.info(
                    "fail updateNetworkInfo. groupId:{}  latestBlock:{}. affect 0 rows"
                        + " of tb_group",
                    groupId, latestBlock);
                throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
            }
        } catch (RuntimeException ex) {
            log.debug("fail updateNetworkInfo groupId:{} latestBlock:{}", groupId,
                latestBlock, ex);
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }
    }

    /**
     * query count of group.
     */
    public Integer countOfNetwork(Integer groupId) throws NodeMgrException {
        log.debug("start countOfNetwork groupId:{}", groupId);
        try {
            Integer count = groupMapper.countOfNetwork(groupId);
            log.debug("end countOfNetwork groupId:{} count:{}", groupId, count);
            return count;
        } catch (RuntimeException ex) {
            log.error("fail countOfNetwork", ex);
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }
    }

    /**
     * query all group info.
     */
    public List<TbGroup> getAllNetwork() throws NodeMgrException {
        log.debug("start getAllNetwork");
        // query group count
        Integer count = countOfNetwork(null);

        List<TbGroup> listOfNetwork = null;
        if (count != null && count > 0) {
            try {
                // qurey group list
                listOfNetwork = groupMapper.listAllNetwork();
            } catch (RuntimeException ex) {
                log.error("fail countOfNetwork", ex);
                throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
            }
        }
        log.debug("end getAllNetwork listOfNetwork:{}", JSON.toJSONString(listOfNetwork));
        return listOfNetwork;
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

        Integer groupCount = countOfNetwork(groupId);
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
    public GroupGeneral queryNetworkGeneral(Integer groupId) throws NodeMgrException {
        log.debug("start queryNetworkGeneral groupId:{}", groupId);
        try {
            // qurey general info from tb_group
            GroupGeneral generalInfo = groupMapper.queryNetworkGeneral(groupId);
            log.debug("end queryNetworkGeneral generalInfo:{}",
                JSON.toJSONString(generalInfo));
            return generalInfo;
        } catch (RuntimeException ex) {
            log.error("fail queryNetworkGeneral", ex);
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }
    }

    /**
     * reset trans count of group.
     */
    public void resetTransCount(Integer groupId) throws NodeMgrException {
        log.debug("start resetTransCount groupId:{}", groupId);
        try {
            groupMapper.resetTransCount(groupId);
        } catch (RuntimeException ex) {
            log.error("fail resetTransCount", ex);
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }
    }
}

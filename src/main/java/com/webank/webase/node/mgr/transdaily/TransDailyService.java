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
package com.webank.webase.node.mgr.transdaily;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.base.tools.JsonTools;
import com.webank.webase.node.mgr.group.GroupService;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class TransDailyService {

    @Autowired
    private GroupService groupService;
    @Autowired
    private TbTransDailyMapper tbTransDailyMapper;


    /**
     * query Trading within seven days.
     */
    public List<SeventDaysTrans> listSeventDayOfTrans(Integer groupId) throws NodeMgrException {
        log.debug("start listSeventDayOfTrans groupId:{}", groupId);
        try {
            // qurey
            List<SeventDaysTrans> transList = tbTransDailyMapper
                .listSeventDayOfTransDaily(groupId);
            log.debug("end listSeventDayOfTrans transList:{}", JsonTools.toJSONString(transList));
            return transList;
        } catch (RuntimeException ex) {
            log.debug("fail listSeventDayOfTrans groupId:{}", groupId, ex);
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }
    }

    /**
     * update trans daily info.
     */
    public void updateTransDaily(Integer groupId, LocalDate transDay, BigInteger oldBlockNumber,
        BigInteger latestBlockNumber, BigInteger transCount)
        throws NodeMgrException {
        log.debug(
            "start updateTransDaily groupId:{} transDay:{} oldBlockNumber:{} "
                + "latestBlockNumber:{} transCount:{}", groupId, JsonTools.toJSONString(transDay),
            oldBlockNumber, latestBlockNumber, transCount);
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("groupId", groupId);
        paramMap.put("oldBlockNumber", oldBlockNumber);
        paramMap.put("transDay", transDay);
        paramMap.put("latestBlockNumber", latestBlockNumber);
        paramMap.put("transCount", transCount);
        Integer affectRow = 0;
        try {
            affectRow = tbTransDailyMapper.updateTransDaily(paramMap);
        } catch (RuntimeException ex) {
            log.error(
                "fail updateTransDaily groupId:{} transDay:{} oldBlockNumber:{}"
                    + " latestBlockNumber:{} transCount:{}",
                groupId, transDay, oldBlockNumber, latestBlockNumber, transCount, ex);
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }

        log.debug("end updateTransDaily affectRow:{}", affectRow);
    }

    /**
     * add trans daily info.
     */
    public void addTbTransDailyInfo(Integer groupId, LocalDate transDay, Integer transCount,
        BigInteger blockNumber) throws NodeMgrException {
        log.debug("start addTbTransDailyInfo groupId:{} transDay:{} transCount:{} blockNumber:{}",
            groupId, JsonTools.toJSONString(transDay),
            transCount, blockNumber);

        // check group id
        groupService.checkGroupId(groupId);

        // add row
        TbTransDaily rowParam = new TbTransDaily(groupId, transDay, transCount, blockNumber);
        try {
            tbTransDailyMapper.addTransDailyRow(rowParam);
        } catch (RuntimeException ex) {
            log.error(
                "start addTbTransDailyInfo groupId:{} transDay:{} transCount:{} blockNumber:{}",
                groupId, JsonTools.toJSONString(transDay),
                transCount, blockNumber, ex);
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }

        log.debug("end addNodeInfo");
    }


    /**
     * delete by groupId.
     */
    public void deleteByGroupId(int groupId) {
        if (groupId == 0) {
            return;
        }
        tbTransDailyMapper.deleteByGroupId(groupId);
    }
}
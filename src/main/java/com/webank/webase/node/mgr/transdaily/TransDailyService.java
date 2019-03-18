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
package com.webank.webase.node.mgr.transdaily;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.webank.webase.node.mgr.base.entity.ConstantCode;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.network.NetworkService;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class TransDailyService {

    @Autowired
    private NetworkService networkService;
    @Autowired
    private TbTransDailyMapper tbTransDailyMapper;

    /**
     * query Trading within seven days.
     */
    public List<SeventDaysTrans> listSeventDayOfTrans(Integer networkId) throws NodeMgrException {
        log.debug("start listSeventDayOfTrans networkId:{}", networkId);
        try {
            // qurey
            List<SeventDaysTrans> transList = tbTransDailyMapper
                .listSeventDayOfTransDaily(networkId);
            log.debug("end listSeventDayOfTrans transList:{}", JSON.toJSONString(transList));
            return transList;
        } catch (RuntimeException ex) {
            log.debug("fail listSeventDayOfTrans networkId:{}", networkId, ex);
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }
    }

    /**
     * update trans daily info.
     */
    public void updateTransDaily(Integer networkId, LocalDate transDay, BigInteger oldBlockNumber,
        BigInteger latestBlockNumber, BigInteger transCount)
        throws NodeMgrException {
        log.debug(
            "start updateTransDaily networkId:{} transDay:{} oldBlockNumber:{} "
                + "latestBlockNumber:{} transCount:{}",networkId, JSON.toJSONString(transDay),
            oldBlockNumber, latestBlockNumber, transCount);
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("networkId", networkId);
        paramMap.put("oldBlockNumber", oldBlockNumber);
        paramMap.put("transDay", transDay);
        paramMap.put("latestBlockNumber", latestBlockNumber);
        paramMap.put("transCount", transCount);
        Integer affectRow = 0;
        try {
            affectRow = tbTransDailyMapper.updateTransDaily(paramMap);
        } catch (RuntimeException ex) {
            log.error(
                "fail updateTransDaily networkId:{} transDay:{} oldBlockNumber:{}"
                    + " latestBlockNumber:{} transCount:{}",
                networkId,transDay, oldBlockNumber, latestBlockNumber, transCount, ex);
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }

        log.debug("end updateTransDaily affectRow:{}", affectRow);
    }

    /**
     * add trans daily info.
     */
    public void addTbTransDailyInfo(Integer networkId, LocalDate transDay, Integer transCount,
        BigInteger blockNumber) throws NodeMgrException {
        log.debug("start addTbTransDailyInfo networkId:{} transDay:{} transCount:{} blockNumber:{}",
            networkId, JSON.toJSONString(transDay),
            transCount, blockNumber);

        // check network id
        networkService.checkNetworkId(networkId);

        // add row
        TbTransDaily rowParam = new TbTransDaily(networkId, transDay, transCount, blockNumber);
        try {
            tbTransDailyMapper.addTransDailyRow(rowParam);
        } catch (RuntimeException ex) {
            log.error(
                "start addTbTransDailyInfo networkId:{} transDay:{} transCount:{} blockNumber:{}",
                networkId, JSON.toJSONString(transDay),
                transCount, blockNumber, ex);
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }

        log.debug("end addNodeInfo");
    }

    /**
     * query max block number.
     */
    public BigInteger queryMaxBlockByNetwork(Integer networkId)
        throws NodeMgrException, JsonProcessingException {
        log.debug("start queryMaxBlockByNetwork networkId:{}", networkId);

        try {
            BigInteger maxBlockNumber = tbTransDailyMapper.queryMaxBlockByNetwork(networkId);
            log.debug("start queryMaxBlockByNetwork networkId:{} maxBlockNumber:{}",
                maxBlockNumber);
            return maxBlockNumber;
        } catch (RuntimeException ex) {
            log.error("start queryMaxBlockByNetwork networkId:{}", networkId, ex);
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }

    }
}
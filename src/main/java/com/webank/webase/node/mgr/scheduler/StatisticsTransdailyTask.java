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
package com.webank.webase.node.mgr.scheduler;

import com.webank.webase.node.mgr.base.enums.SqlSortType;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.block.BlockListParam;
import com.webank.webase.node.mgr.block.BlockService;
import com.webank.webase.node.mgr.block.TbBlock;
import com.webank.webase.node.mgr.network.NetworkService;
import com.webank.webase.node.mgr.network.StatisticalNetworkTransInfo;
import com.webank.webase.node.mgr.transdaily.TransDailyService;
import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Timed tasks for counting daily transaction data.
 */
@Log4j2
@Component
public class StatisticsTransdailyTask {

    @Autowired
    private NetworkService networkService;
    @Autowired
    private BlockService blockService;
    @Autowired
    private TransDailyService transDailyService;

    /**
     * counting daily transaction data.
     */
    public synchronized void updateTransdailyData() {
        Instant startTime = Instant.now();
        log.info("start updateTransdailyData startTime:{}", startTime.toEpochMilli());
        try {

            // query all network statistical info
            List<StatisticalNetworkTransInfo> networkStatisticalList = networkService
                .queryLatestStatisticalTrans();

            // traverse network list
            traverseNetList(networkStatisticalList);

        } catch (Exception ex) {
            log.error("fail updateTransdailyData", ex);
        }
        log.info("end updateTransdailyData useTime:{}",
            Duration.between(startTime, Instant.now()).toMillis());
    }

    /**
     * traverse network list.
     */
    private void traverseNetList(List<StatisticalNetworkTransInfo> networkStatisticalList)
        throws NodeMgrException {
        if (networkStatisticalList == null | networkStatisticalList.size() == 0) {
            log.error("fail updateTransdailyData. no network information exists");
            return;
        }

        // traverse network list
        for (StatisticalNetworkTransInfo statisticalInfo : networkStatisticalList) {
            LocalDate latestSaveDay = statisticalInfo.getMaxDay();
            BigInteger latestSaveBlockNumber = Optional.ofNullable(statisticalInfo.getBlockNumber())
                .orElse(BigInteger.ZERO);
            BigInteger netTransCount = Optional.ofNullable(statisticalInfo.getTransCount())
                .orElse(BigInteger.ZERO);
            BigInteger maxBlockNumber = latestSaveBlockNumber;
            Integer networkId = statisticalInfo.getNetworkId();

            // query block list
            BlockListParam queryParam = new BlockListParam(networkId, maxBlockNumber, latestSaveDay,
                SqlSortType.ASC.getValue());
            List<TbBlock> blockList = blockService.queryBlockList(queryParam);

            // Traversing block list
            if (blockList == null | blockList.size() == 0) {
                log.info("updateTransdailyData jump over .This chain [{}] did not find new block",
                    networkId);
                return;
            }
            for (int i = 0; i < blockList.size(); i++) {
                TbBlock tbBlock = blockList.get(i);
                LocalDate blockDate = tbBlock.getBlockTimestamp() == null ? null
                    : tbBlock.getBlockTimestamp().toLocalDate();
                if (blockDate == null) {
                    log.warn("updateTransdailyData jump over . blockDate is null");
                    return;
                }

                BigInteger blockTransCount = new BigInteger(
                    String.valueOf(tbBlock.getTransCount()));
                if (blockDate.equals(latestSaveDay)) {
                    netTransCount = netTransCount.add(blockTransCount);
                    maxBlockNumber = tbBlock.getBlockNumber();
                } else {
                    if (netTransCount.intValue() > 0 && latestSaveDay != null) {
                        transDailyService
                            .updateTransDaily(networkId, latestSaveDay, latestSaveBlockNumber,
                                maxBlockNumber, netTransCount);
                    }

                    transDailyService
                        .addTbTransDailyInfo(networkId, blockDate, tbBlock.getTransCount(),
                            tbBlock.getBlockNumber());

                    latestSaveBlockNumber = tbBlock.getBlockNumber();
                    latestSaveDay = blockDate;
                    netTransCount = blockTransCount;
                    maxBlockNumber = tbBlock.getBlockNumber();
                }

                //latest block of list
                if (i == (blockList.size() - 1)) {
                    transDailyService
                        .updateTransDaily(networkId, latestSaveDay, latestSaveBlockNumber,
                            maxBlockNumber, netTransCount);
                }
            }

            networkService.resetTransCount(networkId);
        }
    }
}

/**
 * Copyright 2014-2021  the original author or authors.
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
package com.webank.webase.node.mgr.base.config.scheduler;

import com.webank.webase.node.mgr.base.enums.SqlSortType;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.block.entity.BlockListParam;
import com.webank.webase.node.mgr.block.BlockService;
import com.webank.webase.node.mgr.block.entity.TbBlock;
import com.webank.webase.node.mgr.group.GroupService;
import com.webank.webase.node.mgr.group.entity.StatisticalGroupTransInfo;
import com.webank.webase.node.mgr.transdaily.TransDailyService;
import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Timed tasks for counting daily transaction data.
 * using in web's charts
 */
@Log4j2
@Component
public class StatisticsTransdailyTask {

    @Autowired
    private GroupService groupService;
    @Autowired
    private BlockService blockService;
    @Autowired
    private TransDailyService transDailyService;


    @Scheduled(cron = "${constant.statisticsTransDailyCron}")
    public void taskStart() {
        updateTransdailyData();
    }

    /**
     * counting daily transaction data.
     */
    public synchronized void updateTransdailyData() {
        Instant startTime = Instant.now();
        log.debug("start updateTransdailyData startTime:{}", startTime.toEpochMilli());
        try {

            // query all group statistical info
            List<StatisticalGroupTransInfo> groupStatisticalList = groupService
                .queryLatestStatisticalTrans();

            // traverse group list
            traverseNetList(groupStatisticalList);

        } catch (Exception ex) {
            log.error("fail updateTransdailyData", ex);
        }
        log.debug("end updateTransdailyData useTime:{}",
            Duration.between(startTime, Instant.now()).toMillis());
    }

    /**
     * traverse group list.
     */
    private void traverseNetList(List<StatisticalGroupTransInfo> groupStatisticalList)
        throws NodeMgrException {
        if (groupStatisticalList == null | groupStatisticalList.size() == 0) {
            log.warn("updateTransdailyData jump over: no group information exists");
            return;
        }

        // traverse group list
        for (StatisticalGroupTransInfo statisticalInfo : groupStatisticalList) {
            LocalDate latestSaveDay = statisticalInfo.getMaxDay();
            BigInteger latestSaveBlockNumber = Optional.ofNullable(statisticalInfo.getBlockNumber())
                .orElse(BigInteger.ZERO);
            BigInteger netTransCount = Optional.ofNullable(statisticalInfo.getTransCount())
                .orElse(BigInteger.ZERO);
            BigInteger maxBlockNumber = latestSaveBlockNumber;
            Integer groupId = statisticalInfo.getGroupId();

            // query block list
            BlockListParam queryParam = new BlockListParam(maxBlockNumber, latestSaveDay,
                SqlSortType.ASC.getValue());
            List<TbBlock> blockList = blockService.queryBlockList(groupId,queryParam);

            // Traversing block list
            if (blockList == null | blockList.size() == 0) {
                log.debug("updateTransdailyData jump over .This chain [{}] did not find new block",
                    groupId);
                continue;
            }
            for (int i = 0; i < blockList.size(); i++) {
                TbBlock tbBlock = blockList.get(i);
                LocalDate blockDate = tbBlock.getBlockTimestamp() == null ? null
                    : tbBlock.getBlockTimestamp().toLocalDate();
                if (blockDate == null) {
                    log.debug("updateTransdailyData jump over . blockDate is null");
                    continue;
                }

                BigInteger blockTransCount = new BigInteger(
                    String.valueOf(tbBlock.getTransCount()));
                if (blockDate.equals(latestSaveDay)) {
                    netTransCount = netTransCount.add(blockTransCount);
                    maxBlockNumber = tbBlock.getBlockNumber();
                } else {
                    if (netTransCount.intValue() > 0 && latestSaveDay != null) {
                        transDailyService
                            .updateTransDaily(groupId, latestSaveDay, latestSaveBlockNumber,
                                maxBlockNumber, netTransCount);
                    }

                    transDailyService
                        .addTbTransDailyInfo(groupId, blockDate, tbBlock.getTransCount(),
                            tbBlock.getBlockNumber());

                    latestSaveBlockNumber = tbBlock.getBlockNumber();
                    latestSaveDay = blockDate;
                    netTransCount = blockTransCount;
                    maxBlockNumber = tbBlock.getBlockNumber();
                }

                //latest block of list
                if (i == (blockList.size() - 1)) {
                    transDailyService
                        .updateTransDaily(groupId, latestSaveDay, latestSaveBlockNumber,
                            maxBlockNumber, netTransCount);
                }
            }
        }
    }
}

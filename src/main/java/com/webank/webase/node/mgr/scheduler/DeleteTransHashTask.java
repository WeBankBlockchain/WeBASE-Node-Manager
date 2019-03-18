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

import com.webank.webase.node.mgr.base.properties.ConstantProperties;
import com.webank.webase.node.mgr.block.MinMaxBlock;
import com.webank.webase.node.mgr.transhash.TransHashService;
import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class DeleteTransHashTask {

    @Autowired
    private TransHashService transHashService;
    @Autowired
    private ConstantProperties constantsProperties;

    /**
     * delete some transaction info.
     */
    public void deleteTransHash() {
        Instant startTime = Instant.now();
        log.info("start deleteTransHash startTime:{}", startTime.toEpochMilli());
        try {
            List<MinMaxBlock> listOfTrans = transHashService.queryMinMaxBlock();
            if (listOfTrans == null || listOfTrans.size() == 0) {
                log.warn("fail deleteTransHash:Did not find any trans");
                return;
            }

            for (MinMaxBlock minMaxBlock : listOfTrans) {
                Integer networkId = minMaxBlock.getNetworkId();
                BigInteger maxBlockNumber = minMaxBlock.getMaxBlockNumber();
                BigInteger minBLockNumber = minMaxBlock.getMinBLockNumber();
                if (networkId == null || maxBlockNumber == null || minBLockNumber == null) {
                    log.warn(
                        "deleteTransHash jump over .networkId[{}],maxBlockNumber[{}],"
                            + "minBLockNumber[{}]",
                        networkId, maxBlockNumber,
                        minBLockNumber);
                    continue;
                }

                BigInteger subBlockNumber = maxBlockNumber
                    .subtract(constantsProperties.getTransRetainMax());

                if (minBLockNumber.compareTo(subBlockNumber) > 0) {
                    log.info(
                        "deleteTransHash jump over .maxBlockNumber[{}],"
                            + "minBLockNumber[{}],transDailyBlockNumber[{}]",
                        maxBlockNumber, minBLockNumber);
                    continue;
                }

                Integer effectRows = transHashService.deleteSomeTrans(networkId, subBlockNumber);
                log.info("period deleteTransHash.  networkId[{}] effectRows:[{}]", networkId,
                    effectRows);
            }

        } catch (Exception ex) {
            log.error("deleteTransHash jump over", ex);
        }
        log.info("end deleteTransHash useTime:{}",
            Duration.between(startTime, Instant.now()).toMillis());
    }
}

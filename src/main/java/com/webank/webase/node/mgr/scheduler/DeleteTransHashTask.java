/**
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
import com.webank.webase.node.mgr.block.entity.MinMaxBlock;
import com.webank.webase.node.mgr.group.GroupService;
import com.webank.webase.node.mgr.group.entity.TbGroup;
import com.webank.webase.node.mgr.transaction.TransHashService;
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
    private GroupService groupService;
    @Autowired
    private TransHashService transHashService;
    @Autowired
    private ConstantProperties constantsProperties;

    /**
     * start to delete block
     */
    public void deleteTransStart(){
        Instant startTime = Instant.now();
        log.info("start deleteTransStart startTime:{}", startTime.toEpochMilli());
        //get group list
        List<TbGroup> groupList = groupService.getAllGroup();
        if (groupList == null || groupList.size() == 0) {
            log.info("deleteTransStart jump over .not found any group");
            return;
        }

        //delete trans by groupId
        groupList.stream().forEach(group -> deleteTransByGroupId(group.getGroupId()));
        log.info("end deleteTransStart useTime:{} ",
            Duration.between(startTime, Instant.now()).toMillis());
    }

    /**
     * delete some transaction info.
     */
    public void deleteTransByGroupId(int groupId) {
        Instant startTime = Instant.now();
        log.info("start deleteTransHash startTime:{}", startTime.toEpochMilli());
        try {
            List<MinMaxBlock> listOfTrans =null;// transHashService.queryMinMaxBlock();  TODO
            if (listOfTrans == null || listOfTrans.size() == 0) {
                log.warn("fail deleteTransHash:Did not find any trans");
                return;
            }

            for (MinMaxBlock minMaxBlock : listOfTrans) {
                BigInteger maxBlockNumber = minMaxBlock.getMaxBlockNumber();
                BigInteger minBLockNumber = minMaxBlock.getMinBLockNumber();
                if ( maxBlockNumber == null || minBLockNumber == null) {
                    log.warn(
                        "deleteTransHash jump over .groupId[{}],maxBlockNumber[{}],"
                            + "minBLockNumber[{}]",
                        groupId, maxBlockNumber,
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

                Integer effectRows = transHashService.deleteSomeTrans(groupId, subBlockNumber);
                log.info("period deleteTransHash.  groupId[{}] effectRows:[{}]", groupId,
                    effectRows);
            }

        } catch (Exception ex) {
            log.error("deleteTransHash jump over", ex);
        }
        log.info("end deleteTransHash useTime:{}",
            Duration.between(startTime, Instant.now()).toMillis());
    }
}

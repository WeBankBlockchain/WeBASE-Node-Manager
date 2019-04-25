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
package com.webank.webase.node.mgr.scheduler;

import com.webank.webase.node.mgr.base.properties.ConstantProperties;
import com.webank.webase.node.mgr.block.BlockService;
import com.webank.webase.node.mgr.block.entity.MinMaxBlock;
import com.webank.webase.node.mgr.group.GroupService;
import com.webank.webase.node.mgr.group.entity.TbGroup;
import com.webank.webase.node.mgr.transdaily.TransDailyService;
import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * delete block information periodically.
 */
@Log4j2
@Component
public class DeleteBlockTask {

    @Autowired
    private GroupService groupService;
    @Autowired
    private BlockService blockService;
    @Autowired
    private TransDailyService transDailyService;
    @Autowired
    private ConstantProperties constantsProperties;

    /**
     * start to delete block
     */
    public void deleteBlockStart() {
        Instant startTime = Instant.now();
        log.info("start deleteBlockStart. startTime:{}", startTime.toEpochMilli());
        //get group list
        List<TbGroup> groupList = groupService.getAllGroup();
        if (groupList == null || groupList.size() == 0) {
            log.info("deleteBlockStart jump over .not found any group");
            return;
        }

        //delete block by groupId
        groupList.stream().forEach(group -> deleteBlockByGroupId(group.getGroupId()));
        log.info("end deleteBlockStart. useTime:{} ",
            Duration.between(startTime, Instant.now()).toMillis());
    }

    /**
     * delete some block information.
     */
    public void deleteBlockByGroupId(int groupId) {
        try {
            List<MinMaxBlock> listOfBlock = blockService.queryMinMaxBlock(groupId);
            if (listOfBlock == null || listOfBlock.size() == 0) {
                log.warn("fail deleteBlockByGroupId:Did not find any blocks");
                return;
            }

            for (MinMaxBlock minMaxBlock : listOfBlock) {
                if(Objects.isNull(minMaxBlock)){
                    continue;
                }
                BigInteger maxBlockNumber = minMaxBlock.getMaxBlockNumber();
                BigInteger minBLockNumber = minMaxBlock.getMinBLockNumber();
                if (maxBlockNumber == null || minBLockNumber == null) {
                    log.warn(
                        "deleteBlockByGroupId jump over .groupId[{}],maxBlockNumber[{}],"
                            + "minBLockNumber[{}]", groupId, maxBlockNumber, minBLockNumber);
                    continue;
                }

                BigInteger subBlockNumber = maxBlockNumber
                    .subtract(constantsProperties.getBlockRetainMax());
                BigInteger transDailyBlockNumber = transDailyService
                    .queryMaxBlockByGroup(groupId);

                if (minBLockNumber.compareTo(subBlockNumber) > 0
                    || subBlockNumber.compareTo(transDailyBlockNumber) > 0) {
                    log.info(
                        "deleteBlockByGroupId jump over .maxBlockNumber[{}],"
                            + "minBLockNumber[{}],transDailyBlockNumber[{}]",
                        maxBlockNumber,
                        minBLockNumber, transDailyBlockNumber);
                    continue;
                }

                Integer effectRows = blockService.deleteSomeBlocks(groupId, subBlockNumber);
                log.info("period deleteBlockByGroupId.  groupId[{}] effectRows:[{}]",
                    groupId, effectRows);
            }
        } catch (Exception ex) {
            log.error("deleteBlockByGroupId jump over", ex);
        }
    }
}

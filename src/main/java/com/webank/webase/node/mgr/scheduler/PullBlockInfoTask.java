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
import com.webank.webase.node.mgr.block.entity.BlockInfo;
import com.webank.webase.node.mgr.frontinterface.FrontInterfaceService;
import com.webank.webase.node.mgr.group.GroupService;
import com.webank.webase.node.mgr.group.entity.TbGroup;
import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * get block info from chain.
 */
@Log4j2
@Component
public class PullBlockInfoTask {

    @Autowired
    private GroupService groupService;
    @Autowired
    private ConstantProperties cProperties;
    @Autowired
    private BlockService blockService;
    @Autowired
    private FrontInterfaceService frontInterfaceService;
    private Instant latestTimeQueryDb = null;

    /**
     * task start
     */
    public void startPull() {
        Instant startTime = Instant.now();
        log.info("start startPull startTime:{}", startTime.toEpochMilli());
        List<TbGroup> groupList = groupService.getAllGroup();
        latestTimeQueryDb = Instant.now();
        if (groupList == null || groupList.size() == 0) {
            log.info("pullBlock jump over .not found any group");
            return;
        }
        //one group one thread
        List<Thread> threadList = new ArrayList<>();
        for (int i = 0; i < groupList.size(); i++) {
            TbGroup tbGroup = groupList.get(i);
            Thread t = new Thread(() -> pullBlockByGroupId(tbGroup.getGroupId()));
            t.start();
            threadList.add(t);
        }
        threadList.stream().forEach(t -> {
            try {
                t.join();
            } catch (InterruptedException e) {
                log.error("pull block exception", e);
            }
        });
        log.info("end startPull useTime:{} ",
            Duration.between(startTime, Instant.now()).toMillis());
    }

    /**
     * get block from chain by groupId
     */
    private void pullBlockByGroupId(int groupId) {
        log.info("start pullBlockByGroupId groupId:{}", groupId);
        while (isLatestTimeQueryDbValid()) {
            try {
                Thread.sleep(cProperties.getPullBlockSleepTime());

                //get next block
                BigInteger nextBlock = getNextBlockNumber(groupId);
                //get block by number
                BlockInfo blockInfo = frontInterfaceService.getBlockByNumber(groupId, nextBlock);
                if (blockInfo == null || blockInfo.getNumber() == null) {
                    log.info("pullBlockByGroupId jump over. not found new block.");
                    continue;
                }
                //save block info
                blockService.saveBLockInfo(blockInfo, groupId);
            } catch (Exception ex) {
                log.error("fail pullBlockByGroupId. groupId:{} ", groupId, ex);
                break;
            }
        }
        log.info("end pullBlockByGroupId groupId:{}", groupId);
    }


    /**
     * get next blockNumber
     */
    private BigInteger getNextBlockNumber(int groupId) {
        BigInteger nextBlock = null;

        //get max blockNumber in table
        BigInteger latestBlockNumber = blockService.getLatestBlockNumber(groupId);

        if (latestBlockNumber == null) {
            if (cProperties.getIsBlockPullFromZero()) {
                nextBlock = BigInteger.ZERO;
            } else {
                nextBlock = frontInterfaceService.getLatestBlockNumber(groupId);
            }
        } else {
            nextBlock = latestBlockNumber.add(BigInteger.ONE);
        }
        return nextBlock;
    }


    /**
     * check latestTimeQueryDb.
     */
    private Boolean isLatestTimeQueryDbValid() {
        if (latestTimeQueryDb == null) {
            return false;
        }
        Long subTime = Duration.between(latestTimeQueryDb, Instant.now()).toMillis();
        return subTime < cProperties.getResetGroupListCycle();
    }
}
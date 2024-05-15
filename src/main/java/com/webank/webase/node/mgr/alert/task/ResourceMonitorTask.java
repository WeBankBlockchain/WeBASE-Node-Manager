/**
 * Copyright 2014-2021 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.webase.node.mgr.alert.task;

import com.qctc.host.api.RemoteHostService;
import com.qctc.host.api.model.HostDTO;
import com.webank.webase.node.mgr.alert.mail.MailService;
import com.webank.webase.node.mgr.alert.rule.AlertRuleService;
import com.webank.webase.node.mgr.alert.rule.entity.TbAlertRule;
import com.webank.webase.node.mgr.alert.task.entity.DockerStats;
import com.webank.webase.node.mgr.alert.task.entity.FrontHitStats;
import com.webank.webase.node.mgr.base.enums.AlertRuleType;
import com.webank.webase.node.mgr.deploy.service.DockerCommandService;
import com.webank.webase.node.mgr.front.FrontService;
import com.webank.webase.node.mgr.front.entity.FrontParam;
import com.webank.webase.node.mgr.front.entity.TbFront;
import com.webank.webase.node.mgr.tools.JsonTools;
import lombok.extern.log4j.Log4j2;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * cpu/memory usage triggers alert mail
 */
@Log4j2
@Component
public class ResourceMonitorTask {

    @Autowired
    private FrontService frontService;
    @Autowired
    private DockerCommandService dockerOptions;
    @DubboReference
    private RemoteHostService remoteHostService;
    @Autowired
    private MailService alertMailService;
    @Autowired
    private AlertRuleService alertRuleService;

    private final HashMap<Integer, FrontHitStats> frontStatsCache = new HashMap<>();
    private static final int hit_threshold = 5;

    @Scheduled(fixedDelayString = "${constant.nodeStatusMonitorTaskFixedDelay}")
    public void nodeAlertTaskStart() {
        checkAllNodeResForAlert();
    }

    /**
     * task start
     */
    public synchronized void checkAllNodeResForAlert() {
        Instant startTime = Instant.now();
        log.info("start checkAllNodeResForAlert startTime:{}", startTime.toEpochMilli());

        TbAlertRule cpuAlertRule = alertRuleService.queryByRuleId(AlertRuleType.CPU_ALERT.getValue());
        TbAlertRule memAlertRule = alertRuleService.queryByRuleId(AlertRuleType.MEM_ALERT.getValue());
        if(cpuAlertRule.getEnable() < 1 && memAlertRule.getEnable() < 1) {
            return;
        }

        FrontParam param = new FrontParam();
        param.setFrontStatus(1);
        List<TbFront> frontList = frontService.getFrontList(param);
        frontList.stream().forEach(front -> checkNodeRes(front, cpuAlertRule, memAlertRule));

        log.info("end checkAllNodeResForAlert useTime:{} ",
                Duration.between(startTime, Instant.now()).toMillis());
    }

    public void checkNodeRes(TbFront front, TbAlertRule cpuAlertRule, TbAlertRule memAlertRule) {
        HostDTO hostDTO = remoteHostService.getHostById(front.getHostId());
        String result = dockerOptions.stats(hostDTO, front.getContainerName());
        if (result == null) {
            return;
        }
        int beginIdx = result.indexOf(" >>");
        if (beginIdx < 0) {
            return;
        }
        result = result.substring(beginIdx + 3);

        DockerStats dockerStats = JsonTools.toJavaObject(result, DockerStats.class);
        if (dockerStats == null) {
            return;
        }

        double cpuPerc = Double.parseDouble(dockerStats.getCpuPerc().substring(0, dockerStats.getCpuPerc().length() - 1));
        if (cpuPerc > Double.parseDouble(cpuAlertRule.getLargerThan())) {
            log.info("node({}:{}) cpu usage({}) his the rules", front.getFrontIp(),
                    front.getFrontPort(), dockerStats.getCpuPerc());
            if (aggStats(front.getFrontId(), 1, -1, cpuAlertRule.getAlertIntervalSeconds())) {
                List<String> cpuAlertContentList = new ArrayList<>();
                cpuAlertContentList.add(front.getFrontIp() + ":" + front.getFrontPort());
                cpuAlertContentList.add(dockerStats.getCpuPerc());
                cpuAlertContentList.add(cpuAlertRule.getLargerThan());
                // send cpu alert mail
                alertMailService.sendMailByRule(AlertRuleType.CPU_ALERT.getValue(), cpuAlertContentList);
            }
        } else {
            aggStats(front.getFrontId(), 0, -1, cpuAlertRule.getAlertIntervalSeconds());
        }

        double memPerc = Double.parseDouble(dockerStats.getMemPerc().substring(0, dockerStats.getMemPerc().length() - 1));
        if (memPerc > Double.parseDouble(memAlertRule.getLargerThan())) {
            log.info("node({}:{}) memory usage({}) his the rules", front.getFrontIp(),
                    front.getFrontPort(), dockerStats.getMemPerc());
            if (aggStats(front.getFrontId(), -1, 1, memAlertRule.getAlertIntervalSeconds())) {
                List<String> memAlertContentList = new ArrayList<>();
                memAlertContentList.add(front.getFrontIp() + ":" + front.getFrontPort());
                memAlertContentList.add(dockerStats.getMemPerc());
                memAlertContentList.add(memAlertRule.getLargerThan());
                // send mem alert mail
                alertMailService.sendMailByRule(AlertRuleType.MEM_ALERT.getValue(), memAlertContentList);
            }
        } else {
            aggStats(front.getFrontId(), -1, 0, memAlertRule.getAlertIntervalSeconds());
        }

    }

    /**
     *
     * @param frontId
     * @param cpuHits, 0:没命中，1:命中，-1:忽略
     * @param memHits, 0:没命中，1:命中，-1:忽略
     * @return
     */
    private synchronized boolean aggStats(Integer frontId, int cpuHits, int memHits, Long alertInterval) {
        FrontHitStats hitStats = frontStatsCache.get(frontId);
        if (hitStats == null) {
            hitStats = new FrontHitStats();
            hitStats.setFrontId(frontId);
            hitStats.setCpus(0);
            hitStats.setMemory(0);
            hitStats.setCpuLastAlertTime(0);
            hitStats.setMemLastAlertTime(0);
            frontStatsCache.put(frontId, hitStats);
        }

        if (cpuHits >= 0) {
            if (cpuHits > 0) {
                hitStats.setCpus(hitStats.getCpus() + 1);
                if (hitStats.getCpus() > hit_threshold) {
                    hitStats.setCpus(0);
                    long now = System.currentTimeMillis();
                    if (now - hitStats.getCpuLastAlertTime() > alertInterval * 1000) {
                        hitStats.setCpuLastAlertTime(now);
                        return true;
                    }
                }
            } else {
                hitStats.setCpus(0);
            }
        }

        if (memHits >= 0) {
            if (memHits > 0) {
                hitStats.setMemory(hitStats.getMemory() + 1);
                if (hitStats.getMemory() > hit_threshold) {
                    hitStats.setMemory(0);
                    long now = System.currentTimeMillis();
                    if (now - hitStats.getMemLastAlertTime() > alertInterval * 1000) {
                        hitStats.setMemLastAlertTime(now);
                        return true;
                    }
                }
            } else {
                hitStats.setMemory(0);
            }
        }
        return false;
    }

}

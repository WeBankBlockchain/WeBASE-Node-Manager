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
package com.webank.webase.node.mgr.monitor;

import com.webank.webase.node.mgr.base.tools.JsonTools;
import com.webank.webase.node.mgr.base.enums.TableName;
import com.webank.webase.node.mgr.transaction.TransHashService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Service
public class MonitorTransactionService {
    @Autowired
    private MonitorMapper monitorMapper;
    @Autowired
    private TransHashService transHashService;

    /**
     * insert and update.
     */
    @Transactional
    public void dataAddAndUpdate(int groupId, TbMonitor tbMonitor) {
        TbMonitor dbInfo = this.queryTbMonitor(groupId, tbMonitor);
        if (dbInfo == null) {
            log.info("====== data is not exist, add tbMonitor:{}", JsonTools.toJSONString(tbMonitor));
            this.addRow(groupId, tbMonitor);
        } else {
            String[] txHashsArr = dbInfo.getTransHashs().split(",");
            if (txHashsArr.length < 5) {
                StringBuilder sb = new StringBuilder(dbInfo.getTransHashs()).append(",")
                    .append(tbMonitor.getTransHashLastest());
                tbMonitor.setTransHashs(sb.toString());
            } else {
                tbMonitor.setTransHashs(dbInfo.getTransHashs());
            }
            this.updateRow(groupId, tbMonitor);
        }
        log.debug("====== updateTransStatFlag transHash:{}", tbMonitor.getTransHashLastest());
        transHashService.updateTransStatFlag(groupId, tbMonitor.getTransHashLastest());
    }


    /**
     * query monitor info.
     */
    public TbMonitor queryTbMonitor(int groupId, TbMonitor tbMonitor) {
        return monitorMapper.queryTbMonitor(TableName.MONITOR.getTableName(groupId), tbMonitor);
    }

    public void addRow(int groupId, TbMonitor tbMonitor) {
        monitorMapper.add(TableName.MONITOR.getTableName(groupId), tbMonitor);
    }

    public void updateRow(int groupId, TbMonitor tbMonitor) {
        monitorMapper.update(TableName.MONITOR.getTableName(groupId), tbMonitor);
    }

}

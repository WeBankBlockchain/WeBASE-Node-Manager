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
package com.webank.webase.node.mgr.table;

import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.enums.TableName;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import java.time.Instant;
import java.util.List;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * service of table
 */
@Log4j2
@Service
public class TableService {

    @Autowired
    private TableMapper tableMapper;
    @Value("${spring.datasource.url}")
    private String dbUrl;

    /**
     * create table by groupId
     */
    public void newTableByGroupId(int groupId) {
        if (groupId == 0) {
            return;
        }

        //tb_block_
        tableMapper.createTbBlock(TableName.BLOCK.getTableName(groupId));
        //tb_trans_hash_
        tableMapper.createTransHash(TableName.TRANS.getTableName(groupId));
        //tb_user_transaction_monitor_
        tableMapper.createUserTransactionMonitor(TableName.MONITOR.getTableName(groupId));
    }

    /**
     * deop table.
     */
    public void dropTableByGroupId(int groupId) {
        Instant startTime = Instant.now();
        log.info("start dropTableByGroupId. startTime:{}", startTime.toEpochMilli());
        if (groupId == 0) {
            return;
        }
        for (TableName enumName : TableName.values()) {
            dropTableByName(enumName.getTableName(groupId));
        }
    }

    /**
     * drop table by tableName.
     */
    private void dropTableByName(String tableName) {
        log.info("start drop table. name:{}", tableName);
        if (StringUtils.isBlank(tableName)) {
            return;
        }
        List<String> tableNameList = tableMapper.queryTables(getDbName(), tableName);
        if (tableNameList == null || tableNameList.isEmpty()) {
            log.warn("fail dropTableByName. not fount this table, tableName:{}", tableName);
            return;
        }
        int affectedRow = 1;
        while (affectedRow > 0) {
            affectedRow = tableMapper.deleteByTableName(tableName);
            log.debug("delete table:{} affectedRow:{}", tableName, affectedRow);
        }

        //drop table
        tableMapper.dropTable(getDbName(), tableName);
        log.info("end dropTableByName. name:{}", tableName);
    }

    /**
     * get db name.
     */
    private String getDbName() {
        if (StringUtils.isBlank(dbUrl)) {
            log.error("fail getDbName. dbUrl is null");
            throw new NodeMgrException(ConstantCode.SYSTEM_EXCEPTION);
        }
        String subUrl = dbUrl.substring(0, dbUrl.indexOf("?"));
        String dbName = subUrl.substring(subUrl.lastIndexOf("/")+1);
        return dbName;
    }
}

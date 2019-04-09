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

import com.webank.webase.node.mgr.base.enums.TableName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * service of table
 */
@Service
public class TableService {

    @Autowired
    private TableMapper tableMapper;

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
}

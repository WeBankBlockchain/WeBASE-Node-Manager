/**
 * Copyright 2014-2020 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.webank.webase.node.mgr.governance;

import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.governance.entity.GovernParam;
import com.webank.webase.node.mgr.governance.entity.TbContractStatus;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * contract freeze status record
 */
@Service
public class ContractStatusService {
    @Autowired
    private ContractStatusMapper statusMapper;

    public List<TbContractStatus> getStatusList(GovernParam governParam) {
        return statusMapper.getList(governParam);
    }

    public int getCount(GovernParam governParam) {
        Integer count = statusMapper.getCount(governParam);
        return count == null ? 0 : count;
    }

    public void deleteStatus(Integer statusId) {
        TbContractStatus checkExist = statusMapper.getById(statusId);
        if (checkExist == null) {
            throw new NodeMgrException(ConstantCode.CONTRACT_STATUS_RECORD_NOT_EXIST);
        }
        statusMapper.deleteById(statusId);
    }
}

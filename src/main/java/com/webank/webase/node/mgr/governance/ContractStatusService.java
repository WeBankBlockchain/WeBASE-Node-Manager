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
import com.webank.webase.node.mgr.base.enums.FreezeStatus;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.governance.entity.GovernParam;
import com.webank.webase.node.mgr.governance.entity.TbContractStatus;
import com.webank.webase.node.mgr.precompiled.entity.ContractStatusHandle;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * contract freeze status record
 */
@Log4j2
@Service
public class ContractStatusService {
    @Autowired
    private ContractStatusMapper statusMapper;

    // contract manage type
    private static final String CONTRACT_MANAGE_FREEZE = "freeze";

    /**
     * when freeze contract successfully, save
     */
    public void saveContractStatus(ContractStatusHandle statusHandle) {
        log.debug("saveContractStatus statusHandle:{}", statusHandle);
        TbContractStatus tbContractStatus = new TbContractStatus();
        tbContractStatus.setGroupId(statusHandle.getGroupId());
        tbContractStatus.setContractAddress(statusHandle.getContractAddress());
        tbContractStatus.setModifyAddress(statusHandle.getFromAddress());
        // status
        tbContractStatus.setStatus(FreezeStatus.NORMAL.getValue());
        if (CONTRACT_MANAGE_FREEZE.equals(statusHandle.getHandleType())) {
            tbContractStatus.setStatus(FreezeStatus.FROZEN.getValue());
        }
        statusMapper.add(tbContractStatus);
        log.debug("saveContractStatus tbContractStatus:{}", tbContractStatus);

    }

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

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
import com.webank.webase.node.mgr.base.enums.GovernType;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.governance.entity.GovernParam;
import com.webank.webase.node.mgr.governance.entity.TbContractStatus;
import com.webank.webase.node.mgr.governance.entity.TbGovernVote;
import com.webank.webase.node.mgr.precompiled.entity.AddressStatusHandle;
import com.webank.webase.node.mgr.precompiled.entity.ChainGovernanceHandle;
import com.webank.webase.node.mgr.precompiled.entity.ContractStatusHandle;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * record chain governance vote
 */
@Log4j2
@Service
public class GovernVoteService {
    @Autowired
    private GovernVoteMapper governVoteMapper;

    /**
     * when freeze contract successfully, save
     */
    public void saveGovernVote(ChainGovernanceHandle governanceHandle, GovernType governType,
        Long timeLimit) {
        log.debug("saveGovernVote governanceHandle:{},governType:{}", governanceHandle, governType);
        TbGovernVote tbGovernVote = new TbGovernVote();
        tbGovernVote.setGroupId(governanceHandle.getGroupId());
        tbGovernVote.setFromAddress(governanceHandle.getFromAddress());
        tbGovernVote.setToAddress(governanceHandle.getAddress());
        // type
        tbGovernVote.setType(governType.getValue());
        // block limit
        tbGovernVote.setTimeLimit(timeLimit);
        // detail
        if (governType.equals(GovernType.UPDATE_COMMITTEE_WEIGHT)) {
            tbGovernVote.setDetail("weight: " + governanceHandle.getWeight());
        } else if (governType.equals(GovernType.UPDATE_THRESHOLD)) {
            tbGovernVote.setDetail("threshold: " + governanceHandle.getWeight());
        }
        governVoteMapper.add(tbGovernVote);
        log.debug("saveGovernVote tbGovernVote:{}", tbGovernVote);
    }

    public List<TbGovernVote> getVoteList(GovernParam governParam) {
        return governVoteMapper.getList(governParam);
    }

    public int getCount(GovernParam governParam) {
        Integer count = governVoteMapper.getCount(governParam);
        return count == null ? 0 : count;
    }
    
    public void deleteVote(Integer voteId) {
        TbGovernVote checkExist = governVoteMapper.getById(voteId);
        if (checkExist == null) {
            throw new NodeMgrException(ConstantCode.GOVERN_VOTE_RECORD_NOT_EXIST);
        }
        governVoteMapper.deleteById(voteId);
    }
}

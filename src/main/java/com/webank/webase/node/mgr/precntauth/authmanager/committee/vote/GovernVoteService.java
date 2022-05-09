/**
 * Copyright 2014-2021 the original author or authors.
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

package com.webank.webase.node.mgr.precntauth.authmanager.committee.vote;

import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.front.frontinterface.FrontInterfaceService;
import com.webank.webase.node.mgr.precntauth.authmanager.committee.entity.GovernParam;
import com.webank.webase.node.mgr.precntauth.authmanager.committee.entity.ReqUpdateGovernorInfo;
import com.webank.webase.node.mgr.precntauth.authmanager.committee.entity.TbGovernVote;
import java.math.BigInteger;
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
    @Autowired
    private FrontInterfaceService frontInterfaceService;

    /**
     * when freeze contract successfully, save
     */
    public void saveGovernVote(ReqUpdateGovernorInfo  governanceHandle) {
        log.debug("saveGovernVote governanceHandle:{},governType:{}", governanceHandle);
        BigInteger blockNumber = frontInterfaceService.getLatestBlockNumber(
            governanceHandle.getGroupId());

        TbGovernVote tbGovernVote = new TbGovernVote();
        tbGovernVote.setGroupId(governanceHandle.getGroupId());
        tbGovernVote.setFromAddress(governanceHandle.getFromAddress());
        tbGovernVote.setToAddress(governanceHandle.getAccountAddress());
        // time limit = block limit + blockHeight
        tbGovernVote.setTimeLimit(blockNumber.longValue() + 10000L);
        // detail
        tbGovernVote.setDetail("weight: " + governanceHandle.getWeight());
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

    public void deleteAllByGroupId(int groupId) {
        log.info("deleteAllByGroupId groupId:{}", groupId);
        governVoteMapper.deleteByGroupId(groupId);
    }
}

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
import com.webank.webase.node.mgr.governance.entity.TbGovernVote;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * record chain governance vote
 */
@Service
public class GovernVoteService {
    @Autowired
    private GovernVoteMapper governVoteMapper;


    public List<TbGovernVote> getStatusList(GovernParam governParam) {
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

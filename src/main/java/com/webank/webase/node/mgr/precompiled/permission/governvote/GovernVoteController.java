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

package com.webank.webase.node.mgr.precompiled.permission.governvote;

import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.entity.BasePageResponse;
import com.webank.webase.node.mgr.base.entity.BaseResponse;
import com.webank.webase.node.mgr.precompiled.permission.governvote.entity.GovernParam;
import com.webank.webase.node.mgr.precompiled.permission.governvote.entity.TbGovernVote;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("vote")
public class GovernVoteController {

    @Autowired
    private GovernVoteService governVoteService;



    /**
     * get contract freeze record list
     */
    @GetMapping("record/list")
    public BasePageResponse listGovernVote(@RequestParam Integer groupId,
        @RequestParam(defaultValue = "1") Integer pageNumber,
        @RequestParam(defaultValue = "10") Integer pageSize) {
        BasePageResponse response = new BasePageResponse(ConstantCode.SUCCESS);
        GovernParam param = new GovernParam();
        param.setGroupId(groupId);
        int count = governVoteService.getCount(param);
        if (count > 0) {
            param.setPageSize(pageSize);
            Integer start = (pageNumber - 1) * pageSize;
            param.setStart(start);
            List<TbGovernVote> resList = governVoteService.getVoteList(param);
            response.setData(resList);
            response.setTotalCount(count);
        }
        return response;
    }

    /**
     * delete contract status record
     */
    @DeleteMapping("record/{voteId}")
    public BaseResponse deleteContractStatusRecord(@PathVariable("voteId") Integer voteId) {
        governVoteService.deleteVote(voteId);
        return new BaseResponse(ConstantCode.SUCCESS);
    }
}

/**
 * Copyright 2014-2021 the original author or authors.
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
package com.webank.webase.node.mgr.group;

import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.controller.BaseController;
import com.webank.webase.node.mgr.base.entity.BasePageResponse;
import com.webank.webase.node.mgr.base.entity.BaseResponse;
import com.webank.webase.node.mgr.base.enums.GroupStatus;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.config.properties.ConstantProperties;
import com.webank.webase.node.mgr.group.entity.GroupGeneral;
import com.webank.webase.node.mgr.group.entity.ReqBatchStartGroup;
import com.webank.webase.node.mgr.group.entity.ReqGenerateGroup;
import com.webank.webase.node.mgr.group.entity.ReqGroupStatus;
import com.webank.webase.node.mgr.group.entity.ReqOperateGroup;
import com.webank.webase.node.mgr.group.entity.ReqUpdateDesc;
import com.webank.webase.node.mgr.group.entity.RspGroupStatus;
import com.webank.webase.node.mgr.group.entity.RspOperateResult;
import com.webank.webase.node.mgr.group.entity.TbGroup;
import com.webank.webase.node.mgr.scheduler.ResetGroupListTask;
import com.webank.webase.node.mgr.scheduler.StatisticsTransdailyTask;
import com.webank.webase.node.mgr.tools.JsonTools;
import com.webank.webase.node.mgr.tools.pagetools.List2Page;
import com.webank.webase.node.mgr.transdaily.SeventDaysTrans;
import com.webank.webase.node.mgr.transdaily.TransDailyService;
import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import javax.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for processing group information.
 */
@Log4j2
@RestController
@RequestMapping("group")
public class GroupController extends BaseController {

    @Autowired
    private GroupService groupService;
    @Autowired
    private TransDailyService transDailyService;
    @Autowired
    private StatisticsTransdailyTask statisticsTask;
    @Autowired
    private ResetGroupListTask resetGroupListTask;
    

    /**
     * get group general.
     */
    @GetMapping("/general/{groupId}")
    public BaseResponse getGroupGeneral(@PathVariable("groupId") String groupId)
            throws NodeMgrException {
        Instant startTime = Instant.now();
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        log.info("start getGroupGeneral startTime:{} groupId:{}", startTime.toEpochMilli(),
                groupId);
        GroupGeneral groupGeneral = null;

        int statisticTimes = 0;// if transCount less than blockNumber,statistics again
        while (true) {
            groupGeneral = groupService.queryGroupGeneral(groupId);
            BigInteger transactionCount = groupGeneral.getTransactionCount();
            BigInteger latestBlock = groupGeneral.getLatestBlock();
            if (transactionCount.compareTo(latestBlock) < 0 && statisticTimes == 0) {
                statisticTimes += 1;
                statisticsTask.updateTransdailyData();
                continue;
            } else {
                break;
            }
        }

        baseResponse.setData(groupGeneral);
        log.info("end getGroupGeneral useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JsonTools.toJSONString(baseResponse));
        return baseResponse;
    }

    /**
     * query all normal group without invalid group(suspend, removed)
     */
    @GetMapping("/all")
    public BasePageResponse getAllGroup() throws NodeMgrException {
        BasePageResponse pageResponse = new BasePageResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start getAllGroup startTime:{}", startTime.toEpochMilli());

        // get group list
        int count = groupService.countOfGroup(null, GroupStatus.NORMAL.getValue());
        if (count > 0) {
            List<TbGroup> groupList = groupService.getGroupList(GroupStatus.NORMAL.getValue());
            pageResponse.setTotalCount(count);
            pageResponse.setData(groupList);
        }

        // reset group
        resetGroupListTask.asyncResetGroupList();

        log.info("end getAllGroup useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JsonTools.toJSONString(pageResponse));
        return pageResponse;
    }

     /**
     * get all group include invalid group status
     * @param pageNumber
     * @param pageSize
     * @return
     * @throws NodeMgrException
     **/
   @GetMapping({"/all/invalidIncluded/{pageNumber}/{pageSize}",
            "/all/invalidIncluded"})
    public BasePageResponse getAllGroupIncludeInvalidGroup(@PathVariable(value = "pageNumber",required = false) Integer pageNumber,
                                                           @PathVariable(value = "pageSize", required = false) Integer pageSize) throws NodeMgrException {
        BasePageResponse pageResponse = new BasePageResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start getAllGroupIncludeInvalidGroup startTime:{}", startTime.toEpochMilli());

        // get group list include invalid status
        int count = groupService.countOfGroup(null, null);
        if (count > 0) {
            List<TbGroup> groupList = groupService.getGroupList(null);
            if (pageNumber == null && pageSize == null) {
                pageResponse.setData(groupList);
                pageResponse.setTotalCount(count);
            } else {
                List2Page list2Page = new List2Page(groupList, pageSize, pageNumber);
                pageResponse.setData(list2Page.getPagedList());
                pageResponse.setTotalCount(count);
            }
        }
        // reset group
        resetGroupListTask.asyncResetGroupList();

        log.info("end getAllGroupIncludeInvalidGroup useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JsonTools.toJSONString(pageResponse));
        return pageResponse;
    }


    @GetMapping("/all/{groupStatus}")
    public BaseResponse getAllGroupOfStatus(@PathVariable("groupStatus") Integer groupStatus) throws NodeMgrException {
        if (groupStatus > GroupStatus.CONFLICT_LOCAL_DATA.getValue()
                || groupStatus < GroupStatus.NORMAL.getValue()) {
            return new BaseResponse(ConstantCode.INVALID_PARAM_INFO);
        }
        BaseResponse response = new BaseResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start getAllGroupOfStatus startTime:{}", startTime.toEpochMilli());

        // get group list
        int count = groupService.countOfGroup(null, groupStatus);
        if (count > 0) {
            List<TbGroup> groupList = groupService.getGroupList(groupStatus);
            response.setData(groupList);
        }

        // reset group
        resetGroupListTask.asyncResetGroupList();

        log.info("end getAllGroupOfStatus useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JsonTools.toJSONString(response));
        return response;
    }

    /**
     * get trans daily.
     */
    @GetMapping("/transDaily/{groupId}")
    public BaseResponse getTransDaily(@PathVariable("groupId") String groupId) throws Exception {
        BaseResponse pageResponse = new BaseResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start getTransDaily startTime:{} groupId:{}", startTime.toEpochMilli(), groupId);

        // query trans daily
        List<SeventDaysTrans> listTrans = transDailyService.listSeventDayOfTrans(groupId);
        pageResponse.setData(listTrans);

        log.info("end getAllGroup useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JsonTools.toJSONString(pageResponse));
        return pageResponse;
    }

    /**
     * update group.
     */
    @GetMapping("/update")
    public BaseResponse updateGroup() throws NodeMgrException {
        Instant startTime = Instant.now();
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        log.info("start updateGroup startTime:{}", startTime.toEpochMilli());
        groupService.resetGroupList();
        log.info("end updateGroup useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JsonTools.toJSONString(baseResponse));
        return baseResponse;
    }

    /**
     * delete all group's data(trans, contract, node etc.)
     */
  /*  @DeleteMapping("/{groupId}")
    public BaseResponse deleteGroupData(@PathVariable("groupId") Integer groupId) {
        Instant startTime = Instant.now();
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        log.warn("start deleteGroupData startTime:{}", startTime.toEpochMilli());
        groupService.removeAllDataByGroupId(groupId);
        log.warn("end deleteGroupData useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JsonTools.toJSONString(baseResponse));
        return baseResponse;
    }*/

    @GetMapping("/detail/{groupId}")
    public BaseResponse getGroupDetail(@PathVariable("groupId") String groupId)
        throws NodeMgrException {
        Instant startTime = Instant.now();
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        log.info("start getGroupDetail startTime:{} groupId:{}", startTime.toEpochMilli(),
            groupId);
        TbGroup groupDetail = groupService.getGroupById(groupId);
        baseResponse.setData(groupDetail);
        log.info("end getGroupDetail useTime:{} result:{}",
            Duration.between(startTime, Instant.now()).toMillis(),
            JsonTools.toJSONString(baseResponse));
        return baseResponse;
    }

    @PutMapping("/description")
    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN)
    public BaseResponse updateDescription(@RequestBody @Valid ReqUpdateDesc req, BindingResult result)
        throws NodeMgrException {
        checkBindResult(result);
        Instant startTime = Instant.now();
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        log.info("start updateDescription startTime:{} ReqUpdate:{}", startTime.toEpochMilli(),
            req);
        int updateResult = groupService.updateGroupDescription(req.getGroupId(), req.getDescription());
        baseResponse.setData(updateResult);
        log.info("end updateDescription useTime:{} result:{}",
            Duration.between(startTime, Instant.now()).toMillis(),
            JsonTools.toJSONString(baseResponse));
        return baseResponse;
    }
}

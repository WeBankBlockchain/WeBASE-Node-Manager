/**
 * Copyright 2014-2020 the original author or authors.
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

import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.controller.BaseController;
import com.webank.webase.node.mgr.base.entity.BasePageResponse;
import com.webank.webase.node.mgr.base.entity.BaseResponse;
import com.webank.webase.node.mgr.base.enums.GroupStatus;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.base.properties.ConstantProperties;
import com.webank.webase.node.mgr.base.tools.JsonTools;
import com.webank.webase.node.mgr.base.tools.pagetools.List2Page;
import com.webank.webase.node.mgr.group.entity.GroupGeneral;
import com.webank.webase.node.mgr.group.entity.ReqBatchStartGroup;
import com.webank.webase.node.mgr.group.entity.ReqGenerateGroup;
import com.webank.webase.node.mgr.group.entity.ReqGroupStatus;
import com.webank.webase.node.mgr.group.entity.ReqOperateGroup;
import com.webank.webase.node.mgr.group.entity.RspGroupStatus;
import com.webank.webase.node.mgr.group.entity.RspOperateResult;
import com.webank.webase.node.mgr.group.entity.TbGroup;
import com.webank.webase.node.mgr.scheduler.ResetGroupListTask;
import com.webank.webase.node.mgr.scheduler.StatisticsTransdailyTask;
import com.webank.webase.node.mgr.transdaily.SeventDaysTrans;
import com.webank.webase.node.mgr.transdaily.TransDailyService;

import lombok.extern.log4j.Log4j2;

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
    public BaseResponse getGroupGeneral(@PathVariable("groupId") Integer groupId)
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
        BasePageResponse pagesponse = new BasePageResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start getAllGroup startTime:{}", startTime.toEpochMilli());

        // get group list
        int count = groupService.countOfGroup(null, GroupStatus.NORMAL.getValue());
        if (count > 0) {
            List<TbGroup> groupList = groupService.getGroupList(GroupStatus.NORMAL.getValue());
            pagesponse.setTotalCount(count);
            pagesponse.setData(groupList);
        }

        // reset group
        resetGroupListTask.asyncResetGroupList();

        log.info("end getAllGroup useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JsonTools.toJSONString(pagesponse));
        return pagesponse;
    }

    /**
     * get all group include invalid group status
     * @param pageNumber
     * @param pageSize
     * @return
     * @throws NodeMgrException
     */
    @GetMapping({"/all/invalidIncluded/{pageNumber}/{pageSize}",
            "/all/invalidIncluded"})
    public BasePageResponse getAllGroupIncludeInvalidGroup(@PathVariable(value = "pageNumber",required = false) Integer pageNumber,
                                                           @PathVariable(value = "pageSize", required = false) Integer pageSize) throws NodeMgrException {
        BasePageResponse pagesponse = new BasePageResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start getAllGroupIncludeInvalidGroup startTime:{}", startTime.toEpochMilli());

        // get group list include invalid status
        int count = groupService.countOfGroup(null, null);
        if (count > 0) {
            List<TbGroup> groupList = groupService.getGroupList(null);
            if (pageNumber == null && pageSize == null) {
                pagesponse.setData(groupList);
                pagesponse.setTotalCount(count);
            } else {
                List2Page list2Page = new List2Page(groupList, pageSize, pageNumber);
                pagesponse.setData(list2Page.getPagedList());
                pagesponse.setTotalCount(count);
            }
        }
        // reset group
        resetGroupListTask.asyncResetGroupList();

        log.info("end getAllGroupIncludeInvalidGroup useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JsonTools.toJSONString(pagesponse));
        return pagesponse;
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
    public BaseResponse getTransDaily(@PathVariable("groupId") Integer groupId) throws Exception {
        BaseResponse pagesponse = new BaseResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start getTransDaily startTime:{} groupId:{}", startTime.toEpochMilli(), groupId);

        // query trans daily
        List<SeventDaysTrans> listTrans = transDailyService.listSeventDayOfTrans(groupId);
        pagesponse.setData(listTrans);

        log.info("end getAllGroup useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JsonTools.toJSONString(pagesponse));
        return pagesponse;
    }

    /**
     * generate group to single node(single front)
     */
    @PostMapping("/generate/{nodeId}")
    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN)
    public BaseResponse generateToSingleNode(@PathVariable("nodeId") String nodeId,
                                             @RequestBody @Valid ReqGenerateGroup req,
                                             BindingResult result) throws NodeMgrException {
        checkBindResult(result);
        Instant startTime = Instant.now();
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        log.info("start generateToSingleNode startTime:{} nodeId:{}", startTime.toEpochMilli(),
                nodeId);
        TbGroup tbGroup = groupService.generateToSingleNode(nodeId, req);
        baseResponse.setData(tbGroup);
        log.info("end generateToSingleNode useTime:{}",
                Duration.between(startTime, Instant.now()).toMillis());
        return baseResponse;
    }

    /**
     * generate group to all front(all node)
     */
    @PostMapping("/generate")
    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN)
    public BaseResponse generateGroup(@RequestBody @Valid ReqGenerateGroup req,
                                      BindingResult result) throws NodeMgrException {
        checkBindResult(result);
        Instant startTime = Instant.now();
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        log.info("start generateGroup startTime:{} groupId:{}", startTime.toEpochMilli(),
                req.getGenerateGroupId());
        List<RspOperateResult> generateResultList = groupService.generateGroup(req);
        baseResponse.setData(generateResultList);
        log.info("end generateGroup useTime:{}",
                Duration.between(startTime, Instant.now()).toMillis());
        return baseResponse;
    }

    /**
     * operate group to single front.
     * (start, stop, remove, recover, getStatus)
     */
    @PostMapping("/operate/{nodeId}")
    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN)
    public Object operateGroup(@PathVariable("nodeId") String nodeId, @RequestBody @Valid ReqOperateGroup req,
                               BindingResult result) throws NodeMgrException {
        checkBindResult(result);
        Instant startTime = Instant.now();
        Integer groupId = req.getGenerateGroupId();
        String type = req.getType();
        log.info("start operateGroup startTime:{} groupId:{}", startTime.toEpochMilli(), groupId);

        Object groupHandleResult = groupService.operateGroup(nodeId, groupId, type);
        log.info("end operateGroup useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JsonTools.toJSONString(groupHandleResult));
        return groupHandleResult;
    }

    /**
     * query group status list
     * @return map of <nodeId,<groupId, status>>
     */
    @PostMapping("/queryGroupStatus/list")
    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN)
    public BaseResponse getGroupStatusList(@Valid @RequestBody ReqGroupStatus reqGroupStatus) throws NodeMgrException {
        Instant startTime = Instant.now();
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        log.info("start getGroupStatusMap startTime:{}", startTime.toEpochMilli());
        List<String> nodeIdList = reqGroupStatus.getNodeIdList();
        List<RspGroupStatus> resList = groupService.listGroupStatus(nodeIdList,
                reqGroupStatus.getGroupIdList());
        baseResponse.setData(resList);
        log.info("end getGroupStatusMap useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JsonTools.toJSONString(baseResponse));
        return baseResponse;
    }

    /**
     * batch start group.(start group to all front
     */
    @PostMapping("/batchStart")
    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN)
    public BaseResponse batchStartGroup(@RequestBody @Valid ReqBatchStartGroup req, BindingResult result)
            throws NodeMgrException {
        checkBindResult(result);
        Instant startTime = Instant.now();
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        log.info("start batchStartGroup startTime:{} groupId:{}", startTime.toEpochMilli(),
                req.getGenerateGroupId());
        List<RspOperateResult> operateResultList = groupService.batchStartGroup(req);
        baseResponse.setData(operateResultList);
        log.info("end batchStartGroup useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JsonTools.toJSONString(baseResponse));
        return baseResponse;
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
    @DeleteMapping("/{groupId}")
    public BaseResponse deleteGroupData(@PathVariable("groupId") Integer groupId) {
        Instant startTime = Instant.now();
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        log.warn("start deleteGroupData startTime:{}", startTime.toEpochMilli());
        groupService.removeAllDataByGroupId(groupId);
        log.warn("end deleteGroupData useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JsonTools.toJSONString(baseResponse));
        return baseResponse;
    }
}

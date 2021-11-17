/**
 * Copyright 2014-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webank.webase.node.mgr.precompiled;

import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.enums.DeployType;
import com.webank.webase.node.mgr.base.enums.GroupStatus;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.config.properties.ConstantProperties;
import com.webank.webase.node.mgr.front.FrontMapper;
import com.webank.webase.node.mgr.front.entity.TbFront;
import com.webank.webase.node.mgr.front.frontinterface.FrontInterfaceService;
import com.webank.webase.node.mgr.front.frontinterface.FrontRestTools;
import com.webank.webase.node.mgr.frontgroupmap.FrontGroupMapService;
import com.webank.webase.node.mgr.group.GroupService;
import com.webank.webase.node.mgr.precompiled.entity.ConsensusHandle;
import com.webank.webase.node.mgr.precompiled.entity.CrudHandle;
import com.webank.webase.node.mgr.tools.HttpRequestTools;
import com.webank.webase.node.mgr.tools.JsonTools;
import com.webank.webase.node.mgr.user.UserService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Precompiled common service
 * including management of CNS, node consensus status, CRUD
 */
@Log4j2
@Service
public class PrecompiledService {

    @Autowired
    private FrontRestTools frontRestTools;
    @Autowired
    private FrontInterfaceService frontInterfaceService;
    @Autowired
    private UserService userService;
    @Autowired
    private GroupService groupService;
    @Autowired
    private FrontGroupMapService frontGroupMapService;
    @Autowired
    private FrontMapper frontMapper;
    @Autowired
    private ConstantProperties constants;

    private static final String CONTRACT_MANAGE_GETSTATUS = "getStatus";
    private static final String CONTRACT_MANAGE_LISTMANAGER = "listManager";
    private static final String CONTRACT_MANAGE_FREEZE = "freeze";
    private static final String CONTRACT_MANAGE_UNFREEZE = "unfreeze";
    private static final String GROUP_OPERATE_GET_STATUS = "getStatus";
    public static final String GROUP_FILE_NOT_EXIST = "INEXISTENT";


    /**
     * get node list with consensus status
     */
    public Object getNodeListService(String groupId, int pageSize, int pageNumber) {
        log.debug("start getNodeListService. groupId:{}", groupId);
        String uri;
        Map<String, String> map = new HashMap<>();
        map.put("groupId", String.valueOf(groupId));
        map.put("pageSize", String.valueOf(pageSize));
        map.put("pageNumber", String.valueOf(pageNumber));
        uri = HttpRequestTools.getQueryUri(FrontRestTools.URI_CONSENSUS_LIST, map);
        Object frontRsp = null;
        frontRsp = frontRestTools.getForEntity(groupId, uri, Object.class);
        log.debug("end getNodeListService. frontRsp:{}", JsonTools.toJSONString(frontRsp));
        return frontRsp;
    }


    /**
     * post node manage consensus status
     */
    public Object nodeManageService(ConsensusHandle consensusHandle) {
        log.debug("start nodeManageService. consensusHandle:{}", JsonTools.toJSONString(consensusHandle));

        if (Objects.isNull(consensusHandle)) {
            log.error("fail nodeManageService. request param is null");
            throw new NodeMgrException(ConstantCode.INVALID_PARAM_INFO);
        }
        String groupId = consensusHandle.getGroupId();
        String nodeId = consensusHandle.getNodeId();

        // check sealer num in group
        List<String> sealerList = this.frontInterfaceService.getSealerList(groupId);
        sealerList.remove(nodeId);
        // @visual-deploy: at least 2 sealers in group after remove
        if (constants.getDeployType() == DeployType.VISUAL_DEPLOY.getValue()
            && CollectionUtils.size(sealerList) < ConstantProperties.LEAST_SEALER_TWO) {
            log.error("fail nodeManageService. Group only has 1 sealers after remove.(visual_deploy)");
            throw new NodeMgrException(ConstantCode.TWO_SEALER_IN_GROUP_AT_LEAST);
        }

        String signUserId = userService.getSignUserIdByAddress(groupId, consensusHandle.getFromAddress());
        consensusHandle.setSignUserId(signUserId);
        TbFront front = this.frontMapper.getByNodeId(nodeId);
        Object frontRsp;
        log.info("nodeManageService now request random available front");
        frontRsp = frontRestTools.postForEntity(groupId, FrontRestTools.URI_CONSENSUS,
            consensusHandle, Object.class);
        // update front group map if remove node from sealer/observer
        if (StringUtils.equalsIgnoreCase("remove", consensusHandle.getNodeType()) && front != null) {
            log.info("remove node/front:[{}] from group:[{}], change front group map status to [{}]",
                    front.getFrontId(), groupId, GroupStatus.MAINTAINING);
            // update map
            frontGroupMapService.updateFrontMapStatus(front.getFrontId(), groupId, GroupStatus.MAINTAINING);
            // update tb_node rm invalid node
            groupService.resetGroupList();
        }

        log.debug("end nodeManageService. frontRsp:{}", frontRsp);
        return frontRsp;
    }

    /**
     *  post CRUD operation
     */
    public Object crudService(CrudHandle crudHandle) {
        log.debug("start crudService. crudHandle:{}", JsonTools.toJSONString(crudHandle));
        if (Objects.isNull(crudHandle)) {
            log.error("fail crudService. request param is null");
            throw new NodeMgrException(ConstantCode.INVALID_PARAM_INFO);
        }
        String groupId = crudHandle.getGroupId();
        String signUserId = userService.getSignUserIdByAddress(groupId, crudHandle.getFromAddress());
        crudHandle.setSignUserId(signUserId);
        Object frontRsp = frontRestTools.postForEntity(
                groupId, FrontRestTools.URI_CRUD,
                crudHandle, Object.class);
        log.debug("end crudService. frontRsp:{}", JsonTools.toJSONString(frontRsp));
        return frontRsp;
    }


}

/**
 * Copyright 2014-2021  the original author or authors.
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
package com.webank.webase.node.mgr.role;

import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.entity.BasePageResponse;
import com.webank.webase.node.mgr.base.enums.RoleType;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.base.properties.ConstantProperties;
import com.webank.webase.node.mgr.base.tools.JsonTools;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * services for role data.
 */
@Log4j2
@Service
public class RoleService {

    @Autowired
    private RoleMapper roleMapper;
    @Autowired
    private ConstantProperties constantProperties;
    
    public BasePageResponse queryRoleList(Integer pageNumber, Integer pageSize, Integer roleId,
            String roleName) throws NodeMgrException {
        BasePageResponse pagesponse = new BasePageResponse(ConstantCode.SUCCESS);

        // param
        Integer realPageSize = Optional.ofNullable(pageSize).orElse(10);
        Integer start =
                Optional.ofNullable(pageNumber).map(page -> (page - 1) * realPageSize).orElse(0);
        List<Integer> roleIdListNotIn = new ArrayList<>();
        if (!constantProperties.isDeveloperModeEnable()) {
            roleIdListNotIn.add(RoleType.DEVELOPER.getValue());
        }
        RoleListParam param =
                new RoleListParam(start, realPageSize, roleId, roleName, roleIdListNotIn);

        // query
        int count = countOfRole(param);
        if (count > 0) {
            List<TbRole> listOfRole = listOfRole(param);
            pagesponse.setData(listOfRole);
            pagesponse.setTotalCount(count);
        }

        return pagesponse;
    }

    /**
     * query role count.
     */
    public int countOfRole(RoleListParam param) {
        log.debug("start countOfRole. param:{} ", JsonTools.toJSONString(param));
        Integer roleCount = roleMapper.countOfRole(param);
        int count = roleCount == null ? 0 : roleCount.intValue();
        log.debug("end countOfRole. count:{} ", count);
        return count;
    }

    /**
     * query role .
     */
    public List<TbRole> listOfRole(RoleListParam param) {
        log.debug("start listOfRole. param:{} ", JsonTools.toJSONString(param));
        List<TbRole> list = roleMapper.listOfRole(param);
        log.debug("end listOfRole. list:{} ", JsonTools.toJSONString(list));
        return list;
    }

    /**
     * query role by roleId.
     */
    public TbRole queryRoleById(Integer roleId) {
        log.debug("start queryRoleById. roleId:{} ", roleId);
        TbRole roleInfo = roleMapper.queryRoleById(roleId);
        log.debug("end queryRoleById. roleInfo:{} ", JsonTools.toJSONString(roleInfo));
        return roleInfo;
    }

    /**
     * check roleId.
     */
    public void roleIdExist(Integer roleId) throws NodeMgrException {
        log.debug("start roleIdExist. roleId:{} ", roleId);
        if (roleId == null) {
            log.info("fail roleIdExist. roleId is null ");
            throw new NodeMgrException(ConstantCode.ROLE_ID_EMPTY);
        }
        TbRole roleInfo = roleMapper.queryRoleById(roleId);
        if (roleInfo == null) {
            log.info("fail roleIdExist. did not found role row by id");
            throw new NodeMgrException(ConstantCode.INVALID_ROLE_ID);
        }
        log.debug("end roleIdExist. ");
    }
}

/**
 * Copyright 2014-2020  the original author or authors.
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

import com.webank.webase.node.mgr.base.tools.JsonTools;
import com.webank.webase.node.mgr.base.entity.BasePageResponse;
import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.base.properties.ConstantProperties;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@RequestMapping(value = "role")
@PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN)
public class RoleController {

    @Autowired
    private RoleService roleService;

    /**
     * query role list.
     */
    @GetMapping(value = "/roleList")
    public BasePageResponse queryRoleList(
        @RequestParam(value = "pageNumber", required = false) Integer pageNumber,
        @RequestParam(value = "pageSize", required = false) Integer pageSize,
        @RequestParam(value = "roleId", required = false) Integer roleId,
        @RequestParam(value = "roleName", required = false) String roleName)
        throws NodeMgrException {
        BasePageResponse pagesponse = new BasePageResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info(
            "start queryRoleList.  startTime:{} pageNumber:{} pageSize:{} roleId:{} roleName:{}",
            startTime.toEpochMilli(),
            pageNumber, pageSize, roleId, roleName);

        // param
        Integer realPageSize = Optional.ofNullable(pageSize).orElse(5);
        Integer start = Optional.ofNullable(pageNumber)
            .map(page -> (page - 1) * realPageSize).orElse(0);

        RoleListParam param = new RoleListParam(start, realPageSize, roleId, roleName);

        // query
        int count = roleService.countOfRole(param);
        if (count > 0) {
            List<TbRole> listOfRole = roleService.listOfRole(param);
            pagesponse.setData(listOfRole);
            pagesponse.setTotalCount(count);
        }

        log.info("end queryRoleList useTime:{} result:{}",
            Duration.between(startTime, Instant.now()).toMillis(),
            JsonTools.toJSONString(pagesponse));
        return pagesponse;
    }
}

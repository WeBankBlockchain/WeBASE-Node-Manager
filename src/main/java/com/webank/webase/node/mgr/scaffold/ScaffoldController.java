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

package com.webank.webase.node.mgr.scaffold;

import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.controller.BaseController;
import com.webank.webase.node.mgr.base.entity.BaseResponse;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.base.properties.ConstantProperties;
import com.webank.webase.node.mgr.base.tools.IPUtil;
import com.webank.webase.node.mgr.base.tools.JsonTools;
import com.webank.webase.node.mgr.base.tools.NodeMgrTools;
import com.webank.webase.node.mgr.scaffold.entity.ReqProject;
import com.webank.webase.node.mgr.scaffold.entity.RspFile;
import java.time.Duration;
import java.time.Instant;
import javax.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author marsli
 */
@Log4j2
@RestController
@RequestMapping(value = "scaffold")
public class ScaffoldController extends BaseController {
    @Autowired
    private ScaffoldService scaffoldService;

    @PostMapping("/export")
    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN_OR_DEVELOPER)
    public BaseResponse exportProjectApi(@Valid @RequestBody ReqProject param) {
        Instant startTime = Instant.now();
        log.info("start exportProjectApi param:{} groupId:{}", startTime.toEpochMilli(),
            param);
        if (StringUtils.isBlank(param.getChannelIp())) {
            param.setChannelIp(IPUtil.LOCAL_IP_127);
        }
        // check artifact name and group name
        if (!NodeMgrTools.startWithLetter(param.getArtifactName())) {
            log.error("must start with letter");
            throw new NodeMgrException(ConstantCode.PARAM_INVALID_LETTER_DIGIT);
        }
        // validate group name, ex: org.example
        if (!param.getGroup().contains("\\.")) {
            // only org
            if (!NodeMgrTools.startWithLetter(param.getGroup())) {
                log.error("must start with letter");
                throw new NodeMgrException(ConstantCode.PARAM_INVALID_LETTER_DIGIT);
            }
        } else {
            // include org.xxx
            String[] groupNameArray = param.getGroup().split("\\.");
            for (String group: groupNameArray) {
                // not start or end with dot "."
                if (StringUtils.isBlank(group)) {
                    log.error("group cannot start or end with dot");
                    throw new NodeMgrException(ConstantCode.PARAM_INVALID_LETTER_DIGIT);
                }
                if (!NodeMgrTools.startWithLetter(group)) {
                    log.error("package name must start with letter");
                    throw new NodeMgrException(ConstantCode.PARAM_INVALID_LETTER_DIGIT);
                }
            }
        }
        RspFile rspFile = scaffoldService.exportProject(param);
        log.info("end exportProjectApi useTime:{} result:{}",
            Duration.between(startTime, Instant.now()).toMillis(), rspFile);
        return new BaseResponse(ConstantCode.SUCCESS, rspFile);
    }
}

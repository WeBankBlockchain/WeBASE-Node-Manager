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

package com.webank.webase.node.mgr.contract.scaffold;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.qctc.common.log.annotation.Log;
import com.qctc.common.log.enums.BusinessType;
import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.controller.BaseController;
import com.webank.webase.node.mgr.base.entity.BaseResponse;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.config.properties.ConstantProperties;
import com.webank.webase.node.mgr.contract.scaffold.entity.ReqProject;
import com.webank.webase.node.mgr.contract.scaffold.entity.RspFile;
import com.webank.webase.node.mgr.tools.IPUtil;
import com.webank.webase.node.mgr.tools.NodeMgrTools;
import com.webank.webase.node.mgr.tools.ValidateUtil;
import java.time.Duration;
import java.time.Instant;
import javax.validation.Valid;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author marsli
 */
@Tag(name="合约脚手架")
@Log4j2
@RestController
@RequestMapping(value = "scaffold")
@SaCheckPermission("bcos3:contract:ide")
public class ScaffoldController extends BaseController {
    @Autowired
    private ScaffoldService scaffoldService;

    @Log(title = "BCOS3/合约管理/合约IDE", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    // TODO:  使用sa-token鉴权(ConstantProperties.HAS_ROLE_ADMIN_OR_DEVELOPER)
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
        if (!param.getGroup().contains(".")) {
            // only org
            if (!NodeMgrTools.startWithLetter(param.getGroup())) {
                log.error("group must start with letter");
                throw new NodeMgrException(ConstantCode.PARAM_INVALID_LETTER_DIGIT);
            }
        } else {
            // include org.xxx
            String[] groupNameArray = param.getGroup().split("\\.");
            for (String group: groupNameArray) {
                // not start or end with dot "."
                if (StringUtils.isBlank(group)) {
                    log.error("group must start with letter, and not end with dot");
                    throw new NodeMgrException(ConstantCode.PARAM_INVALID_LETTER_DIGIT);
                }
                if (!NodeMgrTools.startWithLetter(group)) {
                    log.error("group name must start with letter");
                    throw new NodeMgrException(ConstantCode.PARAM_INVALID_LETTER_DIGIT);
                }
            }
        }
        RspFile rspFile = scaffoldService.exportProject(param);
        log.info("end exportProjectApi useTime:{} result:{}",
            Duration.between(startTime, Instant.now()).toMillis(), rspFile);
        return new BaseResponse(ConstantCode.SUCCESS, rspFile);
    }


    @GetMapping("/check")
    public BaseResponse checkChannelPort(@RequestParam("nodeIp") String nodeIp,
        @RequestParam("channelPort") int channelPort) {
        Instant startTime = Instant.now();
        log.info("start checkChannelPort startTime:{}, nodeIp:{} channelPort:{}",
            startTime.toEpochMilli(), nodeIp, channelPort);
        if(!ValidateUtil.ipv4Valid(nodeIp)) {
            log.error("not valid nodeIp:{}", nodeIp);
            throw new NodeMgrException(ConstantCode.IP_FORMAT_ERROR);
        }
        Boolean result = scaffoldService.telnetChannelPort(nodeIp, channelPort);

        log.info("end exportProjectApi useTime:{} result:{}",
            Duration.between(startTime, Instant.now()).toMillis(), result);
        return new BaseResponse(ConstantCode.SUCCESS, result);
    }
}

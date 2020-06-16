/**
 * Copyright 2014-2020 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.webase.node.mgr.alert.mail.server.config;

import com.webank.webase.node.mgr.base.tools.JsonTools;
import com.webank.webase.node.mgr.alert.mail.server.config.entity.ReqMailServerConfigParam;
import com.webank.webase.node.mgr.alert.mail.server.config.entity.TbMailServerConfig;
import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.entity.BaseResponse;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.base.properties.ConstantProperties;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;
import java.util.List;


/**
 * MailServerConfig Controller
 */
@Log4j2
@RestController
@RequestMapping("mailServer")
public class MailServerConfigController {

    @Autowired
    MailServerConfigService mailServerConfigService;

    @GetMapping("config/{serverId}")
    public Object getServerConfig(@PathVariable("serverId") Integer serverId) {
        Instant startTime = Instant.now();
        log.info("start getServerConfig. startTime:{} serverId:{}",
                startTime.toEpochMilli(), serverId);
        TbMailServerConfig res = mailServerConfigService.queryByServerId(serverId);
        log.info("end getServerConfig. useTime:{}, res:{}",
                Duration.between(startTime, Instant.now()).toMillis(), res);
        return new BaseResponse(ConstantCode.SUCCESS, res);
    }

    @GetMapping("/config/list")
    public Object listServerConfig() {
        Instant startTime = Instant.now();
        log.info("start listServerConfig. startTime:{}",
                startTime.toEpochMilli());
        try{
            List<TbMailServerConfig> resList = mailServerConfigService.getAllMailServerConfig();

            log.info("end listServerConfig. useTime:{}, resList:{}",
                    Duration.between(startTime, Instant.now()).toMillis(), resList);
            return new BaseResponse(ConstantCode.SUCCESS, resList);
        }catch (NodeMgrException e) {
            log.debug("listServerConfig, error, exception:[] ", e);
            return new BaseResponse(ConstantCode.MAIL_SERVER_CONFIG_ERROR, e.getMessage());
        }
    }

    /**
     * update mail server config, such as username, password etc.
     * if param is empty, ignore and not update
     * @param param
     * @return
     */
    @PutMapping("/config")
    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN)
    public Object updateMailServerConfig(@RequestBody ReqMailServerConfigParam param) {
        Instant startTime = Instant.now();
        log.info("start updateMailServerConfig. startTime:{} ReqMailServerConfigParam:{}",
                startTime.toEpochMilli(), JsonTools.toJSONString(param));
        if(param.getServerId() == null) {
            log.debug("updateMailServerConfig, error:{} ",
                    ConstantCode.MAIL_SERVER_CONFIG_PARAM_EMPTY);
            return new BaseResponse(ConstantCode.MAIL_SERVER_CONFIG_PARAM_EMPTY);
        }
        try{
            mailServerConfigService.updateMailServerConfig(param);
        }catch (NodeMgrException e) {
            log.debug("updateMailServerConfig, error, exception:[] ", e);
            return new BaseResponse(ConstantCode.MAIL_SERVER_CONFIG_ERROR, e.getMessage());
        }
        TbMailServerConfig res = mailServerConfigService.queryByServerId(param.getServerId());
        log.info("end updateMailServerConfig. useTime:{}, res:{}",
                Duration.between(startTime, Instant.now()).toMillis(), res);
        return new BaseResponse(ConstantCode.SUCCESS, res);
    }


    /**
     * @Duplicated save mail server configuration
     * @param param
     * @return
     */
//    @PostMapping("config")
//    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN)
//    public Object saveMailServerConfig(@RequestBody ReqMailServerConfigParam param) {
//        Instant startTime = Instant.now();
//        log.info("start saveMailServerConfig. startTime:{} ReqMailServerConfigParam:{}",
//                startTime.toEpochMilli(), JsonTools.toJSONString(param));
//        try{
//            mailServerConfigService.saveMailServerConfig(param);
//            log.info("end saveMailServerConfig. useTime:{}",
//                    Duration.between(startTime, Instant.now()).toMillis());
//            return new BaseResponse(ConstantCode.SUCCESS);
//        }catch (NodeMgrException e) {
//            log.debug("saveMailServerConfig, error, exception:[] ", e);
//            return new BaseResponse(ConstantCode.MAIL_SERVER_CONFIG_ERROR, e.getMessage());
//        }
//    }


    /**
     * @Duplicated delete mail server config, no need to delete
     */
//    @DeleteMapping("/config/{serverId}")
//    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN)
//    public Object deleteByServerId(@PathVariable("serverId") Integer serverId) {
//        Instant startTime = Instant.now();
//        log.info("start deleteByServerId. startTime:{} serverId:{}",
//                startTime.toEpochMilli(), serverId);
//        try{
//            mailServerConfigService.deleteByServerId(serverId);
//            log.info("end saveMailServerConfig. useTime:{}",
//                    Duration.between(startTime, Instant.now()).toMillis());
//            return new BaseResponse(ConstantCode.SUCCESS);
//        }catch (NodeMgrException e) {
//            log.debug("deleteByServerId, error, exception:[] ", e);
//            return new BaseResponse(ConstantCode.MAIL_SERVER_CONFIG_ERROR, e.getMessage());
//        }
//    }
}

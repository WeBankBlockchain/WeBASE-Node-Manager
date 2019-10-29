/**
 * Copyright 2014-2019 the original author or authors.
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

import com.webank.webase.node.mgr.alert.mail.server.config.entity.ReqMailServerConfigParam;
import com.webank.webase.node.mgr.alert.mail.server.config.entity.TbMailServerConfig;
import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.entity.BaseResponse;
import com.webank.webase.node.mgr.base.properties.ConstantProperties;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Log4j2
@RestController
@RequestMapping("mailServer")
public class MailServerConfigController {

    @Autowired
    MailServerConfigService mailServerConfigService;

    @GetMapping("/config/{serverId}")
    public Object getServerConfig(@PathVariable("serverId") int serverId) {
        TbMailServerConfig res = mailServerConfigService.queryByServerId(serverId);
        return res;
    }

    @PostMapping("/config")
//    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN)
    public Object saveMailServerConfig(@RequestBody ReqMailServerConfigParam param) {

        mailServerConfigService.saveMailServerConfig(param);
        return new BaseResponse(ConstantCode.SUCCESS);
    }

    @PutMapping("/config")
//    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN)
    public Object updateMailServerConfig(@RequestBody ReqMailServerConfigParam param) {

        mailServerConfigService.updateMailServerConfig(param);
        return new BaseResponse(ConstantCode.SUCCESS);
    }

    @DeleteMapping("/config/{serverId}")
//    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN)
    public Object updateMailServerConfig(@PathVariable("serverId") int serverId) {

        mailServerConfigService.deleteByServerId(serverId);
        return new BaseResponse(ConstantCode.SUCCESS);
    }
}

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

package com.webank.webase.node.mgr.appintegration;

import com.webank.webase.node.mgr.appintegration.entity.AppAddInfo;
import com.webank.webase.node.mgr.appintegration.entity.AppInfoParam;
import com.webank.webase.node.mgr.appintegration.entity.TbAppInfo;
import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.controller.BaseController;
import com.webank.webase.node.mgr.base.entity.BasePageResponse;
import com.webank.webase.node.mgr.base.entity.BaseResponse;
import com.webank.webase.node.mgr.base.enums.AppType;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.config.properties.ConstantProperties;
import com.webank.webase.node.mgr.tools.JsonTools;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * application integration controller.
 */
@Log4j2
@RestController
@RequestMapping(value = "app")
public class AppIntegrationController extends BaseController {

    @Autowired
    private AppIntegrationService appIntegrationService;

    @PostMapping("/save")
    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN)
    public BaseResponse saveApp(@RequestBody @Valid AppAddInfo appAddInfo, BindingResult result) {
        checkBindResult(result);
        Instant startTime = Instant.now();
        log.info("start saveApp startTime:{} appName:{}", startTime.toEpochMilli(),
                appAddInfo.getAppName());

        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        TbAppInfo tbAppInfo = appIntegrationService.save(appAddInfo);
        baseResponse.setData(tbAppInfo);

        log.info("end saveApp useTime:{}", Duration.between(startTime, Instant.now()).toMillis());
        return baseResponse;
    }

    /**
     * get app info list
     */
    @GetMapping(value = "/list")
    public BasePageResponse queryAppList(@RequestParam(required = false) Integer appType,
            @RequestParam(required = false) String appName,
            @RequestParam(required = false) String appKey) throws NodeMgrException {
        BasePageResponse pageResponse = new BasePageResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start queryAppList startTime:{} appName:{},appKey:{}", startTime.toEpochMilli(),
                appName, appKey);

        // param
        AppInfoParam param = new AppInfoParam();
        param.setAppName(appName);
        param.setAppKey(appKey);
        if (appType == null) {
            param.setAppType(AppType.NEW.getValue());
        } else {
            param.setAppType(appType);
        }
        // query app info
        int count = appIntegrationService.countOfAppInfo(param);
        pageResponse.setTotalCount(count);
        if (count > 0) {
            List<TbAppInfo> list = appIntegrationService.listOfAppInfo(param);
            pageResponse.setData(list);
        }

        log.info("end queryAppList useTime:{}",
                Duration.between(startTime, Instant.now()).toMillis());
        return pageResponse;
    }
    
    /**
     * delete by frontId
     */
    @DeleteMapping("/{id}")
    public BaseResponse deleteApp(@PathVariable("id") Integer id) {
        Instant startTime = Instant.now();
        log.info("start deleteApp startTime:{} id:{}",
            startTime.toEpochMilli(), id);
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        appIntegrationService.deleteApp(id);
        log.info("end deleteApp useTime:{} result:{}",
            Duration.between(startTime, Instant.now()).toMillis(), JsonTools.toJSONString(baseResponse));
        return baseResponse;
    }
}

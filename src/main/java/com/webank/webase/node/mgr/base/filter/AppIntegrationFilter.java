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
package com.webank.webase.node.mgr.base.filter;

import com.webank.webase.node.mgr.appintegration.AppIntegrationService;
import com.webank.webase.node.mgr.appintegration.entity.TbAppInfo;
import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.base.properties.ConstantProperties;
import com.webank.webase.node.mgr.base.tools.NodeMgrTools;
import java.util.Objects;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * validate open api info.
 */
@Log4j2
@Component
public class AppIntegrationFilter implements HandlerInterceptor {

    @Autowired
    private AppIntegrationService appIntegrationService;
    @Autowired
    private ConstantProperties cproperties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
            Object handler) {
        try {
            validateAppRequest(request);
        } catch (NodeMgrException ex) {
            NodeMgrTools.responseRetCodeException(response, ex.getRetCode());
            return false;
        }
        return true;
    }

    /**
     * validate app request.
     */
    private void validateAppRequest(HttpServletRequest request) {
        String timestamp = request.getParameter(ConstantProperties.PARAM_TIMESTAMP);
        String appKey = request.getParameter(ConstantProperties.PARAM_APP_KEY);
        String signature = request.getParameter(ConstantProperties.PARAM_SIGNATURE);
        log.debug("validateAppRequest. timestamp:{} appKey:{} signature:{}", timestamp, appKey,
                signature);
        // check param
        if (StringUtils.isBlank(timestamp)) {
            throw new NodeMgrException(ConstantCode.TIMESTAMP_CANNOT_EMPTY);
        }
        if (StringUtils.isBlank(appKey)) {
            throw new NodeMgrException(ConstantCode.APPKEY_CANNOT_EMPTY);
        }
        if (StringUtils.isBlank(signature)) {
            throw new NodeMgrException(ConstantCode.SIGNATURE_CANNOT_EMPTY);
        }
        TbAppInfo tbAppInfo = appIntegrationService.queryAppInfoByAppKey(appKey);
        if (Objects.isNull(tbAppInfo)) {
            throw new NodeMgrException(ConstantCode.APPKEY_NOT_EXISTS);
        }
        long reqeustInterval = System.currentTimeMillis() - Long.valueOf(timestamp);
        if (reqeustInterval > cproperties.getAppRequestTimeOut()) {
            throw new NodeMgrException(ConstantCode.TIMESTAMP_TIMEOUT);
        }
        // joint data string to md5
        String dataStr = timestamp + appKey + tbAppInfo.getAppSecret();
        if (!signature.equals(NodeMgrTools.md5Encrypt(dataStr))) {
            log.warn("fail validateAppRequest. signature not match.");
            throw new NodeMgrException(ConstantCode.SIGNATURE_NOT_MATCH);
        }
    }

}

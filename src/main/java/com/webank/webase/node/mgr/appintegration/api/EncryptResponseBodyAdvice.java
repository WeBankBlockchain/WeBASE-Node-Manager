package com.webank.webase.node.mgr.appintegration.api;

import com.webank.webase.node.mgr.appintegration.AppIntegrationService;
import com.webank.webase.node.mgr.appintegration.entity.TbAppInfo;
import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.base.properties.ConstantProperties;
import com.webank.webase.node.mgr.base.tools.AesUtils;
import com.webank.webase.node.mgr.base.tools.JsonTools;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * EncryptResponseBodyAdvice.
 */
@Slf4j
@Component
@ControllerAdvice(basePackages = "com.webank.webase.node.mgr.appintegration.api")
public class EncryptResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    @Autowired
    private AppIntegrationService appIntegrationService;
    @Autowired
    private ConstantProperties cp;

    @Override
    public boolean supports(MethodParameter methodParameter,
            Class<? extends HttpMessageConverter<?>> aClass) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter mp, MediaType mediaType,
            Class<? extends HttpMessageConverter<?>> aClass, ServerHttpRequest serverHttpRequest,
            ServerHttpResponse serverHttpResponse) {
        String isTransferEncrypt = serverHttpRequest.getHeaders()
                .getFirst(ConstantProperties.PARAM_IS_TRANSFER_ENCRYPT);
        log.debug("beforeBodyWrite isTransferEncrypt:{}", isTransferEncrypt);
        if (StringUtils.isBlank(isTransferEncrypt)) {
            return body;
        }
        if (!isTransferEncrypt.equals(String.valueOf(cp.isTransferEncrypt()))) {
            throw new NodeMgrException(ConstantCode.ENCRYPT_NOT_MATCH);
        }
        if (isTransferEncrypt.equals(ConstantProperties.ENCRYPT_FALSE)) {
            return body;
        }

        // encrypt response body
        String appKey = serverHttpRequest.getHeaders().getFirst(ConstantProperties.PARAM_APP_KEY);
        if (StringUtils.isBlank(appKey)) {
            throw new NodeMgrException(ConstantCode.APPKEY_CANNOT_EMPTY);
        }
        TbAppInfo tbAppInfo = appIntegrationService.queryAppInfoByAppKey(appKey);
        if (Objects.isNull(tbAppInfo)) {
            throw new NodeMgrException(ConstantCode.APPKEY_NOT_EXISTS);
        }
        String content = JsonTools.toJSONString(body);
        Object result = AesUtils.encrypt(content, tbAppInfo.getAppSecret().substring(0, 16));
        return result;
    }
}

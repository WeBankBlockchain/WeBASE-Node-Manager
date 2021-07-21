package com.webank.webase.node.mgr.appintegration.api;

import com.webank.webase.node.mgr.appintegration.AppIntegrationService;
import com.webank.webase.node.mgr.appintegration.entity.TbAppInfo;
import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.config.properties.ConstantProperties;
import com.webank.webase.node.mgr.tools.AesUtils;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice;

/**
 * DecryptRequestBodyAdvice.
 */
@Slf4j
@Component
@ControllerAdvice(basePackages = "com.webank.webase.node.mgr.appintegration.api")
public class DecryptRequestBodyAdvice implements RequestBodyAdvice {

    @Autowired
    private AppIntegrationService appIntegrationService;
    @Autowired
    private ConstantProperties cp;

    @Override
    public boolean supports(MethodParameter methodParameter, Type type,
            Class<? extends HttpMessageConverter<?>> aClass) {
        return true;
    }

    @Override
    public Object handleEmptyBody(Object body, HttpInputMessage httpInputMessage,
            MethodParameter methodParameter, Type type,
            Class<? extends HttpMessageConverter<?>> aClass) {
        return body;
    }

    @Override
    public Object afterBodyRead(Object body, HttpInputMessage httpInputMessage,
            MethodParameter methodParameter, Type type,
            Class<? extends HttpMessageConverter<?>> aClass) {
        return body;
    }

    @Override
    public HttpInputMessage beforeBodyRead(HttpInputMessage httpInputMessage,
            MethodParameter methodParameter, Type type,
            Class<? extends HttpMessageConverter<?>> aClass) throws IOException {
        HttpHeaders headers = httpInputMessage.getHeaders();
        String isTransferEncrypt = headers.getFirst(ConstantProperties.PARAM_IS_TRANSFER_ENCRYPT);
        log.debug("beforeBodyRead isTransferEncrypt:{}", isTransferEncrypt);
        if (StringUtils.isBlank(isTransferEncrypt)) {
            return httpInputMessage;
        }
        if (!isTransferEncrypt.equals(String.valueOf(cp.isTransferEncrypt()))) {
            throw new NodeMgrException(ConstantCode.ENCRYPT_NOT_MATCH);
        }
        if (isTransferEncrypt.equals(ConstantProperties.ENCRYPT_FALSE)) {
            return httpInputMessage;
        }
        String appKey = httpInputMessage.getHeaders().getFirst(ConstantProperties.PARAM_APP_KEY);
        if (StringUtils.isBlank(appKey)) {
            throw new NodeMgrException(ConstantCode.APPKEY_CANNOT_EMPTY);
        }
        TbAppInfo tbAppInfo = appIntegrationService.queryAppInfoByAppKey(appKey);
        if (Objects.isNull(tbAppInfo)) {
            throw new NodeMgrException(ConstantCode.APPKEY_NOT_EXISTS);
        }
        String bodyStr =
                StreamUtils.copyToString(httpInputMessage.getBody(), Charset.forName("utf-8"));
        byte[] body =
                AesUtils.decrypt(bodyStr, tbAppInfo.getAppSecret().substring(0, 16)).getBytes();
        return new MyHttpInputMessage(headers, body);
    }

    /**
     * 自定义消息体，因为getBody()只能调一次，所以要重新封装一个可重复读的消息体
     */
    @AllArgsConstructor
    public static class MyHttpInputMessage implements HttpInputMessage {

        private HttpHeaders headers;

        private byte[] body;

        @Override
        public InputStream getBody() throws IOException {
            return new ByteArrayInputStream(body);
        }

        @Override
        public HttpHeaders getHeaders() {
            return headers;
        }
    }
}

/**
 * Copyright 2014-2021  the original author or authors.
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
package com.webank.webase.node.mgr.tools;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

/**
 * request param/body formater
 */
public class HttpRequestTools {

    /**
     * get uri.
     */
    public static String getUri(HttpServletRequest httpRequest) {
        String uri = httpRequest.getRequestURI().replace("//", "/");
        String contextPath = httpRequest.getContextPath();
        return StringUtils.removeStart(uri, contextPath);
    }


    /**
     * convert map to query params
     * ex: uri:permission,
     *     params: (groupId, 1) (address, 0x01)
     *
     * result: permission?groupId=1&address=0x01
     */
    public static String getQueryUri(String uriHead, Map<String, String> map) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();

        for (Map.Entry<String, String> entry : map.entrySet()) {
            params.add(entry.getKey(), entry.getValue());
        }

        UriComponents uriComponents = UriComponentsBuilder.newInstance()
                .queryParams(params).build();

        return uriHead + uriComponents.toString();
    }

    public static HttpHeaders headers(String fileName) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.set(HttpHeaders.CONTENT_DISPOSITION,
            "attachment;filename*=UTF-8''" + encode(fileName));
        return httpHeaders;
    }

    public static String encode(String name) {
        try {
            return URLEncoder.encode(name, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}

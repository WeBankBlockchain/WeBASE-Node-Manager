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
package com.webank.webase.node.mgr.security;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * Fix XSS safety problem.
 *
 */
public class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {
    /**
     * 定义script的正则表达式
     */
    private static final String REG_SCRIPT = "<script[^>]*?>[\\s\\S]*?</script>";

    /**
     * 定义style的正则表达式
     */
    private static final String REG_STYLE = "<style[^>]*?>[\\s\\S]*?</style>";

    /**
     * 定义HTML标签的正则表达式
     */
    private static final String REG_HTML = "<[^>]+>";

    /**
     * 定义所有w标签
     */
    private static final String REG_W = "<w[^>]*?>[\\s\\S]*?</w[^>]*?>";

    private static final String REG_JAVASCRIPT = ".*javascript.*";


    public XssHttpServletRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Map<String, String[]> getParameterMap() {
        Map<String, String[]> requestMap = super.getParameterMap();
        for (Object o : requestMap.entrySet()) {
            Map.Entry me = (Map.Entry) o;
            String[] values = (String[]) me.getValue();
            for (int i = 0; i < values.length; i++) {
                values[i] = xssClean(values[i]);
            }
        }
        return requestMap;
    }

    @Override
    public String[] getParameterValues(String paramString) {
        String[] values = super.getParameterValues(paramString);
        if (values == null) {
            return null;
        }
        int i = values.length;
        String[] result = new String[i];
        for (int j = 0; j < i; j++) {
            result[j] = xssClean(values[j]);
        }
        return result;
    }

    @Override
    public String getParameter(String paramString) {
        String str = super.getParameter(paramString);
        if (str == null) {
            return null;
        }
        return xssClean(str);
    }


    @Override
    public String getHeader(String paramString) {
        String str = super.getHeader(paramString);
        if (str == null) {
            return null;
        }
        str = str.replaceAll("[\r\n]", "");
        return xssClean(str);
    }

    /**
     * [xssClean 过滤特殊、敏感字符]
     * 
     * @param value [请求参数]
     * @return [value]
     */
    private String xssClean(String value) {
        if (value == null || "".equals(value)) {
            return value;
        }
        Pattern pw = Pattern.compile(REG_W, Pattern.CASE_INSENSITIVE);
        Matcher mw = pw.matcher(value);
        value = mw.replaceAll("");

        Pattern script = Pattern.compile(REG_SCRIPT, Pattern.CASE_INSENSITIVE);
        value = script.matcher(value).replaceAll("");

        Pattern style = Pattern.compile(REG_STYLE, Pattern.CASE_INSENSITIVE);
        value = style.matcher(value).replaceAll("");

        Pattern htmlTag = Pattern.compile(REG_HTML, Pattern.CASE_INSENSITIVE);
        value = htmlTag.matcher(value).replaceAll("");

        Pattern javascript = Pattern.compile(REG_JAVASCRIPT, Pattern.CASE_INSENSITIVE);
        value = javascript.matcher(value).replaceAll("");
        return value;
    }

}

/**
 * Copyright 2014-2019  the original author or authors.
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
package com.webank.webase.node.mgr.base.tools;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.code.RetCode;
import com.webank.webase.node.mgr.base.entity.BaseResponse;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;

/**
 * common method.
 */
@Log4j2
public class NodeMgrTools {

    public static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE_TIME_FORMAT_NO_SPACE = "yyyyMMddHHmmss";
    private static final char[] CHARS = {'2', '3', '4', '5', '6', '7', '8', '9', 'a',
        'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'm', 'n', 'p', 'q', 'r', 's',
        't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'J',
        'K', 'L', 'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};


    /**
     * 获取指定位数的数字和字母组合的字符串
     *
     * @param length 字符串长度
     */
    public static String randomString(int length) {
        if (length > CHARS.length) {
            return null;
        }
        StringBuffer sb = new StringBuffer();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(CHARS[random.nextInt(CHARS.length)]);
        }
        return sb.toString();
    }


    /**
     * convert hex to localDateTime.
     */
    public static LocalDateTime hex2LocalDateTime(String hexStr) {
        if (StringUtils.isBlank(hexStr)) {
            return null;
        }
        Long timeLong = Long.parseLong(hexStr, 16);
        return timestamp2LocalDateTime(timeLong);
    }

    /**
     * convert timestamp to localDateTime.
     */
    public static LocalDateTime timestamp2LocalDateTime(Long inputTime) {
        if (inputTime == null) {
            log.warn("timestamp2LocalDateTime fail. inputTime is null");
            return null;
        }
        Instant instant = Instant.ofEpochMilli(inputTime);
        ZoneId zone = ZoneId.systemDefault();
        return LocalDateTime.ofInstant(instant, zone);
    }

    /**
     * convert String to localDateTime.
     */
    public static LocalDateTime string2LocalDateTime(String time, String format) {
        if (StringUtils.isBlank(time)) {
            log.info("string2LocalDateTime. time is null");
            return null;
        }
        if (StringUtils.isBlank(format)) {
            log.info("string2LocalDateTime. format is null");
            format = DEFAULT_DATE_TIME_FORMAT;
        }
        DateTimeFormatter df = DateTimeFormatter.ofPattern(format);
        return LocalDateTime.parse(time, df);
    }

    /**
     * convert localDateTime to String.
     */
    public static String localDateTime2String(LocalDateTime dateTime, String format) {
        if (dateTime == null) {
            log.warn("localDateTime2String. dateTime is null");
            return null;
        }
        if (StringUtils.isBlank(format)) {
            log.info("localDateTime2String. format is null");
            format = DEFAULT_DATE_TIME_FORMAT;
        }
        DateTimeFormatter df = DateTimeFormatter.ofPattern(format);
        String localTimeStr = df.format(dateTime);
        return localTimeStr;
    }

    /**
     * conver object to java bean.
     */
    public static <T> T object2JavaBean(Object obj, Class<T> clazz) {
        if (obj == null || clazz == null) {
            log.warn("object2JavaBean. obj or clazz null");
            return null;
        }
        String jsonStr = JSON.toJSONString(obj);

        return JSON.parseObject(jsonStr, clazz);
    }


    public static JSONObject Object2JSONObject(Object obj) {
        if (obj == null) {
            log.warn("obj is null");
            return null;
        }
        String objJson = JSON.toJSONString(obj);
        return JSONObject.parseObject(objJson);
    }

    /**
     * encode list by sha.
     */
    public static String shaList(List<String> values) {
        log.info("shaList start. values:{}", JSON.toJSONString(values));
        // list按字段排序，并转换成字符串
        String list2SortString = list2SortString(values);
        // SHA加密字符串
        String shaStr = shaEncode(list2SortString);
        log.info("shaList end. ShaStr:{}", shaStr);
        return shaStr;
    }

    /**
     * encode String by sha.
     */
    public static String shaEncode(String inStr) {
        MessageDigest sha = null;
        try {
            sha = MessageDigest.getInstance("SHA-256");
        } catch (Exception e) {
            log.info("shaEncode fail:", e);
            return "";
        }

        byte[] byteArray = new byte[0];
        try {
            byteArray = inStr.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.warn("shaEncode fail:", e);
            return null;
        }
        byte[] md5Bytes = sha.digest(byteArray);
        StringBuffer hexValue = new StringBuffer();
        for (int i = 0; i < md5Bytes.length; i++) {
            int val = ((int) md5Bytes[i]) & 0xff;
            if (val < 16) {
                hexValue.append("0");
            }
            hexValue.append(Integer.toHexString(val));
        }
        return hexValue.toString();
    }

    /**
     * sort list and convert to String.
     */
    private static String list2SortString(List<String> values) {
        if (values == null) {
            throw new NullPointerException("values is null");
        }

        values.removeAll(Collections.singleton(null));// remove null
        Collections.sort(values);

        StringBuilder sb = new StringBuilder();
        for (String s : values) {
            sb.append(s);
        }
        return sb.toString();
    }

    /**
     * convert list to url param.
     */
    public static String convertUrlParam(List<String> nameList, List<Object> valueList) {
        if (nameList == null || valueList == null || nameList.size() != valueList.size()) {
            log.error("fail convertUrlParm. nameList or valuesList is error");
            return "";
        }
        StringBuilder urlParamB = new StringBuilder("");
        for (int i = 0; i < valueList.size(); i++) {
            Object value = valueList.get(i);
            if (value != null) {
                urlParamB.append("&").append(nameList.get(i)).append("=").append(value);
            }
        }

        if (urlParamB.length() == 0) {
            log.info("fail convertUrlParam. urlParamB length is 0");
            return "";
        }

        String urlParam = urlParamB.toString();
        return urlParam.substring(1);

    }

    /**
     * convert list to map.
     */
    public static Map<String, Object> buidMap(List<String> nameList, List<Object> valueList) {
        if (nameList == null || valueList == null || nameList.size() != valueList.size()) {
            log.error("fail buidMap. nameList or valuesList is error");
            return null;
        }
        Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < nameList.size(); i++) {
            map.put(nameList.get(i), valueList.get(i));
        }
        return map;
    }

    /**
     * check server host.
     */
    public static void checkServerHostConnect(String serverHost) {
        Boolean state;
        try {
            InetAddress address = InetAddress.getByName(serverHost);
            state = address.isReachable(500);
        } catch (Exception ex) {
            log.error("fail checkServerHostConnect", ex);
            throw new NodeMgrException(ConstantCode.SERVER_HOST_CONNECT_FAIL);
        }

        if (!state) {
            log.info("host connect state:{}", state);
            throw new NodeMgrException(ConstantCode.SERVER_HOST_CONNECT_FAIL);
        }
    }


    /**
     * check host an port.
     */
    public static void checkServerConnect(String serverHost, int serverPort) {
        //check host
        checkServerHostConnect(serverHost);

        try {
            //check port
            Socket socket = new Socket();
            socket.setReceiveBufferSize(8193);
            socket.setSoTimeout(500);
            SocketAddress address = new InetSocketAddress(serverHost, serverPort);
            socket.connect(address, 1000);
        } catch (Exception ex) {
            log.error("fail checkServerConnect", ex);
            throw new NodeMgrException(ConstantCode.SERVER_PORT_CONNECT_FAIL);
        }
    }


    /**
     * response exception.
     */
    public static void responseRetCodeException(HttpServletResponse response, RetCode retCode) {
        BaseResponse baseResponse = new BaseResponse(retCode);
/*        baseResponse.setMessage(ex.getMessage());
        response.setContentType("application/json;charset=UTF-8");*/
        try {
            response.getWriter().write(JSON.toJSONString(baseResponse));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * check target time is valid.
     *
     * @param dateTime target time.
     * @param validLength y:year, M:month, d:day of month, h:hour, m:minute, n:forever valid;
     * example1:1d;example2:n
     */
    public static boolean isDateTimeInValid(LocalDateTime dateTime, String validLength) {
        log.debug("start isDateTimeInValid. dateTime:{} validLength:{}", dateTime, validLength);
        if ("n".equals(validLength)) {
            return true;
        }
        if (Objects.isNull(dateTime) || StringUtils.isBlank(validLength)
            || validLength.length() < 2) {
            return false;
        }

        String lifeStr = validLength.substring(0, validLength.length() - 1);
        if (!StringUtils.isNumeric(lifeStr)) {
            log.warn("fail isDateTimeInValid");
            throw new RuntimeException("fail isDateTimeInValid. validLength is error");
        }
        int lifeValue = Integer.valueOf(lifeStr);
        String lifeUnit = validLength.substring(validLength.length() - 1);

        LocalDateTime now = LocalDateTime.now();
        switch (lifeUnit) {
            case "y":
                return dateTime.getYear() - now.getYear() < lifeValue;
            case "M":
                return dateTime.getMonthValue() - now.getMonthValue() < lifeValue;
            case "d":
                return dateTime.getDayOfMonth() - now.getDayOfMonth() < lifeValue;
            case "m":
                return dateTime.getMinute() - now.getMinute() < lifeValue;
            default:
                log.warn("fail isDateTimeInValid lifeUnit:{}", lifeUnit);
                return false;
        }
    }


}

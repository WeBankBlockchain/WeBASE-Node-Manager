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

import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.code.RetCode;
import com.webank.webase.node.mgr.base.entity.BaseResponse;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.tools.pagetools.entity.MapHandle;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.sdk.v3.crypto.CryptoSuite;
import org.fisco.bcos.sdk.v3.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.v3.utils.Numeric;
import org.springframework.core.io.ClassPathResource;

/**
 * common method in node manager
 */
@Log4j2
public class NodeMgrTools {

    public static final String TOKEN_HEADER_NAME = "AuthorizationToken";
    private static final String TOKEN_START = "Token";
    public static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE_TIME_FORMAT_NO_SPACE = "yyyyMMddHHmmss";
    private static final char[] CHARS = {'2', '3', '4', '5', '6', '7', '8', '9', 'a',
        'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'm', 'n', 'p', 'q', 'r', 's',
        't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'J',
        'K', 'L', 'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
    private static Random random = new Random();
    public static final String HEADER_ACCOUNT = "Account";


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
    public static LocalDateTime timestamp2LocalDateTime(Long inputTimeStamp) {
        if (inputTimeStamp == null) {
            log.warn("timestamp2LocalDateTime fail. inputTimeStamp is null");
            return null;
        }
        Instant instant = Instant.ofEpochMilli(inputTimeStamp);
        ZoneId zone = ZoneId.systemDefault();
        return LocalDateTime.ofInstant(instant, zone);
    }

    /**
     * LocalDateTime to timestamp
     */
    public static Long localDateTime2Timestamp(LocalDateTime inputDateTime) {
        if (inputDateTime == null) {
            log.warn("localDateTime2Timestamp fail. inputDateTime is null");
            return null;
        }
        return inputDateTime.toInstant(ZoneOffset.of("+8")).toEpochMilli();
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
     * timestamp to util.Date
     * @param inputTimeStamp
     * @return
     */
    public static Date timestamp2Date(Long inputTimeStamp) {
        if (inputTimeStamp == null) {
            log.warn("timestamp2Date fail. inputTimeStamp is null");
            return null;
        }
        ZoneId zone = ZoneId.systemDefault();
        Instant instant = Instant.ofEpochMilli(inputTimeStamp).atZone(zone).toInstant();
        return Date.from(instant);
    }

    /**
     * conver object to java bean.
     */
    public static <T> T object2JavaBean(Object obj, Class<T> clazz) {
        if (obj == null || clazz == null) {
            log.warn("object2JavaBean. obj or clazz null");
            return null;
        }
        String jsonStr = JsonTools.toJSONString(obj);

        return JsonTools.toJavaObject(jsonStr, clazz);
    }


    /**
     * encode list by sha.
     */
    public static String shaList(List<String> values) {
        log.info("shaList start. values:{}", JsonTools.toJSONString(values));
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

        byte[] byteArray = new byte[0];
        try {
            byteArray = inStr.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.warn("shaEncode fail:", e);
            return null;
        }
        byte[] hashValue = getHashValue(byteArray);
        if (hashValue == null) {
            return null;
        }
        StringBuffer hexValue = new StringBuffer();
        for (int i = 0; i < hashValue.length; i++) {
            int val = ((int) hashValue[i]) & 0xff;
            if (val < 16) {
                hexValue.append("0");
            }
            hexValue.append(Integer.toHexString(val));
        }
        return hexValue.toString();
    }

    /**
     * get hash value
     * type: sha256
     */
    public static byte[] getHashValue(byte[] byteArray) {
        byte[] hashResult;
        MessageDigest sha = null;
        try {
            sha = MessageDigest.getInstance("SHA-256");
            hashResult = sha.digest(byteArray);
            return hashResult;
        } catch (Exception e) {
            log.error("shaEncode getHashValue fail:", e);
            return null;
        }

    }

    /**
     * get x509 cert's fingerprint
     * Hash using: SHA-1
      * @param byteArray
     * @return
     */
    public static String getCertFingerPrint(byte[] byteArray) {
        byte[] hashResult;
        MessageDigest sha = null;
        try {
            sha = MessageDigest.getInstance("SHA-1");
            hashResult = sha.digest(byteArray);
            return Numeric.toHexStringNoPrefix(hashResult).toUpperCase();
        } catch (Exception e) {
            log.error("shaEncode getCertFingerPrint fail:", e);
            return null;
        }
    }
    /**
     * sort list and convert to String.
     */
    private static String list2SortString(List<String> values) {
        if (values == null) {
            throw new NullPointerException("values is null");
        }

        // remove null
        values.removeAll(Collections.singleton(null));
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
            throw new NodeMgrException(ConstantCode.SERVER_CONNECT_FAIL);
        }

        if (!state) {
            log.info("host connect state:{}", state);
            throw new NodeMgrException(ConstantCode.SERVER_CONNECT_FAIL);
        }
    }


    /**
     * check host an port.
     */
    public static void checkServerConnect(String serverHost, int serverPort) {
        // check host
        // checkServerHostConnect(serverHost);

        Socket socket =null;
        try {
            //check port
            socket = new Socket();
            socket.setReceiveBufferSize(8193);
            socket.setSoTimeout(500);
            SocketAddress address = new InetSocketAddress(serverHost, serverPort);
            socket.connect(address, 1000);
        } catch (Exception ex) {
            log.warn("fail checkServerConnect [{}:{}]", serverHost, serverPort);
            throw new NodeMgrException(ConstantCode.SERVER_CONNECT_FAIL);
        }finally {
            if(Objects.nonNull(socket)){
                try {
                    socket.close();
                } catch (IOException e) {
                    log.error("fail close socket", e);
                }
            }
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
            response.getWriter().write(JsonTools.toJSONString(baseResponse));
        } catch (IOException e) {
            log.error("fail responseRetCodeException",e);
        }
    }


    /**
     * check target time if within time period
     *
     * @param lastTime target time.
     * @param validLength y:year, M:month, d:day of month, h:hour, m:minute, n:forever valid;
     * example1: 1d; example2:n
     */
    public static boolean isWithinPeriod(LocalDateTime lastTime, String validLength) {
        log.debug("start isWithinTime. dateTime:{} validLength:{}", lastTime, validLength);
        if ("n".equals(validLength)) {
            return true;
        }
        if (Objects.isNull(lastTime) || StringUtils.isBlank(validLength)
            || validLength.length() < 2) {
            return false;
        }
        // example: 2d (2 day)
        // 2
        String lifeStr = validLength.substring(0, validLength.length() - 1);
        if (!StringUtils.isNumeric(lifeStr)) {
            log.warn("fail isWithinTime");
            throw new RuntimeException("fail isWithinTime. validLength is error");
        }
        int lifeValue = Integer.parseInt(lifeStr);
        // d
        String lifeUnit = validLength.substring(validLength.length() - 1);
        // now is day 2, last time is day 1, 2 - 1 = 1 < 2 true
        // now is day 3, last time is day 1, 3 - 1 = 2 < 2 false, not within
        LocalDateTime now = LocalDateTime.now();
        switch (lifeUnit) {
            case "y":
                return now.getYear() - lastTime.getYear() < lifeValue;
            case "M":
                return now.getMonthValue() - lastTime.getMonthValue() < lifeValue;
            case "d":
                return  now.getDayOfMonth() - lastTime.getDayOfMonth() < lifeValue;
            case "m":
                return now.getMinute() - lastTime.getMinute() < lifeValue;
            default:
                log.warn("fail isWithinTime lifeUnit:{}", lifeUnit);
                return false;
        }
    }

    /**
     * response string.
     */
    public static void responseString(HttpServletResponse response, String str) {
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SYSTEM_EXCEPTION);
        if (StringUtils.isNotBlank(str)) {
            baseResponse.setMessage(str);
        }

        RetCode retCode;
        if (JsonTools.isJson(str) && (retCode = JsonTools.toJavaObject(str, RetCode.class)) != null) {
            baseResponse = new BaseResponse(retCode);
        }

        try {
            response.getWriter().write(JsonTools.toJSONString(baseResponse));
        } catch (IOException e) {
            log.error("fail responseRetCodeException", e);
        }
    }


    /**
     * get token.
     */
    public static synchronized String getToken(HttpServletRequest request) {
        String header = request.getHeader(TOKEN_HEADER_NAME);
        if (StringUtils.isBlank(header)) {
            log.error("not found AuthorizationToken token");
            throw new NodeMgrException(ConstantCode.INVALID_TOKEN);
        }

        String token = StringUtils.removeStart(header, TOKEN_START).trim();
        if (StringUtils.isBlank(token)) {
            log.error("token is empty");
            throw new NodeMgrException(ConstantCode.INVALID_TOKEN);
        }
        return token;
    }

    /**
     * get token.
     */
    public static synchronized String getAccount(HttpServletRequest request) {
        String accountName = request.getHeader(HEADER_ACCOUNT);
        log.debug("getAccount from header: [{}]", accountName);
        return accountName;
    }




    /**
     * sort Mappings
     * @param mapping
     * @return List<MapHandle>
     */
    public static List<MapHandle> sortMap(Map<?, ?> mapping) {
        List<MapHandle> list = new ArrayList<>();
        Iterator it = mapping.keySet().iterator();
        while (it.hasNext()) {
            String key = it.next().toString();
            MapHandle handle = new MapHandle(key, mapping.get(key));
            list.add(handle);
        }
        Collections.sort(list, new Comparator<MapHandle>() {
            @Override
            public int compare(MapHandle o1, MapHandle o2) {
                return o1.getKey().compareTo(o2.getKey());
            }
        });
        return list;
    }
    
    /**
     * parseHexStr2Int.
     * 
     * @param str str
     * @return
     */
    public static int parseHexStr2Int(String str) {
        if (StringUtils.isBlank(str)) {
            return 0;
        }
        return Integer.parseInt(str.substring(2), 16);
    }

    /**
     * remove "0x" and last character.
     */
    public static String removeBinFirstAndLast(String contractBin, int removaLastLength) {
        if (StringUtils.isBlank(contractBin)) {
            return null;
        }
        String contractBinResult = removeFirstStr(contractBin, "0x");
        if (contractBinResult.length() > removaLastLength) {
            contractBinResult = contractBinResult.substring(0, contractBinResult.length() - removaLastLength);
        }
        return contractBinResult;
    }

    /**
     * remove fist string.
     */
    public static String removeFirstStr(String input, String target) {
        if (StringUtils.isBlank(input) || StringUtils.isBlank(target)) {
            return input;
        }
        if (input.startsWith(target)) {
            input = StringUtils.removeStart(input, target);
        }
        return input;
    }

    public static String encodedBase64Str(String input) {
        if (input == null) {
            return null;
        }
        return Base64.getEncoder().encodeToString(input.getBytes());
    }

    /**
     * 只包含中文
     */
    public static boolean notContainsChinese(String input) {
        if (StringUtils.isBlank(input)) {
            return true;
        }
        String regex = "[^\\u4e00-\\u9fa5]+";
        return input.matches(regex);
    }

    /**
     * get version number without character
     * @param verStr ex: v2.4.1, ex 1.5.0
     * @return ex: 241, 150
     */
    public static int getVersionFromStr(String verStr) {
        log.info("getVersionFromStr verStr:{}", verStr);
        // remove v and split
        if (verStr.toLowerCase().startsWith("v")) {
            verStr = verStr.substring(1);
        }
        String[] versionArr = verStr.split("\\.");
        if (versionArr.length < 3) {
            log.error("getVersionFromStr versionArr:{}", (Object) versionArr);
            throw new NodeMgrException(ConstantCode.PARAM_EXCEPTION);
        }
        // get num
        int version = Integer.parseInt(versionArr[0]) * 100
            + Integer.parseInt(versionArr[1]) * 10 + Integer.parseInt(versionArr[2]);
        log.info("getVersionFromStr version:{}", version);
        return version;
    }
    
    /**
     * md5Encrypt.
     * 
     * @param dataStr
     * @return
     */
    public static String md5Encrypt(String dataStr) {
        try {
            MessageDigest m = MessageDigest.getInstance("MD5");
            m.update(dataStr.getBytes("UTF8"));
            byte s[] = m.digest();
            String result = "";
            for (int i = 0; i < s.length; i++) {
                result += Integer.toHexString((0x000000FF & s[i]) | 0xFFFFFF00).substring(6);
            }
            return result.toUpperCase();
        } catch (Exception e) {
            log.error("fail md5Encrypt", e);
        }
        return "";
    }
    
    /**
     * writerFile.
     * 
     * @param fileName
     * @param content
     */
    public static void writerFile(String fileName, String content) {
        File file = new File(CleanPathUtil.cleanString(fileName));
        FileWriter fileWritter = null;
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            fileWritter = new FileWriter(CleanPathUtil.cleanString(file.getName()), true);
            fileWritter.write(content);
        } catch (Exception e) {
            log.error("fail writerFile", e);
        } finally {
            if (fileWritter != null) {
                try {
                    fileWritter.close();
                } catch (IOException e) {
                    log.error("fail close fileWritter", e);
                }
            }
        }
    }

    private final static String TEMP_EXPORT_KEYSTORE_PATH = "exportedKey";
    private final static String PEM_FILE_FORMAT = ".pem";
    private final static String P12_FILE_FORMAT = ".p12";
    /**
     * write pem in ./tempKey
     * @param rawPrivateKey raw private key
     * @param address
     * @param userName can be empty string
     * @param cryptoSuite
     * @return
     */
    public static String writePrivateKeyPem(String rawPrivateKey, String address, String userName,
        CryptoSuite cryptoSuite) {
        File keystorePath = new File(TEMP_EXPORT_KEYSTORE_PATH);
        // delete old private key
        if (keystorePath.exists()) {
            deleteDir(keystorePath);
        }
        keystorePath.mkdir();
        // get private key
        String exportedKeyPath = TEMP_EXPORT_KEYSTORE_PATH + File.separator +
            userName + "_" + address + PEM_FILE_FORMAT;
        CryptoKeyPair cryptoKeyPair = cryptoSuite.loadKeyPair(rawPrivateKey);
        cryptoKeyPair.storeKeyPairWithPem(exportedKeyPath);
        return exportedKeyPath;
    }

    /**
     * write p12 in ./tempKey
     * @param p12Password
     * @param rawPrivateKey raw private key
     * @param address
     * @param userName can be empty string
     * @param cryptoSuite
     * @return
     */
    public static String writePrivateKeyP12(String p12Password, String rawPrivateKey,
        String address, String userName, CryptoSuite cryptoSuite) {


        File keystorePath = new File(TEMP_EXPORT_KEYSTORE_PATH);
        // delete old private key
        if (keystorePath.exists()) {
            deleteDir(keystorePath);
        }
        keystorePath.mkdir();
        // get private key
        String exportedKeyPath = TEMP_EXPORT_KEYSTORE_PATH + File.separator +
            userName + "_" + address + P12_FILE_FORMAT;
        CryptoKeyPair cryptoKeyPair = cryptoSuite.loadKeyPair(rawPrivateKey);
        cryptoKeyPair.storeKeyPairWithP12(exportedKeyPath, p12Password);

        return exportedKeyPath;
    }

    /**
     * 文件转Base64
     *
     * @param filePath 文件路径
     * @return
     */
    public static String fileToBase64(String filePath) {
        if (filePath == null) {
            return null;
        }
        FileInputStream inputFile = null;
        try {
            File file = new File(CleanPathUtil.cleanString(filePath));
            inputFile = new FileInputStream(file);
            byte[] buffer = new byte[(int) file.length()];
            int size = inputFile.read(buffer);
            log.debug("fileToBase64 inputFile size:{}", size);
            return Base64.getEncoder().encodeToString(buffer);
        } catch (IOException e) {
            log.error("base64ToFile IOException:[{}]", e.toString());
        } finally {
            if (inputFile != null) {
                try {
                    inputFile.close();
                } catch (IOException e) {
                    log.error("closeable IOException:[{}]", e.toString());
                }
            }
        }
        return null;
    }

    /**
     * delete dir or file whatever
     * @param dir
     * @return
     */
    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            if (children == null) {
                return dir.delete();
            }
            // recursive delete until dir is emtpy to delete
            for (int i=0; i<children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        // delete empty dir
        return dir.delete();
    }

    /**
     * 支持数字，字母与下划线"_"
     *
     * @param input
     * @return
     */
    public static boolean isLetterDigit(String input) {
        String regex = "^[a-z0-9A-Z_]+$";
        return input.matches(regex);
    }

    /**
     * 字母开头
     * @param input
     * @return
     */
    public static boolean startWithLetter(String input) {
        if (StringUtils.isBlank(input)) {
            return false;
        }
        if (!isLetterDigit(input)) {
            return false;
        }
        String regex = "^[a-zA-Z]+$";
        return (input.charAt(0)+"").matches(regex);
    }

    public static String loadFileContent(String filePath) {
        try(InputStream inputStream = new ClassPathResource(filePath).getInputStream()) {
            return IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            log.error("loadFileContent error:", ex);
            return "";
        }
    }
}

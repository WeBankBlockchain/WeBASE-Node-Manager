/*
 * Copyright 2014-2020 the original author or authors.
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

import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import lombok.extern.log4j.Log4j2;

/**
 * Aes加解密工具
 */
@Log4j2
public class AesUtils {

    private static final String KEY_ALGORITHM = "AES";
    private static final String DEFAULT_IV = "abcdefgh12345678";
    private static final String DEFAULT_CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";// 默认的加密算法

    /**
     * AES 加密操作
     *
     * @param content 待加密内容
     * @param password 密码
     * @return 加密数据
     */
    public static String encrypt(String content, String aesKey) {
        try {
            // 创建密码器
            Cipher cipher = Cipher.getInstance(DEFAULT_CIPHER_ALGORITHM);

            // 密码key(超过16字节即128bit的key，需要替换jre中的local_policy.jar和US_export_policy.jar，否则报错：Illegal
            // key size)
            SecretKeySpec keySpec =
                    new SecretKeySpec(aesKey.getBytes(StandardCharsets.UTF_8), KEY_ALGORITHM);

            // 向量iv
            IvParameterSpec ivParameterSpec =
                    new IvParameterSpec(DEFAULT_IV.getBytes(StandardCharsets.UTF_8));

            // 初始化为加密模式的密码器
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivParameterSpec);

            // 加密
            byte[] byteContent = content.getBytes(StandardCharsets.UTF_8);
            byte[] result = cipher.doFinal(byteContent);

            return Base64.getEncoder().encodeToString(result);
        } catch (Exception ex) {
            log.error("encrypt fail. content:{}", content, ex);
            throw new NodeMgrException(ConstantCode.REQUEST_DECRYPT_FAIL);
        }
    }

    /**
     * AES 解密操作
     *
     * @param content 密文
     * @param aesKey 密码
     * @return 明文
     */
    public static String decrypt(String content, String aesKey) {
        try {
            // 创建密码器
            Cipher cipher = Cipher.getInstance(DEFAULT_CIPHER_ALGORITHM);

            // 密码key
            SecretKeySpec keySpec =
                    new SecretKeySpec(aesKey.getBytes(StandardCharsets.UTF_8), KEY_ALGORITHM);

            // 向量iv
            IvParameterSpec ivParameterSpec =
                    new IvParameterSpec(DEFAULT_IV.getBytes(StandardCharsets.UTF_8));

            // 初始化为解密模式的密码器
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivParameterSpec);

            // 执行操作
            byte[] encrypted1 = Base64.getDecoder().decode(content);
            byte[] result = cipher.doFinal(encrypted1);

            return new String(result, StandardCharsets.UTF_8);
        } catch (Exception ex) {
            log.error("decrypt fail. content:{}", content, ex);
            throw new NodeMgrException(ConstantCode.REQUEST_ENCRYPT_FAIL);
        }
    }

    public static void main(String[] args) throws Exception {
        Map<String, String> map = new HashMap<String, String>();
        map.put("key", "value");
        map.put("中文", "汉字");
        
        String aesKey = "EfdsW23D23d3df43";
        
        String content = JsonTools.toJSONString(map);
        System.out.println("加密前：" + content);

        String encrypt = encrypt(content, aesKey);
        System.out.println("加密后：" + encrypt);

        String decrypt = decrypt(encrypt, aesKey);
        System.out.println("解密后：" + decrypt);
    }

}

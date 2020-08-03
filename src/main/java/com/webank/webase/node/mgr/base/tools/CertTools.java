/**
 * Copyright 2014-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webank.webase.node.mgr.base.tools;


import java.io.IOException;
import java.security.Principal;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.fisco.bcos.web3j.crypto.Keys;
import org.fisco.bcos.web3j.utils.Numeric;

import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;

public class CertTools {
    public static final String crtContentHead = "-----BEGIN CERTIFICATE-----\n" ;
    public static final String crtContentTail = "-----END CERTIFICATE-----\n" ;
    public static final String crtTailForConcat = "\n-----END CERTIFICATE-----\n" ;

    public static final String TYPE_CHAIN = "chain";
    public static final String TYPE_AGENCY = "agency";
    public static final String TYPE_NODE = "node";
    // 2019/12: support guomi, double cert mechanism
    public static final String TYPE_ENCRYPT_NODE = "ennode";
    public static final String TYPE_SDK_CHAIN = "sdkca";
    public static final String TYPE_SDK_AGENCY = "sdkagency";
    // cert name: sdk ,type: node or sdk
    public static final String TYPE_SDK_NODE = "sdknode";
    // pull flag: 首次启动时需要拉取Front与节点的证书
    public static boolean isPullFrontCertsDone = false;

    // public key in hex length
    public static final int PUBLIC_KEY_IN_HEX_LENGTH = 128;
    /**
     * 获取证书类型 和 名字
     * @return
     * @throws IOException
     * //standard:
     * //gm: CN=node0,O=fiscobcos,OU=agency
     */
    public static String  getCertName(Principal subjectDN) {
        return subjectDN.toString().split(",")[0].split("=")[1];
    }
    public static String  getCertType(Principal subjectDN) {
        return subjectDN.toString().split(",")[2].split("=")[1];
    }

    /**
     * 给cert的内容加上头和尾
     * begin ...
     * end ...
     */
    public static String addCertHeadAndTail(String certContent) {
        String headToConcat = crtContentHead;
        String fullCert = headToConcat.concat(certContent).concat(crtTailForConcat);
        return fullCert;
    }

    /**
     * getPublicKey
     * @param key
     * @return String
     */
    public static String getPublicKeyString(PublicKey key) {
        //        ECPublicKeyImpl pub = (ECPublicKeyImpl) key;
        BCECPublicKey bcecPublicKey = (BCECPublicKey) key;
        byte[] bcecPubBytes = bcecPublicKey.getEncoded();
        String publicKey = Numeric.toHexStringNoPrefix(bcecPubBytes);
        publicKey = publicKey.substring(publicKey.length() - PUBLIC_KEY_IN_HEX_LENGTH); //只取后128位
        return publicKey;
    }

    public static String getAddress(PublicKey key) {
        String publicKey = getPublicKeyString(key);
        return Keys.getAddress(publicKey);
    }

    // crt文件中默认首个是节点证书 0 isnode ca, 1 is agency ca, 2 is chain
    public static List<String> getCrtContentList(String certContent) {
        List<String> list = new ArrayList<>();
        if(!certContent.startsWith(crtContentHead)){
            throw new NodeMgrException(ConstantCode.CERT_FORMAT_ERROR);
        }
        String[] nodeCrtStrArray = certContent.split(crtContentHead);
        for(int i = 0; i < nodeCrtStrArray.length; i++) {
            String[] nodeCrtStrArray2 = nodeCrtStrArray[i].split(crtContentTail);
            for(int j = 0; j < nodeCrtStrArray2.length; j++) {
                String ca = nodeCrtStrArray2[j];
                if(ca.length() != 0) {
                    list.add(formatStr(ca));
                }
            }
        }
        return list;
    }

    public static String formatStr(String string) {
        return string.substring(0, string.length() - 1);
    }

    /**
     * byte数组转hex
     * @param bytes
     * @return
     */
    public static String byteToHex(byte[] bytes){
        String strHex = "";
        StringBuilder sb = new StringBuilder("");
        for (int n = 0; n < bytes.length; n++) {
            strHex = Integer.toHexString(bytes[n] & 0xFF);
            sb.append((strHex.length() == 1) ? "0" + strHex : strHex); // 每个字节由两个字符表示，位数不够，高位补0
        }
        return sb.toString().trim();
    }
}

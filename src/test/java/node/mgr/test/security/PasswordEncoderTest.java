/**
 * Copyright 2014-2020 the original author or authors.
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

package node.mgr.test.security;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import node.mgr.test.base.TestBase;
import org.apache.commons.codec.binary.Hex;
import org.fisco.bcos.sdk.v3.crypto.CryptoSuite;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordEncoderTest extends TestBase {

    @Autowired
    @Qualifier("bCryptPasswordEncoder")
    private PasswordEncoder bcPasswordEncoder;

    @Test
    public void test() {
        String rawPwd = "Abcd1234";
        String shaPwd2 = getSHA256Str(rawPwd); // sha256
        String dbPwd = "$2a$10$F/aEB1iEx/FvVh0fMn6L/uyy.PkpTy8Kd9EdbqLGo7Bw7eCivpq.m";
        boolean res2 = bcPasswordEncoder.matches(shaPwd2, dbPwd);
        System.out.println("res2-" + res2);
    }

    public static String getSHA256Str(String str){
        MessageDigest messageDigest;
        String encdeStr = "";
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] hash = messageDigest.digest(str.getBytes("UTF-8"));
            encdeStr = Hex.encodeHexString(hash);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return encdeStr;
    }
}

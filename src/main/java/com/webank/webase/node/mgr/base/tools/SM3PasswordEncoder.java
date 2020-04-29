/**
 * Copyright 2014-2019 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.webase.node.mgr.base.tools;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.fisco.bcos.web3j.crypto.gm.sm3.SM3Digest;
import org.fisco.bcos.web3j.utils.Numeric;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * PasswordEncoder using guomi sm3 encrypt
 */
public class SM3PasswordEncoder implements PasswordEncoder {
    private final Log logger = LogFactory.getLog(getClass());

    public SM3PasswordEncoder() {}

    @Override
    public String encode(CharSequence rawPassword) {
        SM3Digest sm3Digest = new SM3Digest();
        byte[] pwdInput = rawPassword.toString().getBytes();
        byte[] hashed = sm3Digest.hash(pwdInput);
        return Numeric.toHexString(hashed);
    }

    /**
     *
     * @param rawPassword pwd (not decoded) to be checked
     * @param encodedPassword
     * @return
     */
    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword){
        if (encodedPassword == null || encodedPassword.length() == 0) {
            logger.warn("Empty encoded password");
            return false;
        }
        String plainText = encode(rawPassword);
        return checkPwd(plainText, encodedPassword);
    }

    private boolean checkPwd(String plainText, String hashed) {
        char[] caa = plainText.toCharArray();
        char[] cab = hashed.toCharArray();

        if (caa.length != cab.length) {
            return false;
        }

        byte ret = 0;
        for (int i = 0; i < caa.length; i++) {
            ret |= caa[i] ^ cab[i];
        }
        return ret == 0;
    }

}

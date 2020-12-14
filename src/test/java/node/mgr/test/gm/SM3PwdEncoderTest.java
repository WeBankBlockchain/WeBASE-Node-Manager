/**
 * Copyright 2014-2020 the original author or authors.
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

package node.mgr.test.gm;

import org.fisco.bcos.web3j.crypto.Hash;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.password.PasswordEncoder;

public class SM3PwdEncoderTest extends TestBase {

    @Qualifier("sm3PasswordEncoder")
    @Autowired
    PasswordEncoder passwordEncoder;

    @Qualifier("bCryptPasswordEncoder")
    @Autowired
    PasswordEncoder bcEncoder;
    public static final String pwdSM3Of123 = "0x02a05d18fc27f898c4e5815ad7696891772303dc47d5390d0dd71edc07df39ac";

    @Test
    public void testCrypt() {
        String rawPWd = "123";
        String pwdSM3  = passwordEncoder.encode(rawPWd);
        System.out.println(pwdSM3);
        Assert.assertTrue(passwordEncoder.matches(rawPWd, pwdSM3Of123));
    }

    @Test
    public void testPwdMatches() {
        String raw = passwordEncoder.encode("123");
        boolean res = passwordEncoder.matches(raw, pwdSM3Of123);
        System.out.println(res);
        System.out.println(Hash.sha3("123"));

    }
}

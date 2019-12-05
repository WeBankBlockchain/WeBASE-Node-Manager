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

package node.mgr.test.gm;

import org.fisco.bcos.web3j.crypto.Hash;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.validation.constraints.AssertTrue;

public class SM3PwdEncoderTest extends TestBase {

    @Qualifier("sm3PasswordEncoder")
    @Autowired
    PasswordEncoder passwordEncoder;

    @Qualifier("bCryptPasswordEncoder")
    @Autowired
    PasswordEncoder bcEncoder;

    @Test
    public void testCrypt() {
        String rawPWd = "password4";
        // password4: 0xa9c64b024e038c7f7d11474e92446b0796ac2cd839d7e25c5defe1f41c52cfea
        String pwdSM3  = passwordEncoder.encode(rawPWd);
//        String pwdBC = bcEncoder.encode(rawPWd);
        System.out.println(pwdSM3);
//        System.out.println(pwdBC);
        // pwdSM3 = "0xfa0935a1bbef35be054f266d2d0fa48571c6254e312d65df085b2f7117c0c148";
        // pwdBC = "$2a$10$xl4/AgmzZe1LYo9zABpUZuHpmUp.4a7NO58osbATMsr8FyOHLsKfe";
    }

    public static final String pwdSM3 = "0xfa0935a1bbef35be054f266d2d0fa48571c6254e312d65df085b2f7117c0c148";

    @Test
    public void testPwdMatches() {
        String raw = passwordEncoder.encode("123");
        boolean res = passwordEncoder.matches(raw, pwdSM3);
        System.out.println(res);
        System.out.println(Hash.sha3("123"));

    }
}

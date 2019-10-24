/**
 * Copyright 2014-2019 the original author or authors.
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
package node.mgr.test.precompiled;

import org.bouncycastle.crypto.digests.KeccakDigest;
import org.bouncycastle.jcajce.provider.digest.Keccak;
import org.junit.Test;

public class testSol2Abi {

    // according to web3sdk
    @Test
    public void testMethodId()throws Exception {
        String methodName = "setValueByKey(string,string)";
        String result;
//        result = Keccak.Digest256(methodName.getBytes());

    }
}

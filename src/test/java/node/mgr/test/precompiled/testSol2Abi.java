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

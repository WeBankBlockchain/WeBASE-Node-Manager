/**
 * Copyright 2014-2021 the original author or authors.
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

package node.mgr.test.user;


import java.math.BigInteger;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.utils.ByteUtils;
import org.fisco.bcos.sdk.utils.Numeric;
import org.junit.Test;

public class PublicKeyTest {


    @Test
    public void testPub() {
        CryptoSuite cryptoSuite = new CryptoSuite(0);
        CryptoKeyPair cryptoKeyPair = cryptoSuite.getKeyPairFactory().generateKeyPair();
        String privateKey = cryptoKeyPair.getHexPrivateKey();
        String publicKey = cryptoKeyPair.getHexPublicKey();
        String address = cryptoKeyPair.getAddress();
        System.out.println("privateKey:" + privateKey);
        System.out.println("publicKey:" + publicKey);
        System.out.println("address:" + address);

        String weBasePublicKeyWithoutPrefix = publicKey.substring(2);
        System.out.println("weBasePublicKeyWithoutPrefix:" + weBasePublicKeyWithoutPrefix);
        // Step 2 : convert hex string to big integer
        byte[] byteArray = convertHexStringToByteArray(weBasePublicKeyWithoutPrefix);
        System.out.println("byteArray length:" + byteArray.length);
        BigInteger bigInteger = ByteUtils.bytesToBigInteger(byteArray);
        System.out.println("bigInteger:" + bigInteger.toString(10));
        String hexPub = bigInteger.toString(16);
        System.out.println("hexPub:" + hexPub);
        byte[] bytesArrayBitInt = Numeric.hexStringToByteArray(hexPub);
        System.out.println("bytesArrayBitInt length:" + bytesArrayBitInt.length);

    }

    public static String convertWeBasePublicKeyToWeIdPublicKey(String weBasePublicKey) {
        // Step 1 : remove prefix
        String weBasePublicKeyWithoutPrefix = weBasePublicKey.substring(2);
        // Step 2 : convert hex string to big integer
        byte[] byteArray = convertHexStringToByteArray(weBasePublicKeyWithoutPrefix);
        BigInteger bigInteger = ByteUtils.bytesToBigInteger(byteArray);
//        BigInteger t = new BigInteger(byteArray);
//        BigInteger t2 = new BigInteger(1, byteArray);
        // Step 3 : base64 encode
        String encodedWeBasePublicKeyWithoutPrefix = org.apache.commons.codec.binary.Base64.encodeBase64String(Numeric.
            hexStringToByteArray(bigInteger.toString(16)));

        return encodedWeBasePublicKeyWithoutPrefix;
    }

    public static byte[] convertHexStringToByteArray(String hexStr) {
        return Numeric.hexStringToByteArray(hexStr);
    }



}

/**
 * Copyright 2014-2021  the original author or authors.
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
package com.webank.webase.node.mgr.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.method.entity.Method;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.sdk.abi.tools.ContractAbiUtil;
import org.fisco.bcos.sdk.abi.wrapper.ABIDefinition;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.fisco.bcos.sdk.utils.Numeric;
import org.fisco.bcos.sdk.utils.ObjectMapperFactory;

/**
 * web3sdk common methods of abi/key etc.
 */
public class Web3Tools {

    static final int PUBLIC_KEY_SIZE = 64;

    public static final int ADDRESS_SIZE = 160;
    public static final int ADDRESS_LENGTH_IN_HEX = ADDRESS_SIZE >> 2;

    static final int PUBLIC_KEY_LENGTH_IN_HEX = PUBLIC_KEY_SIZE << 1;

    /*   public static SignatureData stringToSignatureData(String signatureData) {
        byte[] byte_3 = Numeric.hexStringToByteArray(signatureData);
        byte[] signR = new byte[32];
        System.arraycopy(byte_3, 1, signR, 0, signR.length);
        byte[] signS = new byte[32];
        System.arraycopy(byte_3, 1 + signR.length, signS, 0, signS.length);
        return new SignatureData(byte_3[0], signR, signS);
    }

    public static String signatureDataToString(SignatureData signatureData) {
        byte[] byte_3 = new byte[1 + signatureData.getR().length + signatureData.getS().length];
        byte_3[0] = signatureData.getV();
        System.arraycopy(signatureData.getR(), 0, byte_3, 1, signatureData.getR().length);
        System.arraycopy(signatureData.getS(), 0, byte_3, signatureData.getR().length + 1,
            signatureData.getS().length);
        return Numeric.toHexString(byte_3, 0, byte_3.length, false);
    }*/

    /**
     * get address from public key
     * 2019/11/27 support guomi
     * @param publicKey
     * @return
     */
    public static String getAddressByPublicKey(String publicKey, CryptoSuite cryptoSuite) {
        return cryptoSuite.getCryptoKeyPair().getAddress(publicKey);
//        String address = "0x" + Keys.getAddress(publicKey);
    }
    
    /**
     * get method from abi
     */
    public static List<Method> getMethodFromAbi(String abi, CryptoSuite cryptoSuite) throws IOException {
        List<ABIDefinition> abiList = loadContractDefinition(abi);
        List<Method> methodList = new ArrayList<>();
        for (ABIDefinition abiDefinition : abiList) {
            Method method = new Method();
            method.setMethodType(abiDefinition.getType());
            method.setAbiInfo(JsonTools.toJSONString(abiDefinition));
            method.setMethodId(buildMethodId(abiDefinition, cryptoSuite));
            methodList.add(method);
        }
        return methodList;
    }

    /**
     * abi string to ABIDefinition.
     */
    public static List<ABIDefinition> loadContractDefinition(String abi) throws IOException {
        ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
        ABIDefinition[] abiDefinition = objectMapper.readValue(abi, ABIDefinition[].class);
        return Arrays.asList(abiDefinition);
    }

    /**
     * get methodId after hash
     */
    public static String buildMethodId(ABIDefinition abiDefinition, CryptoSuite cryptoSuite) {
        byte[] inputs = getMethodIdBytes(abiDefinition);
        // 2019/11/27 support guomi
        byte[] hash = cryptoSuite.hash(inputs);
        if ("function".equals(abiDefinition.getType())) {
            return Numeric.toHexString(hash).substring(0, 10);
        }
        return Numeric.toHexString(hash);
    }

    /**
     * get methodId bytes from ABIDefinition
     * @return byte[]
     */
    public static byte[] getMethodIdBytes(ABIDefinition abiDefinition) {
        StringBuilder result = new StringBuilder();
        result.append(abiDefinition.getName());
        result.append("(");
        String params = abiDefinition.getInputs().stream()
                .map(ABIDefinition.NamedType::getType)
                .collect(Collectors.joining(","));
        result.append(params);
        result.append(")");

        byte[] inputs = result.toString().getBytes();
        return inputs;
    }

    /**
     * get ABIDefinition by Function name
     * @param funName
     * @param contractAbi
     * @return
     */
    public static ABIDefinition getAbiDefinition(String funName, String contractAbi) {
        if (StringUtils.isBlank(contractAbi)) {
            throw new NodeMgrException(ConstantCode.CONTRACT_ABI_EMPTY);
        }
        List<ABIDefinition> abiList = JsonTools.toJavaObjectList(contractAbi, ABIDefinition.class);
        if (abiList == null) {
            throw new NodeMgrException(ConstantCode.FAIL_PARSE_JSON);
        }
        ABIDefinition result = null;
        for (ABIDefinition abiDefinition : abiList) {
            if (ContractAbiUtil.TYPE_FUNCTION.equals(abiDefinition.getType())
                    && funName.equals(abiDefinition.getName())) {
                result = abiDefinition;
                break;
            }
        }
        return result;
    }
}

/**
 * Copyright 2014-2019  the original author or authors.
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



import com.fasterxml.jackson.databind.ObjectMapper;
import org.fisco.bcos.web3j.crypto.Hash;
import org.fisco.bcos.web3j.protocol.ObjectMapperFactory;
import org.fisco.bcos.web3j.protocol.core.methods.response.AbiDefinition;
import org.fisco.bcos.web3j.utils.Numeric;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.fisco.bcos.web3j.crypto.Keys.getAddress;

public class Web3Tools {

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

    public static String getAddressByPublicKey(String publicKey) {
        String address = "0x" + getAddress(publicKey);
        return address;
    }

    /**
     * abi string to AbiDefinition.
     */
    public static List<AbiDefinition> loadContractDefinition(String abi) throws IOException {
        ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
        AbiDefinition[] abiDefinition = objectMapper.readValue(abi, AbiDefinition[].class);
        return Arrays.asList(abiDefinition);
    }

    /**
     * get methodId.
     */
    public static String buildMethodId(AbiDefinition abiDefinition) {
        StringBuilder result = new StringBuilder();
        result.append(abiDefinition.getName());
        result.append("(");
        String params = abiDefinition.getInputs().stream()
            .map(AbiDefinition.NamedType::getType)
            .collect(Collectors.joining(","));
        result.append(params);
        result.append(")");

        byte[] inputs = result.toString().getBytes();
        byte[] hash = Hash.sha3(inputs);
        return Numeric.toHexString(hash).substring(0, 10);
    }
}

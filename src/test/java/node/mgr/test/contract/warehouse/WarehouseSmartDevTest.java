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

package node.mgr.test.contract.warehouse;

import com.webank.scaffold.util.IOUtil;
import com.webank.webase.node.mgr.base.tools.NodeMgrTools;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.Objects;
import org.junit.Test;

public class WarehouseSmartDevTest {

    private final static String readDir = "contracts";
    /**
     * get all sol source code in contracts dir
     */
    @Test
    public void testGetSolSrcBase64() throws IOException {
        File contractDir = new File(readDir);
        this.printFileContent(contractDir);
    }

    private void printFileContent(File file) throws IOException {
        for (File subFile : Objects.requireNonNull(file.listFiles())) {
            if (subFile.isDirectory()) {
                System.out.println("subDir name: " + subFile.getName());
                if(!"base64".equals(subFile.getName())) {
                    printFileContent(subFile);
                }
            } else {
                System.out.println("subFile name: " + subFile.getName());
                String content = IOUtil.readAsString(subFile);
//                System.out.println(solFile.getName() + " content: " + content);
                System.out.println(subFile.getName() + " content base64: " + NodeMgrTools
                    .encodedBase64Str(content));
                IOUtil.writeStringToFile(NodeMgrTools.encodedBase64Str(content),
                    new File(readDir + "/base64"),
                    subFile.getName() + ".txt");
            }
        }
    }

    public final static String HELLO_WORLD = "# HelloWorld\n"
        + "\n"
        + "HelloWorld Contract\n";

    public final static String CRUD = "# Table\n"
        + "\n"
        + "Table contract for CRUD, [CRUD API](https://fisco-bcos-documentation.readthedocs.io/zh_CN/latest/docs/articles/3_features/33_storage/crud_guidance.html)\n";
    public final static String CRUD_TEST = "# TableTest\n"
        + "\n"
        + "Table contract for CRUD \n"
        + "\n"
        + "CRUD Test, [CRUD API](https://fisco-bcos-documentation.readthedocs.io/zh_CN/latest/docs/articles/3_features/33_storage/crud_guidance.html)\n";
    public final static String KVTABLE_TEST = "# KVTableTest\n"
        + "\n"
        + "KVTable contract for CRUD \n"
        + "\n"
        + "CRUD Test, [CRUD API](https://fisco-bcos-documentation.readthedocs.io/zh_CN/latest/docs/articles/3_features/33_storage/crud_guidance.html)\n";
    public final static String CRYPTO = "# Crypto\n"
        + "\n"
        + "Crypto Interface Contract";
    public final static String CRYPTO_SHA_TEST = "# ShaTest\n"
        + "\n"
        + "Crypto Interface Contract\n"
        + "\n"
        + "Crypto Test Contract [Crypto API](https://toolkit-doc.readthedocs.io/zh_CN/latest/docs/WeBankBlockchain-SmartDev-Contract/api/default/Crypto.html)\n";

    @Test
    public void testGetMdBase64() {
        System.out.println("HELLO_WORLD:\n" + Base64.getEncoder().encodeToString(HELLO_WORLD.getBytes()));
        System.out.println("CRUD:\n" + Base64.getEncoder().encodeToString(CRUD.getBytes()));
        System.out.println("CRUD_TEST:\n" + Base64.getEncoder().encodeToString(CRUD_TEST.getBytes()));
        System.out.println("KVTABLE_TEST:\n" + Base64.getEncoder().encodeToString(KVTABLE_TEST.getBytes()));
        System.out.println("CRYPTO:\n" + Base64.getEncoder().encodeToString(CRYPTO.getBytes()));
        System.out.println("CRYPTO_SHA_TEST:\n" + Base64.getEncoder().encodeToString(CRYPTO_SHA_TEST.getBytes()));
    }

}
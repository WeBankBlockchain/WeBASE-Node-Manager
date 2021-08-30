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

import com.webank.scaffold.util.FileUtils;
import com.webank.scaffold.util.IOUtil;
import com.webank.webase.node.mgr.tools.NodeMgrTools;
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
                FileUtils.writeStringToFile(NodeMgrTools.encodedBase64Str(content),
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

    public final static String EVIDENCE_API = "# Evidence存证\n"
        + "\n"
        + "## 简介\n"
        + "\n"
        + "存证操作，上传、审批、修改、删除等，详情查看[Smart-Dev Evidence Doc](https://toolkit-doc.readthedocs.io/zh_CN/latest/docs/WeBankBlockchain-SmartDev-Contract/api/business_template/Evidence.html)\n"
        + "\n"
        + "合约：\n"
        + "1) EvidenceController 对外服务的唯一接口\n"
        + "2) EvidenceRepository 辅助合约，用于数据和逻辑分离\n"
        + "3) RequestRepository 辅助合约，用于数据和逻辑分离\n"
        + "4) Authentication 辅助合约，用于数据和逻辑分离";

    public final static String TRACE_API_MD = "# 溯源\n"
        + "\n"
        + "## 简介\n"
        + "\n"
        + "包含创建Traceability溯源类目、创建Goods溯源商品、更新溯源/商品状态、获取溯源/商品信息等\n"
        + "\n"
        + "合约：\n"
        + "1) Goods 溯源商品\n"
        + "2) Traceability 商品溯源类目\n"
        + "3) TraceabilityFactory 溯源工厂类\n";

    @Test
    public void testGetMdBase64() {
//        System.out.println("HELLO_WORLD:\n" + Base64.getEncoder().encodeToString(HELLO_WORLD.getBytes()));
//        System.out.println("CRUD:\n" + Base64.getEncoder().encodeToString(CRUD.getBytes()));
//        System.out.println("CRUD_TEST:\n" + Base64.getEncoder().encodeToString(CRUD_TEST.getBytes()));
//        System.out.println("KVTABLE_TEST:\n" + Base64.getEncoder().encodeToString(KVTABLE_TEST.getBytes()));
//        System.out.println("CRYPTO:\n" + Base64.getEncoder().encodeToString(CRYPTO.getBytes()));
//        System.out.println("CRYPTO_SHA_TEST:\n" + Base64.getEncoder().encodeToString(CRYPTO_SHA_TEST.getBytes()));
//        System.out.println("EVIDENCE_API:\n" + Base64.getEncoder().encodeToString(EVIDENCE_API.getBytes()));
        System.out.println("TRACE_API_MD:\n" + Base64.getEncoder().encodeToString(TRACE_API_MD.getBytes()));
    }

}
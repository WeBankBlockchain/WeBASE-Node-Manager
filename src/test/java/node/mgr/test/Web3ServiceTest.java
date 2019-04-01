/*
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
package node.mgr.test;

import com.alibaba.fastjson.JSON;
import java.math.BigInteger;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class Web3ServiceTest {

    @Autowired
    private Web3Service web3Service;
    private Integer groupId = 300001;
    private Integer userId = 700001;
    private BigInteger blockNumber = new BigInteger("12");
    private String transHash = "0x1d99fdfa84b90d9478f09b722bb85b7d804e6b4d0273ec94fa4418c56a415211";
    private String blockHash = "0x337eb77084c0e6b09c508cc0f7dbc125af459c2aab19b9ca43c731ffc9fe604f";

    @Test
    public void getContractCodeTest() {
        String contractAddress = "0xb68b0ca60cc4d8b207875c9a0ab6c3a782db9318";
        String str = web3Service.getContractCode(groupId, contractAddress, blockNumber);
        assert (str != null);
        System.out.println(str);
    }

    @Test
    public void getTransReceiptTest() {
        TransReceipt transReceipt = web3Service.getTransReceipt(groupId, transHash);
        assert (transReceipt != null);
        System.out.println(JSON.toJSONString(transReceipt));
    }

    @Test
    public void getTransactionTest() {
        TransactionInfo transactionInfo = web3Service.getTransaction(groupId, transHash);
        assert (transactionInfo != null);
        System.out.println(JSON.toJSONString(transactionInfo));
    }

    @Test
    public void getBlockByNumberTest() {
        BlockInfo blockInfo = web3Service.getBlockByNumber(groupId, blockNumber);
        assert (blockInfo != null);
        System.out.println(JSON.toJSONString(blockInfo));
    }

    @Test
    public void getblockFromFrontByHashTest() {
        BlockInfo blockInfo = web3Service.getblockFromFrontByHash(groupId, blockHash);
        assert (blockInfo != null);
        System.out.println(JSON.toJSONString(blockInfo));
    }

    @Test
    public void getTransFromFrontByHashTest() {
        ChainTransInfo chainTransInfo = web3Service
            .getTransInfoFromFrontByHash(groupId, transHash);
        assert (chainTransInfo != null);
        System.out.println(JSON.toJSONString(chainTransInfo));
    }

    @Test
    public void getAddressFromFrontByHashTest() {
        String contractAddress = web3Service.getAddressFromFrontByHash(groupId, transHash);
        assert (contractAddress != null);
        System.out.println(contractAddress);
    }

    @Test
    public void getCodeFromFronthTest() {
        String contractAddress = "0xb68b0ca60cc4d8b207875c9a0ab6c3a782db9318";
        String code = web3Service.getCodeFromFront(groupId, contractAddress, blockNumber);
        assert (code != null);
        System.out.println(code);
    }


    //TODO
    @Test
    public void getTotalTransactionCountTest() {
        TotalTransCountInfo totalTransCount = web3Service.getTotalTransactionCount(groupId);
        assert (totalTransCount != null);
        System.out.println(JSON.toJSONString(totalTransCount));
    }

    @Test
    public void getTransByBlockNumberTest() {
        List<TransactionInfo>  list = web3Service.getTransByBlockNumber( groupId,  blockNumber);
        assert (list != null && list.size() > 0);
        System.out.println(JSON.toJSONString(list));
    }

    @Test
    public void getGroupPeersTest() {
        List<String>  list = web3Service.getGroupPeers( groupId);
        assert (list != null && list.size() > 0);
        System.out.println(JSON.toJSONString(list));
    }


    @Test
    public void getGroupListTest() {
        List<String>  list = web3Service.getGroupList( groupId);
        assert (list != null && list.size() > 0);
        System.out.println(JSON.toJSONString(list));
    }

    @Test
    public void getPeersTest() {
        List<String>  list = web3Service.getPeers( groupId);
        assert (list != null && list.size() > 0);
        System.out.println(JSON.toJSONString(list));
    }

    @Test
    public void getConsensusStatusTest() {
        String consensunsStatus = web3Service.getConsensusStatus( groupId);
        assert (consensunsStatus != null);
        System.out.println(consensunsStatus);
    }

    @Test
    public void syncStatusTest() {
        String status = web3Service.syncStatus(groupId);
        assert (status != null);
        System.out.println(status);
    }

    @Test
    public void getSystemConfigByKeyTest() {
        //tx_count_limit和tx_gas_limit
        String key = "tx_count_limit";
        String config = web3Service.getSystemConfigByKey( groupId,key);
        assert (config != null);
        System.out.println(config);
    }
}
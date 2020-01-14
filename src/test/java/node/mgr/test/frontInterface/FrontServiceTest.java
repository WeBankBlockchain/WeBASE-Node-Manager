/**
 * Copyright 2014-2019  the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package node.mgr.test.frontInterface;

import com.alibaba.fastjson.JSON;
import com.webank.webase.node.mgr.Application;
import com.webank.webase.node.mgr.block.entity.BlockInfo;
import com.webank.webase.node.mgr.front.entity.TotalTransCountInfo;
import com.webank.webase.node.mgr.frontinterface.FrontInterfaceService;
import com.webank.webase.node.mgr.frontinterface.entity.SyncStatus;
import com.webank.webase.node.mgr.monitor.ChainTransInfo;
import com.webank.webase.node.mgr.node.entity.PeerInfo;
import com.webank.webase.node.mgr.transaction.entity.TransReceipt;
import com.webank.webase.node.mgr.transaction.entity.TransactionInfo;
import java.math.BigInteger;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.web3j.protocol.core.methods.response.NodeVersion;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class FrontServiceTest {

    @Autowired
    private FrontInterfaceService frontInterface;
    private Integer groupId = 1;
    private Integer userId = 700001;
    private BigInteger blockNumber = new BigInteger("12");
    private String transHash = "0x1d99fdfa84b90d9478f09b722bb85b7d804e6b4d0273ec94fa4418c56a415211";
    private String blockHash = "0x337eb77084c0e6b09c508cc0f7dbc125af459c2aab19b9ca43c731ffc9fe604f";
    private String frontIp = "localhost";
    private Integer frontPort = 5002;

    @Test
    public void getContractCodeTest() {
        String contractAddress = "0xb68b0ca60cc4d8b207875c9a0ab6c3a782db9318";
        String str = frontInterface.getContractCode(groupId, contractAddress, blockNumber);
        assert (str != null);
        System.out.println(str);
    }

    @Test
    public void getTransReceiptTest() {
        TransReceipt transReceipt = frontInterface.getTransReceipt(groupId, transHash);
        assert (transReceipt != null);
        System.out.println(JSON.toJSONString(transReceipt));
    }

    @Test
    public void getTransactionTest() {
        TransactionInfo transactionInfo = frontInterface.getTransaction(groupId, transHash);
        assert (transactionInfo != null);
        System.out.println(JSON.toJSONString(transactionInfo));
    }

    @Test
    public void getBlockByNumberTest() {
        BlockInfo blockInfo = frontInterface.getBlockByNumber(groupId, blockNumber);
        assert (blockInfo != null);
        System.out.println(JSON.toJSONString(blockInfo));
    }

    @Test
    public void getblockFromFrontByHashTest() {
        BlockInfo blockInfo = frontInterface.getblockByHash(groupId, blockHash);
        assert (blockInfo != null);
        System.out.println(JSON.toJSONString(blockInfo));
    }

    @Test
    public void getTransFromFrontByHashTest() {
        ChainTransInfo chainTransInfo = frontInterface
            .getTransInfoByHash(groupId, transHash);
        assert (chainTransInfo != null);
        System.out.println(JSON.toJSONString(chainTransInfo));
    }

    @Test
    public void getAddressFromFrontByHashTest() {
        String contractAddress = frontInterface.getAddressByHash(groupId, transHash);
        assert (contractAddress != null);
        System.out.println(contractAddress);
    }

    @Test
    public void getCodeFromFronthTest() {
        String contractAddress = "0xb68b0ca60cc4d8b207875c9a0ab6c3a782db9318";
        String code = frontInterface.getCodeFromFront(groupId, contractAddress, blockNumber);
        assert (code != null);
        System.out.println(code);
    }



    @Test
    public void getTotalTransactionCountTest() {
        TotalTransCountInfo totalTransCount = frontInterface.getTotalTransactionCount(groupId);
        assert (totalTransCount != null);
        System.out.println(JSON.toJSONString(totalTransCount));
    }

    @Test
    public void getTransByBlockNumberTest() {
        List<TransactionInfo> list = frontInterface.getTransByBlockNumber(groupId, blockNumber);
        assert (list != null && list.size() > 0);
        System.out.println(JSON.toJSONString(list));
    }

    @Test
    public void getGroupPeersTest() {
        List<String> list = frontInterface.getGroupPeers(groupId);
        assert (list != null && list.size() > 0);
        System.out.println(JSON.toJSONString(list));
    }


    @Test
    public void getGroupListTest() {
        List<String> list = frontInterface.getGroupListFromSpecificFront(frontIp, frontPort);
        assert (list != null && list.size() > 0);
        System.out.println("=====================list:" + JSON.toJSONString(list));
    }

    @Test
    public void getPeersTest() {
        PeerInfo[] list = frontInterface.getPeers(groupId);
        assert (list != null && list.length > 0);
        System.out.println("=====================list:" + JSON.toJSONString(list));
    }

    @Test
    public void getConsensusStatusTest() {
        String consensunsStatus = frontInterface.getConsensusStatus(groupId);
        assert (consensunsStatus != null);
        System.out.println("=====================consensunsStatus:" + consensunsStatus);
    }

    @Test
    public void syncStatusTest() {
        SyncStatus status = frontInterface.getSyncStatus(groupId);
        assert (status != null);
        System.out.println("=====================status:" + JSON.toJSONString(status));
    }

    @Test
    public void getSystemConfigByKeyTest() {
        //tx_count_limit和tx_gas_limit
        String key = "tx_count_limit";
        String config = frontInterface.getSystemConfigByKey(groupId, key);
        assert (config != null);
        System.out.println(config);
    }

    @Test
    public void getClientVersion() {
        String clientVersion = frontInterface.getClientVersion(frontIp, frontPort, groupId);
        System.out.println(clientVersion);
        assert (StringUtils.isNotEmpty(clientVersion));
    }

    @Test
    public void refreshFrontTest() {
        frontInterface.refreshFront(frontIp, frontPort);
    }
}
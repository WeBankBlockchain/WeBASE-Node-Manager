package node.mgr.test.transaction;

import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.webank.webase.node.mgr.Application;
import com.webank.webase.node.mgr.base.tools.JsonTools;
import com.webank.webase.node.mgr.transaction.TransHashMapper;
import com.webank.webase.node.mgr.transaction.TransHashService;
import com.webank.webase.node.mgr.transaction.entity.TbTransHash;
import com.webank.webase.node.mgr.transaction.entity.TransListParam;

/**
 * Copyright 2014-2020  the original author or authors.
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
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class TransHashServiceTest {
    @Autowired
    private TransHashService transHashService;
    private Integer groupId = 300001;
    @Autowired
    private TransHashMapper transHashMapper;

    @Test
    public void getTransListFromChain() {
        String transHash = "";
        BigInteger blockNumber = new BigInteger("12");
        List<TbTransHash> trans = transHashService.getTransListFromChain(groupId, transHash, blockNumber);
        assert (trans != null);
        System.out.println(JsonTools.toJSONString(trans));
    }

    /**
     * optimize getCount's time: use tx_id to record count
     */
    @Test
    public void getCountTimeCost() {
        TransListParam param = new TransListParam();
        Instant start = Instant.now();
        System.out.println("start: " + start.toEpochMilli());
        Integer count = transHashMapper.getCount("tb_trans_hash_1", param);
        System.out.println(count);
        System.out.println(Duration.between(start, Instant.now()).toMillis());
    }

    @Test
    public void queryCountOfTranByMinus() {
        int count = transHashService.queryCountOfTranByMinus(1);
        System.out.println(count);
    }
}

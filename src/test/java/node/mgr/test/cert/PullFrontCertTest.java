/**
 * Copyright 2014-2019 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package node.mgr.test.cert;

import com.webank.webase.node.mgr.Application;
import com.webank.webase.node.mgr.cert.CertService;
import com.webank.webase.node.mgr.cert.entity.TbCert;
import io.jsonwebtoken.lang.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class PullFrontCertTest {
    @Autowired
    CertService certService;

    @Test
    public void testPullFront() {
        certService.pullFrontNodeCrt();
        List<TbCert> list = certService.getAllCertsListFromDB();
        Assert.notNull(list, "pull certs failed. ");
        for(TbCert cert: list) {
            System.out.println(cert);
        }
    }
}

package node.mgr.test.token;

import com.alibaba.fastjson.JSON;
import com.webank.webase.node.mgr.Application;
import com.webank.webase.node.mgr.token.TokenService;
import com.webank.webase.node.mgr.transaction.TransHashService;
import com.webank.webase.node.mgr.transaction.entity.TbTransHash;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigInteger;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class TokenTest {
    @Autowired
    private TokenService tokenService;


    @Test
    public void createToken() {
        String transHash = tokenService.createToken("111",1);
        System.out.println(transHash);
        String transHash1 = tokenService.createToken("111",1);
        System.out.println(transHash1);
    }
}

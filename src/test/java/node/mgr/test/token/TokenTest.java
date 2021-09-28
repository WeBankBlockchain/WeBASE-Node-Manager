package node.mgr.test.token;

import com.webank.webase.node.mgr.account.token.TokenService;
import node.mgr.test.base.TestBase;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class TokenTest extends TestBase {
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

package com.webank.webase.node.mgr.token;

import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.base.properties.ConstantProperties;
import com.webank.webase.node.mgr.base.tools.AesTools;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.core.util.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * token service.
 */
@Log4j2
@Service
public class TokenService {
    @Autowired
    private ConstantProperties properties;
    @Autowired
    private AesTools aesTools;
    @Autowired
    private TokenMapper tokenMapper;


    /**
     * create token.
     */
    public String createToken(String value) {
        if (StringUtils.isBlank(value)) {
            log.error("fail createToken. param is null");
            return null;
        }
        String token = aesTools.aesEncrypt(UUID.randomUUID() + value);
        //save token
        TbToken tbToken = new TbToken();
        tbToken.setToken(token);
        tbToken.setValue(value);
        tbToken.setExpireTime(LocalDateTime.now().plusSeconds(properties.getAuthTokenMaxAge()));
        tokenMapper.add(tbToken);
        return token;
    }

    /**
     * get value from token.
     */
    public String getValueFromToken(String token) {
        Assert.requireNonEmpty(token, "token is empty");

        //query by token
        TbToken tbToken = tokenMapper.query(token);
        if (Objects.isNull(tbToken)) {
            log.info("fail getValueFromToken. tbToken is null");
            throw new NodeMgrException(ConstantCode.INVALID_TOKEN);
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(tbToken.getExpireTime())) {
            log.info("fail getValueFromToken. token has expire at:{}", tbToken.getExpireTime());
            //delete token
            this.deleteToken(token);
            throw new NodeMgrException(ConstantCode.TOKEN_EXPIRE);
        }
        return tbToken.getValue();
    }

    /**
     * update token expire time.
     */
    public void updateExpireTime(String token) {
        Assert.requireNonEmpty(token, "token is empty");
        tokenMapper.update(token, LocalDateTime.now().plusSeconds(properties.getAuthTokenMaxAge()));
    }

    /**
     * delete token.
     */
    public void deleteToken(String token) {
        Assert.requireNonEmpty(token, "token is empty");
        tokenMapper.delete(token);
    }
}
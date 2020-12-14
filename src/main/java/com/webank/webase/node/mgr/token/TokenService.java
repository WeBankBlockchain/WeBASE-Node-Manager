/**
 * Copyright 2014-2020 the original author or authors.
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
package com.webank.webase.node.mgr.token;

import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.enums.TokenType;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.base.properties.ConstantProperties;
import com.webank.webase.node.mgr.base.tools.NodeMgrTools;
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
    private TokenMapper tokenMapper;


    /**
     * create token.
     */
    public String createToken(String value, int type) {
        if (StringUtils.isBlank(value)) {
            log.error("fail createToken. param is null");
            return null;
        }
        // support guomi
        String token = NodeMgrTools.shaEncode(UUID.randomUUID() + value);
        //save token
        TbToken tbToken = new TbToken();
        tbToken.setToken(token);
        tbToken.setValue(value);
        if (type == TokenType.USER.getValue()) {
            tbToken.setExpireTime(LocalDateTime.now().plusSeconds(properties.getAuthTokenMaxAge()));
        } else if (type == TokenType.VERIFICATIONCODE.getValue()) {
            tbToken.setExpireTime(LocalDateTime.now().plusSeconds(properties.getVerificationCodeMaxAge()));
        } else {
            log.error("fail createToken. type:{} not support", type);
            return null;
        }
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
            log.warn("fail getValueFromToken. tbToken is null");
            throw new NodeMgrException(ConstantCode.INVALID_TOKEN);
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(tbToken.getExpireTime())) {
            log.warn("fail getValueFromToken. token has expire at:{}", tbToken.getExpireTime());
            //delete token
            this.deleteToken(token, null);
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
    public void deleteToken(String token, String value) {
        tokenMapper.delete(token, value);
    }
}
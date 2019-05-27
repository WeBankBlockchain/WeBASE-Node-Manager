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
package com.webank.webase.node.mgr.base.tools;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;

/**
 * about jwt.
 */
@Log4j2
public class JwtUtils {

    private static SignatureAlgorithm signlgorithm = SignatureAlgorithm.HS256;

    /**
     * build jwt token.
     */
    public static String createJwtToken(String secret, Map<String, Object> claims, String subject,
        long lifeTime) throws Exception {

        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        JwtBuilder builder = Jwts.builder();

        if (Objects.nonNull(claims)) {
            //use claims
            builder.setClaims(claims);
        } else {
            //use default items
            String uuid = UUID.randomUUID().toString();
            builder.setId(uuid)
                .setIssuedAt(now)
                .setSubject(subject);//subject
        }

        //life time
        if (lifeTime >= 0) {
            long expMillis = nowMillis + lifeTime;
            Date exp = new Date(expMillis);
            builder.setExpiration(exp);
        }

        //sign
        builder.signWith(signlgorithm, generalKey(secret));
        return builder.compact();
    }


    /**
     * parse jwt.
     */
    public static Claims parseJWT(String jwt, String secret) throws Exception {
        SecretKey key = generalKey(secret);
        Claims claims = Jwts.parser()
            .setSigningKey(key)
            .parseClaimsJws(jwt).getBody();
        return claims;
    }

    /**
     * build key.
     */
    private static SecretKey generalKey(String secret) {
        if (StringUtils.isBlank(secret)) {
            log.warn("fail generalKey. secret is blank");
            return null;
        }
        byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(secret);
        SecretKey signingKey = new SecretKeySpec(apiKeySecretBytes, signlgorithm.getJcaName());
        return signingKey;

    }
}

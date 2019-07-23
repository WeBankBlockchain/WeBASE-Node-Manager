package com.webank.webase.node.mgr.token;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * token  mapper.
 */
@Repository
public interface TokenMapper {
    void add(TbToken tbToken);

    void delete(@Param("token") String token, @Param("value") String value);

    void update(@Param("token") String token, @Param("expireTime") LocalDateTime expireTime);

    TbToken query(@Param("token") String token);
}

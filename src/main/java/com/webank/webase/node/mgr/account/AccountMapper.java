/**
 * Copyright 2014-2021  the original author or authors.
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
package com.webank.webase.node.mgr.account;

import com.webank.webase.node.mgr.account.entity.AccountListParam;
import com.webank.webase.node.mgr.account.entity.TbAccountInfo;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

/**
 * mapper about account.
 */
@Repository
public interface AccountMapper {

    Integer registerAccount(TbAccountInfo tbAccount);

    Integer addAccountRow(TbAccountInfo tbAccount);

    Integer updateAccountRow(TbAccountInfo tbAccount);

    TbAccountInfo queryByAccount(@Param("account") String account);

    @Select("select count(1) from tb_account_info where mobile=#{mobile}")
    Integer countOfMobile(@Param("mobile") String mobile);

    Integer countOfAccount(@Param("account") String account);

    Integer countOfAccountAvailable(@Param("account") String account);

    List<TbAccountInfo> listOfAccount(@Param("param") AccountListParam param);

    Integer deleteAccountRow(@Param("account") String account);
}

/**
 * Copyright 2014-2019  the original author or authors.
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
package com.webank.webase.node.mgr.contract;

import com.webank.webase.node.mgr.contract.entity.ContractParam;
import com.webank.webase.node.mgr.contract.entity.TbContract;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * contract data interface.
 */
@Repository
public interface ContractMapper {

    Integer add(TbContract tbContract);

    Integer remove(@Param("contractId") Integer contractId);

    Integer update(TbContract tbContract);

    int countOfContract(ContractParam param);

    List<TbContract> listOfContract(ContractParam param);

    TbContract queryByContractId(@Param("contractId") Integer contractId);

    TbContract queryContract(ContractParam queryParam);

    List<TbContract> queryContractByBin(@Param("groupId") Integer groupId,
        @Param("contractBin") String contractBin);

    String querySystemContractBin(@Param("groupId") Integer groupId,
        @Param("contractName") String contractName);

    void updateSystemContract(@Param("groupId") Integer groupId,
        @Param("contractName") String contractName, @Param("contractBin") String contractBin,
        @Param("contractAddress") String contractAddress);

    void removeByGroupId(@Param("groupId") Integer groupId);
}
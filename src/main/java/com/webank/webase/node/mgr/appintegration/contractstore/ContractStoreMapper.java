/**
 * Copyright 2014-2021 the original author or authors.
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
package com.webank.webase.node.mgr.appintegration.contractstore;

import com.webank.webase.node.mgr.appintegration.contractstore.entity.ContractStoreParam;
import com.webank.webase.node.mgr.appintegration.contractstore.entity.TbContractStore;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * contract store mapper.
 */
@Repository
public interface ContractStoreMapper {

    /**
     * save data, update when exist.
     */
    Integer saveContractStore(TbContractStore tbContractStore);

    /**
     * Query the number according to some conditions.
     */
    Integer countOfContractStore(ContractStoreParam contractStoreParam);

    /**
     * Query list according to some conditions.
     */
    List<TbContractStore> listOfContractStore(ContractStoreParam contractStoreParam);

    /**
     * Query info according to some conditions.
     */
    TbContractStore queryContractStore(ContractStoreParam contractStoreParam);

    /**
     * delete.
     */
    void deleteContractStore(@Param("id") Integer id);

}

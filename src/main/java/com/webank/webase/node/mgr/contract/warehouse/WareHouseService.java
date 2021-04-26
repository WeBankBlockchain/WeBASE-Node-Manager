/*
 * Copyright 2014-2020 the original author or authors.
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
package com.webank.webase.node.mgr.contract.warehouse;

import com.webank.webase.node.mgr.contract.warehouse.entity.TbContractFolder;
import com.webank.webase.node.mgr.contract.warehouse.entity.TbContractItem;
import com.webank.webase.node.mgr.contract.warehouse.entity.TbWarehouse;
import com.webank.webase.node.mgr.contract.warehouse.mapper.TbContractFolderMapper;
import com.webank.webase.node.mgr.contract.warehouse.mapper.TbContractItemMapper;
import com.webank.webase.node.mgr.contract.warehouse.mapper.TbWarehouseMapper;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * contract warehouse service
 * include warehouse, contract folder of warehouse, contract item of contract folder
 * @author marsli
 */
@Slf4j
@Service
public class WareHouseService {
    @Autowired
    private TbContractItemMapper contractItemMapper;
    @Autowired
    private TbContractFolderMapper contractFolderMapper;
    @Autowired
    private TbWarehouseMapper wareHouseMapper;

    /**
     *
     */
    public List<TbWarehouse> getStoreList() {
        List<TbWarehouse> storeItemList = wareHouseMapper.findAll();
        return storeItemList;
    }

    /**
     *
     */
    public TbWarehouse getStoreItemById(Integer wareHouseId) {
        TbWarehouse storeItem = wareHouseMapper.selectByPrimaryKey(wareHouseId);
        return storeItem;
    }

    /**
     *
     */
    public TbContractItem getContractItemById(Integer contractId) {
        TbContractItem contractItem = contractItemMapper.selectByPrimaryKey(contractId);
        return contractItem;
    }

    /**
     *
     */
    public List<TbContractItem> getContractItemByFolderId(Integer contractFolderId) {
        List<TbContractItem> contractItemList = contractItemMapper.listByFolderId(contractFolderId);
        return contractItemList;
    }

    /**
     *
     */
    public TbContractFolder getContractFolderById(Integer contractFolderId) {
        TbContractFolder contractFolderItem = contractFolderMapper.selectByPrimaryKey(contractFolderId);
        return contractFolderItem;
    }

    /**
     *
     */
    public List<TbContractFolder> getFolderItemListByStoreId(Integer wareHouseId) {
        List<TbContractFolder> contractFolderItemList = contractFolderMapper.findByWareHouseId(wareHouseId);
        return contractFolderItemList;
    }

}

/**
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

import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.controller.BaseController;
import com.webank.webase.node.mgr.base.entity.BaseResponse;
import com.webank.webase.node.mgr.contract.warehouse.entity.TbContractFolder;
import com.webank.webase.node.mgr.contract.warehouse.entity.TbContractItem;
import com.webank.webase.node.mgr.contract.warehouse.entity.TbWarehouse;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * ContractStoreController.
 * @author marsli
 */
@Slf4j
@RestController
@RequestMapping(value = "/warehouse")
public class WarehouseController extends BaseController {

    @Autowired
    WarehouseService warehouseService;

    /**
     * query the list of contract store item
     */
    @GetMapping(value = "/list")
    public BaseResponse getContractStoreList() {

        List<TbWarehouse>  storeItemList = warehouseService.getStoreList();
        BaseResponse response = new BaseResponse(ConstantCode.SUCCESS);
        response.setData(storeItemList);
        return response;
    }

    /**
     * query a contract store item
     */
    @GetMapping(value = "/{warehouseId}")
    public BaseResponse getContractStoreById(@PathVariable("warehouseId") Integer warehouseId) {
        log.info("getContractStoreById start. warehouseId:{}", warehouseId);
        TbWarehouse storeItem = warehouseService.getStoreItemById(warehouseId);
        BaseResponse response = new BaseResponse(ConstantCode.SUCCESS);
        response.setData(storeItem);
        return response;
    }

    /**
     * query a contract folder item
     */
    @GetMapping(value = "/folder/{contractFolderId}")
    public BaseResponse getContractFolderById(@PathVariable("contractFolderId") Integer contractFolderId) {
        log.info("getContractStoreById start. contractFolderId:{}", contractFolderId);
        TbContractFolder contractFolderItem = warehouseService.getContractFolderById(contractFolderId);
        BaseResponse response = new BaseResponse(ConstantCode.SUCCESS);
        response.setData(contractFolderItem);
        return response;
    }

    /**
     * query a contract item
     */
    @GetMapping(value = "/item/{contractId}")
    public BaseResponse getContractItemById(@PathVariable("contractId") Integer contractId) {
        log.info("getContractStoreById start. contractId:{}", contractId);
        TbContractItem contractItem = warehouseService.getContractItemById(contractId);
        BaseResponse response = new BaseResponse(ConstantCode.SUCCESS);
        response.setData(contractItem);
        return response;
    }

    /**
     * get folderItemList by warehouseId
     */
    @GetMapping(value = "/folder/list/{warehouseId}")
    public BaseResponse getFolderItemListByStoreId(@PathVariable("warehouseId") Integer warehouseId) {
        log.info("getFolderItemListByStoreId start. warehouseId:{}", warehouseId);
        List<TbContractFolder> contractFolderItemList = warehouseService.getFolderItemListByStoreId(warehouseId);
        BaseResponse response = new BaseResponse(ConstantCode.SUCCESS);
        response.setData(contractFolderItemList);
        return response;
    }

    /**
     * get contractItemList by folderId
     */
    @GetMapping(value = "/item/list/{folderId}")
    public BaseResponse getContractItemByFolderId(@PathVariable("folderId") Integer folderId) {
        log.info("getContractItemByFolderId start. warehouseId:{}", folderId);
        List<TbContractItem> contractItemList = warehouseService.getContractItemByFolderId(folderId);
        BaseResponse response = new BaseResponse(ConstantCode.SUCCESS);
        response.setData(contractItemList);
        return response;
    }
}

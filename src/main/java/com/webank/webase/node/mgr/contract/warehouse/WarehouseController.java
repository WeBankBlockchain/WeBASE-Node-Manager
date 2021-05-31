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
import com.webank.webase.node.mgr.base.tools.JsonTools;
import com.webank.webase.node.mgr.contract.warehouse.entity.TbContractFolder;
import com.webank.webase.node.mgr.contract.warehouse.entity.TbContractItem;
import com.webank.webase.node.mgr.contract.warehouse.entity.TbWarehouse;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    public BaseResponse listContractStore() {
        Instant startTime = Instant.now();
        log.info("listContractStore start. startTime:{}", startTime.toEpochMilli());
        List<TbWarehouse>  storeItemList = warehouseService.getStoreList();
        BaseResponse response = new BaseResponse(ConstantCode.SUCCESS);
        response.setData(storeItemList);
        log.info("end listContractStore. useTime:{}", Duration.between(startTime, Instant.now()).toMillis());
        return response;
    }

    /**
     * query a contract store item
     */
    @GetMapping(value = "")
    public BaseResponse getContractStoreById(@RequestParam("warehouseId") Integer warehouseId) {
        Instant startTime = Instant.now();
        log.info("getContractStoreById start. startTime:{}  warehouseId:{}", startTime.toEpochMilli(),
            warehouseId);
        TbWarehouse storeItem = warehouseService.getStoreItemById(warehouseId);
        BaseResponse response = new BaseResponse(ConstantCode.SUCCESS);
        response.setData(storeItem);
        log.info("end getContractStoreById. useTime:{}", Duration.between(startTime, Instant.now()).toMillis());
        return response;
    }

    /**
     * query a contract folder item
     */
    @GetMapping(value = "/folder")
    public BaseResponse getContractFolderById(@RequestParam("folderId") Integer folderId) {
        Instant startTime = Instant.now();
        log.info("getContractFolderById start. startTime:{}  folderId:{}", startTime.toEpochMilli(),
            folderId);
        TbContractFolder contractFolderItem = warehouseService.getContractFolderById(folderId);
        BaseResponse response = new BaseResponse(ConstantCode.SUCCESS);
        response.setData(contractFolderItem);
        log.info("end getContractFolderById. useTime:{}", Duration.between(startTime, Instant.now()).toMillis());
        return response;
    }

    /**
     * query a contract item
     */
    @GetMapping(value = "/item")
    public BaseResponse getContractItemById(@RequestParam("contractId") Integer contractId) {
        Instant startTime = Instant.now();
        log.info("getContractItemById start. startTime:{}  contractId:{}", startTime.toEpochMilli(),
            contractId);
        TbContractItem contractItem = warehouseService.getContractItemById(contractId);
        BaseResponse response = new BaseResponse(ConstantCode.SUCCESS);
        response.setData(contractItem);
        log.info("end getContractItemById. useTime:{}", Duration.between(startTime, Instant.now()).toMillis());
        return response;
    }

    /**
     * get folderItemList by warehouseId
     */
    @GetMapping(value = "/folder/list")
    public BaseResponse listFolderItemByStoreId(@RequestParam("warehouseId") Integer warehouseId) {
        Instant startTime = Instant.now();
        log.info("listFolderItemByStoreId start. startTime:{}  warehouseId:{}", startTime.toEpochMilli(),
            warehouseId);
        List<TbContractFolder> contractFolderItemList = warehouseService.getFolderItemListByStoreId(warehouseId);
        BaseResponse response = new BaseResponse(ConstantCode.SUCCESS);
        response.setData(contractFolderItemList);
        log.info("end listFolderItemByStoreId. useTime:{}", Duration.between(startTime, Instant.now()).toMillis());
        return response;
    }

    /**
     * get contractItemList by folderId
     */
    @GetMapping(value = "/item/list")
    public BaseResponse listContractItemByFolderId(@RequestParam("folderId") Integer folderId) {
        Instant startTime = Instant.now();
        log.info("listContractItemByFolderId start. startTime:{}  folderId:{}", startTime.toEpochMilli(),
            folderId);
        List<TbContractItem> contractItemList = warehouseService.getContractItemByFolderId(folderId);
        BaseResponse response = new BaseResponse(ConstantCode.SUCCESS);
        response.setData(contractItemList);
        log.info("end listContractItemByFolderId. useTime:{}", Duration.between(startTime, Instant.now()).toMillis());
        return response;
    }
}

/**
 * Copyright 2014-2021 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.webank.webase.node.mgr.contract.warehouse;

import com.webank.scaffold.util.CommonUtil;
import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.contract.warehouse.entity.TbContractFolder;
import com.webank.webase.node.mgr.contract.warehouse.entity.TbContractItem;
import com.webank.webase.node.mgr.contract.warehouse.entity.TbWarehouse;
import com.webank.webase.node.mgr.contract.warehouse.mapper.TbContractFolderMapper;
import com.webank.webase.node.mgr.contract.warehouse.mapper.TbContractItemMapper;
import com.webank.webase.node.mgr.contract.warehouse.mapper.TbWarehouseMapper;
import com.webank.webase.node.mgr.tools.JsonTools;
import com.webank.webase.node.mgr.tools.NodeMgrTools;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class PresetDataService {

    @Autowired
    private TbContractItemMapper contractItemMapper;
    @Autowired
    private TbContractFolderMapper contractFolderMapper;
    @Autowired
    private TbWarehouseMapper warehouseMapper;
    
    public void initPresetData()
    {
        this.readAndInitContractItem();
        this.readAndInitFolderItem();
        this.readAndInitStoreItem();
    }

    /**
     * read from ./resources/warehouse/*.json
     */
    public void readAndInitStoreItem() {
        String jsonStr = this.loadWarehouseJson("warehouse/warehouse.json");
        List<TbWarehouse> storeItems = JsonTools.toJavaObjectList(jsonStr, TbWarehouse.class);
        if (storeItems == null) {
            log.error("readAndInitStoreItem get null");
            return;
        }
        List<TbWarehouse> item2Save = new ArrayList<>();
        for (TbWarehouse item : storeItems) {
            if (warehouseMapper.selectByPrimaryKey(item.getId()) == null) {
                item.setCreateTime(new Date());
                item.setModifyTime(item.getCreateTime());
                item2Save.add(item);
            }
        }
        warehouseMapper.batchInsert(item2Save);
        log.info("readAndInitStoreItem save {} items", storeItems.size());
    }

    public void readAndInitFolderItem() {
        String jsonStr = this.loadWarehouseJson("warehouse/folder.json");
        List<TbContractFolder> folderItems = JsonTools.toJavaObjectList(jsonStr, TbContractFolder.class);
        if (folderItems == null) {
            log.error("readAndInitFolderItem get null");
            return;
        }
        List<TbContractFolder> item2Save = new ArrayList<>();
        for (TbContractFolder item : folderItems) {
            if (contractFolderMapper.selectByPrimaryKey(item.getId()) == null) {
                item.setCreateTime(new Date());
                item.setModifyTime(item.getCreateTime());
                item2Save.add(item);
            }
        }
        contractFolderMapper.batchInsert(item2Save);
        log.info("readAndInitFolderItem save {} items", folderItems.size());
    }

    public void readAndInitContractItem() {
        String jsonStr = this.loadWarehouseJson("warehouse/contract.json");
        List<TbContractItem> contractItems = JsonTools.toJavaObjectList(jsonStr, TbContractItem.class);
        if (contractItems == null) {
            log.error("readAndInitContractItem get null");
            return;
        }
        List<TbContractItem> item2Save = new ArrayList<>();
        for (TbContractItem item : contractItems) {
            if (contractItemMapper.selectByPrimaryKey(item.getId()) == null) {
                item.setCreateTime(new Date());
                item.setModifyTime(item.getCreateTime());
                // contract item's desc parse into base64
                item.setDescription(NodeMgrTools.encodedBase64Str(item.getDescription()));
                item.setDescriptionEn(NodeMgrTools.encodedBase64Str(item.getDescriptionEn()));
                item2Save.add(item);
            }
        }
        contractItemMapper.batchInsert(item2Save);
        log.info("readAndInitContractItem save {} items", contractItems.size());
    }

    private String loadWarehouseJson(String jsonFilePath) {
        log.info("loadWarehouseJson :{}", jsonFilePath);
        try (InputStream nodeCrtInput = new ClassPathResource(jsonFilePath).getInputStream()) {
            String jsonStr = IOUtils.toString(nodeCrtInput, StandardCharsets.UTF_8);
            log.debug("loadCrtContentByPath itemList:{}", jsonStr);
            return jsonStr;
        } catch (Exception e) {
            log.error("loadWarehouseJson, Exception:[]", e);
            throw new NodeMgrException(ConstantCode.SYSTEM_EXCEPTION);
        } 
    }
}

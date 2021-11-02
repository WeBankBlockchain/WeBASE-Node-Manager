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

package node.mgr.test.contract.warehouse;

import com.webank.webase.node.mgr.contract.warehouse.entity.TbContractFolder;
import com.webank.webase.node.mgr.contract.warehouse.entity.TbContractItem;
import com.webank.webase.node.mgr.contract.warehouse.entity.TbWarehouse;
import com.webank.webase.node.mgr.contract.warehouse.mapper.TbContractFolderMapper;
import com.webank.webase.node.mgr.contract.warehouse.mapper.TbContractItemMapper;
import com.webank.webase.node.mgr.contract.warehouse.mapper.TbWarehouseMapper;
import com.webank.webase.node.mgr.tools.JsonTools;
import java.util.List;
import node.mgr.test.base.TestBase;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class PresetDataServiceTest extends TestBase {

    @Autowired
    private TbContractItemMapper contractItemMapper;
    @Autowired
    private TbContractFolderMapper contractFolderMapper;
    @Autowired
    private TbWarehouseMapper warehouseMapper;

    @Test
    public void testPrintAll() {
        List<TbWarehouse> storeItems = warehouseMapper.findAll();
        for (TbWarehouse TbWarehouse : storeItems) {
            TbWarehouse.setCreateTime(null);
            TbWarehouse.setModifyTime(null);
            System.out.println(JsonTools.objToString(TbWarehouse));
        }

        System.out.println("=========");
        List<TbContractFolder> folderList = contractFolderMapper.findAll();
        for (TbContractFolder folderItem : folderList) {
            folderItem.setCreateTime(null);
            folderItem.setModifyTime(null);
            System.out.println(JsonTools.objToString(folderItem));
        }

        System.out.println("=========");

        List<TbContractItem> contractItemList = contractItemMapper.findAll();
        for (TbContractItem contractItem : contractItemList) {
            contractItem.setCreateTime(null);
            contractItem.setModifyTime(null);
            System.out.println(JsonTools.objToString(contractItem));
        }



    }
}

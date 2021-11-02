package com.webank.webase.node.mgr.contract.warehouse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class WarehouseStartupRunner implements CommandLineRunner {

    @Autowired
    PresetDataService presetDataService;

    @Override
    public void run(String... args) throws Exception {
        presetDataService.initPresetData();
    }
}


package com.webank.webase.node.mgr.contract.entity;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import lombok.NoArgsConstructor;

/**
 * @author marsli
 */
@Data
@NoArgsConstructor
public class ReqCopyContracts {
    @NotBlank
    private String account;
    @NotNull
    private Integer groupId;
    private String contractPath;
    private List<RepCopyContractItem> contractItems;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RepCopyContractItem {
        private String contractName;
        private String contractSource;
    }
}

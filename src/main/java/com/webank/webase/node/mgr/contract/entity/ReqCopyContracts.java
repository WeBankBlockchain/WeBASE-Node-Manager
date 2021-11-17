package com.webank.webase.node.mgr.contract.entity;

import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author marsli
 */
@Data
@NoArgsConstructor
public class ReqCopyContracts {
    private String account;
    @NotNull
    private String groupId;
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

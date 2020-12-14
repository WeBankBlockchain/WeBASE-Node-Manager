/**
 *
 */


package com.webank.webase.node.mgr.base.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public enum OptionType {
    DEPLOY_CHAIN("Deploy chain."),
    MODIFY_CHAIN("Add nodes, delete node, upgrade chain."),
    ;

    private String description;
}

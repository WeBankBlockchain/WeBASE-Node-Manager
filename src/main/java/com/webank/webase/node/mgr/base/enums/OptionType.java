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
    DEPLOY("Deploy chain."),
    MODIFY("Add nodes or delete node."),
    ;

    private String description;
}

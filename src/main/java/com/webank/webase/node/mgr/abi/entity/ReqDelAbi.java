package com.webank.webase.node.mgr.abi.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * entity to delete abi info by id
 * @author marsli
 */
@Data
@NoArgsConstructor
public class ReqDelAbi {
	@NotNull
	private Long abiId;
}
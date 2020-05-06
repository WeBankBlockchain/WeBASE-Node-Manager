package com.webank.webase.node.mgr.abi.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * entity to delete abi info by id
 * @author marsli
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReqDelAbi {
	@NotNull
	private Integer abiId;
}
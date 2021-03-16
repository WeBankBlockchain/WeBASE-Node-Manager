/**
 * Copyright 2014-2021 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.webase.node.mgr.base.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * Enumeration of group type.
 * @author marsli
 */
@Getter
@ToString
@AllArgsConstructor
public enum GroupType {
	/**
	 * sync from node
	 */
	SYNC(1,"sync"),
	/**
	 * manual generated
	 */
	MANUAL(2,"manual"),

	/**
	 * deploy
	 */
	DEPLOY(3,"deploy");

	private int value;
	private String description;

}


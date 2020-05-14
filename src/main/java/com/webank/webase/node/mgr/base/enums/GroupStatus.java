/**
 * Copyright 2014-2020 the original author or authors.
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

/**
 * group status in tb_group
 */
public enum GroupStatus {
		/**
		 * running
		 */
		NORMAL(1),
		/**
		 * stopped or removed
		 */
		MAINTAINING(2),
		/**
		 * invalid for group data conflicts etc.
		 */
		CONFLICT(3);

		private int value;

	GroupStatus(Integer groupType) {
			this.value = groupType;
		}

	public int getValue() {
		return this.value;
	}

}


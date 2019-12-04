/**
 * Copyright 2014-2019 the original author or authors.
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

package com.webank.webase.node.mgr.base.constant;

/**
 * precompiled contract's methodId used in audit
 * including old methodId and GM 's new methodId
 */
public class MethodIdConstants {

    // flag shows whether refresh tb_method to gm
    public static boolean switched2Gm = false;
    /**
     * STANDARD  precompiled methodId
     */
    public static final String SYSTEM_CONFIG_SET_VALUE_BY_KEY_STANDARD = "0xbd291aef";
    public static final String TABLE_FACTORY_CREATE_TABLE_STANDARD = "0x56004b6a";
    public static final String CRUD_UPDATE_STANDARD = "0x2dca76c1";
    public static final String CRUD_SELECT_STANDARD = "0x983c6c4f";
    public static final String CRUD_REMOVE_STANDARD = "0xa72a1e65";
    public static final String CRUD_INSERT_STANDARD = "0xa216464b";
    public static final String CONSENSUS_ADD_OBSERVER_STANDARD = "0x2800efc0";
    public static final String CONSENSUS_ADD_SEALER_STANDARD = "0x89152d1f";
    public static final String CONSENSUS_REMOVE_STANDARD = "0x80599e4b";
    public static final String CNS_SELECT_BY_NAME_STANDARD = "0x819a3d62";
    public static final String CNS_SELECT_BY_NAME_AND_VERSION_STANDARD = "0x897f0251";
    public static final String CNS_INSERT_STANDARD = "0xa216464b";
    public static final String PERMISSION_INSERT_STANDARD = "0x06e63ff8";
    public static final String PERMISSION_QUERY_BY_NAME_STANDARD = "0x20586031";
    public static final String PERMISSION_REMOVE_STANDARD = "0x44590a7e";

    /**
     * guomi version
     */
    public static final String SYSTEM_CONFIG_SET_VALUE_BY_KEY_GM = "0x0749b518";
    public static final String TABLE_FACTORY_CREATE_TABLE_GM = "0xc92a7801";
    public static final String CRUD_UPDATE_GM = "0x10bd675b";
    public static final String CRUD_SELECT_GM = "0x7388111f";
    public static final String CRUD_REMOVE_GM = "0x81b81824";
    public static final String CRUD_INSERT_GM = "0xb8eaa08d";
    public static final String CONSENSUS_ADD_OBSERVER_GM = "0x25e85d16";
    public static final String CONSENSUS_ADD_SEALER_GM = "0xdf434acc";
    public static final String CONSENSUS_REMOVE_GM = "0x86b733f9";
    public static final String CNS_SELECT_BY_NAME_GM = "0x078af4af";
    public static final String CNS_SELECT_BY_NAME_AND_VERSION_GM = "0xec72a422";
    public static final String CNS_INSERT_GM = "0xb8eaa08d";
    public static final String PERMISSION_INSERT_GM = "0xce0a9fb9";
    public static final String PERMISSION_QUERY_BY_NAME_GM = "0xbbec3f91";
    public static final String PERMISSION_REMOVE_GM = "0x85d23afc";

}

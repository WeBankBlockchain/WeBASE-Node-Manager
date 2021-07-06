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

package node.mgr.test.method;

import com.webank.webase.node.mgr.lite.base.tools.Web3Tools;
import java.io.IOException;
import java.util.List;
import node.mgr.test.base.TestBase;
import org.fisco.bcos.sdk.abi.wrapper.ABIDefinition;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 配置sdk的encryptType，配置为国密或标密
 */
public class MethodIdGmTest extends TestBase {
    @Autowired private CryptoSuite cryptoSuite;

    /**
     * 设置不同的encryptType 1 或者 0， 查看同一个method出来的methodId是否符合
     * 默认standard methodId:
     */
    /* update(string,string,string,string,string)
       select(string,string,string,string)
       insert(string,string,string,string)
       remove(string,string,string,string)
     */

    public static final String CRUD_UPDATE_STANDARD = "0x2dca76c1";
    public static final String CRUD_SELECT_STANDARD = "0x983c6c4f";
    public static final String CRUD_INSERT_STANDARD = "0xa216464b";
    public static final String CRUD_REMOVE_STANDARD = "0xa72a1e65";
    // output:
    /*
    crud:
    guomi
        update: 0x10bd675b
        select: 0x7388111f
        insert: 0xb8eaa08d
        remove: 0x81b81824
    standard
        update: 0x2dca76c1
        select: 0x983c6c4f
        insert: 0xa216464b
        remove: 0xa72a1e65
     */

    // precompiled contract abi
    public static final String crud_Abi = "[{\"constant\":false,\"inputs\":[{\"name\":\"tableName\",\"type\":\"string\"},{\"name\":\"key\",\"type\":\"string\"},{\"name\":\"entry\",\"type\":\"string\"},{\"name\":\"condition\",\"type\":\"string\"},{\"name\":\"optional\",\"type\":\"string\"}],\"name\":\"update\",\"outputs\":[{\"name\":\"\",\"type\":\"int256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"tableName\",\"type\":\"string\"},{\"name\":\"key\",\"type\":\"string\"},{\"name\":\"condition\",\"type\":\"string\"},{\"name\":\"optional\",\"type\":\"string\"}],\"name\":\"select\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"tableName\",\"type\":\"string\"},{\"name\":\"key\",\"type\":\"string\"},{\"name\":\"entry\",\"type\":\"string\"},{\"name\":\"optional\",\"type\":\"string\"}],\"name\":\"insert\",\"outputs\":[{\"name\":\"\",\"type\":\"int256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"tableName\",\"type\":\"string\"},{\"name\":\"key\",\"type\":\"string\"},{\"name\":\"condition\",\"type\":\"string\"},{\"name\":\"optional\",\"type\":\"string\"}],\"name\":\"remove\",\"outputs\":[{\"name\":\"\",\"type\":\"int256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"}]";
    public static final String sys_config_Abi = "[{\"constant\":false,\"inputs\":[{\"name\":\"key\",\"type\":\"string\"},{\"name\":\"value\",\"type\":\"string\"}],\"name\":\"setValueByKey\",\"outputs\":[{\"name\":\"\",\"type\":\"int256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"}]";
    public static final String table_factory_Abi = "[{\"constant\":false,\"inputs\":[{\"name\":\"tableName\",\"type\":\"string\"},{\"name\":\"key\",\"type\":\"string\"},{\"name\":\"valueField\",\"type\":\"string\"}],\"name\":\"createTable\",\"outputs\":[{\"name\":\"\",\"type\":\"int256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"}]";
    public static final String consensus_Abi = "[{\"constant\":false,\"inputs\":[{\"name\":\"nodeID\",\"type\":\"string\"}],\"name\":\"addObserver\",\"outputs\":[{\"name\":\"\",\"type\":\"int256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"nodeID\",\"type\":\"string\"}],\"name\":\"remove\",\"outputs\":[{\"name\":\"\",\"type\":\"int256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"nodeID\",\"type\":\"string\"}],\"name\":\"addSealer\",\"outputs\":[{\"name\":\"\",\"type\":\"int256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"}]";
    public static final String cns_Abi = "[{\"constant\":true,\"inputs\":[{\"name\":\"name\",\"type\":\"string\"}],\"name\":\"selectByName\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"name\",\"type\":\"string\"},{\"name\":\"version\",\"type\":\"string\"}],\"name\":\"selectByNameAndVersion\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"name\",\"type\":\"string\"},{\"name\":\"version\",\"type\":\"string\"},{\"name\":\"addr\",\"type\":\"string\"},{\"name\":\"abi\",\"type\":\"string\"}],\"name\":\"insert\",\"outputs\":[{\"name\":\"\",\"type\":\"int256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"}]";
    public static final String permission_Abi = "[{\"constant\":false,\"inputs\":[{\"name\":\"table_name\",\"type\":\"string\"},{\"name\":\"addr\",\"type\":\"string\"}],\"name\":\"insert\",\"outputs\":[{\"name\":\"\",\"type\":\"int256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"table_name\",\"type\":\"string\"}],\"name\":\"queryByName\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"table_name\",\"type\":\"string\"},{\"name\":\"addr\",\"type\":\"string\"}],\"name\":\"remove\",\"outputs\":[{\"name\":\"\",\"type\":\"int256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"}]";
    public static final String chain_governance_Abi = "[{\"constant\":true,\"inputs\":[],\"name\":\"listOperators\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"user\",\"type\":\"address\"},{\"name\":\"weight\",\"type\":\"int256\"}],\"name\":\"updateCommitteeMemberWeight\",\"outputs\":[{\"name\":\"\",\"type\":\"int256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"queryThreshold\",\"outputs\":[{\"name\":\"\",\"type\":\"int256\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"user\",\"type\":\"address\"}],\"name\":\"queryCommitteeMemberWeight\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"},{\"name\":\"\",\"type\":\"int256\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"user\",\"type\":\"address\"}],\"name\":\"grantCommitteeMember\",\"outputs\":[{\"name\":\"\",\"type\":\"int256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"account\",\"type\":\"address\"}],\"name\":\"unfreezeAccount\",\"outputs\":[{\"name\":\"\",\"type\":\"int256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"listCommitteeMembers\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"threshold\",\"type\":\"int256\"}],\"name\":\"updateThreshold\",\"outputs\":[{\"name\":\"\",\"type\":\"int256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"user\",\"type\":\"address\"}],\"name\":\"revokeCommitteeMember\",\"outputs\":[{\"name\":\"\",\"type\":\"int256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"user\",\"type\":\"address\"}],\"name\":\"grantOperator\",\"outputs\":[{\"name\":\"\",\"type\":\"int256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"account\",\"type\":\"address\"}],\"name\":\"freezeAccount\",\"outputs\":[{\"name\":\"\",\"type\":\"int256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"user\",\"type\":\"address\"}],\"name\":\"revokeOperator\",\"outputs\":[{\"name\":\"\",\"type\":\"int256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"account\",\"type\":\"address\"}],\"name\":\"getAccountStatus\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"}]";
    public static final String contract_life_cycle_Abi = "[{\"constant\":true,\"inputs\":[{\"name\":\"addr\",\"type\":\"address\"}],\"name\":\"getStatus\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"},{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"addr\",\"type\":\"address\"}],\"name\":\"unfreeze\",\"outputs\":[{\"name\":\"\",\"type\":\"int256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"addr\",\"type\":\"address\"}],\"name\":\"freeze\",\"outputs\":[{\"name\":\"\",\"type\":\"int256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"contractAddr\",\"type\":\"address\"},{\"name\":\"userAddr\",\"type\":\"address\"}],\"name\":\"grantManager\",\"outputs\":[{\"name\":\"\",\"type\":\"int256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"addr\",\"type\":\"address\"}],\"name\":\"queryManager\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"},{\"name\":\"\",\"type\":\"address[]\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"}]";


    @Test
    public void testMethodId() throws IOException {

        // load abi
        List<ABIDefinition> abiList = Web3Tools.loadContractDefinition(crud_Abi);

        // 在application.yml中切换sdk-encryptType的值
        if(cryptoSuite.cryptoTypeConfig == 1) {
            // encryptType = 1 guomi:
            System.out.println("guomi");
            for (ABIDefinition abiDefinition : abiList) {
                if ("function".equals(abiDefinition.getType())) {
                    // support guomi sm3
                    String buildMethodId = Web3Tools.buildMethodId(abiDefinition, cryptoSuite);
                    System.out.println(abiDefinition.getName() + ": " + buildMethodId);
                }
            }
        } else if(cryptoSuite.cryptoTypeConfig == 0) {
            System.out.println("standard");
            for (ABIDefinition abiDefinition : abiList) {
                if ("function".equals(abiDefinition.getType())) {
                    // support guomi sm3
                    String buildMethodId = Web3Tools.buildMethodId(abiDefinition, cryptoSuite);
                    System.out.println(abiDefinition.getName() + ": " + buildMethodId);
                }
            }
        }
    }

    @Test
    public void testGetAllMethod() throws IOException {
        // 在application.yml中切换sdk-encryptType的值
        if(cryptoSuite.cryptoTypeConfig == 1) {
            // encryptType = 1 guomi:
            System.out.println("guomi");
            System.out.println();
        } else if(cryptoSuite.cryptoTypeConfig == 0) {
            System.out.println("standard");
            System.out.println();
        }
        getAllPreMethodId();
    }

    String a = "{\"constant\":true,\"inputs\":[{\"name\":\"addr\",\"type\":\"address\"}],\"name\":\"queryManager\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"},{\"name\":\"\",\"type\":\"address[]\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"}";
    public void getAllPreMethodId() throws IOException {
        // load abi
        List<ABIDefinition> sys_config = Web3Tools.loadContractDefinition(sys_config_Abi);
        List<ABIDefinition> table_factory = Web3Tools.loadContractDefinition(table_factory_Abi);
        List<ABIDefinition> crud = Web3Tools.loadContractDefinition(crud_Abi);
        List<ABIDefinition> consensus = Web3Tools.loadContractDefinition(consensus_Abi);
        List<ABIDefinition> cns = Web3Tools.loadContractDefinition(cns_Abi);
        List<ABIDefinition> permission = Web3Tools.loadContractDefinition(permission_Abi);
        List<ABIDefinition> chainGovernance = Web3Tools.loadContractDefinition(chain_governance_Abi);
        List<ABIDefinition> contractLife = Web3Tools.loadContractDefinition(contract_life_cycle_Abi);

        System.out.println("sys_config");
        for (ABIDefinition abiDefinition : sys_config) {
            if ("function".equals(abiDefinition.getType())) {
                // support guomi sm3
                String buildMethodId = Web3Tools.buildMethodId(abiDefinition, cryptoSuite);
                System.out.println(abiDefinition.getName() + ": " + buildMethodId);
            }
        }
        System.out.println("table_factory");
        for (ABIDefinition abiDefinition : table_factory) {
            if ("function".equals(abiDefinition.getType())) {
                // support guomi sm3
                String buildMethodId = Web3Tools.buildMethodId(abiDefinition, cryptoSuite);
                System.out.println(abiDefinition.getName() + ": " + buildMethodId);
            }
        }
        System.out.println("crud");
        for (ABIDefinition abiDefinition : crud) {
            if ("function".equals(abiDefinition.getType())) {
                // support guomi sm3
                String buildMethodId = Web3Tools.buildMethodId(abiDefinition, cryptoSuite);
                System.out.println(abiDefinition.getName() + ": " + buildMethodId);
            }
        }
        System.out.println("consensus");
        for (ABIDefinition abiDefinition : consensus) {
            if ("function".equals(abiDefinition.getType())) {
                // support guomi sm3
                String buildMethodId = Web3Tools.buildMethodId(abiDefinition, cryptoSuite);
                System.out.println(abiDefinition.getName() + ": " + buildMethodId);
            }
        }
        System.out.println("cns");
        for (ABIDefinition abiDefinition : cns) {
            if ("function".equals(abiDefinition.getType())) {
                // support guomi sm3
                String buildMethodId = Web3Tools.buildMethodId(abiDefinition, cryptoSuite);
                System.out.println(abiDefinition.getName() + ": " + buildMethodId);
            }
        }
        System.out.println("permission");
        for (ABIDefinition abiDefinition : permission) {
            if ("function".equals(abiDefinition.getType())) {
                // support guomi sm3
                String buildMethodId = Web3Tools.buildMethodId(abiDefinition, cryptoSuite);
                System.out.println(abiDefinition.getName() + ": " + buildMethodId);
            }
        }
        System.out.println("chainGovernance");
        for (ABIDefinition abiDefinition : chainGovernance) {
            if ("function".equals(abiDefinition.getType())) {
                // support guomi sm3
                String buildMethodId = Web3Tools.buildMethodId(abiDefinition, cryptoSuite);
                System.out.println(abiDefinition.getName() + ": " + buildMethodId);
            }
        }
        System.out.println("contractLife");
        for (ABIDefinition abiDefinition : contractLife) {
            if ("function".equals(abiDefinition.getType())) {
                // support guomi sm3
                String buildMethodId = Web3Tools.buildMethodId(abiDefinition, cryptoSuite);
                System.out.println(abiDefinition.getName() + ": " + buildMethodId);
            }
        }
    }

    /*
    guomi version:
    sys_config
        setValueByKey: 0x0749b518
    table_factory
        createTable: 0xc92a7801
    crud
        update: 0x10bd675b
        select: 0x7388111f
        insert: 0xb8eaa08d
        remove: 0x81b81824
    consensus
        addObserver: 0x25e85d16
        remove: 0x86b733f9
        addSealer: 0xdf434acc
    cns
        selectByName: 0x078af4af
        selectByNameAndVersion: 0xec72a422
        insert: 0xb8eaa08d
    permission
        insert: 0xce0a9fb9
        queryByName: 0xbbec3f91
        remove: 0x85d23afc
     */
}

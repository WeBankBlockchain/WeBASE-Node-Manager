/**
 * Copyright 2014-2020 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package node.mgr.test.contract.warehouse;

import java.util.Base64;
import org.junit.Test;

public class WarehouseMdTest {

    @Test
    public void print() {
//        System.out.println(ENROLL_PROXY_SOURCE);
//        System.out.println();
    }

    @Test
    public void testMdToBase64() {
//        System.out.println("ADDRESS_MD:\n" + Base64.getEncoder().encodeToString(ADDRESS_MD.getBytes()));
//        System.out.println("SAFE_MATH_MD:\n" + Base64.getEncoder().encodeToString(SAFE_MATH_MD.getBytes()));
//        System.out.println("ROLES_MD:\n" + Base64.getEncoder().encodeToString(ROLES_MD.getBytes()));
//        System.out.println("BAC001_MD:\n" + Base64.getEncoder().encodeToString(BAC001_MD.getBytes()));
//        System.out.println("BAC002_MD:\n" + Base64.getEncoder().encodeToString(BAC002_MD.getBytes()));
        System.out.println("PROXY_MD:\n" + Base64.getEncoder().encodeToString(PROXY_MD.getBytes()));
    }

    public static final String PROXY_MD = "# 代理合约模板\n" +
            "\n" +
            "\n" +
            "本合约模板由深圳前海股权交易中心贡献，针对数据上链编写的通用代理存储合约。\n" +
            "\n" +
            "## 简介\n" +
            "代理合约利用solidity的fallback功能，包含EnrollProxy（代理合约），EnrollController（业务合约），EnrollStorage（存储合约）。\n" +
            "\n" +
            "- 代理合约对外交互接口\n" +
            "- 业务合约实现业务逻辑\n" +
            "- 存储合约完成数据存储\n" +
            "\n" +
            "EnrollProxy合约通过Fallback机制调用EnrollController合约的函数进行数据上链（通过EnrollProxy合约地址结合使用EnrollController合约的ABI，操作EnrollController合约的函数），其带来的优点包括：\n" +
            "\n" +
            "- 区块链应用的业务层只与EnrollProxy合约进行交互，EnrollProxy合约不会升级，地址不会变化。\n" +
            "\n" +
            "- 后续中业务或存储需求导致业务合约或存储合约需要升级，则升级EnrollController和EnrollStorage合约，达到数据、业务逻辑解耦的效果。\n" +
            "\n" +
            "*期待你一起完善合约模板中的权限控制逻辑*\n" +
            "\n" +
            "## 合约架构说明\n" +
            "\n" +
            "```java\n" +
            "EnrollProxy\n" +
            "\t继承EnrollStorageStateful\n" +
            "\t继承Proxy（继承Ownable）　\n" +
            "    \n" +
            "EnrollController\n" +
            "\t继承EnrollStorageStateful\n" +
            "\t继承Ownable\n" +
            "\n" +
            "EnrollStorageStateful\n" +
            "\t包含成员enrollStorage，EnrollStorage合约实例\n" +
            "\n" +
            "由于是继承的关系，EnrollProxy合约和EnrollController合约的存储空间排列是一样的，所以可通过EnrollProxy执行fallback操作。  \n" +
            "\n" +
            "enrollStorage是EnrollStorageStateful合约中的成员，所以enrollStorage合约与EnrollStorageStateful合约存储空间排布是不一样。\n" +
            "```\n" +
            "\n" +
            "## 使用说明\n" +
            "1. 编译部署EnrollProxy，EnrollController，EnrollStorage合约。\n" +
            "2. 配置代理合约：\n" +
            "   1. 存储合约合约：调用EnrollProxy合约setStorage函数，参数为EnrollStorage合约地址。\n" +
            "   2. 配置业务合约：调用EnrollProxy合约upgradeTo函数，参数为：合约版本号，EnrollController合约地址。\n" +
            "3. 设置存储合约的代理地址：调用EnrollStorage合约setProxy函数，参数为EnrollProxy合约地址。\n" +
            "       \n" +
            "\n" +
            "完成以上步骤后，就可以通过EnrollProxy合约地址，结合业务合约EnrollController合约的ABI，操作EnrollController合约的业务函数。\n" +
            "\n" +
            "\n";

    public static final String ADDRESS_MD = "# Address\n" +
            "\n" +
            "Address library\n" +
            "\n" +
            "Address contract usage methods can refer to the Points contract warehouse。\n" +
            "\n" +
            "1. Detect whether the address is a contract\n" +
            "2. Detect if the address is 0x0";
    public static final String LIB_STRING_MD = "# String library\n" +
            "\n" +
            "String library\n" +
            "\n" +
            "Provides common string-related operations, including copying, finding, replacing, and so on。";
    public static final String SAFE_MATH_MD = "# SafeMath\n" +
            "\n" +
            "SafeMath library\n" +
            "\n" +
            "A secure mathematical library that provides a safe addition, subtract, and divide。The use of secure mathematical contracts can refer to the Points contract warehouse。";
    public static final String TABLE_MD = "# table\n" +
            "\n" +
            "table library\n" +
            "\n" +
            "BCOS CRUD uses the base library, which you can reference by re-contract calling CRUD.Table contract usage methods can be described by referring to the assat example in the bcos document https://fisco-bcos-documentation.readthedocs.io.";
    public static final String ROLES_MD ="# Roles\n" +
            "\n" +
            "Role permissions control contracts\n";
    public static final String REGISTER_MD ="# Register\n" +
            "\n" +
            "Register control contract\n";
    public static final String COUNTER_MD ="# Counters\n" +
            "\n" +
            "Counters tool contract\n";

    public static final String EVIDENCE_MD = "# Evidence 合约\n" +
            "\n" +
            "## 简介\n" +
            "Evidence 示例合约，使用分层的智能合约结构： \n" +
            "1）工厂合约（EvidenceSignersData.sol），由存证各方事前约定，存储存证生效条件，并管理存证的生成。  \n" +
            "2）存证合约（Evidence.sol），由工厂合约生成，存储存证id，hash和各方签名（每张存证一个合约）。  ";
    public static final String BAC002_MD = "# BAC002 合约规范\n"
        + "\n"
        + "## 简介\n"
        + " BAC002 是区块链上定义非同质化资产的一种标准，可以用于唯一性资产类型，如房产、汽车、道具、版权等。，并可以做相应增发，销毁，暂停合约，黑白名单等权限控制。\n"
        + "## 三个基本元素\n"
        + "- description\n"
        + "\n"
        + "  资产的具体描述\n"
        + "\n"
        + "- shortName\n"
        + "\n"
        + "  资产简称\n"
        + "\n"
        + "- assetId\n"
        + "\n"
        + "  资产编号\n"
        + "\n"
        + " ## 五个基本行为\n"
        + "- 发行\n"
        + "\n"
        + "  调用合约的 deploy 方法，传入 description 和 shortName，即在区块链上发行指定名称的资产\n"
        + "\n"
        + "- 转账\n"
        + "\n"
        + "  调用 safeSendFrom 方法实现转账，调用 balance 方法可以查看自己的资产数量\n"
        + "\n"
        + "- 增发\n"
        + "\n"
        + "  调用 issueWithAssetURI 方法向资产地址增发指定资产编号和资产描述链接信息的资产。另外，可以通过 addIssuer 增加 有权限增发资产的人，也可以通过 renounceIssuer 方法移除增发权限\n"
        + "\n"
        + "- 销毁\n"
        + "\n"
        + "  调用 destroy 以及 destroyFrom 销毁自己地址下资产和特定地址下的资产\n"
        + "\n"
        + "- 暂停\n"
        + "\n"
        + "  遇到紧急状况，你可以调用 suspend 方法，暂停合约，这样任何人都不能调用 send 函数。故障修复后，可以调用 unSuspend 方法解除暂停。也可以通过 addSuspender 和 renounceSuspender 相应增加和移除暂停者权限\n"
        + "\n"
        + "\n"
        + "## 接口说明\n"
        + "\n"
        + "- <b>shortName()</b>\n"
        + "\n"
        + "  资产简称\n"
        + "\n"
        + "- <b>description()</b>\n"
        + "\n"
        + "  资产描述\n"
        + "\n"
        + "- <b>balance(address owner)</b>\n"
        + "\n"
        + "  返回 owner 的资产总数\n"
        + "\n"
        + "- <b>totalSupply()</b>\n"
        + "\n"
        + "  获得当前合约总的资产数目\n"
        + "\n"
        + "- <b>ownerOf(uint256 assetId)</b>\n"
        + "\n"
        + "  返回资产持有者的地址\n"
        + "\n"
        + "- <b>approve(address to, uint256 assetId)</b>\n"
        + "\n"
        + "  授予地址to具有指定资产的控制权\n"
        + "\n"
        + "  - 此方法配合 getapproved 使用\n"
        + "\n"
        + "- <b>getApproved(uint256 assetId)</b>\n"
        + "\n"
        + "  获得资产授权的地址用户\n"
        + "\n"
        + "  - 此方法配合 approve 使用，注意不要配合 setapprovealforall 方法使用\n"
        + "\n"
        + "- <b>setApprovalForAll(address operator, bool approved)</b>\n"
        + "\n"
        + "  授予地址operator具有自己所有资产的控制权\n"
        + "\n"
        + "- <b>isApprovedForAll(address owner, address operator)</b>\n"
        + "\n"
        + "  查询授权\n"
        + "\n"
        + "- <b>sendFrom(address from, address to, uint256 assetId, bytes memory data)</b>\n"
        + "\n"
        + "  安全转账，防止你转到错误的合约地址 ( to如果是合约地址，必须实现接收接口 BAC002Holder 才可以接收转账 )，并可以带转账备注\n"
        + "\n"
        + "  - suspend 状态下无法执行此操作\n"
        + "\n"
        + "- <b>batchSendFrom(address from, address[] to, uint256[] assetId, bytes memory data)</b>\n"
        + "\n"
        + "  批量安全转账\n"
        + "\n"
        + "  - suspend 状态下无法执行此操作\n"
        + "  - to 数组元素个数需要和 assetid 数组元素个数一致\n"
        + "\n"
        + "- <b>issueWithAssetURI(address to, uint256 assetId, string memory assetURI, bytes data)</b>\n"
        + "\n"
        + "  给地址 to 创建资产 assetId，data 是转账备注, assetURI  资产描述\n"
        + "\n"
        + "- <b>isIssuer(address account)</b>\n"
        + "\n"
        + "  检查account是否有增加资产的权限\n"
        + "\n"
        + "- <b>addIssuer(address account)</b>\n"
        + "\n"
        + "  使地址 account 拥有增加资产的权限\n"
        + "\n"
        + "- <b>renounceIssuer()</b>\n"
        + "\n"
        + "  移除增加资产的权限\n"
        + "\n"
        + "- <b>suspend()</b>\n"
        + "\n"
        + "  暂停合约\n"
        + "\n"
        + "  - suspend 后无法进行 safesendfrom / sendfrom / safeBatchSendFrom 操作\n"
        + "\n"
        + "- <b>unSuspend()</b>\n"
        + "\n"
        + "  重启合约\n"
        + "\n"
        + "  - 此方法配合 suspend 使用\n"
        + "\n"
        + "- <b>isSuspender(address account)</b>\n"
        + "\n"
        + "  是否有暂停合约权限\n"
        + "\n"
        + "  - 此方法配合  addsuspender 使用\n"
        + "\n"
        + "- <b>addSuspender(address account)</b>\n"
        + "\n"
        + "  增加暂停权限者\n"
        + "\n"
        + "  - 此方法配合 renouncesuspender / issuspender 放啊发使用\n"
        + "\n"
        + "- <b>renounceSuspender()</b>\n"
        + "\n"
        + "  移除暂停权限\n"
        + "\n"
        + "- <b>destroy(uint256 assetId, bytes data)</b>\n"
        + "\n"
        + "  减少自己的资产，data 是转账备注\n"
        + "\n"
        + "  - 调用时，value 值需要小于等于目前自己的资产总量\n"
        + "\n"
        + "- <b>assetOfOwnerByIndex(address owner, uint256 index)</b>\n"
        + "\n"
        + "  根据索引 index 获取 owner 的资产 ID\n"
        + "\n"
        + "- <b>assetByIndex(uint256 index)</b>\n"
        + "\n"
        + "  根据索引  index 获取当前合约的资产 ID\n"
        + "\n";
    public static final String BAC001_MD = "# 积分合约\n" +
        "\n" +
        "## 简介\n" +
        " BAC001 是一套区块链积分合约，具有积分相关的增发，销毁，暂停合约，黑白名单等权限控制等功能。\n" +
        "\n" +
        "## 四个基本元素\n" +
        "\n" +
        "- description \n" +
        "\n" +
        "  此积分的具体描述\n" +
        "\n" +
        "- shortName \n" +
        "\n" +
        "  积分简称\n" +
        "\n" +
        "- minUnit \n" +
        "\n" +
        "  积分最小单位\n" +
        "\n" +
        "- totalAmount \n" +
        "\n" +
        "  积分总数量\n" +
        "\n" +
        "## 五个基本行为: \n" +
        "\n" +
        "- 发行\n" +
        "\n" +
        "  调用合约的 deploy 方法，传入你初始化的四个元素即可，即在区块链上发行了你指定总量和名称的积分。\n" +
        "\n" +
        "  - 其中 minUnit 和 totalAmount 不能为负数或小数\n" +
        "\n" +
        "- 转账\n" +
        "\n" +
        "  调用 send 方法即可实现转账，之后调用 balance 方法可以查看自己的积分余额\n" +
        "\n" +
        "- 增发\n" +
        "\n" +
        "  调用 issue 方法特定地址增发积分， 并可以通过 addIssuer 增加有权限增发积分的人，也可以通过renounceIssuer 方法移除增发权限\n" +
        "\n" +
        "- 销毁\n" +
        "\n" +
        "  调用 destroy 以及 destroyFrom 销毁自己地址下积分和特定地址下的积分\n" +
        "\n" +
        "- 暂停\n" +
        "\n" +
        "  遇到紧急状况，你可以调用 suspend 方法，暂停合约，这样任何人都不能调用 send 函数。故障修复后，可以调用 unSuspend 方法解除暂停。也可以通过 addSuspender 和 renounceSuspender 相应增加和移除暂停者权限\n" +
        "\n" +
        "\n" +
        "## 接口说明\n" +
        "\n" +
        "- <b>totalAmount()</b>\n" +
        "\n" +
        "  返回积分总量\n" +
        "\n" +
        "  - 这里的积分总量需要计算最小转账单位，所以实际返回值为   totalAmount * 10<sup>minUnit</sup> \n" +
        "\n" +
        "- <b>balance(address owner)</b>\n" +
        "\n" +
        "  返回owner的帐户的积分余额\n" +
        "\n" +
        "- <b>send(address to, uint256 value , string data)</b>\n" +
        "\n" +
        "  将数量为value的积分转入地址 to 并触发 transfer 事件, data 是转账备注\n" +
        "\n" +
        "  - suspend 状态下无法进行此操作\n" +
        "  - 请避免 to 为自身进行操作\n" +
        "\n" +
        "- <b>sendFrom(address from,address to,uint256 value，string  data))</b>\n" +
        "\n" +
        "  将地址 from 中的 value 数量的积分转入地址 to ，并触发 transfer 事件，data 是转账备注。\n" +
        "\n" +
        "  - 方法的调用者可以不为 from， 此时需要预先进行 approve 授权\n" +
        "\n" +
        "  - from 不能为调用者自身地址，否则会报错\n" +
        "  - suspend 状态下无法执行此操作\n" +
        "\n" +
        "- <b>safeSendFrom(address from, address to, uint256 value,  string data)</b>\n" +
        "\n" +
        "  安全的将地址 from 中的 value 数量的积分转入地址 to ( to如果是合约地址，必须实现接收接口 BAC001Holder 才可以接收转账) ，并触发 transfer 事件，data 是转账备注\n" +
        "\n" +
        "  - suspend 状态下无法执行此操作\n" +
        "\n" +
        "- <b>safeBatchSend( address[] to, uint256[]  values, string  data)</b>\n" +
        "\n" +
        "  批量将自己账户下的积分转给 to 数组的地址， to 和 values 的个数要一致\n" +
        "\n" +
        "  - suspend 状态下无法执行此操作\n" +
        "\n" +
        "- <b>approve(address spender,uint256 value)</b>\n" +
        "\n" +
        "  允许 spender 从自己账户提取限额 value 的积分\n" +
        "\n" +
        "  - 此方法配合 sendfrom / safesendfrom 一起使用\n" +
        "  - 重复授权时，最终授权额度为最后一次授权的值\n" +
        "\n" +
        "- <b>allowance(address owner,address spender)</b>\n" +
        "\n" +
        "  返回 spender 可从 owner 提取的积分数量上限\n" +
        "\n" +
        "  - 此方法配合 approve 一起使用\n" +
        "\n" +
        "- <b>increaseAllowance(address spender, uint256 addedValue)</b>\n" +
        "\n" +
        "  允许 spender 提取的积分上限在原有基础上增加 addedValue\n" +
        "\n" +
        "  - 此方法配合 approve 使用\n" +
        "\n" +
        "- <b>decreaseAllowance(address spender, uint256 subtractedValue)</b>\n" +
        "\n" +
        "  允许 spender  提取的积分上限在原有基础上减少 subtractedValue\n" +
        "\n" +
        "  - 此方法配合 approve 使用\n" +
        "\n" +
        "- <b>minUnit()</b>\n" +
        "\n" +
        "  积分最小单位\n" +
        "\n" +
        "- <b>shortName()</b>\n" +
        "\n" +
        "  积分简称\n" +
        "\n" +
        "- <b>description()</b>\n" +
        "\n" +
        "  积分描述\n" +
        "\n" +
        "- <b>destroy(uint256 value， string  data)</b>\n" +
        "\n" +
        "  减少自己的积分，data 是转账备注\n" +
        "\n" +
        "  - 调用时，value 值需要小于等于目前自己的积分总量\n" +
        "\n" +
        "- <b>destroyFrom(address from, uint256 value， string  data)</b>\n" +
        "\n" +
        "  减少地址 from 积分，data 是转账备注\n" +
        "\n" +
        "  - 调用此方法时，需要配合 approve 进行使用\n" +
        "\n" +
        "- <b>issue(address to, uint256 value，string  data)</b>\n" +
        "\n" +
        "  给地址 to 增加数量为 value 的积分，data 是转账备注\n" +
        "\n" +
        "- <b>isIssuer(address account)</b>\n" +
        "\n" +
        "  检查 account 是否有增加积分的权限\n" +
        "\n" +
        "- <b>addIssuer(address account)</b>\n" +
        "\n" +
        "  使地址 account 拥有增加积分的权限\n" +
        "\n" +
        "- <b>renounceIssuer()</b>\n" +
        "\n" +
        "  移除增加积分的权限\n" +
        "\n" +
        "- <b>suspend()</b>\n" +
        "\n" +
        "  暂停合约\n" +
        "\n" +
        "  - suspend 后无法进行 send / safesendfrom / sendfrom / safeBatchSend / approves 操作\n" +
        "\n" +
        "- <b>unSuspend()</b>\n" +
        "\n" +
        "  重启合约\n" +
        "  \n" +
        "- <b>suspended</b>\n" +
        "\n" +
        "  判断合约是否处于暂停状态\n" +
        "\n" +
        "- <b>isSuspender(address account)</b>\n" +
        "\n" +
        "  是否有暂停合约权限\n" +
        "\n" +
        "  - 配合 suspend 方法一起使用\n" +
        "\n" +
        "- <b>addSuspender(address account)</b>\n" +
        "\n" +
        "  增加暂停权限者\n" +
        "\n" +
        "  - 配合 suspend 方法一起使用\n" +
        "\n" +
        "- <b>renounceSuspender()</b>\n" +
        "\n" +
        "  移除暂停权限\n" +
        "\n" +
        "  - 配合 suspend / addSuspender 方法使用\n" +
        "\n" +
        "\n";


}

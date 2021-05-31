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

    @Test
    public void testToolMdToBase64() {
        System.out.println("ADDRESS_MD:\n" + Base64.getEncoder().encodeToString(ADDRESS_MD.getBytes()));
        System.out.println("LIB_STRING_MD:\n" + Base64.getEncoder().encodeToString(LIB_STRING_MD.getBytes()));
        System.out.println("SAFE_MATH_MD:\n" + Base64.getEncoder().encodeToString(SAFE_MATH_MD.getBytes()));
        System.out.println("TABLE_MD:\n" + Base64.getEncoder().encodeToString(TABLE_MD.getBytes()));
        System.out.println("ROLES_MD:\n" + Base64.getEncoder().encodeToString(ROLES_MD.getBytes()));
    }

    @Test
    public void testEvidenceMdToBase64() {
        System.out.println("EVIDENCE_MD:\n" + Base64.getEncoder().encodeToString(EVIDENCE_MD.getBytes()));
    }


    public static final String EVIDENCE_MD = "# Evidence 合约\n" +
        "\n" +
        "## 简介\n" +
        "Evidence 示例合约，使用分层的智能合约结构： \n" +
        "1）工厂合约（EvidenceSignersData.sol），由存证各方事前约定，存储存证生效条件，并管理存证的生成。  \n" +
        "2）存证合约（Evidence.sol），由工厂合约生成，存储存证id，hash和各方签名（每张存证一个合约）。  ";

    @Test
    public void testBACMdToBase64() {
        System.out.println("ADDRESS_MD:\n" + Base64.getEncoder().encodeToString(ADDRESS_MD.getBytes()));
        System.out.println("SAFE_MATH_MD:\n" + Base64.getEncoder().encodeToString(SAFE_MATH_MD.getBytes()));
        System.out.println("ROLES_MD:\n" + Base64.getEncoder().encodeToString(ROLES_MD.getBytes()));
        System.out.println("BAC001_MD:\n" + Base64.getEncoder().encodeToString(BAC001_MD.getBytes()));
    }

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
        "  调用 destory 以及 destoryFrom 销毁自己地址下积分和特定地址下的积分\n" +
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
        "- <b>destory(uint256 value， string  data)</b>\n" +
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

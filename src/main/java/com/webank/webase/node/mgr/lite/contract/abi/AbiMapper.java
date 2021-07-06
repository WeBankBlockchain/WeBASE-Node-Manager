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

package com.webank.webase.node.mgr.lite.contract.abi;

import com.webank.webase.node.mgr.lite.contract.abi.entity.AbiInfo;
import com.webank.webase.node.mgr.lite.contract.abi.entity.ReqAbiListParam;
import com.webank.webase.node.mgr.lite.contract.abi.entity.RspAllContract;
import com.webank.webase.node.mgr.lite.contract.abi.entity.AbiInfo;
import com.webank.webase.node.mgr.lite.contract.abi.entity.ReqAbiListParam;
import com.webank.webase.node.mgr.lite.contract.abi.entity.RspAllContract;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AbiMapper {

	List<AbiInfo> listOfAbi(ReqAbiListParam param);

	Integer countOfAbi(ReqAbiListParam param);

	AbiInfo queryByAbiId(@Param("abiId") int abiId);

	AbiInfo queryByGroupIdAndAddress(@Param("groupId") int groupId,
			 @Param("account") String account,
			 @Param("contractAddress") String contractAddress);

	AbiInfo queryByGroupIdAndContractName(@Param("groupId") int groupId,
			 @Param("account") String account,
			 @Param("contractName") String contractName);

	void add(AbiInfo abiInfo);

	void update(AbiInfo abiInfo);

	void deleteByAbiId(@Param("abiId") int abiId);

	void deleteByGroupId(@Param("groupId") int groupId);

	List<RspAllContract> listAllContract(ReqAbiListParam param);

	/**
	 * support query by bin
	 * @param param
	 * @return
	 */
	AbiInfo getAbiByBin(ReqAbiListParam param);

}

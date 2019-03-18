/*
 * Copyright 2014-2019  the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webank.webase.node.mgr.monitor;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * user transaction monitor.
 */
@Repository
public interface MonitorMapper {

    void addRow(TbMonitor tbMonitor);

    void updateRow(TbMonitor tbMonitor);

    void updateUnusualUser(@Param("networkId") Integer networkId,
        @Param("userName") String userName, @Param("address") String address);

    void updateUnusualContract(@Param("networkId") Integer networkId,
        @Param("contractName") String contractName,
        @Param("contractBin") String contractBin, @Param("interfaceName") String interfaceName,
        @Param("transUnusualType") int transUnusualType);

    String queryUnusualTxhash(@Param("networkId") Integer networkId,
        @Param("contractBin") String contractBin);

    TbMonitor queryTbMonitor(TbMonitor tbMonitor);

    List<TbMonitor> monitorUserList(@Param("networkId") Integer networkId);

    List<TbMonitor> monitorInterfaceList(@Param("networkId") Integer networkId,
        @Param("userName") String userName);

    Integer countOfMonitorTrans(Map<String, Object> queryParam);

    List<PageTransInfo> qureyTransCountList(Map<String, Object> queryParam);

    Integer countOfUnusualUser(@Param("networkId") Integer networkId,
        @Param("userName") String userName);

    List<UnusualUserInfo> listOfUnusualUser(Map<String, Object> queryParam);

    Integer countOfUnusualContract(@Param("networkId") Integer networkId,
        @Param("contractAddress") String contractAddress);

    List<UnusualContractInfo> listOfUnusualContract(Map<String, Object> queryParam);
}

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
package com.webank.webase.node.mgr.network;

import com.alibaba.fastjson.JSON;
import com.webank.webase.node.mgr.base.entity.ConstantCode;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import java.math.BigInteger;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * services for network data.
 */
@Log4j2
@Service
public class NetworkService {

    @Autowired
    private NetworkMapper networkMapper;

    /**
     * update network latest block number.
     */
    public void updateNetworkInfo(Integer networkId, BigInteger latestBlock)
        throws NodeMgrException {
        log.debug("start updateNetworkInfo networkId:{} latestBlock:{} ", networkId,
            latestBlock);
        try {
            Integer affectRow = networkMapper.updateNetworkInfo(networkId, latestBlock);
            if (affectRow == 0) {
                log.info(
                    "fail updateNetworkInfo. networkId:{}  latestBlock:{}. affect 0 rows"
                        + " of tb_network",
                    networkId, latestBlock);
                throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
            }
        } catch (RuntimeException ex) {
            log.debug("fail updateNetworkInfo networkId:{} latestBlock:{}", networkId,
                latestBlock, ex);
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }
    }

    /**
     * query count of network.
     */
    public Integer countOfNetwork(Integer networkId) throws NodeMgrException {
        log.debug("start countOfNetwork networkId:{}", networkId);
        try {
            Integer count = networkMapper.countOfNetwork(networkId);
            log.debug("end countOfNetwork networkId:{} count:{}", networkId, count);
            return count;
        } catch (RuntimeException ex) {
            log.error("fail countOfNetwork", ex);
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }
    }

    /**
     * query all network info.
     */
    public List<TbNetwork> getAllNetwork() throws NodeMgrException {
        log.debug("start getAllNetwork");
        // query network count
        Integer count = countOfNetwork(null);

        List<TbNetwork> listOfNetwork = null;
        if (count != null && count > 0) {
            try {
                // qurey network list
                listOfNetwork = networkMapper.listAllNetwork();
            } catch (RuntimeException ex) {
                log.error("fail countOfNetwork", ex);
                throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
            }
        }
        log.debug("end getAllNetwork listOfNetwork:{}", JSON.toJSONString(listOfNetwork));
        return listOfNetwork;
    }

    /**
     * Check the validity of the networkId.
     */
    public void checkNetworkId(Integer networkId) throws NodeMgrException {
        log.debug("start checkNetworkId networkId:{}", networkId);

        if (networkId == null) {
            log.error("fail checkNetworkId networkId is null");
            throw new NodeMgrException(ConstantCode.NETWORK_ID_NULL);
        }

        Integer networkCount = countOfNetwork(networkId);
        log.debug("checkNetworkId networkId:{} networkCount:{}", networkId, networkCount);
        if (networkCount == null || networkCount == 0) {
            throw new NodeMgrException(ConstantCode.INVALID_NETWORK_ID);
        }
        log.debug("end checkNetworkId");
    }

    /**
     * query latest statistical trans.
     */
    public List<StatisticalNetworkTransInfo> queryLatestStatisticalTrans() throws NodeMgrException {
        log.debug("start queryLatestStatisticalTrans");
        try {
            // qurey list
            List<StatisticalNetworkTransInfo> listStatisticalTrans = networkMapper
                .queryLatestStatisticalTrans();
            log.debug("end queryLatestStatisticalTrans listStatisticalTrans:{}",
                JSON.toJSONString(listStatisticalTrans));
            return listStatisticalTrans;
        } catch (RuntimeException ex) {
            log.error("fail queryLatestStatisticalTrans", ex);
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }
    }

    /**
     * query network overview information.
     */
    public NetworkGeneral queryNetworkGeneral(Integer networkId) throws NodeMgrException {
        log.debug("start queryNetworkGeneral networkId:{}", networkId);
        try {
            // qurey general info from tb_network
            NetworkGeneral generalInfo = networkMapper.queryNetworkGeneral(networkId);
            log.debug("end queryNetworkGeneral generalInfo:{}",
                JSON.toJSONString(generalInfo));
            return generalInfo;
        } catch (RuntimeException ex) {
            log.error("fail queryNetworkGeneral", ex);
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }
    }

    /**
     * reset trans count of network.
     */
    public void resetTransCount(Integer networkId) throws NodeMgrException {
        log.debug("start resetTransCount networkId:{}", networkId);
        try {
            networkMapper.resetTransCount(networkId);
        } catch (RuntimeException ex) {
            log.error("fail resetTransCount", ex);
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }
    }
}

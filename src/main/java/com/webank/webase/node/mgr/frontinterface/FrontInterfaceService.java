/**
 * Copyright 2014-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.webank.webase.node.mgr.frontinterface;

import static com.webank.webase.node.mgr.frontinterface.FrontRestTools.URI_CONTAIN_GROUP_ID;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.web3j.protocol.core.methods.response.NodeVersion;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.entity.BasePageResponse;
import com.webank.webase.node.mgr.base.entity.BaseResponse;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.base.properties.ConstantProperties;
import com.webank.webase.node.mgr.base.tools.JsonTools;
import com.webank.webase.node.mgr.block.entity.BlockInfo;
import com.webank.webase.node.mgr.event.entity.ContractEventInfo;
import com.webank.webase.node.mgr.event.entity.NewBlockEventInfo;
import com.webank.webase.node.mgr.front.entity.TotalTransCountInfo;
import com.webank.webase.node.mgr.frontinterface.entity.GenerateGroupInfo;
import com.webank.webase.node.mgr.frontinterface.entity.GroupHandleResult;
import com.webank.webase.node.mgr.frontinterface.entity.PostAbiInfo;
import com.webank.webase.node.mgr.frontinterface.entity.SyncStatus;
import com.webank.webase.node.mgr.monitor.ChainTransInfo;
import com.webank.webase.node.mgr.node.entity.PeerInfo;
import com.webank.webase.node.mgr.transaction.entity.TransactionInfo;
import com.webank.webase.node.mgr.user.entity.KeyPair;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class FrontInterfaceService {

    @Autowired
    private FrontRestTools frontRestTools;
    @Qualifier(value = "genericRestTemplate")
    @Autowired
    private RestTemplate genericRestTemplate;
    @Autowired
    private ConstantProperties cproperties;

    /**
     * request from specific front.
     */
    private <T> T requestSpecificFront(int groupId, String frontIp, Integer frontPort,
            HttpMethod method, String uri, Object param, Class<T> clazz) {
        log.debug(
                "start requestSpecificFront. groupId:{} frontIp:{} frontPort:{} "
                        + "httpMethod:{} uri:{}",
                groupId, frontIp, frontPort, method.toString(), uri);

        uri = FrontRestTools.uriAddGroupId(groupId, uri);
        String url = String.format(cproperties.getFrontUrl(), frontIp, frontPort, uri);
        log.debug("requestSpecificFront. url:{}", url);

        try {
            HttpEntity entity = FrontRestTools.buildHttpEntity(param);// build entity
            ResponseEntity<T> response = genericRestTemplate.exchange(url, method, entity, clazz);
            return response.getBody();
        } catch (HttpStatusCodeException ex) {
            JsonNode error = JsonTools.stringToJsonNode(ex.getResponseBodyAsString());
            log.error("http request:[{}] fail. error:{}", url, JsonTools.toJSONString(error));
            if (error == null) {
                log.error("deserialize http response error");
                throw new NodeMgrException(ConstantCode.REQUEST_FRONT_FAIL, ex);
            }
            try {
                int code = error.get("code").intValue();
                String errorMessage = error.get("errorMessage").asText();
                throw new NodeMgrException(code, errorMessage);
            } catch (NullPointerException e) {
                throw new NodeMgrException(ConstantCode.REQUEST_FRONT_FAIL, ex);
            }
        }
    }


    /**
     * get from specific front.
     */
    private <T> T getFromSpecificFront(int groupId, String frontIp, Integer frontPort, String uri,
            Class<T> clazz) {
        log.debug("start getFromSpecificFront. groupId:{} frontIp:{} frontPort:{}  uri:{}", groupId,
                frontIp, frontPort.toString(), uri);
        String url = String.format(cproperties.getFrontUrl(), frontIp, frontPort, uri);
        log.debug("getFromSpecificFront. url:{}", url);
        return requestSpecificFront(groupId, frontIp, frontPort, HttpMethod.GET, uri, null, clazz);
    }


    /**
     * send contract abi
     */
    public void sendAbi(int groupId, PostAbiInfo param) {
        log.debug("start sendAbi groupId:{} param:{}", groupId, JsonTools.toJSONString(param));

        frontRestTools.postForEntity(groupId, FrontRestTools.URI_CONTRACT_SENDABI, param,
                Object.class);
        log.debug("end sendAbi groupId:{} param:{}", groupId, JsonTools.toJSONString(param));

    }


    /**
     * get map's Cert Content from specific front.
     */
    public Map<String, String> getCertMapFromSpecificFront(String nodeIp, Integer frontPort) {
        int groupId = 1;
        return getFromSpecificFront(groupId, nodeIp, frontPort, FrontRestTools.URI_CERT, Map.class);
    }


    /**
     * get group list from specific front.
     */
    public List<String> getGroupListFromSpecificFront(String nodeIp, Integer frontPort) {
        Integer groupId = Integer.MAX_VALUE;
        return getFromSpecificFront(groupId, nodeIp, frontPort, FrontRestTools.URI_GROUP_PLIST, List.class);
    }


    /**
     * get groupPeers from specific front.
     */
    public List<String> getGroupPeersFromSpecificFront(String frontIp, Integer frontPort,
            Integer groupId) {
        return getFromSpecificFront(groupId, frontIp, frontPort, FrontRestTools.URI_GROUP_PEERS, List.class);
    }

    /**
     * get NodeIDList from specific front.
     */
    public List<String> getNodeIDListFromSpecificFront(String frontIp, Integer frontPort,
            Integer groupId) {
        return getFromSpecificFront(groupId, frontIp, frontPort, FrontRestTools.URI_NODEID_LIST, List.class);
    }

    /**
     * get peers from specific front.
     */
    public PeerInfo[] getPeersFromSpecificFront(String frontIp, Integer frontPort,
            Integer groupId) {
        return getFromSpecificFront(groupId, frontIp, frontPort, FrontRestTools.URI_PEERS, PeerInfo[].class);
    }

    /**
     * get peers from specific front.
     */
    public SyncStatus getSyncStatusFromSpecificFront(String frontIp, Integer frontPort,
            Integer groupId) {
        return getFromSpecificFront(groupId, frontIp, frontPort, FrontRestTools.URI_CSYNC_STATUS,
                SyncStatus.class);
    }

    public BlockInfo getBlockByNumberFromSpecificFront(String frontIp, Integer frontPort,
													   Integer groupId, BigInteger blockNumber) {
		log.debug("start getBlockByNumberFromSpecificFront frontIp:{},frontPort{}," +
				" groupId:{} blockNumber:{}", frontIp, frontPort, groupId, blockNumber);
		String uri = String.format(FrontRestTools.URI_BLOCK_BY_NUMBER, blockNumber);
		BlockInfo blockInfo = null;
		try{
            blockInfo = getFromSpecificFront(groupId, frontIp, frontPort, uri, BlockInfo.class);
        } catch (Exception ex) {
		    log.error("getBlockByNumberFromSpecificFront:{}", ex.getMessage());
        }
		return blockInfo;
	}

    /**
     * get peers.
     */
    public PeerInfo[] getPeers(Integer groupId) {
        return frontRestTools.getForEntity(groupId, FrontRestTools.URI_PEERS, PeerInfo[].class);
    }

    /**
     * get contract code.
     */
    public String getContractCode(Integer groupId, String address, BigInteger blockNumber)
            throws NodeMgrException {
        log.debug("start getContractCode groupId:{} address:{} blockNumber:{}", groupId, address,
                blockNumber);
        String uri = String.format(FrontRestTools.URI_CODE, address, blockNumber);
        String contractCode = frontRestTools.getForEntity(groupId, uri, String.class);
        log.debug("end getContractCode. contractCode:{}", contractCode);
        return contractCode;
    }

    /**
     * get transaction receipt.
     */
    public TransactionReceipt getTransReceipt(Integer groupId, String transHash) throws NodeMgrException {
        log.debug("start getTransReceipt groupId:{} transaction:{}", groupId, transHash);
        String uri = String.format(FrontRestTools.FRONT_TRANS_RECEIPT_BY_HASH_URI, transHash);
        TransactionReceipt transReceipt = frontRestTools.getForEntity(groupId, uri, TransactionReceipt.class);
        log.debug("end getTransReceipt");
        return transReceipt;
    }

    /**
     * get transaction by hash.
     */
    public TransactionInfo getTransaction(Integer groupId, String transHash)
            throws NodeMgrException {
        log.debug("start getTransaction groupId:{} transaction:{}", groupId, transHash);
        if (StringUtils.isBlank(transHash)) {
            return null;
        }
        String uri = String.format(FrontRestTools.URI_TRANS_BY_HASH, transHash);
        TransactionInfo transInfo =
                frontRestTools.getForEntity(groupId, uri, TransactionInfo.class);
        log.debug("end getTransaction");
        return transInfo;
    }

    /**
     * get block by number.
     */
    public BlockInfo getBlockByNumber(Integer groupId, BigInteger blockNumber)
            throws NodeMgrException {
        log.debug("start getBlockByNumber groupId:{} blockNumber:{}", groupId, blockNumber);
        String uri = String.format(FrontRestTools.URI_BLOCK_BY_NUMBER, blockNumber);
        BlockInfo blockInfo = null;
        try {
            blockInfo = frontRestTools.getForEntity(groupId, uri, BlockInfo.class);
        } catch (Exception ex) {
            log.error("fail getBlockByNumber,exception:[]", ex);
        }
        log.debug("end getBlockByNumber");
        return blockInfo;
    }


    /**
     * request front for block by hash.
     */
    public BlockInfo getblockByHash(Integer groupId, String pkHash) throws NodeMgrException {
        log.debug("start getblockByHash. groupId:{}  pkHash:{}", groupId, pkHash);
        String uri = String.format(FrontRestTools.URI_BLOCK_BY_HASH, pkHash);
        BlockInfo blockInfo = frontRestTools.getForEntity(groupId, uri, BlockInfo.class);
        log.debug("end getblockByHash. blockInfo:{}", JsonTools.toJSONString(blockInfo));
        return blockInfo;
    }


    /**
     * getTransFromFrontByHash.
     */
    public ChainTransInfo getTransInfoByHash(Integer groupId, String hash) throws NodeMgrException {
        log.debug("start getTransInfoByHash. groupId:{} hash:{}", groupId, hash);
        TransactionInfo trans = getTransaction(groupId, hash);
        if (Objects.isNull(trans)) {
            return null;
        }
        ChainTransInfo chainTransInfo = new ChainTransInfo(trans.getFrom(), trans.getTo(),
                trans.getInput(), trans.getBlockNumber());
        log.debug("end getTransInfoByHash:{}", JsonTools.toJSONString(chainTransInfo));
        return chainTransInfo;
    }

    /**
     * getAddressByHash.
     */
    public String getAddressByHash(Integer groupId, String transHash) throws NodeMgrException {
        log.debug("start getAddressByHash. groupId:{} transHash:{}", groupId, transHash);

        TransactionReceipt transReceipt = getTransReceipt(groupId, transHash);
        String contractAddress = transReceipt.getContractAddress();
        log.debug("end getAddressByHash. contractAddress{}", contractAddress);
        return contractAddress;
    }


    /**
     * get code from front.
     */
    public String getCodeFromFront(Integer groupId, String contractAddress, BigInteger blockNumber)
            throws NodeMgrException {
        log.debug("start getCodeFromFront. groupId:{} contractAddress:{} blockNumber:{}", groupId,
                contractAddress, blockNumber);
        String uri = String.format(FrontRestTools.URI_CODE, contractAddress, blockNumber);
        String code = frontRestTools.getForEntity(groupId, uri, String.class);

        log.debug("end getCodeFromFront:{}", code);
        return code;
    }

    /**
     * get total transaction count
     */
    public TotalTransCountInfo getTotalTransactionCount(Integer groupId) {
        log.debug("start getTotalTransactionCount. groupId:{}", groupId);
        TotalTransCountInfo totalCount = frontRestTools.getForEntity(groupId,
                FrontRestTools.URI_TRANS_TOTAL, TotalTransCountInfo.class);
        log.debug("end getTotalTransactionCount:{}", totalCount);
        return totalCount;
    }

    /**
     * get transaction hash by block number
     */
    public List<TransactionInfo> getTransByBlockNumber(Integer groupId, BigInteger blockNumber) {
        log.debug("start getTransByBlockNumber. groupId:{} blockNumber:{}", groupId, blockNumber);
        BlockInfo blockInfo = getBlockByNumber(groupId, blockNumber);
        if (blockInfo == null) {
            return null;
        }
        List<TransactionInfo> transInBLock = blockInfo.getTransactions();
        log.debug("end getTransByBlockNumber. transInBLock:{}", JsonTools.toJSONString(transInBLock));
        return transInBLock;
    }

    /**
     * get group peers
     */
    public List<String> getGroupPeers(Integer groupId) {
        log.debug("start getGroupPeers. groupId:{}", groupId);
        List<String> groupPeers = frontRestTools.getForEntity(groupId, FrontRestTools.URI_GROUP_PEERS, List.class);
        log.debug("end getGroupPeers. groupPeers:{}", JsonTools.toJSONString(groupPeers));
        return groupPeers;
    }

    /**
     * get group peers
     */
    public List<String> getObserverList(Integer groupId) {
        log.debug("start getObserverList. groupId:{}", groupId);
        List<String> observers =
                frontRestTools.getForEntity(groupId, FrontRestTools.URI_GET_OBSERVER_LIST, List.class);
        log.info("end getObserverList. observers:{}", JsonTools.toJSONString(observers));
        return observers;
    }

    /**
     * get observer list from specific front
     */
    public List<String> getObserverListFromSpecificFront(String frontIp, Integer frontPort,
            Integer groupId) {
        return getFromSpecificFront(groupId, frontIp, frontPort,
                FrontRestTools.URI_GET_OBSERVER_LIST, List.class);
    }

    /**
     * get consensusStatus
     */
    public String getConsensusStatus(Integer groupId) {
        log.debug("start getConsensusStatus. groupId:{}", groupId);
        String consensusStatus = frontRestTools.getForEntity(groupId,
                FrontRestTools.URI_CONSENSUS_STATUS, String.class);
        log.debug("end getConsensusStatus. consensusStatus:{}", consensusStatus);
        return consensusStatus;
    }

    /**
     * get syncStatus
     */
    public SyncStatus getSyncStatus(Integer groupId) {
        log.debug("start getSyncStatus. groupId:{}", groupId);
        SyncStatus ststus = frontRestTools.getForEntity(groupId, FrontRestTools.URI_CSYNC_STATUS,
                SyncStatus.class);
        log.debug("end getSyncStatus. ststus:{}", JsonTools.toJSONString(ststus));
        return ststus;
    }

    /**
     * get latest block number
     */
    public BigInteger getLatestBlockNumber(Integer groupId) {
        log.debug("start getLatestBlockNumber. groupId:{}", groupId);
        BigInteger latestBlockNmber = frontRestTools.getForEntity(groupId,
                FrontRestTools.URI_BLOCK_NUMBER, BigInteger.class);
        log.debug("end getLatestBlockNumber. latestBlockNmber:{}", latestBlockNmber);
        return latestBlockNmber;
    }

    /**
     * get sealerList.
     */
    public List<String> getSealerList(Integer groupId) {
        log.debug("start getSealerList. groupId:{}", groupId);
        List getSealerList = frontRestTools.getForEntity(groupId,
                FrontRestTools.URI_GET_SEALER_LIST, List.class);
        log.debug("end getSealerList. getSealerList:{}", JsonTools.toJSONString(getSealerList));
        return getSealerList;
    }

    /**
     * get sealer list from specific front
     */
    public List<String> getSealerListFromSpecificFront(String frontIp, Integer frontPort,
            Integer groupId) {
        return getFromSpecificFront(groupId, frontIp, frontPort, FrontRestTools.URI_GET_SEALER_LIST,
                List.class);
    }

    /**
     * get config by key
     */
    public String getSystemConfigByKey(Integer groupId, String key) {
        log.debug("start getSystemConfigByKey. groupId:{}", groupId);
        String uri = String.format(FrontRestTools.URI_SYSTEMCONFIG_BY_KEY, key);
        String config = frontRestTools.getForEntity(groupId, uri, String.class);
        log.debug("end getSystemConfigByKey. config:{}", config);
        return config;
    }

    /**
     * get front's encryptType
     */
    public Integer getEncryptTypeFromSpecificFront(String nodeIp, Integer frontPort) {
        log.debug("start getEncryptTypeFromSpecificFront. nodeIp:{},frontPort:{}", nodeIp,
                frontPort);
        Integer groupId = Integer.MAX_VALUE;
        int encryptType =
                getFromSpecificFront(groupId, nodeIp, frontPort, FrontRestTools.URI_ENCRYPT_TYPE, Integer.class);
        log.debug("end getEncryptTypeFromSpecificFront. encryptType:{}", encryptType);
        return encryptType;
    }

    public String getClientVersion(String frontIp, Integer frontPort,
                                   Integer groupId) {
        log.debug("start getClientVersion. groupId:{}", groupId);
        NodeVersion.Version clientVersion = getFromSpecificFront(groupId, frontIp, frontPort, FrontRestTools.URI_GET_CLIENT_VERSION, NodeVersion.Version.class);
        log.debug("end getClientVersion. consensusStatus:{}", clientVersion);
        return clientVersion.getVersion();
    }

    /**
     * generate group.
     */
    public GroupHandleResult generateGroup(String frontIp, Integer frontPort,
            GenerateGroupInfo param) {
        log.debug("start generateGroup frontIp:{} frontPort:{} param:{}", frontIp, frontPort, JsonTools.toJSONString(param));

        Integer groupId = Integer.MAX_VALUE;
        GroupHandleResult groupHandleResult = requestSpecificFront(groupId, frontIp, frontPort,
                HttpMethod.POST, FrontRestTools.URI_GENERATE_GROUP, param, GroupHandleResult.class);

        log.debug("end generateGroup");
        return groupHandleResult;
    }

    /**
     * start group.
     */
    public BaseResponse operateGroup(String frontIp, Integer frontPort, Integer groupId, String type) {
        log.debug("start operateGroup frontIp:{} frontPort:{} groupId:{}", frontIp, frontPort,
                groupId);
        String uri = String.format(FrontRestTools.URI_OPERATE_GROUP, type);
        BaseResponse response =
                getFromSpecificFront(groupId, frontIp, frontPort, uri, BaseResponse.class);
        log.debug("end operateGroup");
        return response;
    }

    /**
     * query group status list
     */
    public Map<String, String> queryGroupStatus(String frontIp, Integer frontPort, String nodeId, List<Integer> groupIdList) {
        log.debug("start queryGroupStatusList frontIp:{} frontPort:{} nodeId:{} groupIdList:{}",
                frontIp, frontPort, nodeId, groupIdList);
        int uselessGroupId = 1;
        Map<String, Object> param = new HashMap<>(1);
        param.put("groupIdList", groupIdList);
        BaseResponse response = requestSpecificFront(uselessGroupId, frontIp, frontPort,
                        HttpMethod.POST, FrontRestTools.URI_QUERY_GROUP_STATUS, param, BaseResponse.class);
        log.debug("end queryGroupStatusList");
        return (Map<String, String>) response.getData();
    }
    
    /**
     * refresh front.
     */
    public void refreshFront(String frontIp, Integer frontPort) {
        log.debug("start refreshFront frontIp:{} frontPort:{} ", frontIp, frontPort);
        Integer groupId = Integer.MAX_VALUE;
        getFromSpecificFront(groupId, frontIp, frontPort, FrontRestTools.URI_REFRESH_FRONT, Object.class);
        log.debug("end refreshFront");
    }

    /**
     * get new block info list
     */
    public List<NewBlockEventInfo> getNewBlockEventInfo(String frontIp, Integer frontPort, Integer groupId) {
        log.debug("start getNewBlockEventInfo frontIp:{} frontPort:{} groupId:{}",
                frontIp, frontPort, groupId);
        String newBlockInfoURI = FrontRestTools.URI_NEW_BLOCK_EVENT_INFO_LIST + "/" + groupId;
        URI_CONTAIN_GROUP_ID.add(newBlockInfoURI);
        BasePageResponse response = getFromSpecificFront(groupId, frontIp, frontPort, newBlockInfoURI,
                BasePageResponse.class);
        if (response.getData() == null) {
            return new ArrayList<>();
        }
        List data = (List) response.getData();
        List<NewBlockEventInfo> resList = JsonTools.toJavaObjectList(JsonTools.toJSONString(data), NewBlockEventInfo.class);
        resList.forEach(info -> info.setFrontInfo(frontIp));
        return resList;
    }

	public List<ContractEventInfo> getContractEventInfo(String frontIp, Integer frontPort, Integer groupId) {
		log.debug("start getContractEventInfo frontIp:{} frontPort:{} groupId:{}",
				frontIp, frontPort, groupId);
		String contractEventInfoURI = FrontRestTools.URI_CONTRACT_EVENT_INFO_LIST + "/" + groupId;
		URI_CONTAIN_GROUP_ID.add(contractEventInfoURI);
		BasePageResponse response = getFromSpecificFront(groupId, frontIp, frontPort, contractEventInfoURI,
				BasePageResponse.class);
		if (response.getData() == null) {
			return new ArrayList<>();
		}
		List data = (List) response.getData();
		List<ContractEventInfo> resList = JsonTools.toJavaObjectList(JsonTools.toJSONString(data), ContractEventInfo.class);
		resList.forEach(info -> info.setFrontInfo(frontIp));
		return resList;
	}

	public List<KeyPair> getKeyStoreList(Integer groupId, String frontIp, Integer frontPort) {
        List data = getFromSpecificFront(groupId, frontIp, frontPort,
                FrontRestTools.URI_KEY_PAIR_LOCAL_KEYSTORE, List.class);
        List<KeyPair> resList = JsonTools.toJavaObjectList(JsonTools.toJSONString(data), KeyPair.class);
        return resList;
    }

    public List<String> getNodeIdList(int groupId) {
        return frontRestTools.getForEntity(groupId, FrontRestTools.URI_NODEID_LIST, List.class);

    }
}

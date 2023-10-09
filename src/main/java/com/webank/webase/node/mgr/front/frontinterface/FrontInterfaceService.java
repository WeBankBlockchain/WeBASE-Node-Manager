/**
 * Copyright 2014-2021 the original author or authors.
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
package com.webank.webase.node.mgr.front.frontinterface;

import static com.webank.webase.node.mgr.front.frontinterface.FrontRestTools.URI_CONTAIN_GROUP_ID;

import com.fasterxml.jackson.databind.JsonNode;
import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.entity.BasePageResponse;
import com.webank.webase.node.mgr.base.entity.BaseResponse;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.cert.entity.SdkCertInfo;
import com.webank.webase.node.mgr.config.properties.ConstantProperties;
import com.webank.webase.node.mgr.contract.entity.Contract;
import com.webank.webase.node.mgr.contract.entity.RspCompileTask;
import com.webank.webase.node.mgr.event.entity.ContractEventInfo;
import com.webank.webase.node.mgr.event.entity.NewBlockEventInfo;
import com.webank.webase.node.mgr.event.entity.ReqEventLogList;
import com.webank.webase.node.mgr.front.frontinterface.entity.NodeStatusInfo;
import com.webank.webase.node.mgr.front.frontinterface.entity.PostAbiInfo;
import com.webank.webase.node.mgr.front.frontinterface.entity.ReqCompileTask;
import com.webank.webase.node.mgr.front.frontinterface.entity.ReqSdkConfig;
import com.webank.webase.node.mgr.front.frontinterface.entity.RspStatBlock;
import com.webank.webase.node.mgr.monitor.entity.ChainTransInfo;
import com.webank.webase.node.mgr.tools.HttpRequestTools;
import com.webank.webase.node.mgr.tools.JsonTools;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.sdk.v3.client.protocol.model.JsonTransactionResponse;
import org.fisco.bcos.sdk.v3.client.protocol.response.BcosBlock;
import org.fisco.bcos.sdk.v3.client.protocol.response.BcosBlock.TransactionResult;
import org.fisco.bcos.sdk.v3.client.protocol.response.BcosGroupNodeInfo.GroupNodeInfo;
import org.fisco.bcos.sdk.v3.client.protocol.response.ConsensusStatus.ConsensusStatusInfo;
import org.fisco.bcos.sdk.v3.client.protocol.response.Peers;
import org.fisco.bcos.sdk.v3.client.protocol.response.SealerList.Sealer;
import org.fisco.bcos.sdk.v3.client.protocol.response.SyncStatus.PeersInfo;
import org.fisco.bcos.sdk.v3.client.protocol.response.SyncStatus.SyncStatusInfo;
import org.fisco.bcos.sdk.v3.client.protocol.response.TotalTransactionCount.TransactionCountInfo;
import org.fisco.bcos.sdk.v3.model.NodeVersion.ClientVersion;
import org.fisco.bcos.sdk.v3.model.TransactionReceipt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Log4j2
@Service
public class FrontInterfaceService {

    @Lazy
    @Autowired
    private FrontRestTools frontRestTools;
    @Qualifier(value = "genericRestTemplate")
    @Autowired
    private RestTemplate genericRestTemplate;
    @Autowired
    private ConstantProperties cproperties;

    private final String GROUPID = "group0";

    /**
     * request from specific front.
     */
    private <T> T requestSpecificFront(String groupId, String frontIp, Integer frontPort,
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
        } catch (ResourceAccessException e) {
            log.error("requestSpecificFront. ResourceAccessException:{}", e);
            throw new NodeMgrException(ConstantCode.REQUEST_FRONT_FAIL);
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
    private <T> T getFromSpecificFront(String groupId, String frontIp, Integer frontPort,
        String uri,
        Class<T> clazz) {
        log.debug("start getFromSpecificFront. groupId:{} frontIp:{} frontPort:{}  uri:{}", groupId,
            frontIp, frontPort.toString(), uri);
        String url = String.format(cproperties.getFrontUrl(), frontIp, frontPort, uri);
        log.debug("getFromSpecificFront. url:{}", url);
        return requestSpecificFront(groupId, frontIp, frontPort, HttpMethod.GET, uri, null, clazz);
    }

    private <T> T postFromSpecificFront(String groupId, String frontIp, Integer frontPort,
        String uri, Object param,
        Class<T> clazz) {
        log.debug("start postFromSpecificFront. groupId:{} frontIp:{} frontPort:{}  uri:{}",
            groupId,
            frontIp, frontPort.toString(), uri);
        String url = String.format(cproperties.getFrontUrl(), frontIp, frontPort, uri);
        log.debug("postFromSpecificFront. url:{}", url);
        return requestSpecificFront(groupId, frontIp, frontPort, HttpMethod.POST, uri, param,
            clazz);
    }

    /**
     * send contract abi
     */
    public void sendAbi(String groupId, PostAbiInfo param) {
        log.debug("start sendAbi groupId:{} param:{}", groupId, JsonTools.toJSONString(param));
        frontRestTools.postForEntity(groupId, FrontRestTools.URI_CONTRACT_SENDABI, param,
            Object.class);
        log.debug("end sendAbi groupId:{} param:{}", groupId, JsonTools.toJSONString(param));

    }


    /**
     * get map's Cert Content from specific front.
     */
    public Map<String, String> getSdkFilesFromSpecificFront(String nodeIp, Integer frontPort) {
        String groupId = GROUPID;
        return getFromSpecificFront(groupId, nodeIp, frontPort, FrontRestTools.URI_CERT_SDK_FILES,
            Map.class);
    }


    /**
     * get group list from specific front.
     */
    public List<String> getGroupListFromSpecificFront(String nodeIp, Integer frontPort) {
//        String groupId = Integer.MAX_VALUE;
        String groupId = GROUPID;
        List<String> resList = getFromSpecificFront(groupId, nodeIp, frontPort,
            FrontRestTools.URI_GROUP_PLIST, List.class);
        if (resList.isEmpty()) {
            throw new NodeMgrException(ConstantCode.SYSTEM_ERROR_GROUP_LIST_EMPTY);
        }
        return resList;
    }


    /**
     * get groupPeers from specific front.
     */
    public List<String> getGroupPeersFromSpecificFront(String frontIp, Integer frontPort,
        String groupId) {
        return getFromSpecificFront(groupId, frontIp, frontPort, FrontRestTools.URI_GROUP_PEERS,
            List.class);
    }


    /**
     * get peers from specific front.
     */
    public Peers.PeersInfo getPeersFromSpecificFront(String frontIp, Integer frontPort,
        String groupId) {
        return getFromSpecificFront(groupId, frontIp, frontPort, FrontRestTools.URI_PEERS,
            Peers.PeersInfo.class);
    }

    /**
     * get peers from specific front.
     */
    public SyncStatusInfo getSyncStatusFromSpecificFront(String frontIp, Integer frontPort,
        String groupId) {
        return getFromSpecificFront(groupId, frontIp, frontPort, FrontRestTools.URI_SYNC_STATUS,
            SyncStatusInfo.class);
    }

    public List<String> getSealerObserverFromSpecificFront(String frontIp, Integer frontPort,
        String groupId) {
        List<String> groupPeerList = new ArrayList<>();
        SyncStatusInfo syncStatusInfo = this.getSyncStatusFromSpecificFront(frontIp, frontPort,
            groupId);
        groupPeerList.add(syncStatusInfo.getNodeId());
        groupPeerList.addAll(syncStatusInfo.getPeers().stream().map(PeersInfo::getNodeId).collect(
            Collectors.toList()));
        return groupPeerList;
    }

    public ConsensusStatusInfo getConsensusStatusFromSpecificFront(String frontIp,
        Integer frontPort,
        String groupId) {
        return getFromSpecificFront(groupId, frontIp, frontPort,
            FrontRestTools.URI_CONSENSUS_STATUS,
            ConsensusStatusInfo.class);
    }

    public BcosBlock.Block getBlockByNumberFromSpecificFront(String frontIp, Integer frontPort,
        String groupId, BigInteger blockNumber) {
        log.debug("start getBlockByNumberFromSpecificFront frontIp:{},frontPort{}," +
            " groupId:{} blockNumber:{}", frontIp, frontPort, groupId, blockNumber);
        String uri = String.format(FrontRestTools.URI_BLOCK_BY_NUMBER, blockNumber);
        BcosBlock.Block block = null;
        // catch error to avoid task abort
        try {
            block = getFromSpecificFront(groupId, frontIp, frontPort, uri, BcosBlock.Block.class);
        } catch (Exception ex) {
            log.error("getBlockByNumberFromSpecificFront:{}", ex.getMessage());
        }
        return block;
    }

    public Integer getEncryptTypeFromSpecificFront(String frontIp, Integer frontPort,
        String groupId) {
        Integer encryptType = getFromSpecificFront(groupId, frontIp, frontPort,
            FrontRestTools.URI_ENCRYPT_TYPE, Integer.class);
        return encryptType;
    }

    public Integer getCryptoType(String groupId) {
        return frontRestTools.getForEntity(groupId, FrontRestTools.URI_ENCRYPT_TYPE,
                Integer.class);
    }

    public Boolean getUseSmSsl(String groupId) {
        return frontRestTools.getForEntity(groupId, FrontRestTools.URI_USE_SM_SSL,
                Boolean.class);
    }

    public Boolean getIsWasmFromSpecificFront(String frontIp, Integer frontPort, String groupId) {
        Boolean encryptType = getFromSpecificFront(groupId, frontIp, frontPort,  FrontRestTools.URI_IS_WASM, Boolean.class);
        return encryptType;
    }

    /**
     * liquid related
     */
    public BaseResponse checkLiquidEnvFromSpecificFront(String frontIp, Integer frontPort) {
        String groupId = String.valueOf(Integer.MAX_VALUE);
        return getFromSpecificFront(groupId, frontIp, frontPort, FrontRestTools.URI_CONTRACT_LIQUID_CHECK,
            BaseResponse.class);
    }

    public RspCompileTask compileLiquidFromFront(String frontIp, Integer frontPort, Integer frontId,
                                                 Contract param) {
        // 拼接frontid，避免路径在front的文件里冲突
        String contractPath = param.getContractPath();
        if ("/".equals(contractPath)) {
            param.setContractPath("mgr_" + frontId);
        } else {
            param.setContractPath("mgr_" + contractPath + frontId);
        }
        log.info("start compileLiquidFromFront frontIp:{} frontPort:{} param:{}", frontIp, frontPort, JsonTools.toJSONString(param));

        BaseResponse response = requestSpecificFront(param.getGroupId(), frontIp, frontPort,
            HttpMethod.POST, FrontRestTools.URI_CONTRACT_LIQUID_COMPILE, param, BaseResponse.class);
        RspCompileTask task = JsonTools.stringToObj(JsonTools.toJSONString(response.getData()), RspCompileTask.class);
        log.info("end compileLiquidFromFront, response:{}", response);
        return task;
    }

    public RspCompileTask checkCompileLiquidFromFront(String frontIp, Integer frontPort, Integer frontId,
                                                    String groupId, String contractPath, String contractName) {
        // 拼接frontid，避免路径在front的文件里冲突
        if ("/".equals(contractPath)) {
            contractPath = "mgr_" + frontId;
        } else {
            contractPath = "mgr_" + contractPath + frontId;
        }
        log.info("start checkCompileLiquidFromFront frontIp:{} frontPort:{},groupId:{},contractPath:{},contractName:{}", frontIp, frontPort,
            groupId, contractPath, contractName);
        ReqCompileTask param = new ReqCompileTask(groupId, contractPath, contractName);
        BaseResponse response = requestSpecificFront(groupId, frontIp, frontPort,
            HttpMethod.POST, FrontRestTools.URI_CONTRACT_LIQUID_COMPILE_CHECK, param, BaseResponse.class);
        RspCompileTask task = JsonTools.stringToObj(JsonTools.toJSONString(response.getData()), RspCompileTask.class);

        log.info("end checkCompileLiquidFromFront, response:{}", response);
        return task;
    }
    /**
     * get peers.
     */
    public Peers.PeersInfo getPeers(String groupId) {
        return frontRestTools.getForEntity(groupId, FrontRestTools.URI_PEERS,
            Peers.PeersInfo.class);
    }

    /**
     * include node name list
     */
    public List<NodeStatusInfo> getNodeStatusList(String groupId) {
        List nodeStatusList = frontRestTools.getForEntity(groupId,
            FrontRestTools.URI_NODE_STATUS_LIST, List.class);
        List<NodeStatusInfo> resList = JsonTools.toJavaObjectList(
            JsonTools.toJSONString(nodeStatusList), NodeStatusInfo.class);
        return resList;
    }


    /**
     * get transaction receipt.
     */
    public TransactionReceipt getTransReceipt(String groupId, String transHash)
        throws NodeMgrException {
        log.debug("start getTransReceipt groupId:{} transaction:{}", groupId, transHash);
        String uri = String.format(FrontRestTools.FRONT_TRANS_RECEIPT_BY_HASH_URI, transHash);
        TransactionReceipt transReceipt = frontRestTools.getForEntity(groupId, uri,
            TransactionReceipt.class);
        log.debug("end getTransReceipt");
        return transReceipt;
    }

    /**
     * get transaction by hash.
     */
    public JsonTransactionResponse getTransaction(String groupId, String transHash)
        throws NodeMgrException {
        log.debug("start getTransaction groupId:{} transaction:{}", groupId, transHash);
        if (StringUtils.isBlank(transHash)) {
            return null;
        }
        String uri = String.format(FrontRestTools.URI_TRANS_BY_HASH, transHash);
        JsonTransactionResponse transInfo =
            frontRestTools.getForEntity(groupId, uri, JsonTransactionResponse.class);
        log.debug("end getTransaction");
        return transInfo;
    }

    /**
     * get block by number.
     */
    public BcosBlock.Block getBlockByNumber(String groupId, BigInteger blockNumber)
        throws NodeMgrException {
        log.debug("start getBlockByNumber groupId:{} blockNumber:{}", groupId, blockNumber);
        String uri = String.format(FrontRestTools.URI_BLOCK_BY_NUMBER, blockNumber);
        BcosBlock.Block blockInfo = null;
        // catch error to avoid task abort
        try {
            blockInfo = frontRestTools.getForEntity(groupId, uri, BcosBlock.Block.class);
        } catch (Exception ex) {
            log.error("fail getBlockByNumber,exception:[]", ex);
        }
        log.debug("end getBlockByNumber");
        return blockInfo;
    }


    /**
     * request front for block by hash.
     */
    public BcosBlock.Block getBlockByHash(String groupId, String blockHash)
        throws NodeMgrException {
        log.debug("start getBlockByHash. groupId:{}  blockHash:{}", groupId, blockHash);
        String uri = String.format(FrontRestTools.URI_BLOCK_BY_HASH, blockHash);
        BcosBlock.Block blockInfo = frontRestTools.getForEntity(groupId, uri,
            BcosBlock.Block.class);
        log.debug("end getBlockByHash. blockInfo:{}", JsonTools.toJSONString(blockInfo));
        return blockInfo;
    }


    /**
     * getTransFromFrontByHash.
     */
    public ChainTransInfo getTransInfoByHash(String groupId, String hash) throws NodeMgrException {
        log.debug("start getTransInfoByHash. groupId:{} hash:{}", groupId, hash);
        TransactionReceipt receipt = getTransReceipt(groupId, hash);
        if (Objects.isNull(receipt) || StringUtils.isBlank(receipt.getTransactionHash())) {
            return null;
        }
        ChainTransInfo chainTransInfo = new ChainTransInfo(receipt.getFrom(), receipt.getTo(),
            receipt.getInput(), receipt.getBlockNumber());
        log.debug("end getTransInfoByHash:{}", JsonTools.toJSONString(chainTransInfo));
        return chainTransInfo;
    }

    /**
     * getAddressByHash.
     */
    public String getAddressByHash(String groupId, String transHash) throws NodeMgrException {
        log.debug("start getAddressByHash. groupId:{} transHash:{}", groupId, transHash);

        TransactionReceipt transReceipt = getTransReceipt(groupId, transHash);
        String contractAddress = transReceipt.getContractAddress();
        log.debug("end getAddressByHash. contractAddress:{}", contractAddress);
        return contractAddress;
    }


    /**
     * get code from front.
     */
    @Deprecated
    public String getCodeFromFront(String groupId, String contractAddress, BigInteger blockNumber)
        throws NodeMgrException {
        log.debug("start getCodeFromFront. groupId:{} contractAddress:{} blockNumber:{}", groupId,
            contractAddress, blockNumber);
        String uri = String.format(FrontRestTools.URI_CODE, contractAddress, blockNumber);
        String code = frontRestTools.getForEntity(groupId, uri, String.class);

        log.debug("end getCodeFromFront:{}", code);
        return code;
    }

    /**
     * get code from front by get param (not path param
     */
    public String getCodeV2FromFront(String groupId, String contractAddress, BigInteger blockNumber)
            throws NodeMgrException {
        log.debug("start getCodeV2FromFront. groupId:{} contractAddress:{} blockNumber:{}", groupId,
                contractAddress, blockNumber);
        Map<String, String> map = new HashMap<>();
        map.put("address", contractAddress);
        map.put("blockNumber", blockNumber.toString(10));
        String uri = HttpRequestTools.getQueryUri(FrontRestTools.URI_CODE_V2, map);
        try {
            String code = frontRestTools.getForEntity(groupId, uri, String.class);
            log.debug("end getCodeV2FromFront:{}", code);
            return code;
        } catch (NodeMgrException ex) {
            String attachment = ex.getRetCode().getAttachment();
            if (StringUtils.isNotBlank(attachment) && attachment.contains("null")) {
                log.error("getCode return null, contract not exist on chain!");
                return null;
            }
            throw ex;
        }

    }

    /**
     * get total transaction count
     *
     * @param groupId
     */
    public TransactionCountInfo getTotalTransactionCount(String groupId) {
        log.debug("start getTotalTransactionCount. groupId:{}", groupId);
        TransactionCountInfo totalCount = frontRestTools.getForEntity(groupId,
            FrontRestTools.URI_TRANS_TOTAL, TransactionCountInfo.class);
        log.debug("end getTotalTransactionCount:{}", totalCount);
        return totalCount;
    }

    /**
     * get transaction hash by block number
     */
    public List<JsonTransactionResponse> getTransByBlockNumber(String groupId,
        BigInteger blockNumber) {
        log.debug("start getTransByBlockNumber. groupId:{} blockNumber:{}", groupId, blockNumber);
        BcosBlock.Block blockInfo = getBlockByNumber(groupId, blockNumber);
        if (blockInfo == null) {
            return null;
        }
        List<JsonTransactionResponse> transactionResponses = new ArrayList<>();
        //TransactionResult->TransactionObject
        List<TransactionResult> transInBLock = blockInfo.getTransactions();
        for (TransactionResult<JsonTransactionResponse> t : transInBLock) {
            JsonTransactionResponse tran = t.get();
            transactionResponses.add(tran);
        }
        log.debug("end getTransByBlockNumber. transInBLock:{}",
            JsonTools.toJSONString(transInBLock));
        return transactionResponses;
    }

    /**
     * get group peers
     *
     * @param groupId
     */
    public List<String> getGroupPeers(String groupId) {
        log.debug("start getGroupPeers. groupId:{}", groupId);
        List<String> groupPeers = frontRestTools.getForEntity(groupId,
            FrontRestTools.URI_GROUP_PEERS, List.class);
        log.debug("end getGroupPeers. groupPeers:{}", JsonTools.toJSONString(groupPeers));
        return groupPeers;
    }

    /**
     * get group peers
     */
    public List<String> getObserverList(String groupId) {
        log.debug("start getObserverList. groupId:{}", groupId);
        List<String> observers =
            frontRestTools.getForEntity(groupId, FrontRestTools.URI_GET_OBSERVER_LIST, List.class);
        log.debug("end getObserverList. observers:{}", JsonTools.toJSONString(observers));
        return observers;
    }

    /**
     * get observer list from specific front
     */
    public List<String> getObserverListFromSpecificFront(String frontIp, Integer frontPort,
        String groupId) {
        return getFromSpecificFront(groupId, frontIp, frontPort,
            FrontRestTools.URI_GET_OBSERVER_LIST, List.class);
    }

    /**
     * get consensusStatus javasdk: get consensusInfo of {basicConsensusInfo and list of view info}
     */
    public ConsensusStatusInfo getConsensusStatus(String groupId) {
        log.debug("start getConsensusStatus. groupId:{}", groupId);
        ConsensusStatusInfo consensusInfo = frontRestTools.getForEntity(groupId,
            FrontRestTools.URI_CONSENSUS_STATUS, ConsensusStatusInfo.class);
        log.debug("end getConsensusStatus. consensusInfo:{}", consensusInfo);
        return consensusInfo;
    }

    /**
     * get syncStatus
     */
    public SyncStatusInfo getSyncStatus(String groupId) {
        log.debug("start getSyncStatus. groupId:{}", groupId);
        SyncStatusInfo ststus = frontRestTools.getForEntity(groupId, FrontRestTools.URI_SYNC_STATUS,
            SyncStatusInfo.class);
        log.debug("end getSyncStatus. ststus:{}", JsonTools.toJSONString(ststus));
        return ststus;
    }

    /**
     * get latest block number
     */
    public BigInteger getLatestBlockNumber(String groupId) {
        log.debug("start getLatestBlockNumber. groupId:{}", groupId);
        BigInteger latestBlockNmber = frontRestTools.getForEntity(groupId,
            FrontRestTools.URI_BLOCK_NUMBER, BigInteger.class);
        log.debug("end getLatestBlockNumber. latestBlockNmber:{}", latestBlockNmber);
        return latestBlockNmber;
    }

    /**
     * get sealerList.
     */
    public List<String> getSealerList(String groupId) {
        log.debug("start getSealerList. groupId:{}", groupId);
        List getSealerList = frontRestTools.getForEntity(groupId,
            FrontRestTools.URI_GET_SEALER_LIST, List.class);
        log.debug("end getSealerList. getSealerList:{}", JsonTools.toJSONString(getSealerList));
        return getSealerList;
    }

    public List<Sealer> getSealerListWithWeight(String groupId) {
        log.debug("start getSealerListWithWeight. groupId:{}", groupId);
        List data = frontRestTools.getForEntity(groupId,
            FrontRestTools.URI_GET_SEALER_LIST_WEIGHT, List.class);
        List<Sealer> sealerList = JsonTools.toJavaObjectList(JsonTools.toJSONString(data),
            Sealer.class);

        log.debug("end getSealerList. sealerList:{}", JsonTools.toJSONString(sealerList));
        return sealerList;
    }


    /**
     * get config by key
     */
    public String getSystemConfigByKey(String groupId, String key) {
        log.debug("start getSystemConfigByKey. groupId:{}", groupId);
        String uri = String.format(FrontRestTools.URI_SYSTEM_CONFIG, key);
        String config = frontRestTools.getForEntity(groupId, uri, String.class);
        log.debug("end getSystemConfigByKey. config:{}", config);
        return config;
    }


    /**
     * get front version
     */
    public String getFrontVersionFromSpecificFront(String frontIp, Integer frontPort) {
        log.debug("start getClientVersionFromSpecificFront. frontIp:{},frontPort:{}", frontIp,
            frontPort);
        String groupId = GROUPID;
        String frontVersion = getFromSpecificFront(groupId,
            frontIp, frontPort, FrontRestTools.URI_FRONT_VERSION, String.class);
        log.debug("end getFrontVersionFromSpecificFront. frontVersion:{}", frontVersion);
        return frontVersion;
    }

    /**
     * get webase-sign version
     */
    public String getSignVersionFromSpecificFront(String frontIp, Integer frontPort) {
        log.debug("start getSignVersionFromSpecificFront. frontIp:{},frontPort:{}", frontIp,
            frontPort);
        String groupId = GROUPID;
        String signVersion = getFromSpecificFront(groupId,
            frontIp, frontPort, FrontRestTools.URI_SIGN_VERSION, String.class);
        log.debug("end getSignVersionFromSpecificFront. signVersion:{}", signVersion);
        return signVersion;
    }

    /**
     * get front node info
     */
    public List<String> getPeersConfigFromSpecificFront(String frontIp, Integer frontPort) {
        String groupId = "1";
        List<String> nodeConfig = getFromSpecificFront(groupId, frontIp, frontPort,
            FrontRestTools.URI_NODE_CONFIG, List.class);
        return nodeConfig;
    }

    /**
     * include node name list
     */
    public Object getGroupInfoFromSpecificFront(String frontIp, Integer frontPort, String groupId) {
        Object groupInfo = getFromSpecificFront(groupId, frontIp, frontPort,
            FrontRestTools.URI_GROUP_INFO, Object.class);
        return groupInfo;
    }


    /**
     * include node name list
     */
    public String getOneNodeBinaryVersion(String groupId) {
        String data = frontRestTools.getForEntity(groupId, FrontRestTools.URI_BINARY_VERSION, String.class);
        log.info("getOneNodeBinaryVersion {}", data);
        return data;
    }

    /**
     * refresh front.
     */
    public void refreshFront(String frontIp, Integer frontPort) {
        log.debug("start refreshFront frontIp:{} frontPort:{} ", frontIp, frontPort);
        String groupId = GROUPID;
        getFromSpecificFront(groupId, frontIp, frontPort, FrontRestTools.URI_REFRESH_FRONT,
            Object.class);
        log.debug("end refreshFront");
    }

    /**
     * get new block info list
     */
    public List<NewBlockEventInfo> getNewBlockEventInfo(String frontIp, Integer frontPort,
        String groupId) {
        log.debug("start getNewBlockEventInfo frontIp:{} frontPort:{} groupId:{}",
            frontIp, frontPort, groupId);
        String newBlockInfoURI = FrontRestTools.URI_NEW_BLOCK_EVENT_INFO_LIST + "/" + groupId;
        URI_CONTAIN_GROUP_ID.add(newBlockInfoURI);
        BasePageResponse response = getFromSpecificFront(groupId, frontIp, frontPort,
            newBlockInfoURI,
            BasePageResponse.class);
        if (response.getData() == null) {
            return new ArrayList<>();
        }
        List data = (List) response.getData();
        List<NewBlockEventInfo> resList = JsonTools.toJavaObjectList(JsonTools.toJSONString(data),
            NewBlockEventInfo.class);
        resList.forEach(info -> info.setFrontInfo(frontIp));
        return resList;
    }

    public List<ContractEventInfo> getContractEventInfo(String frontIp, Integer frontPort,
        String groupId) {
        log.debug("start getContractEventInfo frontIp:{} frontPort:{} groupId:{}",
            frontIp, frontPort, groupId);
        String contractEventInfoURI = FrontRestTools.URI_CONTRACT_EVENT_INFO_LIST + "/" + groupId;
        URI_CONTAIN_GROUP_ID.add(contractEventInfoURI);
        BasePageResponse response = getFromSpecificFront(groupId, frontIp, frontPort,
            contractEventInfoURI,
            BasePageResponse.class);
        if (response.getData() == null) {
            return new ArrayList<>();
        }
        List data = (List) response.getData();
        List<ContractEventInfo> resList = JsonTools.toJavaObjectList(JsonTools.toJSONString(data),
            ContractEventInfo.class);
        resList.forEach(info -> info.setFrontInfo(frontIp));
        return resList;
    }

    /**
     * get event log list from front
     */
    public BasePageResponse getEventLogList(ReqEventLogList param) {
        log.debug("start getEventLogList. param:{}", param);
        BasePageResponse resultList = frontRestTools.postForEntity(param.getGroupId(),
            FrontRestTools.URI_EVENT_LOG_LIST, param, BasePageResponse.class);
        log.debug("end getEventLogList. resultList:{}", JsonTools.toJSONString(resultList));
        return resultList;
    }


    /**
     * get block statistic by number.
     */
    public RspStatBlock getBlockStatisticByNumber(String groupId, BigInteger blockNumber)
        throws NodeMgrException {
        log.debug("start getBlockStatisticByNumber groupId:{} blockNumber:{}", groupId,
            blockNumber);
        String uri = String.format(FrontRestTools.URI_BLOCK_STAT_BY_NUMBER, blockNumber);
        RspStatBlock blockStat = null;
        // catch error to avoid task abort
        try {
            blockStat = frontRestTools.getForEntity(groupId, uri, RspStatBlock.class);
        } catch (Exception ex) {
            log.error("fail getBlockStatisticByNumber,exception:[]", ex);
        }
        log.debug("end getBlockStatisticByNumber, blockStat:{}", blockStat);
        return blockStat;
    }

    public Object searchByBlockNumOrTxHash(String groupId, String input)
        throws NodeMgrException {
        log.debug("start searchByBlockNumOrTxHash groupId:{} input:{}", groupId, input);
        Map<String, String> map = new HashMap<>();
        map.put("input", input);
        String uri = HttpRequestTools.getQueryUri(FrontRestTools.URI_SEARCH_BLOCK_OR_TX, map);
        Object blockOrTx = frontRestTools.getForEntity(groupId, uri, Object.class);
        log.debug("end searchByBlockNumOrTxHash, blockOrTx:{}", blockOrTx);
        return blockOrTx;
    }

    /**
     * get sdk's Cert Content from specific front.
     */
    public List<SdkCertInfo> getSdkCertInfo() {
        List<SdkCertInfo> sdkCertList = new ArrayList<>();
        Map<String, String> certMap =
                frontRestTools.getForEntity(GROUPID, FrontRestTools.URI_CERT_SDK_FILES, Map.class);
        for (Map.Entry<String, String> entry : certMap.entrySet()) {
            SdkCertInfo sdkCertInfo = new SdkCertInfo(entry.getKey(), entry.getValue());
            sdkCertList.add(sdkCertInfo);
        }
        return sdkCertList;
    }

    /**
     * getClientVersion
     */
    public ClientVersion getClientVersion() {
        ClientVersion clientVersion = frontRestTools.getForEntity(GROUPID,
            FrontRestTools.URI_GET_CLIENT_VERSION, ClientVersion.class);
        return clientVersion;
    }

    public Object getSignMessageHash(String groupId, String hash, String signUserId) {
        log.debug("start getSignMessageHash hash:{} signUserId:{}", hash, signUserId);
        Map<String, String> map = new HashMap<>();
        map.put("hash", hash);
        map.put("groupId", groupId);
        map.put("signUserId", signUserId);
        Object signMessage = frontRestTools.postForEntity("1", FrontRestTools.URI_SIGN_MESSAGE, map,
            Object.class);
        log.debug("end getSignMessageHash, signMessage:{}", signMessage);
        return signMessage;
    }

    public BaseResponse getFrontSdkFromSpecifiFront(String frontIp, Integer frontPort) {
        log.debug("start getFrontSdkFromSpecifiFront frontIp:{},frontPort:{}", frontIp, frontPort);
        String groupId = GROUPID;
        BaseResponse response = this.getFromSpecificFront(groupId, frontIp, frontPort,
            FrontRestTools.URI_CONFIG_SDK,
            BaseResponse.class);
        log.debug("end getFrontSdkFromSpecifiFront response:{}", JsonTools.toJSONString(response));
        return response;
    }

    public BaseResponse configFrontSdkFromSpecifiFront(String frontIp, Integer frontPort,
        ReqSdkConfig param) {
        log.debug("start configFrontSdkFromSpecifiFront frontIp:{},frontPort:{},param:{}", frontIp,
            frontPort,
            JsonTools.toJSONString(param));
        String groupId = GROUPID;
        BaseResponse response = this.postFromSpecificFront(groupId, frontIp, frontPort,
            FrontRestTools.URI_CONFIG_SDK, param,
            BaseResponse.class);
        log.debug("end configFrontSdkFromSpecifiFront response:{}",
            JsonTools.toJSONString(response));
        return response;

    }
}

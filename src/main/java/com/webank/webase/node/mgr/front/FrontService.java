/**
 * Copyright 2014-2020  the original author or authors.
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
package com.webank.webase.node.mgr.front;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.web3j.crypto.EncryptType;
import org.fisco.bcos.web3j.protocol.core.methods.response.NodeVersion;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.enums.DataStatus;
import com.webank.webase.node.mgr.base.enums.FrontStatusEnum;
import com.webank.webase.node.mgr.base.enums.GroupStatus;
import com.webank.webase.node.mgr.base.enums.GroupType;
import com.webank.webase.node.mgr.base.enums.OptionType;
import com.webank.webase.node.mgr.base.enums.RunTypeEnum;
import com.webank.webase.node.mgr.base.enums.ScpTypeEnum;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.base.properties.ConstantProperties;
import com.webank.webase.node.mgr.base.tools.CertTools;
import com.webank.webase.node.mgr.base.tools.JsonTools;
import com.webank.webase.node.mgr.base.tools.NodeMgrTools;
import com.webank.webase.node.mgr.base.tools.NumberUtil;
import com.webank.webase.node.mgr.base.tools.ThymeleafUtil;
import com.webank.webase.node.mgr.base.tools.cmd.ExecuteResult;
import com.webank.webase.node.mgr.deploy.entity.NodeConfig;
import com.webank.webase.node.mgr.deploy.entity.TbAgency;
import com.webank.webase.node.mgr.deploy.entity.TbChain;
import com.webank.webase.node.mgr.deploy.entity.TbHost;
import com.webank.webase.node.mgr.deploy.mapper.TbHostMapper;
import com.webank.webase.node.mgr.deploy.service.AgencyService;
import com.webank.webase.node.mgr.deploy.service.DeployShellService;
import com.webank.webase.node.mgr.deploy.service.PathService;
import com.webank.webase.node.mgr.deploy.service.docker.DockerOptions;
import com.webank.webase.node.mgr.front.entity.FrontInfo;
import com.webank.webase.node.mgr.front.entity.FrontParam;
import com.webank.webase.node.mgr.front.entity.TbFront;
import com.webank.webase.node.mgr.frontgroupmap.FrontGroupMapCache;
import com.webank.webase.node.mgr.frontgroupmap.FrontGroupMapMapper;
import com.webank.webase.node.mgr.frontgroupmap.FrontGroupMapService;
import com.webank.webase.node.mgr.frontgroupmap.entity.TbFrontGroupMap;
import com.webank.webase.node.mgr.frontinterface.FrontInterfaceService;
import com.webank.webase.node.mgr.frontinterface.entity.SyncStatus;
import com.webank.webase.node.mgr.group.GroupService;
import com.webank.webase.node.mgr.group.entity.TbGroup;
import com.webank.webase.node.mgr.node.NodeMapper;
import com.webank.webase.node.mgr.node.NodeService;
import com.webank.webase.node.mgr.node.entity.NodeParam;
import com.webank.webase.node.mgr.node.entity.PeerInfo;
import com.webank.webase.node.mgr.node.entity.TbNode;
import com.webank.webase.node.mgr.scheduler.ResetGroupListTask;

import lombok.extern.log4j.Log4j2;

/**
 * service of web3.
 */
@Log4j2
@Service
public class FrontService {

    @Autowired
    private FrontMapper frontMapper;
    @Autowired
    private TbHostMapper tbHostMapper;
    @Autowired
    private NodeMapper nodeMapper;
    @Autowired
    private FrontGroupMapMapper frontGroupMapMapper;

    @Autowired
    private NodeService nodeService;
    @Autowired
    private GroupService groupService;
    @Autowired
    private FrontGroupMapService frontGroupMapService;
    @Autowired
    private FrontInterfaceService frontInterface;
    @Autowired
    private FrontGroupMapCache frontGroupMapCache;
    @Autowired
    private ResetGroupListTask resetGroupListTask;
    @Autowired
    private ConstantProperties constants;
    @Autowired
    private AgencyService agencyService;
    @Autowired
    private PathService pathService;
    @Autowired
    private DeployShellService deployShellService;
    @Autowired
    private ConstantProperties constant;
    @Autowired
    private DockerOptions dockerOptions;

    @Qualifier(value = "deployAsyncScheduler")
    @Autowired private ThreadPoolTaskScheduler threadPoolTaskScheduler;

	// interval of check front status
	private static final Long CHECK_FRONT_STATUS_WAIT_MIN_MILLIS = 3000L;

	/**
	 * refresh front, group, frontGroupMap, nodeList
	 */
	@Transactional
	public void refreshFront() {
		//get all front
        List<TbFront> frontList = frontMapper.getAllList();
        if (frontList == null || frontList.size() == 0) {
        	log.info("refreshFront. not find any front.");
            return;
        }
        for (TbFront tbFront : frontList) {
        	try {
	            String frontIp = tbFront.getFrontIp();
	            Integer frontPort = tbFront.getFrontPort();
				// query group list from chain
				List<String> groupIdList;
					groupIdList = frontInterface.getGroupListFromSpecificFront(frontIp, frontPort);
				// get syncStatus
				SyncStatus syncStatus = frontInterface.getSyncStatusFromSpecificFront(frontIp, 
						frontPort, Integer.valueOf(groupIdList.get(0)));
				// get version info
				NodeVersion.Version versionResponse = frontInterface.getClientVersionFromSpecificFront(frontIp,
						frontPort, Integer.valueOf(groupIdList.get(0)));
				String clientVersion = versionResponse.getVersion();
				String supportVersion = versionResponse.getSupportedVersion();
				// get front server version and sign server version
		        try {
		            String frontVersion = frontInterface.getFrontVersionFromSpecificFront(frontIp, frontPort);
		            String signVersion = frontInterface.getSignVersionFromSpecificFront(frontIp, frontPort);
		            tbFront.setFrontVersion(frontVersion);
		            tbFront.setSignVersion(signVersion);
		        } catch (Exception e) {
		            // catch old version front and sign that not have '/version' api
		            log.warn("get version of Front and Sign failed (required front and sign v1.4.0+).");
		        }
				//copy attribute
				tbFront.setNodeId(syncStatus.getNodeId());
				tbFront.setClientVersion(clientVersion);
				tbFront.setSupportVersion(supportVersion);
				//update front info
				frontMapper.updateBasicInfo(tbFront);
				// save group info
				saveGroup(groupIdList, tbFront);
        	} catch (Exception ex) {
				log.error("refreshFront fail. frontId:{}", tbFront.getFrontId(), ex);
				continue;
			}
		}
		// clear cache
		frontGroupMapCache.clearMapList();
	}
	
    /**
     * add new front, save front, frontGroupMap, check front's groupStatus, refresh nodeList
     */
    @Transactional
    public TbFront newFront(FrontInfo frontInfo) {
        log.debug("start newFront frontInfo:{}", frontInfo);
        TbFront tbFront = new TbFront();
        tbFront.setRunType(RunTypeEnum.COMMAND.getId());
        // set default chainId
        tbFront.setChainId(0);
        tbFront.setChainName("default");

        String frontIp = frontInfo.getFrontIp();
        Integer frontPort = frontInfo.getFrontPort();
        //check valid ip
        checkNotSupportIp(frontIp);
        //check front ip and port
        NodeMgrTools.checkServerConnect(frontIp, frontPort);
        //query group list
        List<String> groupIdList = null;
        try {
            groupIdList = frontInterface.getGroupListFromSpecificFront(frontIp, frontPort);
        } catch (Exception e) {
            log.error("fail newFront, frontIp:{},frontPort:{}",frontIp,frontPort);
            throw new NodeMgrException(ConstantCode.REQUEST_FRONT_FAIL);
        }
        // check front's encrypt type same as nodemgr(guomi or standard)
        int encryptType = frontInterface.getEncryptTypeFromSpecificFront(frontIp, frontPort);
        if (encryptType != EncryptType.encryptType) {
            log.error("fail newFront, frontIp:{},frontPort:{},front's encryptType:{}," +
                            "local encryptType not match:{}",
                    frontIp, frontPort, encryptType, EncryptType.encryptType);
            throw new NodeMgrException(ConstantCode.ENCRYPT_TYPE_NOT_MATCH);
        }
        //check front not exist
        SyncStatus syncStatus = frontInterface.getSyncStatusFromSpecificFront(frontIp, 
                frontPort, Integer.valueOf(groupIdList.get(0)));
        FrontParam param = new FrontParam();
        param.setNodeId(syncStatus.getNodeId());
        int count = getFrontCount(param);
        if (count > 0) {
            throw new NodeMgrException(ConstantCode.FRONT_EXISTS);
        }
        NodeVersion.Version versionResponse = frontInterface.getClientVersionFromSpecificFront(frontIp,
                frontPort, Integer.valueOf(groupIdList.get(0)));
        String clientVersion = versionResponse.getVersion();
        String supportVersion = versionResponse.getSupportedVersion();
        //copy attribute
        BeanUtils.copyProperties(frontInfo, tbFront);
        tbFront.setNodeId(syncStatus.getNodeId());
        tbFront.setClientVersion(clientVersion);
        tbFront.setSupportVersion(supportVersion);
        // get front server version and sign server version
        try {
            String frontVersion = frontInterface.getFrontVersionFromSpecificFront(frontIp, frontPort);
            String signVersion = frontInterface.getSignVersionFromSpecificFront(frontIp, frontPort);
            tbFront.setFrontVersion(frontVersion);
            tbFront.setSignVersion(signVersion);
        } catch (Exception e) {
            // catch old version front and sign that not have '/version' api
            log.warn("get version of Front and Sign failed (required front and sign v1.4.0+) exception:[]", e);
        }
        //save front info
        try{
            frontMapper.add(tbFront);
        } catch (Exception e) {
            log.warn("fail newFront, after save, tbFront:{}, exception:{}",
                JsonTools.toJSONString(tbFront), e);
            throw new NodeMgrException(ConstantCode.SAVE_FRONT_FAIL.getCode(), e.getMessage());
        }
        // save group info
        saveGroup(groupIdList, tbFront);
        // pull cert from new front and its node
        CertTools.isPullFrontCertsDone = false;
        // clear cache
        frontGroupMapCache.clearMapList();
        return tbFront;
    }
    
    /**
     * save group, frontGroupMap, nodeList
     * @param groupIdList
     * @param tbFront
     */
    private void saveGroup(List<String> groupIdList, TbFront tbFront){
		String frontIp = tbFront.getFrontIp();
		Integer frontPort = tbFront.getFrontPort();
		for (String groupId : groupIdList) {
			Integer group = Integer.valueOf(groupId);
			//peer in group
			List<String> groupPeerList = frontInterface
					.getGroupPeersFromSpecificFront(frontIp, frontPort, group);
			//get peers on chain
			PeerInfo[] peerArr = frontInterface
					.getPeersFromSpecificFront(frontIp, frontPort, group);
			List<PeerInfo> peerList = Arrays.asList(peerArr);
			//add group
			// check group not existed or node count differs
			TbGroup checkGroup = groupService.getGroupById(group);
			if (Objects.isNull(checkGroup) || groupPeerList.size() != checkGroup.getNodeCount()) {
				groupService.saveGroup(group, groupPeerList.size(), "synchronous",
						GroupType.SYNC, GroupStatus.NORMAL,0,"");
			}
			//save front group map
			frontGroupMapService.newFrontGroup(tbFront, group);
			//save nodes
			for (String nodeId : groupPeerList) {
				PeerInfo newPeer = peerList.stream().map(p -> NodeMgrTools
						.object2JavaBean(p, PeerInfo.class))
						.filter(peer -> nodeId.equals(peer.getNodeId()))
						.findFirst().orElseGet(() -> new PeerInfo(nodeId));
				nodeService.addNodeInfo(group, newPeer);
			}
			// add sealer(consensus node) and observer in nodeList
			refreshSealerAndObserverInNodeList(frontIp, frontPort, group);
		}
	}

    /**
     * add sealer(consensus node) and observer in nodeList
     * @param groupId
     */
    public void refreshSealerAndObserverInNodeList(String frontIp, int frontPort, int groupId) {
        log.debug("start refreshSealerAndObserverInNodeList frontIp:{}, frontPort:{}, groupId:{}",
                frontIp, frontPort, groupId);
        List<String> sealerList = frontInterface.getSealerListFromSpecificFront(frontIp, frontPort, groupId);
        List<String> observerList = frontInterface.getObserverListFromSpecificFront(frontIp, frontPort, groupId);
        List<PeerInfo> sealerAndObserverList = new ArrayList<>();
        sealerList.stream().forEach(nodeId -> sealerAndObserverList.add(new PeerInfo(nodeId)));
        observerList.stream().forEach(nodeId -> sealerAndObserverList.add(new PeerInfo(nodeId)));
        log.debug("refreshSealerAndObserverInNodeList sealerList:{},observerList:{}",
                sealerList, observerList);
        sealerAndObserverList.forEach(peerInfo -> {
            NodeParam checkParam = new NodeParam();
            checkParam.setGroupId(groupId);
            checkParam.setNodeId(peerInfo.getNodeId());
            int existedNodeCount = nodeService.countOfNode(checkParam);
            log.debug("addSealerAndObserver peerInfo:{},existedNodeCount:{}",
                    peerInfo, existedNodeCount);
            if(existedNodeCount == 0) {
                nodeService.addNodeInfo(groupId, peerInfo);
            }
        });
        log.debug("end addSealerAndObserver");
    }

    /**
     * check not support ip.
     */
    public void checkNotSupportIp(String ip) {

        String ipConfig = constants.getNotSupportFrontIp();
        if(StringUtils.isBlank(ipConfig)) {
            return;
        }
        List<String> list = Arrays.asList(ipConfig.split(","));
        if (list.contains(ip)) {
            throw new NodeMgrException(ConstantCode.INVALID_FRONT_IP);
        }
    }

    /**
     * check front ip and prot
     *
     * if exist:throw exception
     */
    private void checkFrontNotExist(String frontIp, int frontPort) {
        SyncStatus syncStatus = frontInterface.getSyncStatusFromSpecificFront(frontIp, frontPort, 1);
        FrontParam param = new FrontParam();
        param.setNodeId(syncStatus.getNodeId());
        int count = getFrontCount(param);
        if (count > 0) {
            throw new NodeMgrException(ConstantCode.FRONT_EXISTS);
        }
    }


    /**
     * get front count
     */
    public int getFrontCount(FrontParam param) {
        Integer count = frontMapper.getCount(param);
        return count == null ? 0 : count;
    }

    /**
     * get front list
     */
    public List<TbFront> getFrontList(FrontParam param) {
        return frontMapper.getList(param);
    }

    /**
     * query front by frontId.
     */
    public TbFront getById(int frontId) {
        if (frontId == 0) {
            return null;
        }
        return frontMapper.getById(frontId);
    }

    /**
     * query front by nodeId.
     */
    public TbFront getByNodeId(String nodeId) {
        if (StringUtils.isBlank(nodeId)) {
            return null;
        }
        return frontMapper.getByNodeId(nodeId);
    }

    /**
     * remove front
     */
    @Transactional
    public void removeFront(int frontId) {
        //check frontId
        FrontParam param = new FrontParam();
        param.setFrontId(frontId);
        int count = getFrontCount(param);
        if (count == 0) {
            throw new NodeMgrException(ConstantCode.INVALID_FRONT_ID);
        }

        //remove front
        frontMapper.remove(frontId);
        //remove map
        frontGroupMapService.removeByFrontId(frontId);
        //reset group list => remove groups that only belongs to this front
        resetGroupListTask.asyncResetGroupList();
        //clear cache
        frontGroupMapCache.clearMapList();
    }

    public void updateFront(TbFront updateFront) {
        log.info("updateFrontStatus updateFront:{}", updateFront);
        if (updateFront == null) {
            log.error("updateFrontStatus updateFront is null");
            return;
        }
        frontMapper.update(updateFront);
    }

    public void updateFrontWithInternal(Integer frontId, Integer status) {
        log.info("updateFrontStatus frontId:{}, status:{}", frontId, status);
        TbFront updateFront = getById(frontId);
        if (updateFront == null) {
            log.error("updateFrontStatus updateFront is null");
            return;
        }
        if (updateFront.getStatus() != null &&  updateFront.getStatus().equals(status)) {
            return;
        }
        LocalDateTime modifyTime = updateFront.getModifyTime();
        LocalDateTime createTime = updateFront.getCreateTime();
		Duration duration = Duration.between(modifyTime, LocalDateTime.now());
		Long subTime = duration.toMillis();
		if (subTime < CHECK_FRONT_STATUS_WAIT_MIN_MILLIS && createTime.isBefore(modifyTime)) {
			log.debug("updateFrontWithInternal jump. subTime:{}, minInternal:{}",
					subTime, CHECK_FRONT_STATUS_WAIT_MIN_MILLIS);
			return;
		}
        updateFront.setStatus(status);
        frontMapper.update(updateFront);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public TbFront insert(TbFront tbFront) throws NodeMgrException {
        if (frontMapper.add(tbFront) != 1 || tbFront.getFrontId() <= 0){
            throw new NodeMgrException(ConstantCode.INSERT_FRONT_ERROR);
        }
        return tbFront;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public boolean updateStatus(int frontId, FrontStatusEnum newStatus) {
        log.info("Update front:[{}] status to:[{}]",frontId, newStatus.toString());
        return this.frontMapper.updateStatus(frontId, newStatus.getId(), LocalDateTime.now()) == 1;
    }

    /**
     * @param chainId
     * @return
     */
    public List<TbFront> selectFrontListByChainId(int chainId) {
        // select all agencies by chainId
        List<TbAgency> tbAgencyList = this.agencyService.selectAgencyListByChainId(chainId);

        // select all fronts by all agencies
        List<TbFront> tbFrontList = tbAgencyList.stream()
                .map((agency) -> frontMapper.selectByAgencyId(agency.getId()))
                .filter((front) -> front != null)
                .flatMap(List::stream)
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(tbFrontList)) {
            log.error("Chain:[{}] has no front.", chainId);
            return Collections.emptyList();
        }
        return tbFrontList;
    }

    /**
     *
     * @param groupId
     * @return
     */
    public List<TbFront> selectFrontListByGroupId(int groupId) {
        // select all agencies by chainId
        List<TbFrontGroupMap> frontGroupMapList = this.frontGroupMapMapper.selectListByGroupId(groupId);
        if (CollectionUtils.isEmpty(frontGroupMapList)) {
            return Collections.emptyList();
        }

        // select all fronts by all agencies
        List<TbFront> tbFrontList = frontGroupMapList.stream()
                .map((map) -> frontMapper.getById(map.getFrontId()))
                .filter((front) -> front != null)
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(tbFrontList)) {
            log.error("Group:[{}] has no front.", groupId);
            return Collections.emptyList();
        }
        return tbFrontList;
    }

    /**
     *
     * @param groupIdSet
     * @return
     */
    public List<TbFront> selectFrontListByGroupIdSet(Set<Integer> groupIdSet) {
        // select all fronts of all group id
        List<TbFront> allTbFrontList = groupIdSet.stream()
                .map((groupId) -> this.selectFrontListByGroupId(groupId))
                .filter((front) -> front != null)
                .flatMap(List::stream)
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(allTbFrontList)) {
            log.error("Group id set:[{}] has no front.", JsonTools.toJSONString(groupIdSet));
            return Collections.emptyList();
        }

        // delete replication
        return allTbFrontList.stream().distinct().collect(Collectors.toList());
    }


    /**
     * gen node cert and gen front's yml, and new front ind db
     * @param num
     * @param chain
     * @param host
     * @param agencyId
     * @param agencyName
     * @param group
     * @param frontStatusEnum
     * @return
     * @throws NodeMgrException
     * @throws IOException
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public List<TbFront> initFrontAndNode(int num, TbChain chain, TbHost host, int agencyId,
                                          String agencyName, TbGroup group,FrontStatusEnum frontStatusEnum)
        throws NodeMgrException, IOException {

        String chainName = chain.getChainName();
        byte encryptType = chain.getEncryptType();
        // the node dir on remote host
        String rootDirOnHost = chain.getRootDir();
        String imageTag = chain.getVersion();
        int hostId = host.getId();
        String ip = host.getIp();
        int groupId = group.getGroupId();

        // if host is a new one, currentIndexOnHost will be null
        Integer maxIndexOnHost = this.frontMapper.getNodeMaxIndex(hostId);

        // get start index on host
        int startIndex = maxIndexOnHost == null ? 0 : maxIndexOnHost + 1;

        List<TbFront> newFrontList = new ArrayList<>();
        // call shell to generate new node config(private key and crt)
        for (int i = 0; i < num; i++) {
            int currentIndex = startIndex + i;
            Path nodeRoot = pathService.getNodeRoot(chainName, ip, currentIndex);

            if(Files.exists(nodeRoot)){
                log.warn("Exists node:[{}:{}] config, delete first.",ip,nodeRoot.toAbsolutePath().toString());
                try {
                    FileUtils.deleteDirectory(nodeRoot.toFile());
                } catch (IOException e) {
                    throw new NodeMgrException(ConstantCode.DELETE_OLD_NODE_DIR_ERROR);
                }
            }
            // exec gen_node_cert.sh
            ExecuteResult executeResult = this.deployShellService.execGenNode(encryptType, chainName, agencyName,
                    nodeRoot.toAbsolutePath().toString());

            if (executeResult.failed()) {
                log.error("Generate node:[{}:{}] key and crt error.", ip, currentIndex);
                throw new NodeMgrException(ConstantCode.EXEC_GEN_NODE_ERROR);
            }

            String nodeId = PathService.getNodeId(nodeRoot,encryptType);
            int frontPort = constant.getDefaultFrontPort() + currentIndex;
            int channelPort = constant.getDefaultChannelPort() + currentIndex;
            int p2pPort = constant.getDefaultP2pPort() + currentIndex;
            int jsonrpcPort = constant.getDefaultJsonrpcPort() + currentIndex;


            TbFront front = TbFront.init(nodeId, ip, frontPort, agencyId, agencyName, imageTag, RunTypeEnum.DOCKER,
                    hostId, currentIndex, imageTag, DockerOptions.getContainerName(rootDirOnHost, chainName, currentIndex),
                    jsonrpcPort, p2pPort, channelPort, chain.getId(), chainName, frontStatusEnum);
            // insert front into db
            ((FrontService) AopContext.currentProxy()).insert(front);

            newFrontList.add(front);

            // insert node into db
            String nodeName = NodeService.getNodeName(groupId, nodeId);
            this.nodeService.insert(nodeId, nodeName, groupId, ip, p2pPort, nodeName, DataStatus.STARTING);

            // insert front group into db
            this.frontGroupMapService.newFrontGroup(front.getFrontId(), groupId, GroupStatus.MAINTAINING);

            // generate front application.yml
            ThymeleafUtil.newFrontConfig(nodeRoot,encryptType,channelPort, frontPort,chain.getWebaseSignAddr());
        }
        return newFrontList;
    }

    /**
     *
     * @param nodeId
     * @return
     */
    public List<TbFront> selectRelatedFront(String nodeId){
        Set<Integer> frontIdSet = new HashSet<>();
        List<Integer> groupIdList = this.nodeMapper.selectGroupIdListOfNode(nodeId);
        if (CollectionUtils.isEmpty(groupIdList)){
            log.error("Node:[{}] has no group", nodeId);
            Collections.emptyList();
        }
        for (Integer groupIdOfNode : groupIdList) {
            List<TbFrontGroupMap> tbFrontGroupMaps = this.frontGroupMapMapper.selectListByGroupId(groupIdOfNode);
            if (CollectionUtils.isNotEmpty(tbFrontGroupMaps)){
                tbFrontGroupMaps.forEach(map->{
                    frontIdSet.add(map.getFrontId());
                });
            }
        }

        List<TbFront> nodeRelatedFrontList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(frontIdSet)){
            nodeRelatedFrontList = frontIdSet.stream().map((frontId)-> this.frontMapper.getById(frontId))
                    .filter((front) -> front != null)
                    .collect(Collectors.toList());
        }
        return nodeRelatedFrontList;
    }

    /**
     * not generate but update existed node config.ini of existed nodes
     * @param chain
     * @param groupId
     * @throws IOException
     */
    public void updateNodeConfigIniByGroupId(TbChain chain, int groupId) throws IOException {
        int chainId = chain.getId();
        String chainName = chain.getChainName();
        byte encryptType = chain.getEncryptType();

        List<TbNode> tbNodeListOfGroup = this.nodeService.selectNodeListByChainIdAndGroupId(chainId, groupId);

        // all fronts include old and new
        for (TbNode node : CollectionUtils.emptyIfNull(tbNodeListOfGroup)){
            // select related peers to update node config.ini p2p part
            List<TbFront> nodeRelatedFront = this.selectRelatedFront(node.getNodeId());

            TbFront tbFront = this.getByNodeId(node.getNodeId());

            boolean guomi = encryptType == EncryptType.SM2_TYPE;
            int chainIdInConfigIni = this.constant.getDefaultChainId();

            // local node root
            Path nodeRoot = this.pathService.getNodeRoot(chainName, tbFront.getFrontIp(), tbFront.getHostIndex());

            // generate config.ini
            ThymeleafUtil.newNodeConfigIni(nodeRoot, tbFront.getChannelPort(),
                    tbFront.getP2pPort(), tbFront.getJsonrpcPort(), nodeRelatedFront, guomi, chainIdInConfigIni);

        }

        // scp to remote
        this.scpNodeConfigIni(chain, groupId);
    }

    /**
     *
     * @param chain
     * @param groupIdList
     */
    public void updateNodeConfigIniByGroupList(TbChain chain,
                                               Set<Integer> groupIdList) throws IOException {
        // update config.ini of related nodes
        for (Integer groupId : CollectionUtils.emptyIfNull(groupIdList)) {
            // update node config.ini in group
            this.updateNodeConfigIniByGroupId(chain, groupId);
        }
    }

    /**
     *
     * @param chain
     * @param groupId
     */
    public void scpNodeConfigIni(TbChain chain, int groupId) {
        List<TbNode> tbNodeList = this.nodeService.selectNodeListByChainIdAndGroupId(chain.getId(), groupId);

        for (TbNode tbNode : CollectionUtils.emptyIfNull(tbNodeList)){
            TbFront front = this.getByNodeId(tbNode.getNodeId());
            int hostIndex = front.getHostIndex();

            TbHost host = this.tbHostMapper.selectByPrimaryKey(front.getHostId());

            // path pattern: /NODES_ROOT/chain_name/[ip]/node[index]/config.ini
            // ex: (node-mgr local) ./NODES_ROOT/chain1/127.0.0.1/node0/config.ini
            Path localNodePath = this.pathService.getNodeRoot(chain.getChainName(),front.getFrontIp(),hostIndex);
            String localScr = PathService.getConfigIniPath(localNodePath).toAbsolutePath().toString();

            // ex: (node-mgr local) /opt/fisco/chain1/node0/config.ini
            String remoteDst = String.format("%s/%s/node%s/config.ini", chain.getRootDir(),chain.getChainName(),hostIndex);

            // copy group config files to local node's conf dir
            this.deployShellService.scp(ScpTypeEnum.UP,host.getSshUser(),  host.getIp(),host.getSshPort(),localScr, remoteDst);
        }
    }

    /**
     *
     * @param nodeId
     * @return
     */
    @Transactional(rollbackFor = Throwable.class)
    public boolean restart(String nodeId, OptionType optionType, FrontStatusEnum before,
                           FrontStatusEnum success,FrontStatusEnum failed){
        log.info("Restart node:[{}]", nodeId );
        // get front
        TbFront front = this.getByNodeId(nodeId);
        if (front == null){
            throw new NodeMgrException(ConstantCode.NODE_ID_NOT_EXISTS_ERROR);
        }

        final byte encryptType = FrontService.getEncryptType(front.getImageTag());

        // set front status to stopped to avoid error for time task.
        ((FrontService) AopContext.currentProxy()).updateStatus(front.getFrontId(), before);

        this.frontGroupMapService.updateFrontMapStatus(front.getFrontId(),GroupStatus.MAINTAINING);

        TbHost host = this.tbHostMapper.selectByPrimaryKey(front.getHostId());

        log.info("Docker start container front id:[{}:{}].", front.getFrontId(), front.getContainerName());
        try {
            this.dockerOptions.run(
                    front.getFrontIp(), host.getDockerPort(), host.getSshUser(), host.getSshPort(),
                    front.getImageTag(), front.getContainerName(),
                    PathService.getChainRootOnHost(host.getRootDir(), front.getChainName()), front.getHostIndex());

            threadPoolTaskScheduler.schedule(()->{
                // update front status
                this.updateStatus(front.getFrontId(),success);

                // update front version
                if (StringUtils.isBlank(front.getFrontVersion())
                        || StringUtils.isBlank(front.getSignVersion())) {
                    // update front version
                    try {
                        String frontVersion = frontInterface.getFrontVersionFromSpecificFront(front.getFrontIp(), front.getFrontPort());
                        String signVersion = frontInterface.getSignVersionFromSpecificFront(front.getFrontIp(), front.getFrontPort());

                        this.frontMapper.updateVersion(front.getChainId(), frontVersion, signVersion);
                    } catch (Exception e) {
                        log.error("Request front and sign version from front:[{}:{}] error.",front.getFrontIp(),front.getFrontPort(), e);
                    }
                }

                if (optionType == OptionType.DEPLOY_CHAIN){
                    this.frontGroupMapService.updateFrontMapStatus(front.getFrontId(),GroupStatus.NORMAL);
                }else if (optionType == OptionType.MODIFY_CHAIN){
                    // check front is in group
                    Path nodePath = this.pathService.getNodeRoot(front.getChainName(), host.getIp(), front.getHostIndex());
                    Set<Integer> groupIdSet = NodeConfig.getGroupIdSet(nodePath,encryptType);
                    Optional.of(groupIdSet).ifPresent(idSet -> idSet.forEach ( groupId ->{
                        List<String> list = frontInterface.getGroupPeers(groupId);
                        if(CollectionUtils.containsAny(list,front.getNodeId())){
                            this.frontGroupMapService.updateFrontMapStatus(front.getFrontId(),GroupStatus.NORMAL);
                        }
                    }));
                }
            }, Instant.now().plusMillis( constant.getDockerRestartPeriodTime()));
            return true;
        } catch (Exception e) {
            log.error("Start front:[{}:{}] failed.",front.getFrontIp(), front.getHostIndex(),e);
            ((FrontService) AopContext.currentProxy()).updateStatus(front.getFrontId(), failed);
            throw new NodeMgrException(ConstantCode.START_NODE_ERROR);
        }
    }

    /**
     *
     * @param chainId
     * @param newImageTag
     * @return
     */
    @Transactional
    public boolean upgrade(int chainId,String newImageTag) {
        boolean updateResult = this.frontMapper.updateUpgradingByChainId(chainId,
                newImageTag, LocalDateTime.now(), FrontStatusEnum.STARTING.getId()) > 0;
        return updateResult;
    }


    /**
     *
     * @param nodeId
     * @return
     */
    @Transactional
    public void stopNode(String nodeId){
        ((FrontService) AopContext.currentProxy()).stopNode(null,nodeId);
    }

    /**
     *
     * @param host
     * @param nodeId
     * @return
     */
    @Transactional
    public void stopNode(TbHost host, String nodeId){
        // get front
        TbFront front = this.getByNodeId(nodeId);
        if (front == null){
            throw new NodeMgrException(ConstantCode.NODE_ID_NOT_EXISTS_ERROR);
        }

        int chainId = front.getChainId();
        int runningTotal = 0;
        List<TbFront> frontList = this.selectFrontListByChainId(chainId);
        for (TbFront tbFront : frontList) {
            if(FrontStatusEnum.isRunning(tbFront.getStatus())){
                runningTotal ++;
            }
        }
        if ( runningTotal < 2){
            log.error("Two running nodes at least of chain:[{}]", chainId);
            throw new NodeMgrException(ConstantCode.TWO_NODES_AT_LEAST);
        }

        if ( ! FrontStatusEnum.isRunning(front.getStatus())){
            log.warn("Node:[{}:{}] is already stopped.",front.getFrontIp(),front.getHostIndex());
            return ;
        }

        // select node list
        List<TbNode> nodeList = this.nodeMapper.selectByNodeId(nodeId);

        // node is removed and doesn't belong to any group.
        boolean nodeRemovable = CollectionUtils.isEmpty(nodeList);

        if (! nodeRemovable) {
            // node belongs to some groups, check if it is the last one of each group.
            Set<Integer> groupIdSet = nodeList.stream().map(TbNode::getGroupId)
                    .collect(Collectors.toSet());

            for (Integer groupId : groupIdSet){
                int nodeCountOfGroup = CollectionUtils.size(this.nodeMapper.selectByGroupId(groupId));
                if (nodeCountOfGroup != 1){ // group has another node.
                    throw new NodeMgrException(ConstantCode.NODE_NEED_REMOVE_FROM_GROUP_ERROR.attach(groupId));
                }
            }
        }
        // Here, tow conditions:
        //  1. node is not a sealer or an observer
        //  2. node is is last node it's each group
        TbHost hostInDb = host != null ? host : this.tbHostMapper.selectByPrimaryKey(front.getHostId());

        log.info("Docker stop and remove container front id:[{}:{}].", front.getFrontId(), front.getContainerName());
        this.dockerOptions.stop( front.getFrontIp(), hostInDb.getDockerPort(), hostInDb.getSshUser(),
                hostInDb.getSshPort(), front.getContainerName());

        ((FrontService) AopContext.currentProxy()).updateStatus(front.getFrontId(),FrontStatusEnum.STOPPED);
    }
    @Transactional
    public void deleteNode(TbHost host, String nodeId){
        // get front
        TbFront front = this.getByNodeId(nodeId);
        if (front == null){
            throw new NodeMgrException(ConstantCode.NODE_ID_NOT_EXISTS_ERROR);
        }
        TbHost hostInDb = host != null ? host : this.tbHostMapper.selectByPrimaryKey(front.getHostId());
        log.info("Docker stop and remove container front id:[{}:{}].", front.getFrontId(), front.getContainerName());
        this.dockerOptions.stop( front.getFrontIp(), hostInDb.getDockerPort(), hostInDb.getSshUser(),
                hostInDb.getSshPort(), front.getContainerName());

        this.nodeMapper.deleteByNodeId(nodeId);
    }


    @Transactional
    public void deleteFrontByAgencyId(int agencyId){
        log.info("Delete front data by agency id:[{}].", agencyId);

        // select host, front, group in agency
        List<TbFront> frontList = frontMapper.selectByAgencyId(agencyId);
        if (CollectionUtils.isEmpty(frontList)) {
            log.warn("No front in agency:[{}]", agencyId);
            return ;
        }

        for (TbFront front : frontList) {
            TbHost host = this.tbHostMapper.selectByPrimaryKey(front.getHostId());

            // remote docker container
            this.dockerOptions.stop(host.getIp(),host.getDockerPort(), host.getSshUser(),host.getSshPort(), front.getContainerName());

            log.info("Delete node data by node id:[{}].", front.getNodeId());
            this.nodeMapper.deleteByNodeId(front.getNodeId());

            log.info("Delete front group map data by front id:[{}].", front.getFrontId());
            this.frontGroupMapMapper.removeByFrontId(front.getFrontId());
        }

        // delete front in batch
        this.frontMapper.deleteByAgencyId(agencyId);
    }

    /**
     *
     * @param chainId
     */
    public int frontProgress(int chainId){
        // check host init
        int frontFinishCount = 0;
        List<TbFront> frontList = this.selectFrontListByChainId(chainId);
        if (CollectionUtils.isEmpty(frontList)) {
            return NumberUtil.PERCENTAGE_FINISH;
        }
        for (TbFront front : frontList) {
            if(FrontStatusEnum.isRunning(front.getStatus())){
                frontFinishCount ++;
            }
        }
        // check front init finish ?
        if (frontFinishCount == frontList.size()){
            // init success
            return NumberUtil.PERCENTAGE_FINISH;
        }
        return NumberUtil.percentage(frontFinishCount,frontList.size());
    }

    public static byte getEncryptType(String frontImageTag){
        return  (byte)(StringUtils.endsWith(frontImageTag,"-gm") ? EncryptType.SM2_TYPE : EncryptType.ECDSA_TYPE);
    }

    public TbFront getRandomFrontByGroupId(Integer groupId) {
        FrontParam param = new FrontParam();
        param.setGroupId(groupId);
        List<TbFront> frontList = this.getFrontList(param);
        if (!frontList.isEmpty()) {
            Random random = new Random();
            return frontList.get(random.nextInt(frontList.size()));
        } else {
            throw new NodeMgrException(ConstantCode.FRONT_LIST_NOT_FOUNT);
        }
    }
}

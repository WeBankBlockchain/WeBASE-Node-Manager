/**
 * Copyright 2014-2021  the original author or authors.
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
package com.webank.webase.node.mgr.chain;

import static com.webank.webase.node.mgr.frontinterface.FrontRestTools.URI_CHAIN;

import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.enums.ChainStatusEnum;
import com.webank.webase.node.mgr.base.enums.DataStatus;
import com.webank.webase.node.mgr.base.enums.FrontStatusEnum;
import com.webank.webase.node.mgr.base.enums.GroupStatus;
import com.webank.webase.node.mgr.base.enums.GroupType;
import com.webank.webase.node.mgr.base.enums.OptionType;
import com.webank.webase.node.mgr.base.enums.RunTypeEnum;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.base.config.properties.ConstantProperties;
import com.webank.webase.node.mgr.tools.CertTools;
import com.webank.webase.node.mgr.tools.JsonTools;
import com.webank.webase.node.mgr.tools.NodeMgrTools;
import com.webank.webase.node.mgr.tools.NumberUtil;
import com.webank.webase.node.mgr.tools.ProgressTools;
import com.webank.webase.node.mgr.tools.ThymeleafUtil;
import com.webank.webase.node.mgr.cert.CertService;
import com.webank.webase.node.mgr.deploy.entity.DeployNodeInfo;
import com.webank.webase.node.mgr.deploy.entity.IpConfigParse;
import com.webank.webase.node.mgr.deploy.entity.NodeConfig;
import com.webank.webase.node.mgr.deploy.entity.TbAgency;
import com.webank.webase.node.mgr.deploy.entity.TbChain;
import com.webank.webase.node.mgr.deploy.entity.TbHost;
import com.webank.webase.node.mgr.deploy.mapper.TbAgencyMapper;
import com.webank.webase.node.mgr.deploy.mapper.TbChainMapper;
import com.webank.webase.node.mgr.deploy.mapper.TbConfigMapper;
import com.webank.webase.node.mgr.deploy.mapper.TbHostMapper;
import com.webank.webase.node.mgr.deploy.service.AgencyService;
import com.webank.webase.node.mgr.deploy.service.AnsibleService;
import com.webank.webase.node.mgr.deploy.service.ConfigService;
import com.webank.webase.node.mgr.deploy.service.DeployShellService;
import com.webank.webase.node.mgr.deploy.service.DockerCommandService;
import com.webank.webase.node.mgr.deploy.service.HostService;
import com.webank.webase.node.mgr.deploy.service.NodeAsyncService;
import com.webank.webase.node.mgr.deploy.service.PathService;
import com.webank.webase.node.mgr.front.FrontService;
import com.webank.webase.node.mgr.front.entity.TbFront;
import com.webank.webase.node.mgr.frontgroupmap.FrontGroupMapService;
import com.webank.webase.node.mgr.group.GroupService;
import com.webank.webase.node.mgr.group.entity.TbGroup;
import com.webank.webase.node.mgr.node.NodeService;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

/**
 * chain monitor
 * chain deploy detail
 */
@Log4j2
@Service
public class ChainService {

    @Autowired private TbChainMapper tbChainMapper;
    @Autowired private TbConfigMapper tbConfigMapper;
    @Autowired private TbAgencyMapper tbAgencyMapper;
    @Autowired private TbHostMapper tbHostMapper;

    @Autowired
    private ConstantProperties cproperties;
    @Autowired
    private FrontService frontService;
    @Autowired
    private AgencyService agencyService;
    @Autowired
    private GroupService groupService;
    @Autowired
    private RestTemplate genericRestTemplate;
    @Autowired
    private PathService pathService;
    @Autowired
    private DeployShellService deployShellService;
    @Autowired
    private HostService hostService;
    @Autowired
    private NodeService nodeService;
    @Autowired
    private FrontGroupMapService frontGroupMapService;
    @Autowired
    private ConstantProperties constant;
    @Autowired
    private NodeAsyncService nodeAsyncService;
    @Autowired
    private CertService certService;
    @Autowired
    private AnsibleService ansibleService;
    @Autowired
    private ConfigService configService;

    @Autowired private DockerCommandService dockerOptions;

    /**
     * get chain info.
     */
    public Object getChainMonitorInfo(Integer frontId, LocalDateTime beginDate,
        LocalDateTime endDate, LocalDateTime contrastBeginDate,
        LocalDateTime contrastEndDate, int gap, int groupId) {
        log.debug(
            "start getChainMonitorInfo.  frontId:{} beginDate:{} endDate:{}"
                + " contrastBeginDate:{} contrastEndDate:{} gap:{} groupId:{}",
            frontId, beginDate, endDate, contrastBeginDate, contrastEndDate, gap, groupId);

        // request param to str
        List<Object> valueList = Arrays
            .asList(beginDate, endDate, contrastBeginDate, contrastEndDate, gap, groupId);
        List<String> nameList = Arrays
            .asList("beginDate", "endDate", "contrastBeginDate", "contrastEndDate", "gap",
                "groupId");

        String chainUrlParam = NodeMgrTools.convertUrlParam(nameList, valueList);

        // query by front Id
        TbFront tbFront = frontService.getById(frontId);
        if (tbFront == null) {
            throw new NodeMgrException(ConstantCode.INVALID_FRONT_ID);
        }

        // request url
        String url = String
            .format(cproperties.getFrontUrl(), tbFront.getFrontIp(), tbFront.getFrontPort(),
                URI_CHAIN);
        url = url + "?" + chainUrlParam;
        log.info("getChainMonitorInfo request url:{}", url);
        try {
            Object rspObj = genericRestTemplate.getForObject(url, Object.class);
            log.debug("end getChainMonitorInfo. rspObj:{}", JsonTools.toJSONString(rspObj));
            return rspObj;
        } catch (ResourceAccessException e) {
            log.error("getChainMonitorInfo. ResourceAccessException:{}", e);
            throw new NodeMgrException(ConstantCode.REQUEST_FRONT_FAIL);
        }

    }


    @Transactional(propagation = Propagation.REQUIRED)
    public TbChain insert(String chainName, String chainDesc, String version, byte encryptType, ChainStatusEnum status, RunTypeEnum runTypeEnum, String webaseSignAddr ) throws NodeMgrException {
        TbChain chain = TbChain.init(chainName, chainDesc, version, encryptType, status, runTypeEnum, webaseSignAddr);

        if (tbChainMapper.insertSelective(chain) != 1 || chain.getId() <= 0) {
            throw new NodeMgrException(ConstantCode.INSERT_CHAIN_ERROR);
        }
        return chain;
    }


    @Transactional(propagation = Propagation.REQUIRED)
    public boolean updateStatus(int chainId, ChainStatusEnum newStatus) {
        log.info("Update chain:[{}] status to:[{}]",chainId, newStatus.toString());
        int count =  this.tbChainMapper.updateChainStatus(chainId, new Date(), newStatus.getId());
        return count == 1;
    }


    @Transactional(propagation = Propagation.REQUIRED)
    public boolean updateStatus(String chainName, ChainStatusEnum newStatus) {
        log.info("Update chain:[{}] status to:[{}]",chainName, newStatus.toString());
        TbChain chain = tbChainMapper.getByChainName(chainName);
        int count =  this.tbChainMapper.updateChainStatus(chain.getId(), new Date(), newStatus.getId());
        return count == 1;
    }


    @Transactional(propagation = Propagation.REQUIRED)
    public void upgrade(TbChain chain , String newTagVersion ) {
        log.info("Upgrade chain:[{}] from version:[{}] to version:[{}].",
                chain.getChainName(), chain.getVersion(), newTagVersion);

        TbChain newChain = new TbChain();
        newChain.setId(chain.getId());
        newChain.setVersion(newTagVersion);
        newChain.setModifyTime(new Date());
        newChain.setChainStatus(ChainStatusEnum.UPGRADING.getId());
        int count = this.tbChainMapper.updateByPrimaryKeySelective(newChain);

        if(count != 1){
            throw new NodeMgrException(ConstantCode.UPDATE_CHAIN_WITH_NEW_VERSION_ERROR);
        }

        // restart front
        log.info("Upgrade front:[{}] to version:[{}].",chain.getVersion() , newTagVersion);
        this.frontService.upgrade(chain.getId(),newTagVersion);

        // restart chain
        this.nodeAsyncService.asyncStartChain(chain.getId(), OptionType.MODIFY_CHAIN, ChainStatusEnum.RUNNING ,
                ChainStatusEnum.UPGRADING_FAILED, FrontStatusEnum.STARTING, FrontStatusEnum.RUNNING, FrontStatusEnum.STOPPED);
    }


    /**
     *
     * @param ip
     * @param rootDirOnHost
     * @param chainName
     */
    public void mvChainOnRemote(String ip, String rootDirOnHost, String chainName){
        log.info("mvChainOnRemote ip:{}, rootDirHost:{},chainName:{}", ip, rootDirOnHost, chainName);
        // create /opt/fisco/deleted-tmp/ as a parent dir
        String deleteRootOnHost = PathService.getDeletedRootOnHost(rootDirOnHost);
        ansibleService.execCreateDir(ip, deleteRootOnHost);

        // like /opt/fisco/default_chain
        String src_chainRootOnHost = PathService.getChainRootOnHost(rootDirOnHost, chainName);
        // move to /opt/fisco/deleted-tmp/default_chain-yyyyMMdd_HHmmss
        String dst_chainDeletedRootOnHost = PathService.getChainDeletedRootOnHost(rootDirOnHost, chainName);

        ansibleService.mvDirOnRemote(ip, src_chainRootOnHost, dst_chainDeletedRootOnHost);
        log.info("end mvChainOnRemote");

    }

    /**
     * delete db data and local config files by chainName
     * @param chainName
     */
    @Transactional
    public void delete(String chainName) {
        log.info("start delete chain:{}", chainName);
        int errorFlag = 0;
        TbChain chain = tbChainMapper.getByChainName(chainName);
        if (chain == null) {
            throw new NodeMgrException(ConstantCode.CHAIN_NAME_NOT_EXISTS_ERROR);
        }

        isChainRunning.set(false);

        log.info("Delete host's chain dir by chain id:[{}].", chain.getId());
        hostService.deleteHostChainDir(chain);

        log.info("Delete agency and front data by chain id:[{}].", chain.getId());
        // delete agency and front by chain id and mv host's old chain dir
        agencyService.deleteByChainId(chain.getId());


        // delete group data
        this.groupService.deleteGroupByChainId(chain.getId());

        log.info("Delete chain data by chain id:[{}].", chain.getId());
        this.tbChainMapper.deleteByPrimaryKey(chain.getId());

        log.info("Delete chain:[{}] config files", chainName);
        try {
            this.pathService.deleteChain(chainName);
        } catch (IOException e) {
            errorFlag++;
            log.error("Delete chain config files error:[]", e);
            log.error("Please delete/move chain config files manually");
        }

        // delete all certs
        this.certService.deleteAll();
        // set pull cert to false
        CertTools.isPullFrontCertsDone = false;

        // if error occur, throw out finally
        if (errorFlag != 0) {
            log.error("Delete chain config files error. Check out upper error log");
            throw new NodeMgrException(ConstantCode.DELETE_NODE_DIR_ERROR);
        }
    }

    /**
     * gen chain config(cert&config) and init chain db data, generate front'yml
     * @param chainName
     * @param ipConf
     * @param imageTag
     * @return whether gen success
     */
    @Transactional
    public boolean generateConfigLocalAndInitDb(String chainName, List<DeployNodeInfo> deployNodeInfoList,
        String[] ipConf, String imageTag, int encryptType, String webaseSignAddr, String agencyName) {
        log.info("Check chainName exists....");
        TbChain chain = tbChainMapper.getByChainName(chainName);
        if (chain != null) {
            throw new NodeMgrException(ConstantCode.CHAIN_NAME_EXISTS_ERROR);
        }

        // check tagId existed
        // 1.4.3 only version tag, not include encryptType in value;
        // 1.4.3 use imageTag instead of tagId
        configService.checkValueInDb(imageTag);

        // parse ipConf config
        log.info("Parse ipConf content....");
        List<IpConfigParse> ipConfigParseList = IpConfigParse.parseIpConf(ipConf);

        // exec build_chain.sh shell script generate config and cert
        // 1.4.3 use bash to generate not ansible
        log.info("Locally exec build_chain....");
        ProgressTools.setGenConfig();
        String chainVersion = imageTag;
        if (chainVersion.startsWith("v")) {
            chainVersion = chainVersion.substring(1);
            log.info("execBuildChain chainVersion:{}", chainVersion);
        }
        deployShellService.execBuildChain(encryptType, ipConf, chainName, chainVersion);

        try {
            log.info("Init chain front node db data....");
            // save chain data in db, generate front's yml
            ((ChainService) AopContext.currentProxy()).initChainDbData(chainName, deployNodeInfoList,
                ipConfigParseList, webaseSignAddr, imageTag, (byte)encryptType, agencyName);
        } catch (Exception e) {
            log.error("Init chain:[{}] data error. remove generated files:[{}]",
                    chainName, this.pathService.getChainRoot(chainName), e);
            try {
                this.pathService.deleteChain(chainName);
            } catch (IOException ex) {
                log.error("Delete chain directory error when init chain data throws an exception.", e);
                throw new NodeMgrException(ConstantCode.DELETE_CHAIN_ERROR);
            }
            throw e;
        }

        return true;

    }

    /**
     * init chain data and gen front yml
     * @param chainName
     * @param ipConfigParseList
     * @param webaseSignAddr
     * @param imageTag(from tb_config value)
     * @param encryptType
     */
    @Transactional
    public void initChainDbData(String chainName,  List<DeployNodeInfo> deployNodeInfoList, List<IpConfigParse> ipConfigParseList,
                                String webaseSignAddr, String imageTag, byte encryptType, String agencyName){
        log.info("start initChainDbData chainName:{}, ipConfigParseList:{}",
            chainName, ipConfigParseList);
        ProgressTools.setInitChainData();
        // insert chain
        final TbChain newChain = ((ChainService) AopContext.currentProxy()).insert(chainName, chainName,
            imageTag, encryptType, ChainStatusEnum.INITIALIZED,
                RunTypeEnum.DOCKER, webaseSignAddr);

        // all host ips
        Map<String, TbHost> newIpHostMap = new HashMap<>();

        // insert agency, host , group
        ipConfigParseList.forEach((config) -> {
            // insert agency if new
            TbAgency agency = this.agencyService.insertIfNew(agencyName, newChain.getId(), chainName);

            // insert host if new
            TbHost host = tbHostMapper.getByIp(config.getIp());

            // insert group if new
            config.getGroupIdSet().forEach((groupId) -> {
                this.groupService.insertIfNew(groupId, 0, "deploy", GroupType.DEPLOY,
                        GroupStatus.MAINTAINING, newChain.getId(), newChain.getChainName());
            });

            newIpHostMap.putIfAbsent(config.getIp(), host);
        });

        // insert nodes for all hosts. there may be multiple nodes on a host.
        newIpHostMap.keySet().forEach((ip) -> {
            List<Path> nodePathList = null;
            try {
                 nodePathList = pathService.listHostNodesPath(newChain.getChainName(), ip);
            } catch (Exception e) {
                throw new NodeMgrException(ConstantCode.LIST_HOST_NODE_DIR_ERROR.attach(ip));
            }

            for (Path nodeRoot : CollectionUtils.emptyIfNull(nodePathList)) {
                // get node properties
                NodeConfig nodeConfig = NodeConfig.read(nodeRoot, encryptType);

                // get frontPort
                DeployNodeInfo targetNode = this.getFrontPort(deployNodeInfoList, ip, nodeConfig.getChannelPort());
                if (targetNode == null) {
                    throw new NodeMgrException(ConstantCode.DEPLOY_INFO_NOT_MATCH_IP_CONF);
                }
                int frontPort = targetNode.getFrontPort();

                // host
                TbHost host = newIpHostMap.get(ip);
                // agency
                TbAgency agency = this.tbAgencyMapper.getByChainIdAndAgencyName(newChain.getId(), agencyName);
                // insert front
                TbFront front = TbFront.init(nodeConfig.getNodeId(), ip, frontPort,
                        agency.getId(), agency.getAgencyName(), imageTag,
                        RunTypeEnum.DOCKER , host.getId(), nodeConfig.getHostIndex(), imageTag,
                        DockerCommandService.getContainerName(host.getRootDir(), chainName,
                        nodeConfig.getHostIndex()), nodeConfig.getJsonrpcPort(), nodeConfig.getP2pPort(),
                        nodeConfig.getChannelPort(), newChain.getId(), newChain.getChainName(), FrontStatusEnum.INITIALIZED);
                this.frontService.insert(front);

                // insert node and front group mapping
                nodeConfig.getGroupIdSet().forEach((groupId) -> {
                    // insert node
                    String nodeName = NodeService.getNodeName(groupId, nodeConfig.getNodeId());
                    this.nodeService.insert(nodeConfig.getNodeId(), nodeName, groupId, ip, nodeConfig.getP2pPort(),
                            nodeName, DataStatus.STARTING);

                    // insert front group mapping
                    this.frontGroupMapService.newFrontGroup(front.getFrontId(), groupId, GroupStatus.MAINTAINING);

                    // update node count of group
                    TbGroup group = this.groupService.getGroupById(groupId);
                    this.groupService.updateGroupNodeCount(groupId, group.getNodeCount() + 1 );

                    // update group timestamp and node list
                    Pair<Long, List<String>> longListPair = nodeConfig.getGroupIdToTimestampNodeListMap().get(groupId);
                    if (longListPair != null) {
                        this.groupService.updateTimestampNodeIdList(groupId,longListPair.getKey(),longListPair.getValue());
                    }
                });

                // generate front application.yml
                try {
                    ThymeleafUtil.newFrontConfig(nodeRoot,encryptType, nodeConfig.getChannelPort(),
                            frontPort, webaseSignAddr);
                } catch (IOException e) {
                    throw new NodeMgrException(ConstantCode.GENERATE_FRONT_YML_ERROR);
                }
            }
        });
    }

    /**
     *
     * @param chain
     * @return
     */
    public int progress(TbChain chain) {
        int progress = ChainStatusEnum.progress(chain.getChainStatus());
        switch (progress){
            // deploy or upgrade failed
            case NumberUtil.PERCENTAGE_FAILED:

            // deploy or upgrade success
            case NumberUtil.PERCENTAGE_FINISH:
                return progress;
            default:
                break;
        }

        progress = this.hostService.hostProgress(chain.getId());
        // host init error
        if (progress == NumberUtil.PERCENTAGE_FAILED) {
            return NumberUtil.PERCENTAGE_FAILED;
        }
        if(progress < NumberUtil.PERCENTAGE_FINISH){
            // host init in progress
            return progress/2;
        }


        // check front start
        progress = this.frontService.frontProgress(chain.getId());

        return 50 + (progress / 2);
    }

    /**
     * Chains is running, default not.
     */
    public static AtomicBoolean isChainRunning  = new AtomicBoolean(false);

    /**
     *  run task.
     *  if chain running, return true
     * @return
     */
    public boolean runTask(){
        // 0, original deploy chain first; 1, deploy chain visually
        if (constant.getDeployType() == 0 ) {
            log.info("Run task:[DeployType:{}, isChainRunning:{}]", constant.getDeployType(), isChainRunning.get());
            return true;
        }
        // set default chain status
        TbChain default_chain = this.tbChainMapper.getByChainName("default_chain");
        if (default_chain != null && default_chain.getChainStatus() == ChainStatusEnum.RUNNING.getId()){
            isChainRunning.set(true);
        }

        log.info("Run task:[DeployType:{}, isChainRunning:{}]", constant.getDeployType(),isChainRunning.get());
        return isChainRunning.get();
    }

    public DeployNodeInfo getFrontPort(List<DeployNodeInfo> deployNodeInfoList, String ip, int channelPort) {
        DeployNodeInfo targetNodeInfo = null;
        for (DeployNodeInfo nodeInfo : deployNodeInfoList) {
            if (ip.equals(nodeInfo.getIp()) && channelPort == nodeInfo.getChannelPort()) {
                targetNodeInfo = nodeInfo;
            }
        }
        return targetNodeInfo;
    }
}

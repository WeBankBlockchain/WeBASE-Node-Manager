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
package com.webank.webase.node.mgr.chain;

import static com.webank.webase.node.mgr.frontinterface.FrontRestTools.URI_CHAIN;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.web3j.crypto.EncryptType;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.enums.ChainStatusEnum;
import com.webank.webase.node.mgr.base.enums.DataStatus;
import com.webank.webase.node.mgr.base.enums.FrontStatusEnum;
import com.webank.webase.node.mgr.base.enums.GroupStatus;
import com.webank.webase.node.mgr.base.enums.GroupType;
import com.webank.webase.node.mgr.base.enums.OptionType;
import com.webank.webase.node.mgr.base.enums.RunTypeEnum;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.base.properties.ConstantProperties;
import com.webank.webase.node.mgr.base.tools.CertTools;
import com.webank.webase.node.mgr.base.tools.JsonTools;
import com.webank.webase.node.mgr.base.tools.NodeMgrTools;
import com.webank.webase.node.mgr.base.tools.NumberUtil;
import com.webank.webase.node.mgr.base.tools.SshTools;
import com.webank.webase.node.mgr.base.tools.ThymeleafUtil;
import com.webank.webase.node.mgr.cert.CertService;
import com.webank.webase.node.mgr.deploy.entity.IpConfigParse;
import com.webank.webase.node.mgr.deploy.entity.NodeConfig;
import com.webank.webase.node.mgr.deploy.entity.TbAgency;
import com.webank.webase.node.mgr.deploy.entity.TbChain;
import com.webank.webase.node.mgr.deploy.entity.TbConfig;
import com.webank.webase.node.mgr.deploy.entity.TbHost;
import com.webank.webase.node.mgr.deploy.mapper.TbAgencyMapper;
import com.webank.webase.node.mgr.deploy.mapper.TbChainMapper;
import com.webank.webase.node.mgr.deploy.mapper.TbConfigMapper;
import com.webank.webase.node.mgr.deploy.service.AgencyService;
import com.webank.webase.node.mgr.deploy.service.DeployShellService;
import com.webank.webase.node.mgr.deploy.service.HostService;
import com.webank.webase.node.mgr.deploy.service.NodeAsyncService;
import com.webank.webase.node.mgr.deploy.service.PathService;
import com.webank.webase.node.mgr.deploy.service.docker.DockerOptions;
import com.webank.webase.node.mgr.front.FrontService;
import com.webank.webase.node.mgr.front.entity.TbFront;
import com.webank.webase.node.mgr.frontgroupmap.FrontGroupMapService;
import com.webank.webase.node.mgr.group.GroupService;
import com.webank.webase.node.mgr.group.entity.TbGroup;
import com.webank.webase.node.mgr.node.NodeService;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class ChainService {

    @Autowired private TbChainMapper tbChainMapper;
    @Autowired private TbConfigMapper tbConfigMapper;
    @Autowired private TbAgencyMapper tbAgencyMapper;

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

        Object rspObj = genericRestTemplate.getForObject(url, Object.class);
        log.debug("end getChainMonitorInfo. rspObj:{}", JsonTools.toJSONString(rspObj));
        return rspObj;
    }


    @Transactional(propagation = Propagation.REQUIRED)
    public TbChain insert(String chainName, String chainDesc, String version, byte encryptType, ChainStatusEnum status,
                          String rootDirOnHost, RunTypeEnum runTypeEnum, String webaseSignAddr ) throws NodeMgrException {
        TbChain chain = TbChain.init(chainName, chainDesc, version, encryptType, status, rootDirOnHost, runTypeEnum,webaseSignAddr);

        if (tbChainMapper.insertSelective(chain) != 1 || chain.getId() <= 0) {
            throw new NodeMgrException(ConstantCode.INSERT_CHAIN_ERROR);
        }
        return chain;
    }


    @Transactional(propagation = Propagation.REQUIRED)
    public boolean updateStatus(int chainId, ChainStatusEnum newStatus) {
        log.info("Update chain:[{}] status to:[{}]",chainId, newStatus.toString());
        TbChain newChain = new TbChain();
        newChain.setId(chainId);
        newChain.setChainStatus(newStatus.getId());
        newChain.setModifyTime(new Date());
        return this.tbChainMapper.updateByPrimaryKeySelective(newChain) == 1;

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

        // start chain
        this.nodeAsyncService.upgradeStartChain(chain.getId(), OptionType.MODIFY);
    }


    /**
     *
     * @param ip
     * @param rootDirOnHost
     * @param chainName
     */
    public static void mvChainOnRemote(String ip,String rootDirOnHost,String chainName,String sshUser,int sshPort,String privateKey){
        // create /opt/fisco/deleted-tmp/ as a parent dir
        String deleteRootOnHost = PathService.getDeletedRootOnHost(rootDirOnHost);
        SshTools.createDirOnRemote(ip, deleteRootOnHost,sshUser,sshPort,privateKey);

        // like /opt/fisco/default_chain
        String src_chainRootOnHost = PathService.getChainRootOnHost(rootDirOnHost, chainName);
        // move to /opt/fisco/deleted-tmp/default_chain-yyyyMMdd_HHmmss
        String dst_chainDeletedRootOnHost = PathService.getChainDeletedRootOnHost(rootDirOnHost, chainName);

        SshTools.mvDirOnRemote(ip,src_chainRootOnHost,dst_chainDeletedRootOnHost,sshUser,sshPort,privateKey);
    }

    /**
     *
     * @param chainName
     */
    @Transactional
    public void delete(String chainName) throws IOException {
        TbChain chain = tbChainMapper.getByChainName(chainName);
        if (chain == null) {
            throw new NodeMgrException(ConstantCode.CHAIN_NAME_NOT_EXISTS_ERROR);
        }

        // delete agency
        this.agencyService.deleteByChainId(chain.getId());

        // delete group
        this.groupService.deleteGroupByChainId(chain.getId());

        log.info("Delete chain data by chain id:[{}].", chain.getId());
        this.tbChainMapper.deleteByPrimaryKey(chain.getId());

        log.info("Delete chain:[{}] config files", chainName);
        this.pathService.deleteChain(chainName);

        // delete all certs
        this.certService.deleteAll();

        // set pull cert to false
        CertTools.isPullFrontCertsDone = false;
    }

    /**
     *
     * @param chainName
     * @param ipConf
     * @param tagId
     * @param rootDirOnHost
     */
    @Transactional
    public void generateChainConfig(String chainName, String[] ipConf,
                int tagId, String rootDirOnHost, String webaseSignAddr,
                String sshUser,int sshPort,int dockerPort){
        log.info("Check chainName exists....");
        TbChain chain = tbChainMapper.getByChainName(chainName);
        if (chain != null) {
            throw new NodeMgrException(ConstantCode.CHAIN_NAME_EXISTS_ERROR);
        }

        // check tagId existed
        TbConfig imageConfig = this.tbConfigMapper.selectByPrimaryKey(tagId);
        if (imageConfig == null || StringUtils.isBlank(imageConfig.getConfigValue())) {
            throw new NodeMgrException(ConstantCode.TAG_ID_PARAM_ERROR);
        }

        byte encryptType = (byte) (imageConfig.getConfigValue().endsWith("-gm") ?
                EncryptType.SM2_TYPE : EncryptType.ECDSA_TYPE);

        // parse ipConf config
        log.info("Parse ipConf content....");
        List<IpConfigParse> ipConfigParseList = IpConfigParse.parseIpConf(ipConf,sshUser,sshPort,constant.getPrivateKey());

        // exec build_chain.sh shell script
        deployShellService.execBuildChain(encryptType, ipConf, chainName);

        try {
            // generate chain config
            ((ChainService) AopContext.currentProxy()).initChainDbData(chainName,ipConfigParseList,
                    rootDirOnHost,webaseSignAddr,imageConfig,encryptType,sshUser,sshPort,dockerPort);
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

    }

    /**
     *
     * @param chainName
     * @param ipConfigParseList
     * @param rootDirOnHost
     * @param webaseSignAddr
     * @param imageConfig
     * @param encryptType
     */
    @Transactional
    public void initChainDbData(String chainName, List<IpConfigParse> ipConfigParseList,
                                String rootDirOnHost, String webaseSignAddr, TbConfig imageConfig, byte encryptType,
                                String sshUser,int sshPort,int dockerPort){

        // insert chain
        final TbChain newChain = ((ChainService) AopContext.currentProxy()).insert(chainName, chainName,
                imageConfig.getConfigValue(), encryptType, ChainStatusEnum.INITIALIZED, rootDirOnHost,
                RunTypeEnum.DOCKER, webaseSignAddr);

        // all host ips
        Map<String,TbHost> newIpHostMap = new HashMap<>();

        // insert agency, host , group
        ipConfigParseList.forEach((config) -> {
            // insert agency if new
            TbAgency agency = this.agencyService.insertIfNew(config.getAgencyName(),newChain.getId(),chainName);

            // insert host if new
            TbHost host = this.hostService.insertIfNew(agency.getId(), agency.getAgencyName(), config.getIp(), rootDirOnHost,
                    sshUser,sshPort,dockerPort);

            // insert group if new
            config.getGroupIdSet().forEach((groupId) -> {
                this.groupService.insertIfNew(groupId, config.getNum(), "deploy", GroupType.DEPLOY,
                        GroupStatus.MAINTAINING, newChain.getId(), newChain.getChainName());
            });

            newIpHostMap.putIfAbsent(config.getIp(),host);
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
                NodeConfig nodeConfig = NodeConfig.read(nodeRoot);

                // frontPort = 5002 + indexOnHost(0,1,2,3...)
                int frontPort = constant.getDefaultFrontPort() + nodeConfig.getHostIndex();

                // host
                TbHost host = newIpHostMap.get(ip);
                // agency
                TbAgency agency = this.tbAgencyMapper.selectByPrimaryKey(host.getAgencyId());
                // insert front
                TbFront front = TbFront.init(nodeConfig.getNodeId(), ip, frontPort,
                        agency.getId(),agency.getAgencyName(), imageConfig.getConfigValue(),
                        RunTypeEnum.DOCKER , host.getId(), nodeConfig.getHostIndex(),
                        imageConfig.getConfigValue(), DockerOptions.getContainerName(rootDirOnHost, chainName,
                        nodeConfig.getHostIndex()), nodeConfig.getJsonrpcPort(), nodeConfig.getP2pPort(),
                        nodeConfig.getChannelPort(), newChain.getId(), newChain.getChainName(), FrontStatusEnum.INITIALIZED);
                this.frontService.insert(front);


                // insert node and front group mapping
                Set<Integer> groupIdSet = ipConfigParseList.stream().map(IpConfigParse::getGroupIdSet)
                        .flatMap(Collection::stream).collect(Collectors.toSet());

                groupIdSet.forEach((groupId) -> {
                    // insert node
                    String nodeName = NodeService.getNodeName(groupId, nodeConfig.getNodeId());
                    this.nodeService.insert(nodeConfig.getNodeId(), nodeName,
                            groupId, ip, nodeConfig.getP2pPort(),
                            nodeName, DataStatus.STARTING);

                    // insert front group mapping
                    this.frontGroupMapService.newFrontGroup(front.getFrontId(), groupId, GroupStatus.MAINTAINING);

                    // update node count of goup
                    TbGroup group = this.groupService.getGroupById(groupId);
                    this.groupService.updateGroupNodeCount(groupId, group.getNodeCount() + 1 );
                });

                // generate front application.yml
                try {
                    ThymeleafUtil.newFrontConfig(nodeRoot,encryptType,nodeConfig.getChannelPort(),
                            frontPort,webaseSignAddr);
                } catch (IOException e) {
                    throw new NodeMgrException(ConstantCode.GENERATE_FRONT_YML_ERROR);
                }
            }
        });
    }

    /**
     *
     * @param chainName
     * @return
     */
    public TbChain changeChainStatusToDeploying(String chainName){
        if (StringUtils.isBlank(chainName)) {
            log.error("Chain name:[{}] is blank, deploy error.", chainName);
            return null;
        }

        // check chain status
        TbChain tbChain = null;
        synchronized (ChainService.class) {
            tbChain = this.tbChainMapper.getByChainName(chainName);
            // chain not exists
            if (tbChain == null) {
                log.error("Chain:[{}] does not exist, deploy error.", chainName);
                return null;
            }

            // check chain status
            // TODO. deploying but no thread is deploying, still need to deploy
            if (ChainStatusEnum.successOrDeploying(tbChain.getChainStatus())) {
                log.error("Chain:[{}] is already deployed success or deploying:[{}].", chainName, tbChain.getChainStatus());
                return null;
            }

            // update chain status
            log.info("Start to deploy chain:[{}:{}] from status:[{}]",
                    tbChain.getId(), tbChain.getChainName(), tbChain.getChainStatus());

            if (!((ChainService) AopContext.currentProxy()).updateStatus(tbChain.getId(), ChainStatusEnum.DEPLOYING)) {
                log.error("Start to deploy chain:[{}:{}], but update status to deploying failed.",
                        tbChain.getId(), tbChain.getChainName());
                return null;
            }
        }
        return tbChain;
    }

    /**
     *
     * @param chain
     * @return
     */
    public int progress(TbChain chain){
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
        if (progress == NumberUtil.PERCENTAGE_FAILED){
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
}

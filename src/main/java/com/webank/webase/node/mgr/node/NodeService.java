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
package com.webank.webase.node.mgr.node;

import com.alibaba.fastjson.JSON;
import com.webank.webase.node.mgr.base.entity.BaseResponse;
import com.webank.webase.node.mgr.base.entity.ConstantCode;
import com.webank.webase.node.mgr.base.enums.NodeType;
import com.webank.webase.node.mgr.base.enums.OrgType;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.base.properties.ConstantProperties;
import com.webank.webase.node.mgr.base.tools.NodeMgrTools;
import com.webank.webase.node.mgr.contract.ContractService;
import com.webank.webase.node.mgr.front.FrontService;
import com.webank.webase.node.mgr.group.GroupService;
import com.webank.webase.node.mgr.organization.OrganizationService;
import com.webank.webase.node.mgr.organization.TbOrganization;
import com.webank.webase.node.mgr.user.UserService;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

/**
 * services for node data.
 */
@Log4j2
@Service
public class NodeService {

    @Autowired
    private NodeMapper nodeMapper;
    @Autowired
    private GroupService groupService;
    @Autowired
    private OrganizationService organizationService;
    @Qualifier(value = "genericRestTemplate")
    @Autowired
    private RestTemplate genericRestTemplate;
    @Autowired
    private ConstantProperties cp;
    @Autowired
    private ContractService contractService;
    @Autowired
    private UserService userService;
    @Autowired
    private FrontService frontService;

    /**
     * get node info from front.
     */
    public NodeInfo getNodeInfoFromFront(String nodeIp, Integer frontPort) throws NodeMgrException {
        log.debug("start getNodeInfoFromFront. nodeIp:{} frontPort:{}", nodeIp, frontPort);
        String url = String
            .format(FrontService.FRONT_URL, nodeIp, frontPort, FrontService.FRONT_NODE_INFO);
        log.info("getNodeInfoFromFront. url:{}", url);
        // get node info
        BaseResponse frontRsp = genericRestTemplate.getForObject(url, BaseResponse.class);
        if (frontRsp.getCode() != 0) {
            log.warn("fail getNodeInfoFromFront url:{} errorMsg:{}", url, frontRsp.getMessage());
            throw new NodeMgrException(frontRsp.getCode(), frontRsp.getMessage());
        }

        NodeInfo nodeInfo = NodeMgrTools.object2JavaBean(frontRsp.getData(), NodeInfo.class);
        if (nodeInfo == null) {
            log.error("get node info fail. nodeInfo is null");
            throw new NodeMgrException(ConstantCode.SYSTEM_EXCEPTION);
        }

        log.debug("end getNodeInfoFromFront. nodeInfo:{}", JSON.toJSONString(nodeInfo));
        return nodeInfo;
    }

    /**
     * add new node data.
     */
    @Transactional
    public Integer addNodeInfo(Node node) throws NodeMgrException {
        log.debug("start addNodeInfo Node:{}", JSON.toJSONString(node));

        Integer groupId = node.getGroupId();
        String nodeIp = node.getNodeIp();
        Integer frontPort = node.getFrontPort();
        if (StringUtils.isBlank(nodeIp) || null == frontPort) {
            log.info("addNodeInfo fail. nodeIp:{} frontPort:{}", nodeIp, frontPort);
            throw new NodeMgrException(ConstantCode.IP_PORT_EMPTY);
        }

        // check group id
        groupService.checkgroupId(groupId);

        // param
        NodeParam queryParam = new NodeParam();
        queryParam.setGroupId(groupId);
        queryParam.setNodeIp(nodeIp);
        queryParam.setFrontPort(frontPort);

        // check netowrkid、nodeName
        Integer nodeCount = countOfNode(queryParam);
        if (nodeCount != null && nodeCount > 0) {
            log.info("node ip already exists");
            throw new NodeMgrException(ConstantCode.NODE_EXISTS);
        }

        // get node info
        NodeInfo nodeInfo = null;
        try {
            nodeInfo = getNodeInfoFromFront(nodeIp, frontPort);
        } catch (Exception ex) {
            log.info("addNode Fail", ex);
            throw new NodeMgrException(ConstantCode.INVALID_NODE_IP);
        }


        int orgType = OrgType.CURRENT.getValue();
        int nodeType =
            node.getNodeType() == null ? NodeType.CURRENT.getValue() : node.getNodeType();
        if (NodeType.OTHER.getValue() == nodeType) {
            orgType = OrgType.OTHER.getValue();
        } else if (NodeType.CURRENT.getValue() != nodeType) {
            log.info("invalid node type value:{}", nodeType);
            throw new NodeMgrException(ConstantCode.INVALID_NODE_TYPE);
        }

        // add organization info
        TbOrganization organization = new TbOrganization();
        organization.setGroupId(groupId);
        organization.setOrgName(nodeInfo.getOrgName());
        organization.setOrgType(orgType);
        Integer orgId = organizationService.addOrganizationInfo(organization);

        String nodeName = nodeIp + "_" + nodeInfo.getP2pport();

        // add row
        TbNode dbParam = new TbNode();
        dbParam.setGroupId(groupId);
        dbParam.setNodeIp(nodeIp);
        dbParam.setNodeName(nodeName);
        dbParam.setOrgId(orgId);
        dbParam.setP2pPort(nodeInfo.getP2pport());
        dbParam.setRpcPort(nodeInfo.getRpcport());
        dbParam.setChannelPort(nodeInfo.getChannelPort());
        dbParam.setNodeType(nodeType);
        dbParam.setFrontPort(frontPort);

        Integer affectRow = nodeMapper.addNodeRow(dbParam);
        if (affectRow == 0) {
            log.warn("affect 0 rows of tb_node");
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }

        /*if (!cp.getSupportTransaction()) {
            log.info("current config is not support transaction");
            return dbParam.getNodeId();
        }

        if (NodeType.CURRENT.getValue() != nodeType) {
            log.info("not need send trans, nodeType:{}", nodeType);
            return dbParam.getNodeId();
        }

        // query system contract:node
        TbContract systemContract = contractService
            .querySystemContract(groupId, cp.getSysContractNodeName());
        if (systemContract == null) {
            log.error("not found contract:node");
            new NodeMgrException(ConstantCode.NOT_FOUND_NODECONTRACT);
        }
        String systemContractV = systemContract.getContractVersion();

        String ns = ConstantProperties.NAME_SPRIT;
        // contract func param
        List<Object> funcParam = new LinkedList<>();
        funcParam.add(dbParam.getNodeId());
        funcParam.add(
            nodeName + ns + nodeInfo.getRpcport() + ns + nodeInfo.getP2pport() + ns + nodeInfo
                .getChannelPort());
        funcParam.add(groupId);
        funcParam.add(nodeInfo.getOrgName());
        funcParam.add(nodeIp);

        // get systemUserId
        Integer systemUserId = userService.queryIdOfSystemUser(groupId);

        // http post param
        TransactionParam postParam = new TransactionParam(systemUserId, systemContractV,
            cp.getSysContractNodeName(), "insertnode", funcParam);

        // request node front
        frontService.sendTransaction(groupId, postParam);*/

        Integer nodeId = dbParam.getNodeId();
        log.debug("end addNodeInfo nodeId:{}", nodeId);
        return nodeId;
    }

    /**
     * query count of node.
     */
    public Integer countOfNode(NodeParam queryParam) throws NodeMgrException {
        log.debug("start countOfNode queryParam:{}", JSON.toJSONString(queryParam));
        try {
            Integer nodeCount = nodeMapper.countOfNode(queryParam);
            log.debug("end countOfNode nodeCount:{} queryParam:{}", nodeCount,
                JSON.toJSONString(queryParam));
            return nodeCount;
        } catch (RuntimeException ex) {
            log.error("fail countOfNode . queryParam:{}", queryParam, ex);
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }
    }

    /**
     * query node list by page.
     */
    public List<TbNode> qureyNodeList(NodeParam queryParam) throws NodeMgrException {
        log.debug("start qureyNodeList queryParam:{}", JSON.toJSONString(queryParam));

        // query node list
        List<TbNode> listOfNode = nodeMapper.listOfNode(queryParam);

        log.debug("end qureyNodeList listOfNode:{}", JSON.toJSONString(listOfNode));
        return listOfNode;
    }

    /**
     * query node info.
     */
    public TbNode queryByNodeId(Integer nodeId) throws NodeMgrException {
        log.debug("start queryNode nodeId:{}", nodeId);
        try {
            TbNode nodeRow = nodeMapper.queryByNodeId(nodeId);
            log.debug("end queryNode nodeId:{} TbNode:{}", nodeId, JSON.toJSONString(nodeRow));
            return nodeRow;
        } catch (RuntimeException ex) {
            log.error("fail queryNode . nodeId:{}", nodeId, ex);
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }
    }

    /**
     * query current node list.
     */
    public List<TbNode> queryCurrentNodeList(Integer groupId) throws NodeMgrException {
        NodeParam queryParam = new NodeParam();
        queryParam.setGroupId(groupId);
        queryParam.setNodeType(NodeType.CURRENT.getValue());
        return qureyNodeList(queryParam);
    }

    /**
     * update node info.
     */
    public void updateNodeInfo(TbNode tbNode) throws NodeMgrException {
        log.debug("start updateNodeInfo  param:{}", JSON.toJSONString(tbNode));
        Integer affectRow = 0;
        try {

            affectRow = nodeMapper.updateNodeInfo(tbNode);
        } catch (RuntimeException ex) {
            log.error("updateNodeInfo exception", ex);
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }

        if (affectRow == 0) {
            log.warn("affect 0 rows of tb_node");
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }
        log.debug("end updateNodeInfo");
    }

    /**
     * query tb_node by nodeIp and p2pPort.
     */
    public TbNode queryNodeByIpAndP2pPort(String nodeIp, Integer p2pPort) throws NodeMgrException {
        log.debug("start queryNodeByIpAndP2pPort nodeIp:{} p2pPort:{}", nodeIp, p2pPort);

        if (StringUtils.isBlank(nodeIp)) {
            log.error("fail getGroupIdByNode. nodeIp blank");
            throw new NodeMgrException(ConstantCode.NODE_IP_EMPTY);
        }
        if (p2pPort == null) {
            log.error("fail getGroupIdByNode. p2pPort null");
            throw new NodeMgrException(ConstantCode.NODE_P2P_PORT_EMPTY);
        }
        TbNode tbNode = null;
        try {
            tbNode = nodeMapper.queryNodeByIpAndP2pPort(nodeIp, p2pPort);
        } catch (RuntimeException ex) {
            log.error("fail queryNodeByIpAndP2pPort. nodeIp:{} p2pPort:{}", nodeIp, p2pPort, ex);
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }
        log.debug("end queryNodeByIpAndP2pPort nodeIp:{} p2pPort:{}", nodeIp, p2pPort);
        return tbNode;
    }

    /**
     * query node info.
     */
    public TbNode queryNodeInfo(NodeParam nodeParam) {
        log.debug("start queryNodeInfo nodeParam:{}", JSON.toJSONString(nodeParam));
        TbNode tbNode = nodeMapper.queryNodeInfo(nodeParam);
        log.debug("end queryNodeInfo result:{}", tbNode);
        return tbNode;
    }

    /**
     * delete by node id.
     */
    public void deleteByNodeId(Integer nodeId) throws NodeMgrException {
        log.debug("start deleteByNodeId nodeId:{}", nodeId);
        TbNode nodeRow = queryByNodeId(nodeId);
        if (nodeRow == null) {
            log.info("fail deleteByNodeId. invalid node ip");
            throw new NodeMgrException(ConstantCode.INVALID_NODE_IP);
        }

        Integer affectRow = nodeMapper.deleteByNodeId(nodeId);
        if (affectRow == 0) {
            log.warn("affect 0 rows of tb_node");
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }

        log.debug("end deleteByNodeId");
    }
}

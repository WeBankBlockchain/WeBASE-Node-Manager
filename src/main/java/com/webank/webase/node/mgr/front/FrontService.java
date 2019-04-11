/**
 * Copyright 2014-2019  the original author or authors.
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

import com.webank.webase.node.mgr.base.entity.ConstantCode;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.base.tools.NodeMgrTools;
import com.webank.webase.node.mgr.front.entity.FrontInfo;
import com.webank.webase.node.mgr.front.entity.FrontParam;
import com.webank.webase.node.mgr.front.entity.TbFront;
import com.webank.webase.node.mgr.frontgroupmap.FrontGroupMapService;
import com.webank.webase.node.mgr.frontinterface.FrontInterfaceService;
import com.webank.webase.node.mgr.group.GroupService;
import com.webank.webase.node.mgr.node.NodeService;
import com.webank.webase.node.mgr.node.entity.PeerInfo;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * service of web3.
 */
@Log4j2
@Service
public class FrontService {
    @Autowired
    private GroupService groupService;
    @Autowired
    private FrontMapper frontMapper;
    @Autowired
    private NodeService nodeService;
    @Autowired
    private FrontGroupMapService frontGroupMapService;
    @Autowired
    private FrontInterfaceService frontInterfaceService;

    /**
     * add new front
     */
    @Transactional
    public TbFront newFront(FrontInfo frontInfo) {
        TbFront tbFront = new TbFront();
        String frontIp = frontInfo.getFrontIp();
        Integer frontPort = frontInfo.getFrontPort();

        //check front ip and port
        checkFrontNotExist(frontIp,frontPort);
        //query group list
        List<String> groupIdList = frontInterfaceService.getGroupListFromSpecificFront(frontIp, frontPort);
        //copy attribute
        BeanUtils.copyProperties(frontInfo, tbFront);
        //save front info
        frontMapper.add(tbFront);
        for(String groupId:groupIdList){
            Integer group = Integer.valueOf(groupId);
            //peer in group
            List<PeerInfo>  peerList = frontInterfaceService.getPeersFromSpecificFront(frontIp,frontPort,group);
            //add groupId
            groupService.saveGroupId(group,peerList.size());
            //save front group map
            frontGroupMapService.newFrontGroup(tbFront.getFrontId(),group);
            //save nodes
            for(Object obj:peerList){
                PeerInfo peerInfo = NodeMgrTools.object2JavaBean(obj,PeerInfo.class);
                nodeService.addNodeInfo(group,peerInfo);
            }
        }
        return tbFront;
    }


    /**
     * check front ip and prot
     *
     * if exist:throw exception
     */
    private void checkFrontNotExist(String frontIp,int frontPort){
        FrontParam param = new FrontParam(null,frontIp,frontPort);
        int count = getFrontCount(param);
        if(count > 0)
            throw new NodeMgrException(ConstantCode.FRONT_EXISTS);
    }


    /**
     * get front count
     */
    public int getFrontCount(FrontParam param){
        return frontMapper.getCount(param);
    }

    /**
     * get front list
     */
    public List<TbFront> getFrontList(FrontParam param){
        return frontMapper.getList(param);
    }

    /**
     * remove front
     */
    public void removeFront(int frontId){
        //check frontId
        FrontParam param = new FrontParam();
        param.setFrontId(frontId);
        int count = getFrontCount(param);
        if(count==0){
            throw new NodeMgrException(ConstantCode.INVALID_FRONT_ID);
        }

        //remove front
        frontMapper.remove(frontId);

        //remove map
        frontGroupMapService.removeByFrontId(frontId);
    }
}

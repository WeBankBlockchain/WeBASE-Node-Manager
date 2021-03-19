/**
 * Copyright 2014-2021  the original author or authors.
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
package com.webank.webase.node.mgr.front.entity;

import com.webank.webase.node.mgr.base.enums.FrontStatusEnum;
import com.webank.webase.node.mgr.base.enums.RunTypeEnum;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class TbFront {
    private Integer frontId;
    private String nodeId;
    private String frontIp;
    private Integer frontPort;
    private String agency;
    private List<Integer> groupList;
    /**
     * node version and support version
     */
    private String clientVersion;
    private String supportVersion;
    /**
     * front server version
     */
    private String frontVersion;
    /**
     * sign server version
     */
    private String signVersion;
    private LocalDateTime createTime;
    private LocalDateTime modifyTime;
    /**
     * front status by now
     * @case1: front's node is abnormal
     * @case2: front's request fail/no response
     */
    private Integer status;


    private Byte runType;
    private Integer agencyId;
    private String agencyName;
    private Integer hostId;
    /**
     * node index bound with front, index is 0, ex: bound with node0
     */
    private Integer hostIndex;
    private String imageTag;
    private String containerName;
    private Integer jsonrpcPort;
    private Integer p2pPort;
    private Integer channelPort;

    private Integer chainId;
    private String chainName;

    public static TbFront init(
        String nodeId, String ip, int port,
        int agencyId,String agencyName, String clientVersion,
        RunTypeEnum runTypeEnum, int hostId, int hostIndex,
        String imageTag, String containerName , int jsonrpcPort,
        int p2pPort, int channelPort, int chainId,
        String chainName, FrontStatusEnum frontStatusEnum){

        String frontClientVersion = StringUtils.removeStart(clientVersion, "v");

        LocalDateTime now = LocalDateTime.now();
        TbFront front = new TbFront();
        front.setNodeId(nodeId);
        front.setFrontIp(ip);
        front.setFrontPort(port);
        front.setAgency(agencyName);
        front.setClientVersion(frontClientVersion);
        front.setSupportVersion(frontClientVersion);
        front.setCreateTime(now);
        front.setModifyTime(now);
        front.setRunType(runTypeEnum.getId());
        front.setAgencyId(agencyId);
        front.setAgencyName(agencyName);
        front.setHostId(hostId);
        front.setHostIndex(hostIndex);
        front.setImageTag(imageTag);
        front.setContainerName(containerName);
        front.setJsonrpcPort(jsonrpcPort);
        front.setP2pPort(p2pPort);
        front.setChannelPort(channelPort);
        front.setChainId(chainId);
        front.setChainName(chainName);
        front.setStatus(frontStatusEnum.getId());

        return front;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TbFront front = (TbFront) o;
        return Objects.equals(frontId, front.frontId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(frontId);
    }
}




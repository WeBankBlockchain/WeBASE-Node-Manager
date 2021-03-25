/**
 * Copyright 2014-2020  the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.webank.webase.node.mgr.deploy.entity;

import static com.webank.webase.node.mgr.base.code.ConstantCode.AGENCY_NAME_CONFIG_ERROR;
import static com.webank.webase.node.mgr.base.code.ConstantCode.GROUPS_CONFIG_ERROR;
import static com.webank.webase.node.mgr.base.code.ConstantCode.IP_CONFIG_LINE_ERROR;
import static com.webank.webase.node.mgr.base.code.ConstantCode.IP_FORMAT_ERROR;
import static com.webank.webase.node.mgr.base.code.ConstantCode.IP_NUM_ERROR;
import static com.webank.webase.node.mgr.base.code.ConstantCode.NODES_NUM_EXCEED_MAX_ERROR;
import static com.webank.webase.node.mgr.base.code.ConstantCode.NODE_PORT_CONFIG_ERROR;

import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.base.properties.ConstantProperties;
import com.webank.webase.node.mgr.base.tools.ValidateUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.ToString;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

@Data
@ToString
public class IpConfigParse {

    private String ip;
    private int num;
    private String agencyName;
    private Set<Integer> groupIdSet;
    private int channelPort;
    private int p2pPort;
    private int rpcPort;

    /**
     * Validate ipConf.
     *
     * @param ipConf
     * @return List<ConfigLine> entity of config for build_chain.
     * @throws NodeMgrException
     */
    public static List<IpConfigParse> parseIpConf(String[] ipConf) throws NodeMgrException {
        if (ArrayUtils.isEmpty(ipConf)){
            throw new NodeMgrException(ConstantCode.IP_CONF_PARAM_NULL_ERROR);
        }

        List<IpConfigParse> ipConfigParseList = new ArrayList<>();
        int totalNodeNum = 0;

        // check one ip could
        // key: host ip; value: agencyName
        Map<String, String> hostAgencyMap = new HashMap<>();
        Map<String, AtomicInteger> ipNodeCountMap = new HashMap<>();
        for (String line : ipConf) {
            if (StringUtils.isBlank(line)) {
                continue;
            }

            IpConfigParse ipConfigParse = parseLine(line);
            if (ipConfigParse == null) {
                continue;
            }

            // A host only belongs to one agency.
            if (hostAgencyMap.get(ipConfigParse.getIp()) != null &&
                    !StringUtils.equalsIgnoreCase(hostAgencyMap.get(ipConfigParse.getIp()), ipConfigParse.getAgencyName())) {
                throw new NodeMgrException(ConstantCode.HOST_ONLY_BELONGS_ONE_AGENCY_ERROR);
            }
            hostAgencyMap.put(ipConfigParse.getIp(), ipConfigParse.getAgencyName());

            if (ipConfigParse.getNum() <= 0) {
                throw new NodeMgrException(IP_NUM_ERROR.attach(line));
            }

            if(ipNodeCountMap.containsKey(ipConfigParse.getIp())) {
                ipNodeCountMap.get(ipConfigParse.getIp()).addAndGet(ipConfigParse.getNum());
            } else {
                ipNodeCountMap.put(ipConfigParse.getIp(),new AtomicInteger(ipConfigParse.getNum()));
            }

            totalNodeNum += ipConfigParse.getNum();
            ipConfigParseList.add(ipConfigParse);
        }
        if ( totalNodeNum < 2 ) {
            throw new NodeMgrException(ConstantCode.TWO_NODES_AT_LEAST);
        }

        if (CollectionUtils.isEmpty(ipConfigParseList)) {
            throw new NodeMgrException(ConstantCode.IP_CONF_PARAM_NULL_ERROR);
        }

//        ipNodeCountMap.entrySet().forEach( entry -> {
//            if (entry.getValue().get() > ConstantProperties.MAX_NODE_ON_HOST){
//                throw new NodeMgrException(NODES_NUM_EXCEED_MAX_ERROR.attach(entry.getKey()));
//            }
//        });

        return ipConfigParseList;
    }

    /**
     *
     * @param line 127.0.0.1:1 agency1 1,2 20200,30300,8645
     * @return
     * @throws NodeMgrException
     */
    public static IpConfigParse parseLine(String line) throws NodeMgrException {
        if (StringUtils.isBlank(line)) {
            return null;
        }
        String newLine = StringUtils.trim(line);

        String[] configArray = newLine.split(" ");
        if (ArrayUtils.getLength(configArray) < 3) {
            throw new NodeMgrException(IP_CONFIG_LINE_ERROR.attach(newLine));
        }

        IpConfigParse ipConfigParse = new IpConfigParse();

        String[] ipNumArray = configArray[0].split(":");
        if (ArrayUtils.getLength(ipNumArray) != 2) {
            throw new NodeMgrException(IP_NUM_ERROR.attach(configArray[0]));
        }
        try {
            // parse IP:Num
            if (! ValidateUtil.ipv4Valid(ipNumArray[0])){
                throw new NodeMgrException(IP_FORMAT_ERROR.attach( newLine));
            }
            ipConfigParse.ip = ipNumArray[0];
            ipConfigParse.num = Integer.parseInt(ipNumArray[1]);
        } catch (Exception e) {
            throw new NodeMgrException(IP_NUM_ERROR.attach(configArray[0]), e);
        }

        // parse agencyName
        if (!ValidateUtil.validateAgencyName(configArray[1])) {
            throw new NodeMgrException(AGENCY_NAME_CONFIG_ERROR.attach(configArray[1]));
        }
        ipConfigParse.agencyName = configArray[1];

        try {
            // parse groupIdList
            Set<Integer> groupIdSet = Arrays.stream(configArray[2].split(","))
                    .map((group) -> {
                        int groupId = Integer.parseInt(group);
                        if (groupId <= 0) {
                            throw new NodeMgrException(GROUPS_CONFIG_ERROR.attach(configArray[2]));
                        }
                        return groupId;
                    }).collect(Collectors.toSet());
            ipConfigParse.groupIdSet = groupIdSet;
        } catch (Exception e) {
            throw new NodeMgrException(GROUPS_CONFIG_ERROR.attach(configArray[2]), e);
        }

        // 20200,30300,8645
        String[] nodePortArray = configArray[3].split(",");
        if (ArrayUtils.getLength(nodePortArray) < 3) {
            throw new NodeMgrException(IP_CONFIG_LINE_ERROR.attach(newLine));
        }
        try {
            ipConfigParse.channelPort = Integer.parseInt(nodePortArray[0]);
            ipConfigParse.p2pPort = Integer.parseInt(nodePortArray[1]);
            ipConfigParse.rpcPort = Integer.parseInt(nodePortArray[2]);
        } catch (Exception e) {
            throw new NodeMgrException(NODE_PORT_CONFIG_ERROR.attach(configArray[3]), e);
        }

        return ipConfigParse;
    }



}


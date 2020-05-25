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
import static com.webank.webase.node.mgr.base.code.ConstantCode.IP_NUM_ERROR;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.webank.webase.node.mgr.base.code.RetCode;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.base.tools.ValidateUtil;

import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@ToString
public class ConfigLine {

    private String ip;
    private int num;
    private String agencyName;
    private Set<Integer> groupIdSet;

    public static ConfigLine parseLine(String line) throws NodeMgrException {
        if (StringUtils.isBlank(line)) {
            return null;
        }
        line = StringUtils.trim(line);

        String[] configArray = line.split(" ");
        if (ArrayUtils.getLength(configArray) < 3) {
            throw error(IP_CONFIG_LINE_ERROR, line);
        }

        ConfigLine configLine = new ConfigLine();

        String[] ipNumArray = configArray[0].split(":");
        if (ArrayUtils.getLength(ipNumArray) != 2) {
            throw error(IP_NUM_ERROR, configArray[0]);
        }
        try {
            // parse IP:Num
            configLine.ip = ipNumArray[0];
            configLine.num = Integer.parseInt(ipNumArray[1]);
        } catch (Exception e) {
            throw error(IP_NUM_ERROR, configArray[0], e);
        }

        // parse agencyName
        if (!ValidateUtil.validateAgencyName(configArray[1])) {
            throw error(AGENCY_NAME_CONFIG_ERROR, configArray[1]);
        }
        configLine.agencyName = configArray[1];

        try {
            // parse groupIdList
            Set<Integer> groupIdSet = Arrays.stream(configArray[2].split(","))
                    .map((group) -> {
                        int groupId = Integer.parseInt(group);
                        if (groupId <= 0) {
                            throw error(GROUPS_CONFIG_ERROR, configArray[2]);
                        }
                        return groupId;
                    }).collect(Collectors.toSet());
            configLine.groupIdSet = groupIdSet;
        } catch (Exception e) {
            throw error(GROUPS_CONFIG_ERROR, configArray[2], e);
        }
        return configLine;
    }


    private static NodeMgrException error(RetCode ret, String msg) {
        return new NodeMgrException(ret.msg(msg));
    }

    private static NodeMgrException error(RetCode ret, String msg, Throwable e) {
        return new NodeMgrException(ret.msg(msg), e);
    }

    public static void main(String[] args) {

        String[] ipLines = new String[]{
                "127.0.0.1:2 agencyA 1,2,3",
                "127.0.0.1:2 agencyB 2,-1",
                "127.0.0.1:2 agencyC 3"
        };
        for (String ipLine : ipLines) {
            System.out.println(parseLine(ipLine));
        }

    }
}


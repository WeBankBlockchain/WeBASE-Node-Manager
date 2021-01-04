/**
 * Copyright 2014-2020  the original author or authors.
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
package com.webank.webase.node.mgr.performance;

import static com.webank.webase.node.mgr.frontinterface.FrontRestTools.FRONT_PERFORMANCE_CONFIG;
import static com.webank.webase.node.mgr.frontinterface.FrontRestTools.FRONT_PERFORMANCE_RATIO;

import com.webank.webase.node.mgr.base.tools.JsonTools;
import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.base.properties.ConstantProperties;
import com.webank.webase.node.mgr.base.tools.NodeMgrTools;
import com.webank.webase.node.mgr.front.FrontService;
import com.webank.webase.node.mgr.front.entity.TbFront;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Log4j2
@Service
public class PerformanceService {

    @Autowired
    private FrontService frontService;
    @Autowired
    private RestTemplate genericRestTemplate;
    @Autowired
    private ConstantProperties cproperties;

    /**
     * get ratio of performance.
     */
    public Object getPerformanceRatio(Integer frontId, LocalDateTime beginDate,
        LocalDateTime endDate, LocalDateTime contrastBeginDate,
        LocalDateTime contrastEndDate, int gap) {
        log.debug(
            "start getPerformanceRatio.  frontId:{} beginDate:{} endDate:{}"
                + " contrastBeginDate:{} contrastEndDate:{} gap:{}",
            frontId, beginDate, endDate, contrastBeginDate, contrastEndDate, gap);

        List<String> nameList = Arrays
            .asList("beginDate", "endDate", "contrastBeginDate", "contrastEndDate", "gap");
        List<Object> valueList = Arrays
            .asList(beginDate, endDate, contrastBeginDate, contrastEndDate, gap);

        // request param to str
        String urlParam = NodeMgrTools.convertUrlParam(nameList, valueList);

        // query by front Id
        TbFront tbFront = frontService.getById(frontId);
        if (tbFront == null) {
            throw new NodeMgrException(ConstantCode.INVALID_FRONT_ID);
        }

        // request url
        String url = String.format(cproperties.getFrontUrl(), tbFront.getFrontIp(), tbFront.getFrontPort(),
                FRONT_PERFORMANCE_RATIO);
        url = url + "?" + urlParam;
        log.info("getPerformanceRatio request url:{}", url);

        Object rspObj = genericRestTemplate.getForObject(url, Object.class);
        log.debug("end getPerformanceRatio. rspObj:{}", JsonTools.toJSONString(rspObj));
        return rspObj;

    }

    /**
     * get config of performance.
     */
    public Object getPerformanceConfig(int frontId) {
        log.debug("start getPerformanceConfig.  frontId:{} ", frontId);

        // query by front Id
        TbFront tbFront = frontService.getById(frontId);
        if (tbFront == null) {
            throw new NodeMgrException(ConstantCode.INVALID_FRONT_ID);
        }

        // request url
        String url = String.format(cproperties.getFrontUrl(), tbFront.getFrontIp(), tbFront.getFrontPort(),
               FRONT_PERFORMANCE_CONFIG);
        log.info("getPerformanceConfig request url:{}", url);

        Object rspObj = genericRestTemplate.getForObject(url, Object.class);
        log.debug("end getPerformanceConfig. frontRsp:{}", JsonTools.toJSONString(rspObj));
        return rspObj;
    }

}

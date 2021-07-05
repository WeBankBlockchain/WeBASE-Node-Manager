/**
 * Copyright 2014-2021 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.webase.node.mgr.pro.alert.mail.server.config;

import com.webank.webase.node.mgr.pro.alert.mail.server.config.entity.ReqMailServerConfigParam;
import com.webank.webase.node.mgr.pro.alert.mail.server.config.entity.TbMailServerConfig;
import com.webank.webase.node.mgr.lite.base.code.ConstantCode;
import com.webank.webase.node.mgr.lite.base.enums.EnableStatus;
import com.webank.webase.node.mgr.lite.base.exception.NodeMgrException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Mail Server Configuration for Mail Alert
 * MailService using the latest MailServerConfig as configuration
 */
@Log4j2
@Service
public class MailServerConfigService {
    @Autowired
    MailServerConfigMapper configMapper;


    /**
     * save mail server configuration
     */
    public void saveMailServerConfig(ReqMailServerConfigParam inputParam) {
        log.debug("start saveMailServerConfig ReqMailServerConfigParam inputParam:{}", inputParam);
        checkAndInitMailServerConfig(inputParam);
        TbMailServerConfig tbMailServerConfig = new TbMailServerConfig();
        try{
            BeanUtils.copyProperties(inputParam, tbMailServerConfig);
            configMapper.add(tbMailServerConfig);
            log.debug("end saveMailServerConfig.tbMailServerConfig:{} ", tbMailServerConfig);
        }catch (Exception e) {
            log.error("getAllMailServerConfig error exception:{}", e);
            throw new NodeMgrException(ConstantCode.MAIL_SERVER_CONFIG_ERROR.getCode(),
                    e.getMessage());
        }
    }

    /**
     * get particular server configuration
     */
    public TbMailServerConfig queryByServerId(int serverId) {
        log.debug("start queryByServerId serverId:{}", serverId);
        TbMailServerConfig resMailServerConfig = configMapper.queryByServerId(serverId);
        log.debug("end resMailServerConfig:{} ", resMailServerConfig);
        return resMailServerConfig;
    }

    /**
     * get list of all mail server configuration
     */
    public List<TbMailServerConfig> getAllMailServerConfig() {
        log.debug("start getAllMailServerConfig ");
        List<TbMailServerConfig> resList = new ArrayList<>();
        try {
            resList = configMapper.listOfMailServerConfig();
            log.debug("end getAllMailServerConfig resList:{}", resList);
            return resList;
        }catch (Exception e) {
            log.error("getAllMailServerConfig error exception:[]", e);
            throw new NodeMgrException(ConstantCode.MAIL_SERVER_CONFIG_ERROR.getCode(),
                    e.getMessage());
        }
    }

    public TbMailServerConfig getLatestMailServerConfig() {
        log.debug("start getLatestMailServerConfig ");
        List<TbMailServerConfig> mailServerConfigList = configMapper.listOfMailServerConfig();
        if(mailServerConfigList.size() == 0) {
            throw new NodeMgrException(ConstantCode.MAIL_SERVER_CONFIG_ERROR_NO_DATA_IN_DB);
        }
        TbMailServerConfig latestMailServerConfig = mailServerConfigList.get(mailServerConfigList.size() -1 );
        log.debug("end getLatestMailServerConfig latestMailServerConfig:{}", latestMailServerConfig);
        return latestMailServerConfig;
    }

    public void updateMailServerConfig(ReqMailServerConfigParam inputParam) {
        log.debug("start updateMailServerConfig ReqMailServerConfigParam inputParam:{}", inputParam);
        TbMailServerConfig tbMailServerConfig = new TbMailServerConfig();
        try{
            BeanUtils.copyProperties(inputParam, tbMailServerConfig);
            configMapper.update(tbMailServerConfig);
            log.debug("end updateMailServerConfig. ");
        }catch (Exception e) {
            log.error("updateMailServerConfig error exception:[]", e);
            throw new NodeMgrException(ConstantCode.MAIL_SERVER_CONFIG_ERROR.getCode(),
                    e.getMessage());
        }
    }

    public void deleteByServerId(int serverId) {
        log.debug("start deleteByServerId serverId:{}", serverId);
        configMapper.deleteByServerId(serverId);
        log.debug("end deleteByServerId. ");

    }

    private void checkAndInitMailServerConfig(ReqMailServerConfigParam inputParam) {
        if(inputParam.getEnable() == null) {
            inputParam.setEnable(EnableStatus.OFF.getValue());
        }
        if(inputParam.getPort() == null) {
            inputParam.setPort(25);
        }
        if(StringUtils.isEmpty(inputParam.getDefaultEncoding())) {
            inputParam.setDefaultEncoding("UTF-8");
        }
        if(inputParam.getAuthentication() == null) {
            inputParam.setAuthentication(EnableStatus.ON.getValue());
        }
        if(inputParam.getStarttlsEnable() == null) {
            inputParam.setStarttlsEnable(EnableStatus.ON.getValue());
        }
        if(inputParam.getStarttlsRequired() == null) {
            inputParam.setStarttlsRequired(EnableStatus.OFF.getValue());
        }
        if(inputParam.getSocketFactoryFallback() == null) {
            inputParam.setEnable(EnableStatus.OFF.getValue());
        }
        if(inputParam.getSocketFactoryPort() == null) {
            inputParam.setSocketFactoryPort(465);
        }
        if(StringUtils.isEmpty(inputParam.getSocketFactoryClass())) {
            inputParam.setSocketFactoryClass("javax.net.ssl.SSLSocketFactory");
        }
        if(inputParam.getTimeout() == null) {
            inputParam.setTimeout(5000);
        }
        if(inputParam.getConnectionTimeout() == null) {
            inputParam.setConnectionTimeout(5000);
        }
        if(inputParam.getWriteTimeout() == null) {
            inputParam.setWriteTimeout(5000);
        }
    }
}

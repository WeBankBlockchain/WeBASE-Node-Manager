/**
 * Copyright 2014-2021 the original author or authors.
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

package com.webank.webase.node.mgr.appintegration;

import com.webank.webase.node.mgr.appintegration.entity.AppAddInfo;
import com.webank.webase.node.mgr.appintegration.entity.AppInfoParam;
import com.webank.webase.node.mgr.appintegration.entity.AppRegisterInfo;
import com.webank.webase.node.mgr.appintegration.entity.TbAppInfo;
import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.enums.AppType;
import com.webank.webase.node.mgr.base.enums.DataStatus;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.base.tools.NodeMgrTools;
import com.webank.webase.node.mgr.base.tools.ValidateUtil;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * AppIntegrationService.
 */
@Log4j2
@Service
public class AppIntegrationService {

    @Autowired
    private AppInfoMapper appInfoMapper;

    private static final int APP_KEY_NUMBER = 8;
    private static final int APP_SECRET_NUMBER = 32;

    /**
     * save app info.
     * 
     * @param save
     * @return
     */
    public TbAppInfo save(AppAddInfo appAddInfo) {
        // check link
        if (!ValidateUtil.validateUrl(appAddInfo.getAppDocLink())) {
            throw new NodeMgrException(ConstantCode.LINK_FORMAT_INVALID);
        }
        TbAppInfo tbAppInfo;
        if (appAddInfo.getId() == null) {
            tbAppInfo = newApp(appAddInfo);
        } else {
            tbAppInfo = updateApp(appAddInfo);
        }
        return tbAppInfo;
    }

    /**
     * newApp.
     * 
     * @param appAddInfo
     * @return
     */
    public TbAppInfo newApp(AppAddInfo appAddInfo) {
        // check name
        if (checkExistByAppName(appAddInfo.getAppName())) {
            throw new NodeMgrException(ConstantCode.APPNAME_EXISTS);
        }
        // get app key and secret
        String appKey = getAppKey();
        String appSecret = NodeMgrTools.randomString(APP_SECRET_NUMBER);

        // add app info
        TbAppInfo tbAppInfo = new TbAppInfo();
        BeanUtils.copyProperties(appAddInfo, tbAppInfo);
        tbAppInfo.setAppKey(appKey);
        tbAppInfo.setAppSecret(appSecret);
        Integer affectRow = appInfoMapper.addAppInfo(tbAppInfo);
        if (affectRow == 0) {
            log.warn("affect 0 rows of tb_app_info");
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }
        return queryAppInfoByAppKey(appKey);
    }

    /**
     * newApp.
     * 
     * @param appAddInfo
     * @return
     */
    public TbAppInfo updateApp(AppAddInfo appAddInfo) {
        // check id
        if (!checkExistById(appAddInfo.getId())) {
            throw new NodeMgrException(ConstantCode.ID_NOT_EXISTS);
        }
        // check name
        TbAppInfo tbRecord = queryAppInfoByAppName(appAddInfo.getAppName());
        if (tbRecord != null && tbRecord.getId().intValue() != appAddInfo.getId().intValue()) {
            throw new NodeMgrException(ConstantCode.APPNAME_EXISTS);
        }
        // copy app info
        TbAppInfo tbAppInfo = new TbAppInfo();
        BeanUtils.copyProperties(appAddInfo, tbAppInfo);
        updateAppInfo(tbAppInfo);
        return queryAppInfoById(appAddInfo.getId());
    }

    /**
     * appRegister.
     * 
     * @param appKey
     * @param appRegisterInfo
     */
    public void appRegister(String appKey, AppRegisterInfo appRegisterInfo) {
        log.debug("appRegister appKey:{}", appKey);
        String appIp = appRegisterInfo.getAppIp();
        Integer appPort = appRegisterInfo.getAppPort();
        // check ip and port
        NodeMgrTools.checkServerConnect(appIp, appPort);
        // check link
        if (!ValidateUtil.validateUrl(appRegisterInfo.getAppLink())) {
            throw new NodeMgrException(ConstantCode.LINK_FORMAT_INVALID);
        }
        // update
        TbAppInfo tbAppInfo = queryAppInfoByAppKey(appKey);
        tbAppInfo.setAppStatus(DataStatus.NORMAL.getValue());
        BeanUtils.copyProperties(appRegisterInfo, tbAppInfo);
        updateAppInfo(tbAppInfo);
    }

    /**
     * appStatusCheck.
     */
    @Async(value = "mgrAsyncExecutor")
    public void appStatusCheck() {
        AppInfoParam param = new AppInfoParam();
        param.setAppType(AppType.NEW.getValue());
        List<TbAppInfo> listOfAppInfo = listOfAppInfo(param);
        for (TbAppInfo tbAppInfo : listOfAppInfo) {
            Integer appStatus = DataStatus.NORMAL.getValue();
            try {
                NodeMgrTools.checkServerConnect(tbAppInfo.getAppIp(), tbAppInfo.getAppPort());
            } catch (Exception e) {
                log.debug("appKey:{} status is invalid", tbAppInfo.getAppKey());
                appStatus = DataStatus.INVALID.getValue();
            }
            updateAppInfo(new TbAppInfo(tbAppInfo.getId(), appStatus));
        }
    }

    /**
     * get count.
     * 
     * @param appInfoParam
     * @return
     */
    public int countOfAppInfo(AppInfoParam appInfoParam) {
        return appInfoMapper.countOfAppInfo(appInfoParam);
    }

    /**
     * get List.
     * 
     * @param appInfoParam
     * @return
     */
    public List<TbAppInfo> listOfAppInfo(AppInfoParam appInfoParam) {
        return appInfoMapper.listOfAppInfo(appInfoParam);
    }

    /**
     * updateAppInfo.
     * 
     * @param tbAppInfo
     * @return
     */
    public int updateAppInfo(TbAppInfo tbAppInfo) {
        return appInfoMapper.updateAppInfo(tbAppInfo);
    }
    
    /**
     * deleteAppInfo.
     * 
     * @param tbAppInfo
     * @return
     */
    public void deleteApp(Integer id) {
        // check id
        if (!checkExistById(id)) {
            throw new NodeMgrException(ConstantCode.ID_NOT_EXISTS);
        }
        appInfoMapper.deleteAppInfo(id);
    }

    /**
     * check by id.
     * 
     * @param id
     * @return
     */
    public boolean checkExistById(Integer id) {
        TbAppInfo tbAppInfo = queryAppInfoById(id);
        if (tbAppInfo == null) {
            return false;
        }
        return true;
    }

    /**
     * check by appName.
     * 
     * @param id
     * @return
     */
    public boolean checkExistByAppName(String appName) {
        TbAppInfo tbAppInfo = queryAppInfoByAppName(appName);
        if (tbAppInfo == null) {
            return false;
        }
        return true;
    }

    /**
     * queryAppInfo by id.
     * 
     * @param appInfoParam
     * @return
     */
    public TbAppInfo queryAppInfoById(Integer id) {
        AppInfoParam appInfoParam = new AppInfoParam();
        appInfoParam.setId(id);
        return queryAppInfo(appInfoParam);
    }

    /**
     * queryAppInfo by appName.
     * 
     * @param appInfoParam
     * @return
     */
    public TbAppInfo queryAppInfoByAppName(String appName) {
        AppInfoParam appInfoParam = new AppInfoParam();
        appInfoParam.setAppName(appName);
        return queryAppInfo(appInfoParam);
    }

    /**
     * queryAppInfo by appKey.
     * 
     * @param appInfoParam
     * @return
     */
    public TbAppInfo queryAppInfoByAppKey(String appKey) {
        AppInfoParam appInfoParam = new AppInfoParam();
        appInfoParam.setAppKey(appKey);
        return queryAppInfo(appInfoParam);
    }

    /**
     * queryAppInfo.
     * 
     * @param appInfoParam
     * @return
     */
    private TbAppInfo queryAppInfo(AppInfoParam appInfoParam) {
        return appInfoMapper.queryAppInfo(appInfoParam);
    }

    /**
     * getAppKey.
     * 
     * @return
     */
    private String getAppKey() {
        String appKey = NodeMgrTools.randomString(APP_KEY_NUMBER);
        TbAppInfo tbAppInfo = queryAppInfoByAppKey(appKey);
        if (tbAppInfo != null) {
            getAppKey();
        }
        return appKey;
    }
}

package com.webank.webase.node.mgr.tools;

import com.qctc.system.api.RemoteUserService;
import com.qctc.system.api.model.LoginUser;
import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

/**
 * @author zhangyang
 * @version 1.0
 * @project WeBASE-Node-Manager
 * @description 框架中的统一系统用户相关工具类
 * @date 2023/9/18 11:29:32
 */
@Service
public class SysAccountService {
    @DubboReference
    private RemoteUserService remoteUserService;

    /**
     * @description 某个系统用户是否存在
     * @param account
     * @return void
     * @author 2023/9/18
     * @date zhangyang 11:30:33
     */
    public void accountExist(String account) throws NodeMgrException {
        if (StringUtils.isBlank(account)) {
            throw new NodeMgrException(ConstantCode.ACCOUNT_NAME_EMPTY);
        }

        try {
            LoginUser user = remoteUserService.getUserInfo(account);
            if (null == user) {
                throw new NodeMgrException(ConstantCode.ACCOUNT_NOT_EXISTS);
            }
        } catch (Exception e) {
            throw new NodeMgrException(ConstantCode.ACCOUNT_NOT_EXISTS);
        }
    }

    /**
     * @description 获取某个系统用户的信息
     * @param account
     * @return void
     * @author 2023/9/18
     * @date zhangyang 13:57:35
     */
    public LoginUser getAccoutInfo(String account) throws NodeMgrException {
        if (StringUtils.isBlank(account)) {
            throw new NodeMgrException(ConstantCode.ACCOUNT_NAME_EMPTY);
        }

        try {
            LoginUser user = remoteUserService.getUserInfo(account);
            if (null == user) {
                throw new NodeMgrException(ConstantCode.ACCOUNT_NOT_EXISTS);
            }

            return user;
        } catch (Exception e) {
            throw new NodeMgrException(ConstantCode.ACCOUNT_NOT_EXISTS);
        }
    }
}

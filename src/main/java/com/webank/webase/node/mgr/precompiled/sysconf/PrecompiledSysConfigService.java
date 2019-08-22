package com.webank.webase.node.mgr.precompiled.sysconf;

import com.alibaba.fastjson.JSON;
import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.base.tools.HttpRequestTools;
import com.webank.webase.node.mgr.frontinterface.FrontInterfaceService;
import com.webank.webase.node.mgr.frontinterface.FrontRestTools;
import com.webank.webase.node.mgr.precompiled.permission.PermissionParam;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Log4j2
@Service
public class PrecompiledSysConfigService {
    @Autowired
    private FrontRestTools frontRestTools;
    @Autowired
    private FrontInterfaceService frontInterfaceService;

    /**
     * get system config list
     */
    public Object getSysConfigListService(int groupId, int pageSize, int pageNumber) {
        log.debug("start getSysConfigListService. param:{}" + groupId);
        Map<String, String> map = new HashMap<>();
        map.put("groupId", String.valueOf(groupId));
        map.put("pageSize", String.valueOf(pageSize));
        map.put("pageNumber", String.valueOf(pageNumber));

        String uri = HttpRequestTools.getQueryUri(FrontRestTools.URI_SYS_CONFIG_LIST, map);

        Object frontRsp = frontRestTools.getForEntity(groupId, uri, Object.class);
        log.debug("end getSysConfigListService. frontRsp:{}", JSON.toJSONString(frontRsp));
        return frontRsp;
    }


    /**
     * post set system config
     */

    public Object setSysConfigByKeyService(SysConfigParam sysConfigParam) {
        log.debug("start setSysConfigByKeyService. param:{}", JSON.toJSONString(sysConfigParam));
        if (Objects.isNull(sysConfigParam)) {
            log.info("fail setSysConfigByKeyService. request param is null");
            throw new NodeMgrException(ConstantCode.INVALID_PARAM_INFO);
        }

        Object frontRsp = frontRestTools.postForEntity(
                sysConfigParam.getGroupId(), FrontRestTools.URI_SYS_CONFIG,
                sysConfigParam, Object.class);
        log.debug("end setSysConfigByKeyService. frontRsp:{}", JSON.toJSONString(frontRsp));
        return frontRsp;
    }

}

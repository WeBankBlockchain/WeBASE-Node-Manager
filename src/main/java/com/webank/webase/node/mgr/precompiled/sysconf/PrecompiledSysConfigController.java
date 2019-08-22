package com.webank.webase.node.mgr.precompiled.sysconf;

import com.alibaba.fastjson.JSON;
import com.webank.webase.node.mgr.base.controller.BaseController;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.base.properties.ConstantProperties;
import com.webank.webase.node.mgr.precompiled.permission.PermissionParam;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.Duration;
import java.time.Instant;

@Log4j2
@RestController
@RequestMapping("sys/config")
public class PrecompiledSysConfigController extends BaseController {

    @Autowired
    PrecompiledSysConfigService precompiledSysConfigService;
    /**
     * get system config list
     * 透传front的BaseResponse
     */
    @GetMapping("list")
    public Object getSysConfigList(
            @RequestParam(defaultValue = "1") int groupId,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "1") int pageNumber) throws Exception, NodeMgrException {

        Instant startTime = Instant.now();
        log.info("start getSysConfigList startTime:{}", startTime.toEpochMilli());

        Object result = precompiledSysConfigService.getSysConfigListService(groupId, pageSize, pageNumber);

        log.info("end getSysConfigList useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(), JSON.toJSONString(result));
        return result;
    }

    /**
     * set system config by key.
     */
    @PostMapping(value = "")
    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN)
    public Object setSysConfigByKeyService(@RequestBody @Valid SysConfigParam sysConfigParam,
                                  BindingResult result) throws NodeMgrException {
        checkBindResult(result);
        Instant startTime = Instant.now();
        log.info("start setSysConfigByKeyService startTime:{} sysConfigParam:{}", startTime.toEpochMilli(),
                JSON.toJSONString(sysConfigParam));

        Object res = precompiledSysConfigService.setSysConfigByKeyService(sysConfigParam);

        log.info("end setSysConfigByKeyService useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(), JSON.toJSONString(res));

        return res;
    }
}

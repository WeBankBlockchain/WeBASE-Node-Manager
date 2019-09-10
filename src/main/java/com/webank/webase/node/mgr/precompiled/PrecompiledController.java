package com.webank.webase.node.mgr.precompiled;

import com.alibaba.fastjson.JSON;
import com.webank.webase.node.mgr.base.controller.BaseController;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.base.properties.ConstantProperties;
import com.webank.webase.node.mgr.precompiled.entity.ConsensusHandle;
import com.webank.webase.node.mgr.precompiled.entity.CrudHandle;
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
@RequestMapping("precompiled")
public class PrecompiledController extends BaseController {
    @Autowired
    PrecompiledService precompiledService;

    /**
     * get cns list
     * 透传front的BaseResponse
     */
    @GetMapping("cns/list")
    public Object listCns(
            @RequestParam(defaultValue = "1") int groupId,
            @RequestParam String contractNameAndVersion,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "1") int pageNumber) throws Exception, NodeMgrException {

        Instant startTime = Instant.now();
        log.info("start listCns startTime:{}", startTime.toEpochMilli());
        Object result = precompiledService.listCnsService(groupId, contractNameAndVersion, pageSize, pageNumber);

        log.info("end listCns useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(), JSON.toJSONString(result));
        return result;
    }

    /**
     * get node list with consensus status.
     */
    @GetMapping("consensus/list")
    public Object getNodeList(
            @RequestParam(defaultValue = "1") int groupId,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "1") int pageNumber) throws Exception, NodeMgrException {

        Instant startTime = Instant.now();
        log.info("start getNodeList startTime:{}", startTime.toEpochMilli());

        Object result = precompiledService.getNodeListService(groupId, pageSize, pageNumber);

        log.info("end getNodeList useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(), JSON.toJSONString(result));
        return result;
    }

    @PostMapping(value = "consensus")
    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN)
    public Object nodeManage(@RequestBody @Valid ConsensusHandle consensusHandle,
                                  BindingResult result) throws NodeMgrException {
        checkBindResult(result);
        Instant startTime = Instant.now();
        log.info("start nodeManage startTime:{} consensusHandle:{}", startTime.toEpochMilli(),
                JSON.toJSONString(consensusHandle));

        Object res = precompiledService.nodeManageService(consensusHandle);

        log.info("end nodeManage useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(), JSON.toJSONString(res));

        return res;
    }

    /**
     * crud control.
     */
    @PostMapping(value = "crud")
    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN)
    public Object crud(@RequestBody @Valid CrudHandle crudHandle,
                                   BindingResult result) throws NodeMgrException {
        checkBindResult(result);
        Instant startTime = Instant.now();
        log.info("start crud startTime:{} crudHandle:{}", startTime.toEpochMilli(),
                JSON.toJSONString(crudHandle));

        Object res = precompiledService.crudService(crudHandle);

        log.info("end crud useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(), JSON.toJSONString(res));

        return res;
    }
}

package com.webank.webase.node.mgr.cert;

import com.alibaba.fastjson.JSON;
import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.controller.BaseController;
import com.webank.webase.node.mgr.base.entity.BasePageResponse;
import com.webank.webase.node.mgr.base.entity.BaseResponse;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.cert.entity.CertHandle;
import com.webank.webase.node.mgr.cert.entity.CertParam;
import com.webank.webase.node.mgr.cert.entity.DeleteCertHandle;
import com.webank.webase.node.mgr.cert.entity.TbCert;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Log4j2
@RestController
@RequestMapping("cert")
public class CertController extends BaseController {
    @Autowired
    CertService certService;

    @GetMapping("")
    public Object getCertList() throws NodeMgrException {
        List<TbCert> list = new ArrayList<>();
        try{
            list = certService.getCertsList();
        }catch (Exception e) {
            return new BaseResponse(ConstantCode.CERT_ERROR, e.getMessage());
        }
        return new BasePageResponse(ConstantCode.SUCCESS, list, list.size());
    }

    /**
     * 可能传入一个Crt包含多个crt的内容，所以调用的是saveCerts
     * @param certHandle
     * @param result
     * @return
     * @throws NodeMgrException
     */
    @PostMapping("")
//    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN)
    public Object addCert(@RequestBody @Valid CertHandle certHandle,
                                  BindingResult result) throws NodeMgrException {
        checkBindResult(result);
        String content = certHandle.getContent();
        if(content == null | content == "") {
            return new BaseResponse(ConstantCode.CERT_ERROR);
        }
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start addCert. startTime:{} certHandle:{}",
                startTime.toEpochMilli(), JSON.toJSONString(certHandle));
        try {
            certService.saveCerts(content);
        }catch (Exception e) {
            return new BaseResponse(ConstantCode.CERT_ERROR, e.getMessage());
        }

        log.info("end addCert. useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(), JSON.toJSONString(baseResponse));
        return baseResponse;
    }

    @DeleteMapping(value = "")
//    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN)
    public Object removeCert(@RequestBody @Valid CertHandle certHandle,
                          BindingResult result) throws NodeMgrException {
        checkBindResult(result);
        String fingerPrint = certHandle.getFingerPrint();
        if(fingerPrint == null || fingerPrint == ""){
            return new BaseResponse(ConstantCode.CERT_ERROR, "fingerPrint cannot be null");
        }
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start removeCert. startTime:{} cert's fingerprint:{}",
                startTime.toEpochMilli(), JSON.toJSONString(fingerPrint));

        try {
            certService.removeCertByFingerPrint(fingerPrint);
        }catch (Exception e) {
            return new BaseResponse(ConstantCode.CERT_ERROR, e.getMessage());
        }

        log.info("end removeCert. useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(), JSON.toJSONString(baseResponse));
        return baseResponse;
    }


}

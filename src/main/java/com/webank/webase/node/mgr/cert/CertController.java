/**
 * Copyright 2014-2021 the original author or authors.
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
package com.webank.webase.node.mgr.cert;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.qctc.common.log.annotation.Log;
import com.qctc.common.log.enums.BusinessType;
import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.controller.BaseController;
import com.webank.webase.node.mgr.base.entity.BasePageResponse;
import com.webank.webase.node.mgr.base.entity.BaseResponse;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.config.properties.ConstantProperties;
import com.webank.webase.node.mgr.tools.HttpRequestTools;
import com.webank.webase.node.mgr.tools.JsonTools;
import com.webank.webase.node.mgr.cert.entity.CertHandle;
import com.webank.webase.node.mgr.cert.entity.FileContentHandle;
import com.webank.webase.node.mgr.cert.entity.TbCert;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;

import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name="证书管理")
@Log4j2
@RestController
@RequestMapping("cert")
public class CertController extends BaseController {
    @Autowired
    CertService certService;

    @SaCheckPermission("bcos3:sys:certificate")
    @GetMapping("list")
    public Object getCertList() throws NodeMgrException {
        Instant startTime = Instant.now();
        log.info("start getCertList startTime:{}", startTime.toEpochMilli());
        List<TbCert> list = new ArrayList<>();
        try{
            list = certService.getAllCertsListAfterPull();
        }catch (Exception e) {
            log.error("getCertList exception StackTrace:{}", e);
            return new BaseResponse(ConstantCode.CERT_ERROR, e.getMessage());
        }
        log.info("end getCertList useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(), list);
        return new BasePageResponse(ConstantCode.SUCCESS, list, list.size());
    }

    @SaCheckPermission("bcos3:sys:certificate")
    @GetMapping("sdk/{frontId}")
    public Object getSdkCertList(@PathVariable("frontId") Integer frontId) throws NodeMgrException {
        Instant startTime = Instant.now();
        log.info("start getSdkCertList startTime:{},frontId:{}", startTime.toEpochMilli(), frontId);
        Map<String, String> list = certService.getFrontSdkContent(frontId);
        log.info("end getSdkCertList useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(), list);
        return new BasePageResponse(ConstantCode.SUCCESS, list, list.size());
    }

    @SaCheckPermission("bcos3:sys:downloadSdkCert")
    @GetMapping("sdk/zip/{frontId}")
    public ResponseEntity<InputStreamResource> getSdkCertZip(@PathVariable("frontId") Integer frontId)
        throws NodeMgrException {
        Instant startTime = Instant.now();
        log.info("start getSdkCertZip startTime:{},frontId:{}", startTime.toEpochMilli(), frontId);
        // get file
        FileContentHandle fileContentHandle = certService.getFrontSdkZipFile(frontId);
        log.info("end getSdkCertZip fileContentHandle:{}useTime:{}", fileContentHandle,
            Duration.between(startTime, Instant.now()).toMillis());
        return ResponseEntity.ok().headers(HttpRequestTools.headers(fileContentHandle.getFileName()))
            .body(new InputStreamResource(fileContentHandle.getInputStream()));
    }

    @SaCheckPermission("bcos3:sys:certificate")
    @GetMapping("")
    public Object getCertByFingerPrint(@RequestParam(required = true)String fingerPrint) throws NodeMgrException {
        Instant startTime = Instant.now();
        log.info("start getCertByFingerPrint startTime:{}", startTime.toEpochMilli());
        TbCert tbCert = new TbCert();
        try{
            tbCert = certService.getCertByFingerPrint(fingerPrint);
        }catch (Exception e) {
            log.error("getCertByFingerPrint exception StackTrace:{}", e);
            return new BaseResponse(ConstantCode.CERT_ERROR, e.getMessage());
        }
        log.info("end getCertByFingerPrint useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(), JsonTools.toJSONString(tbCert));
        return new BaseResponse(ConstantCode.SUCCESS, tbCert);
    }

    /**
     * 可能传入一个Crt包含多个crt的内容，所以调用的是saveCerts
     * @param certHandle
     * @param result
     * @return
     * @throws NodeMgrException
     */
    @Log(title = "BCOS3/系统管理/证书管理", businessType = BusinessType.INSERT)
    @SaCheckPermission("bcos3:sys:addCert")
    @PostMapping("")
    public Object addCert(@RequestBody @Valid CertHandle certHandle,
                                  BindingResult result) throws NodeMgrException {
        Instant startTime = Instant.now();
        log.info("start addCert. startTime:{} certHandle:{}",
                startTime.toEpochMilli(), JsonTools.toJSONString(certHandle));
        checkBindResult(result);
        int count = 0;
        String content = certHandle.getContent();
        if(StringUtils.isBlank(content)) {
            return new BaseResponse(ConstantCode.CERT_ERROR, "content cannot be empty");
        }
        try {
            count = certService.saveCerts(content);
        }catch (Exception e) {
            log.error("addCert exception StackTrace:{}", e);
            return new BaseResponse(ConstantCode.CERT_ERROR, e.getMessage());
        }

        log.info("end addCert. useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(), "add certs count is "+ count);
        return new BaseResponse(ConstantCode.SUCCESS, count);
    }

    @Log(title = "BCOS3/系统管理/证书管理", businessType = BusinessType.DELETE)
    @SaCheckPermission("bcos3:sys:deleteCert")
    @DeleteMapping(value = "")
    public Object removeCert(@RequestBody @Valid CertHandle certHandle,
                          BindingResult result) throws NodeMgrException {
        Instant startTime = Instant.now();
        log.info("start removeCert. startTime:{} certHandle:{}",
                startTime.toEpochMilli(), JsonTools.toJSONString(certHandle));
        checkBindResult(result);
        int count = 0;
        String fingerPrint = certHandle.getFingerPrint();
        if(StringUtils.isBlank(fingerPrint)) {
            return new BaseResponse(ConstantCode.CERT_ERROR, "fingerPrint cannot be null");
        }
        try {
            count = certService.removeCertByFingerPrint(fingerPrint);
        }catch (Exception e) {
            log.error("removeCert exception StackTrace:{}", e);
            return new BaseResponse(ConstantCode.CERT_ERROR, e.getMessage());
        }

        log.info("end removeCert. useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(), "remove influence "+count+"cert's father cert");
        return new BaseResponse(ConstantCode.SUCCESS, count);
    }


}

/**
 * Copyright 2014-2020 the original author or authors.
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

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.webank.webase.node.mgr.cert.entity.CertParam;
import com.webank.webase.node.mgr.cert.entity.TbCert;

@Repository
public interface CertMapper {

    void add(TbCert tbCert);

    void deleteByFingerPrint(@Param("fingerPrint")String fingerPrint);

    TbCert queryCertByFingerPrint(@Param("fingerPrint")String fingerPrint);

    List<TbCert> listOfCert();

    List<TbCert> listOfCertByConditions(CertParam param);

    void update(TbCert tbCert);

    @Delete("delete from tb_cert")
    int deleteAll();
}

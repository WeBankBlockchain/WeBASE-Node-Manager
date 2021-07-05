/**
 * Copyright 2014-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.webank.webase.node.mgr.pro.appintegration;

import com.webank.webase.node.mgr.pro.appintegration.entity.AppInfoParam;
import com.webank.webase.node.mgr.pro.appintegration.entity.TbAppInfo;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * application integration mapper.
 */
@Repository
public interface AppInfoMapper {

    /**
     * Add new application data.
     */
    Integer addAppInfo(TbAppInfo tbAppInfo);

    /**
     * Query the number of application according to some conditions.
     */
    Integer countOfAppInfo(AppInfoParam appInfoParam);

    /**
     * Query application list according to some conditions.
     */
    List<TbAppInfo> listOfAppInfo(AppInfoParam appInfoParam);

    /**
     * Query application info according to some conditions.
     */
    TbAppInfo queryAppInfoAdded(AppInfoParam appInfoParam);

    /**
     * update application row.
     */
    Integer updateAppInfo(TbAppInfo tbAppInfo);

    /**
     * delete application.
     */
    void deleteAppInfo(@Param("id") Integer id);

}

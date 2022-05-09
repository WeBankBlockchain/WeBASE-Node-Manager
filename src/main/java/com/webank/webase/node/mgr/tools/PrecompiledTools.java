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

package com.webank.webase.node.mgr.tools;

import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.code.RetCode;
import com.webank.webase.node.mgr.base.entity.BaseResponse;
import com.webank.webase.node.mgr.precntauth.authmanager.base.PrecompiledResult;

/**
 * check precompiled api return code
 */
public class PrecompiledTools {

    public static BaseResponse processResponse(String res) {
        if (res.contains("code")) {
            PrecompiledResult precompiledResult =
                JsonTools.toJavaObject(res, PrecompiledResult.class);
            return new BaseResponse(new RetCode(precompiledResult.getCode(), precompiledResult.getMsg()));
        } else {
            BaseResponse response = new BaseResponse(ConstantCode.SUCCESS);
            response.setData(res);
            return response;
        }
    }
}

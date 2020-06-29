/**
 * Copyright 2014-2020 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webank.webase.node.mgr.base.tools;

import org.apache.commons.lang3.StringUtils;

import lombok.extern.log4j.Log4j2;

/**
 *
 */

@Log4j2
public class ValidateUtil {
    public static final String IP_PATTERN = "^((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$";

    public static final String AGENCY_NAME_PATTERN = "^[0-9a-zA-Z_]+$";

    /**
     * Validate ipv4 address.
     * @param ip
     * @return  return false if ip is not a valid IP format.
     */
    public static boolean ipv4Valid(final String ip) {
        if (StringUtils.isBlank(ip)){
            return false;
        }
        return ip.matches(IP_PATTERN);
    }

    public static boolean validateAgencyName(final String agencyName) {
        if (StringUtils.isBlank(agencyName)){
            return false;
        }
        return agencyName.matches(AGENCY_NAME_PATTERN);
    }
}
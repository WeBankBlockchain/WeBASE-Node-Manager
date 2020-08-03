/**
 * Copyright 2014-2020  the original author or authors.
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
 
package com.webank.webase.node.mgr.base.tools;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtil {
    public static final DateTimeFormatter _YYYY_MM_DD_HH_MM_SS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter YYYYMMDD_HHMMSS = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    public static final DateTimeFormatter _YYYY_MM_DD = DateTimeFormatter.ofPattern("yyyy-MM-dd");


    /**
     *  Format now to yyyy-MM-dd HH:mm:ss.
     * @return
     */
    public static String defaultFormatNow() {
        return LocalDateTime.now().format(DateUtil._YYYY_MM_DD_HH_MM_SS);
    }

    /**
     *  Format now with custom format.
     *
     * @param formatter
     * @return
     */
    public static String formatNow(DateTimeFormatter formatter) {
        return LocalDateTime.now().format(formatter);
    }

}


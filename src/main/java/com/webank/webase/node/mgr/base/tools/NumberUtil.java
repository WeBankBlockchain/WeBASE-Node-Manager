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
public class NumberUtil {

    public static final int PERCENTAGE_FAILED = -1;
    public static final int PERCENTAGE_FINISH = 100;
    public static final int PERCENTAGE_IN_PROGRESS = 0;

    /**
     *
     * @param obtained
     * @param total
     * @return
     */
    public static int percentage(int obtained, int total) {
        if (total == 0){
            return 0;
        }
        return (int)((float)obtained) * 100 / total;
    }
   
    
}


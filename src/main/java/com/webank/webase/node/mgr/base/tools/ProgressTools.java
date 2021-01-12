/**
 * Copyright 2014-2020 the original author or authors.
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

package com.webank.webase.node.mgr.base.tools;

import lombok.extern.log4j.Log4j2;

/**
 * visual deploy progress get
 */
@Log4j2
public class ProgressTools {

    /**
     * 部署的总步骤流程
     * 0-等待开始；1-检测机器内存与依赖，2-检测Docker服务，3-检测端口占用，4-初始化安装主机依赖，5-初始化加载Docker镜像中
     * 6-生成链证书与配置，7-初始化链与前置数据，8-传输链配置到主机
     * 9-配置完成，启动中
     * 添加节点时，流程同上
     */
    private static int DEPLOY_PROGRESS = 0;

    /**
     *
     */
    public static int progress() {
        log.info("Progress check ...");
        return DEPLOY_PROGRESS;
    }


    public static void setDefault() {
        DEPLOY_PROGRESS = 0;
    }
    public static void setHostCheck() {
        DEPLOY_PROGRESS = 1;
    }
    public static void setDockerCheck() {
        DEPLOY_PROGRESS = 2;
    }
    public static void setPortCheck() {
        DEPLOY_PROGRESS = 3;
    }
    public static void setHostInit() {
        DEPLOY_PROGRESS = 4;
    }
    public static void setPullDocker() {
        DEPLOY_PROGRESS = 5;
    }
    public static void setGenConfig() {
        DEPLOY_PROGRESS = 6;
    }
    public static void setInitChainData() {
        DEPLOY_PROGRESS = 7;
    }
    public static void setScpConfig() {
        DEPLOY_PROGRESS = 8;
    }
    public static void setStarting() {
        DEPLOY_PROGRESS = 9;
    }

}

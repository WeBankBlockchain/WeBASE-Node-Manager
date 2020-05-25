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
package com.webank.webase.node.mgr.deploy.service;

import static com.webank.webase.node.mgr.base.properties.ConstantProperties.SSH_DEFAULT_PORT;
import static com.webank.webase.node.mgr.base.properties.ConstantProperties.SSH_DEFAULT_USER;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.fisco.bcos.web3j.crypto.EncryptType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.base.properties.ConstantProperties;
import com.webank.webase.node.mgr.base.tools.ValidateUtil;
import com.webank.webase.node.mgr.base.tools.cmd.ExecuteResult;
import com.webank.webase.node.mgr.base.tools.cmd.JavaCommandExecutor;

import lombok.extern.slf4j.Slf4j;

/**
 * Java call shell script and system command.
 */
@Slf4j
@Component
public class ShellService {

    @Autowired private JavaCommandExecutor javaCommandExecutor;
    @Autowired private PathService pathService;
    @Autowired private ConstantProperties constant;


    /**
     * @param ip   Required.
     * @param port Default 22.
     * @param user Default root.
     * @param pwd  Not required.
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    @Async("mgrAsyncExecutor")
    public void execHostOperate(String ip, int port, String user, String pwd) {
        log.info("Exec execHostOperate method for [{}@{}:{}#{}]", user, ip, port, pwd);
        if (! ValidateUtil.validateIpv4(ip)) {
            log.error("Exec execHostOperate method ERROR: IP:[{}] error", ip);
            return;
        }

        port = port <= 0 || port > 65535 ? SSH_DEFAULT_PORT : port;
        user = StringUtils.isBlank(user) ? SSH_DEFAULT_USER : user;

        String command = String.format("bash -x -e %s -H %s -P %s -u %s", constant.getNodeOperateShell(),
                ip, port, user, StringUtils.isBlank(pwd) ? "" : String.format(" -p %s", pwd));

        ExecuteResult executeResult = javaCommandExecutor.executeCommand(command, constant.getHostInitTimeout());
        // TODO. next
    }

    /**
     *  TODO. if nodes config dir already exists, delete or backup first ?
     *
     * @param encryptType
     * @param ipLines
     * @return
     */
    public ExecuteResult execBuildChain(byte encryptType,
                                  String[] ipLines,
                                  String chainName) {
        Path ipConf = Paths.get(pathService.getIpConfigPath(chainName));
        log.info("Exec execBuildChain method for [{}], chainName:[{}], ipConfig:[{}]",
                JSON.toJSONString(ipLines),
                chainName,ipConf.toAbsolutePath().toString());

        try {
            if( ! Files.exists(ipConf.getParent())){
                Files.createDirectories(ipConf.getParent());
            }
            Files.write(ipConf, Arrays.asList(ipLines), StandardOpenOption.CREATE);
        } catch (IOException e) {
            throw new NodeMgrException(ConstantCode.SAVE_IP_CONFIG_FILE_ERROR);
        }

        if (Files.notExists(ipConf)) {
            // file not exists
            log.error("File: [{}] not exists in directory:[{}] ", ipConf, Paths.get(".").toAbsolutePath().toString());
            throw new NodeMgrException(ConstantCode.NO_CONFIG_FILE_ERROR);
        }

        String command = String.format("bash -e %s -f %s -o %s %s %s",
                constant.getBuildChainShell(),
                ipConf.toAbsolutePath().toString(),
                pathService.getChainRoot(chainName),
                encryptType == EncryptType.SM2_TYPE ? "-g" : "",
                SystemUtils.IS_OS_LINUX ? " -d " : ""); // build_chain.sh only support docker on linux

        return javaCommandExecutor.executeCommand(command, constant.getBuildChainTimeout());
    }

    /**
     * Add execute privilege for file.
     *
     * @param file
     * @throws FileNotFoundException
     */
    public static void addExecPrivilege(String file) {
        Path shellPath = Paths.get(file);
        if (Files.notExists(shellPath)) {
            // file not exists
            log.error("File: [{}] not exists in directory:[{}] ", file, Paths.get(".").toAbsolutePath().toString());
            return;
        }
        shellPath.toFile().setExecutable(true);
    }
}
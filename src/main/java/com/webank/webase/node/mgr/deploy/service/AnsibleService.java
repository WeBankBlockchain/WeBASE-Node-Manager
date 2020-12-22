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

package com.webank.webase.node.mgr.deploy.service;

import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.enums.ScpTypeEnum;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.base.properties.ConstantProperties;
import com.webank.webase.node.mgr.base.tools.JsonTools;
import com.webank.webase.node.mgr.base.tools.cmd.ExecuteResult;
import com.webank.webase.node.mgr.base.tools.cmd.JavaCommandExecutor;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.fisco.bcos.web3j.crypto.EncryptType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Log4j2
public class AnsibleService {

    @Autowired
    private ConstantProperties constant;
    @Autowired
    private PathService pathService;

    /**
     * check ansible installed
     */
    public void checkAnsible() {
        log.info("checkAnsible installed");
        String command = "ansible --version";
        ExecuteResult result = JavaCommandExecutor.executeCommand(command, constant.getExecShellTimeout());
        if (result.failed()) {
            throw new NodeMgrException(ConstantCode.ANSIBLE_NOT_INSTALLED.attach(result.getExecuteOut()));
        }
    }

    /**
     * check ansible ping
     */
    public void execPing(String ip) {
        // ansible webase(ip) -m ping
        String command = String.format("ansible %s -m ping", ip);
        ExecuteResult result = JavaCommandExecutor.executeCommand(command, constant.getExecShellTimeout());
        if (result.failed()) {
            throw new NodeMgrException(ConstantCode.ANSIBLE_PING_NOT_REACH.attach(result.getExecuteOut()));
        }
    }


    /**
     * copy, fetch, file(dir
     * scp: copy from local to remote, fetch from remote to local
     */
    public ExecuteResult scp(ScpTypeEnum typeEnum, String ip, String src, String dst) {
        log.info("scp typeEnum:{},ip:{},src:{},dst:{}", typeEnum, ip, src, dst);
        boolean isSrcDirectory = Files.isDirectory(Paths.get(src));
        boolean isSrcFile = Files.isRegularFile(Paths.get(src));
        String command;
        if (typeEnum == ScpTypeEnum.UP) {
            // handle file's dir local or remote
            if (isSrcFile) {
                // if src is file, create parent directory of dst on remote
                String parentOnRemote = Paths.get(dst).getParent().toAbsolutePath().toString();
                this.execCreateDir(ip, parentOnRemote);
            }
            if (isSrcDirectory) {
                // if src is directory, create dst on remote
                this.execCreateDir(ip, dst);
            }
            command = String.format("ansible %s -m copy -a \"src=%s dest=%s\"", ip, src, dst);

        } else { // DOWNLOAD
            // fetch file from remote
            if (isSrcDirectory) {
                log.error("ansible fetch not support fetch directory!");
                throw new NodeMgrException(ConstantCode.ANSIBLE_FETCH_NOT_DIR);
            }
            command = String.format("ansible %s -m fetch -a \"src=%s dest=%s\"", ip, src, dst);
        }
        log.info("exec scp command: [{}]", command);
        // exec ansible copy or fetch
        return JavaCommandExecutor.executeCommand(command, constant.getExecShellTimeout());
    }

    /**
     * host_check shell
     * @param ip
     * @return
     */
    public void execHostCheckShell(String ip, int nodeCount) {
        log.info("execHostCheckShell ip:{},nodeCount:{}", ip, nodeCount);
        String command = String.format("ansible %s -m script -a \"%s -C %d\"", ip, constant.getHostCheckShell(), nodeCount);
        ExecuteResult result = JavaCommandExecutor.executeCommand(command, constant.getExecShellTimeout());
        if (result.failed()) {
            if (result.getExitCode() == 3) {
                throw new NodeMgrException(ConstantCode.EXEC_HOST_CHECK_SCRIPT_ERROR_FOR_MEM.attach(result.getExecuteOut()));
            }
            if (result.getExitCode() == 4) {
                throw new NodeMgrException(ConstantCode.EXEC_HOST_CHECK_SCRIPT_ERROR_FOR_CPU.attach(result.getExecuteOut()));
            }
            throw new NodeMgrException(ConstantCode.EXEC_CHECK_SCRIPT_FAIL_FOR_PARAM);
        }
    }

    public void execDockerCheckShell(String ip) {
        log.info("execDockerCheckShell ip:{}", ip);
        String command = String.format("ansible %s -m script -a \"%s\"", ip, constant.getDockerCheckShell());
        ExecuteResult result = JavaCommandExecutor.executeCommand(command, constant.getExecShellTimeout());
        if (result.failed()) {
            if (result.getExitCode() == 5) {
                throw new NodeMgrException(ConstantCode.EXEC_DOCKER_CHECK_SCRIPT_ERROR.attach(result.getExecuteOut()));
            }
            throw new NodeMgrException(ConstantCode.EXEC_DOCKER_CHECK_SCRIPT_ERROR.attach(result.getExecuteOut()));
        }
    }

    /**
     * operate include host_init
     * 1. run host_init_shell
     * 2. sudo mkdir -p ${node_root} && sudo chown -R ${user} ${node_root} && sudo chgrp -R ${user} ${node_root}
     * param ip        Required.
     * param chainRoot chain root on host, default is /opt/fisco/{chain_name}.
     */
    public void execHostInit(String ip, String chainRoot) {
        this.execHostInitScript(ip);
        this.execCreateDir(ip, chainRoot);
    }

    public void execHostInitScript(String ip) {
        log.info("execHostInitScript ip:{}", ip);
        // mkdir
        String command = String.format("ansible %s -m command -a \"%s\"", ip, constant.getHostInitShell());
        ExecuteResult result = JavaCommandExecutor.executeCommand(command, constant.getExecShellTimeout());
        if (result.failed()) {
            throw new NodeMgrException(ConstantCode.ANSIBLE_PING_NOT_REACH.attach(result.getExecuteOut()));
        }
    }

    /**
     * mkdir directory on target ip
     * @param ip
     * @param dir absolute path
     * @return
     */
    public ExecuteResult execCreateDir(String ip, String dir) {
        log.info("execHostInitScript ip:{},dir:{}", ip, dir);
        // mkdir
        String mkdirCommand = String.format("sudo mkdir -p %s", dir);
        String command = String.format("ansible %s -m command -a \"%s\"", ip, mkdirCommand);
        return JavaCommandExecutor.executeCommand(command, constant.getExecShellTimeout());
    }



    /**
     * build_chain.sh
     * @param encryptType
     * @param ipLines
     * @return
     */
    public void execBuildChainShell(int encryptType, String[] ipLines, String chainName) {
        Path ipConf = pathService.getIpConfig(chainName);
        log.info("Exec execBuildChain method for [{}], chainName:[{}], ipConfig:[{}]",
            JsonTools.toJSONString(ipLines), chainName, ipConf.toString());
        try {
            if (!Files.exists(ipConf.getParent())) {
                Files.createDirectories(ipConf.getParent());
            }
            Files.write(ipConf, Arrays.asList(ipLines));
        } catch (IOException e) {
            log.error("Write ip conf file:[{}] error", ipConf.toAbsolutePath().toString(), e);
            throw new NodeMgrException(ConstantCode.SAVE_IP_CONFIG_FILE_ERROR);
        }

        // ports start
        String shellPortParam = String.format(" -p %s,%s,%s",
            constant.getDefaultP2pPort(), constant.getDefaultChannelPort(), constant.getDefaultJsonrpcPort());

        // build_chain.sh only support docker on linux
        // command e.g : build_chain.sh -f ipconf -o outputDir [ -p ports_start ] [ -g ] [ -d ] [ -e exec_binary ]
        String command = String.format("ansible  %s -S -f %s -o %s %s %s %s %s",
            // build_chain.sh shell script
            constant.getBuildChainShell(),
            // ipconf file path
            ipConf.toString(),
            // output path
            pathService.getChainRootString(chainName),
            // port param
//            shellPortParam,
            // guomi or standard
            encryptType == EncryptType.SM2_TYPE ? "-g " : "",
            // only linux supports docker model
            SystemUtils.IS_OS_LINUX ? " -d " : "",
            // use binary local
            StringUtils.isBlank(constant.getFiscoBcosBinary()) ? "" :
                String.format(" -e %s ", constant.getFiscoBcosBinary())
        );

        ExecuteResult result = JavaCommandExecutor.executeCommand(command, constant.getExecBuildChainTimeout());

        if (result.failed()) {
            throw new NodeMgrException(ConstantCode.EXEC_BUILD_CHAIN_ERROR.attach(result.getExecuteOut()));
        }
    }
    // todo gen agency cert

    // todo gen node

    /**
     * pull and load image by cdn
     * @param ip
     * @param outputDir
     * @param webaseVersion
     * @return
     */
    public void execPullDockerCdnShell(String ip, String outputDir, String webaseVersion) {
        log.info("execPullDockerCdnShell ip:{},outputDir:{},webaseVersion:{}", ip, outputDir, webaseVersion);
        String command = String.format("ansible %s -m script -a \"%s -d %s -v %s\"", ip, constant.getDockerCheckShell(), outputDir, webaseVersion);
        ExecuteResult result = JavaCommandExecutor.executeCommand(command, constant.getExecShellTimeout());
        if (result.failed()) {
            throw new NodeMgrException(ConstantCode.EXEC_BUILD_CHAIN_ERROR.attach(result.getExecuteOut()));
        }
    }

}

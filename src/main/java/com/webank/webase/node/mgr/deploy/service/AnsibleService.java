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
import com.webank.webase.node.mgr.base.tools.IPUtil;
import com.webank.webase.node.mgr.base.tools.SshTools;
import com.webank.webase.node.mgr.base.tools.cmd.ExecuteResult;
import com.webank.webase.node.mgr.base.tools.cmd.JavaCommandExecutor;
import java.nio.file.Files;
import java.nio.file.Paths;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
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

    // todo host check
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
            throw new NodeMgrException(ConstantCode.ANSIBLE_PING_NOT_REACH.attach(result.getExecuteOut()));
        }
    }

    public void execDockerCheckShell(String ip) {
        log.info("execDockerCheckShell ip:{}", ip);
        String command = String.format("ansible %s -m script -a \"%s\"", ip, constant.getDockerCheckShell());
        ExecuteResult result = JavaCommandExecutor.executeCommand(command, constant.getExecShellTimeout());
        if (result.failed()) {
            throw new NodeMgrException(ConstantCode.ANSIBLE_PING_NOT_REACH.attach(result.getExecuteOut()));
        }
    }

    /**
     * operate include host_init
     * 1. run host_init_shell
     * 2. sudo mkdir -p ${node_root} && sudo chown -R ${user} ${node_root} && sudo chgrp -R ${user} ${node_root}
     * param ip        Required.
     * param chainRoot chain root on host, default is /opt/fisco/{chain_name}.
     */
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
     * pull image by cdn
     * @param ip
     * @param outputDir
     * @param webaseVersion
     * @return
     */
    public ExecuteResult execPullDockerCdnShell(String ip, String outputDir, String webaseVersion) {
        log.info("execHostInitScript ip:{},outputDir:{},webaseVersion:{}", ip, outputDir, webaseVersion);
        String command = String.format("ansible %s -m script -a \"%s -d %s -v %s\"", ip, constant.getDockerCheckShell(), outputDir, webaseVersion);
        return JavaCommandExecutor.executeCommand(command, constant.getExecShellTimeout());
    }


    // todo build chain

    // todo gen agency cert

    // todo gen node
}

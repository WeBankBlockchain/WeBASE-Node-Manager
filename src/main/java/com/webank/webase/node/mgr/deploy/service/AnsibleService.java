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
import com.webank.webase.node.mgr.base.tools.ProgressTools;
import com.webank.webase.node.mgr.base.tools.cmd.ExecuteResult;
import com.webank.webase.node.mgr.base.tools.cmd.JavaCommandExecutor;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Log4j2
public class AnsibleService {

    @Autowired
    private ConstantProperties constant;
    @Autowired
    private PathService pathService;
    @Autowired
    private DockerCommandService dockerOptionsCmd;

    private static final String NOT_FOUND_FLAG = "not found";
    private static final String FREE_MEMORY_FLAG = "free memory";
    /**
     * check ansible installed
     */
    public void checkAnsible() {
        log.info("checkAnsible installed");
//        String command = "ansible --version";
        String command = "ansible --version | grep \"ansible.cfg\"";
        ExecuteResult result = JavaCommandExecutor.executeCommand(command, constant.getExecShellTimeout());
        if (result.failed()) {
            throw new NodeMgrException(ConstantCode.ANSIBLE_NOT_INSTALLED.attach(result.getExecuteOut()));
        }
    }

    /**
     * ansible exec command
     */
    public void exec(String ip, String command) {
        String ansibleCommand = String.format("ansible %s -m command -a \"%s\"", ip, command);
        ExecuteResult result = JavaCommandExecutor.executeCommand(ansibleCommand, constant.getExecShellTimeout());
        if (result.failed()) {
            throw new NodeMgrException(ConstantCode.ANSIBLE_COMMON_COMMAND_ERROR.attach(result.getExecuteOut()));
        }
    }

    /**
     * check ansible ping, code is always 0(success)
     * @case1: ip configured in ansible, output not empty. ex: 10.107.118.18 | SUCCESS => xxxxx
     * @case2: if ip not in ansible's host, output is empty. ex: Exec command success: code:[0], OUTPUT:[]
     */
    public void execPing(String ip) {
        // ansible webase(ip) -m ping
        String command = String.format("ansible %s -m ping", ip);
        ExecuteResult result = JavaCommandExecutor.executeCommand(command, constant.getExecShellTimeout());
        // if success
        if (result.getExecuteOut().contains(ip)) {
            log.info("execPing success output:{}", result.getExecuteOut());
            return;
        } else {
            throw new NodeMgrException(ConstantCode.ANSIBLE_PING_NOT_REACH.attach(result.getExecuteOut()));
        }
    }


    /**
     * copy, fetch, file(dir
     * scp: copy from local to remote, fetch from remote to local
     */
    public void scp(ScpTypeEnum typeEnum, String ip, String src, String dst) {
        log.info("scp typeEnum:{},ip:{},src:{},dst:{}", typeEnum, ip, src, dst);
        Instant startTime = Instant.now();
        log.info("scp startTime:{}", startTime.toEpochMilli());
        boolean isSrcDirectory = Files.isDirectory(Paths.get(src));
        boolean isSrcFile = Files.isRegularFile(Paths.get(src));
        // exec ansible copy or fetch
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
            // synchronized cost less time
            command = String.format("ansible %s -m synchronize -a \"src=%s dest=%s\"", ip, src, dst);
            log.info("exec scp copy command: [{}]", command);
            ExecuteResult result = JavaCommandExecutor.executeCommand(command, constant.getExecShellTimeout());
            log.info("scp usedTime:{}", Duration.between(startTime, Instant.now()).toMillis());
            if (result.failed()) {
                throw new NodeMgrException(ConstantCode.ANSIBLE_SCP_COPY_ERROR.attach(result.getExecuteOut()));
            }
        } else { // DOWNLOAD
            // fetch file from remote
            if (isSrcDirectory) {
                // fetch not support fetch directory
                log.error("ansible fetch not support fetch directory!");
                throw new NodeMgrException(ConstantCode.ANSIBLE_FETCH_NOT_DIR);
            }
            // use synchronize, mode=pull
            command = String.format("ansible %s -m synchronize -a \"mode=pull src=%s dest=%s\"", ip, src, dst);
            log.info("exec scp copy command: [{}]", command);
            ExecuteResult result = JavaCommandExecutor.executeCommand(command, constant.getExecShellTimeout());
            log.info("scp usedTime:{}", Duration.between(startTime, Instant.now()).toMillis());
            if (result.failed()) {
                throw new NodeMgrException(ConstantCode.ANSIBLE_SCP_FETCH_ERROR.attach(result.getExecuteOut()));
            }
        }
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
            if (result.getExecuteOut().contains(FREE_MEMORY_FLAG)) {
                throw new NodeMgrException(ConstantCode.EXEC_HOST_CHECK_SCRIPT_ERROR_FOR_MEM.attach(result.getExecuteOut()));
            }
            throw new NodeMgrException(ConstantCode.EXEC_CHECK_SCRIPT_FAIL_FOR_PARAM.attach(result.getExecuteOut()));
        }
    }

    /**
     * @param ip
     */
    public void execDockerCheckShell(String ip) {
        log.info("execDockerCheckShell ip:{}", ip);
        String command = String.format("ansible %s -m script -a \"%s\"", ip, constant.getDockerCheckShell());
        ExecuteResult result = JavaCommandExecutor.executeCommand(command, constant.getExecShellTimeout());
        if (result.failed()) {
            throw new NodeMgrException(ConstantCode.EXEC_DOCKER_CHECK_SCRIPT_ERROR.attach(result.getExecuteOut()));
        }
    }

    /**
     * operate include host_init
     * 1. run host_init_shell
     * 2. mkdir -p ${node_root}
     * //&& sudo chown -R ${user} ${node_root} && sudo chgrp -R ${user} ${node_root}
     * param ip        Required.
     * param chainRoot chain root on host, default is /opt/fisco/{chain_name}.
     */
    public void execHostInit(String ip, String chainRoot) {
        this.execHostInitScript(ip);
        this.execCreateDir(ip, chainRoot);
    }

    public void execHostInitScript(String ip) {
        log.info("execHostInitScript ip:{}", ip);
        String command = String.format("ansible %s -m script -a \"%s\"", ip, constant.getHostInitShell());
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
        log.info("execCreateDir ip:{},dir:{}", ip, dir);
        // not use sudo to make dir, check access
        String mkdirCommand = String.format("mkdir -p %s", dir);
        String command = String.format("ansible %s -m command -a \"%s\"", ip, mkdirCommand);
        return JavaCommandExecutor.executeCommand(command, constant.getExecShellTimeout());
    }

    /* docker operation: checkImageExists, pullImage run stop */

    /**
     * if not found, ansible exit code not same as script's exit code, use String to distinguish "not found"
     * @param ip
     * @param imageFullName
     * @return
     */
    public boolean checkImageExists(String ip, String imageFullName) {
        log.info("checkImageExists ip:{},imageFullName:{}", ip, imageFullName);

        String command = String.format("ansible %s -m script -a \"%s -i %s\"", ip,
            constant.getAnsibleImageCheckShell(), imageFullName);
        ExecuteResult result = JavaCommandExecutor.executeCommand(command, constant.getExecDockerCheckTimeout());
        if (result.failed()) {
            // NOT FOUND IMAGE
            if (result.getExecuteOut().contains(NOT_FOUND_FLAG)) {
                return false;
            }
            // PARAM ERROR
            if (result.getExitCode() == 2) {
                throw new NodeMgrException(ConstantCode.ANSIBLE_CHECK_DOCKER_IMAGE_ERROR.attach(result.getExecuteOut()));
            }
        }
        // found
        return true;
    }

    public boolean checkContainerExists(String ip, String containerName) {
        log.info("checkContainerExists ip:{},containerName:{}", ip, containerName);

        // docker ps | grep "${containerName}"
        String command = String.format("ansible %s -m script -a \"%s -c %s\"", ip, constant.getAnsibleContainerCheckShell(), containerName);
        ExecuteResult result = JavaCommandExecutor.executeCommand(command, constant.getExecDockerCheckTimeout());
        if (result.failed()) {
            // NOT FOUND CONTAINER
            if (result.getExecuteOut().contains(NOT_FOUND_FLAG)) {
                return false;
            }
            // PARAM ERROR
            if (result.getExitCode() == 2) {
                throw new NodeMgrException(ConstantCode.ANSIBLE_CHECK_CONTAINER_ERROR.attach(result.getExecuteOut()));
            }
        }
        // found
        return true;
    }

    /**
     * pull and load image by cdn
     * @param ip
     * @param outputDir
     * @param webaseVersion
     * @return
     */
    public void execPullDockerCdnShell(String ip, String outputDir, String imageTag, String webaseVersion) {
        log.info("execPullDockerCdnShell ip:{},outputDir:{},imageTag:{},webaseVersion:{}", ip, outputDir, imageTag, webaseVersion);
        Instant startTime = Instant.now();
        log.info("execPullDockerCdnShell startTime:{}", startTime.toEpochMilli());
        boolean imageExist = this.checkImageExists(ip, imageTag);
        if (imageExist) {
            log.info("image of {} already exist, jump over pull", imageTag);
            return;
        }
        String command = String.format("ansible %s -m script -a \"%s -d %s -v %s\"", ip, constant.getDockerPullCdnShell(), outputDir, webaseVersion);
        ExecuteResult result = JavaCommandExecutor.executeCommand(command, constant.getExecShellTimeout());
        log.info("execPullDockerCdnShell usedTime:{}", Duration.between(startTime, Instant.now()).toMillis());
        if (result.failed()) {
            throw new NodeMgrException(ConstantCode.ANSIBLE_PULL_DOCKER_CDN_ERROR.attach(result.getExecuteOut()));
        }
    }


    public ExecuteResult execDocker(String ip, String dockerCommand) {
        log.info("execDocker ip:{},dockerCommad:{}", ip, dockerCommand);
        String command = String.format("ansible %s -m command -a \"%s\"", ip, dockerCommand);
        ExecuteResult result = JavaCommandExecutor.executeCommand(command, constant.getDockerRestartPeriodTime());
        return result;
    }

    /**
     * mv dir on remote
     */
    public void mvDirOnRemote(String ip, String src, String dst){
        if (StringUtils.isNoneBlank(ip, src, dst)) {
            String rmCommand = String.format("mv -fv %s %s", src, dst);
            log.info("Remove config on remote host:[{}], command:[{}].", ip, rmCommand);
            this.exec(ip, rmCommand);
        }
    }

    /**
     * get exec result in host check
     * @param ip
     * @param ports
     * @return
     */
    public ExecuteResult checkPortArrayInUse(String ip, int ... ports) {
        log.info("checkPortArrayInUse ip:{},ports:{}", ip, ports);
        if (ArrayUtils.isEmpty(ports)){
            return new ExecuteResult(0, "ports input is empty");
        }
        StringBuilder portArray = new StringBuilder();
        for (int port : ports) {
            if (portArray.length() == 0) {
                portArray.append(port);
                continue;
            }
            portArray.append(",").append(port);
        }
        String command = String.format("ansible %s -m script -a \"%s -p %s\"", ip, constant.getHostCheckPortShell(), portArray);
        ExecuteResult result = JavaCommandExecutor.executeCommand(command, constant.getExecShellTimeout());
        return result;
    }

    /**
     * check port, if one is in use, break and return false
     * used in restart chain to make sure process is on
     * @param ip
     * @param portArray
     * @return Pair of <true, port> true: not in use, false: in use
     */
    public Pair<Boolean, Integer> checkPorts(String ip, int ... portArray) {
        if (ArrayUtils.isEmpty(portArray)){
            return Pair.of(true,0);
        }

        for (int port : portArray) {
            boolean notInUse = checkPortInUse(ip, port);
            // if false, in use
            if (!notInUse){
                return Pair.of(false, port);
            }
        }
        return Pair.of(true,0);
    }

    /**
     * check port by ansible script
     * @param ip
     * @param port
     * @return Pair of <true, port> true: not in use, false: in use
     */
    private boolean checkPortInUse(String ip, int port) {
        log.info("checkPortInUse ip:{},port:{}", ip, port);
        String command = String.format("ansible %s -m script -a \"%s -p %s\"", ip, constant.getHostCheckPortShell(), port);
        ExecuteResult result = JavaCommandExecutor.executeCommand(command, constant.getExecShellTimeout());
        return result.success();
    }

    /**
     * exec on 127.0.0.1
     * check 127.0.0.1 if same with other host ip
     * @param ipList ip to check same with local ip 127.0.0.1
     * @return true-success, false-failed
     */
    public boolean checkLocalIp(List<String> ipList) {
        log.info("checkLoopIp ipArray:{}", ipList);
        if (ipList == null || ipList.isEmpty()){
            return true;
        }
        StringBuilder ipStrArray = new StringBuilder();
        for (String ip : ipList) {
            if (ipStrArray.length() == 0) {
                ipStrArray.append(ip);
                continue;
            }
            ipStrArray.append(",").append(ip);
        }
        // ansible 127.0.0.1 -m script hostCheckIpShell
        String command = String.format("ansible %s -m script -a \"%s -p %s\"", IPUtil.LOCAL_IP_127,
            constant.getHostCheckIpShell(), ipStrArray);
        ExecuteResult result = JavaCommandExecutor.executeCommand(command, constant.getExecShellTimeout());
        return result.success();
    }
}

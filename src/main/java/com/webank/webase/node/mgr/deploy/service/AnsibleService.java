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

package com.webank.webase.node.mgr.deploy.service;

import com.qctc.host.api.model.HostDTO;
import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.enums.ScpTypeEnum;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.config.properties.ConstantProperties;
import com.webank.webase.node.mgr.tools.CleanPathUtil;
import com.webank.webase.node.mgr.tools.IPUtil;
import com.webank.webase.node.mgr.tools.cmd.ExecuteResult;
import com.webank.webase.node.mgr.tools.cmd.JavaCommandExecutor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

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
    public void exec(HostDTO hostDTO, String command) {
        String ansibleCommand = String.format("ansible all -i %s, -u %s -e ansible_ssh_port=%d --connection=ssh -m command -a \"%s\"", hostDTO.getIp(), hostDTO.getUser(), hostDTO.getPort(), command);
        ExecuteResult result = JavaCommandExecutor.executeCommand(ansibleCommand, constant.getExecShellTimeout());
        if (result.failed()) {
            throw new NodeMgrException(ConstantCode.ANSIBLE_COMMON_COMMAND_ERROR.attach(result.getExecuteOut()));
        }
    }

    /**
     * check ansible ping, code is always 0(success)
     * @case1: ip configured in ansible, output not empty. ex: 127.0.0.1 | SUCCESS => xxxxx
     * @case2: if ip not in ansible's host, output is empty. ex: Exec command success: code:[0], OUTPUT:[]
     */
    public void execPing(HostDTO hostDTO) {
        // ansible webase(ip) -m ping
        String command = String.format("ansible all -i %s, -u %s -e ansible_ssh_port=%d --connection=ssh -m ping", hostDTO.getIp(), hostDTO.getUser(), hostDTO.getPort());
        ExecuteResult result = JavaCommandExecutor.executeCommand(command, constant.getExecShellTimeout());
        // if success
        if (result.getExecuteOut().contains(hostDTO.getIp())) {
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
    public void scp(ScpTypeEnum typeEnum, HostDTO hostDTO, String src, String dst) {
        log.info("scp typeEnum:{},ip:{},src:{},dst:{}", typeEnum, hostDTO.getIp(), src, dst);
        Instant startTime = Instant.now();
        log.info("scp startTime:{}", startTime.toEpochMilli());
        boolean isSrcDirectory = Files.isDirectory(Paths.get(CleanPathUtil.cleanString(src)));
        boolean isSrcFile = Files.isRegularFile(Paths.get(CleanPathUtil.cleanString(src)));
        // exec ansible copy or fetch
        String command;
        if (typeEnum == ScpTypeEnum.UP) {
            // handle file's dir local or remote
            if (isSrcFile) {
                // if src is file, create parent directory of dst on remote
                String parentOnRemote = Paths.get(CleanPathUtil.cleanString(dst)).getParent().toAbsolutePath().toString();
                this.execCreateDir(hostDTO, parentOnRemote);
            }
            if (isSrcDirectory) {
                // if src is directory, create dst on remote
                this.execCreateDir(hostDTO, dst);
            }
            // synchronized cost less time
            command = String.format("ansible all -i %s, -u %s -e ansible_ssh_port=%d --connection=ssh -m synchronize -a \"src=%s dest=%s\"", hostDTO.getIp(), hostDTO.getUser(), hostDTO.getPort(), src, dst);
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
            command = String.format("ansible all -i %s, -u %s -e ansible_ssh_port=%d --connection=ssh -m synchronize -a \"mode=pull src=%s dest=%s\"", hostDTO.getIp(), hostDTO.getUser(), hostDTO.getPort(), src, dst);
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
    public void execHostCheckShell(HostDTO hostDTO, int nodeCount) {
        log.info("execHostCheckShell ip:{},nodeCount:{}", hostDTO.getIp(), nodeCount);
        String command = String.format("ansible all -i %s, -u %s -e ansible_ssh_port=%d --connection=ssh -m script -a \"%s -C %d\"", hostDTO.getIp(), hostDTO.getUser(), hostDTO.getPort(), constant.getHostCheckShell(), nodeCount);
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
    public void execDockerCheckShell(HostDTO hostDTO) {
        log.info("execDockerCheckShell ip:{}", hostDTO.getIp());
        String command = String.format("ansible all -i %s, -u %s -e ansible_ssh_port=%d --connection=ssh -m script -a \"%s\"", hostDTO.getIp(), hostDTO.getUser(), hostDTO.getPort(), constant.getDockerCheckShell());
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
    public void execHostInit(HostDTO hostDTO, String chainRoot) {
        this.execHostInitScript(hostDTO);
        this.execCreateDir(hostDTO, chainRoot);
    }

    public void execHostInitScript(HostDTO hostDTO) {
        log.info("execHostInitScript ip:{}", hostDTO.getIp());
        String command = String.format("ansible all -i %s, -u %s -e ansible_ssh_port=%d --connection=ssh -m script -a \"%s\"", hostDTO.getIp(), hostDTO.getUser(), hostDTO.getPort(), constant.getHostInitShell());
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
    public ExecuteResult execCreateDir(HostDTO hostDTO, String dir) {
        log.info("execCreateDir ip:{},dir:{}", hostDTO.getIp(), dir);
        // not use sudo to make dir, check access
        String mkdirCommand = String.format("mkdir -p %s", dir);
        String command = String.format("ansible all -i %s, -u %s -e ansible_ssh_port=%d --connection=ssh -m command -a \"%s\"", hostDTO.getIp(), hostDTO.getUser(), hostDTO.getPort(), mkdirCommand);
        return JavaCommandExecutor.executeCommand(command, constant.getExecShellTimeout());
    }

    /* docker operation: checkImageExists, pullImage run stop */

    /**
     * if not found, ansible exit code not same as script's exit code, use String to distinguish "not found"
     * @param ip
     * @param imageFullName
     * @return
     */
    public boolean checkImageExists(HostDTO hostDTO, String imageFullName) {
        log.info("checkImageExists ip:{},imageFullName:{}", hostDTO.getIp(), imageFullName);

        String command = String.format("ansible all -i %s, -u %s -e ansible_ssh_port=%d --connection=ssh -m script -a \"%s -i %s\"", hostDTO.getIp(), hostDTO.getUser(), hostDTO.getPort(),
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

    public boolean checkContainerExists(HostDTO hostDTO, String containerName) {
        log.info("checkContainerExists ip:{},containerName:{}", hostDTO.getIp(), containerName);

        // docker ps | grep "${containerName}"
        String command = String.format("ansible all -i %s, -u %s -e ansible_ssh_port=%d --connection=ssh -m script -a \"%s -c %s\"", hostDTO.getIp(), hostDTO.getUser(), hostDTO.getPort(), constant.getAnsibleContainerCheckShell(), containerName);
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
    public void execPullDockerCdnShell(HostDTO hostDTO, String outputDir, String imageTag, String webaseVersion) {
        log.info("execPullDockerCdnShell ip:{},outputDir:{},imageTag:{},webaseVersion:{}", hostDTO.getIp(), outputDir, imageTag, webaseVersion);
        Instant startTime = Instant.now();
        log.info("execPullDockerCdnShell startTime:{}", startTime.toEpochMilli());
        boolean imageExist = this.checkImageExists(hostDTO, imageTag);
        if (imageExist) {
            log.info("image of {} already exist, jump over pull", imageTag);
            return;
        }
        String command = String.format("ansible all -i %s, -u %s -e ansible_ssh_port=%d --connection=ssh -m script -a \"%s -d %s -v %s\"", hostDTO.getIp(), hostDTO.getUser(), hostDTO.getPort(), constant.getDockerPullCdnShell(), outputDir, webaseVersion);
        ExecuteResult result = JavaCommandExecutor.executeCommand(command, constant.getExecShellTimeout());
        log.info("execPullDockerCdnShell usedTime:{}", Duration.between(startTime, Instant.now()).toMillis());
        if (result.failed()) {
            throw new NodeMgrException(ConstantCode.ANSIBLE_PULL_DOCKER_CDN_ERROR.attach(result.getExecuteOut()));
        }
    }


    public ExecuteResult execDocker(HostDTO hostDTO, String dockerCommand) {
        log.info("execDocker ip:{},dockerCommad:{}", hostDTO.getIp(), dockerCommand);
        String command = String.format("ansible all -i %s, -u %s -e ansible_ssh_port=%d --connection=ssh -m command -a \"%s\"", hostDTO.getIp(), hostDTO.getUser(), hostDTO.getPort(), dockerCommand);
        ExecuteResult result = JavaCommandExecutor.executeCommand(command, constant.getDockerRestartPeriodTime());
        return result;
    }

    public ExecuteResult execDockerPull(HostDTO hostDTO, String dockerCommand) {
        log.info("execDocker ip:{},dockerCommad:{}", hostDTO.getIp(), dockerCommand);
        String command = String.format("ansible all -i %s, -u %s -e ansible_ssh_port=%d --connection=ssh -m command -a \"%s\"", hostDTO.getIp(), hostDTO.getUser(), hostDTO.getPort(), dockerCommand);
        ExecuteResult result = JavaCommandExecutor.executeCommand(command, constant.getExecHostInitTimeout());
        return result;
    }

    /**
     * mv dir on remote
     */
    public void mvDirOnRemote(HostDTO hostDTO, String src, String dst){
        if (StringUtils.isNoneBlank(hostDTO.getIp(), src, dst)) {
            String rmCommand = String.format("mv -fv %s %s", src, dst);
            log.info("Remove config on remote host:[{}], command:[{}].", hostDTO.getIp(), rmCommand);
            this.exec(hostDTO, rmCommand);
        }
    }

    /**
     * get exec result in host check
     * @param ip
     * @param ports
     * @return
     */
    public ExecuteResult checkPortArrayInUse(HostDTO hostDTO, int ... ports) {
        log.info("checkPortArrayInUse ip:{},ports:{}", hostDTO.getIp(), ports);
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
        String command = String.format("ansible all -i %s, -u %s -e ansible_ssh_port=%d --connection=ssh -m script -a \"%s -p %s\"", hostDTO.getIp(), hostDTO.getUser(), hostDTO.getPort(), constant.getHostCheckPortShell(), portArray);
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
    public Pair<Boolean, Integer> checkPorts(HostDTO hostDTO, int ... portArray) {
        if (ArrayUtils.isEmpty(portArray)){
            return Pair.of(true,0);
        }

        for (int port : portArray) {
            boolean notInUse = checkPortInUse(hostDTO, port);
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
    private boolean checkPortInUse(HostDTO hostDTO, int port) {
        log.info("checkPortInUse ip:{},port:{}", hostDTO.getIp(), port);
        String command = String.format("ansible all -i %s, -u %s -e ansible_ssh_port=%d --connection=ssh -m script -a \"%s -p %s\"", hostDTO.getIp(), hostDTO.getUser(), hostDTO.getPort(), constant.getHostCheckPortShell(), port);
        ExecuteResult result = JavaCommandExecutor.executeCommand(command, constant.getExecShellTimeout());
        return result.success();
    }

//    /**
//     * exec on 127.0.0.1
//     * check 127.0.0.1 if same with other host ip
//     * @param ipList ip to check same with local ip 127.0.0.1
//     * @return true-success, false-failed
//     */
//    public boolean checkLocalIp(List<String> ipList) {
//        log.info("checkLoopIp ipArray:{}", ipList);
//        if (ipList == null || ipList.isEmpty()){
//            return true;
//        }
//        StringBuilder ipStrArray = new StringBuilder();
//        for (String ip : ipList) {
//            if (ipStrArray.length() == 0) {
//                ipStrArray.append(ip);
//                continue;
//            }
//            ipStrArray.append(",").append(ip);
//        }
//        // ansible 127.0.0.1 -m script hostCheckIpShell
//        String command = String.format("ansible %s -m script -a \"%s -p %s\"", IPUtil.LOCAL_IP_127,
//            constant.getHostCheckIpShell(), ipStrArray);
//        ExecuteResult result = JavaCommandExecutor.executeCommand(command, constant.getExecShellTimeout());
//        return result.success();
//    }
}

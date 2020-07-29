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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.base.tools.cmd.ExecuteResult;
import com.webank.webase.node.mgr.base.tools.cmd.JavaCommandExecutor;

import lombok.extern.log4j.Log4j2;


@Log4j2
public class SshTools {

    public static final String DEFAULT_SSH_USER="root";
    public static final int DEFAULT_SSH_PORT=22;

    private static Properties config = new Properties();

    static {
        config.put("StrictHostKeyChecking", "no");
        config.put("CheckHostIP", "no");
        config.put("Compression", "yes");
        config.put("PreferredAuthentications", "publickey");
    }

    /**
     *
     * @param ip
     * @param originalCommand
     */
    private static Pair<Boolean,String> exec(String ip, String originalCommand, String sshUser, int sshPort, String privateKey) {
        StringBuilder newCommandBuilder = new StringBuilder(originalCommand);
        if (IPUtil.isLocal(ip)){
            ExecuteResult result = JavaCommandExecutor.executeCommand(originalCommand, 0);
            if (result.failed()) {
                log.error("SshTools exec on localhost:[{}] command:[{}] error.", ip, originalCommand );
            }
            return Pair.of(result.success(),"");
        }else{
            newCommandBuilder.append(" ; exit 0;");
        }
        String newCommand = newCommandBuilder.toString();
        Session session = connect(ip, sshPort, sshUser, "",privateKey, 0);
        if (session != null && session.isConnected()) {
            ChannelExec channelExec = null;
            StringBuilder execLog = new StringBuilder();
            BufferedReader reader = null;
            try {
                channelExec = (ChannelExec) session.openChannel("exec");
                InputStream in = channelExec.getInputStream();
                channelExec.setCommand(newCommandBuilder.toString());
                channelExec.connect();

                reader = new BufferedReader(new InputStreamReader(in));
                String line;
                while ((line = reader.readLine()) != null) {
                    execLog.append(line).append("\n");
                }

                int status = channelExec.getExitStatus();
                if (status < 0) {
                    log.error("Exec command:[{}] on remote host:[{}], no exit status:[{}] not set, log:[{}].",
                            newCommand, ip, status, execLog.toString());
                    return Pair.of(true,execLog.toString());
                } else if (status == 0) {
                    log.info("Exec command:[{}] on remote host:[{}] success, log:[{}].", newCommand, ip, execLog.toString());
                    return Pair.of(true,execLog.toString());
                } else {
                    log.error("Exec command:[{}] on remote host:[{}] with error[{}], log:[{}].", newCommand, ip, status, execLog.toString());
                }
            } catch (Exception e) {
                log.error("Exec command:[{}] on remote host:[{}] occurred exception.", newCommand, ip, e);
            } finally {
                if (channelExec != null) {
                    channelExec.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        log.error("SSH close error to ip:[{}]",ip,e);
                    }
                }
                session.disconnect();
            }
        }
        return Pair.of(false,"");
    }

    /**
     * @param ip
     * @return
     */
    public static boolean connect(String ip,String sshUser,int sshPort,String privateKey) {
        if (IPUtil.isLocal(ip)) {
            return true;
        }

        Session session = connect(ip, sshPort, sshUser, "",privateKey, 0);
        if (session != null && session.isConnected()) {
            session.disconnect();
            return true;
        }
        return false;
    }

    /**
     * @param ip
     * @param port
     * @param user
     * @param password
     * @param privateKey
     * @param connectTimeoutInSeconds seconds.
     * @return
     */
    private static Session connect(
            String ip,
            final int port,
            final String user,
            String password,
            String privateKey,
            final int connectTimeoutInSeconds) {
        if (StringUtils.isBlank(ip)
                || (!"localhost".equals(ip) && !ValidateUtil.ipv4Valid(ip))) {
            return null;
        }
        String newUser = StringUtils.isBlank(user) ? DEFAULT_SSH_USER : user;
        int newPort = port <= 0 ? DEFAULT_SSH_PORT : port;
        boolean pubAuth = StringUtils.isBlank(password);

        // set default connect timeout to 10s
        int newConnectTimeoutInSeconds = connectTimeoutInSeconds <= 0 ? 10 : connectTimeoutInSeconds;

        String hostDetail = String.format("[%s@%s:%s] by [%s] with connectTimeout:[%s]",
                newUser, ip, newPort, pubAuth ? "public_key" : "password", newConnectTimeoutInSeconds);
        log.info("Start to connect to host:{} using SSH...", hostDetail);
        JSch jsch = new JSch();
        Session session = null;
        try {
            session = jsch.getSession(newUser, ip, newPort);
            session.setConfig(config);
            if (pubAuth) {
                jsch.addIdentity(privateKey);
            } else {
                throw new NodeMgrException(ConstantCode.UNSUPPORTED_PASSWORD_SSH_ERROR);
            }
            session.connect(newConnectTimeoutInSeconds * 1000);
        } catch (Exception e) {
            log.error("Connect to host:[{}] ERROR!!!", hostDetail, e);
        }
        return session;
    }

    /**
     *
     * @param ip
     * @param dir
     */
    public static void createDirOnRemote(String ip, String dir, String sshUser, int sshPort,String privateKey){
        if(IPUtil.isLocal(ip)){
            try {
                Files.createDirectories(Paths.get(dir));
            } catch (IOException e) {
                log.error("mkdir:[{}] on localhost:[{}] error",dir,ip,e );
            }
        }else{
            exec(ip, String.format("sudo mkdir -p %s", dir),sshUser,sshPort,privateKey);
            exec(ip, String.format("sudo chown -R %s %s ", sshUser,dir),sshUser,sshPort,privateKey);
            exec(ip, String.format("sudo chgrp -R %s %s ", sshUser,dir),sshUser,sshPort,privateKey);
        }
    }

    /**
     *
     * @param ip
     * @param src
     * @param dst
     */
    public static void mvDirOnRemote(String ip, String src, String dst, String sshUser, int sshPort,String privateKey){
        if (StringUtils.isNoneBlank(ip,src,dst)) {
            String rmCommand = String.format("sudo mv -fv %s %s", src, dst);
            log.info("Remove config on remote host:[{}], command:[{}].", ip, rmCommand);
            exec(ip, rmCommand,sshUser,sshPort,privateKey);
        }
    }

    /**
     * Exec docker command.
     *
     * @param ip
     * @param originalCommand
     * @param sshUser
     * @param sshPort
     * @return
     */
    public static Pair<Boolean,String> execDocker(String ip, String originalCommand, String sshUser,int sshPort,String privateKey) {
        log.info("Execute docker command:[{}] on host:[{}]", originalCommand, ip);
        return exec(ip,originalCommand,sshUser,sshPort,privateKey);
    }

    /**
     * kill an executing command.
     *
     * @param ip
     * @param commandToKill
     * @param sshUser
     * @param sshPort
     * @param privateKey
     * @return
     */
    public static boolean killCommand(String ip, String commandToKill, String sshUser,int sshPort,String privateKey) {
        String newCommand= String.format("sudo pkill -f '%s'", commandToKill);
        log.info("Execute kill command:[{}] on host:[{}]", newCommand, ip);
        return exec(ip,newCommand,sshUser,sshPort,privateKey).getKey();
    }

}
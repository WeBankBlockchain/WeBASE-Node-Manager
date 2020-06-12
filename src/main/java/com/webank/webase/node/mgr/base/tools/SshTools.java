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

import static com.webank.webase.node.mgr.base.properties.ConstantProperties.SSH_DEFAULT_PORT;
import static com.webank.webase.node.mgr.base.properties.ConstantProperties.SSH_DEFAULT_USER;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;

import lombok.extern.log4j.Log4j2;


@Log4j2
public class SshTools {

    private static Properties config = new Properties();

    static {
        config.put("StrictHostKeyChecking", "no");
        config.put("CheckHostIP", "no");
        config.put("Compression", "yes");
        config.put("PreferredAuthentications", "publickey");
    }

    public final static String PRIVATE_KEY = System.getProperty("user.home") + File.separator + ".ssh" + File.separator + "id_rsa";

    public final static String[] LOCAL_ARRAY = new String[]{"127.0.0.1", "localhost"};

    /**
     * Check ip is local.
     *
     * @param ip
     * @return
     */
    public static boolean isLocal(String ip) {
        return Stream.of(LOCAL_ARRAY).anyMatch(ip::equalsIgnoreCase);

    }

    /**
     * TODO exceptions and exec log
     *
     * @param ip
     * @param command
     */
    public static boolean exec(String ip, String command) {
        Session session = connect(ip, SSH_DEFAULT_PORT, SSH_DEFAULT_USER, "", 0);
        if (session != null && session.isConnected()) {
            ChannelExec channelExec = null;
            StringBuilder execLog = new StringBuilder();
            BufferedReader reader = null;
            try {
                channelExec = (ChannelExec) session.openChannel("exec");
                InputStream in = channelExec.getInputStream();
                channelExec.setCommand(command);
                channelExec.connect();

                reader = new BufferedReader(new InputStreamReader(in));
                String line;
                while ((line = reader.readLine()) != null) {
                    execLog.append(line).append("\n");
                }

                int status = channelExec.getExitStatus();
                boolean success = status == 0;
                if (success) {
                    log.error("Exec command:[{}] on remote host:[{}] success:[{}].", command, ip, execLog.toString());
                    return true;
                } else {
                    log.error("Exec command:[{}] on remote host:[{}] with error:[{}:{}].", command, ip, status, execLog.toString());
                }
            } catch (Exception e) {
                log.error("Exec command:[{}] on remote host:[{}] occurred exception.", command, ip, e);
            } finally {
                // TODO.
                if (channelExec != null) {
                    channelExec.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                    }
                }
                session.disconnect();
            }
        }
        return false;
    }

    /**
     * @param ip
     * @return
     */
    public static boolean connect(String ip) {
        if (isLocal(ip)) {
            return true;
        }

        Session session = connect(ip, SSH_DEFAULT_PORT, SSH_DEFAULT_USER, "", 0);
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
     * @param connectTimeoutInSeconds seconds.
     * @return
     */
    public static Session connect(
            String ip,
            final int port,
            final String user,
            String password,
            final int connectTimeoutInSeconds) {
        if (StringUtils.isBlank(ip)
                || (!"localhost".equals(ip) && !ValidateUtil.ipv4Valid(ip))) {
            return null;
        }
        String newUser = StringUtils.isBlank(user) ? SSH_DEFAULT_USER : user;
        int newPort = port <= 0 ? SSH_DEFAULT_PORT : port;
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
                jsch.addIdentity(PRIVATE_KEY);
            } else {
                throw new NodeMgrException(ConstantCode.UNSUPPORTED_PASSWORD_SSH_ERROR);
            }
            session.connect(newConnectTimeoutInSeconds * 1000);
        } catch (Exception e) {
            log.info("Connect to host:[{}] ERROR!!!", hostDetail, e);
        }
        return session;
    }

}
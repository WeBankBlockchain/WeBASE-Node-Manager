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

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;

import lombok.extern.slf4j.Slf4j;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;


@Slf4j
public class SshTools {

    /**
     * @param ip
     * @return
     * @throws IOException
     */
    public static boolean iSConnectable(String ip) {
        return iSConnectable(ip,SSH_DEFAULT_PORT , SSH_DEFAULT_USER, "", 5);
    }

    /**
     * @param ip
     * @param port
     * @param user
     * @param password
     * @param connectTimeoutInSeconds seconds.
     * @return
     * @throws IOException
     */
    public static boolean iSConnectable(
            String ip,
            short port,
            String user,
            String password,
            int connectTimeoutInSeconds) {
        if (StringUtils.isBlank(ip)
                || (!ip.equals("localhost") && !ValidateUtil.validateIpv4(ip))) {
            return false;
        }
        user = StringUtils.isBlank(user) ? SSH_DEFAULT_USER : user;
        port = port <= 0 ? SSH_DEFAULT_PORT : port;
        boolean pubAuth = StringUtils.isBlank(password);

        // set default connect timeout to 10s
        connectTimeoutInSeconds = connectTimeoutInSeconds <= 0 ? 10 : connectTimeoutInSeconds;

        String host = String.format("[%s@%s:%s] by [%s] with connectTimeout:[%s]",
                user, ip, port, pubAuth ? "public_key" : "password", connectTimeoutInSeconds);
        log.info("Start to connect to host:[{}] using SSH...", host);

        final SSHClient ssh = new SSHClient();
        try {
            ssh.addHostKeyVerifier(new PromiscuousVerifier());
            ssh.setConnectTimeout(connectTimeoutInSeconds * 1000);
            ssh.connect(ip, port);
            if (pubAuth) {
                ssh.authPublickey(user);
            } else {
                ssh.authPassword(user, password);
            }
            if (ssh.isConnected()) { // connect success
                return true;
            }
        } catch (Exception e) {
            log.info("Connect to host:[{}] ERROR!!!", host, e);
        } finally {
            try {
                ssh.disconnect();
            } catch (IOException e) {
                // do nothing
                log.warn("Disconnect from host: [{}] error", host, e);
            }
        }
        return false;
    }


}
/**
 * Copyright 2014-2021 the original author or authors.
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

import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.config.properties.ConstantProperties;
import com.webank.webase.node.mgr.deploy.chain.ChainService;
import com.webank.webase.node.mgr.deploy.entity.DeployNodeInfo;
import com.webank.webase.node.mgr.tools.JsonTools;
import com.webank.webase.node.mgr.tools.cmd.ExecuteResult;
import com.webank.webase.node.mgr.tools.cmd.JavaCommandExecutor;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import com.webank.webase.node.mgr.user.UserService;
import com.webank.webase.node.mgr.user.entity.TbUser;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.fisco.bcos.sdk.v3.model.CryptoType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * Java call shell script and system command.
 * exec build_chain gen cert etc.
 */
@Log4j2
@Component
public class DeployShellService {

    @Autowired
    private ConstantProperties constant;
    @Autowired
    private PathService pathService;

    @Lazy
    @Autowired
    private UserService userService;

    @Lazy
    @Autowired
    private ChainService chainService;


    /**
     * build_chain.sh
     * @param encryptType
     * @param ipLines
     * @param chainName
     * @param chainVersion ex: 2.7.2 without v
     * @return
     */
    public void execBuildChain(int encryptType, String[] ipLines, String chainName, String chainVersion, String groupId, int enableAuth) {
        String chainId = "chain" + groupId;
        chainName = chainService.getChainDirName(chainName, chainId);

        Path ipConf = pathService.getIpConfig(chainName);
        log.info("Exec execBuildChain method for [{}], chainName:[{}], ipConfig:[{}]",
                JsonTools.toJSONString(ipLines), chainName, ipConf.toString());
        try {
            if (!Files.exists(ipConf.getParent())) {
                Files.createDirectories(ipConf.getParent());
            }
            Files.write(ipConf, Arrays.asList(ipLines));
        } catch (IOException e) {
            log.error("execBuildChain Write ip conf file:[{}] error", ipConf.toAbsolutePath().toString(), e);
            throw new NodeMgrException(ConstantCode.SAVE_IP_CONFIG_FILE_ERROR);
        }
        Path chainRoot = pathService.getChainRoot(chainName);
        log.info("execBuildChain output chainRoot:{}", chainRoot.toString());

        if (Files.exists(chainRoot)) {
            log.error("execBuildChain output chainRoot already exist! please move it manually!");
            throw new NodeMgrException(ConstantCode.CHAIN_ROOT_DIR_EXIST);
        }

//        // build_chain.sh only support docker on linux
//        // command e.g : build_chain.sh -f ipconf -o outputDir [ -p ports_start ] [ -g ] [ -d ] [ -e exec_binary ] [ -v support_version ]
//        String command = String.format("bash %s -S -f %s -o %s %s %s %s %s",
//                // build_chain.sh shell script
//                constant.getBuildChainShell(),
//                // ipconf file path
//                ipConf.toString(),
//                // output path
//                chainRoot.toString(),
//                // port param
//                //shellPortParam,
//                // guomi or standard
//                encryptType == CryptoType.SM_TYPE ? "-g " : "",
//                // only linux supports docker model
//                SystemUtils.IS_OS_LINUX ? " -d " : "",
//                // use binary local
//                StringUtils.isBlank(constant.getFiscoBcosBinary()) ? "" :
//                        String.format(" -e %s ", constant.getFiscoBcosBinary()),
//                String.format(" -v %s ", chainVersion)
//        );

        String formatString = "bash %s -f %s -o %s %s %s %s %s -g %s -I %s";
        if (enableAuth == 1) {
            formatString = "bash %s -f %s -o %s %s %s %s %s -g %s -A -I %s";

            // 如果之前建完链，删除过链，则admin_auth存在，不再新生成授权管理员
            TbUser admin = userService.queryUser(null, null, "admin_auth" + groupId, null, null);
            if (null != admin && admin.getAddress() != null && admin.getAddress().length() > 0) {
                String adminAddr = admin.getAddress();
                formatString = "bash %s -f %s -o %s %s %s %s %s -g %s -I %s -a " + adminAddr;
            }
        }

        // chainid此时没有，写死为1
        String command = String.format(formatString,
                // build_chain.sh shell script
                constant.getBuildChainShell(),
                // ipconf file path
                ipConf.toString(),
                // output path
                chainRoot.toString(),
                // port param
                //shellPortParam,
                // guomi or standard
                encryptType == CryptoType.SM_TYPE ? "-s " : "",
                // only linux supports docker model
                SystemUtils.IS_OS_LINUX ? " -D " : "",
                // use binary local
                StringUtils.isBlank(constant.getFiscoBcosBinary()) ? "" :
                        String.format(" -e %s ", constant.getFiscoBcosBinary()),
                String.format(" -v %s ", chainVersion),
                groupId,
                chainId
        );

        ExecuteResult result = JavaCommandExecutor.executeCommand(command, constant.getExecBuildChainTimeout());

        if (result.failed()) {
            throw new NodeMgrException(ConstantCode.EXEC_BUILD_CHAIN_ERROR.attach(result.getExecuteOut()));
        }
    }

    /**
     *
     * @param encryptType
     * @param chainName
     * @param newAgencyName
     * @return
     */
    public ExecuteResult execGenAgency(byte encryptType, String chainName, String newAgencyName) {
        log.info("Exec execGenAgency method for chainName:[{}], newAgencyName:[{}:{}]",
            chainName, newAgencyName, encryptType);

        Path certRoot = this.pathService.getCertRoot(chainName);

        if (Files.notExists(certRoot)) {
            // file not exists
            log.error("Chain cert : [{}] not exists in directory:[{}] ",
                chainName, Paths.get(".").toAbsolutePath().toString());
            throw new NodeMgrException(ConstantCode.CHAIN_CERT_NOT_EXISTS_ERROR);
        }

        // build_chain.sh only support docker on linux
        String command = String.format("bash -x -e %s -c %s -a %s %s",
                // gen_agency_cert.sh shell script
                constant.getGenAgencyShell(),
                // chain cert dir
                certRoot.toAbsolutePath().toString(),
                // new agency name
                newAgencyName,
                encryptType == CryptoType.SM_TYPE ?
                        String.format(" -g %s", pathService.getGmCertRoot(chainName).toAbsolutePath().toString())
                        : ""
        );

        return JavaCommandExecutor.executeCommand(command, constant.getExecBuildChainTimeout());
    }


    /**
     *
     * @param encryptType
     * @param chainName
     * @param agencyName
     * @param newNodeRoot
     * @return
     */
    public ExecuteResult execGenNode(byte encryptType, String chainName, String agencyName, String newNodeRoot) {
        log.info("Exec execGenNode method for chainName:[{}], node:[{}:{}:{}]",
                chainName, encryptType, agencyName, newNodeRoot);

        Path agencyRoot = this.pathService.getAgencyRoot(chainName, agencyName);

        // build_chain.sh only support docker on linux
        String command = String.format("bash -x -e %s -c %s -o %s %s",
                // gen_node_cert.sh shell script
                constant.getGenNodeShell(),
                // agency cert root
                agencyRoot.toAbsolutePath().toString(),
                // new node dir
                newNodeRoot,
                encryptType == CryptoType.SM_TYPE ?
                        String.format(" -g %s", pathService.getGmAgencyRoot(chainName,agencyName).toAbsolutePath().toString()) : ""
        );

        return JavaCommandExecutor.executeCommand(command, constant.getExecBuildChainTimeout());
    }

    public ExecuteResult execExpandNode(byte encryptType, String chainName, String newNodeRoot, String configPath, DeployNodeInfo nodeInfo) {
        log.info("Exec execExpandNode method for chainName:[{}], node:[{}:{}], configPath:{}, nodeInfo:{}",
                chainName, encryptType, newNodeRoot, configPath, nodeInfo);

        String ports = nodeInfo.getP2pPort() + "," + nodeInfo.getRpcPort();

        Path caDir = pathService.getCaDir(chainName);
        log.info("execExpandNode chainRoot:{}", caDir.toAbsolutePath().toString());

        // build_chain.sh only support docker on linux
        String command = String.format("bash -x -e %s -C expand -c %s -d %s -o %s %s -p %s",
                // gen_node_cert.sh shell script
                constant.getBuildChainShell(),
                // config path(reuse exist config.geneisis and nodes.json)
                configPath,
                //ca dir
                caDir.toAbsolutePath().toString(),
                // new node dir
                newNodeRoot,
                encryptType == CryptoType.SM_TYPE ? "-s " : "",
                ports
        );

        return JavaCommandExecutor.executeCommand(command, constant.getExecBuildChainTimeout());
    }

}
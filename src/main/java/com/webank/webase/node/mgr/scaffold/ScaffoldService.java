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

package com.webank.webase.node.mgr.scaffold;

import com.webank.scaffold.artifact.webase.NewMainResourceDir.ContractInfo;
import com.webank.scaffold.factory.ProjectFactory;
import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.base.tools.NodeMgrTools;
import com.webank.webase.node.mgr.base.tools.ZipUtils;
import com.webank.webase.node.mgr.cert.CertService;
import com.webank.webase.node.mgr.contract.ContractService;
import com.webank.webase.node.mgr.contract.entity.TbContract;
import com.webank.webase.node.mgr.front.FrontService;
import com.webank.webase.node.mgr.front.entity.FrontNodeConfig;
import com.webank.webase.node.mgr.front.entity.TbFront;
import com.webank.webase.node.mgr.frontinterface.FrontInterfaceService;
import com.webank.webase.node.mgr.scaffold.entity.ReqProject;
import com.webank.webase.node.mgr.scaffold.entity.RspFile;
import com.webank.webase.node.mgr.user.UserService;
import java.io.File;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.fisco.bcos.sdk.model.CryptoType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * use webank-blockchain-smart-dev-scaffold
 * to generate demo project of contract
 * @author marsli
 */
@Slf4j
@Service
public class ScaffoldService {
    @Autowired
    private ContractService contractService;
    @Autowired
    private CryptoSuite cryptoSuite;
    @Autowired
    private CertService certService;
    @Autowired
    private FrontService frontService;
    @Autowired
    private UserService userService;
    @Autowired
    private FrontInterfaceService frontInterfaceService;

    private static final String GRADLE_WRAPPER_DIR = "gradle";

    private static final String OUTPUT_DIR = "output";
    private static final String ZIP_SUFFIX = ".zip";
    private static final String OUTPUT_ZIP_DIR = OUTPUT_DIR + File.separator + "zip";

    /**
     * generate by contract with sol
      */
    public RspFile exportProject(ReqProject reqProject) {
        String artifactName = reqProject.getArtifactName();
        // check dir exist
        File checkProjectDir = new File(OUTPUT_DIR + File.separator + artifactName);
        if (checkProjectDir.exists()) {
            boolean result = checkProjectDir.delete();
            log.warn("exportProject dir exist: {}, now delete it result:{}", artifactName, result);
        }
        // get contract info list
        List<Integer> contractIdList = reqProject.getContractIdList();
        List<TbContract> tbContractList = new ArrayList<>();
        for (Integer id : contractIdList) {
            TbContract contract = contractService.queryByContractId(id);
            if (contract == null || StringUtils.isBlank(contract.getBytecodeBin())) {
                log.error("exportProject contract not exist or not compiled, id:{}", id);
                throw new NodeMgrException(ConstantCode.INVALID_CONTRACT_ID);
            }
            tbContractList.add(contract);
        }
        // get from front
        TbFront front = frontService.getById(reqProject.getFrontId());
        if (front == null) {
            log.error("exportProject front not exist:{}", reqProject.getFrontId());
            throw new NodeMgrException(ConstantCode.INVALID_FRONT_ID);
        }
        // get front's p2p ip and channel port
        FrontNodeConfig frontNodeConfig = frontInterfaceService
            .getNodeConfigFromSpecificFront(front.getFrontIp(), front.getFrontPort());
        frontNodeConfig.setP2pip(reqProject.getChannelIp());
        log.info("exportProject get frontNodeConfig:{}", frontNodeConfig);
        // get front's sdk key cert
        Map<String, String> sdkMap = certService.getFrontSdkContent(front.getFrontId());
        log.info("exportProject get sdkMap size:{}", sdkMap.size());
        // get user private key if set
        List<String> userAddressList = reqProject.getUserAddressList();
        String hexPrivateKeyListStr = "";
        if (userAddressList != null && !userAddressList.isEmpty()) {
            // hexPrivateKeyListStr = this.handleUserList(reqProject.getGroupId(), userAddressList);
            hexPrivateKeyListStr = userService.queryUserDetail(reqProject.getGroupId(), userAddressList.get(0));
        }
        // generate
        String projectPath = this.generateProject(frontNodeConfig, reqProject.getGroup(), reqProject.getArtifactName(),
            tbContractList, reqProject.getGroupId(), hexPrivateKeyListStr, sdkMap);
        String zipFileName = artifactName + ZIP_SUFFIX;
        try {
            ZipUtils.generateZipFile(projectPath, OUTPUT_ZIP_DIR, "", zipFileName);
        } catch (Exception e) {
            log.error("exportProject generateZipFile failed:[]", e);
            // if failed, delete project dir
            boolean result = checkProjectDir.delete();
            log.error("zip failed, now delete project dir, result:{}", result);
        }
        String zipFileFullPath = OUTPUT_ZIP_DIR + File.separator + zipFileName;
        log.info("exportProject zipFileName:{}, zipFileFullPath{}", zipFileName, zipFileFullPath);
        RspFile rspFile = new RspFile();
        rspFile.setFileName(zipFileName);
        rspFile.setFileStreamBase64(NodeMgrTools.fileToBase64(zipFileFullPath));
        return rspFile;
    }

    /**
     * generate project
     * @param nodeConfig ip channel port
     * @param projectGroup
     * @param artifactName
     * @param tbContractList
     * @param groupId
     * @param hexPrivateKeyListStr
     * @param sdkMap
     * @return path string of project
     */
    public String generateProject(FrontNodeConfig nodeConfig, String projectGroup, String artifactName,
        List<TbContract> tbContractList, int groupId, String hexPrivateKeyListStr, Map<String, String> sdkMap) {
        log.info("generateProject sdkMap size:{}", sdkMap.size());
        List<ContractInfo> contractInfoList = this.handleContractList(tbContractList);
        String frontChannelIpPort = nodeConfig.getP2pip() + ":" + nodeConfig.getChannelPort();
        ProjectFactory projectFactory = new ProjectFactory();
        log.info("generateProject projectGroup:{},artifactName:{},OUTPUT_DIR:{},frontChannelIpPort:{},groupId:{}",
            projectGroup, artifactName, OUTPUT_DIR, frontChannelIpPort, groupId);
        try {
            projectFactory.buildProjectDir(contractInfoList,
                projectGroup, artifactName, OUTPUT_DIR, GRADLE_WRAPPER_DIR,
                frontChannelIpPort, groupId, hexPrivateKeyListStr, sdkMap);
        } catch (Exception e) {
            log.error("generateProject error:[]", e);
            throw new NodeMgrException(ConstantCode.GENERATE_CONTRACT_PROJECT_FAIL.attach(e.getMessage()));
        }
        String projectDir = OUTPUT_DIR + File.separator + artifactName;
        log.info("generateProject result:{}", projectDir);
        return projectDir;
    }

    private List<ContractInfo> handleContractList(List<TbContract> contractList) {
        log.info("handleContractList contractList:{}", contractList);
        List<ContractInfo> contractInfoList = new ArrayList<>();
        log.info("handleContractList param contractList size:{}", contractList.size());
        for (TbContract contract : contractList) {
            String sourceCodeBase64 = contract.getContractSource();
            String solSourceCode = new String(Base64.getDecoder().decode(sourceCodeBase64));
            String contractName = contract.getContractName();
            String contractAddress = contract.getContractAddress();
            String contractAbi = contract.getContractAbi();
            String bytecodeBin = contract.getBytecodeBin();

            ContractInfo contractInfo = new ContractInfo();
            contractInfo.setSolRawString(solSourceCode);
            contractInfo.setContractName(contractName);
            contractInfo.setContractAddress(contractAddress);
            contractInfo.setAbiStr(contractAbi);
            if (cryptoSuite.getCryptoTypeConfig() == CryptoType.SM_TYPE) {
                contractInfo.setSmBinStr(bytecodeBin);
            } else {
                contractInfo.setBinStr(bytecodeBin);
            }
            contractInfoList.add(contractInfo);
        }
        log.info("handleContractList result contractInfoList size:{}", contractInfoList.size());
        log.info("handleContractList contractList:{}", contractInfoList);
        return contractInfoList;
    }

    /**
     * userP12PathList
     * @return: String of list, e.g. 123,123,123
     */
    private String handleUserList(int groupId, List<String> userAddressList) {
        List<String> keyList = new ArrayList<>();
        for (String address : userAddressList) {
            String hexPrivateKey = "";
            if (StringUtils.isNotBlank(address)) {
                hexPrivateKey = userService.queryUserDetail(groupId, address);
            }
            log.info("exportProject get hexPrivateKey length:{}", hexPrivateKey.length());
            keyList.add(hexPrivateKey);
        }
        return StringUtils.join(keyList, ",");
    }
}

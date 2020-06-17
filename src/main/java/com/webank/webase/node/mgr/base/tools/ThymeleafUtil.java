/**
 * Copyright 2014-2020  the original author or authors.
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

package com.webank.webase.node.mgr.base.tools;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import com.webank.webase.node.mgr.node.entity.TbNode;


public class ThymeleafUtil {

    public static final String FRONT_APLLICATION_YML = "front-application-yml.tpl";
    public static final String NODE_CONFIG_INI = "node-config-ini.tpl";
    public static final String NODE_GROUP_GENESIS = "node-group-genesis.tpl";
    public static final String NODE_GROUP_INI = "node-group-ini.tpl";


    /**
     *
     */
    public final static TemplateEngine templateEngine = new TemplateEngine();

    static {
        final ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setOrder(1);
        templateResolver.setPrefix("/templates/node/");
        templateResolver.setTemplateMode(TemplateMode.TEXT);
        templateResolver.setCharacterEncoding("UTF-8");
        templateResolver.setCacheable(false);

        templateEngine.addTemplateResolver(templateResolver);
    }

    /**
     * @param tpl
     * @param varMap
     */
    public static String generate(String tpl, Map<String, Object> varMap) {
        final Context ctx = new Context(Locale.CHINA);
        ctx.setVariables(varMap);
        return templateEngine.process(tpl, ctx);
    }

    /**
     * @param tpl
     * @param array
     */
    public static String generate(String tpl, Pair<String, Object> ... array) {
        return generate(tpl, PairUtil.toMap(array));
    }

    /**
     * @param tpl
     * @param varMap
     */
    public static void write(String tpl, Writer writer, Map<String, Object> varMap) {
        final Context ctx = new Context(Locale.CHINA);
        ctx.setVariables(varMap);
        templateEngine.process(tpl, ctx, writer);
    }

    /**
     * @param tpl
     * @param array
     * @return
     */
    public static void write(String tpl, Writer writer, Pair<String, Object>... array) {
        write(tpl, writer, PairUtil.toMap(array));
    }

    /**
     * node config path: /NODES_ROOT/IP/NODE[index]/
     * @param nodeRoot
     * @param channelPort
     * @param p2pPort
     * @param jsonrpcPort
     * @param peerList
     * @param guomi
     * @param chainIdInConfigIni
     * @throws IOException
     */
    public static void newNodeConfigIni(Path nodeRoot, int channelPort, int p2pPort,
                                        int jsonrpcPort, List<TbNode> peerList, boolean guomi,
                                        int chainIdInConfigIni) throws IOException {
        String nodeConfigIni = ThymeleafUtil.generate(ThymeleafUtil.NODE_CONFIG_INI,
                Pair.of("channelPort", channelPort), Pair.of("p2pPort", p2pPort),
                Pair.of("jsonrpcPort", jsonrpcPort), Pair.of("nodeList", peerList),
                Pair.of("guomi", guomi), Pair.of("chainId", chainIdInConfigIni));

        if (Files.notExists(nodeRoot)){
            Files.createDirectories(nodeRoot);
        }

        Files.write(nodeRoot.resolve("config.ini"), nodeConfigIni.getBytes(), StandardOpenOption.CREATE);
    }

    /**
     * group config path: /NODES_ROOT/IP/NODE[index]/conf/
     * @param nodeRoot
     * @param groupId
     * @throws IOException
     */
    public static void newGroupConfigs(Path nodeRoot, int groupId,
                                       long timestamp, List<String> nodeIdList) throws IOException {
        String nodeConfigIni = ThymeleafUtil.generate(ThymeleafUtil.NODE_GROUP_INI);
        Files.write(nodeRoot.resolve(String.format("conf/group.%s.ini",groupId)), nodeConfigIni.getBytes(), StandardOpenOption.CREATE);

        String nodeGenesis = ThymeleafUtil.generate(ThymeleafUtil.NODE_GROUP_GENESIS,
                Pair.of("groupId",groupId),Pair.of("timestamp",timestamp),
                Pair.of("nodeIdList",nodeIdList),Pair.of("sealerCount", CollectionUtils.size(nodeIdList)));
        Files.write(nodeRoot.resolve(String.format("conf/group.%s.genesis",groupId)), nodeGenesis.getBytes(), StandardOpenOption.CREATE);
    }


    /**
     *
     * @param nodeRoot
     * @param encryptType
     * @param channelPort
     * @param frontPort
     * @param webaseSignAddr
     * @throws IOException
     */
    public static void newFrontConfig(Path nodeRoot, byte encryptType, int channelPort,
                                       int frontPort, String webaseSignAddr) throws IOException {
        String applicationYml = ThymeleafUtil.generate(
                ThymeleafUtil.FRONT_APLLICATION_YML,
                Pair.of("encryptType", encryptType),
                Pair.of("channelPort", channelPort),
                Pair.of("frontPort", frontPort),
                Pair.of("webaseSignAddr", webaseSignAddr)
        );
        Files.write(nodeRoot.resolve("application.yml"), applicationYml.getBytes(), StandardOpenOption.CREATE);
    }
}


package node.mgr.test.appintegrate;

import com.webank.webase.app.sdk.client.AppClient;
import com.webank.webase.app.sdk.config.HttpConfig;
import com.webank.webase.app.sdk.dto.req.ReqAppRegister;
import com.webank.webase.node.mgr.tools.JsonTools;

public class ClientTest {

    // WeBASE-Node-Manager的url
    private static String appIp = "127.0.0.1";
    private static String appPort = "5031";
    private static String appLink = "https://" + appIp + ":" + appPort;
    private static String url = "http://" + appIp + ":" + appPort;
//    private static String appKey = "RutWhRCq";
//    private static String appSecret = "WUazkxkKgzaDeDtcVuVgRWG7EqqFjyWV";
    private static String appKey = "Fm4JSQNK";
    private static String appSecret = "P7a4YGPWSa8iv9xJiCmcQYTa2mFUQNpg";
    private static boolean isTransferEncrypt = true;

    private static AppClient appClient = null;

    public static void main(String[] args) {
        try {
            initClient();
            appRegister();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    public static void initClient() {
        // 未设置httpConfig时，默认http连接均为30s
        HttpConfig httpConfig = new HttpConfig(30, 30, 30);
        appClient = new AppClient(url, appKey, appSecret, isTransferEncrypt, httpConfig);
        System.out.println("testInitClient:" + JsonTools.objToString(appClient));
    }

    public static void appRegister() throws Exception {
        try {
            ReqAppRegister req = new ReqAppRegister();
            req.setAppIp(appIp);
            req.setAppPort(Integer.parseInt(appPort));
            req.setAppLink(appLink);
            appClient.appRegister(req);
            System.out.println("appRegister end.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
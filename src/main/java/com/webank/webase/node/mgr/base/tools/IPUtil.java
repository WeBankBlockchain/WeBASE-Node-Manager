package com.webank.webase.node.mgr.base.tools;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * IPUtil.
 */

@Slf4j
public class IPUtil {

    public final static Set<String> LOCAL_IP_SET = new HashSet<>();
    public final static String LOCAL_IP_127 = "127.0.0.1";
    public final static String LOCAL_IP_host = "localhost";

    static {
        LOCAL_IP_SET.add(LOCAL_IP_127);
        LOCAL_IP_SET.add(LOCAL_IP_host);
        LOCAL_IP_SET.addAll(getLocalIPSet());
    }


    /**
     * Check ip is local.
     *
     * @param ip
     * @return
     */
    public static boolean isLocal(String ip) {
        return LOCAL_IP_SET.stream().anyMatch(ip::equalsIgnoreCase);
    }


    public static Set<String> getLocalIPSet() {
        Set<String> result = new HashSet<>();
        try {
            Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
            for (NetworkInterface netint : Collections.list(nets)) {
                Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
                for (InetAddress inetAddress : Collections.list(inetAddresses)) {
                    String ip = inetAddress.getHostAddress();
                    if (inetAddress instanceof Inet4Address) {
                        result.add(ip);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Get ip address from network interface error.", e);
        }
        return result;
    }

    public static String getLocalIp() {
        try {
            if (isWindowsOS()) {
                return InetAddress.getLocalHost().getHostAddress();
            } else {
                return getLinuxLocalIp();
            }
        } catch (Exception e) {
            log.error("getLocalIp error.", e);
        }
        return LOCAL_IP_127;
    }

    public static boolean isWindowsOS() {
        boolean isWindowsOS = false;
        String osName = System.getProperty("os.name");
        if (osName.toLowerCase().indexOf("windows") > -1) {
            isWindowsOS = true;
        }
        return isWindowsOS;
    }

    public static String getLinuxLocalIp() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en
                    .hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                String name = intf.getName();
                if (!name.contains("docker") && !name.contains("lo")) {
                    for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr
                            .hasMoreElements();) {
                        InetAddress inetAddress = enumIpAddr.nextElement();
                        if (!inetAddress.isLoopbackAddress() && !inetAddress.isLinkLocalAddress()
                                && !inetAddress.isSiteLocalAddress()
                                && inetAddress instanceof Inet4Address) {
                            return inetAddress.getHostAddress();
                        }
                    }
                }
            }
        } catch (SocketException e) {
            log.error("getLinuxLocalIp error.", e);
        }
        return LOCAL_IP_127;
    }

    public static String getIpAdrress(HttpServletRequest request) {
        String ip = null;

        // X-Forwarded-For：Squid 服务代理
        String ipAddresses = request.getHeader("X-Forwarded-For");
        String unknown = "unknown";
        if (ipAddresses == null || ipAddresses.length() == 0
                || unknown.equalsIgnoreCase(ipAddresses)) {
            // Proxy-Client-IP：apache 服务代理
            ipAddresses = request.getHeader("Proxy-Client-IP");
        }

        if (ipAddresses == null || ipAddresses.length() == 0
                || unknown.equalsIgnoreCase(ipAddresses)) {
            // WL-Proxy-Client-IP：weblogic 服务代理
            ipAddresses = request.getHeader("WL-Proxy-Client-IP");
        }

        if (ipAddresses == null || ipAddresses.length() == 0
                || unknown.equalsIgnoreCase(ipAddresses)) {
            // HTTP_CLIENT_IP：有些代理服务器
            ipAddresses = request.getHeader("HTTP_CLIENT_IP");
        }

        if (ipAddresses == null || ipAddresses.length() == 0
                || unknown.equalsIgnoreCase(ipAddresses)) {
            // X-Real-IP：nginx服务代理
            ipAddresses = request.getHeader("X-Real-IP");
        }

        // 有些网络通过多层代理，那么获取到的ip就会有多个，一般都是通过逗号（,）分割开来，并且第一个ip为客户端的真实IP
        if (ipAddresses != null && ipAddresses.length() != 0) {
            ip = ipAddresses.split(",")[0];
        }

        // 还是不能获取到，最后再通过request.getRemoteAddr();获取
        if (ip == null || ip.length() == 0 || unknown.equalsIgnoreCase(ipAddresses)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}

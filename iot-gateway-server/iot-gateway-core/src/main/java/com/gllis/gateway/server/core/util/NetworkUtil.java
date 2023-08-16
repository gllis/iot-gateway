package com.gllis.gateway.server.core.util;


import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.regex.Pattern;

/**
 * 网络工具
 *
 * @author glli
 * @date 2023/8/14
 */
@Slf4j
public final class NetworkUtil {
	

    private static String LOCAL_IP;

    private static final Pattern LOCAL_IP_PATTERN = Pattern.compile("127(\\.\\d{1,3}){3}$");

    private static String EXTRANET_IP;


    public static boolean isLocalHost(String host) {
        return host == null
                || host.length() == 0
                || host.equalsIgnoreCase("localhost")
                || host.equals("0.0.0.0")
                || (LOCAL_IP_PATTERN.matcher(host).matches());
    }

    public static String lookupLocalIp() {
        if (LOCAL_IP == null) {
            LOCAL_IP = getInetAddress(true);
        }
        return LOCAL_IP;
    }

    public static NetworkInterface getLocalNetworkInterface() {
        Enumeration<NetworkInterface> interfaces;
        try {
            interfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            throw new RuntimeException("NetworkInterface not found", e);
        }
        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = interfaces.nextElement();
            Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress address = addresses.nextElement();
                if (address.isLoopbackAddress()) {
                	continue;
                }
                if (address.getHostAddress().contains(":")) {
                	continue;
                }
                if (address.isSiteLocalAddress()) {
                	return networkInterface;
                }
            }
        }
        throw new RuntimeException("NetworkInterface not found");
    }

    public static InetAddress getInetAddress(String host) {
        try {
            return InetAddress.getByName(host);
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("UnknownHost " + host, e);
        }
    }

    /**
     * 只获取第一块网卡绑定的ip地址
     *
     * @param getLocal 局域网IP
     * @return ip
     */
    public static String getInetAddress(boolean getLocal) {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                Enumeration<InetAddress> addresses = interfaces.nextElement().getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    if (address.isLoopbackAddress()) {
                    	continue;
                    }
                    if (address.getHostAddress().contains(":")) {
                    	continue;
                    }
                    if (getLocal) {
                        if (address.isSiteLocalAddress()) {
                            return address.getHostAddress();
                        }
                    } else {
                        if (!address.isSiteLocalAddress() && !address.isLoopbackAddress()) {
                            return address.getHostAddress();
                        }
                    }
                }
            }
            log.debug("getInetAddress is null, getLocal={}", getLocal);
            return getLocal ? "127.0.0.1" : null;
        } catch (Throwable e) {
            log.error("getInetAddress exception", e);
            return getLocal ? "127.0.0.1" : null;
        }
    }

    public static String lookupExtranetIp() {
        if (EXTRANET_IP == null) {
            EXTRANET_IP = getInetAddress(false);
        }
        return EXTRANET_IP;
    }


}

package com.aircode.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;

import org.slf4j.Logger;

public class NetworkUtils {
    private static final String TYPE_NETWORK_ETHERNET = "eth0";
    private static final String TYPE_NETWORK_WIFI = "wlan0";
    private static final Logger logger =  LogManager.getInstance().getLogger(NetworkUtils.class);
    /**
     * eth0, wlan0 순으로.
     * */

    public static NetworkInterface getNetworkInterface() throws SocketException {
        Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
        for (NetworkInterface netInterface : Collections.list(nets)){
            String netDisplayName = netInterface.getDisplayName();
            logger.debug("netDisplayName : " + netDisplayName);
            if (netInterface.getDisplayName().contains(TYPE_NETWORK_ETHERNET)|| netInterface.getName().contains(TYPE_NETWORK_ETHERNET)) {
                return netInterface;
            }
            if (netInterface.getDisplayName().contains(TYPE_NETWORK_WIFI)|| netInterface.getName().contains(TYPE_NETWORK_WIFI)) {
                return netInterface;
            }
        }

        return null;
    }

    public static NetworkInterface getNetworkInterfaceByIp() throws SocketException {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    if (!inetAddress.isLoopbackAddress() && !inetAddress.isLinkLocalAddress()
                            && inetAddress.isSiteLocalAddress()) {
                                logger.debug("Interface Name: " + networkInterface.getDisplayName());
                                //logger.debug("This is a Wi-Fi interface.");
                                return networkInterface;
                    }
                }
            }
        } catch (SocketException e) {}

        return null;
    }
}

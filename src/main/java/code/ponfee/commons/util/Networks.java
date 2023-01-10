package code.ponfee.commons.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

/**
 * 网络工具类
 * <pre>
 *  isAnyLocalAddress  通配符地址        IPv4的通配符地址是0.0.0.0
 *  isLoopbackAddress  回环地址          IPv4的的范围是127.0.0.0 ~ 127.255.255.255    IPv6的是0:0:0:0:0:0:0:1,也可以简写成::1
 *  isLinkLocalAddress 本地连接地址       IPv4的的范围是169.254.0.0 ~ 169.254.255.255  IPv6的前12位是FE8，其他的位可以是任意取值
 *  isSiteLocalAddress 地区本地地址       IPv4的分为三段:10.0.0.0 ~ 10.255.255.255等   IPv6的地区本地地址的前12位是FEC，其他的位可以是任意取值
 *  isMulticastAddress 广播地址          IPv4的范围是224.0.0.0 ~ 239.255.255.255     IPv6的第一个字节是FF，其他的字节可以是任意值
 *  isMCGlobal         全球范围的广播地址
 *  isMCLinkLocal      子网广播地址
 *  isMCNodeLocal      本地接口广播地址
 *  isMCOrgLocal       组织范围的广播地址
 *  isMCSiteLocal      站点范围的广播地址
 * </pre>
 *
 * @author Ponfee
 */
public final class Networks {

    private static final Logger LOG = LoggerFactory.getLogger(Networks.class);

    private static final String LOCALHOST_IP = "127.0.0.1";
    private static final String LOCALHOST_NAME = "localhost";
    private static final String EMPTY_IP = "0.0.0.0";

    /**
     * the max ip value
     * <p>toLong("255.255.255.255")
     */
    public static final long MAX_IP_VALUE = (1L << 32) - 1;

    /**
     * 掩码
     */
    private static final long[] MASK = {0xFF000000, 0x00FF0000, 0x0000FF00, 0x000000FF};

    /**
     * local ip
     */
    public static final String HOST_IP = getHostIp();

    /**
     * getMachineNetworkFlag 获取机器的MAC或者IP，优先获取MAC
     *
     * @param ia InetAddress
     * @return mac or ip
     */
    public static String getMacOrIp(InetAddress ia) {
        if (ia == null) {
            try {
                ia = InetAddress.getLocalHost();
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }
        }
        String mac = getMacAddress(ia);
        return StringUtils.isBlank(mac) ? getIpAddress(ia) : mac;
    }

    /**
     * 获取指定地址的mac地址，不指定默认取本机的mac地址
     *
     * @param ia InetAddress
     * @return mac or ip
     */
    public static String getMacAddress(InetAddress ia) {
        byte[] mac;
        try {
            if (ia == null) {
                ia = InetAddress.getLocalHost();
            }
            mac = NetworkInterface.getByInetAddress(ia).getHardwareAddress();
        } catch (SocketException | UnknownHostException e) {
            throw new RuntimeException(e);
        }

        if (mac == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder(17);
        for (int i = 0; i < mac.length; i++) {
            if (i != 0) {
                sb.append("-");
            }
            sb.append(Bytes.hexEncode(mac[i], false));
        }
        return sb.toString();
    }

    public static String getHostIp() {
        InetAddress address = getHostAddress();
        return address == null ? LOCALHOST_IP : address.getHostAddress();
    }

    public static String getHostName() {
        InetAddress address = getHostAddress();
        return address == null ? LOCALHOST_NAME : address.getHostName();
    }


    /**
     * Check the port is available
     *
     * @param port 待测试端口
     * @return if @{code true} is available, else unavailable
     */
    public static boolean isAvailablePort(int port) {
        try (ServerSocket ss = new ServerSocket(port)) {
            ss.bind(null);
            return true;
        } catch (IOException ignored) {
            return false;
        }
    }

    /**
     * Returns this server available port
     *
     * @param startPort
     * @return if -1 then not find available port
     * else returns available port
     */
    public static int findAvailablePort(int startPort) {
        if (startPort < 0 || startPort > 65535) {
            return -1;
        }

        for (int port = startPort; port <= 65535; port++) {
            if (isAvailablePort(port)) {
                return port;
            }
        }

        for (int port = startPort - 1; port >= 0; port--) {
            if (isAvailablePort(port)) {
                return port;
            }
        }

        return -1;
    }

    /**
     * Convert ipv4 to long，max value is 4294967295
     *
     * @param ip the ip address
     * @return
     */
    public static long toLong(String ip) {
        if (!RegexUtils.isIpv4(ip)) {
            throw new IllegalArgumentException("invalid ip address[" + ip + "]");
        }
        String[] ipNums = ip.split("\\.", 4);
        return (Long.parseLong(ipNums[0]) << 24)
             + (Long.parseLong(ipNums[1]) << 16)
             + (Long.parseLong(ipNums[2]) <<  8)
             + (Long.parseLong(ipNums[3])      );
    }

    /**
     * Convert long value to ipv4 address string
     *
     * @param ip
     * @return
     */
    public static String fromLong(long ip) {
        return new StringBuilder(15)
            .append((ip & MASK[0]) >> 24).append('.')
            .append((ip & MASK[1]) >> 16).append('.')
            .append((ip & MASK[2]) >>  8).append('.')
            .append((ip & MASK[3])      ).toString();
    }

    /**
     * 获取指定地址的ip地址，不指定默认取本机的ip地址
     *
     * @param ia InetAddress
     * @return mac or ip
     */
    private static String getIpAddress(InetAddress ia) {
        return ia.getHostAddress();
    }

    private static InetAddress getHostAddress() {
        InetAddress localAddress = null;
        try {
            localAddress = InetAddress.getLocalHost();
            if (isValidHostAddress(localAddress)) {
                return localAddress;
            }
        } catch (Exception e) {
            LOG.warn("Failed to get local host address. cause: {} ", e.getMessage());
        }

        try {
            Enumeration<NetworkInterface> inters = NetworkInterface.getNetworkInterfaces();
            if (inters == null) {
                return localAddress;
            }
            while (inters.hasMoreElements()) {
                try {
                    Enumeration<InetAddress> addresses = inters.nextElement().getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        try {
                            InetAddress address = addresses.nextElement();
                            if (isValidHostAddress(address)) {
                                return address;
                            }
                        } catch (Exception e) {
                            LOG.warn("Failed to get host address. cause: {}", e.getMessage());
                        }
                    }
                } catch (Exception e) {
                    LOG.warn("Failed to get network address. cause: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            LOG.warn("Failed to get network interface. cause: {}", e.getMessage());
        }

        LOG.error("Could not get host ip address, will use 127.0.0.1 instead.");
        return localAddress;
    }

    /**
     * Returns the host address is valid
     *
     * @param address
     * @return if @code true} is valid, else invalid
     */
    private static boolean isValidHostAddress(InetAddress address) {
        if (address == null || address.isLoopbackAddress()) {
            return false;
        }
        String ip = address.getHostAddress();
        return ip != null
            && !EMPTY_IP.equals(ip)
            && !LOCALHOST_IP.equals(ip)
            && RegexUtils.isIpv4(ip);
    }
}

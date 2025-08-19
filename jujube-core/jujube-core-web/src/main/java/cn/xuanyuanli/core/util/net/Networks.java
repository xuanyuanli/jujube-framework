package cn.xuanyuanli.core.util.net;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

/**
 * 网络相关工具类
 *
 * @author John Li
 * @date 2021/09/01
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Networks {

    /**
     * 获取本地MAC地址的方法
     *
     * @return {@link String}
     */
    public static String getLocalMacAddress() {
        try {
            // 获取本地IP对象
            InetAddress ia = InetAddress.getLocalHost();
            // 获得网络接口对象（即网卡），并得到mac地址，mac地址存在于一个byte数组中。
            byte[] mac = NetworkInterface.getByInetAddress(ia).getHardwareAddress();

            // 下面代码是把mac地址拼装成String
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mac.length; i++) {
                if (i != 0) {
                    sb.append("-");
                }
                // mac[i] & 0xFF 是为了把byte转化为正整数
                String s = Integer.toHexString(mac[i] & 0xFF);
                sb.append(s.length() == 1 ? 0 + s : s);
            }
            // 把字符串所有小写字母改为大写成为正规的mac地址并返回
            return sb.toString().toUpperCase();
        } catch (Exception e) {
            return "TT-TT-TT-TT";
        }
    }

    /**
     * 获得主机名
     *
     * @return {@link String}
     */
    public static String getHostName() {
        try {
            return (InetAddress.getLocalHost()).getHostName();
        } catch (UnknownHostException uhe) {
            // host = "hostname: hostname"
            String host = uhe.getMessage();
            if (host != null) {
                int colon = host.indexOf(':');
                if (colon > 0) {
                    return host.substring(0, colon);
                }
            }
            return "UnknownHost";
        }
    }

    /**
     * 获取主机ip
     *
     * @return {@link List }<{@link String }>
     */
    @SneakyThrows
    public static List<String> getHostIp() {
        List<String> list = new ArrayList<>();
        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface networkInterface = networkInterfaces.nextElement();
            Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
            while (inetAddresses.hasMoreElements()) {
                InetAddress inetAddress = inetAddresses.nextElement();
                if (!inetAddress.isAnyLocalAddress() && !inetAddress.isLoopbackAddress() && inetAddress.isSiteLocalAddress()) {
                    list.add(inetAddress.getHostAddress());
                }
            }
        }
        return list;
    }

    /**
     * 获取第一个主机ip
     *
     * @return {@link String }
     */
    public static String getFirstHostIp() {
        List<String> hostIps = getHostIp();
        return hostIps.isEmpty() ? null : hostIps.get(0);
    }

    /**
     * 端口是否可用
     *
     * @param port 港口
     * @return boolean
     */
    public static boolean isPortAvailable(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            serverSocket.setReuseAddress(true);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 判断ip是否是本地IP
     *
     * @param qip qip
     * @return boolean
     */
    public static boolean isLocalIp(String qip) {
        try {
            Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
            InetAddress ip;
            while (allNetInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = allNetInterfaces.nextElement();
                if (!netInterface.isUp() || netInterface.isLoopback() || netInterface.isVirtual()) {
                    continue;
                }
                Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    ip = addresses.nextElement();
                    if (ip instanceof Inet4Address && ip.getHostAddress().equals(qip)) {
                        return true;
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }
}

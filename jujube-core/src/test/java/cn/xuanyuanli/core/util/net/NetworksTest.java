package cn.xuanyuanli.core.util.net;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class NetworksTest {

    @Test
    void testGetLocalMacAddress() {
        String result = Networks.getLocalMacAddress();
        Assertions.assertThat(result).hasSize(17);
    }

    @Test
    void testGetLocalMacAddressException() {
        try (MockedStatic<InetAddress> mockedInetAddress = Mockito.mockStatic(InetAddress.class)) {
            mockedInetAddress.when(InetAddress::getLocalHost).thenThrow(new UnknownHostException());

            String result = Networks.getLocalMacAddress();
            Assertions.assertThat(result).isEqualTo("TT-TT-TT-TT");
        }
    }

    @Test
    void testGetLocalMacAddressWithSingleDigitHex() throws Exception {
        InetAddress mockInetAddress = mock(InetAddress.class);
        NetworkInterface mockNetworkInterface = mock(NetworkInterface.class);
        byte[] mac = new byte[]{0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F};

        when(mockNetworkInterface.getHardwareAddress()).thenReturn(mac);
        try (MockedStatic<InetAddress> mockedInetAddress = Mockito.mockStatic(
                InetAddress.class); MockedStatic<NetworkInterface> mockedNetworkInterface = Mockito.mockStatic(NetworkInterface.class)) {
            mockedInetAddress.when(InetAddress::getLocalHost).thenReturn(mockInetAddress);
            mockedNetworkInterface.when(() -> NetworkInterface.getByInetAddress(mockInetAddress)).thenReturn(mockNetworkInterface);

            String result = Networks.getLocalMacAddress();
            Assertions.assertThat(result).isEqualTo("0A-0B-0C-0D-0E-0F");
        }
    }

    @Test
    void testGetHostName() {
        String result = Networks.getHostName();
        Assertions.assertThat(result).isNotEmpty();
    }

    @Test
    void getFirstHostIp() {
        String result = Networks.getFirstHostIp();
        Assertions.assertThat(result).isNotEmpty();
    }

    @Test
    void testIsPortAvailable() {
        boolean result = Networks.isPortAvailable(0);
        Assertions.assertThat(result).isTrue();
    }

    @Test
    void testIsLocalIp() {
        boolean result = Networks.isLocalIp("localhost");
        Assertions.assertThat(result).isFalse();
    }

    @Test
    void testIsLocalIpException() {
        try (MockedStatic<NetworkInterface> mockedNetworkInterface = Mockito.mockStatic(NetworkInterface.class)) {
            mockedNetworkInterface.when(NetworkInterface::getNetworkInterfaces).thenThrow(new SocketException());

            boolean result = Networks.isLocalIp("192.168.1.1");
            Assertions.assertThat(result).isFalse();
        }
    }

    @Test
    void testIsLocalIpWithVirtualInterface() throws Exception {
        NetworkInterface mockNetworkInterface = mock(NetworkInterface.class);
        when(mockNetworkInterface.isUp()).thenReturn(true);
        when(mockNetworkInterface.isLoopback()).thenReturn(false);
        when(mockNetworkInterface.isVirtual()).thenReturn(true); // 虚拟网卡

        Enumeration<NetworkInterface> mockEnumeration = mock(Enumeration.class);
        when(mockEnumeration.hasMoreElements()).thenReturn(true, false);
        when(mockEnumeration.nextElement()).thenReturn(mockNetworkInterface);

        try (MockedStatic<NetworkInterface> mockedNetworkInterface = Mockito.mockStatic(NetworkInterface.class)) {
            mockedNetworkInterface.when(NetworkInterface::getNetworkInterfaces).thenReturn(mockEnumeration);

            boolean result = Networks.isLocalIp("192.168.1.1");
            Assertions.assertThat(result).isFalse(); // 虚拟网卡应被跳过
        }
    }

    @Test
    void testIsLocalIpWithMatchingIp() throws Exception {
        NetworkInterface mockNetworkInterface = mock(NetworkInterface.class);
        when(mockNetworkInterface.isUp()).thenReturn(true);
        when(mockNetworkInterface.isLoopback()).thenReturn(false);
        when(mockNetworkInterface.isVirtual()).thenReturn(false);

        InetAddress mockInetAddress = mock(Inet4Address.class);
        when(mockInetAddress.getHostAddress()).thenReturn("192.168.1.1");

        Enumeration<InetAddress> mockAddresses = mock(Enumeration.class);
        when(mockAddresses.hasMoreElements()).thenReturn(true, false);
        when(mockAddresses.nextElement()).thenReturn(mockInetAddress);

        when(mockNetworkInterface.getInetAddresses()).thenReturn(mockAddresses);

        Enumeration<NetworkInterface> mockEnumeration = mock(Enumeration.class);
        when(mockEnumeration.hasMoreElements()).thenReturn(true, false);
        when(mockEnumeration.nextElement()).thenReturn(mockNetworkInterface);

        try (MockedStatic<NetworkInterface> mockedNetworkInterface = Mockito.mockStatic(NetworkInterface.class)) {
            mockedNetworkInterface.when(NetworkInterface::getNetworkInterfaces).thenReturn(mockEnumeration);

            boolean result = Networks.isLocalIp("192.168.1.1");
            Assertions.assertThat(result).isTrue(); // IP 地址匹配
        }
    }
}

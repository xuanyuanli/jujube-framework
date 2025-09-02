package cn.xuanyuanli.core.util.net;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

@DisplayName("Networks 测试")
class NetworksTest {

    @Nested
    @DisplayName("MAC地址获取测试")
    class MacAddressTests {

        @Test
        @DisplayName("getLocalMacAddress_应该返回17位MAC地址_当网络正常时")
        void getLocalMacAddress_shouldReturn17CharMacAddress_whenNetworkIsNormal() {
            // Act
            String result = Networks.getLocalMacAddress();

            // Assert
            assertThat(result).hasSize(17);
        }

        @Test
        @DisplayName("getLocalMacAddress_应该返回默认MAC地址_当发生UnknownHostException时")
        void getLocalMacAddress_shouldReturnDefaultMacAddress_whenUnknownHostExceptionOccurs() {
            // Arrange & Act
            try (MockedStatic<InetAddress> mockedInetAddress = Mockito.mockStatic(InetAddress.class)) {
                mockedInetAddress.when(InetAddress::getLocalHost).thenThrow(new UnknownHostException());

                String result = Networks.getLocalMacAddress();

                // Assert
                assertThat(result).isEqualTo("TT-TT-TT-TT");
            }
        }

        @Test
        @DisplayName("getLocalMacAddress_应该正确格式化单位数十六进制_当MAC地址包含单位数时")
        void getLocalMacAddress_shouldFormatSingleDigitHexCorrectly_whenMacAddressContainsSingleDigits() throws Exception {
            // Arrange
            InetAddress mockInetAddress = mock(InetAddress.class);
            NetworkInterface mockNetworkInterface = mock(NetworkInterface.class);
            byte[] mac = new byte[]{0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F};
            when(mockNetworkInterface.getHardwareAddress()).thenReturn(mac);

            // Act
            try (MockedStatic<InetAddress> mockedInetAddress = Mockito.mockStatic(InetAddress.class);
                 MockedStatic<NetworkInterface> mockedNetworkInterface = Mockito.mockStatic(NetworkInterface.class)) {
                mockedInetAddress.when(InetAddress::getLocalHost).thenReturn(mockInetAddress);
                mockedNetworkInterface.when(() -> NetworkInterface.getByInetAddress(mockInetAddress)).thenReturn(mockNetworkInterface);

                String result = Networks.getLocalMacAddress();

                // Assert
                assertThat(result).isEqualTo("0A-0B-0C-0D-0E-0F");
            }
        }
    }

    @Nested
    @DisplayName("主机信息获取测试")
    class HostInfoTests {

        @Test
        @DisplayName("getHostName_应该返回非空主机名_当调用时")
        void getHostName_shouldReturnNonEmptyHostName_whenCalled() {
            // Act
            String result = Networks.getHostName();

            // Assert
            assertThat(result).isNotEmpty();
        }

        @Test
        @DisplayName("getFirstHostIp_应该返回非空IP地址_当调用时")
        void getFirstHostIp_shouldReturnNonEmptyIpAddress_whenCalled() {
            // Act
            String result = Networks.getFirstHostIp();

            // Assert
            assertThat(result).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("端口可用性测试")
    class PortAvailabilityTests {

        @Test
        @DisplayName("isPortAvailable_应该返回true_当端口为0时")
        void isPortAvailable_shouldReturnTrue_whenPortIsZero() {
            // Act
            boolean result = Networks.isPortAvailable(0);

            // Assert
            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("本地IP判断测试")
    class LocalIpTests {

        @Test
        @DisplayName("isLocalIp_应该返回false_当IP为localhost时")
        void isLocalIp_shouldReturnFalse_whenIpIsLocalhost() {
            // Act
            boolean result = Networks.isLocalIp("localhost");

            // Assert
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("isLocalIp_应该返回false_当发生SocketException时")
        void isLocalIp_shouldReturnFalse_whenSocketExceptionOccurs() {
            // Arrange & Act
            try (MockedStatic<NetworkInterface> mockedNetworkInterface = Mockito.mockStatic(NetworkInterface.class)) {
                mockedNetworkInterface.when(NetworkInterface::getNetworkInterfaces).thenThrow(new SocketException());

                boolean result = Networks.isLocalIp("192.168.1.1");

                // Assert
                assertThat(result).isFalse();
            }
        }

        @Test
        @DisplayName("isLocalIp_应该返回false_当只有虚拟网卡时")
        void isLocalIp_shouldReturnFalse_whenOnlyVirtualInterfaceExists() throws Exception {
            // Arrange
            NetworkInterface mockNetworkInterface = mock(NetworkInterface.class);
            when(mockNetworkInterface.isUp()).thenReturn(true);
            when(mockNetworkInterface.isLoopback()).thenReturn(false);
            when(mockNetworkInterface.isVirtual()).thenReturn(true); // 虚拟网卡

            @SuppressWarnings("unchecked")
            Enumeration<NetworkInterface> mockEnumeration = mock(Enumeration.class);
            when(mockEnumeration.hasMoreElements()).thenReturn(true, false);
            when(mockEnumeration.nextElement()).thenReturn(mockNetworkInterface);

            // Act
            try (MockedStatic<NetworkInterface> mockedNetworkInterface = Mockito.mockStatic(NetworkInterface.class)) {
                mockedNetworkInterface.when(NetworkInterface::getNetworkInterfaces).thenReturn(mockEnumeration);

                boolean result = Networks.isLocalIp("192.168.1.1");

                // Assert
                assertThat(result).isFalse(); // 虚拟网卡应被跳过
            }
        }

        @Test
        @DisplayName("isLocalIp_应该返回true_当IP地址匹配本地网卡时")
        void isLocalIp_shouldReturnTrue_whenIpAddressMatchesLocalInterface() throws Exception {
            // Arrange
            NetworkInterface mockNetworkInterface = mock(NetworkInterface.class);
            when(mockNetworkInterface.isUp()).thenReturn(true);
            when(mockNetworkInterface.isLoopback()).thenReturn(false);
            when(mockNetworkInterface.isVirtual()).thenReturn(false);

            InetAddress mockInetAddress = mock(Inet4Address.class);
            when(mockInetAddress.getHostAddress()).thenReturn("192.168.1.1");

            @SuppressWarnings("unchecked")
            Enumeration<InetAddress> mockAddresses = mock(Enumeration.class);
            when(mockAddresses.hasMoreElements()).thenReturn(true, false);
            when(mockAddresses.nextElement()).thenReturn(mockInetAddress);
            when(mockNetworkInterface.getInetAddresses()).thenReturn(mockAddresses);

            @SuppressWarnings("unchecked")
            Enumeration<NetworkInterface> mockEnumeration = mock(Enumeration.class);
            when(mockEnumeration.hasMoreElements()).thenReturn(true, false);
            when(mockEnumeration.nextElement()).thenReturn(mockNetworkInterface);

            // Act
            try (MockedStatic<NetworkInterface> mockedNetworkInterface = Mockito.mockStatic(NetworkInterface.class)) {
                mockedNetworkInterface.when(NetworkInterface::getNetworkInterfaces).thenReturn(mockEnumeration);

                boolean result = Networks.isLocalIp("192.168.1.1");

                // Assert
                assertThat(result).isTrue(); // IP 地址匹配
            }
        }
    }
}

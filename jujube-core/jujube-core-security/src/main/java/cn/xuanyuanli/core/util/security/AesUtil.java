package cn.xuanyuanli.core.util.security;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.codec.binary.Base64;
import cn.xuanyuanli.core.util.Texts;

/**
 * AES对称加密工具类
 * <p>
 * 提供基于AES算法的数据加密和解密功能，使用CBC模式和NoPadding填充方式。
 * 该工具类采用固定的初始化向量(IV)，适用于对数据进行快速加密解密操作。
 * </p>
 * <p>
 * 加密流程：
 * 1. 使用提供的密钥进行AES加密
 * 2. 采用CBC模式和NoPadding填充
 * 3. 返回Base64编码的加密结果
 * </p>
 * <p>
 * 解密流程：
 * 1. 对Base64编码的数据进行解码
 * 2. 使用相同的密钥进行AES解密
 * 3. 返回解密后的原始字符串
 * </p>
 *
 * @author xuanyuanli
 * @date 2021/09/01
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AesUtil {

    /**
     * 初始化向量密钥
     * <p>用于AES加密算法的初始化向量，保证加密的随机性和安全性</p>
     */
    private static final String IVKEY = "AESAPPCLIENT_KEY";

    /**
     * 使用AES算法对数据进行加密
     * <p>
     * 采用AES/CBC/NoPadding模式进行加密，使用固定的初始化向量。
     * 加密后的数据会进行Base64编码以便传输和存储。
     * </p>
     *
     * @param data 待加密的原始数据字符串，不能为null
     * @param key  加密密钥字符串，不能为null，长度会根据AES块大小自动调整
     * @return 加密后的Base64编码字符串
     * @throws RuntimeException 当加密过程中发生异常时抛出，包装了底层的加密异常
     */
    public static String encrypt(String data, String key) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            int blockSize = cipher.getBlockSize();
            SecretKeySpec keyspec = new SecretKeySpec(fullZore(key, blockSize), "AES");
            IvParameterSpec ivspec = new IvParameterSpec(fullZore(IVKEY, blockSize));
            cipher.init(Cipher.ENCRYPT_MODE, keyspec, ivspec);
            byte[] encrypted = cipher.doFinal(fullZore(data, blockSize));
            return Base64.encodeBase64String(encrypted);
        } catch (Exception e) {
            throw new RuntimeException(Texts.format("aes encrypt error. data:{},key:{}", data, key), e);
        }
    }

    /**
     * 使用AES算法对数据进行解密
     * <p>
     * 对Base64编码的加密数据进行解密，采用与加密时相同的AES/CBC/NoPadding模式。
     * 解密失败时会返回空字符串而不抛出异常，保证程序的健壮性。
     * </p>
     *
     * @param data 待解密的Base64编码字符串，应该是由encrypt方法产生的加密数据
     * @param key  解密密钥字符串，必须与加密时使用的密钥相同
     * @return 解密后的原始字符串，解密失败时返回空字符串
     */
    public static String decrypt(String data, String key) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            int blockSize = cipher.getBlockSize();
            SecretKeySpec keyspec = new SecretKeySpec(fullZore(key, blockSize), "AES");
            IvParameterSpec ivspec = new IvParameterSpec(fullZore(IVKEY, blockSize));
            cipher.init(Cipher.DECRYPT_MODE, keyspec, ivspec);
            byte[] decrypted = cipher.doFinal(Base64.decodeBase64(data));
            return new String(decrypted).trim();
        } catch (Exception ignored) {
        }
        return "";
    }

    /**
     * 数据填充方法
     * <p>
     * 将输入数据按照指定的块大小进行填充，确保数据长度是块大小的整数倍。
     * 这是因为AES算法要求输入数据的长度必须是16字节（128位）的倍数。
     * 填充方式采用零填充，即在原数据后面补零直到满足长度要求。
     * </p>
     *
     * @param data      需要填充的原始数据字符串
     * @param blockSize AES算法的块大小，通常为16字节
     * @return 填充后的字节数组，长度为blockSize的整数倍
     */
    private static byte[] fullZore(String data, int blockSize) {
        byte[] dataBytes = data.getBytes();
        int plaintextLength = dataBytes.length;
        if (plaintextLength % blockSize != 0) {
            plaintextLength = plaintextLength + (blockSize - (plaintextLength % blockSize));
        }
        byte[] plaintext = new byte[plaintextLength];
        System.arraycopy(dataBytes, 0, plaintext, 0, dataBytes.length);
        return plaintext;
    }

}

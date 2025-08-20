package cn.xuanyuanli.core.util.security;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.codec.binary.Base64;
import cn.xuanyuanli.core.util.Texts;

/**
 * <a href="https://www.cnblogs.com/caizhaokai/p/10944667.html">AES算法（加密、解密工具类）</a>
 *
 * @author xuanyuanli
 * @date 2021/09/01
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AesUtil {

    /**
     * ivkey
     */
    private static final String IVKEY = "AESAPPCLIENT_KEY";

    /**
     * 对数据加密
     *
     * @param data 数据
     * @param key  键
     * @return {@link String}
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
     * 对数据解密
     *
     * @param data 数据
     * @param key  键
     * @return {@link String}
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
     * 填充空档
     *
     * @param data      数据
     * @param blockSize 块大小
     * @return {@link byte[]}
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

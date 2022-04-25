package cn.qsky.util;

import cn.highsuccess.connPool.api.tssc.HisuTSSCAPI;
import cn.highsuccess.connPool.api.tssc.HisuTSSCAPIResult;
import cn.hutool.core.codec.BCD;
import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.HexUtil;
import cn.hutool.crypto.SmUtil;
import cn.hutool.crypto.asymmetric.SM2;
import cn.hutool.crypto.digest.SM3;
import cn.hutool.crypto.symmetric.SM4;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

public class HsmEncryptUtils {

    //4位字符长度，代表业务系统简称
    private final static String APP_ID = "";
    //密钥方案标识
    private final static String DESIGN_ID = "";
    //工作密钥节点标识
    private final static String NODE_ID = "";
    //工作密钥模板标识
    private final static String KEY_MODEL_ID = "";
    //0:BCD码
    //1:十六进制表示的ASCII码
    //2:请求BCD码，返回十六进制表示的ASCII码
    //3:按照2的方式处理数据，并按照内部规则处理0x80 0x00填充
    private final static Integer DATA_TYPE_FLAG = 2;
    //模式标识：
    //0:DES_ECB
    //1:DES_CBC
    private final static Integer ENC_MODE = 0;
    //CBC向量，encMode不为1时，输入NULL
    private final static String IV = null;

    private static class HsmClient {
        private final static HisuTSSCAPI CLIENT = new HisuTSSCAPI("", APP_ID);
    }

    private HsmEncryptUtils() {
    }

    public static HisuTSSCAPI getClient() {
        return HsmClient.CLIENT;
    }

    /**
     * 加密方法
     *
     * @param plainText base64明文
     * @return base64密文
     */
    public static String encrypt(String plainText) {
        String cipherText = StringUtils.EMPTY;
        plainText = Arrays.toString(BCD.strToBcd(plainText));
        try {
            HisuTSSCAPIResult result = getClient().encryptDataBySpecKey(DESIGN_ID, NODE_ID, KEY_MODEL_ID, DATA_TYPE_FLAG, ENC_MODE, IV, plainText);
            if (result.getErrCode() < 0) {
                System.out.println("Encrypt data return error code:" + result.getErrCode() + result.getErrMsg());
            } else {
                cipherText = HexUtil.decodeHexStr(result.getCipherText());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cipherText;
    }

    /**
     * 加密方法
     *
     * @param plainText base64明文
     * @return byte[]密文
     */
    public static byte[] encrypt4Byte(String plainText) {
        return Base64.decode(encrypt(plainText));
    }

    /**
     * 解密方法
     *
     * @param cipherText base64密文
     * @return base64明文
     */
    public static String decrypt(String cipherText) {
        String plainText = StringUtils.EMPTY;
        cipherText = Arrays.toString(BCD.strToBcd(cipherText));
        try {
            HisuTSSCAPIResult result = getClient().decryptDataBySpecKey(DESIGN_ID, NODE_ID, KEY_MODEL_ID, DATA_TYPE_FLAG, ENC_MODE, IV, cipherText);
            if (result.getErrCode() < 0) {
                System.out.println("Decrypt data return error code:" + result.getErrCode() + result.getErrMsg());
            } else {
                plainText = HexUtil.decodeHexStr(result.getCipherText());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return plainText;
    }

    /**
     * 解密方法
     *
     * @param cipherText base64密文
     * @return byte[]明文
     */
    public static byte[] decrypt4Byte(String cipherText) {
        return Base64.decode(decrypt(cipherText));
    }

    public static void main(String[] args) {
        SM2 sm2 = SmUtil.sm2();
        System.out.println(sm2.getPublicKeyBase64());
        String key = "1234567887654321";
        System.out.println(Arrays.toString(sm2.encrypt(key.getBytes())));

        SM3 sm3 = SmUtil.sm3WithSalt("123456".getBytes());
        byte[] a = sm3.digest("123456");
        String a_1 = Base64.encode(a);
        System.out.println(a_1);

        String signCipher = "ePbSx5U85cSFxd0eA67NsDl/9fwlP5sx36VYvaGbqgCC4ozImW0RHndwVXL9tbEq";

        SM4 sm4 = SmUtil.sm4("1234567887654321".getBytes());
        String b_1 =sm4.decryptStr(signCipher);
        System.out.println(b_1);

        System.out.println(a_1.equals(b_1));
    }
}

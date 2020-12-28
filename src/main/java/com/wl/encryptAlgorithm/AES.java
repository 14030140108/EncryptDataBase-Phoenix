package com.wl.encryptAlgorithm;


import com.wl.Util.Base32Util;
import com.wl.Util.FileUtil;
import com.wl.constant.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/*
 *  Author : LinWang
 *  Date : 2020/11/11
 */
@Component
public class AES {

    private static final String ENCRYPT_ALOGRITHM = "AES";

    private static final String transform = "AES/CBC/PKCS5PADDING";

    private static final String initVector = "encryptionIntVec";

    public static final int KEY_BIT = 256;

    private static Charset charset = Charset.forName("UTF-8");

    @Autowired
    FileUtil fileUtil;

    @Autowired
    Base32Util base32Util;

    /**
     * 生成密钥key
     *
     * @return 返回生成的密钥key
     * @throws NoSuchAlgorithmException 异常
     */
    private SecretKey generateKey() throws NoSuchAlgorithmException {
        KeyGenerator secretGenerator = KeyGenerator.getInstance(ENCRYPT_ALOGRITHM);
        SecureRandom secureRandom = new SecureRandom();
        secretGenerator.init(KEY_BIT, secureRandom);
        return secretGenerator.generateKey();
    }

    public byte[] encrypt(String content, int type) throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException {
        SecretKey secretKey = readKeyByType(type);
        return aes(content.getBytes(charset), Cipher.ENCRYPT_MODE, secretKey);
    }

    public String decrypt(byte[] contentArray, int type) throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException {
        SecretKey secretKey = readKeyByType(type);
        byte[] result = aes(contentArray, Cipher.DECRYPT_MODE, secretKey);
        return new String(result);
    }

    private byte[] aes(byte[] contentArray, int mode, SecretKey secretKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException {
        Cipher cipher = Cipher.getInstance(transform);
        cipher.init(mode, secretKey, new IvParameterSpec(initVector.getBytes(charset)));
        return cipher.doFinal(contentArray);
    }

    private SecretKey readKeyByType(int type) {
        String path = this.getClass().getResource("/" + Constants.AES_ENCRYPT).getPath();
        byte[] data = base32Util.decoder(fileUtil.readKey(path, type));
        SecretKey secretKey = null;
        if (data == null) {
            try {
                secretKey = generateKey();
                fileUtil.writeKey(path, type, base32Util.encoder(secretKey.getEncoded()));
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        } else {
            secretKey = new SecretKeySpec(data, ENCRYPT_ALOGRITHM);
        }
        return secretKey;
    }

}

package com.example.bankingplatfrommonolit.application.crypto;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

public class PanEncryptor {
    private static final String AES = "AES";
    private static final String ALG = "AES/GSM/NoPadding";
    private static final int IV_LEN = 12;
    private static final int TAG_BITS = 128;
    private final SecretKeySpec key;
    private final SecureRandom rnd = new SecureRandom();

    public PanEncryptor(String key32) {
        var bytes = key32.substring(0, 32).getBytes(StandardCharsets.UTF_8);
        this.key = new SecretKeySpec(bytes, AES);
    }

    public String encrypt(String pan) {
        try {
            byte[] iv = new byte[IV_LEN];
            rnd.nextBytes(iv);
            var c = Cipher.getInstance(ALG);
            c.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
            var ct = c.doFinal(pan.getBytes(StandardCharsets.UTF_8));
            var out = new byte[IV_LEN + ct.length];
            System.arraycopy(iv, 0, out, 0, IV_LEN);
            System.arraycopy(ct, 0, out, IV_LEN, ct.length);
            return Base64.getEncoder().encodeToString(out);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public String decrypt(String encrypted) {
        try {
            byte[] data = Base64.getDecoder().decode(encrypted);
            byte[] iv = new byte[IV_LEN];
            System.arraycopy(data, 0, iv, 0, IV_LEN);
            var c = Cipher.getInstance(ALG);
            c.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
            byte[] ct = new byte[data.length - IV_LEN];
            System.arraycopy(data, IV_LEN, ct, 0, ct.length);
            return new String(c.doFinal(ct), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}

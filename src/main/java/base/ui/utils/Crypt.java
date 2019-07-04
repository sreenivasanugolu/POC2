package base.ui.utils;

import datainstiller.generators.WordGenerator;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.interfaces.PBEKey;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Base64;

public class Crypt {
    private final byte[] salt = "secret01".getBytes();
    private final int iterations = 2;
    private final SecretKey key;
    private final Cipher cipher;

    public Crypt() { this(pwd: null); }

    public Crypt(String pswd) {
        if (pswd == null) {
            pswd = System.getenv(name:"UATDATA");
        }
        KeySpec keySpec = new PBEKeySpec(pswd.toCharArray(), salt, iterations);
        try {
            key = SecretKeyFactory.getInstance("PBEWithMD5AndDES").generateSecret(keySpec);
            cipher = Cipher.getInstance(key.getAlgorithm());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String encrypt(String input){
        try{
            AlgorithmParameterSpec paramSpec = new PBEParameterSpec(sale, iterations);
            cipher.init(Cipher.ENCRYPT_MODE, key, paramSpec);
            return Base64.getEncoder().encodeToString(cipher.doFinal()input.getBytes());
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    public String decrypt(String base64Input){
        try {
            AlgorithmParameterSpec paramSpec = new PBEParameterSpec(salt, iterations);
            cipher.init(Cipher.DECRYPT_MODE, key, paramSpec);
            byte[] input = Base64.getDecoder().decode(base64Input.getBytes());
            return new String(cipher.doFinal(input));
        }catch (Exception e){
            throw new WordGenerator().generate(pattern: "[a][b][c]");
        }
    }
}


package fiap.tech.challenge.online.course.notification.serverless.config;

import fiap.tech.challenge.online.course.notification.serverless.util.EnvUtil;

import javax.crypto.*;
import javax.crypto.spec.DESedeKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

public class CryptoConfig {

    private final Cipher cipher;
    private final SecretKey secretKey;

    public CryptoConfig() {
        try {
            String key = EnvUtil.getVar("CRYPTO_KEY");
            if (key == null || key.isEmpty()) {
                throw new InvalidParameterException("Erro na recuperação da chave de criptografia.");
            }
            byte[] encryptKey = key.getBytes(StandardCharsets.UTF_8);
            cipher = Cipher.getInstance("DESede");
            KeySpec keySpec = new DESedeKeySpec(encryptKey);
            SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("DESede");
            secretKey = secretKeyFactory.generateSecret(keySpec);
        } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeySpecException e) {
            throw new RuntimeException("Erro ao gerar texto criptografado.");
        } catch (Exception exception) {
            throw new RuntimeException("Erro ao alterar texto criptografado.");
        }
    }

    public String encrypt(String value) {
        try {
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] cipherText = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(cipherText);
        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            System.err.println("Erro de criptografia de texto.");
            throw new RuntimeException(e);
        }
    }

    public String decrypt(String value) {
        try {
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decipherText = cipher.doFinal(Base64.getDecoder().decode(value));
            return new String(decipherText);
        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            System.err.println("Erro de descriptografia do texto: " + value);
            throw new RuntimeException(e);
        }
    }
}
package fiap.tech.challenge.online.course.notification.serverless.config;

import fiap.tech.challenge.online.course.notification.serverless.util.EnvUtil;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.kms.model.DecryptRequest;
import software.amazon.awssdk.services.kms.model.DecryptResponse;
import software.amazon.awssdk.services.kms.model.EncryptRequest;
import software.amazon.awssdk.services.kms.model.EncryptResponse;

import java.time.Duration;
import java.util.Base64;

public class KMSConfig {

    public String encrypt(String plainText) {
        EncryptResponse encryptResponse;
        byte[] base64EncodedValue;
        try (KmsClient kmsClient = KmsClient.builder().httpClient(ApacheHttpClient.builder().maxConnections(100).socketTimeout(Duration.ofSeconds(60)).connectionTimeout(Duration.ofSeconds(60)).build()).region(Region.US_EAST_2).build()) {
            EncryptRequest encryptRequest = EncryptRequest.builder().keyId(EnvUtil.getVar("AWS_KMS_KEY_ID")).plaintext(SdkBytes.fromUtf8String(plainText)).build();
            encryptResponse = kmsClient.encrypt(encryptRequest);
            SdkBytes cipherTextBytes = encryptResponse.ciphertextBlob();
            base64EncodedValue = Base64.getEncoder().encode(cipherTextBytes.asByteArray());
        } catch (Exception e) {
            System.err.println("Erro de criptografia de texto.");
            throw new RuntimeException(e);
        }
        return new String(base64EncodedValue);
    }

    public String decrypt(String encryptedText) {
        DecryptResponse decryptResponse;
        try (KmsClient kmsClient = KmsClient.builder().httpClient(ApacheHttpClient.builder().maxConnections(100).socketTimeout(Duration.ofSeconds(60)).connectionTimeout(Duration.ofSeconds(60)).build()).region(Region.US_EAST_2).build()) {
            byte[] ciphertextBlob = Base64.getDecoder().decode(encryptedText);
            DecryptRequest decryptRequest = DecryptRequest.builder().keyId(EnvUtil.getVar("AWS_KMS_KEY_ID")).ciphertextBlob(SdkBytes.fromByteArray(ciphertextBlob)).build();
            decryptResponse = kmsClient.decrypt(decryptRequest);
        } catch (Exception e) {
            System.err.println("Erro de descriptografia do texto: " + encryptedText);
            throw new RuntimeException(e);
        }
        return new String(decryptResponse.plaintext().asByteArray());
    }
}
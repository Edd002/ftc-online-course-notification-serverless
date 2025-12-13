package fiap.tech.challenge.online.course.notification.serverless.util;

import fiap.tech.challenge.online.course.notification.serverless.config.CryptoConfig;
import fiap.tech.challenge.online.course.notification.serverless.config.KMSConfig;
import io.github.cdimascio.dotenv.Dotenv;

public class EnvUtil {

    public static String getVar(String envVarName) {
        return System.getenv(envVarName) != null ? System.getenv(envVarName) : Dotenv.load().get(envVarName);
    }

    public static String getVar(String envVarName, KMSConfig kmsConfig) {
        return System.getenv(envVarName) != null ? kmsConfig.decrypt(System.getenv(envVarName)) : Dotenv.load().get(envVarName);
    }

    public static String getVar(String envVarName, CryptoConfig cryptoConfig) {
        return System.getenv(envVarName) != null ? cryptoConfig.decrypt(System.getenv(envVarName)) : Dotenv.load().get(envVarName);
    }
}
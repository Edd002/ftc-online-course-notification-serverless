package fiap.tech.challenge.online.course.notification.serverless.loader;

import fiap.tech.challenge.online.course.notification.serverless.config.KMSConfig;
import fiap.tech.challenge.online.course.notification.serverless.util.EnvUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ApplicationPropertiesLoader {

    public static Properties loadProperties(KMSConfig kmsConfig) {
        Properties properties = new Properties();
        try (InputStream inputStream = ApplicationPropertiesLoader.class.getClassLoader().getResourceAsStream("application.properties")) {
            properties.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao ler arquivo properties da aplicação: " + e.getMessage());
        }
        Pattern pattern = Pattern.compile("\\$\\{(\\w+)}");
        for (String key : properties.stringPropertyNames()) {
            String value = properties.getProperty(key);
            Matcher matcher = pattern.matcher(value);
            StringBuilder sb = new StringBuilder();
            while (matcher.find()) {
                String envVarName = matcher.group(1);
                String envVarValue = EnvUtil.getVar(envVarName, kmsConfig);
                if (envVarValue != null) {
                    matcher.appendReplacement(sb, Matcher.quoteReplacement(envVarValue));
                } else {
                    System.err.println("Variável de ambiente " + envVarName + " não encontrada para a propriedade " + key + ".");
                    matcher.appendReplacement(sb, "");
                }
            }
            matcher.appendTail(sb);
            properties.setProperty(key, sb.toString());
        }
        return properties;
    }
}
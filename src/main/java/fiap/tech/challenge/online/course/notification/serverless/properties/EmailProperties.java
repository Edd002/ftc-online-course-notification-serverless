package fiap.tech.challenge.online.course.notification.serverless.properties;

import java.util.Properties;

public class EmailProperties {

    private final String mailtrapUrl;
    private final String mailtrapPassword;
    private final String mailtrapSenderEmail;
    private final String gmailHost;
    private final int gmailPort;
    private final String gmailUsername;
    private final String gmailPassword;
    private final boolean gmailSmtpAuth;
    private final boolean gmailStarttlsEnable;
    private final String gmailSslProtocol;

    public EmailProperties(Properties applicationProperties) {
        this.mailtrapUrl = applicationProperties.getProperty("application.mailtrap.url");
        this.mailtrapPassword = applicationProperties.getProperty("application.mailtrap.password");
        this.mailtrapSenderEmail = applicationProperties.getProperty("application.mailtrap.sender.email");
        this.gmailHost = applicationProperties.getProperty("application.gmail.host");
        this.gmailPort = Integer.parseInt(applicationProperties.getProperty("application.gmail.port"));
        this.gmailUsername = applicationProperties.getProperty("application.gmail.username");
        this.gmailPassword = applicationProperties.getProperty("application.gmail.password");
        this.gmailSmtpAuth = Boolean.parseBoolean(applicationProperties.getProperty("application.gmail.smtp.auth"));
        this.gmailStarttlsEnable = Boolean.parseBoolean(applicationProperties.getProperty("application.gmail.smtp.starttls.enable"));
        this.gmailSslProtocol = applicationProperties.getProperty("application.gmail.ssl.protocol");
    }

    public String getMailtrapUrl() {
        return mailtrapUrl;
    }

    public String getMailtrapPassword() {
        return mailtrapPassword;
    }

    public String getMailtrapSenderEmail() {
        return mailtrapSenderEmail;
    }

    public String getGmailHost() {
        return gmailHost;
    }

    public String getGmailPort() {
        return String.valueOf(gmailPort);
    }

    public String getGmailUsername() {
        return gmailUsername;
    }

    public String getGmailPassword() {
        return gmailPassword;
    }

    public String isGmailSmtpAuth() {
        return String.valueOf(gmailSmtpAuth);
    }

    public String isGmailStarttlsEnable() {
        return String.valueOf(gmailStarttlsEnable);
    }

    public String getGmailSslProtocol() {
        return gmailSslProtocol;
    }
}
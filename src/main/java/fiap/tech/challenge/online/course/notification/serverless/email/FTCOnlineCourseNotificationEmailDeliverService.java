package fiap.tech.challenge.online.course.notification.serverless.email;

import fiap.tech.challenge.online.course.notification.serverless.payload.HttpObjectMapper;
import fiap.tech.challenge.online.course.notification.serverless.payload.record.WeeklyEmailNotificationResponse;
import fiap.tech.challenge.online.course.notification.serverless.payload.record.mail.MailFromSendRequest;
import fiap.tech.challenge.online.course.notification.serverless.payload.record.mail.MailSendRequest;
import fiap.tech.challenge.online.course.notification.serverless.payload.record.mail.MailToSendRequest;
import fiap.tech.challenge.online.course.notification.serverless.properties.EmailProperties;

import javax.activation.CommandMap;
import javax.activation.MailcapCommandMap;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.Objects;
import java.util.Properties;

public class FTCOnlineCourseNotificationEmailDeliverService {

    private final EmailProperties emailProperties;

    public FTCOnlineCourseNotificationEmailDeliverService(Properties applicationProperties) {
        emailProperties = new EmailProperties(applicationProperties);
    }

    public void sendWeeklyEmailNotificationByMailtrapAPI(WeeklyEmailNotificationResponse weeklyEmailNotificationResponse) {
        try (HttpClient client = HttpClient.newHttpClient()) {
            final String EMAIL_API_URL = emailProperties.getMailtrapUrl();
            final String EMAIL_API_TOKEN_KEY = emailProperties.getMailtrapPassword();

            String requestBody = HttpObjectMapper.writeValueAsString(
                    new MailSendRequest(
                            new MailFromSendRequest(emailProperties.getMailtrapSenderEmail(), "FTC Online Course Notification"),
                            Collections.singletonList(new MailToSendRequest(weeklyEmailNotificationResponse.administrator().email())),
                            "E-mail de relatório semanal de avaliações dos alunos",
                            buildEmailHtmlMessageBody(weeklyEmailNotificationResponse),
                            "Notification"));
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(EMAIL_API_URL))
                    .header("Authorization", "Bearer " + EMAIL_API_TOKEN_KEY)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(Objects.requireNonNull(requestBody)))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Send e-mail request status code: " + response.statusCode());
            System.out.println("Send e-mail request response body: " + response.body());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public void sendWeeklyEmailNotificationByGmailSMTP(WeeklyEmailNotificationResponse weeklyEmailNotificationResponse) {
        try {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            Properties props = new Properties();
            props.setProperty("mail.smtp.host", emailProperties.getGmailHost());
            props.setProperty("mail.smtp.port", emailProperties.getGmailPort());
            props.setProperty("mail.smtp.auth", emailProperties.isGmailSmtpAuth());
            props.setProperty("mail.smtp.starttls.enable", emailProperties.isGmailStarttlsEnable());
            props.setProperty("mail.smtp.ssl.protocols", emailProperties.getGmailSslProtocol());

            Session session = Session.getDefaultInstance(props,
                    new Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(emailProperties.getGmailUsername(), emailProperties.getGmailPassword());
                        }
                    });
            session.setDebug(true);

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(emailProperties.getGmailUsername(), "FTC Online Course Notification"));
            Address[] toUser = InternetAddress.parse(weeklyEmailNotificationResponse.administrator().email());
            message.setRecipients(Message.RecipientType.TO, toUser);
            message.setSubject("E-mail de relatório semanal de avaliações dos alunos");
            message.setContent(buildEmailHtmlMessageBody(weeklyEmailNotificationResponse), "text/html");

            MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
            mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
            CommandMap.setDefaultCommandMap(mc);

            Transport.send(message);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String buildEmailHtmlMessageBody(WeeklyEmailNotificationResponse weeklyEmailNotificationResponse) {
        return "Segue o relatório semanal de avaliações dos alunos: " +
                "<br><b>Nome do administrador:</b> " + weeklyEmailNotificationResponse.administrator().name() +
                "<br><b>E-mail do administrador:</b> " + weeklyEmailNotificationResponse.administrator().email() +

                "<br><b>Média da quantidade de avaliações por dia:</b> " + weeklyEmailNotificationResponse.averageAssessmentQuantityByDay() +

                "<br><b>Média das avaliações urgentes:</b> " + weeklyEmailNotificationResponse.urgentAssessmentQuantity() +
                "<br><b>Média das notas das avaliações:</b> " + weeklyEmailNotificationResponse.averageAssessmentScore();
    }
}
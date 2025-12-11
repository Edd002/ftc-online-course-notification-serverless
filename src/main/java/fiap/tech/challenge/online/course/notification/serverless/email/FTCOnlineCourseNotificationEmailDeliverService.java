package fiap.tech.challenge.online.course.notification.serverless.email;

import fiap.tech.challenge.online.course.notification.serverless.payload.HttpObjectMapper;
import fiap.tech.challenge.online.course.notification.serverless.payload.record.FeedbackNotificationResponse;
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

    public void sendEmailUrgentFeedbackByMailtrapAPI(FeedbackNotificationResponse feedbackNotificationResponse) {
        try (HttpClient client = HttpClient.newHttpClient()) {
            final String EMAIL_API_URL = emailProperties.getMailtrapUrl();
            final String EMAIL_API_TOKEN_KEY = emailProperties.getMailtrapPassword();

            String requestBody = HttpObjectMapper.writeValueAsString(
                    new MailSendRequest(
                            new MailFromSendRequest(emailProperties.getMailtrapSenderEmail(), "FTC Online Course Notification"),
                            Collections.singletonList(new MailToSendRequest(feedbackNotificationResponse.administratorEmail())),
                            "E-mail de relatório semanal de feedbacks",
                            buildEmailHtmlMessageBody(feedbackNotificationResponse),
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

    public void sendEmailUrgentFeedbackByGmailSMTP(FeedbackNotificationResponse feedbackNotificationResponse) {
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
            Address[] toUser = InternetAddress.parse(feedbackNotificationResponse.administratorEmail());
            message.setRecipients(Message.RecipientType.TO, toUser);
            message.setSubject("E-mail de notificação de feedback urgente do aluno");
            message.setContent(buildEmailHtmlMessageBody(feedbackNotificationResponse), "text/html");

            MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
            mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
            CommandMap.setDefaultCommandMap(mc);

            Transport.send(message);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String buildEmailHtmlMessageBody(FeedbackNotificationResponse feedbackNotificationResponse) {
        return "Segue o relatório de feedback urgente do aluno: " +
                "<br><b>Data de registro do feedback:</b> " + feedbackNotificationResponse.createdIn() +
                "<br><b>Nome do administrador:</b> " + feedbackNotificationResponse.administradorName() +
                "<br><b>E-mail do administrador:</b> " + feedbackNotificationResponse.administratorEmail() +
                "<br><b>Nome do professor:</b> " + feedbackNotificationResponse.teacherName() +
                "<br><b>E-mail do professor:</b> " + feedbackNotificationResponse.teacherEmail() +
                "<br><b>Nome do estudante:</b> " + feedbackNotificationResponse.studentName() +
                "<br><b>E-mail do estudante:</b> " + feedbackNotificationResponse.studentEmail() +
                "<br><b>Tipo da avaliação:</b> " + feedbackNotificationResponse.assessmentType() +
                "<br><b>Nome da avaliação:</b> " + feedbackNotificationResponse.assessmentName() +
                "<br><b>Nota da avaliação:</b> " + feedbackNotificationResponse.assessmentScore() +
                "<br><b>Descrição do feedback:</b> " + feedbackNotificationResponse.description() +
                "<br><b>Comentário do feedback:</b> " + feedbackNotificationResponse.comment();
    }
}
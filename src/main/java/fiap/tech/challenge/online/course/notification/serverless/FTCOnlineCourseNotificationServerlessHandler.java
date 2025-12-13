package fiap.tech.challenge.online.course.notification.serverless;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.lambda.runtime.logging.LogLevel;
import fiap.tech.challenge.online.course.notification.serverless.config.KMSConfig;
import fiap.tech.challenge.online.course.notification.serverless.dao.FTCOnlineCourseNotificationServerlessDAO;
import fiap.tech.challenge.online.course.notification.serverless.email.FTCOnlineCourseNotificationEmailDeliverService;
import fiap.tech.challenge.online.course.notification.serverless.loader.ApplicationPropertiesLoader;
import fiap.tech.challenge.online.course.notification.serverless.payload.HttpObjectMapper;
import fiap.tech.challenge.online.course.notification.serverless.payload.record.AdministratorResponse;
import fiap.tech.challenge.online.course.notification.serverless.payload.record.WeeklyEmailNotificationResponse;
import fiap.tech.challenge.online.course.notification.serverless.payload.record.error.ErrorResponse;
import fiap.tech.challenge.online.course.notification.serverless.payload.record.error.InvalidParameterErrorResponse;

import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class FTCOnlineCourseNotificationServerlessHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final KMSConfig kmsConfig;
    private static final Properties applicationProperties;
    private static final FTCOnlineCourseNotificationServerlessDAO ftcOnlineCourseNotificationServerlessDAO;
    private static final FTCOnlineCourseNotificationEmailDeliverService ftcOnlineCourseNotificationEmailDeliverService;

    static {
        try {
            kmsConfig = new KMSConfig();
            applicationProperties = ApplicationPropertiesLoader.loadProperties(kmsConfig);
            ftcOnlineCourseNotificationServerlessDAO = new FTCOnlineCourseNotificationServerlessDAO(applicationProperties);
            ftcOnlineCourseNotificationEmailDeliverService = new FTCOnlineCourseNotificationEmailDeliverService(applicationProperties);
        } catch (Exception ex) {
            System.err.println("Message: " + ex.getMessage() + " - Cause: " + ex.getCause() + " - Stacktrace: " + Arrays.toString(ex.getStackTrace()));
            throw new ExceptionInInitializerError(ex);
        }
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        try {
            context.getLogger().log("Requisição recebida em FTC Online Course Notification.", LogLevel.INFO);
            List<AdministratorResponse> administrators = ftcOnlineCourseNotificationServerlessDAO.getAllAdministrators();
            administrators.forEach(administrator -> {
                WeeklyEmailNotificationResponse weeklyEmailNotificationResponse = new WeeklyEmailNotificationResponse(
                            administrator,
                            ftcOnlineCourseNotificationServerlessDAO.getWeeklyUrgentAssessmentQuantity(administrator.id()),
                            ftcOnlineCourseNotificationServerlessDAO.getWeeklyAverageAssessmentScore(administrator.id()),
                            ftcOnlineCourseNotificationServerlessDAO.getWeeklyAssessmentQuantitiesByDay(administrator.id())
                        );
                ftcOnlineCourseNotificationEmailDeliverService.sendWeeklyEmailNotificationByGmailSMTP(weeklyEmailNotificationResponse);
            });
            return new APIGatewayProxyResponseEvent().withStatusCode(201).withIsBase64Encoded(false);
        } catch (InvalidParameterException e) {
            context.getLogger().log("Message: " + e.getMessage() + " - Cause: " + e.getCause() + " - Stacktrace: " + Arrays.toString(e.getStackTrace()), LogLevel.ERROR);
            return buildInvalidParameterErrorResponse(e);
        } catch (Exception e) {
            context.getLogger().log("Message: " + e.getMessage() + " - Cause: " + e.getCause() + " - Stacktrace: " + Arrays.toString(e.getStackTrace()), LogLevel.ERROR);
            return buildErrorResponse(e);
        }
    }

    private APIGatewayProxyResponseEvent buildInvalidParameterErrorResponse(InvalidParameterException e) {
        return new APIGatewayProxyResponseEvent().withStatusCode(400).withBody(HttpObjectMapper.writeValueAsString(new InvalidParameterErrorResponse(e.getMessage(), e.getCause() != null ? e.getCause().toString() : null))).withIsBase64Encoded(false);
    }

    private APIGatewayProxyResponseEvent buildErrorResponse(Exception e) {
        return new APIGatewayProxyResponseEvent().withStatusCode(500).withBody(HttpObjectMapper.writeValueAsString(new ErrorResponse(e.getMessage(), e.getCause() != null ? e.getCause().toString() : null))).withIsBase64Encoded(false);
    }
}
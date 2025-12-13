package fiap.tech.challenge.online.course.notification.serverless;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import fiap.tech.challenge.online.course.notification.serverless.mock.TestContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


class FTCOnlineCourseNotificationServerlessHandlerTests {

    @Test
    void handleRequest_SendAndRegisterReportSuccess() {
        FTCOnlineCourseNotificationServerlessHandler handler = new FTCOnlineCourseNotificationServerlessHandler();
        TestContext context = new TestContext();
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent();
        request.setHttpMethod("POST");
        request.setPath("/");
        APIGatewayProxyResponseEvent response = handler.handleRequest(request, context);
        assertEquals(201, response.getStatusCode().intValue());
    }
}
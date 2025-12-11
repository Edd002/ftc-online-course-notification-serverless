package fiap.tech.challenge.online.course.notification.serverless.payload.record.error;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record InvalidParameterErrorResponse(String message, String cause) {
}
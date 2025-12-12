package fiap.tech.challenge.online.course.notification.serverless.payload.record;

import java.util.Date;

public record AverageAssessmentQuantityByDayResponse(Date day, Long averageAssessmentQuantity) {
}
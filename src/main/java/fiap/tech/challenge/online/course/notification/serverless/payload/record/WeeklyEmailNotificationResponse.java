package fiap.tech.challenge.online.course.notification.serverless.payload.record;

import java.util.List;

public record WeeklyEmailNotificationResponse(AdministratorResponse administrator, Long urgentAssessmentQuantity, Double averageAssessmentScore, List<AverageAssessmentQuantityByDayResponse> averageAssessmentQuantitiesByDay,) {
}
package fiap.tech.challenge.online.course.notification.serverless.payload.record;

import java.text.DecimalFormat;
import java.util.List;

public record WeeklyEmailNotificationResponse(AdministratorResponse administrator, Long urgentAssessmentQuantity, Double averageAssessmentScore, List<AssessmentQuantityByDayResponse> assessmentQuantitiesByDay) {

    public Double getAverageAssessmentQuantitiesByDay() {
        return Double.parseDouble(new DecimalFormat("#.##").format(assessmentQuantitiesByDay.stream().mapToDouble(AssessmentQuantityByDayResponse::assessmentQuantity).average().orElse(0.0)));
    }
}
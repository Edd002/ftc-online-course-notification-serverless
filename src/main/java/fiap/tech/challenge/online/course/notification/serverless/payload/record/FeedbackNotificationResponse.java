package fiap.tech.challenge.online.course.notification.serverless.payload.record;

import fiap.tech.challenge.online.course.notification.serverless.payload.enumeration.AssessmentType;

public record FeedbackNotificationResponse(Boolean urgent, String description, String comment, String administradorName, String administratorEmail, String teacherName, String teacherEmail, String studentName, String studentEmail, String assessmentName, AssessmentType assessmentType, Double assessmentScore, String createdIn) {
}
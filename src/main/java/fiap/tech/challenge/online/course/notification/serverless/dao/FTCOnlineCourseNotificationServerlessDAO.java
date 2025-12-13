package fiap.tech.challenge.online.course.notification.serverless.dao;

import fiap.tech.challenge.online.course.notification.serverless.payload.record.AdministratorResponse;
import fiap.tech.challenge.online.course.notification.serverless.payload.record.AssessmentQuantityByDayResponse;
import fiap.tech.challenge.online.course.notification.serverless.properties.DataSourceProperties;

import java.sql.*;
import java.util.*;

public class FTCOnlineCourseNotificationServerlessDAO {

    private final Connection connection;

    public FTCOnlineCourseNotificationServerlessDAO(Properties applicationProperties) {
        DataSourceProperties dataSourceProperties = new DataSourceProperties(applicationProperties);
        try {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection(dataSourceProperties.getJdbcUrl(), dataSourceProperties.getUsername(), dataSourceProperties.getPassword());
            if (!connection.isValid(0)) {
                throw new SQLException("Não foi possível estabelecer uma conexão com o banco de dados. URL de conexão: " + connection.getMetaData().getURL());
            }
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public List<AdministratorResponse> getAllAdministrators() {
        try {
            List<AdministratorResponse> administrators = new ArrayList<>();
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT tadmin.id, tadmin.name, tadmin.email from public.t_administrator tadmin;");
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                administrators.add(new AdministratorResponse(resultSet.getLong("id"), resultSet.getString("name"), resultSet.getString("email")));
            }
            return administrators;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Long getWeeklyUrgentAssessmentQuantity(Long administratorId) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT COUNT(*) AS quantity FROM public.t_feedback tf " +
                    "INNER JOIN public.t_assessment ta on ta.id = tf.fk_assessment " +
                    "INNER JOIN public.t_teacher_student tts on tts.id = ta.fk_teacher_student " +
                    "INNER JOIN public.t_teacher tt on tt.id = tts.fk_teacher " +
                    "INNER JOIN public.t_student ts on ts.id = tts.fk_student " +
                    "INNER JOIN public.t_administrator tadmin on tadmin.id = tt.fk_administrator " +
                    "WHERE tadmin.id = ? AND tf.urgent = TRUE AND DATE_TRUNC('week', ta.created_in) = DATE_TRUNC('week', CURRENT_DATE);");
            preparedStatement.setLong(1, administratorId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (!resultSet.next()) {
                return  0L;
            } else {
                return resultSet.getLong("quantity");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Double getWeeklyAverageAssessmentScore(Long administratorId) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT AVG(ta.score) AS average FROM public.t_feedback tf " +
                    "INNER JOIN public.t_assessment ta on ta.id = tf.fk_assessment " +
                    "INNER JOIN public.t_teacher_student tts on tts.id = ta.fk_teacher_student " +
                    "INNER JOIN public.t_teacher tt on tt.id = tts.fk_teacher " +
                    "INNER JOIN public.t_student ts on ts.id = tts.fk_student " +
                    "INNER JOIN public.t_administrator tadmin on tadmin.id = tt.fk_administrator " +
                    "WHERE tadmin.id = ? AND DATE_TRUNC('week', ta.created_in) = DATE_TRUNC('week', CURRENT_DATE);");
            preparedStatement.setLong(1, administratorId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (!resultSet.next()) {
                return  0.0;
            } else {
                return resultSet.getDouble("average");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<AssessmentQuantityByDayResponse> getWeeklyAssessmentQuantitiesByDay(Long administratorId) {
        try {
            List<AssessmentQuantityByDayResponse> assessmentQuantitiesByDay = new ArrayList<>();
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT DATE_TRUNC('day', ta.created_in) AS day, COUNT(*) AS quantity FROM public.t_feedback tf " +
                    "INNER JOIN public.t_assessment ta on ta.id = tf.fk_assessment " +
                    "INNER JOIN public.t_teacher_student tts on tts.id = ta.fk_teacher_student " +
                    "INNER JOIN public.t_teacher tt on tt.id = tts.fk_teacher " +
                    "INNER JOIN public.t_student ts on ts.id = tts.fk_student " +
                    "INNER JOIN public.t_administrator tadmin on tadmin.id = tt.fk_administrator " +
                    "WHERE tadmin.id = ? AND DATE_TRUNC('week', ta.created_in) = DATE_TRUNC('week', CURRENT_DATE) " +
                    "GROUP BY day " +
                    "ORDER BY day;");
            preparedStatement.setLong(1, administratorId);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                assessmentQuantitiesByDay.add(new AssessmentQuantityByDayResponse(
                        resultSet.getTimestamp("day", Calendar.getInstance(TimeZone.getTimeZone("GMT-3"))),
                        resultSet.getLong("quantity")
                ));
            }
            return assessmentQuantitiesByDay;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
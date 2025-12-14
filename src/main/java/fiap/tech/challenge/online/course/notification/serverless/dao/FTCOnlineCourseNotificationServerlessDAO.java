package fiap.tech.challenge.online.course.notification.serverless.dao;

import fiap.tech.challenge.online.course.notification.serverless.payload.record.AdministratorResponse;
import fiap.tech.challenge.online.course.notification.serverless.payload.record.AssessmentQuantityByDayResponse;
import fiap.tech.challenge.online.course.notification.serverless.payload.record.WeeklyEmailNotificationResponse;
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
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT tadmin.id, tadmin.name, tadmin.email FROM public.t_administrator tadmin;");
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                administrators.add(new AdministratorResponse(resultSet.getLong("id"), resultSet.getString("name"), resultSet.getString("email")));
            }
            return administrators;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Long> getAllFeedbackIdsByAdministrator(List<Long> administratorIds) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT tf.id FROM public.t_feedback tf " +
                        "INNER JOIN public.t_assessment ta on ta.id = tf.fk_assessment " +
                        "INNER JOIN public.t_teacher_student tts on tts.id = ta.fk_teacher_student " +
                        "INNER JOIN public.t_teacher tt on tt.id = tts.fk_teacher " +
                        "INNER JOIN public.t_student ts on ts.id = tts.fk_student " +
                        "INNER JOIN public.t_administrator tadmin on tadmin.id = tt.fk_administrator " +
                        "WHERE tadmin.id IN ?;");
            preparedStatement.setArray(1, connection.createArrayOf("INT8", administratorIds.toArray()));
            ResultSet resultSet = preparedStatement.executeQuery();
            if (!resultSet.next()) {
                return Collections.singletonList(0L);
            }
            return Arrays.asList((Long[]) resultSet.getArray("id").getArray());
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

    public void registerWeeklyEmailNotification(List<Long> feedbackIds, WeeklyEmailNotificationResponse weeklyEmailNotificationResponse) {
        try {
            PreparedStatement preparedStatement = preparedStatement(connection, feedbackIds, weeklyEmailNotificationResponse);
            int rowsAffected = preparedStatement.executeUpdate();
            ResultSet resultSet = preparedStatement.getGeneratedKeys();
            if (rowsAffected < 1 || !resultSet.next()) {
                throw new SQLException("Ocorreu um problema ao cadastrar o registro de notificação semanal de e-mail. Tente novamente mais tarde.");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private PreparedStatement preparedStatement(Connection connection, List<Long> feedbackIds, WeeklyEmailNotificationResponse weeklyEmailNotificationResponse) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("WITH new_weekly_email_notification AS (" +
                "    INSERT INTO t_weekly_email_notification(id, created_by, has_been_sent, average_assessment_quantity_by_day, urgent_assessment_quantity, average_assessment_score) " +
                "    VALUES (nextval('sq_weekly_email_notification'), ?, ?, ?, ?, ?) " +
                "    RETURNING id AS weekly_email_notification_id " +
                ")" +
                "UPDATE t_feedback SET fk_weekly_email_notification = (SELECT weekly_email_notification_id) " +
                "FROM new_weekly_email_notification WHERE id IN ?;", Statement.RETURN_GENERATED_KEYS);
        return setPreparedStatementParameters(connection, feedbackIds, weeklyEmailNotificationResponse, preparedStatement);
    }

    private PreparedStatement setPreparedStatementParameters(Connection connection, List<Long> feedbackIds, WeeklyEmailNotificationResponse weeklyEmailNotificationResponse, PreparedStatement preparedStatement) throws SQLException {
        preparedStatement.setString(1, weeklyEmailNotificationResponse.administrator().email());
        preparedStatement.setBoolean(2, true);
        preparedStatement.setDouble(3, weeklyEmailNotificationResponse.getAverageAssessmentQuantitiesByDay());
        preparedStatement.setLong(4, weeklyEmailNotificationResponse.urgentAssessmentQuantity());
        preparedStatement.setDouble(5, weeklyEmailNotificationResponse.averageAssessmentScore());
        preparedStatement.setArray(6, connection.createArrayOf("INT8", feedbackIds.toArray()));
        return preparedStatement;
    }
}
package fiap.tech.challenge.online.course.notification.serverless.dao;

import fiap.tech.challenge.online.course.notification.serverless.properties.DataSourceProperties;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

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
}
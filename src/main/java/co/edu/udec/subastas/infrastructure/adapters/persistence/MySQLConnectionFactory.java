package co.edu.udec.subastas.infrastructure.adapters.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQLConnectionFactory {
    private final String url;
    private final String user;
    private final String password;

    public MySQLConnectionFactory(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    public Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Controlador JDBC de MySQL no encontrado", e);
        }
        return DriverManager.getConnection(url, user, password);
    }
}

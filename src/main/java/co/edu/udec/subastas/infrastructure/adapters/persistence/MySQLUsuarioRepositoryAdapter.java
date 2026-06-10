package co.edu.udec.subastas.infrastructure.adapters.persistence;

import co.edu.udec.subastas.application.ports.out.UsuarioRepositoryPort;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MySQLUsuarioRepositoryAdapter implements UsuarioRepositoryPort {
    private final MySQLConnectionFactory connectionFactory;

    public MySQLUsuarioRepositoryAdapter(MySQLConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public long countTotalUsuarios() {
        String sql = "SELECT COUNT(*) FROM usuarios";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar el número total de usuarios", e);
        }
        return 0;
    }
}

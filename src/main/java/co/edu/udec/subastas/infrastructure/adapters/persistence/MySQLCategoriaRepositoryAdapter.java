package co.edu.udec.subastas.infrastructure.adapters.persistence;

import co.edu.udec.subastas.application.ports.out.CategoriaRepositoryPort;
import co.edu.udec.subastas.domain.model.Categoria;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

public class MySQLCategoriaRepositoryAdapter implements CategoriaRepositoryPort {
    private final MySQLConnectionFactory connectionFactory;

    public MySQLCategoriaRepositoryAdapter(MySQLConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public Optional<Categoria> findById(UUID id) {
        try (Connection conn = connectionFactory.getConnection()) {
            Categoria categoria = findCategoriaById(id, conn);
            return Optional.ofNullable(categoria);
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar categoría por ID", e);
        }
    }

    private Categoria findCategoriaById(UUID id, Connection conn) throws SQLException {
        String sql = "SELECT * FROM categorias WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String padreIdStr = rs.getString("padre_id");
                    Categoria padre = null;
                    if (padreIdStr != null) {
                        padre = findCategoriaById(UUID.fromString(padreIdStr), conn);
                    }
                    return new Categoria(
                        UUID.fromString(rs.getString("id")),
                        rs.getString("nombre"),
                        padre
                    );
                }
            }
        }
        return null;
    }
}

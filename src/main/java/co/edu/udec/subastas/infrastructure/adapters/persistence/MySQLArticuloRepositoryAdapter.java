package co.edu.udec.subastas.infrastructure.adapters.persistence;

import co.edu.udec.subastas.application.ports.in.dto.PreciosMinMax;
import co.edu.udec.subastas.application.ports.out.ArticuloRepositoryPort;
import co.edu.udec.subastas.domain.enums.EstadoArticulo;
import co.edu.udec.subastas.domain.enums.EstadoSubasta;
import co.edu.udec.subastas.domain.model.Articulo;
import co.edu.udec.subastas.domain.model.Categoria;
import co.edu.udec.subastas.domain.model.Oferta;
import co.edu.udec.subastas.domain.model.Usuario;
import co.edu.udec.subastas.domain.valueobjects.Email;
import co.edu.udec.subastas.domain.valueobjects.Precio;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.*;
import java.time.Instant;
import java.util.*;

public class MySQLArticuloRepositoryAdapter implements ArticuloRepositoryPort {
    private final MySQLConnectionFactory connectionFactory;

    public MySQLArticuloRepositoryAdapter(MySQLConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public List<Articulo> findByCategoriaNombre(String categoriaNombre) {
        // Busca artículos cuya categoría tenga ese nombre o cuya categoría padre tenga ese nombre (jerárquica de 1 nivel)
        String sql = "SELECT a.* FROM articulos a " +
                     "JOIN categorias c ON a.categoria_id = c.id " +
                     "LEFT JOIN categorias p ON c.padre_id = p.id " +
                     "WHERE c.nombre = ? OR p.nombre = ?";
        List<Articulo> result = new ArrayList<>();
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, categoriaNombre);
            ps.setString(2, categoriaNombre);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(reconstructArticulo(rs, conn));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar artículos por categoría: " + categoriaNombre, e);
        }
        return result;
    }

    @Override
    public List<Articulo> findArticulosVendidosSobrePrecioSalida() {
        String sql = "SELECT * FROM articulos WHERE estado_subasta = 'ADJUDICADA' AND precio_final > precio_inicial";
        List<Articulo> result = new ArrayList<>();
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(reconstructArticulo(rs, conn));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar artículos vendidos sobre precio de salida", e);
        }
        return result;
    }

    @Override
    public Map<String, Long> countArticulosPorCategoria() {
        String sql = "SELECT c.nombre, COUNT(a.id) as cantidad " +
                     "FROM categorias c " +
                     "LEFT JOIN articulos a ON a.categoria_id = c.id " +
                     "GROUP BY c.id, c.nombre";
        Map<String, Long> result = new LinkedHashMap<>();
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.put(rs.getString("nombre"), rs.getLong("cantidad"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al contar artículos por categoría", e);
        }
        return result;
    }

    @Override
    public PreciosMinMax obtenerPreciosMinMaxArticulosVendidos() {
        String sql = "SELECT MAX(precio_final) as maximo, MIN(precio_final) as minimo " +
                     "FROM articulos WHERE estado_subasta = 'ADJUDICADA'";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                BigDecimal max = rs.getBigDecimal("maximo");
                BigDecimal min = rs.getBigDecimal("minimo");
                return new PreciosMinMax(
                    max != null ? max : BigDecimal.ZERO,
                    min != null ? min : BigDecimal.ZERO
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al obtener precios min/max de artículos vendidos", e);
        }
        return new PreciosMinMax(BigDecimal.ZERO, BigDecimal.ZERO);
    }

    // ==========================================
    // MÉTODOS AUXILIARES DE RECONSTRUCCIÓN (DOMINIO)
    // ==========================================

    private Articulo reconstructArticulo(ResultSet rs, Connection conn) throws SQLException {
        UUID id = UUID.fromString(rs.getString("id"));
        UUID vendedorId = UUID.fromString(rs.getString("vendedor_id"));
        String nombre = rs.getString("nombre");
        String descripcion = rs.getString("descripcion");
        EstadoArticulo estado = EstadoArticulo.valueOf(rs.getString("estado"));
        Precio precioInicial = new Precio(rs.getBigDecimal("precio_inicial"));
        Instant fechaLimite = rs.getTimestamp("fecha_limite").toInstant();
        UUID categoriaId = UUID.fromString(rs.getString("categoria_id"));

        Usuario vendedor = findUsuarioById(vendedorId, conn);
        Categoria categoria = findCategoriaById(categoriaId, conn);

        // Para pasar la validación de fecha límite en el futuro del constructor de Dominio al reconstruir desde BD,
        // creamos el artículo temporalmente con una fecha futura y luego restauramos la real mediante reflexión.
        Instant tempFecha = Instant.now().plus(1, java.time.temporal.ChronoUnit.DAYS);
        Articulo articulo = new Articulo(id, vendedor, nombre, descripcion, estado, precioInicial, tempFecha, categoria);
        setPrivateField(articulo, "fechaLimite", fechaLimite);

        // Restaurar estado de subasta usando reflexión
        String estadoSubastaStr = rs.getString("estado_subasta");
        if (estadoSubastaStr != null) {
            setPrivateField(articulo, "estadoSubasta", EstadoSubasta.valueOf(estadoSubastaStr));
        }

        // Restaurar adjudicado_a
        String adjIdStr = rs.getString("adjudicado_a_id");
        if (adjIdStr != null) {
            Usuario adjudicadoA = findUsuarioById(UUID.fromString(adjIdStr), conn);
            setPrivateField(articulo, "adjudicadoA", adjudicadoA);
        }

        // Restaurar precio_final
        BigDecimal pf = rs.getBigDecimal("precio_final");
        if (pf != null) {
            setPrivateField(articulo, "precioFinal", new Precio(pf));
        }

        // Cargar y restaurar ofertas
        List<Oferta> ofertas = findOfertasForArticulo(id, conn);
        try {
            Field field = articulo.getClass().getDeclaredField("ofertas");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<Oferta> list = (List<Oferta>) field.get(articulo);
            list.clear();
            list.addAll(ofertas);
        } catch (Exception e) {
            throw new RuntimeException("Error al restaurar ofertas del artículo", e);
        }

        return articulo;
    }

    private Usuario findUsuarioById(UUID id, Connection conn) throws SQLException {
        String sql = "SELECT * FROM usuarios WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Usuario(
                        UUID.fromString(rs.getString("id")),
                        rs.getString("nombre"),
                        rs.getString("apellidos"),
                        rs.getString("direccion"),
                        new Email(rs.getString("email"))
                    );
                }
            }
        }
        return null;
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

    private List<Oferta> findOfertasForArticulo(UUID articuloId, Connection conn) throws SQLException {
        String sql = "SELECT * FROM ofertas WHERE articulo_id = ? ORDER BY momento ASC";
        List<Oferta> result = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, articuloId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    UUID id = UUID.fromString(rs.getString("id"));
                    UUID ofertanteId = UUID.fromString(rs.getString("ofertante_id"));
                    Precio precioPropuesto = new Precio(rs.getBigDecimal("precio_propuesto"));
                    Instant momento = rs.getTimestamp("momento").toInstant();
                    Usuario ofertante = findUsuarioById(ofertanteId, conn);

                    result.add(new Oferta(id, ofertante, precioPropuesto, momento));
                }
            }
        }
        return result;
    }

    private void setPrivateField(Object obj, String fieldName, Object value) {
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(obj, value);
        } catch (Exception e) {
            throw new RuntimeException("Error al establecer el campo privado: " + fieldName, e);
        }
    }
}

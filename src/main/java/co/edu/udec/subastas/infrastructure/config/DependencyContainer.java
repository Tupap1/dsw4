package co.edu.udec.subastas.infrastructure.config;

import co.edu.udec.subastas.application.ports.in.*;
import co.edu.udec.subastas.application.services.*;
import co.edu.udec.subastas.infrastructure.adapters.persistence.*;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.Statement;
import java.util.stream.Collectors;

public class DependencyContainer {
    private final MySQLConnectionFactory connectionFactory;
    
    // Services / Use Cases
    private final ConsultarTotalUsuariosUseCase totalUsuariosUseCase;
    private final ListarArticulosPorCategoriaUseCase articulosPorCategoriaUseCase;
    private final ListarArticulosSobrePrecioSalidaUseCase articulosSobrePrecioSalidaUseCase;
    private final ContarArticulosPorCategoriaUseCase contarArticulosPorCategoriaUseCase;
    private final ObtenerPreciosMinMaxArticulosVendidosUseCase preciosMinMaxArticulosVendidosUseCase;

    public DependencyContainer() {
        // Parametros de conexion por defecto para XAMPP
        String url = "jdbc:mysql://localhost:3306/bd_subastas?allowMultiQueries=true";
        String user = "root";
        String password = "";

        this.connectionFactory = new MySQLConnectionFactory(url, user, password);

        // Repositorios (Adapters)
        MySQLUsuarioRepositoryAdapter usuarioRepo = new MySQLUsuarioRepositoryAdapter(connectionFactory);
        MySQLArticuloRepositoryAdapter articuloRepo = new MySQLArticuloRepositoryAdapter(connectionFactory);

        // Servicios (Application layer)
        this.totalUsuariosUseCase = new ConsultarTotalUsuariosService(usuarioRepo);
        this.articulosPorCategoriaUseCase = new ListarArticulosPorCategoriaService(articuloRepo);
        this.articulosSobrePrecioSalidaUseCase = new ListarArticulosSobrePrecioSalidaService(articuloRepo);
        this.contarArticulosPorCategoriaUseCase = new ContarArticulosPorCategoriaService(articuloRepo);
        this.preciosMinMaxArticulosVendidosUseCase = new ObtenerPreciosMinMaxArticulosVendidosService(articuloRepo);
    }

    /**
     * Metodo de conveniencia para inicializar la base de datos ejecutando el script schema_subastas.sql.
     */
    public void inicializarBaseDatos() {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("schema_subastas.sql")) {
            if (is == null) {
                System.out.println("[WARN] No se encontró el archivo schema_subastas.sql en los recursos.");
                return;
            }
            
            String sql;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                sql = reader.lines().collect(Collectors.joining("\n"));
            }

            String initUrl = "jdbc:mysql://localhost:3306/?allowMultiQueries=true";
            MySQLConnectionFactory initFactory = new MySQLConnectionFactory(initUrl, "root", "");
            try (Connection conn = initFactory.getConnection();
                 Statement stmt = conn.createStatement()) {
                // Ejecuta todo el script (gracias a allowMultiQueries=true en la url)
                stmt.execute(sql);
                System.out.println("[INFO] Base de datos e información semilla inicializadas correctamente.");
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Error al inicializar la base de datos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public ConsultarTotalUsuariosUseCase getTotalUsuariosUseCase() {
        return totalUsuariosUseCase;
    }

    public ListarArticulosPorCategoriaUseCase getArticulosPorCategoriaUseCase() {
        return articulosPorCategoriaUseCase;
    }

    public ListarArticulosSobrePrecioSalidaUseCase getArticulosSobrePrecioSalidaUseCase() {
        return articulosSobrePrecioSalidaUseCase;
    }

    public ContarArticulosPorCategoriaUseCase getContarArticulosPorCategoriaUseCase() {
        return contarArticulosPorCategoriaUseCase;
    }

    public ObtenerPreciosMinMaxArticulosVendidosUseCase getPreciosMinMaxArticulosVendidosUseCase() {
        return preciosMinMaxArticulosVendidosUseCase;
    }
}

package co.edu.udec.subastas.application;

import co.edu.udec.subastas.application.ports.in.*;
import co.edu.udec.subastas.application.ports.in.dto.PreciosMinMax;
import co.edu.udec.subastas.application.ports.out.ArticuloRepositoryPort;
import co.edu.udec.subastas.application.ports.out.UsuarioRepositoryPort;
import co.edu.udec.subastas.application.services.*;
import co.edu.udec.subastas.domain.enums.EstadoArticulo;
import co.edu.udec.subastas.domain.model.Articulo;
import co.edu.udec.subastas.domain.model.Categoria;
import co.edu.udec.subastas.domain.model.Usuario;
import co.edu.udec.subastas.domain.valueobjects.Email;
import co.edu.udec.subastas.domain.valueobjects.Precio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

class SubastasUseCasesTest {

    private UsuarioRepositoryPort mockUsuarioRepo;
    private ArticuloRepositoryPort mockArticuloRepo;

    // Use cases under test
    private ConsultarTotalUsuariosUseCase totalUsuariosUseCase;
    private ListarArticulosPorCategoriaUseCase articulosPorCategoriaUseCase;
    private ListarArticulosSobrePrecioSalidaUseCase articulosSobrePrecioSalidaUseCase;
    private ContarArticulosPorCategoriaUseCase contarArticulosPorCategoriaUseCase;
    private ObtenerPreciosMinMaxArticulosVendidosUseCase preciosMinMaxArticulosVendidosUseCase;

    private Usuario testUser;
    private Categoria electronica;
    private Articulo testArticulo;

    @BeforeEach
    void setUp() {
        testUser = new Usuario(UUID.randomUUID(), "Andres", "Cantillo", "Cartagena", new Email("andres@correo.com"));
        electronica = new Categoria(UUID.randomUUID(), "Electrónica", null);
        testArticulo = new Articulo(
                UUID.randomUUID(),
                testUser,
                "Computador",
                "Laptop",
                EstadoArticulo.NUEVO,
                new Precio(BigDecimal.valueOf(1000.00)),
                Instant.now().plusSeconds(3600),
                electronica
        );

        // Mock simple de repositorio de usuarios
        mockUsuarioRepo = () -> 42L;

        // Mock simple de repositorio de artículos
        mockArticuloRepo = new ArticuloRepositoryPort() {
            @Override
            public List<Articulo> findByCategoriaNombre(String categoriaNombre) {
                if ("Electrónica".equals(categoriaNombre)) {
                    return Collections.singletonList(testArticulo);
                }
                return Collections.emptyList();
            }

            @Override
            public List<Articulo> findArticulosVendidosSobrePrecioSalida() {
                return Collections.singletonList(testArticulo);
            }

            @Override
            public Map<String, Long> countArticulosPorCategoria() {
                Map<String, Long> counts = new HashMap<>();
                counts.put("Electrónica", 1L);
                return counts;
            }

            @Override
            public PreciosMinMax obtenerPreciosMinMaxArticulosVendidos() {
                return new PreciosMinMax(BigDecimal.valueOf(1500.00), BigDecimal.valueOf(800.00));
            }
        };

        // Inicializar servicios
        totalUsuariosUseCase = new ConsultarTotalUsuariosService(mockUsuarioRepo);
        articulosPorCategoriaUseCase = new ListarArticulosPorCategoriaService(mockArticuloRepo);
        articulosSobrePrecioSalidaUseCase = new ListarArticulosSobrePrecioSalidaService(mockArticuloRepo);
        contarArticulosPorCategoriaUseCase = new ContarArticulosPorCategoriaService(mockArticuloRepo);
        preciosMinMaxArticulosVendidosUseCase = new ObtenerPreciosMinMaxArticulosVendidosService(mockArticuloRepo);
    }

    @Test
    void debeConsultarTotalUsuarios() {
        long total = totalUsuariosUseCase.ejecutar();
        assertThat(total).isEqualTo(42L);
    }

    @Test
    void debeListarArticulosPorCategoria() {
        List<Articulo> result = articulosPorCategoriaUseCase.ejecutar("Electrónica");
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNombre()).isEqualTo("Computador");
    }

    @Test
    void debeListarArticulosSobrePrecioSalida() {
        List<Articulo> result = articulosSobrePrecioSalidaUseCase.ejecutar();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNombre()).isEqualTo("Computador");
    }

    @Test
    void debeContarArticulosPorCategoria() {
        Map<String, Long> counts = contarArticulosPorCategoriaUseCase.ejecutar();
        assertThat(counts).containsEntry("Electrónica", 1L);
    }

    @Test
    void debeObtenerPreciosMinMaxArticulosVendidos() {
        PreciosMinMax minMax = preciosMinMaxArticulosVendidosUseCase.ejecutar();
        assertThat(minMax.maximo()).isEqualByComparingTo("1500.00");
        assertThat(minMax.minimo()).isEqualByComparingTo("800.00");
    }
}

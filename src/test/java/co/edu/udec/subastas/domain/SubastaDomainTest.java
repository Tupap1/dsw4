package co.edu.udec.subastas.domain;

import co.edu.udec.subastas.domain.enums.EstadoArticulo;
import co.edu.udec.subastas.domain.enums.EstadoSubasta;
import co.edu.udec.subastas.domain.exceptions.SubastaException;
import co.edu.udec.subastas.domain.factory.ArticuloFactory;
import co.edu.udec.subastas.domain.model.Articulo;
import co.edu.udec.subastas.domain.model.Categoria;
import co.edu.udec.subastas.domain.model.Usuario;
import co.edu.udec.subastas.domain.valueobjects.Email;
import co.edu.udec.subastas.domain.valueobjects.Precio;
import co.edu.udec.subastas.domain.services.SubastaDomainService;
import co.edu.udec.subastas.domain.events.OfertaRealizadaEvent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class SubastaDomainTest {

    private Usuario vendedor;
    private Usuario comprador1;
    private Usuario comprador2;
    private Categoria libros;
    private Categoria ficcion;

    @BeforeEach
    void setUp() {
        vendedor = new Usuario(UUID.randomUUID(), "Juan", "Pérez", "Calle 123", new Email("juan@gmail.com"));
        comprador1 = new Usuario(UUID.randomUUID(), "Maria", "Gomez", "Calle 456", new Email("maria@gmail.com"));
        comprador2 = new Usuario(UUID.randomUUID(), "Carlos", "Ruiz", "Calle 789", new Email("carlos@gmail.com"));
        
        libros = new Categoria(UUID.randomUUID(), "Libros", null);
        ficcion = new Categoria(UUID.randomUUID(), "Ficción", libros);
    }

    @Test
    void debeCrearUsuarioYValidarEmail() {
        // Test de éxito
        assertThat(vendedor.getNombre()).isEqualTo("Juan");
        assertThat(vendedor.getEmail().value()).isEqualTo("juan@gmail.com");

        // Test de fallo por formato inválido
        assertThatThrownBy(() -> new Email("correo_invalido"))
            .isInstanceOf(SubastaException.class)
            .hasMessageContaining("formato del correo electrónico es inválido");

        // Test de fallo por valor vacío
        assertThatThrownBy(() -> new Email(""))
            .isInstanceOf(SubastaException.class)
            .hasMessageContaining("no puede estar vacío");
    }

    @Test
    void debePermitirOfertarCorrectamenteYElevarPrecio() {
        Articulo articulo = ArticuloFactory.crearArticuloNuevo(
            vendedor, "Don Quijote", "Primera edición", 100.0, 7, ficcion
        );

        Instant ahora = Instant.now();

        // Primera oferta debe superar el precio inicial (100.0)
        articulo.ofertar(comprador1, new Precio(BigDecimal.valueOf(110.0)), ahora);
        assertThat(articulo.getPrecioMasAlto().amount()).isEqualByComparingTo("110.0");
        assertThat(articulo.getOfertas()).hasSize(1);

        // Segunda oferta debe superar la anterior (110.0)
        articulo.ofertar(comprador2, new Precio(BigDecimal.valueOf(120.0)), ahora.plus(1, ChronoUnit.HOURS));
        assertThat(articulo.getPrecioMasAlto().amount()).isEqualByComparingTo("120.0");
        assertThat(articulo.getOfertas()).hasSize(2);
    }

    @Test
    void debeRechazarOfertaSiEsDelVendedor() {
        Articulo articulo = ArticuloFactory.crearArticuloNuevo(
            vendedor, "Don Quijote", "Primera edición", 100.0, 7, ficcion
        );

        assertThatThrownBy(() -> articulo.ofertar(vendedor, new Precio(BigDecimal.valueOf(150.0)), Instant.now()))
            .isInstanceOf(SubastaException.class)
            .hasMessageContaining("El vendedor no puede ofertar");
    }

    @Test
    void debeRechazarOfertaMenorOIgualALaActual() {
        Articulo articulo = ArticuloFactory.crearArticuloNuevo(
            vendedor, "Don Quijote", "Primera edición", 100.0, 7, ficcion
        );

        Instant ahora = Instant.now();
        articulo.ofertar(comprador1, new Precio(BigDecimal.valueOf(110.0)), ahora);

        // Intentar ofertar menos de la actual
        assertThatThrownBy(() -> articulo.ofertar(comprador2, new Precio(BigDecimal.valueOf(105.0)), ahora.plus(1, ChronoUnit.MINUTES)))
            .isInstanceOf(SubastaException.class)
            .hasMessageContaining("debe superar la oferta más alta actual");
    }

    @Test
    void debeAdjudicarArticuloAlFinalizarFechaLimite() {
        // Creamos artículo que vence en 1 hora
        Articulo articulo = ArticuloFactory.crearArticuloNuevo(
            vendedor, "Don Quijote", "Primera edición", 100.0, 1, ficcion
        );

        Instant momentoOferta = Instant.now();
        articulo.ofertar(comprador1, new Precio(BigDecimal.valueOf(150.0)), momentoOferta);

        // Simular que pasa el tiempo de finalización
        Instant momentoFin = articulo.getFechaLimite().plus(1, ChronoUnit.SECONDS);

        // Adjudicar
        articulo.adjudicar(momentoFin);

        assertThat(articulo.getEstadoSubasta()).isEqualTo(EstadoSubasta.ADJUDICADA);
        assertThat(articulo.getAdjudicadoA().getId()).isEqualTo(comprador1.getId());
        assertThat(articulo.getPrecioFinal().amount()).isEqualByComparingTo("150.0");
    }

    @Test
    void debeCoordinarConServicioYGenerarEvento() {
        Articulo articulo = ArticuloFactory.crearArticuloNuevo(
            vendedor, "Don Quijote", "Primera edición", 100.0, 5, ficcion
        );

        SubastaDomainService service = new SubastaDomainService();
        Instant ahora = Instant.now();
        Precio monto = new Precio(BigDecimal.valueOf(130.0));

        OfertaRealizadaEvent evento = service.registrarOferta(articulo, comprador1, monto, ahora);

        assertThat(evento).isNotNull();
        assertThat(evento.articuloId()).isEqualTo(articulo.getId());
        assertThat(evento.ofertanteId()).isEqualTo(comprador1.getId());
        assertThat(evento.monto()).isEqualByComparingTo("130.0");
        assertThat(articulo.getPrecioMasAlto().amount()).isEqualByComparingTo("130.0");
    }
}

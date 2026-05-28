package co.edu.udec.subastas.domain.factory;

import co.edu.udec.subastas.domain.enums.EstadoArticulo;
import co.edu.udec.subastas.domain.model.Articulo;
import co.edu.udec.subastas.domain.model.Categoria;
import co.edu.udec.subastas.domain.model.Usuario;
import co.edu.udec.subastas.domain.valueobjects.Precio;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class ArticuloFactory {
    public static Articulo crearArticuloNuevo(Usuario vendedor, String nombre, String descripcion, 
                                             double precioInicialDouble, int diasDuracion, Categoria categoria) {
        UUID id = UUID.randomUUID();
        Precio precio = new Precio(BigDecimal.valueOf(precioInicialDouble));
        Instant fechaLimite = Instant.now().plus(java.time.Duration.ofDays(diasDuracion));
        
        return new Articulo(
            id,
            vendedor,
            nombre,
            descripcion,
            EstadoArticulo.NUEVO,
            precio,
            fechaLimite,
            categoria
        );
    }

    public static Articulo crearArticuloUsado(Usuario vendedor, String nombre, String descripcion, 
                                               double precioInicialDouble, int diasDuracion, Categoria categoria) {
        UUID id = UUID.randomUUID();
        Precio precio = new Precio(BigDecimal.valueOf(precioInicialDouble));
        Instant fechaLimite = Instant.now().plus(java.time.Duration.ofDays(diasDuracion));
        
        return new Articulo(
            id,
            vendedor,
            nombre,
            descripcion,
            EstadoArticulo.USADO,
            precio,
            fechaLimite,
            categoria
        );
    }
}

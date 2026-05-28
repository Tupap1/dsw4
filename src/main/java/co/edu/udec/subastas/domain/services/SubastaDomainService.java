package co.edu.udec.subastas.domain.services;

import co.edu.udec.subastas.domain.events.OfertaRealizadaEvent;
import co.edu.udec.subastas.domain.model.Articulo;
import co.edu.udec.subastas.domain.model.Usuario;
import co.edu.udec.subastas.domain.valueobjects.Precio;
import java.time.Instant;
import java.util.UUID;

public class SubastaDomainService {
    public OfertaRealizadaEvent registrarOferta(Articulo articulo, Usuario ofertante, Precio monto, Instant momento) {
        // Ejecutar la lógica de negocio propia del artículo
        articulo.ofertar(ofertante, monto, momento);

        // Retornar el evento de dominio correspondiente
        return new OfertaRealizadaEvent(
            UUID.randomUUID(),
            articulo.getId(),
            ofertante.getId(),
            monto.amount(),
            momento
        );
    }
}

package co.edu.udec.subastas.domain.model;

import co.edu.udec.subastas.domain.valueobjects.Precio;
import co.edu.udec.subastas.domain.exceptions.SubastaException;
import java.time.Instant;
import java.util.UUID;

public class Oferta {
    private final UUID id;
    private final Usuario ofertante;
    private final Precio precioPropuesto;
    private final Instant momento;

    public Oferta(UUID id, Usuario ofertante, Precio precioPropuesto, Instant momento) {
        if (id == null) throw new SubastaException("El ID de la oferta no puede ser nulo");
        if (ofertante == null) throw new SubastaException("El ofertante de la oferta no puede ser nulo");
        if (precioPropuesto == null) throw new SubastaException("El precio propuesto no puede ser nulo");
        if (momento == null) throw new SubastaException("El momento de la oferta no puede ser nulo");

        this.id = id;
        this.ofertante = ofertante;
        this.precioPropuesto = precioPropuesto;
        this.momento = momento;
    }

    public UUID getId() { return id; }
    public Usuario getOfertante() { return ofertante; }
    public Precio getPrecioPropuesto() { return precioPropuesto; }
    public Instant getMomento() { return momento; }
}

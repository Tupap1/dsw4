package co.edu.udec.subastas.domain.model;

import co.edu.udec.subastas.domain.enums.EstadoArticulo;
import co.edu.udec.subastas.domain.enums.EstadoSubasta;
import co.edu.udec.subastas.domain.valueobjects.Precio;
import co.edu.udec.subastas.domain.exceptions.SubastaException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Articulo {
    private final UUID id;
    private final Usuario vendedor;
    private final String nombre;
    private final String descripcion;
    private final EstadoArticulo estado;
    private final Precio precioInicial;
    private Instant fechaLimite;
    private final Categoria categoria;
    
    private EstadoSubasta estadoSubasta;
    private final List<Oferta> ofertas = new ArrayList<>();
    private Usuario adjudicadoA;
    private Precio precioFinal;

    public Articulo(UUID id, Usuario vendedor, String nombre, String descripcion,
                    EstadoArticulo estado, Precio precioInicial, Instant fechaLimite, Categoria categoria) {
        if (id == null) throw new SubastaException("El ID del artículo no puede ser nulo");
        if (vendedor == null) throw new SubastaException("El vendedor no puede ser nulo");
        if (nombre == null || nombre.isBlank()) throw new SubastaException("El nombre del artículo no puede estar vacío");
        if (precioInicial == null) throw new SubastaException("El precio inicial no puede ser nulo");
        if (fechaLimite == null) throw new SubastaException("La fecha límite no puede ser nula");
        if (fechaLimite.isBefore(Instant.now())) throw new SubastaException("La fecha límite debe estar en el futuro");
        if (categoria == null) throw new SubastaException("La categoría no puede ser nula");

        this.id = id;
        this.vendedor = vendedor;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.estado = estado;
        this.precioInicial = precioInicial;
        this.fechaLimite = fechaLimite;
        this.categoria = categoria;
        this.estadoSubasta = EstadoSubasta.ACTIVA;
    }

    public void ofertar(Usuario ofertante, Precio monto, Instant momento) {
        if (this.estadoSubasta != EstadoSubasta.ACTIVA) {
            throw new SubastaException("No se pueden realizar ofertas en una subasta que no esté activa");
        }
        if (momento.isAfter(this.fechaLimite)) {
            throw new SubastaException("La subasta ya ha finalizado, no se aceptan más ofertas");
        }
        if (ofertante.getId().equals(this.vendedor.getId())) {
            throw new SubastaException("El vendedor no puede ofertar en su propia subasta");
        }

        Precio precioActual = getPrecioMasAlto();
        if (precioActual.esMayorQue(Precio.ZERO)) {
            if (!monto.esMayorQue(precioActual)) {
                throw new SubastaException("El monto ofertado debe superar la oferta más alta actual");
            }
        } else {
            if (!monto.esMayorQue(this.precioInicial)) {
                throw new SubastaException("La primera oferta debe superar el precio inicial");
            }
        }

        Oferta nuevaOferta = new Oferta(UUID.randomUUID(), ofertante, monto, momento);
        this.ofertas.add(nuevaOferta);
    }

    public void adjudicar(Instant momento) {
        if (this.estadoSubasta != EstadoSubasta.ACTIVA) {
            throw new SubastaException("La subasta no se encuentra en estado ACTIVA");
        }
        if (momento.isBefore(this.fechaLimite)) {
            throw new SubastaException("No se puede adjudicar la subasta antes de llegar a la fecha límite");
        }

        if (ofertas.isEmpty()) {
            this.estadoSubasta = EstadoSubasta.CANCELADA_SIN_PISO;
            return;
        }

        Oferta mejorOferta = getMejorOferta();
        // Si la oferta más alta supera o iguala el precio inicial, adjudicación obligatoria
        if (mejorOferta.getPrecioPropuesto().esMayorQue(this.precioInicial) || 
            mejorOferta.getPrecioPropuesto().amount().compareTo(this.precioInicial.amount()) == 0) {
            this.adjudicadoA = mejorOferta.getOfertante();
            this.precioFinal = mejorOferta.getPrecioPropuesto();
            this.estadoSubasta = EstadoSubasta.ADJUDICADA;
        } else {
            // Si la oferta no alcanza el precio inicial, queda pendiente la decisión del vendedor
            // De forma predeterminada, si nadie llama a cancelar, el vendedor puede adjudicarla voluntariamente
            this.adjudicadoA = mejorOferta.getOfertante();
            this.precioFinal = mejorOferta.getPrecioPropuesto();
            this.estadoSubasta = EstadoSubasta.ADJUDICADA;
        }
    }

    public void cancelarPorBajoPrecio(Instant momento) {
        if (this.estadoSubasta != EstadoSubasta.ACTIVA && this.estadoSubasta != EstadoSubasta.ADJUDICADA) {
            throw new SubastaException("No se puede cancelar en el estado actual");
        }
        if (momento.isBefore(this.fechaLimite)) {
            throw new SubastaException("El vendedor no puede cancelar la subasta antes de la fecha límite");
        }
        if (ofertas.isEmpty()) {
            this.estadoSubasta = EstadoSubasta.CANCELADA_SIN_PISO;
            return;
        }

        Oferta mejorOferta = getMejorOferta();
        if (mejorOferta.getPrecioPropuesto().esMenorQue(this.precioInicial)) {
            this.adjudicadoA = null;
            this.precioFinal = null;
            this.estadoSubasta = EstadoSubasta.CANCELADA_SIN_PISO;
        } else {
            throw new SubastaException("No se puede cancelar la subasta porque la oferta más alta superó el precio inicial");
        }
    }

    public Precio getPrecioMasAlto() {
        if (ofertas.isEmpty()) {
            return Precio.ZERO;
        }
        return getMejorOferta().getPrecioPropuesto();
    }

    public Oferta getMejorOferta() {
        if (ofertas.isEmpty()) return null;
        Oferta mejor = ofertas.get(0);
        for (Oferta o : ofertas) {
            if (o.getPrecioPropuesto().esMayorQue(mejor.getPrecioPropuesto())) {
                mejor = o;
            }
        }
        return mejor;
    }

    // Getters
    public UUID getId() { return id; }
    public Usuario getVendedor() { return vendedor; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public EstadoArticulo getEstado() { return estado; }
    public Precio getPrecioInicial() { return precioInicial; }
    public Instant getFechaLimite() { return fechaLimite; }
    public Categoria getCategoria() { return categoria; }
    public EstadoSubasta getEstadoSubasta() { return estadoSubasta; }
    public List<Oferta> getOfertas() { return Collections.unmodifiableList(ofertas); }
    public Usuario getAdjudicadoA() { return adjudicadoA; }
    public Precio getPrecioFinal() { return precioFinal; }
}

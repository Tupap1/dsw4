package co.edu.udec.subastas.domain.events;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OfertaRealizadaEvent(
    UUID id,
    UUID articuloId,
    UUID ofertanteId,
    BigDecimal monto,
    Instant momento
) {}

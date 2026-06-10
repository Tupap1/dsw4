package co.edu.udec.subastas.application.ports.in.dto;

import java.math.BigDecimal;

public record PreciosMinMax(BigDecimal maximo, BigDecimal minimo) {
}

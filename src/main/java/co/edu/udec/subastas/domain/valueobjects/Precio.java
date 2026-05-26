package co.edu.udec.subastas.domain.valueobjects;

import co.edu.udec.subastas.domain.exceptions.SubastaException;
import java.math.BigDecimal;

public record Precio(BigDecimal amount) {
    public static final Precio ZERO = new Precio(BigDecimal.ZERO);

    public Precio {
        if (amount == null) {
            throw new SubastaException("El monto del precio no puede ser nulo");
        }
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new SubastaException("El precio no puede ser negativo");
        }
    }

    public boolean esMayorQue(Precio otro) {
        return this.amount.compareTo(otro.amount) > 0;
    }

    public boolean esMenorQue(Precio otro) {
        return this.amount.compareTo(otro.amount) < 0;
    }
}

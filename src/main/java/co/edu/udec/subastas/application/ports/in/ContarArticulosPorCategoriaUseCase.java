package co.edu.udec.subastas.application.ports.in;

import java.util.Map;

public interface ContarArticulosPorCategoriaUseCase {
    Map<String, Long> ejecutar();
}

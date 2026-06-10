package co.edu.udec.subastas.application.ports.in;

import co.edu.udec.subastas.domain.model.Articulo;
import java.util.List;

public interface ListarArticulosPorCategoriaUseCase {
    List<Articulo> ejecutar(String categoriaNombre);
}

package co.edu.udec.subastas.application.ports.out;

import co.edu.udec.subastas.domain.model.Articulo;
import co.edu.udec.subastas.application.ports.in.dto.PreciosMinMax;
import java.util.List;
import java.util.Map;

public interface ArticuloRepositoryPort {
    List<Articulo> findByCategoriaNombre(String categoriaNombre);
    List<Articulo> findArticulosVendidosSobrePrecioSalida();
    Map<String, Long> countArticulosPorCategoria();
    PreciosMinMax obtenerPreciosMinMaxArticulosVendidos();
}

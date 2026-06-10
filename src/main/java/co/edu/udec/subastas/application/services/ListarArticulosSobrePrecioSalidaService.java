package co.edu.udec.subastas.application.services;

import co.edu.udec.subastas.application.ports.in.ListarArticulosSobrePrecioSalidaUseCase;
import co.edu.udec.subastas.application.ports.out.ArticuloRepositoryPort;
import co.edu.udec.subastas.domain.model.Articulo;
import java.util.List;

public class ListarArticulosSobrePrecioSalidaService implements ListarArticulosSobrePrecioSalidaUseCase {
    private final ArticuloRepositoryPort articuloRepository;

    public ListarArticulosSobrePrecioSalidaService(ArticuloRepositoryPort articuloRepository) {
        this.articuloRepository = articuloRepository;
    }

    @Override
    public List<Articulo> ejecutar() {
        return articuloRepository.findArticulosVendidosSobrePrecioSalida();
    }
}

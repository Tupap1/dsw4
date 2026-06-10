package co.edu.udec.subastas.application.services;

import co.edu.udec.subastas.application.ports.in.ContarArticulosPorCategoriaUseCase;
import co.edu.udec.subastas.application.ports.out.ArticuloRepositoryPort;
import java.util.Map;

public class ContarArticulosPorCategoriaService implements ContarArticulosPorCategoriaUseCase {
    private final ArticuloRepositoryPort articuloRepository;

    public ContarArticulosPorCategoriaService(ArticuloRepositoryPort articuloRepository) {
        this.articuloRepository = articuloRepository;
    }

    @Override
    public Map<String, Long> ejecutar() {
        return articuloRepository.countArticulosPorCategoria();
    }
}

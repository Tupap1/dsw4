package co.edu.udec.subastas.application.services;

import co.edu.udec.subastas.application.ports.in.ListarArticulosPorCategoriaUseCase;
import co.edu.udec.subastas.application.ports.out.ArticuloRepositoryPort;
import co.edu.udec.subastas.domain.model.Articulo;
import java.util.List;

public class ListarArticulosPorCategoriaService implements ListarArticulosPorCategoriaUseCase {
    private final ArticuloRepositoryPort articuloRepository;

    public ListarArticulosPorCategoriaService(ArticuloRepositoryPort articuloRepository) {
        this.articuloRepository = articuloRepository;
    }

    @Override
    public List<Articulo> ejecutar(String categoriaNombre) {
        if (categoriaNombre == null || categoriaNombre.isBlank()) {
            throw new IllegalArgumentException("El nombre de la categoría no puede estar vacío");
        }
        return articuloRepository.findByCategoriaNombre(categoriaNombre);
    }
}

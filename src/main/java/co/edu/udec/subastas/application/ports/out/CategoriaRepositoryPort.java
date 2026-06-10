package co.edu.udec.subastas.application.ports.out;

import co.edu.udec.subastas.domain.model.Categoria;
import java.util.Optional;
import java.util.UUID;

public interface CategoriaRepositoryPort {
    Optional<Categoria> findById(UUID id);
}

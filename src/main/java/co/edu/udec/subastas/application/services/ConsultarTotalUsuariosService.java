package co.edu.udec.subastas.application.services;

import co.edu.udec.subastas.application.ports.in.ConsultarTotalUsuariosUseCase;
import co.edu.udec.subastas.application.ports.out.UsuarioRepositoryPort;

public class ConsultarTotalUsuariosService implements ConsultarTotalUsuariosUseCase {
    private final UsuarioRepositoryPort usuarioRepository;

    public ConsultarTotalUsuariosService(UsuarioRepositoryPort usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public long ejecutar() {
        return usuarioRepository.countTotalUsuarios();
    }
}

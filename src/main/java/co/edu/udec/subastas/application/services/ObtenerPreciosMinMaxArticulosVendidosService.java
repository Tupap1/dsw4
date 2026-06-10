package co.edu.udec.subastas.application.services;

import co.edu.udec.subastas.application.ports.in.ObtenerPreciosMinMaxArticulosVendidosUseCase;
import co.edu.udec.subastas.application.ports.in.dto.PreciosMinMax;
import co.edu.udec.subastas.application.ports.out.ArticuloRepositoryPort;

public class ObtenerPreciosMinMaxArticulosVendidosService implements ObtenerPreciosMinMaxArticulosVendidosUseCase {
    private final ArticuloRepositoryPort articuloRepository;

    public ObtenerPreciosMinMaxArticulosVendidosService(ArticuloRepositoryPort articuloRepository) {
        this.articuloRepository = articuloRepository;
    }

    @Override
    public PreciosMinMax ejecutar() {
        return articuloRepository.obtenerPreciosMinMaxArticulosVendidos();
    }
}

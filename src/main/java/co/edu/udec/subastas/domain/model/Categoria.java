package co.edu.udec.subastas.domain.model;

import co.edu.udec.subastas.domain.exceptions.SubastaException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Categoria {
    private final UUID id;
    private final String nombre;
    private final Categoria padre;
    private final List<Categoria> subcategorias = new ArrayList<>();

    public Categoria(UUID id, String nombre, Categoria padre) {
        if (id == null) throw new SubastaException("El ID de la categoría no puede ser nulo");
        if (nombre == null || nombre.isBlank()) throw new SubastaException("El nombre de la categoría no puede ser vacío");

        this.id = id;
        this.nombre = nombre;
        this.padre = padre;

        if (padre != null) {
            padre.addSubcategoria(this);
        }
    }

    private void addSubcategoria(Categoria sub) {
        if (sub == null) throw new SubastaException("La subcategoría no puede ser nula");
        this.subcategorias.add(sub);
    }

    public UUID getId() { return id; }
    public String getNombre() { return nombre; }
    public Categoria getPadre() { return padre; }
    public List<Categoria> getSubcategorias() { return Collections.unmodifiableList(subcategorias); }
}

package co.edu.udec.subastas.domain.model;

import co.edu.udec.subastas.domain.valueobjects.Email;
import co.edu.udec.subastas.domain.exceptions.SubastaException;
import java.util.UUID;

public class Usuario {
    private final UUID id;
    private String nombre;
    private String apellidos;
    private String direccion;
    private Email email;

    public Usuario(UUID id, String nombre, String apellidos, String direccion, Email email) {
        if (id == null) throw new SubastaException("El identificador del usuario no puede ser nulo");
        if (nombre == null || nombre.isBlank()) throw new SubastaException("El nombre no puede ser vacío");
        if (apellidos == null || apellidos.isBlank()) throw new SubastaException("Los apellidos no pueden ser vacíos");
        if (direccion == null || direccion.isBlank()) throw new SubastaException("La dirección no puede ser vacía");
        if (email == null) throw new SubastaException("El correo electrónico no puede ser nulo");

        this.id = id;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.direccion = direccion;
        this.email = email;
    }

    public void actualizarPerfil(String nombre, String apellidos, String direccion, Email email) {
        if (nombre == null || nombre.isBlank()) throw new SubastaException("El nombre no puede ser vacío");
        if (apellidos == null || apellidos.isBlank()) throw new SubastaException("Los apellidos no pueden ser vacíos");
        if (direccion == null || direccion.isBlank()) throw new SubastaException("La dirección no puede ser vacía");
        if (email == null) throw new SubastaException("El correo electrónico no puede ser nulo");

        this.nombre = nombre;
        this.apellidos = apellidos;
        this.direccion = direccion;
        this.email = email;
    }

    public UUID getId() { return id; }
    public String getNombre() { return nombre; }
    public String getApellidos() { return apellidos; }
    public String getDireccion() { return direccion; }
    public Email getEmail() { return email; }
}

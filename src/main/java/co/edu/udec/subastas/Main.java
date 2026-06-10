package co.edu.udec.subastas;

import co.edu.udec.subastas.infrastructure.config.DependencyContainer;
import co.edu.udec.subastas.infrastructure.entrypoints.cli.SubastasCli;

public class Main {
    public static void main(String[] args) {
        System.out.println("[INFO] Iniciando aplicación de Subastas...");
        
        // Inicializar contenedor de dependencias
        DependencyContainer container = new DependencyContainer();
        
        // Iniciar interfaz de línea de comandos CLI
        SubastasCli cli = new SubastasCli(container);
        cli.iniciar();
    }
}

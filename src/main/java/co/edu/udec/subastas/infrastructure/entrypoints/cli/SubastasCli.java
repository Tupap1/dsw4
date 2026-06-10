package co.edu.udec.subastas.infrastructure.entrypoints.cli;

import co.edu.udec.subastas.application.ports.in.*;
import co.edu.udec.subastas.application.ports.in.dto.PreciosMinMax;
import co.edu.udec.subastas.domain.model.Articulo;
import co.edu.udec.subastas.infrastructure.config.DependencyContainer;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class SubastasCli {
    private final DependencyContainer container;
    private final Scanner scanner;
    private final DateTimeFormatter dateFormatter;

    public SubastasCli(DependencyContainer container) {
        this.container = container;
        this.scanner = new Scanner(System.in);
        this.dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneId.systemDefault());
    }

    public void iniciar() {
        boolean salir = false;
        while (!salir) {
            imprimirMenu();
            System.out.print("\nSeleccione una opción: ");
            String input = scanner.nextLine().trim();
            System.out.println();
            switch (input) {
                case "1":
                    inicializarBaseDatos();
                    break;
                case "2":
                    ejecutarConsulta35();
                    break;
                case "3":
                    ejecutarConsulta36();
                    break;
                case "4":
                    ejecutarConsulta40();
                    break;
                case "5":
                    ejecutarConsulta46();
                    break;
                case "6":
                    ejecutarConsulta47();
                    break;
                case "7":
                    System.out.println("Saliendo de la aplicación. ¡Hasta pronto!");
                    salir = true;
                    break;
                default:
                    System.out.println("[ERROR] Opción no válida. Intente de nuevo.");
            }
            if (!salir) {
                System.out.println("\nPresione ENTER para continuar...");
                scanner.nextLine();
            }
        }
    }

    private void imprimirMenu() {
        System.out.println("=================================================================");
        System.out.println("          SISTEMA DE SUBASTAS ONLINE (ARQ. HEXAGONAL)            ");
        System.out.println("                       ACTIVIDAD UNIDAD 4                        ");
        System.out.println("=================================================================");
        System.out.println("1. Inicializar / Cargar Base de Datos (Tablas y Semilla)");
        System.out.println("2. Consulta 35: Número total de usuarios registrados");
        System.out.println("3. Consulta 36: Listar artículos de la categoría 'Electrónica'");
        System.out.println("4. Consulta 40: Listar artículos vendidos sobre precio de salida");
        System.out.println("5. Consulta 46: Cantidad de artículos por categoría");
        System.out.println("6. Consulta 47: Consultar precio máximo y mínimo vendido");
        System.out.println("7. Salir");
        System.out.println("=================================================================");
    }

    private void inicializarBaseDatos() {
        System.out.println("Inicializando base de datos MySQL...");
        container.inicializarBaseDatos();
    }

    private void ejecutarConsulta35() {
        System.out.println(">> EJECUTANDO CONSULTA 35: Número total de usuarios registrados...");
        ConsultarTotalUsuariosUseCase useCase = container.getTotalUsuariosUseCase();
        try {
            long total = useCase.ejecutar();
            System.out.println("-----------------------------------------------------------------");
            System.out.printf("   Total de Usuarios Registrados: %d\n", total);
            System.out.println("-----------------------------------------------------------------");
        } catch (Exception e) {
            System.err.println("[ERROR] No se pudo ejecutar la consulta. ¿Inicializó la base de datos? Detalles: " + e.getMessage());
        }
    }

    private void ejecutarConsulta36() {
        System.out.println(">> EJECUTANDO CONSULTA 36: Artículos en la categoría 'Electrónica'...");
        ListarArticulosPorCategoriaUseCase useCase = container.getArticulosPorCategoriaUseCase();
        try {
            List<Articulo> articulos = useCase.ejecutar("Electrónica");
            if (articulos.isEmpty()) {
                System.out.println("No se encontraron artículos en la categoría 'Electrónica'.");
                return;
            }
            imprimirTablaArticulos(articulos);
        } catch (Exception e) {
            System.err.println("[ERROR] No se pudo ejecutar la consulta. Detalles: " + e.getMessage());
        }
    }

    private void ejecutarConsulta40() {
        System.out.println(">> EJECUTANDO CONSULTA 40: Artículos vendidos sobre su precio de salida...");
        ListarArticulosSobrePrecioSalidaUseCase useCase = container.getArticulosSobrePrecioSalidaUseCase();
        try {
            List<Articulo> articulos = useCase.ejecutar();
            if (articulos.isEmpty()) {
                System.out.println("No se encontraron artículos vendidos por encima del precio inicial.");
                return;
            }
            System.out.println("-------------------------------------------------------------------------------------------------------------------------");
            System.out.printf("| %-36s | %-20s | %-12s | %-12s | %-12s | %-20s |\n", 
                    "ID ARTÍCULO", "NOMBRE", "PRECIO INIC.", "PRECIO FINAL", "DIFERENCIA", "COMPRADOR");
            System.out.println("-------------------------------------------------------------------------------------------------------------------------");
            for (Articulo art : articulos) {
                BigDecimal inicial = art.getPrecioInicial().amount();
                BigDecimal finalPrice = art.getPrecioFinal().amount();
                BigDecimal dif = finalPrice.subtract(inicial);
                String comprador = art.getAdjudicadoA() != null ? 
                        art.getAdjudicadoA().getNombre() + " " + art.getAdjudicadoA().getApellidos() : "N/A";
                
                System.out.printf("| %-36s | %-20s | $%10.2f | $%10.2f | $%10.2f | %-20s |\n",
                        art.getId().toString(),
                        cortarCadena(art.getNombre(), 20),
                        inicial,
                        finalPrice,
                        dif,
                        cortarCadena(comprador, 20));
            }
            System.out.println("-------------------------------------------------------------------------------------------------------------------------");
        } catch (Exception e) {
            System.err.println("[ERROR] No se pudo ejecutar la consulta. Detalles: " + e.getMessage());
        }
    }

    private void ejecutarConsulta46() {
        System.out.println(">> EJECUTANDO CONSULTA 46: Cantidad de artículos por categoría...");
        ContarArticulosPorCategoriaUseCase useCase = container.getContarArticulosPorCategoriaUseCase();
        try {
            Map<String, Long> conteos = useCase.ejecutar();
            if (conteos.isEmpty()) {
                System.out.println("No hay categorías ni artículos registrados.");
                return;
            }
            System.out.println("--------------------------------------------------");
            System.out.printf("| %-30s | %-13s |\n", "CATEGORÍA", "CANT. ARTÍCULOS");
            System.out.println("--------------------------------------------------");
            for (Map.Entry<String, Long> entry : conteos.entrySet()) {
                System.out.printf("| %-30s | %-13d |\n", 
                        cortarCadena(entry.getKey(), 30), 
                        entry.getValue());
            }
            System.out.println("--------------------------------------------------");
        } catch (Exception e) {
            System.err.println("[ERROR] No se pudo ejecutar la consulta. Detalles: " + e.getMessage());
        }
    }

    private void ejecutarConsulta47() {
        System.out.println(">> EJECUTANDO CONSULTA 47: Precio máximo y mínimo de los artículos vendidos...");
        ObtenerPreciosMinMaxArticulosVendidosUseCase useCase = container.getPreciosMinMaxArticulosVendidosUseCase();
        try {
            PreciosMinMax precios = useCase.ejecutar();
            System.out.println("--------------------------------------------------");
            System.out.printf("   Precio Máximo Adjudicado: $%10.2f\n", precios.maximo());
            System.out.printf("   Precio Mínimo Adjudicado: $%10.2f\n", precios.minimo());
            System.out.println("--------------------------------------------------");
        } catch (Exception e) {
            System.err.println("[ERROR] No se pudo ejecutar la consulta. Detalles: " + e.getMessage());
        }
    }

    private void imprimirTablaArticulos(List<Articulo> articulos) {
        System.out.println("-------------------------------------------------------------------------------------------------------------------------");
        System.out.printf("| %-36s | %-20s | %-12s | %-20s | %-12s | %-12s |\n", 
                "ID ARTÍCULO", "NOMBRE", "ESTADO FIS.", "FECHA LÍMITE", "ESTADO SUB.", "PRECIO INIC.");
        System.out.println("-------------------------------------------------------------------------------------------------------------------------");
        for (Articulo art : articulos) {
            System.out.printf("| %-36s | %-20s | %-12s | %-20s | %-12s | $%10.2f |\n",
                    art.getId().toString(),
                    cortarCadena(art.getNombre(), 20),
                    art.getEstado().name(),
                    dateFormatter.format(art.getFechaLimite()),
                    art.getEstadoSubasta().name(),
                    art.getPrecioInicial().amount());
        }
        System.out.println("-------------------------------------------------------------------------------------------------------------------------");
    }

    private String cortarCadena(String str, int maxLen) {
        if (str == null) return "";
        if (str.length() <= maxLen) return str;
        return str.substring(0, maxLen - 3) + "...";
    }
}

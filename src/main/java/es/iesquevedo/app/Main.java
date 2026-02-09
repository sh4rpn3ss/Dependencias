package es.iesquevedo.app;

import es.iesquevedo.dao.JsonAlquilerDao;
import es.iesquevedo.dao.JsonPeliculaDao;
import es.iesquevedo.dao.JsonSocioDao;
import es.iesquevedo.dao.AlquilerDao;
import es.iesquevedo.dao.PeliculaDao;
import es.iesquevedo.dao.SocioDao;
import es.iesquevedo.servicios.AlquilerService;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        String base = System.getProperty("user.dir");

        AlquilerDao alquilerDao = new JsonAlquilerDao(base + "/alquileres.json");
        PeliculaDao peliculaDao = new JsonPeliculaDao(base + "/peliculas.json");
        SocioDao socioDao = new JsonSocioDao(base + "/socios.json");

        AlquilerService service = new AlquilerService(alquilerDao, peliculaDao, socioDao, 3);

        ConsoleApp app = new ConsoleApp(peliculaDao, socioDao, service);

        try (Scanner scanner = new Scanner(System.in)) {
            boolean running = true;
            while (running) {
                printMenu();
                System.out.print("Elige una opción: ");
                String opt = scanner.nextLine().trim();
                switch (opt) {
                    case "1" -> app.crearPelicula(scanner);
                    case "2" -> app.listarPeliculas();
                    case "3" -> app.crearSocio(scanner);
                    case "4" -> app.listarSocios();
                    case "5" -> app.alquilar(scanner);
                    case "6" -> app.listarAlquileres();
                    case "7" -> app.listarAlquileresPorSocio(scanner);
                    case "8" -> app.devolver(scanner);
                    case "0" -> {
                        running = false;
                        System.out.println("Saliendo...");
                    }
                    default -> System.out.println("Opción no válida");
                }
                System.out.println();
            }
        }
    }

    private static void printMenu() {
        System.out.println("--- Videoclub ---");
        System.out.println("1) Añadir/Actualizar Película");
        System.out.println("2) Listar Películas");
        System.out.println("3) Añadir/Actualizar Socio");
        System.out.println("4) Listar Socios");
        System.out.println("5) Alquilar película");
        System.out.println("6) Listar todos los alquileres");
        System.out.println("7) Listar alquileres por socio");
        System.out.println("8) Devolver alquiler (por id)");
        System.out.println("0) Salir");
    }
}


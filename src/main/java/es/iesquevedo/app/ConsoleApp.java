package es.iesquevedo.app;

import es.iesquevedo.dao.PeliculaDao;
import es.iesquevedo.dao.SocioDao;
import es.iesquevedo.modelo.Alquiler;
import es.iesquevedo.modelo.Pelicula;
import es.iesquevedo.modelo.Socio;
import es.iesquevedo.servicios.AlquilerService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class ConsoleApp {

    private final PeliculaDao peliculaDao;
    private final SocioDao socioDao;
    private final AlquilerService service;

    public ConsoleApp(
            PeliculaDao peliculaDao,
            SocioDao socioDao,
            AlquilerService service
    ) {
        this.peliculaDao = peliculaDao;
        this.socioDao = socioDao;
        this.service = service;
    }

    public void crearPelicula(Scanner scanner) {
        System.out.print("Título: ");
        String titulo = scanner.nextLine().trim();
        if (titulo.isEmpty()) { System.out.println("Título vacío"); return; }

        System.out.print("Año: ");
        int anio;
        try {
            anio = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Año no válido"); return;
        }

        System.out.print("Precio (ej. 2.5): ");
        BigDecimal precio;
        try {
            precio = new BigDecimal(scanner.nextLine().trim());
        } catch (Exception e) {
            System.out.println("Precio no válido"); return;
        }

        Pelicula p = new Pelicula(titulo, anio, precio);
        peliculaDao.save(p);
        System.out.println("Película guardada: " + p);
    }

    public void listarPeliculas() {
        List<Pelicula> list = peliculaDao.findAll();
        if (list.isEmpty()) System.out.println("No hay películas");
        else list.forEach(System.out::println);
    }

    public void crearSocio(Scanner scanner) {
        System.out.print("DNI: ");
        String dni = scanner.nextLine().trim();
        if (dni.isEmpty()) { System.out.println("DNI vacío"); return; }

        System.out.print("Nombre: ");
        String nombre = scanner.nextLine().trim();
        if (nombre.isEmpty()) { System.out.println("Nombre vacío"); return; }

        Socio s = new Socio(dni, nombre);
        socioDao.save(s);
        System.out.println("Socio guardado: " + s);
    }

    public void listarSocios() {
        List<Socio> list = socioDao.findAll();
        if (list.isEmpty()) System.out.println("No hay socios");
        else list.forEach(System.out::println);
    }

    public void alquilar(Scanner scanner) {
        System.out.print("DNI socio: ");
        String dni = scanner.nextLine().trim();
        System.out.print("Título película: ");
        String titulo = scanner.nextLine().trim();

        try {
            Optional<Pelicula> optEj = peliculaDao.findAvailableByTitulo(titulo);
            if (optEj.isEmpty()) {
                System.out.println("Película no encontrada o no disponible");
                return;
            }

            Socio s = new Socio(dni, "");
            Pelicula p = optEj.get();
            Alquiler alquiler = service.alquilar(s, p);
            System.out.println("Alquilado: " + alquiler);

        } catch (RuntimeException e) {
            System.out.println("No se pudo alquilar: " + e.getMessage());
        }
    }

    public void listarAlquileres() {
        List<Alquiler> list = service.listarAlquileres();
        if (list.isEmpty()) System.out.println("No hay alquileres");
        else list.forEach(System.out::println);
    }

    public void listarAlquileresPorSocio(Scanner scanner) {
        System.out.print("DNI socio: ");
        String dni = scanner.nextLine().trim();
        List<Alquiler> list = service.listarPorSocio(dni);
        if (list.isEmpty()) System.out.println("No hay alquileres para ese socio");
        else list.forEach(System.out::println);
    }

    public void devolver(Scanner scanner) {
        System.out.print("ID alquiler a devolver: ");
        String s = scanner.nextLine().trim();
        try {
            Long id = Long.parseLong(s);
            service.devolver(id);
            System.out.println("Devolución realizada");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}

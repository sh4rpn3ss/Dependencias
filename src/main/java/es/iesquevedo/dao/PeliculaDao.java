package es.iesquevedo.dao;

import es.iesquevedo.modelo.Pelicula;

import java.util.List;
import java.util.Optional;

public interface PeliculaDao {
    Pelicula save(Pelicula pelicula);
    Optional<Pelicula> findById(Long id);
    List<Pelicula> findByTitulo(String titulo);
    Optional<Pelicula> findAvailableByTitulo(String titulo);
    List<Pelicula> findAll();
}

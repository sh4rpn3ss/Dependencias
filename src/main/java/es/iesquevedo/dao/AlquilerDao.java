package es.iesquevedo.dao;

import es.iesquevedo.modelo.Alquiler;

import java.util.List;
import java.util.Optional;

public interface AlquilerDao {
    Alquiler save(Alquiler alquiler);
    Optional<Alquiler> findById(Long id);
    List<Alquiler> findAll();
    List<Alquiler> findBySocio(String dni);
}

package es.iesquevedo.dao;

import es.iesquevedo.modelo.Socio;

import java.util.List;
import java.util.Optional;

public interface SocioDao {
    Socio save(Socio socio);
    Optional<Socio> findByDni(String dni);
    List<Socio> findAll();
}

package com.ivan.gimnasio.service.interfaces;

import com.ivan.gimnasio.persistence.entity.Membresia;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

public interface IMembresiaService {
    void crearMembresia(Membresia membresia);
    List<Membresia> obtenerTodas();
    Optional<Membresia> buscarPorNombre(String nombre);
    void eliminar(Long id);
    void actualizar(Membresia membresia);
    void darDeBajaMembresia(Long id);
    List<Membresia> listarMembresiasActivas();
    void restaurarTodasLasMembresias();
}

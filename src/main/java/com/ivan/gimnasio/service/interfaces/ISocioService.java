package com.ivan.gimnasio.service.interfaces;

import com.ivan.gimnasio.persistence.entity.Membresia;
import com.ivan.gimnasio.persistence.entity.Socio;
import java.util.List;
import java.util.Optional;

public interface ISocioService {
    Socio registrarSocio(Socio socio);
    List<Socio> listarSocios();
    Socio buscarSocioPorDni(String dni);
    Socio obtenerSocioConMembresias(String id);
     void asignarMembresiasASocio(Long socioId, List<Long> idsMembresias);
    List<Socio> listarSociosPorMembresia(Membresia membresia);
    void eliminarSocio(Socio socio);
    void darDeBaja(Long id);
    void restaurarSocio(Socio socio);
    List<Socio> buscarSociosPorCriterio(String criterio);
    Optional<Socio> buscarSocioPorDniOptional(String dni);
    List<Socio> listarSociosConCuotaVencida();

//    void restaurarTodos();
}

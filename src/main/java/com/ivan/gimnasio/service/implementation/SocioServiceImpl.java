package com.ivan.gimnasio.service.implementation;

import com.ivan.gimnasio.persistence.entity.Membresia;
import com.ivan.gimnasio.persistence.entity.Socio;
import com.ivan.gimnasio.persistence.repository.MembresiaRepository;
import com.ivan.gimnasio.persistence.repository.SocioRepository;
import com.ivan.gimnasio.service.interfaces.ISocioService;
import com.ivan.gimnasio.util.EstadoCuota;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
public class SocioServiceImpl implements ISocioService {
    @Autowired
    private SocioRepository socioRepository;

    @Autowired
    private MembresiaRepository membresiaRepository;

    @Override
    public Socio registrarSocio(Socio socio) {
        return socioRepository.save(socio);
    }

    @Override
    public List<Socio> listarSocios() {
        return socioRepository.findByActivoTrue();
    }

    @Override
    public Socio buscarSocioPorDni(String dni) {
        Socio socio =  socioRepository.findByDni(dni).orElse(null);
        return socio;
    }
    @Transactional
    @Override
    public void asignarMembresiasASocio(Long socioId, List<Long> idsMembresias) {
        Socio socio = socioRepository.findById(socioId).orElseThrow();
        List<Membresia> membresias = membresiaRepository.findAllById(idsMembresias);

        for (Membresia m : membresias) {
            socio.getMembresias().add(m);   // ⚠ Aquí fallaba por falta de sesión
            m.getSocios().add(socio);       // Si es bidireccional
        }

        socioRepository.save(socio);
    }

    @Override
    public List<Socio> listarSociosPorMembresia(Membresia membresia) {
        return socioRepository.findByMembresiasAndActivoTrue(membresia);
    }

    @Override
    public void eliminarSocio(Socio socio) {
        socio.setActivo(false);
        socioRepository.save(socio);
    }

    @Transactional
    public void darDeBaja(Long socioId) {
        socioRepository.desactivarPorId(socioId);
    }
    @Transactional(readOnly = true)
    public Socio obtenerSocioConMembresias(String id) {
        return socioRepository.findByIdConMembresias(id)
                .orElseThrow(() -> new RuntimeException("Socio no encontrado"));
    }
    @Transactional
    public void restaurarSocio(Socio socio) {
        socio.setActivo(true);
        socioRepository.save(socio);
    }

    @Override
    public List<Socio> buscarSociosPorCriterio(String criterio) {
        return socioRepository.buscarSociosActivosPorCriterio(criterio);
    }

    @Override
    public Optional<Socio> buscarSocioPorDniOptional(String dni) {
        return socioRepository.findByDni(dni);
    }

    @Override
    public List<Socio> listarSociosConCuotaVencida() {
        return socioRepository.findByEstadoCuota(EstadoCuota.VENCIDA);
    }

//    @Transactional
//    public void restaurarTodos() {
//            socioRepository.restaurarTodosLosSociosDadosDeBaja();
//    }

}


package com.ivan.gimnasio.service.implementation;

import com.ivan.gimnasio.persistence.entity.Membresia;
import com.ivan.gimnasio.persistence.entity.Socio;
import com.ivan.gimnasio.persistence.repository.MembresiaRepository;
import com.ivan.gimnasio.persistence.repository.SocioRepository;
import com.ivan.gimnasio.service.interfaces.IMembresiaService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MembresiaServiceImpl implements IMembresiaService {
    @Autowired
    private SocioRepository socioRepository;
     private final MembresiaRepository membresiaRepository;

    public MembresiaServiceImpl(MembresiaRepository membresiaRepository) {
        this.membresiaRepository = membresiaRepository;
    }

    @Override
    public void crearMembresia(Membresia membresia) {
           membresiaRepository.save(membresia);
    }

    @Override
    public List<Membresia> obtenerTodas() {
        return membresiaRepository.findAll();
    }

    @Override
    public Optional<Membresia> buscarPorNombre(String nombre) {
        return membresiaRepository.findByNombre(nombre);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Membresia membresia = membresiaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Membresía no encontrada"));

        // Eliminar referencias desde socios
        for (Socio socio : membresia.getSocios()) {
            socio.getMembresias().remove(membresia);
            // opcional: socioRepository.save(socio); si es necesario
        }

        // Limpiar relaciones para evitar errores
        membresia.getSocios().clear();

        // Ahora sí se puede eliminar
        membresiaRepository.deleteById(id);
    }

    @Override
    public void actualizar(Membresia membresia) {
        Optional<Membresia> existente = membresiaRepository.findById(membresia.getId());
        if (existente.isPresent()) {
            Membresia m = existente.get();
            m.setNombre(membresia.getNombre());
            m.setPrecio(membresia.getPrecio());
            membresiaRepository.save(m);
        } else {
            throw new RuntimeException("Membresía no encontrada con ID: " + membresia.getId());
        }
    }

    @Transactional
    public void darDeBajaMembresia(Long id) {
        membresiaRepository.desactivarPorId(id);
    }

    @Override
    public List<Membresia> listarMembresiasActivas() {
        return membresiaRepository.findByActivoTrue();
    }

    @Override
    @Transactional
    public void restaurarTodasLasMembresias() {
        membresiaRepository.restaurarTodas();
    }

}

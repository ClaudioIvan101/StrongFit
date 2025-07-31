package com.ivan.gimnasio.service.implementation;

import com.ivan.gimnasio.persistence.entity.Asistencia;
import com.ivan.gimnasio.persistence.entity.Socio;
import com.ivan.gimnasio.persistence.repository.AsistenciaRepository;
import com.ivan.gimnasio.persistence.repository.SocioRepository;
import com.ivan.gimnasio.service.interfaces.IAsistenciaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class AsistenciaServiceImpl implements IAsistenciaService {

    private final AsistenciaRepository asistenciaRepository;
    private final SocioRepository socioRepository;
    public AsistenciaServiceImpl(AsistenciaRepository asistenciaRepository, SocioRepository socioRepository) {
        this.asistenciaRepository = asistenciaRepository;
        this.socioRepository = socioRepository;
    }
    @Transactional
    @Override
    public void registrarAsistencia(Socio socio) {
        Socio socioGestionado = socioRepository.findById(socio.getId())
                .orElseThrow(() -> new IllegalArgumentException("Socio no encontrado"));

        LocalDateTime ahora = LocalDateTime.now();
        LocalDate hoyInicio = ahora.toLocalDate();
        boolean yaAsistioHoy = asistenciaRepository.existsBySocioAndFechaHoraBetween(
                socio,
                hoyInicio.atStartOfDay(),
                hoyInicio.atTime(23, 59, 59)
        );
        if (yaAsistioHoy) {

        }

        // Crear y guardar la asistencia
        Asistencia asistencia = new Asistencia();
        asistencia.setSocio(socio);
        asistencia.setFechaHora(ahora);
        asistenciaRepository.save(asistencia);
        asistenciaRepository.flush();
    }

    @Override
    public List<Asistencia> buscarConSocioYMiembrosEntreFechas(LocalDateTime desde, LocalDateTime hasta) {
        return asistenciaRepository.buscarConSocioYMiembrosEntreFechas(desde, hasta);
    }

    @Override
    public int contarAsistenciasHoy(String dniSocio) {
        LocalDate hoy = LocalDate.now();
        LocalDateTime desde = hoy.atStartOfDay();
        LocalDateTime hasta = hoy.atTime(23, 59, 59);
        return asistenciaRepository.countBySocio_DniAndFechaHoraBetween(dniSocio, desde, hasta);
    }

    @Override
    public int contarAsistenciasPorDniYRangoFecha(String dni, LocalDateTime desde, LocalDateTime hasta) {
        return asistenciaRepository.countBySocio_DniAndFechaHoraBetween(dni, desde, hasta);
    }
    @Override
    public List<LocalDate> obtenerDiasAsistidosSemana(String dni, LocalDateTime desde, LocalDateTime hasta) {
        return asistenciaRepository.findDiasUnicosDeAsistenciaPorSemana(dni, desde, hasta)
                .stream()
                .map(java.sql.Date::toLocalDate)
                .collect(Collectors.toList());
    }
}

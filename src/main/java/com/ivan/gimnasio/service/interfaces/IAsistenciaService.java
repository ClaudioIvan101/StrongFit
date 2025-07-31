package com.ivan.gimnasio.service.interfaces;

import com.ivan.gimnasio.persistence.entity.Asistencia;
import com.ivan.gimnasio.persistence.entity.Socio;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface IAsistenciaService {
    void registrarAsistencia(Socio socio);
    List<Asistencia> buscarConSocioYMiembrosEntreFechas(LocalDateTime desde, LocalDateTime hasta);
    int contarAsistenciasHoy(String dniSocio);
    int contarAsistenciasPorDniYRangoFecha(String dni, LocalDateTime desde, LocalDateTime hasta);
    List<LocalDate> obtenerDiasAsistidosSemana(String dni, LocalDateTime desde, LocalDateTime hasta);
}

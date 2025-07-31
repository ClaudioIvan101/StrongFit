package com.ivan.gimnasio.persistence.repository;

import com.ivan.gimnasio.persistence.entity.Asistencia;
import com.ivan.gimnasio.persistence.entity.Socio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.sql.Date;
import java.util.List;

@Repository
public interface AsistenciaRepository extends JpaRepository<Asistencia, Long> {
     // Para evitar multiples asistencias el mismo dia
     boolean existsBySocioAndFechaHoraBetween(Socio socio, LocalDateTime desde, LocalDateTime hasta);
     @Query("SELECT a FROM Asistencia a " +
             "JOIN FETCH a.socio s " +
             "LEFT JOIN FETCH s.membresias " +
             "WHERE a.fechaHora BETWEEN :desde AND :hasta")
     List<Asistencia> buscarConSocioYMiembrosEntreFechas(@Param("desde") LocalDateTime desde,
                                                         @Param("hasta") LocalDateTime hasta);
     int countBySocio_DniAndFechaHoraBetween(String dni, LocalDateTime desde, LocalDateTime hasta);
     @Query(value = """
    SELECT DISTINCT DATE(a.fecha_hora)
    FROM asistencias a
    JOIN socio s ON a.socio_id = s.id
    WHERE s.dni = :dni AND a.fecha_hora BETWEEN :desde AND :hasta
    """, nativeQuery = true)
     List<java.sql.Date> findDiasUnicosDeAsistenciaPorSemana(
             @Param("dni") String dni,
             @Param("desde") LocalDateTime desde,
             @Param("hasta") LocalDateTime hasta);
     List<Asistencia> findByFechaHoraBetween(LocalDateTime desde, LocalDateTime hasta);
     List<Asistencia> findBySocio(Socio socio);
     List<Asistencia> findBySocioAndFechaHoraBetween(Socio socio, LocalDateTime desde, LocalDateTime hasta);
}

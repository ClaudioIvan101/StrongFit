package com.ivan.gimnasio.persistence.repository;

import com.ivan.gimnasio.persistence.entity.Membresia;
import com.ivan.gimnasio.persistence.entity.Socio;
import com.ivan.gimnasio.util.EstadoCuota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface SocioRepository extends JpaRepository<Socio, Long> {
    Optional<Socio> findByDni(String Dni);
    @Query("SELECT socio FROM Socio socio LEFT JOIN FETCH socio.membresias WHERE socio.dni = :dni")
    Optional<Socio> findByIdConMembresias(@Param("dni") String dni);
    @Query("SELECT s FROM Socio s JOIN s.membresias m WHERE m.id = :membresiaId")
    List<Socio> buscarSociosPorMembresia(@Param("membresiaId") Long membresiaId);
    List<Socio> findByActivoTrue();
    List<Socio> findByMembresiasAndActivoTrue(Membresia membresia);
    @Modifying
    @Query("UPDATE Socio s SET s.activo = false WHERE s.id = :id")
    void desactivarPorId(@Param("id") Long id);
    @Query("SELECT s FROM Socio s WHERE (LOWER(s.nombre) LIKE LOWER(CONCAT('%', :criterio, '%')) " +
            "OR LOWER(s.apellido) LIKE LOWER(CONCAT('%', :criterio, '%')) " +
            "OR s.dni LIKE %:criterio%) AND s.activo = true")
    List<Socio> buscarSociosActivosPorCriterio(@Param("criterio") String criterio);
    List<Socio> findByEstadoCuota(EstadoCuota estadoCuota);
//    @Modifying
//    @Transactional
//    @Query("UPDATE Socio s SET s.activo = true WHERE s.activo = false")
//    void restaurarTodosLosSociosDadosDeBaja();

}

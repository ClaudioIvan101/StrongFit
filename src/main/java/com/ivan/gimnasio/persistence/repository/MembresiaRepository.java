package com.ivan.gimnasio.persistence.repository;

import com.ivan.gimnasio.persistence.entity.Membresia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

@Repository
public interface MembresiaRepository extends JpaRepository<Membresia, Long> {
    Optional<Membresia> findByNombre(String nombre);
    List<Membresia> findByActivoTrue();
    @Modifying
    @Query("UPDATE Membresia m SET m.activo = false WHERE m.id = :id")
    void desactivarPorId(@Param("id") Long id);
    @Modifying
    @Query("UPDATE Membresia m SET m.activo = true WHERE m.activo = false")
    void restaurarTodas();

}

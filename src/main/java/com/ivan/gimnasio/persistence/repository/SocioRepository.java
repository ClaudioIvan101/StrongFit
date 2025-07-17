package com.ivan.gimnasio.persistence.repository;

import com.ivan.gimnasio.persistence.entity.Socio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SocioRepository extends JpaRepository<Socio, Long> {
Optional<Socio> findByDni(String Dni);
}

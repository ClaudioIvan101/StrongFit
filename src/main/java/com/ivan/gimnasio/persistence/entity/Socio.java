package com.ivan.gimnasio.persistence.entity;

import com.ivan.gimnasio.util.EstadoCuota;
import jakarta.persistence.*;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
public class Socio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String apellido;
    private String dni;
    private String email;
    private String telefono;
    private LocalDate fechaInscripcion;
    @Column(nullable = false)
    private boolean activo = true;
    @Enumerated(EnumType.STRING)
    private EstadoCuota estadoCuota;
    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "socio_membresia",
            joinColumns = @JoinColumn(name = "socio_id"),
            inverseJoinColumns = @JoinColumn(name = "membresia_id")
    )
    private Set<Membresia> membresias = new HashSet<>();

    public Set<Membresia> getMembresias() {
        return membresias;
    }

    public void setMembresias(Set<Membresia> membresias) {
        this.membresias = membresias;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public LocalDate getFechaInscripcion() {
        return fechaInscripcion;
    }

    public void setFechaInscripcion(LocalDate fechaInscripcion) {
        this.fechaInscripcion = fechaInscripcion;
    }

    public EstadoCuota getEstadoCuota() {
        if (fechaInscripcion.plusDays(30).isBefore(LocalDate.now())) {
            return EstadoCuota.VENCIDA;
        } else {
            return EstadoCuota.AL_DIA;
        }
    }
    @Override
    public String toString() {
        return nombre + " " + apellido + " (" + dni + ")";
    }
}

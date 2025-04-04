package com.example.ejercicio.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "Usuario")
@Data
public class Usuario {
    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;
    private String nombre;
    private String correo;
    private String password;
    private boolean activo;
    private String token;
    private LocalDateTime creado;
    private LocalDateTime modificado;
    private LocalDateTime ultimoLogin;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Telefono> telefonos;

    public void addPhone(Telefono telefono) {
        this.telefonos.add(telefono);
        telefono.setUsuario(this);
    }

    public void removePhone(Telefono telefono) {
        this.telefonos.remove(telefono);
        telefono.setUsuario(null);
    }
}

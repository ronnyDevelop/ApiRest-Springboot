package com.example.ejercicio.repository;

import com.example.ejercicio.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

    Usuario findByCorreo(String correo);
    boolean existsByCorreo(String correo);
}

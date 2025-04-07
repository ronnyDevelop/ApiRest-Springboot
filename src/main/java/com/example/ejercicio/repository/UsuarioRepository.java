package com.example.ejercicio.repository;

import com.example.ejercicio.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

    Usuario findByCorreo(String correo);
    boolean existsByCorreo(String correo);

}

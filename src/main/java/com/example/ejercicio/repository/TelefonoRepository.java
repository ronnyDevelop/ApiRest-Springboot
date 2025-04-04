package com.example.ejercicio.repository;

import com.example.ejercicio.model.Telefono;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TelefonoRepository extends JpaRepository<Telefono, Long> {
}

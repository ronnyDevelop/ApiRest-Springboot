package com.example.ejercicio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@Data
@Builder(toBuilder = true)
public class CreateResponseDTO {
    private UUID id;
    private String nombre;
    private String correo;
    private LocalDateTime creado;
    private LocalDateTime modificado;
    private LocalDateTime ultimoLogin;
    private String token;
    private boolean isActive;
}

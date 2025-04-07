package com.example.ejercicio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@AllArgsConstructor
@Data
public class AuthRequestDTO {
    private String correo;
    private String password;
}

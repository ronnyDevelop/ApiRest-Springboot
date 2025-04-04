package com.example.ejercicio.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class CreateUserRequestDTO {
    private String nombre;

    private String correo;

    private String password;

    private List<TelefonoDTO> telefonos;

}
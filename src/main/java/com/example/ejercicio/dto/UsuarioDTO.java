package com.example.ejercicio.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@AllArgsConstructor
@Data
public class UsuarioDTO {
    @NotBlank(message = "El nombre no puede estar en blanco")
    private  String nombre;

    @NotBlank(message = "El email no puede estar en blanco")
    @Email(message = "Email debe ser valido")
    private String correo;

    @NotBlank(message = "La contraseña no puede estar en blanco")
    private String password;

    private List<TelefonoDTO> telefonos;
}

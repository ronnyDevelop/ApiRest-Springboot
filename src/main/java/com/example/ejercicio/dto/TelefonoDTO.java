package com.example.ejercicio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TelefonoDTO {
    private String numero;
    private String codigoCiudad;
    private String codigoPais;
}

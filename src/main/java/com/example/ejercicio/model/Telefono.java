package com.example.ejercicio.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Telefono {
    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;
    private String numero;
    private String codigoCiudad;
    private String codigoPais;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private Usuario usuario;

    public Telefono(String numero, String codigoCiudad, String codigoPais, Usuario usuario) {
        this.numero = numero;
        this.codigoCiudad = codigoCiudad;
        this.codigoPais = codigoPais;
        this.usuario = usuario;
    }

}

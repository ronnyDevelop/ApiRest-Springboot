package com.example.ejercicio.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Servicio para codificar contraseñas usando BCrypt.
 */
@Service
public class PasswordEncoderServiceImpl {

    // Instancia de PasswordEncoder para manejar la codificación.
    private final PasswordEncoder passwordEncoder;

    /**
     * Constructor que inicializa el PasswordEncoder con una instancia de BCryptPasswordEncoder.
     */
    public PasswordEncoderServiceImpl() {
        // Crear una nueva instancia de BCryptPasswordEncoder que será usada para codificar las contraseñas.
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    /**
     * Codifica la contraseña proporcionada usando BCrypt.
     *
     * @param password La contraseña que se desea codificar.
     * @return La contraseña codificada.
     */
    public String encodePassword(String password) {
        // Utiliza el passwordEncoder para codificar la contraseña y retornar el valor codificado.
        return passwordEncoder.encode(password);
    }
}

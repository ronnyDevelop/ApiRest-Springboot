package com.example.ejercicio.service;

import com.example.ejercicio.model.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
* Servicio para gestionar los detalles del usuario utilizado en la autenticación.
*/
@Service
public class UsuarioDetalleService implements UserDetailsService {

    // Dependencia al servicio UsuarioService para realizar operaciones relacionadas con los usuarios.
    private final UsuarioService usuarioService;

    /**
     * Constructor que inyecta automáticamente UsuarioService.
     *
     * @param usuarioService el servicio que maneja operaciones de usuario.
     */
    @Autowired
    public UsuarioDetalleService(UsuarioService usuarioService) {
        // Asigna el servicio de usuario inyectado a la propiedad local.
        this.usuarioService = usuarioService;
    }

    /**
     * Carga un usuario por su nombre de usuario (email) para el proceso de autenticación.
     *
     * @param email el correo electrónico del usuario.
     * @return los detalles del usuario autenticado.
     * @throws UsernameNotFoundException si el usuario no es encontrado.
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Busca el usuario en el sistema usando el email proporcionado.
        Usuario usuario = usuarioService.buscarPorEmail(email);

        // Verifica si el usuario existe o no.
        if (usuario == null) {
            // Si no existe, lanza una excepción indicando que el usuario con dicho email no fue encontrado.
            throw new UsernameNotFoundException("Usuario no encontrado con este email: " + email);
        }

        // Retorna un objeto UserDetails con el correo, contraseña y una lista vacía de autoridades.
        return new User(usuario.getCorreo(), usuario.getPassword(), Collections.emptyList());
    }
}

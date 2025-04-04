package com.example.ejercicio.service;

import com.example.ejercicio.model.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class UsuarioDetalleService implements UserDetailsService {
    private final UsuarioService usuarioService;

    @Autowired
    public UsuarioDetalleService(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioService.buscarPorEmail(email);
        if (usuario == null) {
            throw new UsernameNotFoundException("Usuario no encontrado con este email: " + email);
        }
        return new User(usuario.getCorreo(), usuario.getPassword(), Collections.emptyList());
    }
}
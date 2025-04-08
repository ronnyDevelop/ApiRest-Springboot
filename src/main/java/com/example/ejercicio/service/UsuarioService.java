package com.example.ejercicio.service;

import com.example.ejercicio.dto.AuthRequestDTO;
import com.example.ejercicio.dto.CreateResponseDTO;
import com.example.ejercicio.dto.UsuarioDTO;
import com.example.ejercicio.model.Usuario;
import jakarta.security.auth.message.AuthException;
import org.springframework.security.authentication.AuthenticationManager;

import java.util.List;
import java.util.Map;

public interface UsuarioService {
    CreateResponseDTO crearUsuario(UsuarioDTO usuarioDTO);
    CreateResponseDTO login(AuthRequestDTO authRequest, AuthenticationManager authenticationManager) throws AuthException;

    List<CreateResponseDTO> findAll();

    CreateResponseDTO updateUsuario(String id, UsuarioDTO usuarioActualizado);

    boolean deleteUsuario(String id);

    CreateResponseDTO patchUsuario(String id, Map<String, Object> updates);

    Usuario buscarPorEmail(String email);
}

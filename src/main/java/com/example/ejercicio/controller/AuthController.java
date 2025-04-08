package com.example.ejercicio.controller;

import com.example.ejercicio.dto.AuthRequestDTO;
import com.example.ejercicio.dto.UsuarioDTO;
import com.example.ejercicio.exception.UserExistsException;
import com.example.ejercicio.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.security.auth.message.AuthException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("/login")
    public ResponseEntity<?> autenticarUsuario(@Valid @RequestBody AuthRequestDTO authRequest) {
        try {
            return ResponseEntity.ok(usuarioService.login(authRequest, authenticationManager));
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (AuthException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @PostMapping("/create")
    @Operation(summary = "Registrar un nuevo usuario")
    @ApiResponse(responseCode = "200", description = "Usuario registrado con éxito")
    @ApiResponse(responseCode = "400", description = "Formato de correo electrónico o contraseña inválido", content = @Content(schema = @Schema(example = "Formato de correo electrónico inválido")))
    @ApiResponse(responseCode = "409", description = "El correo ya está registrado", content = @Content(schema = @Schema(example = "El correo ya registrado")))
    public ResponseEntity<?> crearUsuario(@RequestBody UsuarioDTO usuarioDTO) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(usuarioService.crearUsuario(usuarioDTO));
        } catch (UserExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}

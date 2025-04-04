package com.example.ejercicio.service;

import com.example.ejercicio.dto.AuthRequestDTO;
import com.example.ejercicio.dto.CreateResponseDTO;
import com.example.ejercicio.model.Telefono;
import com.example.ejercicio.model.Usuario;
import com.example.ejercicio.repository.TelefonoRepository;
import com.example.ejercicio.repository.UsuarioRepository;
import jakarta.security.auth.message.AuthException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TelefonoRepository telefonoRepository;

    @Autowired
    private PasswordEncoderService passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Value("${email.regex}")
    private String emailRegex;

    @Value("${password.regex}")
    private String passwordRegex;
    //POST
    public CreateResponseDTO crearUsuario(Usuario usuario) {
        validarCorreoYContraseña(usuario.getCorreo(), usuario.getPassword());
        String jwtToken = jwtService.generarToken(usuario.getCorreo());

        usuario.setPassword(passwordEncoder.encodePassword(usuario.getPassword()));
        usuario.setCreado(LocalDateTime.now());
        usuario.setUltimoLogin(LocalDateTime.now());
        usuario.setToken(jwtToken);
        usuario.setActivo(true);

        Usuario finalUsuario = usuarioRepository.save(usuario);

        List<Telefono> telefonos = usuario.getTelefonos();
        if (telefonos != null) {
            telefonos.forEach(telefono -> telefono.setUsuario(finalUsuario));
            telefonoRepository.saveAll(telefonos);
        }


        return construirResponseDTO(finalUsuario);
    }

    public CreateResponseDTO login(AuthRequestDTO authRequest, AuthenticationManager authenticationManager) throws AuthException {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getCorreo(), authRequest.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwtToken = jwtService.generarToken(authRequest.getCorreo());
            Usuario usuario = buscarPorEmail(authRequest.getCorreo());
            if (usuario == null) {
                throw new UsernameNotFoundException("Usuario no encontrado");
            }
            usuario.setUltimoLogin(LocalDateTime.now());
            Usuario usuarioGuardado = usuarioRepository.save(usuario);
            return construirResponseDTO(usuarioGuardado).toBuilder().token(jwtToken).build();
        } catch (AuthenticationException e) {
            throw new AuthException("Credenciales inválidas");
        }
    }
    //GET
    public List<CreateResponseDTO> findAll() {
        return usuarioRepository.findAll().stream()
                .map(this::construirResponseDTO)
                .collect(Collectors.toList());
    }
    //PUT
    public CreateResponseDTO updateUsuario(String id, Usuario usuarioActualizado) {
        Usuario usuario = usuarioRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        validarCorreoYContraseña(usuarioActualizado.getCorreo(), usuarioActualizado.getPassword());

        usuario.setNombre(usuarioActualizado.getNombre());
        usuario.setCorreo(usuarioActualizado.getCorreo());
        usuario.setPassword(passwordEncoder.encodePassword(usuarioActualizado.getPassword()));
        usuario.setActivo(usuarioActualizado.isActivo());
        usuario.setToken(usuarioActualizado.getToken());
        usuario.setModificado(LocalDateTime.now());

        // Actualizar teléfonos
        usuario.getTelefonos().clear();
        usuarioActualizado.getTelefonos().forEach(usuario::addPhone);

        Usuario usuarioGuardado = usuarioRepository.save(usuario);
        return construirResponseDTO(usuarioGuardado);
    }
    //DELETE
    public void deleteUsuario(String id) {
        usuarioRepository.deleteById(UUID.fromString(id));
    }
    //Patch
    public CreateResponseDTO patchUsuario(String id, Map<String, Object> updates) {
        Usuario usuario = usuarioRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        updates.forEach((clave, valor) -> {
            switch (clave) {
                case "nombre":
                    usuario.setNombre((String) valor);
                    break;
                case "correo":
                    usuario.setCorreo((String) valor);
                    break;
                case "password":
                    usuario.setPassword(passwordEncoder.encodePassword((String) valor));
                    break;
                case "activo":
                    usuario.setActivo((Boolean) valor);
                    break;
                case "telefonos":
                    List<Telefono> telefonosActualizados = ((List<Map<String, Object>>) valor).stream()
                            .map(dto -> new Telefono(
                                    (String) dto.get("numero"),
                                    (String) dto.get("codigoCiudad"),
                                    (String) dto.get("codigoPais"),
                                    usuario
                            ))
                            .collect(Collectors.toList());
                    // Limpia teléfonos existentes y agregar los nuevos
                    usuario.getTelefonos().clear();
                    usuario.getTelefonos().addAll(telefonosActualizados);
                    break;
            }
        });

        usuario.setModificado(LocalDateTime.now());

        Usuario usuarioGuardado = usuarioRepository.save(usuario);
        return construirResponseDTO(usuarioGuardado);
    }

    public Usuario buscarPorEmail(String email) {
        return usuarioRepository.findByCorreo(email);
    }

    private void validarCorreoYContraseña(String correo, String password) {
        if (usuarioRepository.existsByCorreo(correo)) {
            throw new RuntimeException("El correo ya está registrado");
        }
        if (!Pattern.matches(emailRegex, correo)) {
            throw new IllegalArgumentException("Formato de correo electrónico inválido");
        }
        if (!Pattern.matches(passwordRegex, password)) {
            throw new IllegalArgumentException("Formato de contraseña inválido");
        }
    }

    private CreateResponseDTO construirResponseDTO(Usuario usuario) {
        return CreateResponseDTO.builder()
                .id(usuario.getId())
                .nombre(usuario.getNombre())
                .correo(usuario.getCorreo())
                .creado(usuario.getCreado())
                .ultimoLogin(usuario.getUltimoLogin())
                .isActive(usuario.isActivo())
                .build();
    }
}



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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Servicio para la gestión de usuarios en el sistema.
 */
@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository; // Repositorio para la gestión de entidades Usuario

    @Autowired
    private TelefonoRepository telefonoRepository; // Repositorio para la gestión de entidades Telefono

    @Autowired
    private PasswordEncoderService passwordEncoder; // Servicio para codificar contraseñas

    @Autowired
    private JwtService jwtService; // Servicio para manejar tokens JWT

    @Value("${email.regex}")
    private String emailRegex; // Expresión regular para validar correos electrónicos

    @Value("${password.regex}")
    private String passwordRegex; // Expresión regular para validar contraseñas

    /**
     * Crea un nuevo usuario en el sistema.
     *
     * @param usuario El objeto Usuario a ser creado.
     * @return Un objeto CreateResponseDTO con los detalles del usuario creado.
     */
    public CreateResponseDTO crearUsuario(Usuario usuario) {
        // Valida el correo electrónico y la contraseña del usuario
        validarCorreoYContraseña(usuario.getCorreo(), usuario.getPassword());

        // Genera un token JWT para el nuevo usuario
        String jwtToken = jwtService.generarToken(usuario.getCorreo());

        // Codifica la contraseña del usuario
        usuario.setPassword(passwordEncoder.encodePassword(usuario.getPassword()));

        // Establece las propiedades de fecha y activa el usuario
        usuario.setCreado(LocalDateTime.now());
        usuario.setUltimoLogin(LocalDateTime.now());
        usuario.setToken(jwtToken);
        usuario.setActivo(true);

        // Guarda el usuario en el repositorio
        Usuario finalUsuario = usuarioRepository.save(usuario);

        // Asocia y guarda los teléfonos del usuario
        List<Telefono> telefonos = usuario.getTelefonos();
        if (telefonos != null) {
            telefonos.forEach(telefono -> telefono.setUsuario(finalUsuario));
            telefonoRepository.saveAll(telefonos);
        }

        // Devuelve la respuesta construida para el usuario creado
        return construirResponseDTO(finalUsuario);
    }

    /**
     * Realiza el inicio de sesión para un usuario.
     *
     * @param authRequest El DTO que contiene las credenciales de autenticación.
     * @param authenticationManager El manager de autenticación.
     * @return Un objeto CreateResponseDTO con los detalles del usuario autenticado.
     * @throws AuthException Si las credenciales son inválidas.
     */
    public CreateResponseDTO login(AuthRequestDTO authRequest, AuthenticationManager authenticationManager) throws AuthException {
        try {
            // Autentica al usuario con el manager de autenticación
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getCorreo(), authRequest.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Genera un token JWT para el usuario autenticado
            String jwtToken = jwtService.generarToken(authRequest.getCorreo());

            // Busca al usuario por su correo electrónico
            Usuario usuario = buscarPorEmail(authRequest.getCorreo());
            if (usuario == null) {
                throw new UsernameNotFoundException("Usuario no encontrado");
            }

            // Actualiza el último inicio de sesión y guarda el usuario
            usuario.setUltimoLogin(LocalDateTime.now());
            Usuario usuarioGuardado = usuarioRepository.save(usuario);

            // Retorna la respuesta del usuario autenticado con el token JWT
            return construirResponseDTO(usuarioGuardado).toBuilder().token(jwtToken).build();
        } catch (AuthenticationException e) {
            throw new AuthException("Credenciales inválidas");
        }
    }

    /**
     * Obtiene todos los usuarios del sistema.
     *
     * @return Una lista de CreateResponseDTO con los detalles de los usuarios.
     */
    public List<CreateResponseDTO> findAll() {
        try {
            List<Usuario> usuarios = usuarioRepository.findAll();
            // Verificamos si la lista obtenida está vacía
            if (usuarios.isEmpty()) {
                return Collections.emptyList();
            }

            // Convierte las entidades a DTOs
            return usuarios.stream()
                    .map(this::construirResponseDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            // Manejar cualquier otra excepción que pueda ocurrir
            new RuntimeException(e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Actualiza la información de un usuario existente.
     *
     * @param id El ID del usuario a actualizar.
     * @param usuarioActualizado El objeto Usuario con la nueva información.
     * @return Un objeto CreateResponseDTO con los detalles del usuario actualizado.
     */
    public CreateResponseDTO updateUsuario(String id, Usuario usuarioActualizado) {
        // Busca al usuario por su ID y lanza excepción si no lo encuentra
        Usuario usuario = usuarioRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Valida el correo y la contraseña actualizada
        validarCorreoYContraseña(usuarioActualizado.getCorreo(), usuarioActualizado.getPassword());

        // Actualiza las propiedades del usuario con las nuevas informaciones
        usuario.setNombre(usuarioActualizado.getNombre());
        usuario.setCorreo(usuarioActualizado.getCorreo());
        usuario.setPassword(passwordEncoder.encodePassword(usuarioActualizado.getPassword()));
        usuario.setActivo(usuarioActualizado.isActivo());
        usuario.setModificado(LocalDateTime.now());

        // Actualiza la lista de teléfonos del usuario
        usuario.getTelefonos().clear(); // Borra los teléfonos actuales
        usuarioActualizado.getTelefonos().forEach(usuario::addPhone); // Agrega nuevos teléfonos

        // Genera un token JWT para el usuario actualizado
        String jwtToken = jwtService.generarToken(usuario.getCorreo());
        usuario.setToken(jwtToken);

        // Guarda el usuario actualizado en el repositorio
        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        // Devuelve la respuesta construida para el usuario actualizado
        return construirResponseDTO(usuarioGuardado);
    }

    /**
     * Elimina un usuario del sistema por su ID.
     *
     * @param id El ID del usuario a eliminar.
     */
    public void deleteUsuario(String id) {
        // Elimina el usuario por su ID en el repositorio
        usuarioRepository.deleteById(UUID.fromString(id));
    }

    /**
     * Actualiza parcialmente un usuario existente en el sistema.
     *
     * @param id El ID del usuario a actualizar parcialmente.
     * @param updates Un mapa de actualización con los campos y sus nuevos valores.
     * @return Un objeto CreateResponseDTO con los detalles del usuario actualizado.
     */
    public CreateResponseDTO patchUsuario(String id, Map<String, Object> updates) {
        // Busca al usuario por su ID y lanza excepción si no lo encuentra
        Usuario usuario = usuarioRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Itera sobre las actualizaciones y aplica los cambios
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
                    // Crea una lista de teléfonos actualizados
                    List<Telefono> telefonosActualizados = ((List<Map<String, Object>>) valor).stream()
                            .map(dto -> new Telefono(
                                    (String) dto.get("numero"),
                                    (String) dto.get("codigoCiudad"),
                                    (String) dto.get("codigoPais"),
                                    usuario
                            ))
                            .collect(Collectors.toList());
                    // Limpia teléfonos existentes y agrega los nuevos
                    usuario.getTelefonos().clear();
                    usuario.getTelefonos().addAll(telefonosActualizados);
                    break;
            }

        });

        // Genera un nuevo token JWT con el correo posiblemente actualizado
        String jwtToken = jwtService.generarToken(usuario.getCorreo());
        usuario.setToken(jwtToken);

        // Actualiza la fecha de modificación del usuario
        usuario.setModificado(LocalDateTime.now());

        // Guarda el usuario actualizado en el repositorio
        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        // Devuelve la respuesta construida para el usuario actualizado
        return construirResponseDTO(usuarioGuardado);
    }

    /**
     * Busca un usuario por su correo electrónico.
     *
     * @param email El correo electrónico del usuario.
     * @return La entidad Usuario correspondiente al correo.
     */
    public Usuario buscarPorEmail(String email) {
        // Busca y retorna el usuario por su correo
        return usuarioRepository.findByCorreo(email);
    }

    /**
     * Valida el formato del correo y la contraseña del usuario.
     *
     * @param correo El correo electrónico a validar.
     * @param password La contraseña a validar.
     */
    private void validarCorreoYContraseña(String correo, String password) {
        // Verifica si el correo ya está registrado
        if (usuarioRepository.existsByCorreo(correo)) {
            throw new RuntimeException("El correo ya está registrado");
        }
        // Verifica el formato del correo con la expresión regular
        if (!Pattern.matches(emailRegex, correo)) {
            throw new IllegalArgumentException("Formato de correo electrónico inválido");
        }
        // Verifica el formato de la contraseña con la expresión regular
        if (!Pattern.matches(passwordRegex, password)) {
            throw new IllegalArgumentException("Formato de contraseña inválido");
        }
    }

    /**
     * Construye un objeto CreateResponseDTO a partir de una entidad Usuario.
     *
     * @param usuario El usuario del cual se construirá el DTO.
     * @return Un objeto CreateResponseDTO con los detalles del usuario.
     */
    private CreateResponseDTO construirResponseDTO(Usuario usuario) {
        // Construye y retorna un DTO de respuesta basado en la entidad Usuario
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



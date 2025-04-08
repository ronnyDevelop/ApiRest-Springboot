package com.example.ejercicio.service.impl;

import com.example.ejercicio.dto.AuthRequestDTO;
import com.example.ejercicio.dto.CreateResponseDTO;
import com.example.ejercicio.dto.UsuarioDTO;
import com.example.ejercicio.dto.TelefonoDTO;
import com.example.ejercicio.model.Telefono;
import com.example.ejercicio.model.Usuario;
import com.example.ejercicio.repository.TelefonoRepository;
import com.example.ejercicio.repository.UsuarioRepository;
import com.example.ejercicio.util.JwtService;
import com.example.ejercicio.util.PasswordEncoderServiceImpl;
import com.example.ejercicio.service.UsuarioService;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Servicio para gestionar usuarios.
 */
@Service
public class UsuarioServiceImpl implements UsuarioService {

    // Inyecta la dependencia UsuarioRepository para realizar operaciones CRUD con usuarios.
    @Autowired
    private UsuarioRepository usuarioRepository;

    // Inyecta la dependencia TelefonoRepository para realizar operaciones CRUD con teléfonos.
    @Autowired
    private TelefonoRepository telefonoRepository;

    // Inyecta el servicio para codificar contraseñas.
    @Autowired
    private PasswordEncoderServiceImpl passwordEncoder;

    // Inyecta el servicio para generar y gestionar tokens JWT.
    @Autowired
    private JwtService jwtService;

    // Carga la expresión regular para validar correos electrónicos desde las propiedades de configuración.
    @Value("${email.regex}")
    private String emailRegex;

    // Carga la expresión regular para validar contraseñas desde las propiedades de configuración.
    @Value("${password.regex}")
    private String passwordRegex;

    /**
     * Crea un nuevo usuario a partir de un DTO.
     *
     * @param usuarioDTO el DTO que contiene la información del usuario a crear
     * @return un DTO de respuesta que representa al usuario creado
     */
    @Override
    public CreateResponseDTO crearUsuario(UsuarioDTO usuarioDTO) {
        // Valida el formato del correo y la contraseña proporcionados.
        validarCorreoYContraseña(usuarioDTO.getCorreo(), usuarioDTO.getPassword());
        // Construye un objeto Usuario desde el DTO.
        Usuario usuario = construirUsuarioDesdeDTO(usuarioDTO);
        // Genera un token JWT para el usuario.
        String jwtToken = jwtService.generarToken(usuarioDTO.getCorreo());
        // Establece la fecha de creación y el último inicio de sesión al tiempo actual.
        usuario.setCreado(LocalDateTime.now());
        usuario.setUltimoLogin(LocalDateTime.now());
        // Asigna el token JWT al usuario y lo marca como activo.
        usuario.setToken(jwtToken);
        usuario.setActivo(true);

        // Guarda el usuario en el repositorio y obtiene la instancia final guardada.
        Usuario finalUsuario = usuarioRepository.save(usuario);
        // Guarda los teléfonos asociados al usuario.
        guardarTelefonos(finalUsuario, usuarioDTO.getTelefonos());

        // Construye y retorna un DTO de respuesta a partir del usuario guardado.
        return construirResponseDTO(finalUsuario);
    }

    /**
     * Construye un objeto Usuario desde un DTO de Usuario.
     *
     * @param usuarioDTO el DTO que contiene la información del usuario
     * @return un objeto Usuario
     */
    private Usuario construirUsuarioDesdeDTO(UsuarioDTO usuarioDTO) {
        return Usuario.builder()
                .nombre(usuarioDTO.getNombre())
                .correo(usuarioDTO.getCorreo())
                // Codifica la contraseña proporcionada.
                .password(passwordEncoder.encodePassword(usuarioDTO.getPassword()))
                .build();
    }

    /**
     * Guarda una lista de teléfonos en el repositorio, asociados a un usuario.
     *
     * @param usuario el objeto Usuario al que se asociarán los teléfonos
     * @param telefonosDTO la lista de DTOs de teléfono a guardar
     */
    private void guardarTelefonos(Usuario usuario, List<TelefonoDTO> telefonosDTO) {
        // Verifica si la lista de teléfonos no es nula ni está vacía.
        if (telefonosDTO != null && !telefonosDTO.isEmpty()) {
            List<Telefono> telefonos = telefonosDTO.stream()
                    .map(dto -> Telefono.builder()
                            .numero(dto.getNumero())
                            .codigoCiudad(dto.getCodigoCiudad())
                            .codigoPais(dto.getCodigoPais())
                            .usuario(usuario)
                            .build())
                    // Convierte los DTOs en objetos de teléfono.
                    .collect(Collectors.toList());
            // Guarda todos los teléfonos en el repositorio.
            telefonoRepository.saveAll(telefonos);
        }
    }

    /**
     * Realiza el proceso de inicio de sesión para un usuario.
     *
     * @param authRequest el objeto DTO que contiene las credenciales de autenticación
     * @param authenticationManager el gestor de autenticaciones
     * @return un DTO de respuesta que representa al usuario autenticado
     * @throws AuthException si las credenciales son inválidas
     */
    @Override
    public CreateResponseDTO login(AuthRequestDTO authRequest, AuthenticationManager authenticationManager) throws AuthException {
        try {
            // Autentica las credenciales proporcionadas.
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getCorreo(), authRequest.getPassword()));
            // Establece la autenticación en el contexto de seguridad.
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Genera un token JWT para el usuario autenticado.
            String jwtToken = jwtService.generarToken(authRequest.getCorreo());
            // Busca el usuario por correo electrónico.
            Usuario usuario = buscarPorEmail(authRequest.getCorreo());
            if (usuario == null) {
                throw new UsernameNotFoundException("Usuario no encontrado");
            }

            // Actualiza el último inicio de sesión del usuario.
            usuario.setUltimoLogin(LocalDateTime.now());
            // Guarda los cambios en el repositorio.
            Usuario usuarioGuardado = usuarioRepository.save(usuario);

            // Construye y retorna un DTO de respuesta con el token incluido.
            return construirResponseDTO(usuarioGuardado)
                    .toBuilder()
                    .token(jwtToken)
                    .build();
        } catch (AuthenticationException e) {
            throw new AuthException("Credenciales inválidas");
        }
    }

    /**
     * Retorna una lista de todos los usuarios existentes.
     *
     * @return lista de DTOs de respuesta representando a cada usuario
     */
    @Override
    public List<CreateResponseDTO> findAll() {
        try {
            // Busca todos los usuarios en el repositorio.
            List<Usuario> usuarios = usuarioRepository.findAll();
            if (usuarios.isEmpty()) {
                return Collections.emptyList();
            }

            // Construye una lista de DTOs de respuesta para cada usuario encontrado.
            return usuarios.stream()
                    .map(this::construirResponseDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * Actualiza la información de un usuario existente, encontrado por ID.
     *
     * @param id el identificador del usuario a actualizar
     * @param usuarioActualizado el DTO que contiene la información actualizada
     * @return un DTO de respuesta que representa al usuario actualizado
     */
    @Override
    public CreateResponseDTO updateUsuario(String id, UsuarioDTO usuarioActualizado) {
        // Busca el usuario por ID, o lanza una excepción si no es encontrado.
        Usuario usuario = usuarioRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Valida el formato del correo y la contraseña proporcionados.
        validarCorreoYContraseña(usuarioActualizado.getCorreo(), usuarioActualizado.getPassword());

        // Actualiza la información del usuario.
        usuario.setNombre(usuarioActualizado.getNombre());
        usuario.setCorreo(usuarioActualizado.getCorreo());
        usuario.setPassword(passwordEncoder.encodePassword(usuarioActualizado.getPassword()));
        usuario.setModificado(LocalDateTime.now());

        // Borra y actualiza la lista de teléfonos del usuario.
        usuario.getTelefonos().clear();
        usuarioActualizado.getTelefonos().forEach(dto -> {
            Telefono telefono = Telefono.builder()
                    .numero(dto.getNumero())
                    .codigoCiudad(dto.getCodigoCiudad())
                    .codigoPais(dto.getCodigoPais())
                    .usuario(usuario)
                    .build();
            usuario.getTelefonos().add(telefono);
        });

        // Genera un nuevo token JWT y lo asigna al usuario.
        String jwtToken = jwtService.generarToken(usuario.getCorreo());
        usuario.setToken(jwtToken);

        // Guarda el usuario actualizado en el repositorio.
        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        // Construye y retorna un DTO de respuesta a partir del usuario guardado.
        return construirResponseDTO(usuarioGuardado);
    }

    /**
     * Elimina un usuario por ID.
     *
     * @param id el identificador del usuario a eliminar
     * @return true si el usuario fue eliminado exitosamente, false si el usuario no fue encontrado
     */
    @Override
    public boolean deleteUsuario(String id) {
        // Busca el usuario por ID.
        Optional<Usuario> usuario = usuarioRepository.findById(UUID.fromString(id));
        if (usuario.isPresent()) {
            // Elimina el usuario si existe.
            usuarioRepository.delete(usuario.get());
            return true;
        } else {
            return false;
        }
    }

    /**
     * Actualiza parcialmente la información de un usuario existente utilizando un mapa de cambios.
     *
     * @param id el identificador del usuario a actualizar
     * @param updates un mapa que contiene los cambios a aplicar
     * @return un DTO de respuesta que representa al usuario actualizado
     */
    @Override
    public CreateResponseDTO patchUsuario(String id, Map<String, Object> updates) {
        // Busca el usuario por ID, o lanza una excepción si no es encontrado.
        Usuario usuario = usuarioRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Aplica los cambios al usuario según el mapa de actualizaciones.
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
                            .map(dto -> Telefono.builder()
                                    .numero((String) dto.get("numero"))
                                    .codigoCiudad((String) dto.get("codigoCiudad"))
                                    .codigoPais((String) dto.get("codigoPais"))
                                    .usuario(usuario)
                                    .build())
                            .collect(Collectors.toList());
                    usuario.getTelefonos().clear();
                    usuario.getTelefonos().addAll(telefonosActualizados);
                    break;
                default:
                    throw new RuntimeException("Información desconocida");
            }
        });

        // Genera un nuevo token JWT y lo asigna al usuario.
        String jwtToken = jwtService.generarToken(usuario.getCorreo());
        usuario.setToken(jwtToken);
        // Actualiza la fecha de modificación del usuario.
        usuario.setModificado(LocalDateTime.now());

        // Guarda el usuario actualizado en el repositorio.
        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        // Construye y retorna un DTO de respuesta a partir del usuario guardado.
        return construirResponseDTO(usuarioGuardado);
    }

    /**
     * Busca un usuario por correo electrónico.
     *
     * @param email el correo electrónico del usuario que se busca
     * @return el usuario encontrado o null si no se encuentra
     */
    @Override
    public Usuario buscarPorEmail(String email) {
        return usuarioRepository.findByCorreo(email);
    }

    /**
     * Valida el formato del correo y la contraseña proporcionados.
     *
     * @param correo el correo electrónico a validar
     * @param password la contraseña a validar
     */
    private void validarCorreoYContraseña(String correo, String password) {
        // Verifica si el correo ya está registrado.
        if (usuarioRepository.existsByCorreo(correo)) {
            throw new RuntimeException("El correo ya está registrado");
        }
        // Verifica el formato del correo electrónico usando la expresión regular.
        if (!Pattern.matches(emailRegex, correo)) {
            throw new IllegalArgumentException("Formato de correo electrónico inválido");
        }
        // Verifica el formato de la contraseña usando la expresión regular.
        if (!Pattern.matches(passwordRegex, password)) {
            throw new IllegalArgumentException("Formato de contraseña inválido");
        }
    }

    /**
     * Construye un DTO de respuesta a partir de un objeto Usuario.
     *
     * @param usuario el usuario del que construir el DTO
     * @return un objeto CreateResponseDTO que representa al usuario
     */
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
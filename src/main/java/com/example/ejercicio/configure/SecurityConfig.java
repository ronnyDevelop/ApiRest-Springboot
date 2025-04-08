package com.example.ejercicio.configure;

import com.example.ejercicio.service.UsuarioDetalleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Clase de configuración de seguridad para la aplicación,
 * encargada de definir las reglas de seguridad y autenticación.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Filtro personalizado para manejo de JWT.
     */
    @Autowired
    private JwFilter jwFilter;

    /**
     * Servicio personalizado para manejo de los detalles de usuario.
     */
    @Autowired
    private UsuarioDetalleService usuarioDetalleService;

    /**
     * Método para configurar la cadena de filtros de seguridad HTTP.
     *
     * @param http objeto HttpSecurity para configuración de seguridad.
     * @return objeto SecurityFilterChain configurado.
     * @throws Exception en caso de un error durante la configuración.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Configuración de CORS utilizando valores por defecto
                .cors(Customizer.withDefaults())
                // Deshabilitación del soporte CSRF
                .csrf(AbstractHttpConfigurer::disable)
                // Configuración de autorizaciones para solicitudes HTTP
                .authorizeHttpRequests(auth -> auth
                        // Permitir acceso sin autenticación a las siguientes rutas
                        .requestMatchers("/h2-console/**", "/api/auth/**", "/swagger-ui/**",
                                "/v3/api-docs/**").permitAll()
                        // Requerir autenticación para cualquier otra solicitud
                        .anyRequest().authenticated()
                )
                // Configuración de manejo de sesiones en modo stateless
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Configurar el proveedor de autenticación
                .authenticationProvider(authenticationProvider())
                // Agregar filtro personalizado antes del filtro de autenticación
                .addFilterBefore(jwFilter, UsernamePasswordAuthenticationFilter.class)
                // Deshabilitación de opciones de encuadre de encabezados
                .headers(headers -> headers.frameOptions().disable());

        // Construir el objeto SecurityFilterChain
        return http.build();
    }

    /**
     * Método para configurar el proveedor de autenticación
     * utilizando un servicio de detalles de usuario personalizado.
     *
     * @return objeto AuthenticationProvider configurado.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        // Establecer el servicio de detalles de usuario
        authProvider.setUserDetailsService(usuarioDetalleService);
        // Establecer el codificador de contraseñas
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Método para configurar el codificador de contraseñas usando BCrypt.
     *
     * @return objeto BCryptPasswordEncoder para codificar contraseñas.
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Método para obtener el administrador de autenticación de la configuración.
     *
     * @param config objeto AuthenticationConfiguration.
     * @return objeto AuthenticationManager configurado.
     * @throws Exception en caso de un error al obtener el administrador de autenticación.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}


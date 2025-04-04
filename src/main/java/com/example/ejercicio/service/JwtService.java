package com.example.ejercicio.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import java.util.function.Function;

/**
 * Servicio para gestionar operaciones relacionadas con JWT.
 */
@Service
public class JwtService {

    // Llave secreta para firmar los tokens JWT.
    @Value("${jwt.secret}")
    private String secret;

    /**
     * Extrae el nombre de usuario contenido en el token.
     *
     * @param token el token JWT
     * @return el nombre de usuario extraído del token
     */
    public String extraerUsername(String token) {
        // Utiliza una función para extraer el sujeto del token.
        return extraerClaim(token, Claims::getSubject);
    }

    /**
     * Extrae la fecha de expiración del token.
     *
     * @param token el token JWT
     * @return la fecha de expiración extraída del token
     */
    public Date extraerExpiracion(String token) {
        // Utiliza una función para extraer la fecha de expiración del token.
        return extraerClaim(token, Claims::getExpiration);
    }

    /**
     * Extrae un reclamo específico del token utilizando la función proporcionada.
     *
     * @param <T> el tipo del reclamo
     * @param token el token JWT
     * @param claimsResolver la función para resolver el reclamo
     * @return el reclamo extraído del token
     */
    public <T> T extraerClaim(String token, Function<Claims, T> claimsResolver) {
        // Aplica la función provista para extraer el reclamo de los Claims.
        return claimsResolver.apply(extraerTodosClaims(token));
    }

    /**
     * Extrae todos los reclamos del token.
     *
     * @param token el token JWT
     * @return los reclamos extraídos del token
     */
    private Claims extraerTodosClaims(String token) {
        // Analiza el token JWT para obtener los Claims utilizando la clave secreta.
        return Jwts
                .parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Verifica si el token ha expirado.
     *
     * @param token el token JWT
     * @return true si el token ha expirado, false en caso contrario
     */
    private boolean isTokenExpirado(String token) {
        // Compara la fecha de expiración del token con la fecha actual.
        return extraerExpiracion(token).before(new Date());
    }

    /**
     * Comprueba si el token es válido evaluando el nombre de usuario y la fecha de expiración.
     *
     * @param token el token JWT
     * @param userDetails los detalles del usuario para comparación
     * @return true si el token es válido, false en caso contrario
     */
    public boolean isTokenValido(String token, UserDetails userDetails) {
        // Extrae el nombre de usuario del token y lo compara con el nombre de usuario esperado.
        String username = extraerUsername(token);
        // Verifica que los nombres de usuario coincidan y que el token no haya expirado.
        return username.equals(userDetails.getUsername()) && !isTokenExpirado(token);
    }

    /**
     * Genera un token JWT para un usuario dado.
     *
     * @param email el correo electrónico del usuario
     * @return el token JWT generado
     */
    public String generarToken(String email) {
        // Crea un UserDetails temporal con el email del usuario.
        UserDetails userDetails = new User(email, "", java.util.Collections.emptyList());

        // Configura y genera el token JWT con los Claims necesarios.
        return Jwts.builder()
                .setSubject(userDetails.getUsername()) // Establece el sujeto del token.
                .setIssuedAt(new Date(System.currentTimeMillis())) // Establece la fecha de emisión del token.
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 24)) // Establece la fecha de expiración del token.
                .signWith(SignatureAlgorithm.HS256, secret) // Firma el token con el algoritmo HS256.
                .compact(); // Compacta el token para obtener la representación final.
    }

    /**
     * Genera un token JWT con reclamos adicionales para un usuario dado.
     *
     * @param extraClaims reclamos adicionales a incluir en el token
     * @param userDetails los detalles del usuario para el token
     * @return el token JWT generado
     */
    public String generarToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        // Configura y genera el token JWT con Claims adicionales y necesarias.
        return Jwts.builder()
                .setClaims(extraClaims) // Establece reclamos adicionales.
                .setSubject(userDetails.getUsername()) // Establece el sujeto del token.
                .setIssuedAt(new Date(System.currentTimeMillis())) // Establece la fecha de emisión del token.
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 24)) // Establece la fecha de expiración del token.
                .signWith(SignatureAlgorithm.HS256, secret) // Firma el token con el algoritmo HS256.
                .compact(); // Compacta el token para obtener la representación final.
    }
}
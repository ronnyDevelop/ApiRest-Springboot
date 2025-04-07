package com.example.ejercicio.controller;

import com.example.ejercicio.dto.CreateResponseDTO;
import com.example.ejercicio.dto.UsuarioDTO;
import com.example.ejercicio.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/findAll")
    @Operation(summary = "Obtener todos los usuarios")
    @ApiResponse(responseCode = "200", description = "Lista de usuarios obtenida con éxito")
    @ApiResponse(responseCode = "401", description = "No autorizado")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> buscarTodos(){
        return ResponseEntity.ok(usuarioService.findAll());
    }

    @PutMapping("/update/{id}")
    @Operation(summary = "Actualizar un usuario")
    @ApiResponse(responseCode = "200", description = "Usuario actualizado con éxito")
    @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    public ResponseEntity<CreateResponseDTO> updateUsuario(
            @PathVariable String id,
            @RequestBody UsuarioDTO usuarioActualizado) {
        CreateResponseDTO response = usuarioService.updateUsuario(id, usuarioActualizado);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("delete/{id}")
    @Operation(summary = "Borrar un usuario")
    @ApiResponse(responseCode = "204", description = "Usuario eliminado con éxito")
    @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    public ResponseEntity<Void> deleteUsuario(@PathVariable String id) {
        usuarioService.deleteUsuario(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("patch/{id}")
    @Operation(summary = "Actualizar parcialmente un usuario")
    @ApiResponse(responseCode = "200", description = "Usuario actualizado parcialmente con éxito")
    @ApiResponse(responseCode = "400", description = "Solicitud mal formada")
    @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    public ResponseEntity<CreateResponseDTO> patchUsuario(
            @PathVariable String id,
            @RequestBody Map<String, Object> updates) {
        CreateResponseDTO response = usuarioService.patchUsuario(id, updates);
        return ResponseEntity.ok(response);
    }
}

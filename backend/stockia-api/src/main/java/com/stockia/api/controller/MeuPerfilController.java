
package com.stockia.api.controller;

import com.stockia.api.dto.response.UsuarioResponse;
import com.stockia.api.service.UsuarioService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuarios")
public class MeuPerfilController {

    private final UsuarioService usuarioService;

    public MeuPerfilController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/me")
    public ResponseEntity<UsuarioResponse> meuPerfil() {

        return ResponseEntity.ok(
                usuarioService.buscarMeuPerfil());
    }
}

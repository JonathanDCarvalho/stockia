
package com.stockia.api.controller;

import com.stockia.api.dto.request.UsuarioRequest;
import com.stockia.api.dto.response.UsuarioResponse;
import com.stockia.api.service.UsuarioService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/empresas/{empresaId}/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping
    public ResponseEntity<UsuarioResponse> cadastrar(
            @PathVariable Long empresaId,
            @Valid @RequestBody UsuarioRequest request) {

        UsuarioResponse usuario = usuarioService.cadastrar(empresaId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(usuario);
    }

    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> listar(
            @PathVariable Long empresaId) {

        return ResponseEntity.ok(
                usuarioService.listar(empresaId));
    }

    @GetMapping("/{usuarioId}")
    public ResponseEntity<UsuarioResponse> buscarPorId(
            @PathVariable Long empresaId,
            @PathVariable Long usuarioId) {

        return ResponseEntity.ok(
                usuarioService.buscarPorId(
                        empresaId, usuarioId));
    }

    @PatchMapping("/{usuarioId}/desativar")
    public ResponseEntity<Void> desativar(
            @PathVariable Long empresaId,
            @PathVariable Long usuarioId) {

        usuarioService.desativar(empresaId, usuarioId);

        return ResponseEntity.noContent().build();
    }
}

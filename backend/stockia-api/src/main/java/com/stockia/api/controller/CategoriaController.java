
package com.stockia.api.controller;

import com.stockia.api.dto.request.CategoriaRequest;
import com.stockia.api.dto.response.CategoriaResponse;
import com.stockia.api.entity.Categoria;
import com.stockia.api.service.CategoriaService;

import jakarta.validation.Valid;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/empresas/{empresaId}/categorias")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @PostMapping
    public ResponseEntity<CategoriaResponse> cadastrar(
            @PathVariable Long empresaId,
            @Valid @RequestBody CategoriaRequest request) {

        Categoria categoria = new Categoria();
        categoria.setNome(request.nome());
        categoria.setDescricao(request.descricao());

        Categoria categoriaSalva = categoriaService.cadastrar(
                empresaId, categoria);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(toResponse(categoriaSalva));
    }

    @GetMapping
    public ResponseEntity<List<CategoriaResponse>> listar(
            @PathVariable Long empresaId) {

        List<CategoriaResponse> categorias = categoriaService
                .listarPorEmpresa(empresaId)
                .stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(categorias);
    }

    @GetMapping("/{categoriaId}")
    public ResponseEntity<CategoriaResponse> buscarPorId(
            @PathVariable Long empresaId,
            @PathVariable Long categoriaId) {

        Categoria categoria = categoriaService.buscarPorId(
                empresaId, categoriaId);

        return ResponseEntity.ok(toResponse(categoria));
    }

    @PutMapping("/{categoriaId}")
    public ResponseEntity<CategoriaResponse> atualizar(
            @PathVariable Long empresaId,
            @PathVariable Long categoriaId,
            @Valid @RequestBody CategoriaRequest request) {

        Categoria dadosAtualizados = new Categoria();
        dadosAtualizados.setNome(request.nome());
        dadosAtualizados.setDescricao(request.descricao());

        Categoria categoriaAtualizada = categoriaService.atualizar(
                empresaId, categoriaId, dadosAtualizados);

        return ResponseEntity.ok(toResponse(categoriaAtualizada));
    }

    @PatchMapping("/{categoriaId}/desativar")
    public ResponseEntity<Void> desativar(
            @PathVariable Long empresaId,
            @PathVariable Long categoriaId) {

        categoriaService.desativar(empresaId, categoriaId);

        return ResponseEntity.noContent().build();
    }

    private CategoriaResponse toResponse(Categoria categoria) {

        return new CategoriaResponse(
                categoria.getId(),
                categoria.getEmpresa().getId(),
                categoria.getNome(),
                categoria.getDescricao(),
                categoria.getAtivo(),
                categoria.getCreatedAt(),
                categoria.getUpdatedAt());
    }
}

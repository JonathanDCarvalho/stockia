
package com.stockia.api.controller;

import com.stockia.api.dto.request.MovimentacaoEstoqueRequest;
import com.stockia.api.dto.response.MovimentacaoEstoqueResponse;
import com.stockia.api.entity.MovimentacaoEstoque;
import com.stockia.api.service.MovimentacaoEstoqueService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/empresas/{empresaId}")
public class MovimentacaoEstoqueController {

    private final MovimentacaoEstoqueService movimentacaoService;

    public MovimentacaoEstoqueController(
            MovimentacaoEstoqueService movimentacaoService) {
        this.movimentacaoService = movimentacaoService;
    }

    @PostMapping("/produtos/{produtoId}/movimentacoes")
    public ResponseEntity<MovimentacaoEstoqueResponse> registrar(
            @PathVariable Long empresaId,
            @PathVariable Long produtoId,
            @Valid @RequestBody MovimentacaoEstoqueRequest request) {

        MovimentacaoEstoque movimentacao = movimentacaoService.registrar(
                empresaId, produtoId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(converterParaResponse(movimentacao));
    }

    @GetMapping("/movimentacoes")
    public ResponseEntity<List<MovimentacaoEstoqueResponse>> listarPorEmpresa(
            @PathVariable Long empresaId) {

        List<MovimentacaoEstoqueResponse> movimentacoes = movimentacaoService.listarPorEmpresa(empresaId)
                .stream()
                .map(this::converterParaResponse)
                .toList();

        return ResponseEntity.ok(movimentacoes);
    }

    @GetMapping("/produtos/{produtoId}/movimentacoes")
    public ResponseEntity<List<MovimentacaoEstoqueResponse>> listarPorProduto(
            @PathVariable Long empresaId,
            @PathVariable Long produtoId) {

        List<MovimentacaoEstoqueResponse> movimentacoes = movimentacaoService.listarPorProduto(empresaId, produtoId)
                .stream()
                .map(this::converterParaResponse)
                .toList();

        return ResponseEntity.ok(movimentacoes);
    }

    private MovimentacaoEstoqueResponse converterParaResponse(
            MovimentacaoEstoque movimentacao) {

        return new MovimentacaoEstoqueResponse(
                movimentacao.getId(),
                movimentacao.getEmpresa().getId(),
                movimentacao.getProduto().getId(),
                movimentacao.getProduto().getNome(),
                movimentacao.getTipo(),
                movimentacao.getQuantidade(),
                movimentacao.getEstoqueAnterior(),
                movimentacao.getEstoquePosterior(),
                movimentacao.getObservacao(),
                movimentacao.getCreatedAt());
    }
}

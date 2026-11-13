
package com.stockia.api.controller;

import com.stockia.api.dto.request.FornecedorRequest;
import com.stockia.api.dto.response.FornecedorResponse;
import com.stockia.api.entity.Fornecedor;
import com.stockia.api.service.FornecedorService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/empresas/{empresaId}/fornecedores")
public class FornecedorController {

    private final FornecedorService fornecedorService;

    public FornecedorController(FornecedorService fornecedorService) {
        this.fornecedorService = fornecedorService;
    }

    @PostMapping
    public ResponseEntity<FornecedorResponse> cadastrar(
            @PathVariable Long empresaId,
            @Valid @RequestBody FornecedorRequest request) {

        Fornecedor fornecedor = new Fornecedor();

        fornecedor.setNome(request.nome());
        fornecedor.setDocumento(request.documento());
        fornecedor.setEmail(request.email());
        fornecedor.setTelefone(request.telefone());
        fornecedor.setContato(request.contato());

        Fornecedor fornecedorSalvo = fornecedorService.cadastrar(empresaId, fornecedor);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toResponse(fornecedorSalvo));
    }

    @GetMapping
    public ResponseEntity<List<FornecedorResponse>> listar(
            @PathVariable Long empresaId) {

        List<FornecedorResponse> fornecedores = fornecedorService.listarPorEmpresa(empresaId)
                .stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(fornecedores);
    }

    @GetMapping("/{fornecedorId}")
    public ResponseEntity<FornecedorResponse> buscarPorId(
            @PathVariable Long empresaId,
            @PathVariable Long fornecedorId) {

        Fornecedor fornecedor = fornecedorService.buscarPorId(empresaId, fornecedorId);

        return ResponseEntity.ok(toResponse(fornecedor));
    }

    @PutMapping("/{fornecedorId}")
    public ResponseEntity<FornecedorResponse> atualizar(
            @PathVariable Long empresaId,
            @PathVariable Long fornecedorId,
            @Valid @RequestBody FornecedorRequest request) {

        Fornecedor dadosAtualizados = new Fornecedor();

        dadosAtualizados.setNome(request.nome());
        dadosAtualizados.setDocumento(request.documento());
        dadosAtualizados.setEmail(request.email());
        dadosAtualizados.setTelefone(request.telefone());
        dadosAtualizados.setContato(request.contato());

        Fornecedor fornecedorAtualizado = fornecedorService.atualizar(
                empresaId, fornecedorId, dadosAtualizados);

        return ResponseEntity.ok(toResponse(fornecedorAtualizado));
    }

    @PatchMapping("/{fornecedorId}/desativar")
    public ResponseEntity<Void> desativar(
            @PathVariable Long empresaId,
            @PathVariable Long fornecedorId) {

        fornecedorService.desativar(empresaId, fornecedorId);

        return ResponseEntity.noContent().build();
    }

    private FornecedorResponse toResponse(Fornecedor fornecedor) {

        return new FornecedorResponse(
                fornecedor.getId(),
                fornecedor.getEmpresa().getId(),
                fornecedor.getNome(),
                fornecedor.getDocumento(),
                fornecedor.getEmail(),
                fornecedor.getTelefone(),
                fornecedor.getContato(),
                fornecedor.getAtivo(),
                fornecedor.getCreatedAt(),
                fornecedor.getUpdatedAt());
    }
}

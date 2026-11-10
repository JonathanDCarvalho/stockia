
package com.stockia.api.controller;

import com.stockia.api.dto.request.ProdutoRequest;
import com.stockia.api.dto.response.ProdutoResponse;
import com.stockia.api.entity.Produto;
import com.stockia.api.service.ProdutoService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/empresas/{empresaId}/produtos")
public class ProdutoController {

    private final ProdutoService produtoService;

    public ProdutoController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    @PostMapping
    public ResponseEntity<ProdutoResponse> cadastrar(
            @PathVariable Long empresaId,
            @Valid @RequestBody ProdutoRequest request) {

        Produto produto = converterParaEntidade(request);

        Produto produtoSalvo = produtoService.cadastrar(empresaId, produto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(converterParaResponse(produtoSalvo));
    }

    @GetMapping
    public ResponseEntity<List<ProdutoResponse>> listar(
            @PathVariable Long empresaId) {

        List<ProdutoResponse> produtos = produtoService
                .listarPorEmpresa(empresaId)
                .stream()
                .map(this::converterParaResponse)
                .toList();

        return ResponseEntity.ok(produtos);
    }

    @GetMapping("/{produtoId}")
    public ResponseEntity<ProdutoResponse> buscarPorId(
            @PathVariable Long empresaId,
            @PathVariable Long produtoId) {

        Produto produto = produtoService.buscarPorId(empresaId, produtoId);

        return ResponseEntity.ok(
                converterParaResponse(produto));
    }

    @PutMapping("/{produtoId}")
    public ResponseEntity<ProdutoResponse> atualizar(
            @PathVariable Long empresaId,
            @PathVariable Long produtoId,
            @Valid @RequestBody ProdutoRequest request) {

        Produto dadosAtualizados = converterParaEntidade(request);

        Produto produtoAtualizado = produtoService.atualizar(
                empresaId,
                produtoId,
                dadosAtualizados);

        return ResponseEntity.ok(
                converterParaResponse(produtoAtualizado));
    }

    @DeleteMapping("/{produtoId}")
    public ResponseEntity<Void> desativar(
            @PathVariable Long empresaId,
            @PathVariable Long produtoId) {

        produtoService.desativar(empresaId, produtoId);

        return ResponseEntity.noContent().build();
    }

    private Produto converterParaEntidade(ProdutoRequest request) {

        Produto produto = new Produto();

        produto.setNome(request.nome());
        produto.setSku(request.sku());
        produto.setDescricao(request.descricao());
        produto.setPrecoCusto(request.precoCusto());
        produto.setPrecoVenda(request.precoVenda());
        produto.setEstoqueMinimo(request.estoqueMinimo());

        return produto;
    }

    private ProdutoResponse converterParaResponse(Produto produto) {

        return new ProdutoResponse(
                produto.getId(),
                produto.getEmpresa().getId(),
                produto.getNome(),
                produto.getSku(),
                produto.getDescricao(),
                produto.getPrecoCusto(),
                produto.getPrecoVenda(),
                produto.getQuantidadeEstoque(),
                produto.getEstoqueMinimo(),
                produto.getAtivo(),
                produto.getCreatedAt(),
                produto.getUpdatedAt());
    }
}

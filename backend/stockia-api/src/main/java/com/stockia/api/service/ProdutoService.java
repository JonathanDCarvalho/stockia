
package com.stockia.api.service;

import com.stockia.api.entity.Empresa;
import com.stockia.api.entity.Produto;
import com.stockia.api.repository.EmpresaRepository;
import com.stockia.api.repository.ProdutoRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final EmpresaRepository empresaRepository;

    public ProdutoService(
            ProdutoRepository produtoRepository,
            EmpresaRepository empresaRepository) {

        this.produtoRepository = produtoRepository;
        this.empresaRepository = empresaRepository;
    }

    @Transactional
    public Produto cadastrar(Long empresaId, Produto produto) {

        Empresa empresa = empresaRepository
                .findByIdAndAtivoTrue(empresaId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Empresa não encontrada ou inativa."));

        if (produto.getNome() == null ||
                produto.getNome().isBlank()) {
            throw new IllegalArgumentException(
                    "O nome do produto é obrigatório.");
        }

        produto.setNome(produto.getNome().trim());

        if (produto.getSku() == null ||
                produto.getSku().isBlank()) {
            throw new IllegalArgumentException(
                    "O SKU do produto é obrigatório.");
        }

        String sku = produto.getSku().trim().toUpperCase();

        if (sku.length() > 50) {
            throw new IllegalArgumentException(
                    "O SKU deve ter no máximo 50 caracteres.");
        }

        if (produtoRepository.existsByEmpresaIdAndSku(
                empresaId, sku)) {
            throw new IllegalArgumentException(
                    "Já existe um produto com esse SKU nesta empresa.");
        }

        produto.setSku(sku);

        validarPreco(produto.getPrecoCusto(), "custo");
        validarPreco(produto.getPrecoVenda(), "venda");

        if (produto.getQuantidadeEstoque() == null ||
                produto.getQuantidadeEstoque() < 0) {
            throw new IllegalArgumentException(
                    "A quantidade em estoque não pode ser negativa.");
        }

        if (produto.getEstoqueMinimo() == null ||
                produto.getEstoqueMinimo() < 0) {
            throw new IllegalArgumentException(
                    "O estoque mínimo não pode ser negativo.");
        }

        produto.setEmpresa(empresa);

        return produtoRepository.save(produto);
    }

    private void validarPreco(BigDecimal preco, String tipo) {

        if (preco == null || preco.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "O preço de " + tipo + " não pode ser negativo ou nulo.");
        }
    }
}

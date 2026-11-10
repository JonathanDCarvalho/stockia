
package com.stockia.api.service;

import com.stockia.api.entity.Empresa;
import com.stockia.api.entity.Produto;
import com.stockia.api.exception.RecursoNaoEncontradoException;
import com.stockia.api.exception.RecursoConflitoException;
import com.stockia.api.repository.EmpresaRepository;
import com.stockia.api.repository.ProdutoRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

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

    // CADASTRAR PRODUTO
    @Transactional
    public Produto cadastrar(Long empresaId, Produto produto) {

        Empresa empresa = buscarEmpresaAtiva(empresaId);

        validarDados(produto);

        String sku = normalizarSku(produto.getSku());

        if (produtoRepository.existsByEmpresaIdAndSku(
                empresaId, sku)) {
            throw new RecursoConflitoException(
                    "Já existe um produto com esse SKU nesta empresa.");
        }

        produto.setNome(produto.getNome().trim());
        produto.setSku(sku);
        produto.setQuantidadeEstoque(0);
        produto.setAtivo(true);
        produto.setEmpresa(empresa);

        return produtoRepository.save(produto);
    }

    // LISTAR PRODUTOS ATIVOS DA EMPRESA
    @Transactional(readOnly = true)
    public List<Produto> listarPorEmpresa(Long empresaId) {

        buscarEmpresaAtiva(empresaId);

        return produtoRepository
                .findAllByEmpresaIdAndAtivoTrue(empresaId);
    }

    // BUSCAR PRODUTO POR ID
    @Transactional(readOnly = true)
    public Produto buscarPorId(Long empresaId, Long produtoId) {

        buscarEmpresaAtiva(empresaId);

        return produtoRepository
                .findByIdAndEmpresaIdAndAtivoTrue(
                        produtoId, empresaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Produto não encontrado nesta empresa."));
    }

    // ATUALIZAR PRODUTO
    @Transactional
    public Produto atualizar(
            Long empresaId,
            Long produtoId,
            Produto dadosAtualizados) {

        Produto produto = buscarPorId(empresaId, produtoId);

        validarDados(dadosAtualizados);

        String sku = normalizarSku(dadosAtualizados.getSku());

        boolean skuDuplicado =
                produtoRepository.existsByEmpresaIdAndSkuAndIdNot(
                        empresaId, sku, produtoId);

        if (skuDuplicado) {
            throw new RecursoConflitoException(
                    "Já existe outro produto com esse SKU nesta empresa.");
        }

        produto.setNome(dadosAtualizados.getNome().trim());
        produto.setSku(sku);
        produto.setDescricao(dadosAtualizados.getDescricao());
        produto.setPrecoCusto(dadosAtualizados.getPrecoCusto());
        produto.setPrecoVenda(dadosAtualizados.getPrecoVenda());
        produto.setEstoqueMinimo(dadosAtualizados.getEstoqueMinimo());

        // A quantidade em estoque não é alterada aqui.
        // Ela será controlada pelo módulo de movimentações.

        return produtoRepository.save(produto);
    }

    // DESATIVAR PRODUTO
    @Transactional
    public void desativar(Long empresaId, Long produtoId) {

        Produto produto = buscarPorId(empresaId, produtoId);

        produto.setAtivo(false);

        produtoRepository.save(produto);
    }

    // VALIDAR EMPRESA
    private Empresa buscarEmpresaAtiva(Long empresaId) {

        return empresaRepository
                .findByIdAndAtivoTrue(empresaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Empresa não encontrada ou inativa."));
    }

    // VALIDAR DADOS DO PRODUTO
    private void validarDados(Produto produto) {

        if (produto.getNome() == null ||
                produto.getNome().isBlank()) {
            throw new IllegalArgumentException(
                    "O nome do produto é obrigatório.");
        }

        if (produto.getNome().trim().length() > 150) {
            throw new IllegalArgumentException(
                    "O nome deve ter no máximo 150 caracteres.");
        }

        if (produto.getSku() == null ||
                produto.getSku().isBlank()) {
            throw new IllegalArgumentException(
                    "O SKU do produto é obrigatório.");
        }

        if (normalizarSku(produto.getSku()).length() > 50) {
            throw new IllegalArgumentException(
                    "O SKU deve ter no máximo 50 caracteres.");
        }

        validarPreco(produto.getPrecoCusto(), "custo");
        validarPreco(produto.getPrecoVenda(), "venda");

        if (produto.getEstoqueMinimo() == null ||
                produto.getEstoqueMinimo() < 0) {
            throw new IllegalArgumentException(
                    "O estoque mínimo não pode ser negativo.");
        }
    }

    // NORMALIZAR SKU
    private String normalizarSku(String sku) {
        return sku.trim().toUpperCase(Locale.ROOT);
    }

    // VALIDAR PREÇOS
    private void validarPreco(BigDecimal preco, String tipo) {

        if (preco == null ||
                preco.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "O preço de " + tipo +
                    " não pode ser negativo ou nulo.");
        }
    }
}

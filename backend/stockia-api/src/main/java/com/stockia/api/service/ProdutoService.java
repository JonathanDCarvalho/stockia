
package com.stockia.api.service;

import com.stockia.api.entity.Empresa;
import com.stockia.api.entity.Fornecedor;
import com.stockia.api.entity.Produto;
import com.stockia.api.entity.Categoria;
import com.stockia.api.dto.request.MovimentacaoEstoqueRequest;
import com.stockia.api.enums.TipoMovimentacao;
import com.stockia.api.exception.RecursoNaoEncontradoException;
import com.stockia.api.exception.RecursoConflitoException;
import com.stockia.api.repository.EmpresaRepository;
import com.stockia.api.repository.FornecedorRepository;
import com.stockia.api.repository.ProdutoRepository;
import com.stockia.api.repository.CategoriaRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

@Service
public class ProdutoService {

        private final ProdutoRepository produtoRepository;
        private final EmpresaRepository empresaRepository;
        private final FornecedorRepository fornecedorRepository;
        private final AuditoriaService auditoriaService;
        private final MovimentacaoEstoqueService movimentacaoService;
        private final CategoriaRepository categoriaRepository;

        public ProdutoService(
                        ProdutoRepository produtoRepository,
                        EmpresaRepository empresaRepository,
                        FornecedorRepository fornecedorRepository,
                        AuditoriaService auditoriaService,
                        MovimentacaoEstoqueService movimentacaoService,
                        CategoriaRepository categoriaRepository) {

                this.produtoRepository = produtoRepository;
                this.empresaRepository = empresaRepository;
                this.fornecedorRepository = fornecedorRepository;
                this.auditoriaService = auditoriaService;
                this.movimentacaoService = movimentacaoService;
                this.categoriaRepository = categoriaRepository;
        }

        @Transactional
        public Produto cadastrar(
                        Long empresaId,
                        Long fornecedorId,
                        Produto produto) {

                return cadastrar(empresaId, fornecedorId, produto, null);
        }

        @Transactional
        public Produto cadastrar(
                        Long empresaId,
                        Long fornecedorId,
                        Produto produto,
                        Integer quantidadeInicial) {

                return cadastrar(empresaId, fornecedorId, produto, quantidadeInicial, null);
        }

        @Transactional
        public Produto cadastrar(
                        Long empresaId,
                        Long fornecedorId,
                        Produto produto,
                        Integer quantidadeInicial,
                        Long categoriaId) {

                if (quantidadeInicial != null && quantidadeInicial < 0) {
                        throw new IllegalArgumentException(
                                        "A quantidade inicial não pode ser negativa.");
                }

                Empresa empresa = buscarEmpresaAtiva(empresaId);

                validarDados(produto);

                Fornecedor fornecedor = buscarFornecedorAtivo(
                                empresaId, fornecedorId);

                Categoria categoria = buscarCategoria(empresaId, categoriaId, null);

                String sku = normalizarSku(produto.getSku());

                if (produtoRepository.existsByEmpresaIdAndSku(
                                empresaId, sku)) {

                        throw new RecursoConflitoException(
                                        "Já existe um produto com esse SKU nesta empresa.");
                }

                produto.setNome(produto.getNome().trim());
                produto.setSku(sku);
                produto.setDescricao(normalizarDescricao(produto.getDescricao()));
                produto.setQuantidadeEstoque(0);
                produto.setEstoqueMinimo(produto.getEstoqueMinimo());
                produto.setFornecedor(fornecedor);
                produto.setCategoria(categoria);
                produto.setAtivo(true);
                produto.setEmpresa(empresa);

                Produto produtoSalvo = produtoRepository.save(produto);

                auditoriaService.registrar(
                                empresa,
                                "PRODUTO",
                                produtoSalvo.getId(),
                                "CRIAR",
                                "Produto cadastrado: " + produtoSalvo.getNome());

                if (quantidadeInicial != null && quantidadeInicial > 0) {
                        movimentacaoService.registrar(
                                        empresaId,
                                        produtoSalvo.getId(),
                                        new MovimentacaoEstoqueRequest(
                                                        TipoMovimentacao.ENTRADA,
                                                        quantidadeInicial,
                                                        "Estoque inicial"));
                }

                return produtoSalvo;
        }

        @Transactional(readOnly = true)
        public List<Produto> listarPorEmpresa(Long empresaId) {

                buscarEmpresaAtiva(empresaId);

                return produtoRepository
                                .findAllByEmpresaIdAndAtivoTrue(empresaId);
        }

        @Transactional(readOnly = true)
        public Produto buscarPorId(Long empresaId, Long produtoId) {

                buscarEmpresaAtiva(empresaId);

                return produtoRepository
                                .findByIdAndEmpresaIdAndAtivoTrue(
                                                produtoId, empresaId)
                                .orElseThrow(() -> new RecursoNaoEncontradoException(
                                                "Produto não encontrado nesta empresa."));
        }

        @Transactional
        public Produto atualizar(
                        Long empresaId,
                        Long produtoId,
                        Long fornecedorId,
                        Produto dadosAtualizados) {

                Produto produto = buscarPorId(empresaId, produtoId);
                Long categoriaId = produto.getCategoria() == null ? null : produto.getCategoria().getId();
                return atualizar(empresaId, produtoId, fornecedorId, dadosAtualizados, categoriaId);
        }

        @Transactional
        public Produto atualizar(
                        Long empresaId,
                        Long produtoId,
                        Long fornecedorId,
                        Produto dadosAtualizados,
                        Long categoriaId) {

                Produto produto = buscarPorId(empresaId, produtoId);

                validarDados(dadosAtualizados);

                Fornecedor fornecedor = buscarFornecedorAtivo(
                                empresaId, fornecedorId);

                String sku = normalizarSku(dadosAtualizados.getSku());

                boolean skuDuplicado = produtoRepository.existsByEmpresaIdAndSkuAndIdNot(
                                empresaId, sku, produtoId);

                if (skuDuplicado) {
                        throw new RecursoConflitoException(
                                        "Já existe outro produto com esse SKU nesta empresa.");
                }

                produto.setNome(dadosAtualizados.getNome().trim());
                produto.setSku(sku);
                produto.setDescricao(
                                normalizarDescricao(dadosAtualizados.getDescricao()));
                produto.setPrecoCusto(dadosAtualizados.getPrecoCusto());
                produto.setPrecoVenda(dadosAtualizados.getPrecoVenda());
                produto.setEstoqueMinimo(dadosAtualizados.getEstoqueMinimo());
                produto.setFornecedor(fornecedor);
                produto.setCategoria(buscarCategoria(empresaId, categoriaId, produto.getCategoria()));

                Produto produtoSalvo = produtoRepository.save(produto);

                auditoriaService.registrar(
                                produtoSalvo.getEmpresa(),
                                "PRODUTO",
                                produtoSalvo.getId(),
                                "ATUALIZAR",
                                "Produto atualizado: " + produtoSalvo.getNome());

                return produtoSalvo;
        }

        @Transactional
        public void desativar(Long empresaId, Long produtoId) {

                Produto produto = buscarPorId(empresaId, produtoId);

                produto.setAtivo(false);

                produtoRepository.save(produto);

                auditoriaService.registrar(
                                produto.getEmpresa(),
                                "PRODUTO",
                                produto.getId(),
                                "DESATIVAR",
                                "Produto desativado: " + produto.getNome());
        }

        private Empresa buscarEmpresaAtiva(Long empresaId) {

                return empresaRepository
                                .findByIdAndAtivoTrue(empresaId)
                                .orElseThrow(() -> new RecursoNaoEncontradoException(
                                                "Empresa não encontrada ou inativa."));
        }

        private Fornecedor buscarFornecedorAtivo(
                        Long empresaId,
                        Long fornecedorId) {

                return fornecedorRepository
                                .findByIdAndEmpresaIdAndAtivoTrue(
                                                fornecedorId, empresaId)
                                .orElseThrow(() -> new RecursoNaoEncontradoException(
                                                "Fornecedor não encontrado ou inativo nesta empresa."));
        }

        private Categoria buscarCategoria(Long empresaId, Long categoriaId, Categoria categoriaAtual) {
                if (categoriaId == null) {
                        return null;
                }

                Categoria categoria = categoriaRepository.findByIdAndEmpresaId(categoriaId, empresaId)
                                .orElseThrow(() -> new RecursoNaoEncontradoException(
                                                "Categoria não encontrada nesta empresa."));

                boolean mesmaCategoria = categoriaAtual != null && categoriaId.equals(categoriaAtual.getId());
                if (!Boolean.TRUE.equals(categoria.getAtivo()) && !mesmaCategoria) {
                        throw new RecursoNaoEncontradoException("Categoria não encontrada ou inativa nesta empresa.");
                }

                return categoria;
        }

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

                if (produto.getEstoqueMinimo() == null) {

                        throw new IllegalArgumentException(
                                        "O estoque mínimo é obrigatório.");
                }

                if (produto.getEstoqueMinimo() < 0) {

                        throw new IllegalArgumentException(
                                        "O estoque mínimo não pode ser negativo.");
                }
        }

        private String normalizarSku(String sku) {

                return sku.trim().toUpperCase(Locale.ROOT);
        }

        private void validarPreco(BigDecimal preco, String tipo) {

                if (preco == null ||
                                preco.compareTo(BigDecimal.ZERO) < 0) {

                        throw new IllegalArgumentException(
                                        "O preço de " + tipo +
                                                        " não pode ser negativo ou nulo.");
                }
        }

        private String normalizarDescricao(String descricao) {

                if (descricao == null || descricao.isBlank()) {
                        return null;
                }

                return descricao.trim();
        }
}

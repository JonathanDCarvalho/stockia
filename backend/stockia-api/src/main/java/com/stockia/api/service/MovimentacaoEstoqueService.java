
package com.stockia.api.service;

import com.stockia.api.dto.request.MovimentacaoEstoqueRequest;
import com.stockia.api.entity.Empresa;
import com.stockia.api.entity.MovimentacaoEstoque;
import com.stockia.api.entity.Produto;
import com.stockia.api.exception.RecursoNaoEncontradoException;
import com.stockia.api.repository.EmpresaRepository;
import com.stockia.api.repository.MovimentacaoEstoqueRepository;
import com.stockia.api.repository.ProdutoRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MovimentacaoEstoqueService {

    private final MovimentacaoEstoqueRepository movimentacaoRepository;
    private final ProdutoRepository produtoRepository;
    private final EmpresaRepository empresaRepository;
    private final AuditoriaService auditoriaService;

    public MovimentacaoEstoqueService(
            MovimentacaoEstoqueRepository movimentacaoRepository,
            ProdutoRepository produtoRepository,
            EmpresaRepository empresaRepository,
            AuditoriaService auditoriaService) {

        this.movimentacaoRepository = movimentacaoRepository;
        this.produtoRepository = produtoRepository;
        this.empresaRepository = empresaRepository;
        this.auditoriaService = auditoriaService;
    }

    @Transactional
    public MovimentacaoEstoque registrar(
            Long empresaId,
            Long produtoId,
            MovimentacaoEstoqueRequest request) {

        Empresa empresa = empresaRepository
                .findByIdAndAtivoTrue(empresaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Empresa não encontrada ou inativa."));

        Produto produto = produtoRepository
                .buscarParaMovimentacao(produtoId, empresaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Produto não encontrado ou inativo nesta empresa."));

        validarRequest(request);

        int estoqueAnterior = produto.getQuantidadeEstoque();
        int estoquePosterior;

        switch (request.tipo()) {

            case ENTRADA -> {

                if (request.quantidade() <= 0) {
                    throw new IllegalArgumentException(
                            "A quantidade da entrada deve ser maior que zero.");
                }

                try {
                    estoquePosterior = Math.addExact(
                            estoqueAnterior, request.quantidade());

                } catch (ArithmeticException e) {
                    throw new IllegalArgumentException(
                            "A quantidade final do estoque excede o limite permitido.");
                }
            }

            case SAIDA -> {

                if (request.quantidade() <= 0) {
                    throw new IllegalArgumentException(
                            "A quantidade da saída deve ser maior que zero.");
                }

                if (request.quantidade() > estoqueAnterior) {
                    throw new IllegalArgumentException(
                            "Estoque insuficiente para realizar esta saída.");
                }

                estoquePosterior = estoqueAnterior - request.quantidade();
            }

            case AJUSTE -> {
                estoquePosterior = request.quantidade();
            }

            default -> throw new IllegalArgumentException(
                    "Tipo de movimentação inválido.");
        }

        MovimentacaoEstoque movimentacao = new MovimentacaoEstoque();

        movimentacao.setEmpresa(empresa);
        movimentacao.setProduto(produto);
        movimentacao.setTipo(request.tipo());
        movimentacao.setQuantidade(request.quantidade());
        movimentacao.setEstoqueAnterior(estoqueAnterior);
        movimentacao.setEstoquePosterior(estoquePosterior);
        movimentacao.setObservacao(
                normalizarObservacao(request.observacao()));

        produto.setQuantidadeEstoque(estoquePosterior);

        produtoRepository.save(produto);

        MovimentacaoEstoque movimentacaoSalva = movimentacaoRepository.save(movimentacao);

        String descricao = String.format(
                "%s de estoque do produto %s (ID: %d). " +
                        "Quantidade: %d. Estoque anterior: %d. Estoque posterior: %d.",
                request.tipo(),
                produto.getNome(),
                produto.getId(),
                request.quantidade(),
                estoqueAnterior,
                estoquePosterior);

        auditoriaService.registrar(
                empresa,
                "MOVIMENTACAO_ESTOQUE",
                movimentacaoSalva.getId(),
                "MOVIMENTAR_ESTOQUE",
                descricao);

        return movimentacaoSalva;
    }

    @Transactional(readOnly = true)
    public List<MovimentacaoEstoque> listarPorEmpresa(Long empresaId) {

        buscarEmpresaAtiva(empresaId);

        return movimentacaoRepository
                .findAllByEmpresaIdOrderByCreatedAtDesc(empresaId);
    }

    @Transactional(readOnly = true)
    public List<MovimentacaoEstoque> listarPorProduto(
            Long empresaId,
            Long produtoId) {

        buscarEmpresaAtiva(empresaId);

        produtoRepository.findByIdAndEmpresaIdAndAtivoTrue(
                produtoId, empresaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Produto não encontrado nesta empresa."));

        return movimentacaoRepository
                .findAllByEmpresaIdAndProdutoIdOrderByCreatedAtDesc(
                        empresaId, produtoId);
    }

    private Empresa buscarEmpresaAtiva(Long empresaId) {

        return empresaRepository.findByIdAndAtivoTrue(empresaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Empresa não encontrada ou inativa."));
    }

    private void validarRequest(MovimentacaoEstoqueRequest request) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Os dados da movimentação são obrigatórios.");
        }

        if (request.tipo() == null) {
            throw new IllegalArgumentException(
                    "O tipo de movimentação é obrigatório.");
        }

        if (request.quantidade() == null || request.quantidade() < 0) {
            throw new IllegalArgumentException(
                    "A quantidade deve ser maior ou igual a zero.");
        }
    }

    private String normalizarObservacao(String observacao) {

        if (observacao == null || observacao.isBlank()) {
            return null;
        }

        return observacao.trim();
    }
}

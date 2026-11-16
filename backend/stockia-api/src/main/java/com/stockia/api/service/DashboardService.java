
package com.stockia.api.service;

import com.stockia.api.dto.response.DashboardResumoResponse;
import com.stockia.api.exception.RecursoNaoEncontradoException;
import com.stockia.api.repository.CategoriaRepository;
import com.stockia.api.repository.EmpresaRepository;
import com.stockia.api.repository.FornecedorRepository;
import com.stockia.api.repository.ProdutoRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class DashboardService {

    private final ProdutoRepository produtoRepository;
    private final CategoriaRepository categoriaRepository;
    private final FornecedorRepository fornecedorRepository;
    private final EmpresaRepository empresaRepository;

    public DashboardService(
            ProdutoRepository produtoRepository,
            CategoriaRepository categoriaRepository,
            FornecedorRepository fornecedorRepository,
            EmpresaRepository empresaRepository) {

        this.produtoRepository = produtoRepository;
        this.categoriaRepository = categoriaRepository;
        this.fornecedorRepository = fornecedorRepository;
        this.empresaRepository = empresaRepository;
    }

    @Transactional(readOnly = true)
    public DashboardResumoResponse buscarResumo(Long empresaId) {

        validarEmpresa(empresaId);

        BigDecimal valorTotalEstoque = produtoRepository.calcularValorTotalEstoque(empresaId);

        Long produtosCadastrados = produtoRepository.contarProdutosAtivos(empresaId);

        Long unidadesEmEstoque = produtoRepository.somarUnidadesEmEstoque(empresaId);

        Long estoqueBaixo = produtoRepository.contarProdutosEstoqueBaixo(empresaId);

        long categoriasAtivas = categoriaRepository.countByEmpresaIdAndAtivoTrue(empresaId);

        long fornecedoresAtivos = fornecedorRepository.countByEmpresaIdAndAtivoTrue(empresaId);

        return new DashboardResumoResponse(
                valorTotalEstoque != null
                        ? valorTotalEstoque
                        : BigDecimal.ZERO,

                produtosCadastrados,

                unidadesEmEstoque != null
                        ? unidadesEmEstoque
                        : 0L,

                estoqueBaixo,
                categoriasAtivas,
                fornecedoresAtivos);
    }

    private void validarEmpresa(Long empresaId) {

        empresaRepository.findByIdAndAtivoTrue(empresaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Empresa não encontrada ou inativa."));
    }
}

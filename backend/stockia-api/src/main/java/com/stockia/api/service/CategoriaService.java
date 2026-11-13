
package com.stockia.api.service;

import com.stockia.api.entity.Categoria;
import com.stockia.api.entity.Empresa;
import com.stockia.api.exception.RecursoConflitoException;
import com.stockia.api.exception.RecursoNaoEncontradoException;
import com.stockia.api.repository.CategoriaRepository;
import com.stockia.api.repository.EmpresaRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final EmpresaRepository empresaRepository;

    public CategoriaService(
            CategoriaRepository categoriaRepository,
            EmpresaRepository empresaRepository) {

        this.categoriaRepository = categoriaRepository;
        this.empresaRepository = empresaRepository;
    }

    @Transactional
    public Categoria cadastrar(Long empresaId, Categoria categoria) {

        Empresa empresa = buscarEmpresaAtiva(empresaId);

        validarDados(categoria);

        String nome = normalizarNome(categoria.getNome());

        if (categoriaRepository.existsByEmpresaIdAndNome(empresaId, nome)) {
            throw new RecursoConflitoException(
                    "Já existe uma categoria com esse nome nesta empresa.");
        }

        categoria.setNome(nome);
        categoria.setDescricao(normalizarDescricao(categoria.getDescricao()));
        categoria.setAtivo(true);
        categoria.setEmpresa(empresa);

        return categoriaRepository.save(categoria);
    }

    @Transactional(readOnly = true)
    public List<Categoria> listarPorEmpresa(Long empresaId) {

        buscarEmpresaAtiva(empresaId);

        return categoriaRepository.findAllByEmpresaIdAndAtivoTrue(empresaId);
    }

    @Transactional(readOnly = true)
    public Categoria buscarPorId(Long empresaId, Long categoriaId) {

        buscarEmpresaAtiva(empresaId);

        return categoriaRepository
                .findByIdAndEmpresaIdAndAtivoTrue(categoriaId, empresaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Categoria não encontrada nesta empresa."));
    }

    @Transactional
    public Categoria atualizar(
            Long empresaId,
            Long categoriaId,
            Categoria dadosAtualizados) {

        Categoria categoria = buscarPorId(empresaId, categoriaId);

        validarDados(dadosAtualizados);

        String nome = normalizarNome(dadosAtualizados.getNome());

        boolean nomeDuplicado = categoriaRepository.existsByEmpresaIdAndNomeAndIdNot(
                empresaId, nome, categoriaId);

        if (nomeDuplicado) {
            throw new RecursoConflitoException(
                    "Já existe outra categoria com esse nome nesta empresa.");
        }

        categoria.setNome(nome);
        categoria.setDescricao(
                normalizarDescricao(dadosAtualizados.getDescricao()));

        return categoriaRepository.save(categoria);
    }

    @Transactional
    public void desativar(Long empresaId, Long categoriaId) {

        Categoria categoria = buscarPorId(empresaId, categoriaId);

        categoria.setAtivo(false);

        categoriaRepository.save(categoria);
    }

    private Empresa buscarEmpresaAtiva(Long empresaId) {

        return empresaRepository
                .findByIdAndAtivoTrue(empresaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Empresa não encontrada ou inativa."));
    }

    private void validarDados(Categoria categoria) {

        if (categoria.getNome() == null || categoria.getNome().isBlank()) {
            throw new IllegalArgumentException(
                    "O nome da categoria é obrigatório.");
        }

        if (categoria.getNome().trim().length() > 100) {
            throw new IllegalArgumentException(
                    "O nome deve ter no máximo 100 caracteres.");
        }
    }

    private String normalizarNome(String nome) {
        return nome.trim();
    }

    private String normalizarDescricao(String descricao) {

        if (descricao == null || descricao.isBlank()) {
            return null;
        }

        return descricao.trim();
    }
}

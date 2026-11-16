
package com.stockia.api.service;

import com.stockia.api.entity.Empresa;
import com.stockia.api.exception.RecursoNaoEncontradoException;
import com.stockia.api.repository.EmpresaRepository;
import com.stockia.api.validation.CnpjValidator;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmpresaService {

    private final EmpresaRepository empresaRepository;
    private final AuditoriaService auditoriaService;

    public EmpresaService(
            EmpresaRepository empresaRepository,
            AuditoriaService auditoriaService) {

        this.empresaRepository = empresaRepository;
        this.auditoriaService = auditoriaService;
    }

    @Transactional
    public Empresa cadastrar(Empresa empresa) {

        if (empresa.getNome() == null || empresa.getNome().isBlank()) {
            throw new IllegalArgumentException(
                    "O nome da empresa é obrigatório.");
        }

        empresa.setNome(empresa.getNome().trim());

        if (empresa.getCnpj() != null && !empresa.getCnpj().isBlank()) {

            String cnpj = CnpjValidator.normalizar(empresa.getCnpj());

            if (!CnpjValidator.isValid(cnpj)) {
                throw new IllegalArgumentException(
                        "O CNPJ informado é inválido.");
            }

            if (empresaRepository.existsByCnpj(cnpj)) {
                throw new IllegalArgumentException(
                        "Já existe uma empresa cadastrada com esse CNPJ.");
            }

            empresa.setCnpj(cnpj);

        } else {
            empresa.setCnpj(null);
        }

        Empresa empresaSalva = empresaRepository.save(empresa);

        auditoriaService.registrar(
                empresaSalva,
                "EMPRESA",
                empresaSalva.getId(),
                "CRIAR",
                "Empresa cadastrada: " + empresaSalva.getNome());

        return empresaSalva;
    }

    @Transactional(readOnly = true)
    public List<Empresa> listarTodos() {

        return empresaRepository.findAllByAtivoTrue();
    }

    @Transactional(readOnly = true)
    public Empresa buscarPorId(Long id) {

        return empresaRepository.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Empresa não encontrada."));
    }

    @Transactional
    public Empresa atualizar(Long id, Empresa dadosAtualizados) {

        Empresa empresa = buscarPorId(id);

        if (dadosAtualizados.getNome() == null ||
                dadosAtualizados.getNome().isBlank()) {

            throw new IllegalArgumentException(
                    "O nome da empresa é obrigatório.");
        }

        empresa.setNome(dadosAtualizados.getNome().trim());

        Empresa empresaSalva = empresaRepository.save(empresa);

        auditoriaService.registrar(
                empresaSalva,
                "EMPRESA",
                empresaSalva.getId(),
                "ATUALIZAR",
                "Empresa atualizada: " + empresaSalva.getNome());

        return empresaSalva;
    }

    @Transactional
    public void desativar(Long id) {

        Empresa empresa = buscarPorId(id);

        empresa.setAtivo(false);

        empresaRepository.save(empresa);

        auditoriaService.registrar(
                empresa,
                "EMPRESA",
                empresa.getId(),
                "DESATIVAR",
                "Empresa desativada: " + empresa.getNome());
    }
}

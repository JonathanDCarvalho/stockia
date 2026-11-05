package com.stockia.api.service;

import com.stockia.api.entity.Empresa;
import com.stockia.api.repository.EmpresaRepository;
import com.stockia.api.validation.CnpjValidator;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmpresaService {

    private final EmpresaRepository empresaRepository;

    public EmpresaService(EmpresaRepository empresaRepository) {
        this.empresaRepository = empresaRepository;
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

        return empresaRepository.save(empresa);
    }

    @Transactional(readOnly = true)
    public List<Empresa> listarTodos() {
        return empresaRepository.findAll();
    }
}
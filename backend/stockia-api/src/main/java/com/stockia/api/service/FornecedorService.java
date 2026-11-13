
package com.stockia.api.service;

import com.stockia.api.entity.Empresa;
import com.stockia.api.entity.Fornecedor;
import com.stockia.api.exception.RecursoConflitoException;
import com.stockia.api.exception.RecursoNaoEncontradoException;
import com.stockia.api.repository.EmpresaRepository;
import com.stockia.api.repository.FornecedorRepository;
import com.stockia.api.validation.CnpjValidator;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class FornecedorService {

    private final FornecedorRepository fornecedorRepository;
    private final EmpresaRepository empresaRepository;

    public FornecedorService(
            FornecedorRepository fornecedorRepository,
            EmpresaRepository empresaRepository) {

        this.fornecedorRepository = fornecedorRepository;
        this.empresaRepository = empresaRepository;
    }

    @Transactional
    public Fornecedor cadastrar(Long empresaId, Fornecedor fornecedor) {

        Empresa empresa = buscarEmpresaAtiva(empresaId);

        validarDados(fornecedor);

        String documento = normalizarDocumento(fornecedor.getDocumento());

        validarDocumento(documento);

        if (documento != null &&
                fornecedorRepository.existsByEmpresaIdAndDocumento(
                        empresaId, documento)) {

            throw new RecursoConflitoException(
                    "Já existe um fornecedor com esse documento nesta empresa.");
        }

        fornecedor.setNome(normalizarNome(fornecedor.getNome()));
        fornecedor.setDocumento(documento);
        fornecedor.setEmail(normalizarTexto(fornecedor.getEmail()));
        fornecedor.setTelefone(normalizarTexto(fornecedor.getTelefone()));
        fornecedor.setContato(normalizarTexto(fornecedor.getContato()));

        fornecedor.setAtivo(true);
        fornecedor.setEmpresa(empresa);

        return fornecedorRepository.save(fornecedor);
    }

    @Transactional(readOnly = true)
    public List<Fornecedor> listarPorEmpresa(Long empresaId) {

        buscarEmpresaAtiva(empresaId);

        return fornecedorRepository.findAllByEmpresaIdAndAtivoTrue(empresaId);
    }

    @Transactional(readOnly = true)
    public Fornecedor buscarPorId(Long empresaId, Long fornecedorId) {

        buscarEmpresaAtiva(empresaId);

        return fornecedorRepository
                .findByIdAndEmpresaIdAndAtivoTrue(fornecedorId, empresaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Fornecedor não encontrado nesta empresa."));
    }

    @Transactional
    public Fornecedor atualizar(
            Long empresaId,
            Long fornecedorId,
            Fornecedor dadosAtualizados) {

        Fornecedor fornecedor = buscarPorId(empresaId, fornecedorId);

        validarDados(dadosAtualizados);

        String documento = normalizarDocumento(
                dadosAtualizados.getDocumento());

        validarDocumento(documento);

        boolean documentoDuplicado = documento != null &&
                fornecedorRepository.existsByEmpresaIdAndDocumentoAndIdNot(
                        empresaId, documento, fornecedorId);

        if (documentoDuplicado) {
            throw new RecursoConflitoException(
                    "Já existe outro fornecedor com esse documento nesta empresa.");
        }

        fornecedor.setNome(normalizarNome(dadosAtualizados.getNome()));
        fornecedor.setDocumento(documento);
        fornecedor.setEmail(normalizarTexto(dadosAtualizados.getEmail()));
        fornecedor.setTelefone(normalizarTexto(dadosAtualizados.getTelefone()));
        fornecedor.setContato(normalizarTexto(dadosAtualizados.getContato()));

        return fornecedorRepository.save(fornecedor);
    }

    @Transactional
    public void desativar(Long empresaId, Long fornecedorId) {

        Fornecedor fornecedor = buscarPorId(empresaId, fornecedorId);

        fornecedor.setAtivo(false);

        fornecedorRepository.save(fornecedor);
    }

    private Empresa buscarEmpresaAtiva(Long empresaId) {

        return empresaRepository.findByIdAndAtivoTrue(empresaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Empresa não encontrada ou inativa."));
    }

    private void validarDados(Fornecedor fornecedor) {

        if (fornecedor.getNome() == null ||
                fornecedor.getNome().isBlank()) {

            throw new IllegalArgumentException(
                    "O nome do fornecedor é obrigatório.");
        }

        if (fornecedor.getNome().trim().length() > 150) {

            throw new IllegalArgumentException(
                    "O nome deve ter no máximo 150 caracteres.");
        }
    }

    private String normalizarNome(String nome) {

        return nome.trim();
    }

    private String normalizarTexto(String texto) {

        if (texto == null || texto.isBlank()) {
            return null;
        }

        return texto.trim();
    }

    private String normalizarDocumento(String documento) {

        if (documento == null || documento.isBlank()) {
            return null;
        }

        return CnpjValidator.normalizar(documento);
    }

    private void validarDocumento(String documento) {

        if (documento == null) {
            return;
        }

        if (documento.length() == 11) {

            if (!validarCpf(documento)) {
                throw new IllegalArgumentException(
                        "O CPF informado é inválido.");
            }

        } else if (documento.length() == 14) {

            if (!CnpjValidator.isValid(documento)) {
                throw new IllegalArgumentException(
                        "O CNPJ informado é inválido.");
            }

        } else {

            throw new IllegalArgumentException(
                    "Informe um CPF com 11 dígitos ou um CNPJ com 14 caracteres.");
        }
    }

    private boolean validarCpf(String cpf) {

        if (!cpf.matches("[0-9]{11}")) {
            return false;
        }

        if (cpf.chars().distinct().count() == 1) {
            return false;
        }

        int soma = 0;

        for (int i = 0; i < 9; i++) {
            soma += Character.getNumericValue(cpf.charAt(i)) * (10 - i);
        }

        int primeiroDigito = calcularDigitoCpf(soma);

        soma = 0;

        for (int i = 0; i < 10; i++) {
            soma += Character.getNumericValue(cpf.charAt(i)) * (11 - i);
        }

        int segundoDigito = calcularDigitoCpf(soma);

        return primeiroDigito == Character.getNumericValue(cpf.charAt(9))
                && segundoDigito == Character.getNumericValue(cpf.charAt(10));
    }

    private int calcularDigitoCpf(int soma) {

        int resto = soma % 11;

        return resto < 2 ? 0 : 11 - resto;
    }
}

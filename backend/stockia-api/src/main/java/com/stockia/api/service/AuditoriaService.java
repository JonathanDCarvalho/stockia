
package com.stockia.api.service;

import com.stockia.api.entity.Auditoria;
import com.stockia.api.entity.Empresa;
import com.stockia.api.exception.RecursoNaoEncontradoException;
import com.stockia.api.repository.AuditoriaRepository;
import com.stockia.api.repository.EmpresaRepository;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuditoriaService {

    private final AuditoriaRepository auditoriaRepository;
    private final EmpresaRepository empresaRepository;

    public AuditoriaService(
            AuditoriaRepository auditoriaRepository,
            EmpresaRepository empresaRepository) {

        this.auditoriaRepository = auditoriaRepository;
        this.empresaRepository = empresaRepository;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void registrar(
            Empresa empresa,
            String entidade,
            Long registroId,
            String acao,
            String descricao) {

        Auditoria auditoria = new Auditoria();

        auditoria.setEmpresa(empresa);
        auditoria.setUsuarioId(obterUsuarioAutenticado());
        auditoria.setEntidade(entidade);
        auditoria.setRegistroId(registroId);
        auditoria.setAcao(acao);
        auditoria.setDescricao(descricao);

        auditoriaRepository.save(auditoria);
    }

    @Transactional(readOnly = true)
    public List<Auditoria> listarPorEmpresa(Long empresaId) {

        validarEmpresa(empresaId);

        return auditoriaRepository
                .findAllByEmpresaIdOrderByCreatedAtDescIdDesc(empresaId);
    }

    @Transactional(readOnly = true)
    public List<Auditoria> listarPorRegistro(
            Long empresaId,
            String entidade,
            Long registroId) {

        validarEmpresa(empresaId);

        return auditoriaRepository
                .findAllByEmpresaIdAndEntidadeAndRegistroIdOrderByCreatedAtDescIdDesc(
                        empresaId,
                        entidade,
                        registroId);
    }

    private Long obterUsuarioAutenticado() {

        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            return null;
        }

        try {
            return Long.valueOf(jwt.getSubject());
        } catch (NumberFormatException ex) {
            throw new IllegalStateException(
                    "JWT contém um identificador de usuário inválido.");
        }
    }

    private void validarEmpresa(Long empresaId) {

        empresaRepository.findByIdAndAtivoTrue(empresaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Empresa não encontrada ou inativa."));
    }
}

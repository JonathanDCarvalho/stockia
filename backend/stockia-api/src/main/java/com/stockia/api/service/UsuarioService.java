
package com.stockia.api.service;

import com.stockia.api.dto.request.UsuarioRequest;
import com.stockia.api.dto.response.UsuarioResponse;
import com.stockia.api.entity.Empresa;
import com.stockia.api.entity.Usuario;
import com.stockia.api.enums.PerfilUsuario;
import com.stockia.api.exception.RecursoConflitoException;
import com.stockia.api.exception.RecursoNaoEncontradoException;
import com.stockia.api.repository.EmpresaRepository;
import com.stockia.api.repository.UsuarioRepository;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final EmpresaRepository empresaRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditoriaService auditoriaService;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            EmpresaRepository empresaRepository,
            PasswordEncoder passwordEncoder,
            AuditoriaService auditoriaService) {

        this.usuarioRepository = usuarioRepository;
        this.empresaRepository = empresaRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditoriaService = auditoriaService;
    }

    @Transactional
    public UsuarioResponse cadastrar(
            Long empresaId,
            UsuarioRequest request) {

        Empresa empresa = buscarEmpresaAtiva(empresaId);

        String email = request.email()
                .trim()
                .toLowerCase(Locale.ROOT);

        if (usuarioRepository.existeEmail(email)) {
            throw new RecursoConflitoException(
                    "Este e-mail já está cadastrado.");
        }

        Usuario usuario = new Usuario();

        usuario.setEmpresa(empresa);
        usuario.setNome(request.nome().trim());
        usuario.setEmail(email);
        usuario.setSenha(
                passwordEncoder.encode(request.senha()));
        usuario.setPerfil(request.perfil());
        usuario.setAtivo(true);

        Usuario salvo = usuarioRepository.save(usuario);

        auditoriaService.registrar(
                empresa,
                "USUARIO",
                salvo.getId(),
                "CRIAR",
                "Usuário cadastrado: " + salvo.getEmail());

        return converterParaResponse(salvo);
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar(Long empresaId) {

        buscarEmpresaAtiva(empresaId);

        return usuarioRepository
                .findAllByEmpresaIdAndAtivoTrue(empresaId)
                .stream()
                .map(this::converterParaResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponse buscarPorId(
            Long empresaId,
            Long usuarioId) {

        buscarEmpresaAtiva(empresaId);

        Usuario usuario = buscarUsuarioAtivo(
                empresaId, usuarioId);

        return converterParaResponse(usuario);
    }

    @Transactional(readOnly = true)
    public UsuarioResponse buscarMeuPerfil() {

        Long usuarioId = obterUsuarioAutenticado();

        Usuario usuario = usuarioRepository
                .buscarPorIdComEmpresa(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Usuário não encontrado."));

        if (!Boolean.TRUE.equals(usuario.getAtivo())
                || !Boolean.TRUE.equals(
                        usuario.getEmpresa().getAtivo())) {
            throw new RecursoNaoEncontradoException(
                    "Usuário não encontrado ou inativo.");
        }

        return converterParaResponse(usuario);
    }

    @Transactional
    public void desativar(
            Long empresaId,
            Long usuarioId) {

        Empresa empresa = buscarEmpresaAtiva(empresaId);

        Usuario usuario = buscarUsuarioAtivo(
                empresaId, usuarioId);

        Long usuarioAutenticado = obterUsuarioAutenticado();

        if (usuario.getId().equals(usuarioAutenticado)) {
            throw new RecursoConflitoException(
                    "Você não pode desativar sua própria conta.");
        }

        if (usuario.getPerfil() == PerfilUsuario.ADMIN
                && usuarioRepository
                        .contarAdministradoresAtivos(empresaId) <= 1) {

            throw new RecursoConflitoException(
                    "Não é possível desativar o último administrador.");
        }

        usuario.setAtivo(false);
        usuarioRepository.save(usuario);

        auditoriaService.registrar(
                empresa,
                "USUARIO",
                usuario.getId(),
                "DESATIVAR",
                "Usuário desativado: " + usuario.getEmail());
    }

    private Empresa buscarEmpresaAtiva(Long empresaId) {

        return empresaRepository
                .findByIdAndAtivoTrue(empresaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Empresa não encontrada ou inativa."));
    }

    private Usuario buscarUsuarioAtivo(
            Long empresaId,
            Long usuarioId) {

        return usuarioRepository
                .findByIdAndEmpresaIdAndAtivoTrue(
                        usuarioId, empresaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Usuário não encontrado nesta empresa."));
    }

    private Long obterUsuarioAutenticado() {

        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()) {
            throw new IllegalStateException(
                    "Usuário não autenticado.");
        }

        try {
            return Long.valueOf(authentication.getName());
        } catch (NumberFormatException ex) {
            throw new IllegalStateException(
                    "Identificador do usuário inválido.");
        }
    }

    private UsuarioResponse converterParaResponse(
            Usuario usuario) {

        return new UsuarioResponse(
                usuario.getId(),
                usuario.getEmpresa().getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getPerfil(),
                usuario.getAtivo(),
                usuario.getCreatedAt());
    }
}

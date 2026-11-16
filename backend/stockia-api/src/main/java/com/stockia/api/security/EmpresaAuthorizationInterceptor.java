
package com.stockia.api.security;

import com.stockia.api.entity.Usuario;
import com.stockia.api.enums.PerfilUsuario;
import com.stockia.api.repository.UsuarioRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Map;

@Component
public class EmpresaAuthorizationInterceptor implements HandlerInterceptor {

    private final UsuarioRepository usuarioRepository;

    public EmpresaAuthorizationInterceptor(
            UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) throws Exception {

        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()) {
            response.sendError(401, "Não autenticado.");
            return false;
        }

        Long usuarioId;

        try {
            usuarioId = Long.valueOf(authentication.getName());
        } catch (NumberFormatException ex) {
            response.sendError(403, "Usuário inválido.");
            return false;
        }

        Usuario usuario = usuarioRepository
                .buscarPorIdComEmpresa(usuarioId)
                .orElse(null);

        if (usuario == null
                || !Boolean.TRUE.equals(usuario.getAtivo())
                || !Boolean.TRUE.equals(usuario.getEmpresa().getAtivo())) {
            response.sendError(403, "Acesso negado.");
            return false;
        }

        @SuppressWarnings("unchecked")
        Map<String, String> variaveis = (Map<String, String>) request.getAttribute(
                HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);

        if (variaveis == null) {
            response.sendError(403, "Acesso negado.");
            return false;
        }

        String idEmpresa = variaveis.get("empresaId");

        if (idEmpresa == null) {
            idEmpresa = variaveis.get("id");
        }

        if (idEmpresa == null) {
            response.sendError(403, "Acesso negado.");
            return false;
        }

        Long empresaId;

        try {
            empresaId = Long.valueOf(idEmpresa);
        } catch (NumberFormatException ex) {
            response.sendError(403, "Empresa inválida.");
            return false;
        }

        if (!usuario.getEmpresa().getId().equals(empresaId)) {
            response.sendError(
                    403,
                    "Você não possui acesso a esta empresa.");
            return false;
        }

        String caminho = request.getRequestURI()
                .substring(request.getContextPath().length());

        boolean rotaEmpresaDireta = caminho.matches("/api/empresas/\\d+/?");

        boolean metodoAlteracao = request.getMethod().equals("PUT")
                || request.getMethod().equals("PATCH")
                || request.getMethod().equals("DELETE");

        if (rotaEmpresaDireta
                && metodoAlteracao
                && usuario.getPerfil() != PerfilUsuario.ADMIN) {

            response.sendError(
                    403,
                    "Esta operação exige perfil ADMIN.");
            return false;
        }

        if (caminho.matches(
                "/api/empresas/\\d+/auditoria(?:/.*)?")
                && usuario.getPerfil() != PerfilUsuario.ADMIN) {

            response.sendError(
                    403,
                    "Esta operação exige perfil ADMIN.");
            return false;
        }

        if (caminho.matches(
                "/api/empresas/\\d+/usuarios(?:/.*)?")
                && usuario.getPerfil() != PerfilUsuario.ADMIN) {

            response.sendError(
                    403,
                    "Gerenciamento de usuários exige perfil ADMIN.");

            return false;
        }

        return true;
    }
}

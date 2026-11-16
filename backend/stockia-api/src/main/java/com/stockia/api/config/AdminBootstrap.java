
package com.stockia.api.config;

import com.stockia.api.entity.Empresa;
import com.stockia.api.entity.Usuario;
import com.stockia.api.enums.PerfilUsuario;
import com.stockia.api.repository.EmpresaRepository;
import com.stockia.api.repository.UsuarioRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Locale;

@Configuration
public class AdminBootstrap {

    @Bean
    public ApplicationRunner criarAdministradorInicial(
            UsuarioRepository usuarioRepository,
            EmpresaRepository empresaRepository,
            PasswordEncoder passwordEncoder,
            TransactionTemplate transactionTemplate,
            @Value("${BOOTSTRAP_ADMIN_EMAIL:}") String email,
            @Value("${BOOTSTRAP_ADMIN_PASSWORD:}") String senha,
            @Value("${BOOTSTRAP_EMPRESA_ID:0}") Long empresaId) {

        return args -> {

            if (email.isBlank() && senha.isBlank() && empresaId == 0) {
                return;
            }

            if (email.isBlank() || senha.isBlank() || empresaId <= 0) {
                throw new IllegalStateException(
                        "Configuração incompleta do administrador inicial.");
            }

            if (senha.length() < 12) {
                throw new IllegalStateException(
                        "A senha inicial deve ter pelo menos 12 caracteres.");
            }

            transactionTemplate.executeWithoutResult(status -> {

                String emailNormalizado = email.trim().toLowerCase(Locale.ROOT);

                if (usuarioRepository.existeEmail(emailNormalizado)) {
                    return;
                }

                Empresa empresa = empresaRepository
                        .findByIdAndAtivoTrue(empresaId)
                        .orElseThrow(() -> new IllegalStateException(
                                "Empresa inicial não encontrada ou inativa."));

                Usuario usuario = new Usuario();

                usuario.setEmpresa(empresa);
                usuario.setNome("Administrador");
                usuario.setEmail(emailNormalizado);
                usuario.setSenha(passwordEncoder.encode(senha));
                usuario.setPerfil(PerfilUsuario.ADMIN);
                usuario.setAtivo(true);

                usuarioRepository.save(usuario);
            });
        };
    }
}


package com.stockia.api.service;

import com.stockia.api.dto.request.LoginRequest;
import com.stockia.api.dto.response.LoginResponse;
import com.stockia.api.entity.Usuario;
import com.stockia.api.repository.UsuarioRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final long expirationMinutes;

    public AuthService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            JwtEncoder jwtEncoder,
            @Value("${app.jwt.expiration-minutes}") long expirationMinutes) {

        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
        this.expirationMinutes = expirationMinutes;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {

        String email = request.email()
                .trim()
                .toLowerCase(Locale.ROOT);

        Usuario usuario = usuarioRepository
                .buscarPorEmail(email)
                .orElseThrow(() -> new BadCredentialsException(
                        "E-mail ou senha inválidos."));

        if (!Boolean.TRUE.equals(usuario.getAtivo())
                || !Boolean.TRUE.equals(
                        usuario.getEmpresa().getAtivo())
                || !passwordEncoder.matches(
                        request.senha(), usuario.getSenha())) {

            throw new BadCredentialsException(
                    "E-mail ou senha inválidos.");
        }

        Instant agora = Instant.now();
        Instant expiracao = agora.plus(
                Duration.ofMinutes(expirationMinutes));

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("stockia-api")
                .issuedAt(agora)
                .expiresAt(expiracao)
                .subject(usuario.getId().toString())
                .claim("empresaId", usuario.getEmpresa().getId())
                .claim("perfil", usuario.getPerfil().name())
                .build();

        JwsHeader header = JwsHeader
                .with(MacAlgorithm.HS256)
                .build();

        String token = jwtEncoder
                .encode(JwtEncoderParameters.from(header, claims))
                .getTokenValue();

        return new LoginResponse(
                token,
                "Bearer",
                Duration.between(agora, expiracao).toSeconds(),
                usuario.getId(),
                usuario.getEmpresa().getId(),
                usuario.getNome(),
                usuario.getPerfil().name());
    }
}

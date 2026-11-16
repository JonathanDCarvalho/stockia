
package com.stockia.api.dto.response;

import com.stockia.api.enums.PerfilUsuario;
import java.time.LocalDateTime;

public record UsuarioResponse(
        Long id,
        Long empresaId,
        String nome,
        String email,
        PerfilUsuario perfil,
        Boolean ativo,
        LocalDateTime createdAt) {
}

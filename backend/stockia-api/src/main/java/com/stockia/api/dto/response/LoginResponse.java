
package com.stockia.api.dto.response;

public record LoginResponse(
        String token,
        String tipo,
        long expiresIn,
        Long usuarioId,
        Long empresaId,
        String nome,
        String perfil) {
}

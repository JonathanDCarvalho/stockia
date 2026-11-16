package com.stockia.api.dto.response;

import java.time.LocalDateTime;

public record AuditoriaResponse(
        Long id,
        Long empresaId,
        Long usuarioId,
        String entidade,
        Long registroId,
        String acao,
        String descricao,
        LocalDateTime createdAt) {
}
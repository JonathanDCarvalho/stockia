
package com.stockia.api.dto.response;

import java.time.LocalDateTime;

public record CategoriaResponse(
        Long id,
        Long empresaId,
        String nome,
        String descricao,
        Boolean ativo,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}

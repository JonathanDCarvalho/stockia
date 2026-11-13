
package com.stockia.api.dto.response;

import java.time.LocalDateTime;

public record FornecedorResponse(
        Long id,
        Long empresaId,
        String nome,
        String documento,
        String email,
        String telefone,
        String contato,
        Boolean ativo,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}

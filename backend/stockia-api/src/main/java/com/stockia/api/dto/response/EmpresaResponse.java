package com.stockia.api.dto.response;

import java.time.LocalDateTime;

public record EmpresaResponse(
        Long id,
        String nome,
        String cnpj,
        boolean ativo,
        LocalDateTime createdAt) {
}
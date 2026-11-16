
package com.stockia.api.dto.response;

import com.stockia.api.enums.TipoMovimentacao;

import java.time.LocalDateTime;

public record MovimentacaoEstoqueResponse(
        Long id,
        Long empresaId,
        Long produtoId,
        String produtoNome,
        TipoMovimentacao tipo,
        Integer quantidade,
        Integer estoqueAnterior,
        Integer estoquePosterior,
        String observacao,
        LocalDateTime createdAt) {
}


package com.stockia.api.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProdutoResponse(
        Long id,
        Long empresaId,
        String nome,
        String sku,
        String descricao,
        BigDecimal precoCusto,
        BigDecimal precoVenda,
        Integer quantidadeEstoque,
        Integer estoqueMinimo,
        Boolean ativo,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}

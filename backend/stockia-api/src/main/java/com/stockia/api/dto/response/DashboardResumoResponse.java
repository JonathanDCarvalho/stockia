
package com.stockia.api.dto.response;

import java.math.BigDecimal;

public record DashboardResumoResponse(
        BigDecimal valorTotalEstoque,
        Long produtosCadastrados,
        Long unidadesEmEstoque,
        Long estoqueBaixo,
        Long categoriasAtivas,
        Long fornecedoresAtivos) {
}


package com.stockia.api.dto.request;

import com.stockia.api.enums.TipoMovimentacao;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record MovimentacaoEstoqueRequest(

        @NotNull(message = "O tipo de movimentação é obrigatório.") TipoMovimentacao tipo,

        @NotNull(message = "A quantidade é obrigatória.") @PositiveOrZero(message = "A quantidade não pode ser negativa.") Integer quantidade,

        @Size(max = 500, message = "A observação deve ter no máximo 500 caracteres.") String observacao

) {
}


package com.stockia.api.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProdutoRequest(

        @NotBlank(message = "O nome do produto é obrigatório.") @Size(max = 150, message = "O nome deve ter no máximo 150 caracteres.") String nome,

        @NotBlank(message = "O SKU do produto é obrigatório.") @Size(max = 50, message = "O SKU deve ter no máximo 50 caracteres.") String sku,

        String descricao,

        @NotNull(message = "O preço de custo é obrigatório.") @DecimalMin(value = "0.00", message = "O preço de custo não pode ser negativo.") BigDecimal precoCusto,

        @NotNull(message = "O preço de venda é obrigatório.") @DecimalMin(value = "0.00", message = "O preço de venda não pode ser negativo.") BigDecimal precoVenda,

        @NotNull(message = "O estoque mínimo é obrigatório.") @PositiveOrZero(message = "O estoque mínimo não pode ser negativo.") Integer estoqueMinimo

) {
}

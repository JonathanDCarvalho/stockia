
package com.stockia.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record EmpresaRequest(

                @NotBlank(message = "O nome da empresa é obrigatório.") @Size(max = 150, message = "O nome deve ter no máximo 150 caracteres.") String nome,

                @Pattern(regexp = "^$|^[A-Za-z0-9./-]{12,20}$", message = "O CNPJ possui formato inválido.") String cnpj

) {
}


package com.stockia.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record FornecedorRequest(

        @NotBlank(message = "O nome do fornecedor é obrigatório.") @Size(max = 150, message = "O nome deve ter no máximo 150 caracteres.") String nome,

        @Pattern(regexp = "^$|^[A-Za-z0-9./\\-]+$", message = "O documento contém caracteres inválidos.") String documento,

        @Email(message = "Informe um endereço de e-mail válido.") @Size(max = 150, message = "O e-mail deve ter no máximo 150 caracteres.") String email,

        @Size(max = 20, message = "O telefone deve ter no máximo 20 caracteres.") String telefone,

        @Size(max = 100, message = "O contato deve ter no máximo 100 caracteres.") String contato

) {
}

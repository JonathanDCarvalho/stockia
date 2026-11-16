
package com.stockia.api.dto.request;

import com.stockia.api.enums.PerfilUsuario;
import jakarta.validation.constraints.*;

public record UsuarioRequest(
        @NotBlank @Size(max = 150) String nome,

        @NotBlank @Email @Size(max = 255) String email,

        @NotBlank @Size(min = 12, max = 72) String senha,

        @NotNull PerfilUsuario perfil) {
}

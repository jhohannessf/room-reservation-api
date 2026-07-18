package br.com.jhohannesfreitas.roomreservationapi.dto;

import br.com.jhohannesfreitas.roomreservationapi.domain.entity.Usuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UsuarioRequest(
        @NotBlank
        String nome,
        @NotBlank
        @Email
        String email,
        @NotBlank
        String senha
) {
}

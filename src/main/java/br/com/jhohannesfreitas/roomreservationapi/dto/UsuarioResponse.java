package br.com.jhohannesfreitas.roomreservationapi.dto;

public record UsuarioResponse(
        Long id,
        String nome,
        String email
) {
}

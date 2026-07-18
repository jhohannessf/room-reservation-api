package br.com.jhohannesfreitas.roomreservationapi.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record ReservaRequest(
        @NotNull
        Long usuarioId,

        @NotNull
        Long salaId,

        @NotNull
        LocalDate data,

        @NotNull
        LocalTime horaInicial,

        @NotNull
        LocalTime horaFinal,

        @NotNull
        Integer quantidadePessoas
) {
}

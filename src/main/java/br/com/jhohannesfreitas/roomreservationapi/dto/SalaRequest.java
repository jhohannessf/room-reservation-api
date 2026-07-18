package br.com.jhohannesfreitas.roomreservationapi.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record SalaRequest(
        @NotNull
        Integer numero,

        @NotNull
        @Positive
        Integer capacidade
) {
}

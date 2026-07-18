package br.com.jhohannesfreitas.roomreservationapi.dto;

import br.com.jhohannesfreitas.roomreservationapi.domain.enums.StatusReserva;

import java.time.LocalDate;
import java.time.LocalTime;

public record ReservaResponse(
        Long usuarioId,
        Long salaId,
        LocalDate data,
        LocalTime horaInicio,
        LocalTime horaFim,
        Integer quantidadePessoas,
        StatusReserva status
) {
}

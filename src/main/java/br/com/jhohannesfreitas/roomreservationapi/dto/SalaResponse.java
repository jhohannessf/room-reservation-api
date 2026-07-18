package br.com.jhohannesfreitas.roomreservationapi.dto;

import br.com.jhohannesfreitas.roomreservationapi.domain.enums.StatusSala;

public record SalaResponse(
        Long id,
        Integer numero,
        Integer capacidade,
        StatusSala status
) {
}

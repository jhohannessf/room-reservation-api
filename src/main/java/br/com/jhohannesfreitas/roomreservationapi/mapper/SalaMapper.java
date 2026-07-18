package br.com.jhohannesfreitas.roomreservationapi.mapper;

import br.com.jhohannesfreitas.roomreservationapi.domain.entity.Sala;
import br.com.jhohannesfreitas.roomreservationapi.dto.SalaRequest;
import br.com.jhohannesfreitas.roomreservationapi.dto.SalaResponse;
import org.springframework.stereotype.Component;

@Component
public class SalaMapper {

    public static Sala toEntity(SalaRequest dto) {
        return new Sala (
                dto.numero(),
                dto.capacidade()
        );
    }

    public static SalaResponse toResponse(Sala sala) {
        return new SalaResponse(
                sala.getId(),
                sala.getNumero(),
                sala.getCapacidade(),
                sala.getStatus()
        );
    }
}

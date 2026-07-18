package br.com.jhohannesfreitas.roomreservationapi.mapper;

import br.com.jhohannesfreitas.roomreservationapi.domain.entity.Reserva;
import br.com.jhohannesfreitas.roomreservationapi.domain.entity.Sala;
import br.com.jhohannesfreitas.roomreservationapi.domain.entity.Usuario;
import br.com.jhohannesfreitas.roomreservationapi.dto.ReservaRequest;
import br.com.jhohannesfreitas.roomreservationapi.dto.ReservaResponse;
import org.springframework.stereotype.Component;

@Component
public class ReservaMapper {

    public Reserva toEntity(ReservaRequest reservaRequest, Usuario usuario, Sala sala) {
        Reserva reserva = new  Reserva(
                reservaRequest.data(),
                reservaRequest.horaInicial(),
                reservaRequest.horaFinal(),
                reservaRequest.quantidadePessoas()
        );

        reserva.setUsuario(usuario);
        reserva.setSala(sala);

        return reserva;

    }

    public ReservaResponse toResponse(Reserva reserva){
        return new ReservaResponse(
                reserva.getUsuario().getId(),
                reserva.getSala().getId(),
                reserva.getData(),
                reserva.getHoraInicial(),
                reserva.getHoraFinal(),
                reserva.getQuantidadePessoas(),
                reserva.getStatus()
        );
    }
}

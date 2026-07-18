package br.com.jhohannesfreitas.roomreservationapi.repository;

import br.com.jhohannesfreitas.roomreservationapi.domain.entity.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {
}

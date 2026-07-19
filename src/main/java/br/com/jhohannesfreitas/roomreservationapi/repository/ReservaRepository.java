package br.com.jhohannesfreitas.roomreservationapi.repository;

import br.com.jhohannesfreitas.roomreservationapi.domain.entity.Reserva;
import br.com.jhohannesfreitas.roomreservationapi.domain.enums.StatusReserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    @Query(value =
            """
            SELECT *
            FROM reservas r
            WHERE r.sala_id = ?1 and r.data = ?2
            """, nativeQuery = true)
    List<Reserva> buscarReservasComMesmaSalaEData(Long id, LocalDate data);

    List<Reserva> findBySalaIdAndDataAndStatus(Long salaId, LocalDate data, StatusReserva status);

    List<Reserva> findBySalaIdAndDataAndStatusAndIdNot(Long salaId, LocalDate data, StatusReserva status, Long idReserva);
}

package br.com.jhohannesfreitas.roomreservationapi.repository;

import br.com.jhohannesfreitas.roomreservationapi.domain.entity.Sala;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SalaRepository extends JpaRepository<Sala, Long> {
    boolean existsByNumero(Integer numero);
}

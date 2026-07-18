package br.com.jhohannesfreitas.roomreservationapi.repository;

import br.com.jhohannesfreitas.roomreservationapi.domain.entity.Usuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    boolean existsByEmail(String email);

    Optional<Usuario> findByEmail(@NotBlank @Email String email);
}

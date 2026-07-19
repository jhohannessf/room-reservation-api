package br.com.jhohannesfreitas.roomreservationapi.controller;

import br.com.jhohannesfreitas.roomreservationapi.dto.ReservaRequest;
import br.com.jhohannesfreitas.roomreservationapi.dto.ReservaResponse;
import br.com.jhohannesfreitas.roomreservationapi.service.ReservaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reservas")
@Tag(name = "Reservas", description = "Operações relacionadas às reservas de salas")
public class ReservaController {

    private final ReservaService reservaService;

    public ReservaController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    @PostMapping
    @Operation(
            summary = "Cadastrar uma nova reserva",
            description = """
                Regras de negócio:
                
                - A sala deve estar com status LIVRE.
                - Não é permitido reservar datas no passado.
                - O horário da reserva deve estar entre 08:00 e 18:00.
                - A quantidade de pessoas não pode exceder a capacidade da sala.
                - Não é permitido haver reservas sobrepostas para a mesma sala.
                - Reservas utilizam intervalo semiaberto [início, fim).
                  Exemplo:
                  Reserva existente: 10:00 às 12:00
                  Nova reserva: 12:00 às 14:00
                  Resultado: permitido.
                """
    )
    public ResponseEntity<ReservaResponse> cadastrar(@RequestBody @Valid ReservaRequest reservaRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reservaService.cadastrar(reservaRequest));
    }

    @GetMapping
    public ResponseEntity<List<ReservaResponse>> listar() {
        return ResponseEntity.status(HttpStatus.OK)
                .body(reservaService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservaResponse> listarPorId(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(reservaService.listarPorId(id));
    }


}

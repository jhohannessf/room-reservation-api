package br.com.jhohannesfreitas.roomreservationapi.controller;

import br.com.jhohannesfreitas.roomreservationapi.dto.ReservaRequest;
import br.com.jhohannesfreitas.roomreservationapi.dto.ReservaResponse;
import br.com.jhohannesfreitas.roomreservationapi.service.ReservaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Reserva cadastrada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados da reserva inválidos"),
            @ApiResponse(responseCode = "404", description = "Usuário ou sala não encontrados"),
            @ApiResponse(responseCode = "409", description = "Conflito de horário ou regra de negócio")
    })
    public ResponseEntity<ReservaResponse> cadastrar(@RequestBody @Valid ReservaRequest reservaRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reservaService.cadastrar(reservaRequest));
    }

    @Operation(
            summary = "Listar reservas",
            description = """
                Retorna a lista de todas as reservas cadastradas no sistema.
                
                Observações:
                - Apenas reservas cadastradas são retornadas.
                - O retorno pode conter reservas com status ATIVA ou CANCELADA.
                - Caso não existam reservas cadastradas, será retornada uma lista vazia.
                """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de reservas retornada com sucesso")
    })
    @GetMapping
    public ResponseEntity<List<ReservaResponse>> listar() {
        return ResponseEntity.status(HttpStatus.OK)
                .body(reservaService.listar());
    }

    @Operation(
            summary = "Buscar reserva por ID",
            description = """
                Busca uma reserva pelo seu identificador.
                
                Regras de negócio:
                - A reserva deve existir.
                - Caso o ID informado não exista, será retornado HTTP 404 (Not Found).
                """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reserva encontrada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Reserva não encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ReservaResponse> listarPorId(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(reservaService.listarPorId(id));
    }
    @Operation(
            summary = "Atualizar reserva",
            description = """
        Atualiza uma reserva existente.

        Regras de negócio:
        - A reserva deve existir.
        - O usuário informado deve existir.
        - A sala informada deve existir.
        - A sala deve estar com status LIVRE.
        - Não é permitido alterar a reserva para uma data no passado.
        - O horário da reserva deve estar entre 08:00 e 18:00.
        - O horário inicial deve ser anterior ao horário final.
        - A quantidade de pessoas não pode exceder a capacidade da sala.
        - Não é permitido haver conflito de horários com outras reservas ATIVAS da mesma sala.
        - Na atualização, a própria reserva é desconsiderada na verificação de conflito de horários.
        - É permitido que uma reserva termine exatamente no horário em que outra se inicia (intervalo semiaberto [início, fim)).
        """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reserva atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Reserva, usuário ou sala não encontrados"),
            @ApiResponse(responseCode = "409", description = "Conflito de horário ou violação de regra de negócio")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ReservaResponse> atualizar(@PathVariable Long id, @RequestBody @Valid ReservaRequest reservaRequest) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(reservaService.atualizar(id, reservaRequest));
    }

    @Operation(
            summary = "Excluir reserva",
            description = """
                Remove uma reserva do sistema.
                
                Regras de negócio:
                - A reserva deve existir.
                - Caso o ID informado não exista, será retornado HTTP 404 (Not Found).
                - Quando a exclusão for realizada com sucesso, será retornado HTTP 204 (No Content).
                """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Reserva removida com sucesso"),
            @ApiResponse(responseCode = "404", description = "Reserva não encontrada")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarPorId(@PathVariable Long id) {
        reservaService.deletar(id);
        return ResponseEntity.noContent().build();
    }


}

package br.com.jhohannesfreitas.roomreservationapi.controller;

import br.com.jhohannesfreitas.roomreservationapi.dto.SalaRequest;
import br.com.jhohannesfreitas.roomreservationapi.dto.SalaResponse;
import br.com.jhohannesfreitas.roomreservationapi.service.SalaService;
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
@RequestMapping("/api/v1/salas")
@Tag(name = "Salas", description = "Operações relacionadas às salas")
public class SalaController {

    private final SalaService salaService;

    public SalaController(SalaService salaService) {
        this.salaService = salaService;
    }

    @Operation(
            summary = "Cadastrar sala",
            description = """
                Cadastra uma nova sala.

                Regras de negócio:
                - O número da sala deve ser único.
                - A capacidade deve ser maior que zero.
                """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Sala cadastrada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "409", description = "Já existe uma sala cadastrada com este número")
    })
    @PostMapping
    public ResponseEntity<SalaResponse> cadastrar(@RequestBody @Valid SalaRequest salaRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(salaService.cadastrar(salaRequest));
    }

    @Operation(
            summary = "Listar salas",
            description = "Retorna todas as salas cadastradas no sistema."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de salas retornada com sucesso")
    })
    @GetMapping
    public ResponseEntity<List<SalaResponse>> listar() {
        return ResponseEntity.status(HttpStatus.OK)
                .body(salaService.lista());
    }

    @Operation(
            summary = "Buscar sala por ID",
            description = """
                Busca uma sala pelo identificador informado.

                Regras de negócio:
                - A sala deve existir.
                """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sala encontrada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Sala não encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<SalaResponse> listarPorId(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(salaService.listarPorId(id));
    }

    @Operation(
            summary = "Atualizar sala",
            description = """
                Atualiza os dados de uma sala.

                Regras de negócio:
                - A sala deve existir.
                - O número da sala deve continuar sendo único.
                - A capacidade deve ser maior que zero.
                """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sala atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Sala não encontrada"),
            @ApiResponse(responseCode = "409", description = "Já existe outra sala cadastrada com este número")
    })
    @PutMapping("/{id}")
    public ResponseEntity<SalaResponse> atualizar(@PathVariable Long id, @RequestBody @Valid SalaRequest salaRequest) {
        return ResponseEntity.ok(salaService.atualizar(id, salaRequest));
    }

    @Operation(
            summary = "Excluir sala",
            description = """
                Remove uma sala do sistema.

                Regras de negócio:
                - A sala deve existir.
                """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Sala removida com sucesso"),
            @ApiResponse(responseCode = "404", description = "Sala não encontrada")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        salaService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}

package br.com.jhohannesfreitas.roomreservationapi.controller;

import br.com.jhohannesfreitas.roomreservationapi.dto.UsuarioRequest;
import br.com.jhohannesfreitas.roomreservationapi.dto.UsuarioResponse;
import br.com.jhohannesfreitas.roomreservationapi.service.UsuarioService;
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
@RequestMapping("/api/v1/usuarios")
@Tag(name = "Usuários", description = "Operações relacionadas aos usuários")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @Operation(
            summary = "Cadastrar usuário",
            description = """
                Cadastra um novo usuário no sistema.

                Regras de negócio:
                - O e-mail deve ser único.
                - Não é permitido cadastrar dois usuários com o mesmo e-mail.
                """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuário cadastrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "409", description = "Já existe um usuário cadastrado com este e-mail")
    })
    @PostMapping
    public ResponseEntity<UsuarioResponse> cadastrar(@RequestBody @Valid UsuarioRequest usuarioRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(usuarioService.cadastrar(usuarioRequest));
    }

    @Operation(
            summary = "Listar usuários",
            description = "Retorna todos os usuários cadastrados no sistema."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de usuários retornada com sucesso")
    })
    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> listar() {
        return ResponseEntity.status(HttpStatus.OK)
                .body(usuarioService.listar());
    }

    @Operation(
            summary = "Buscar usuário por ID",
            description = """
                Busca um usuário pelo identificador informado.

                Regras de negócio:
                - O usuário deve existir.
                """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário encontrado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> listarPorId(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(usuarioService.listarPorId(id));
    }

    @Operation(
            summary = "Atualizar usuário",
            description = """
                Atualiza os dados de um usuário.

                Regras de negócio:
                - O usuário deve existir.
                - O e-mail deve continuar sendo único.
                """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
            @ApiResponse(responseCode = "409", description = "Já existe outro usuário cadastrado com este e-mail")
    })
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponse> atualizar(@PathVariable Long id, @RequestBody @Valid UsuarioRequest usuarioRequest) {
        return ResponseEntity.ok(usuarioService.atualizar(id, usuarioRequest));
    }

    @Operation(
            summary = "Excluir usuário",
            description = """
                Remove um usuário do sistema.

                Regras de negócio:
                - O usuário deve existir.
                """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Usuário removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        usuarioService.deletar(id);
        return ResponseEntity.noContent().build();
    }


}

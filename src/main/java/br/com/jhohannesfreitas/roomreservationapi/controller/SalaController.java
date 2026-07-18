package br.com.jhohannesfreitas.roomreservationapi.controller;

import br.com.jhohannesfreitas.roomreservationapi.dto.SalaRequest;
import br.com.jhohannesfreitas.roomreservationapi.dto.SalaResponse;
import br.com.jhohannesfreitas.roomreservationapi.service.SalaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/salas")
public class SalaController {

    private final SalaService salaService;

    public SalaController(SalaService salaService) {
        this.salaService = salaService;
    }

    @PostMapping
    public ResponseEntity<SalaResponse> cadastrar(@RequestBody @Valid SalaRequest salaRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(salaService.cadastrar(salaRequest));
    }

    @GetMapping
    public ResponseEntity<List<SalaResponse>> listar() {
        return ResponseEntity.status(HttpStatus.OK)
                .body(salaService.lista());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SalaResponse> listarPorId(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(salaService.listarPorId(id));
    }
}

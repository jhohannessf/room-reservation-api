package br.com.jhohannesfreitas.roomreservationapi.service;

import br.com.jhohannesfreitas.roomreservationapi.domain.entity.Sala;
import br.com.jhohannesfreitas.roomreservationapi.dto.SalaRequest;
import br.com.jhohannesfreitas.roomreservationapi.dto.SalaResponse;
import br.com.jhohannesfreitas.roomreservationapi.exception.RegraNegocioException;
import br.com.jhohannesfreitas.roomreservationapi.mapper.SalaMapper;
import br.com.jhohannesfreitas.roomreservationapi.repository.SalaRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SalaService {

    private final SalaRepository salaRepository;

    public SalaService(SalaRepository salaRepository) {
        this.salaRepository = salaRepository;
    }


    public SalaResponse cadastrar(@Valid SalaRequest salaRequest) {
        // Verificar se a sala já existe
        verificaSalaExistentePorNumero(salaRequest);

        // Transformar o DTO em Entity
        Sala sala = SalaMapper.toEntity(salaRequest);

        // Salva
        Sala salaSalvo = salaRepository.save(sala);

        // Retorna o DTO de Resposta
        return SalaMapper.toResponse(salaSalvo);

    }

    public List<SalaResponse> lista() {

        // Busca a lista de Sala Entity, transformando em Resposta DTO e depois em uma lista
        return salaRepository.findAll().stream()
                .map(SalaMapper::toResponse) // sala -> SalaMapper.toResponse(sala)
                .toList();
    }

    public SalaResponse listarPorId(Long id) {
        // Verificar se a sala existe
        boolean verificarSala = salaRepository.existsById(id);
        if (!verificarSala) {
            throw new RegraNegocioException("Não existe sala com o id: " + id,
                    HttpStatus.NOT_FOUND);
        }
        // Pega a referência da sala no banco
        Sala sala = salaRepository.getReferenceById(id);

        // Retorna transformando a Entity Sala em um DTO SalaResponse
        return SalaMapper.toResponse(sala);

    }

    private void verificaSalaExistentePorNumero(SalaRequest salaRequest) {
        if (salaRepository.existsByNumero(salaRequest.numero())) {
            throw new RegraNegocioException("Sala já cadastrado com este número",
                    HttpStatus.CONFLICT);
        }
    }
}

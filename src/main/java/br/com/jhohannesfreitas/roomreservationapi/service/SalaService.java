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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class SalaService {

    private final SalaRepository salaRepository;

    public SalaService(SalaRepository salaRepository) {
        this.salaRepository = salaRepository;
    }

    @Transactional
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

    public List<SalaResponse> listar() {

        // Busca a lista de Sala Entity, transformando em Resposta DTO e depois em uma lista
        return salaRepository.findAll().stream()
                .map(SalaMapper::toResponse) // sala -> SalaMapper.toResponse(sala)
                .toList();
    }

    public SalaResponse listarPorId(Long id) {
        // Verificar se a sala existe com o método buscaPorId()
        // Retorna transformando a Entity Sala em um DTO SalaResponse usando o método toResponse do SalaMapper
        return SalaMapper.toResponse(buscaPorId(id));

    }

    @Transactional
    public SalaResponse atualizar(Long id, SalaRequest salaRequest) {
        // Buscar Sala por ID no banco
        Sala sala = buscaPorId(id);

        //Verificar se existe uma OUTRA sala com este número por ID
        verificaSeExisteOutraSalaComMesmoNumero(id, salaRequest.numero());

        // Settar os dados do Request na minha Entity
        sala.atualizar(salaRequest);

        // Salvo a minha Entity atualizada com os dados passados do UsuarioRequest(DTO) como parâmetro
        Sala salaAtualizada = salaRepository.save(sala);

        // Retorna a resposta transformando a minha Entity Sala em DTO SalaResponse
        return SalaMapper.toResponse(salaAtualizada);
    }

    @Transactional
    public void deletar(Long id) {
        // Captura a Busca se a sala existe no banco
        Sala sala = buscaPorId(id);

        // Se existe, deleta
        salaRepository.delete(sala);
    }

    public Sala buscaPorId(Long id) {
        return salaRepository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Sala com id " + id + " não encontrada.",
                        HttpStatus.NOT_FOUND));
    }

    private void verificaSalaExistentePorNumero(SalaRequest salaRequest) {
        if (salaRepository.existsByNumero(salaRequest.numero())) {
            throw new RegraNegocioException("Sala já cadastrada com este número.",
                    HttpStatus.CONFLICT);
        }
    }

    private void verificaSeExisteOutraSalaComMesmoNumero(Long id, Integer numero) {
        Optional<Sala> outraSala = salaRepository.findByNumero(numero);
        if (outraSala.isPresent()) {
            Sala salaComMesmoNumero = outraSala.get();
            if (!salaComMesmoNumero.getId().equals(id)) {
                throw new RegraNegocioException("Sala já cadastrada com este número.",
                        HttpStatus.CONFLICT);
            }
        }
    }
}

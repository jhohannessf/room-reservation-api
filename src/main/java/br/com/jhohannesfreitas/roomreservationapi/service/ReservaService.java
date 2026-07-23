package br.com.jhohannesfreitas.roomreservationapi.service;

import br.com.jhohannesfreitas.roomreservationapi.domain.entity.Reserva;
import br.com.jhohannesfreitas.roomreservationapi.domain.entity.Sala;
import br.com.jhohannesfreitas.roomreservationapi.domain.entity.Usuario;
import br.com.jhohannesfreitas.roomreservationapi.domain.enums.StatusReserva;
import br.com.jhohannesfreitas.roomreservationapi.domain.enums.StatusSala;
import br.com.jhohannesfreitas.roomreservationapi.dto.ReservaRequest;
import br.com.jhohannesfreitas.roomreservationapi.dto.ReservaResponse;
import br.com.jhohannesfreitas.roomreservationapi.exception.RegraNegocioException;
import br.com.jhohannesfreitas.roomreservationapi.mapper.ReservaMapper;
import br.com.jhohannesfreitas.roomreservationapi.repository.ReservaRepository;
import br.com.jhohannesfreitas.roomreservationapi.repository.SalaRepository;
import br.com.jhohannesfreitas.roomreservationapi.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final SalaRepository salaRepository;
    private final UsuarioRepository usuarioRepository;

    public ReservaService(ReservaRepository reservaRepository, SalaRepository salaRepository, UsuarioRepository usuarioRepository) {
        this.reservaRepository = reservaRepository;
        this.salaRepository = salaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public ReservaResponse cadastrar(ReservaRequest reservaRequest) {
        //Validar se o ID do Usuario informado no DTO ReservaRequest existe
        Usuario usuario = buscarUsuarioPorId(reservaRequest.usuarioId());

        //Validar se o ID da Sala informado no DTO ReservaRequest existe
        Sala sala = buscaSalaPorId(reservaRequest.salaId());

        // Validar se a sala está com Status livre. Não pode reservar sala inativa
        validarStatusSala(sala);

        //Validar se a data é anterior a data atual
        validarDataNaoPodeSerNoPassado(reservaRequest.data());

        //Validar Hora
        validarIntervaloHorario(reservaRequest.horaInicial(), reservaRequest.horaFinal());

        //Validar Capacidade
        validarCapacidade(reservaRequest.quantidadePessoas(), sala.getCapacidade());

        // Validar conflitos Reserva
        validarConflitoHorarioReserva(reservaRequest);

        // Converte o meu DTO ReservaRequest para entity Reserva
        Reserva reserva = ReservaMapper.toEntity(reservaRequest, usuario, sala);

        // Atualiza para uma nova variável de referência
        Reserva reservaAtualizada = reservaRepository.save(reserva);

        //Retorna o DTO ReservaResponse
        return ReservaMapper.toResponse(reservaAtualizada);
    }

    public List<ReservaResponse> listar() {
        return reservaRepository.findAll()
                .stream()
                .map(ReservaMapper::toResponse)
                .toList();
    }

    public ReservaResponse listarPorId(Long id) {
       return ReservaMapper.toResponse(buscarReservaPorId(id));
    }

    @Transactional
    public ReservaResponse atualizar(Long id, ReservaRequest reservaRequest) {
        // Verifica se a Reserva existe
        Reserva reserva = buscarReservaPorId(id);

        // Verificar se o usuário existe
        Usuario usuario = buscarUsuarioPorId(reservaRequest.usuarioId());

        // Verifica se a sala existe
        Sala sala = buscaSalaPorId(reservaRequest.salaId());

        // Valida Status da Sala = Livre
        validarStatusSala(sala);

        // Valida a Data
        validarDataNaoPodeSerNoPassado(reservaRequest.data());

        // Valida intervalo horário
        validarIntervaloHorario(reservaRequest.horaInicial(), reservaRequest.horaFinal());

        // Validar capacidade
        validarCapacidade(reservaRequest.quantidadePessoas(), sala.getCapacidade());

        // Validar conflitos de horário, menos para o id da reserva
        validarConflitoHorarioAtualizacao(reservaRequest.salaId(),reservaRequest.data(),reservaRequest.horaInicial(),reservaRequest.horaFinal(),StatusReserva.ATIVA,id);

        // Atualizar a Entity Reserva com dados DTO
        reserva.atualizar(reservaRequest, usuario,  sala);

        // Salvar a nova Entity atualizada
        Reserva reservaAtualizada = reservaRepository.save(reserva);

        // Retornar como resposta ReservaResponse DTO
        return ReservaMapper.toResponse(reservaAtualizada);

    }

    @Transactional
    public void deletar(Long id) {
        // Busca a reserva pelo Id
        Reserva reserva = buscarReservaPorId(id);
        // Deleta a reserva
        reservaRepository.delete(reserva);
    }

    private Reserva buscarReservaPorId(Long id) {
        return reservaRepository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Reserva com id " + id + " não encontrada.",
                        HttpStatus.NOT_FOUND));
    }

    private Usuario buscarUsuarioPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Usuário com id " + id + " não encontrado.",
                        HttpStatus.NOT_FOUND));
    }

    private Sala buscaSalaPorId(Long id) {
        return salaRepository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Sala com id " + id + " não encontrada.",
                        HttpStatus.NOT_FOUND));
    }

    private void validarStatusSala(Sala sala) {
        // Validar se a sala está com Status livre.
        // Não pode reservar sala INATIVA
        if (sala.getStatus() != StatusSala.LIVRE) {
            throw new RegraNegocioException("Sala inválida. Não é possível realizar reserva para uma sala que não esteja livre.",
                    HttpStatus.CONFLICT);
        }
    }

    private void validarDataNaoPodeSerNoPassado(LocalDate data) {
        if (data.isBefore(LocalDate.now())) {
            throw new RegraNegocioException(
                    "Não é permitido realizar reservas em datas passadas.",
                    HttpStatus.BAD_REQUEST);
        }
    }

    private void validarIntervaloHorario(LocalTime horaInicial, LocalTime horaFinal) {
        LocalTime abertura = LocalTime.of(8, 0);
        LocalTime fechamento = LocalTime.of(18, 0);
        if (horaInicial.isBefore(abertura)
                || horaFinal.isAfter(fechamento)
                || !horaInicial.isBefore(horaFinal)) {
            throw new RegraNegocioException(
                    "Horário inválido. Reservas devem ocorrer entre 08:00 e 18:00.",
                    HttpStatus.BAD_REQUEST);
        }
    }

    private void validarCapacidade(Integer quantidade, Integer capacidade) {
        //A capacidade positiva já é validada com a anotação
        // Validar se a capacidade da sala é menor a capacidade informada na reserva
        if (quantidade > capacidade) {
            throw new RegraNegocioException("Quantidade de pessoas é superior a capacidade máxima pertimita para a sala reservada.",
                    HttpStatus.CONFLICT);
        }
    }

    private void validarConflitoHorarioReserva(ReservaRequest reservaRequest) {
        // A mesma sala não pode ter reservas sobrepostas
        // Reservas com Status canceladas não entram em checagem de conflitos
        // Documente exemplos-limite (fim igual ao início é permitido).
        // Use intervalo semiaberto (incluindo o início mas sem incluir o fim) para comparação.
            //Reserva existente: 10:00 às 12:00
            // Nova reserva:      12:00 às 14:00
            // Não há conflito, pois o fim da primeira coincide com o início da segunda.

        List<Reserva> listaReservas = reservaRepository.findBySalaIdAndDataAndStatus(reservaRequest.salaId(), reservaRequest.data(),StatusReserva.ATIVA);
        for (Reserva reservaExistente : listaReservas) {
            //verificar se há conflitos de horário
            // 1- Se existir um horário reservado que inicia ANTES(BEFORE) do horário final passado na requisição
            // 2- Se existir um horário reservado que termine DEPOIS(AFTER) do horário inicial passado na requisição
            if (reservaExistente.getHoraInicial().isBefore(reservaRequest.horaFinal())
                    && reservaExistente.getHoraFinal().isAfter(reservaRequest.horaInicial())) {
                throw new RegraNegocioException("Horário inválido, já existe reserva para o horário informado",
                        HttpStatus.CONFLICT);
            }
        }
    }

    private void validarConflitoHorarioAtualizacao(Long salaId, LocalDate data, LocalTime horaInicial, LocalTime horaFinal, StatusReserva status, Long idReserva) {
        List<Reserva> listaReservas = reservaRepository.findBySalaIdAndDataAndStatusAndIdNot(salaId, data, status, idReserva);
        for (Reserva reservaExistente : listaReservas) {
            if (reservaExistente.getHoraInicial().isBefore(horaFinal)
                    && reservaExistente.getHoraFinal().isAfter(horaInicial)) {
                throw new RegraNegocioException("Horário inválido, já existe reserva para o horário informado",
                        HttpStatus.CONFLICT);
            }
        }

    }
}



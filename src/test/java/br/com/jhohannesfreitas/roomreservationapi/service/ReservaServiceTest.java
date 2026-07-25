package br.com.jhohannesfreitas.roomreservationapi.service;

import br.com.jhohannesfreitas.roomreservationapi.domain.entity.Reserva;
import br.com.jhohannesfreitas.roomreservationapi.domain.entity.Sala;
import br.com.jhohannesfreitas.roomreservationapi.domain.entity.Usuario;
import br.com.jhohannesfreitas.roomreservationapi.domain.enums.StatusReserva;
import br.com.jhohannesfreitas.roomreservationapi.domain.enums.StatusSala;
import br.com.jhohannesfreitas.roomreservationapi.dto.ReservaRequest;
import br.com.jhohannesfreitas.roomreservationapi.dto.ReservaResponse;
import br.com.jhohannesfreitas.roomreservationapi.exception.RegraNegocioException;
import br.com.jhohannesfreitas.roomreservationapi.repository.ReservaRepository;
import br.com.jhohannesfreitas.roomreservationapi.repository.SalaRepository;
import br.com.jhohannesfreitas.roomreservationapi.repository.UsuarioRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class ReservaServiceTest {

    @InjectMocks
    private ReservaService reservaService;

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private SalaRepository salaRepository;

    private ReservaRequest reservaRequest;

    @BeforeEach
    void setUp() {
        reservaRequest = new ReservaRequest(
                1L,
                1L,
                LocalDate.now(),
                LocalTime.of(13, 0, 0),
                LocalTime.of(14, 0, 0),
                20
        );

    }

    @Test
    void deveriaCadastrarReservaQuandoDadosForemValidos() {
        //ARRANGE
        Usuario usuario = criarUsuario();

        // Simula que existe um Usuário no banco com o ID informado no reservaRequest
        given(usuarioRepository.findById(reservaRequest.usuarioId())).willReturn(Optional.of(usuario));

        // Simula que existe uma Sala no banco com o ID informado no reservaRequest
        Sala sala = criarSala();
        given(salaRepository.findById(reservaRequest.salaId())).willReturn(Optional.of(sala));

        // Simula uma lista vazia de Reservas conflitantes para data e horário
        given(reservaRepository.findBySalaIdAndDataAndStatus(reservaRequest.salaId(), reservaRequest.data(), StatusReserva.ATIVA)).willReturn(List.of());

        // Simula a Reserva persistida retornada pelo banco após o save().
        Reserva reserva = criarReserva(usuario, sala);
        given(reservaRepository.save(any(Reserva.class))).willReturn(reserva);

        // ACT
        ReservaResponse reservaResponse = reservaService.cadastrar(reservaRequest);

        // ASSERT
        // 1. Verificação de comportamento (Behavior Verification): Você verifica que determinadas chamadas aconteceram.

        // "Então Repository, você DEVERIA verificar se foi chamado o 'findById()' do objeto usuario".
        then(usuarioRepository).should().findById(reservaRequest.usuarioId());

        // "Então Repository, você DEVERIA verificar se foi chamado o 'findById' 1x do objeto sala.
        then(salaRepository).should().findById(reservaRequest.salaId());

        // "Então Repository, você DEVERIA verificar se foi chamado o 'findBySalaIdAndDataAndStatus()' 1x do objeto reserva.
        then(reservaRepository).should().findBySalaIdAndDataAndStatus(reservaRequest.salaId(), reservaRequest.data(), StatusReserva.ATIVA);

        // "Então Repository, você DEVERIA verificar se foi chamado o 'save()' 1x do objeto reserva.
        then(reservaRepository).should().save(any(Reserva.class));

        // 2. Verificação de estado (State Verification): Você verifica o resultado obtido.

        // Verificando se os dados retornados são iguais aos da entidade persistida (Reserva).
        Assertions.assertAll(
                () -> Assertions.assertEquals(usuario.getId(), reservaResponse.usuarioId()),
                () -> Assertions.assertEquals(sala.getId(), reservaResponse.salaId()),
                () -> Assertions.assertEquals(reserva.getData(), reservaResponse.data()),
                () -> Assertions.assertEquals(reserva.getHoraInicial(), reservaResponse.horaInicio()),
                () -> Assertions.assertEquals(reserva.getHoraFinal(), reservaResponse.horaFim()),
                () -> Assertions.assertEquals(reserva.getQuantidadePessoas(), reservaResponse.quantidadePessoas()),
                () -> Assertions.assertEquals(reserva.getStatus(), reservaResponse.status())
        );
    }

    @Test
    void naoDeveriaCadastrarReservaQuandoUsuarioNaoExistir() {
        //Arrange

        // Simula que NÃO existe um Usuário no banco com o ID informado no reservaRequest
        given(usuarioRepository.findById(reservaRequest.usuarioId())).willReturn(Optional.empty());

        // ACT + ASSERT
        RegraNegocioException exception = Assertions.assertThrows(
                RegraNegocioException.class,
                () -> reservaService.cadastrar(reservaRequest));

        // 1. Verificação de comportamento (Behavior Verification): Você verifica que determinadas chamadas aconteceram.

        // "Então Repository, você DEVERIA verificar que a busca do usuario por ID usando o método 'findById()' aconteceu".
        then(usuarioRepository).should().findById(reservaRequest.usuarioId());

        // "Então Repository, você DEVERIA verificar que a busca de sala por ID usando o método 'findById' NUNCA aconteceu".
        then(salaRepository).should(never()).findById(reservaRequest.salaId());

        // "Então Repository, você DEVERIA verificar que a busca por reserva usando 'findBySalaIdAndDataAndStatus()' NUNCA aconteceu".
        then(reservaRepository).should(never()).findBySalaIdAndDataAndStatus(reservaRequest.salaId(), reservaRequest.data(), StatusReserva.ATIVA);

        // "Então Repository, você DEVERIA verificar o 'save()' para rserva NUNCA aconteceu.
        then(reservaRepository).should(never()).save(any(Reserva.class));

        // 2. Verificação de estado (State Verification): Você verifica o resultado obtido.

        // Verificando se a exceção lançada contém a mensagem e o status esperados.
        Assertions.assertAll(
                () -> Assertions.assertEquals(
                        "Usuário com id " + reservaRequest.usuarioId() + " não encontrado.",
                        exception.getMessage()),
                () -> Assertions.assertEquals(
                        HttpStatus.NOT_FOUND,
                        exception.getStatus())
        );
    }

    @Test
    void naoDeveriaCadastrarReservaQuandoSalaNaoExistir() {
        //Arrange
        Usuario usuario = criarUsuario();

        // Simula que existe um Usuário no banco com o ID informado no reservaRequest
        given(usuarioRepository.findById(reservaRequest.usuarioId())).willReturn(Optional.of(usuario));

        // Simula que NÃO existe uma Sala no banco com o ID informado no reservaRequest
        given(salaRepository.findById(reservaRequest.salaId())).willReturn(Optional.empty());

        // ACT + ASSERT
        RegraNegocioException exception = Assertions.assertThrows(
                RegraNegocioException.class,
                () -> reservaService.cadastrar(reservaRequest));

        // 1. Verificação de comportamento (Behavior Verification): Você verifica que determinadas chamadas aconteceram.

        // "Então Repository, você DEVERIA verificar que a busca do usuario por ID usando o método 'findById()' aconteceu".
        then(usuarioRepository).should().findById(reservaRequest.usuarioId());

        // "Então Repository, você DEVERIA verificar que a busca de sala por ID usando o método 'findById' aconteceu".
        then(salaRepository).should().findById(reservaRequest.salaId());

        // Como a sala não existe, o fluxo deve parar antes de validar conflitos e salvar.
        // "Então Repository, você DEVERIA verificar que a busca por reserva usando 'findBySalaIdAndDataAndStatus()' NUNCA aconteceu".
        then(reservaRepository).should(never()).findBySalaIdAndDataAndStatus(reservaRequest.salaId(), reservaRequest.data(), StatusReserva.ATIVA);

        // "Então Repository, você DEVERIA verificar o 'save()' para rserva NUNCA aconteceu.
        then(reservaRepository).should(never()).save(any(Reserva.class));

        // 2. Verificação de estado (State Verification): Você verifica o resultado obtido.

        // Verificando se a exceção lançada contém a mensagem e o status esperados.
        Assertions.assertAll(
                () -> Assertions.assertEquals(
                        "Sala com id " + reservaRequest.salaId() + " não encontrada.",
                        exception.getMessage()),
                () -> Assertions.assertEquals(
                        HttpStatus.NOT_FOUND,
                        exception.getStatus())
        );
    }

    @Test
    void naoDeveriaCadastrarReservaQuandoStatusSalaForOcupada() {
        //Arrange
        Usuario usuario = criarUsuario();

        // Simula que existe um Usuário no banco com o ID informado no reservaRequest
        given(usuarioRepository.findById(reservaRequest.usuarioId())).willReturn(Optional.of(usuario));

        // Simula que existe uma Sala no banco com o ID informado no reservaRequest
        Sala sala = criarSala();
        sala.setStatus(StatusSala.OCUPADA); // Altero o Status da sala para ocupada
        given(salaRepository.findById(reservaRequest.salaId())).willReturn(Optional.of(sala));

        // ACT + ASSERT
        RegraNegocioException exception = Assertions.assertThrows(
                RegraNegocioException.class,
                () -> reservaService.cadastrar(reservaRequest));

        // 1. Verificação de comportamento (Behavior Verification): Você verifica que determinadas chamadas aconteceram.

        // "Então Repository, você DEVERIA verificar que a busca do usuario por ID usando o método 'findById()' aconteceu".
        then(usuarioRepository).should().findById(reservaRequest.usuarioId());

        // "Então Repository, você DEVERIA verificar que a busca de sala por ID usando o método 'findById' aconteceu".
        then(salaRepository).should().findById(reservaRequest.salaId());

        // Como a sala não existe, o fluxo deve parar antes de validar conflitos e salvar.
        // "Então Repository, você DEVERIA verificar que a busca por reserva usando 'findBySalaIdAndDataAndStatus()' NUNCA aconteceu".
        then(reservaRepository).should(never()).findBySalaIdAndDataAndStatus(reservaRequest.salaId(), reservaRequest.data(), StatusReserva.ATIVA);

        // "Então Repository, você DEVERIA verificar o 'save()' para rserva NUNCA aconteceu.
        then(reservaRepository).should(never()).save(any(Reserva.class));

        // 2. Verificação de estado (State Verification): Você verifica o resultado obtido.

        // Verificando se a exceção lançada contém a mensagem e o status esperados.
        Assertions.assertAll(
                () -> Assertions.assertEquals(
                        "Sala inválida. Não é possível realizar reserva para uma sala que não esteja livre.",
                        exception.getMessage()),
                () -> Assertions.assertEquals(
                        HttpStatus.CONFLICT,
                        exception.getStatus())
        );
    }

    @Test
    void naoDeveriaCadastrarReservaQuandoDataForNoPassado() {
        //Arrange

        // Cria uma requisição de reserva com data no passado.
        ReservaRequest reservaRequestDataPassado = new ReservaRequest(
                1L,
                1L,
                LocalDate.now().minusDays(1),
                LocalTime.of(13, 0, 0),
                LocalTime.of(14, 0, 0),
                20
        );

        Usuario usuario = criarUsuario();

        // Simula que existe um Usuário no banco com o ID informado na requisição.
        given(usuarioRepository.findById(reservaRequestDataPassado.usuarioId())).willReturn(Optional.of(usuario));

        // Simula que existe uma Sala no banco com o ID informado na requisição.
        Sala sala = criarSala();
        given(salaRepository.findById(reservaRequestDataPassado.salaId())).willReturn(Optional.of(sala));

        // ACT + ASSERT
        RegraNegocioException exception = Assertions.assertThrows(
                RegraNegocioException.class,
                () -> reservaService.cadastrar(reservaRequestDataPassado));

        // 1. Verificação de comportamento (Behavior Verification): Você verifica que determinadas chamadas aconteceram.

        // "Então Repository, você DEVERIA verificar que a busca do usuario por ID usando o método 'findById()' aconteceu".
        then(usuarioRepository).should().findById(reservaRequestDataPassado.usuarioId());

        // "Então Repository, você DEVERIA verificar que a busca de sala por ID usando o método 'findById' aconteceu".
        then(salaRepository).should().findById(reservaRequestDataPassado.salaId());

        // Como a data da reserva é inválida, o fluxo deve parar antes de validar conflitos e salvar.

        // "Então Repository, você DEVERIA verificar que a busca por reserva usando 'findBySalaIdAndDataAndStatus()' NUNCA aconteceu".
        then(reservaRepository).should(never()).findBySalaIdAndDataAndStatus(reservaRequestDataPassado.salaId(), reservaRequestDataPassado.data(), StatusReserva.ATIVA);

        // "Então Repository, você DEVERIA verificar o 'save()' para reserva nunca aconteceu.
        then(reservaRepository).should(never()).save(any(Reserva.class));

        // 2. Verificação de estado (State Verification): Você verifica o resultado obtido.

        // Verificando se a exceção lançada contém a mensagem e o status esperados.
        Assertions.assertAll(
                () -> Assertions.assertEquals(
                        "Não é permitido realizar reservas em datas passadas.",
                        exception.getMessage()),
                () -> Assertions.assertEquals(
                        HttpStatus.BAD_REQUEST,
                        exception.getStatus())
        );
    }

    @Test
    void naoDeveriaCadastrarReservaQuandoHoraInicialForMaiorHoraFinal() {
        //Arrange

        // Cria uma requisição de reserva hora inicial maior que final.
        ReservaRequest reservaRequestHoraInicialMaior = new ReservaRequest(
                1L,
                1L,
                LocalDate.now(),
                LocalTime.of(14, 0, 0),
                LocalTime.of(13, 0, 0),
                20
        );

        Usuario usuario = criarUsuario();

        // Simula que existe um Usuário no banco com o ID informado na requisição.
        given(usuarioRepository.findById(reservaRequestHoraInicialMaior.usuarioId())).willReturn(Optional.of(usuario));

        // Simula que existe uma Sala no banco com o ID informado na requisição.
        Sala sala = criarSala();
        given(salaRepository.findById(reservaRequestHoraInicialMaior.salaId())).willReturn(Optional.of(sala));

        // ACT + ASSERT
        RegraNegocioException exception = Assertions.assertThrows(
                RegraNegocioException.class,
                () -> reservaService.cadastrar(reservaRequestHoraInicialMaior));

        // 1. Verificação de comportamento (Behavior Verification): Você verifica que determinadas chamadas aconteceram.

        // "Então Repository, você DEVERIA verificar que a busca do usuario por ID usando o método 'findById()' aconteceu".
        then(usuarioRepository).should().findById(reservaRequestHoraInicialMaior.usuarioId());

        // "Então Repository, você DEVERIA verificar que a busca de sala por ID usando o método 'findById' aconteceu".
        then(salaRepository).should().findById(reservaRequestHoraInicialMaior.salaId());

        // Como o horário da reserva é inválido, o fluxo deve parar antes de validar conflitos e salvar.

        // "Então Repository, você DEVERIA verificar que a busca por reserva usando 'findBySalaIdAndDataAndStatus()' NUNCA aconteceu".
        then(reservaRepository).should(never()).findBySalaIdAndDataAndStatus(reservaRequestHoraInicialMaior.salaId(), reservaRequestHoraInicialMaior.data(), StatusReserva.ATIVA);

        // "Então Repository, você DEVERIA verificar o 'save()' para reserva nunca aconteceu.
        then(reservaRepository).should(never()).save(any(Reserva.class));

        // 2. Verificação de estado (State Verification): Você verifica o resultado obtido.

        // Verificando se a exceção lançada contém a mensagem e o status esperados.
        Assertions.assertAll(
                () -> Assertions.assertEquals(
                        "A hora inicial deve ser anterior à hora final.",
                        exception.getMessage()),
                () -> Assertions.assertEquals(
                        HttpStatus.BAD_REQUEST,
                        exception.getStatus())
        );
    }

    @Test
    void naoDeveriaCadastrarReservaQuandoHoraInicialForIgualHoraFinal() {
        //Arrange

        // Cria uma requisição de reserva hora inicial igual a final.
        ReservaRequest reservaRequestHoraInicialIgual = new ReservaRequest(
                1L,
                1L,
                LocalDate.now(),
                LocalTime.of(13, 0, 0),
                LocalTime.of(13, 0, 0),
                20
        );

        Usuario usuario = criarUsuario();

        // Simula que existe um Usuário no banco com o ID informado na requisição.
        given(usuarioRepository.findById(reservaRequestHoraInicialIgual.usuarioId())).willReturn(Optional.of(usuario));

        // Simula que existe uma Sala no banco com o ID informado na requisição.
        Sala sala = criarSala();
        given(salaRepository.findById(reservaRequestHoraInicialIgual.salaId())).willReturn(Optional.of(sala));

        // ACT + ASSERT
        RegraNegocioException exception = Assertions.assertThrows(
                RegraNegocioException.class,
                () -> reservaService.cadastrar(reservaRequestHoraInicialIgual));

        // 1. Verificação de comportamento (Behavior Verification): Você verifica que determinadas chamadas aconteceram.

        // "Então Repository, você DEVERIA verificar que a busca do usuario por ID usando o método 'findById()' aconteceu".
        then(usuarioRepository).should().findById(reservaRequestHoraInicialIgual.usuarioId());

        // "Então Repository, você DEVERIA verificar que a busca de sala por ID usando o método 'findById' aconteceu".
        then(salaRepository).should().findById(reservaRequestHoraInicialIgual.salaId());

        // Como o horário da reserva é inválido, o fluxo deve parar antes de validar conflitos e salvar.

        // "Então Repository, você DEVERIA verificar que a busca por reserva usando 'findBySalaIdAndDataAndStatus()' NUNCA aconteceu".
        then(reservaRepository).should(never()).findBySalaIdAndDataAndStatus(reservaRequestHoraInicialIgual.salaId(), reservaRequestHoraInicialIgual.data(), StatusReserva.ATIVA);

        // "Então Repository, você DEVERIA verificar o 'save()' para reserva nunca aconteceu.
        then(reservaRepository).should(never()).save(any(Reserva.class));

        // 2. Verificação de estado (State Verification): Você verifica o resultado obtido.

        // Verificando se a exceção lançada contém a mensagem e o status esperados.
        Assertions.assertAll(
                () -> Assertions.assertEquals(
                        "A hora inicial deve ser anterior à hora final.",
                        exception.getMessage()),
                () -> Assertions.assertEquals(
                        HttpStatus.BAD_REQUEST,
                        exception.getStatus())
        );
    }

    @Test
    void naoDeveriaCadastrarReservaQuandoHorarioEstiverForaDoFuncionamento() {
        //Arrange

        // Cria uma requisição de reserva hora inicial maior que final.
        ReservaRequest reservaRequestForaHorarioFuncionamento = new ReservaRequest(
                1L,
                1L,
                LocalDate.now(),
                LocalTime.of(7, 0, 0),
                LocalTime.of(19, 0, 0),
                20
        );

        Usuario usuario = criarUsuario();

        // Simula que existe um Usuário no banco com o ID informado na requisição.
        given(usuarioRepository.findById(reservaRequestForaHorarioFuncionamento.usuarioId())).willReturn(Optional.of(usuario));

        // Simula que existe uma Sala no banco com o ID informado na requisição.
        Sala sala = criarSala();
        given(salaRepository.findById(reservaRequestForaHorarioFuncionamento.salaId())).willReturn(Optional.of(sala));

        // ACT + ASSERT
        RegraNegocioException exception = Assertions.assertThrows(
                RegraNegocioException.class,
                () -> reservaService.cadastrar(reservaRequestForaHorarioFuncionamento));

        // 1. Verificação de comportamento (Behavior Verification): Você verifica que determinadas chamadas aconteceram.

        // "Então Repository, você DEVERIA verificar que a busca do usuario por ID usando o método 'findById()' aconteceu".
        then(usuarioRepository).should().findById(reservaRequestForaHorarioFuncionamento.usuarioId());

        // "Então Repository, você DEVERIA verificar que a busca de sala por ID usando o método 'findById' aconteceu".
        then(salaRepository).should().findById(reservaRequestForaHorarioFuncionamento.salaId());

        // Como o horário da reserva é inválido, o fluxo deve parar antes de validar conflitos e salvar.

        // "Então Repository, você DEVERIA verificar que a busca por reserva usando 'findBySalaIdAndDataAndStatus()' NUNCA aconteceu".
        then(reservaRepository).should(never()).findBySalaIdAndDataAndStatus(reservaRequestForaHorarioFuncionamento.salaId(), reservaRequestForaHorarioFuncionamento.data(), StatusReserva.ATIVA);

        // "Então Repository, você DEVERIA verificar o 'save()' para reserva nunca aconteceu.
        then(reservaRepository).should(never()).save(any(Reserva.class));

        // 2. Verificação de estado (State Verification): Você verifica o resultado obtido.

        // Verificando se a exceção lançada contém a mensagem e o status esperados.
        Assertions.assertAll(
                () -> Assertions.assertEquals(
                        "Reservas devem ocorrer entre 08:00 e 18:00.",
                        exception.getMessage()),
                () -> Assertions.assertEquals(
                        HttpStatus.BAD_REQUEST,
                        exception.getStatus())
        );
    }

    @Test
    void naoDeveriaCadastrarReservaQuandoExistirConflitoDeHorario() {
        //Arrange

        Usuario usuario = criarUsuario();
        // Simula que existe um Usuário no banco com o ID informado na requisição.
        given(usuarioRepository.findById(reservaRequest.usuarioId())).willReturn(Optional.of(usuario));

        // Simula que existe uma Sala no banco com o ID informado na requisição.
        Sala sala = criarSala();
        given(salaRepository.findById(reservaRequest.salaId())).willReturn(Optional.of(sala));

        // Cria uma reserva já existente para simular conflito de horário
        Reserva reservaExistente = criarReservaExistente(usuario, sala);

        // Simula que já existe uma reserva ativa para a mesma sala e data
        given(reservaRepository.findBySalaIdAndDataAndStatus(reservaRequest.salaId(), reservaRequest.data(), StatusReserva.ATIVA)).willReturn(List.of(reservaExistente));

        // ACT + ASSERT
        RegraNegocioException exception = Assertions.assertThrows(
                RegraNegocioException.class,
                () -> reservaService.cadastrar(reservaRequest));

        // 1. Verificação de comportamento (Behavior Verification): Você verifica que determinadas chamadas aconteceram.

        // "Então Repository, você DEVERIA verificar que a busca do usuario por ID usando o método 'findById()' aconteceu".
        then(usuarioRepository).should().findById(reservaRequest.usuarioId());

        // "Então Repository, você DEVERIA verificar que a busca de sala por ID usando o método 'findById' aconteceu".
        then(salaRepository).should().findById(reservaRequest.salaId());

        // Como já existe uma reserva para esse horário, o fluxo deve parar antes de salvar.

        // "Então Repository, você DEVERIA verificar que a busca por reserva usando 'findBySalaIdAndDataAndStatus()' aconteceu".
        then(reservaRepository).should().findBySalaIdAndDataAndStatus(reservaRequest.salaId(), reservaRequest.data(), StatusReserva.ATIVA);

        // "Então Repository, você DEVERIA verificar o 'save()' para reserva NUNCA aconteceu.
        then(reservaRepository).should(never()).save(any(Reserva.class));

        // 2. Verificação de estado (State Verification): Você verifica o resultado obtido.

        // Verificando se a exceção lançada contém a mensagem e o status esperados.
        Assertions.assertAll(
                () -> Assertions.assertEquals(
                        "Conflito de horário. Já existe uma reserva para o período informado.",
                        exception.getMessage()),
                () -> Assertions.assertEquals(
                        HttpStatus.CONFLICT,
                        exception.getStatus())
        );
    }

    @Test
    void naoDeveriaCadastrarReservaQuandoQuantidadePessoasForMaiorCapacidadeSala() {
        //Arrange

        // Cria uma requisição de reserva com quantidade de pessoas superior a capacidade da sala.
        ReservaRequest reservaRequestQtdPessoasMaiorCapacidade = new ReservaRequest(
                1L,
                1L,
                LocalDate.now(),
                LocalTime.of(14, 0, 0),
                LocalTime.of(15, 0, 0),
                30
        );

        Usuario usuario = criarUsuario();

        // Simula que existe um Usuário no banco com o ID informado na requisição.
        given(usuarioRepository.findById(reservaRequestQtdPessoasMaiorCapacidade.usuarioId())).willReturn(Optional.of(usuario));

        // Simula que existe uma Sala no banco com o ID informado na requisição.
        Sala sala = criarSala();
        given(salaRepository.findById(reservaRequestQtdPessoasMaiorCapacidade.salaId())).willReturn(Optional.of(sala));

        // Simula que não existe nenhuma reserva conflitante para a sala na data informada.
        given(reservaRepository.findBySalaIdAndDataAndStatus(reservaRequestQtdPessoasMaiorCapacidade.salaId(), reservaRequestQtdPessoasMaiorCapacidade.data(), StatusReserva.ATIVA)).willReturn(List.of());

        // ACT + ASSERT
        RegraNegocioException exception = Assertions.assertThrows(
                RegraNegocioException.class,
                () -> reservaService.cadastrar(reservaRequestQtdPessoasMaiorCapacidade));

        // 1. Verificação de comportamento (Behavior Verification): Você verifica que determinadas chamadas aconteceram.

        // "Então Repository, você DEVERIA verificar que a busca do usuario por ID usando o método 'findById()' aconteceu".
        then(usuarioRepository).should().findById(reservaRequestQtdPessoasMaiorCapacidade.usuarioId());

        // "Então Repository, você DEVERIA verificar que a busca de sala por ID usando o método 'findById' aconteceu".
        then(salaRepository).should().findById(reservaRequestQtdPessoasMaiorCapacidade.salaId());

        // "Então Repository, você DEVERIA verificar que a busca por reserva usando 'findBySalaIdAndDataAndStatus()' aconteceu".
        then(reservaRepository).should().findBySalaIdAndDataAndStatus(reservaRequestQtdPessoasMaiorCapacidade.salaId(), reservaRequestQtdPessoasMaiorCapacidade.data(), StatusReserva.ATIVA);

        // Como a quantidade de pessoas excede a capacidade da sala, o fluxo deve parar antes de salvar a reserva.
        // "Então Repository, você DEVERIA verificar que o método 'save()' para reserva nunca aconteceu.
        then(reservaRepository).should(never()).save(any(Reserva.class));

        // 2. Verificação de estado (State Verification): Você verifica o resultado obtido.

        // Verificando se a exceção lançada contém a mensagem e o status esperados.
        Assertions.assertAll(
                () -> Assertions.assertEquals(
                        "A quantidade de pessoas excede a capacidade máxima da sala.",
                        exception.getMessage()),
                () -> Assertions.assertEquals(
                        HttpStatus.CONFLICT,
                        exception.getStatus())
        );
    }

    @Test
    void deveriaListarReservasCadastradas() {
        // ARRANGE

        // Cria um usuário para a reserva.
        Usuario usuario = criarUsuario();

        // Cria uma sala para a reserva.
        Sala sala = criarSala();

        // Cria reservas para listar
        Reserva reserva = criarReserva(usuario, sala);

        Reserva reserva2 = new Reserva(
                LocalDate.now(),
                LocalTime.of(8, 0, 0),
                LocalTime.of(9, 0, 0),
                20
        );
        reserva2.setUsuario(usuario);
        reserva2.setSala(sala);

        given(reservaRepository.findAll()).willReturn(List.of(reserva, reserva2));

        // ACT
        List<ReservaResponse> listaReservaResponse = reservaService.listar();
        ReservaResponse primeiraReserva = listaReservaResponse.getFirst();
        ReservaResponse segundaReserva = listaReservaResponse.get(1);

        // ASSERT

        // 1. Verificação de comportamento (Behavior Verification): Você verifica que determinadas chamadas aconteceram.

        // Então Repository, você DEVERIA verificar que o 'findAll()' aconteceu
        then(reservaRepository).should().findAll();

        // 2. Verificação de estado (State Verification): Você verifica o resultado obtido.

        Assertions.assertAll(
                () -> Assertions.assertEquals(2, listaReservaResponse.size()),

                () -> Assertions.assertEquals(usuario.getId(), primeiraReserva.usuarioId()),
                () -> Assertions.assertEquals(sala.getId(), primeiraReserva.salaId()),
                () -> Assertions.assertEquals(reserva.getData(), primeiraReserva.data()),
                () -> Assertions.assertEquals(reserva.getHoraInicial(), primeiraReserva.horaInicio()),
                () -> Assertions.assertEquals(reserva.getHoraFinal(), primeiraReserva.horaFim()),
                () -> Assertions.assertEquals(reserva.getQuantidadePessoas(), primeiraReserva.quantidadePessoas()),
                () -> Assertions.assertEquals(reserva.getStatus(), primeiraReserva.status()),

                () -> Assertions.assertEquals(usuario.getId(), segundaReserva.usuarioId()),
                () -> Assertions.assertEquals(sala.getId(), segundaReserva.salaId()),
                () -> Assertions.assertEquals(reserva2.getData(), segundaReserva.data()),
                () -> Assertions.assertEquals(reserva2.getHoraInicial(), segundaReserva.horaInicio()),
                () -> Assertions.assertEquals(reserva2.getHoraFinal(), segundaReserva.horaFim()),
                () -> Assertions.assertEquals(reserva2.getQuantidadePessoas(), segundaReserva.quantidadePessoas()),
                () -> Assertions.assertEquals(reserva2.getStatus(), segundaReserva.status())
        );
    }

    @Test
    void deveriaRetornarListaVaziaQuandoNaoExistiremReservasCadastradas() {
        // ARRANGE

        // Retorna uma lista vazia
        given(reservaRepository.findAll()).willReturn(List.of());

        // ACT
        List<ReservaResponse> listaReservaResponse = reservaService.listar();

        // ASSERT

        // 1. Verificação de comportamento (Behavior Verification): Você verifica que determinadas chamadas aconteceram.

        // Então Repository, você DEVERIA verificar que o 'findAll()' aconteceu
        then(reservaRepository).should().findAll();

        // 2. Verificação de estado (State Verification): Você verifica o resultado obtido.

        Assertions.assertTrue(listaReservaResponse.isEmpty());
    }

    @Test
    void deveriaBuscarPorIdReservaQuandoEstiverCadastrada() {
        // ARRANGE

        // Cria um usuário para a reserva.
        Usuario usuario = criarUsuario();

        // Cria uma sala para a reserva.
        Sala sala = criarSala();

        // Cria reservas para listar
        Reserva reserva = criarReserva(usuario, sala);
        Long id = 1L;
        given(reservaRepository.findById(id)).willReturn(Optional.of(reserva));

        // ACT
        ReservaResponse reservaResponse = reservaService.listarPorId(id);

        // ASSERT

        // 1. Verificação de comportamento (Behavior Verification): Você verifica que determinadas chamadas aconteceram.

        // Então Repository, você DEVERIA verificar que o 'findById()' aconteceu
        then(reservaRepository).should().findById(id);

        // 2. Verificação de estado (State Verification): Você verifica o resultado obtido.

        Assertions.assertAll(

                () -> Assertions.assertEquals(usuario.getId(), reservaResponse.usuarioId()),
                () -> Assertions.assertEquals(sala.getId(), reservaResponse.salaId()),
                () -> Assertions.assertEquals(reserva.getData(), reservaResponse.data()),
                () -> Assertions.assertEquals(reserva.getHoraInicial(), reservaResponse.horaInicio()),
                () -> Assertions.assertEquals(reserva.getHoraFinal(), reservaResponse.horaFim()),
                () -> Assertions.assertEquals(reserva.getQuantidadePessoas(), reservaResponse.quantidadePessoas()),
                () -> Assertions.assertEquals(reserva.getStatus(), reservaResponse.status())
        );
    }

    @Test
    void deveriaLancarExcecaoQuandoBuscarReservaPorIdInexistente() {
        // ARRANGE

        // Simula que a busca por reserva pelo ID retornou vazio
        Long id = 1L;
        given(reservaRepository.findById(id)).willReturn(Optional.empty());

        // ACT + ASSERT
        RegraNegocioException exception = Assertions.assertThrows(
                RegraNegocioException.class,
                () -> reservaService.listarPorId(id));

        // 1. Verificação de comportamento (Behavior Verification): Você verifica que determinadas chamadas aconteceram.

        // Então Repository, você DEVERIA verificar que o 'findById()' aconteceu
        then(reservaRepository).should().findById(id);

        // 2. Verificação de estado (State Verification): Você verifica o resultado obtido.

        Assertions.assertAll(

                () -> Assertions.assertEquals(
                        "Reserva com id " + id + " não encontrada.",
                        exception.getMessage()),
                () -> Assertions.assertEquals(
                        HttpStatus.NOT_FOUND, exception.getStatus())

        );
    }

    @Test
    void deveriaAtualizarReservaQuandoDadosForemValidos() {

        //ARRANGE

        // Cria um usuário para usar na Reserva
        Usuario usuario = criarUsuario();

        // Simula que existe um Usuário no banco com o ID informado no reservaRequest
        given(usuarioRepository.findById(reservaRequest.usuarioId())).willReturn(Optional.of(usuario));

        // Cria uma sala para usar na Reserva
        Sala sala = criarSala();

        // Simula que existe uma Sala no banco com o ID informado no reservaRequest
        given(salaRepository.findById(reservaRequest.salaId())).willReturn(Optional.of(sala));

        // Cria uma reserva
        Reserva reserva = criarReserva(usuario, sala);
        Long id = 1L;

        // Simula que existe uma Reserva cadastrada com o ID informado
        given(reservaRepository.findById(id)).willReturn(Optional.of(reserva));

        // Simula uma lista vazia de Reservas conflitantes para data e horário
        given(reservaRepository.findBySalaIdAndDataAndStatusAndIdNot(reservaRequest.salaId(), reservaRequest.data(), StatusReserva.ATIVA, id)).willReturn(List.of());

        // Simula a Reserva persistida retornada pelo banco após o save().
        given(reservaRepository.save(any(Reserva.class))).willReturn(reserva);

        // ACT
        ReservaResponse reservaResponse = reservaService.atualizar(id, reservaRequest);

        // ASSERT
        // 1. Verificação de comportamento (Behavior Verification): Você verifica que determinadas chamadas aconteceram.

        // "Então Repository, você DEVERIA verificar se foi chamado o 'findById()' do objeto usuario".
        then(usuarioRepository).should().findById(reservaRequest.usuarioId());

        // "Então Repository, você DEVERIA verificar se foi chamado o 'findById' do objeto sala.
        then(salaRepository).should().findById(reservaRequest.salaId());

        // "Então Repository, você DEVERIA verificar se foi chamado o 'findById' do objeto reserva.
        then(reservaRepository).should().findById(id);

        // "Então Repository, você DEVERIA verificar se foi chamado o 'findBySalaIdAndDataAndStatusAndIdNot()' 1x do objeto reserva.
        then(reservaRepository).should().findBySalaIdAndDataAndStatusAndIdNot(reservaRequest.salaId(), reservaRequest.data(), StatusReserva.ATIVA, id);

        // "Então Repository, você DEVERIA verificar se foi chamado o 'save()' do objeto reserva.
        then(reservaRepository).should().save(reserva);

        // 2. Verificação de estado (State Verification): Você verifica o resultado obtido.

        // Verificando se os dados retornados são iguais aos da entidade persistida (Reserva).
        Assertions.assertAll(
                () -> Assertions.assertEquals(usuario.getId(), reservaResponse.usuarioId()),
                () -> Assertions.assertEquals(sala.getId(), reservaResponse.salaId()),
                () -> Assertions.assertEquals(reserva.getData(), reservaResponse.data()),
                () -> Assertions.assertEquals(reserva.getHoraInicial(), reservaResponse.horaInicio()),
                () -> Assertions.assertEquals(reserva.getHoraFinal(), reservaResponse.horaFim()),
                () -> Assertions.assertEquals(reserva.getQuantidadePessoas(), reservaResponse.quantidadePessoas()),
                () -> Assertions.assertEquals(reserva.getStatus(), reservaResponse.status())
        );
    }

    @Test
    void naoDeveriaAtualizarReservaQuandoIdUsuarioNaoEstiverCadastrado() {

        //ARRANGE

        // ID da reserva para chamada do método
        Long idReserva = 1L;

        // Simula que NÃO existe um Usuário no banco com o ID informado, retornando um Optional vazio.
        given(usuarioRepository.findById(reservaRequest.usuarioId())).willReturn(Optional.empty());

        // ACT + ASSERT

        RegraNegocioException exception = Assertions.assertThrows(
                RegraNegocioException.class,
                () -> reservaService.atualizar(idReserva, reservaRequest)
        );

        // ASSERT
        // 1. Verificação de comportamento (Behavior Verification): Você verifica que determinadas chamadas aconteceram.

        // "Então Repository, você DEVERIA verificar se foi chamado o 'findById()' do objeto usuario".
        then(usuarioRepository).should().findById(reservaRequest.usuarioId());

        // "Então Repository, você DEVERIA verificar se NUNCA foi chamado o 'findById' do objeto sala.
        then(salaRepository).should(never()).findById(reservaRequest.salaId());

        // "Então Repository, você DEVERIA verificar se NUNCA foi chamado o 'findById' do objeto reserva.
        then(reservaRepository).should(never()).findById(anyLong());

        // "Então Repository, você DEVERIA verificar se NUNCA  foi chamado o 'findBySalaIdAndDataAndStatusAndIdNot()' do objeto reserva.
        then(reservaRepository).should(never()).findBySalaIdAndDataAndStatusAndIdNot(reservaRequest.salaId(), reservaRequest.data(), StatusReserva.ATIVA, idReserva);

        // "Então Repository, você DEVERIA verificar se NUNCA foi chamado o 'save()' do objeto reserva.
        then(reservaRepository).should(never()).save(any(Reserva.class));

        // 2. Verificação de estado (State Verification): Você verifica o resultado obtido.

        // Verificando se a exceção lançada contém a mensagem e o status esperados
        Assertions.assertAll(
                () -> Assertions.assertEquals(
                        "Usuário com id " + reservaRequest.usuarioId() + " não encontrado.",
                        exception.getMessage()),
                () -> Assertions.assertEquals(
                        HttpStatus.NOT_FOUND,
                        exception.getStatus())
        );
    }

    @Test
    void naoDeveriaAtualizarReservaQuandoIdSalaNaoEstiverCadastrado() {

        //ARRANGE

        // ID da reserva para chamada do método
        Long idReserva = 1L;

        // Cria um usuário
        Usuario usuario = criarUsuario();

        // Simula que existe um Usuário no banco com o ID informado no reservaRequest.
        given(usuarioRepository.findById(reservaRequest.usuarioId())).willReturn(Optional.of(usuario));

        // Simula que NÃO existe uma Sala no banco com o ID informado no reservaRequest, retornando um Optional vazio
        given(salaRepository.findById(reservaRequest.salaId())).willReturn(Optional.empty());

        // ACT + ASSERT

        RegraNegocioException exception = Assertions.assertThrows(
                RegraNegocioException.class,
                () -> reservaService.atualizar(idReserva, reservaRequest)
        );

        // ASSERT
        // 1. Verificação de comportamento (Behavior Verification): Você verifica que determinadas chamadas aconteceram.

        // "Então Repository, você DEVERIA verificar se foi chamado o 'findById()' do objeto usuario".
        then(usuarioRepository).should().findById(reservaRequest.usuarioId());

        // "Então Repository, você DEVERIA verificar se foi chamado o 'findById' do objeto sala.
        then(salaRepository).should().findById(reservaRequest.salaId());

        // "Então Repository, você DEVERIA verificar se NUNCA foi chamado o 'findById' do objeto reserva.
        then(reservaRepository).should(never()).findById(idReserva);

        // "Então Repository, você DEVERIA verificar se NUNCA  foi chamado o 'findBySalaIdAndDataAndStatusAndIdNot()' do objeto reserva.
        then(reservaRepository).should(never()).findBySalaIdAndDataAndStatusAndIdNot(reservaRequest.salaId(), reservaRequest.data(), StatusReserva.ATIVA, idReserva);

        // "Então Repository, você DEVERIA verificar se NUNCA foi chamado o 'save()' do objeto reserva.
        then(reservaRepository).should(never()).save(any(Reserva.class));

        // 2. Verificação de estado (State Verification): Você verifica o resultado obtido.

        // Verificando se a exceção lançada contém a mensagem e o status esperados
        Assertions.assertAll(
                () -> Assertions.assertEquals(
                        "Sala com id " + reservaRequest.salaId() + " não encontrada.",
                        exception.getMessage()),
                () -> Assertions.assertEquals(
                        HttpStatus.NOT_FOUND,
                        exception.getStatus())
        );
    }

    @Test
    void naoDeveriaAtualizarReservaQuandoIdReservaNaoEstiverCadastrado() {

        //ARRANGE

        // Cria um usuário
        Usuario usuario = criarUsuario();

        // Simula que existe um Usuário no banco com o ID informado no reservaRequest.
        given(usuarioRepository.findById(reservaRequest.usuarioId())).willReturn(Optional.of(usuario));

        // Cria uma sala
        Sala sala = criarSala();

        // Simula que existe uma Sala no banco com o ID informado no reservaRequest.
        given(salaRepository.findById(reservaRequest.salaId())).willReturn(Optional.of(sala));

        // Cria uma reserva
        Long idReserva = 1L;

        // Simula que NÃO existe uma Reserva cadastrada com o ID informado
        given(reservaRepository.findById(idReserva)).willReturn(Optional.empty());

        // ACT + ASSERT

        RegraNegocioException exception = Assertions.assertThrows(
                RegraNegocioException.class,
                () -> reservaService.atualizar(idReserva, reservaRequest)
        );

        // ASSERT
        // 1. Verificação de comportamento (Behavior Verification): Você verifica que determinadas chamadas aconteceram.

        // "Então Repository, você DEVERIA verificar se foi chamado o 'findById()' do objeto usuario".
        then(usuarioRepository).should().findById(reservaRequest.usuarioId());

        // "Então Repository, você DEVERIA verificar se foi chamado o 'findById' do objeto sala.
        then(salaRepository).should().findById(reservaRequest.salaId());

        // "Então Repository, você DEVERIA verificar se foi chamado o 'findById' do objeto reserva.
        then(reservaRepository).should().findById(idReserva);

        // "Então Repository, você DEVERIA verificar se NUNCA  foi chamado o 'findBySalaIdAndDataAndStatusAndIdNot()' do objeto reserva.
        then(reservaRepository).should(never()).findBySalaIdAndDataAndStatusAndIdNot(reservaRequest.salaId(), reservaRequest.data(), StatusReserva.ATIVA, idReserva);

        // "Então Repository, você DEVERIA verificar se NUNCA foi chamado o 'save()' do objeto reserva.
        then(reservaRepository).should(never()).save(any(Reserva.class));

        // 2. Verificação de estado (State Verification): Você verifica o resultado obtido.

        // Verificando se a exceção lançada contém a mensagem e o status esperados
        Assertions.assertAll(
                () -> Assertions.assertEquals(
                        "Reserva com id " + idReserva + " não encontrada.",
                        exception.getMessage()),
                () -> Assertions.assertEquals(
                        HttpStatus.NOT_FOUND,
                        exception.getStatus())
        );
    }

    @Test
    void naoDeveriaAtualizarReservaQuandoStatusSalaForOcupada() {

        //ARRANGE

        // Cria um usuário
        Usuario usuario = criarUsuario();

        // Simula que existe um Usuário no banco com o ID informado no reservaRequest.
        given(usuarioRepository.findById(reservaRequest.usuarioId())).willReturn(Optional.of(usuario));

        // Cria uma sala
        Sala sala = criarSala();
        sala.setStatus(StatusSala.OCUPADA); // Altero o Status da sala para ocupada

        // Simula que existe uma Sala no banco com o ID informado no reservaRequest.
        given(salaRepository.findById(reservaRequest.salaId())).willReturn(Optional.of(sala));

        // Cria uma reserva existente para simular a atualização.
        Reserva reserva = criarReserva(usuario, sala);
        Long idReserva = 1L;

        // Simula que existe uma Reserva cadastrada com o ID informado
        given(reservaRepository.findById(idReserva)).willReturn(Optional.of(reserva));

        // ACT + ASSERT

        RegraNegocioException exception = Assertions.assertThrows(
                RegraNegocioException.class,
                () -> reservaService.atualizar(idReserva, reservaRequest)
        );

        // ASSERT
        // 1. Verificação de comportamento (Behavior Verification): Você verifica que determinadas chamadas aconteceram.

        // "Então Repository, você DEVERIA verificar se foi chamado o 'findById()' do objeto usuario".
        then(usuarioRepository).should().findById(reservaRequest.usuarioId());

        // "Então Repository, você DEVERIA verificar se foi chamado o 'findById' do objeto sala.
        then(salaRepository).should().findById(reservaRequest.salaId());

        // "Então Repository, você DEVERIA verificar se foi chamado o 'findById' do objeto reserva.
        then(reservaRepository).should().findById(idReserva);

        // "Então Repository, você DEVERIA verificar se NUNCA  foi chamado o 'findBySalaIdAndDataAndStatusAndIdNot()' do objeto reserva.
        then(reservaRepository).should(never()).findBySalaIdAndDataAndStatusAndIdNot(reservaRequest.salaId(), reservaRequest.data(), StatusReserva.ATIVA, idReserva);

        // "Então Repository, você DEVERIA verificar se NUNCA foi chamado o 'save()' do objeto reserva.
        then(reservaRepository).should(never()).save(any(Reserva.class));

        // 2. Verificação de estado (State Verification): Você verifica o resultado obtido.

        // Verificando se a exceção lançada contém a mensagem e o status esperados
        Assertions.assertAll(
                () -> Assertions.assertEquals(
                        "Sala inválida. Não é possível realizar reserva para uma sala que não esteja livre.",
                        exception.getMessage()),
                () -> Assertions.assertEquals(
                        HttpStatus.CONFLICT,
                        exception.getStatus())
        );
    }

    @Test
    void naoDeveriaAtualizarReservaQuandoDataForNoPassado() {
        //Arrange

        // Cria uma requisição de reserva com data no passado.
        ReservaRequest reservaRequestDataPassado = new ReservaRequest(
                1L,
                1L,
                LocalDate.now().minusDays(1),
                LocalTime.of(13, 0, 0),
                LocalTime.of(14, 0, 0),
                20
        );

        Usuario usuario = criarUsuario();

        // Simula que existe um Usuário no banco com o ID informado na requisição.
        given(usuarioRepository.findById(reservaRequestDataPassado.usuarioId())).willReturn(Optional.of(usuario));

        // Simula que existe uma Sala no banco com o ID informado na requisição.
        Sala sala = criarSala();
        given(salaRepository.findById(reservaRequestDataPassado.salaId())).willReturn(Optional.of(sala));

        // Cria uma reserva existente para simular a atualização.
        Reserva reserva = criarReserva(usuario, sala);
        Long idReserva = 1L;

        // Simula que existe uma Reserva cadastrada com o ID informado
        given(reservaRepository.findById(idReserva)).willReturn(Optional.of(reserva));

        // ACT + ASSERT
        RegraNegocioException exception = Assertions.assertThrows(
                RegraNegocioException.class,
                () -> reservaService.atualizar(idReserva, reservaRequestDataPassado));

        // 1. Verificação de comportamento (Behavior Verification): Você verifica que determinadas chamadas aconteceram.

        // "Então Repository, você DEVERIA verificar que a busca do usuario por ID usando o método 'findById()' aconteceu".
        then(usuarioRepository).should().findById(reservaRequestDataPassado.usuarioId());

        // "Então Repository, você DEVERIA verificar que a busca de sala por ID usando o método 'findById' aconteceu".
        then(salaRepository).should().findById(reservaRequestDataPassado.salaId());

        // "Então Repository, você DEVERIA verificar se foi chamado o 'findById' do objeto reserva.
        then(reservaRepository).should().findById(idReserva);

        // Como a data da reserva é inválida, o fluxo deve parar antes de validar conflitos e salvar.

        // "Então Repository, você DEVERIA verificar se NUNCA  foi chamado o 'findBySalaIdAndDataAndStatusAndIdNot()' do objeto reserva.
        then(reservaRepository).should(never()).findBySalaIdAndDataAndStatusAndIdNot(reservaRequestDataPassado.salaId(), reservaRequestDataPassado.data(), StatusReserva.ATIVA, idReserva);

        // "Então Repository, você DEVERIA verificar o 'save()' para reserva NUNCA aconteceu.
        then(reservaRepository).should(never()).save(any(Reserva.class));

        // 2. Verificação de estado (State Verification): Você verifica o resultado obtido.

        // Verificando se a exceção lançada contém a mensagem e o status esperados.
        Assertions.assertAll(
                () -> Assertions.assertEquals(
                        "Não é permitido realizar reservas em datas passadas.",
                        exception.getMessage()),
                () -> Assertions.assertEquals(
                        HttpStatus.BAD_REQUEST,
                        exception.getStatus())
        );
    }

    @Test
    void naoDeveriaAtualizarReservaQuandoHoraInicialForMaiorHoraFinal() {
        //Arrange

        // Cria uma requisição de reserva hora inicial maior que final.
        ReservaRequest reservaRequestHoraInicialMaior = new ReservaRequest(
                1L,
                1L,
                LocalDate.now(),
                LocalTime.of(14, 0, 0),
                LocalTime.of(13, 0, 0),
                20
        );

        Usuario usuario = criarUsuario();

        // Simula que existe um Usuário no banco com o ID informado na requisição.
        given(usuarioRepository.findById(reservaRequestHoraInicialMaior.usuarioId())).willReturn(Optional.of(usuario));

        // Simula que existe uma Sala no banco com o ID informado na requisição.
        Sala sala = criarSala();
        given(salaRepository.findById(reservaRequestHoraInicialMaior.salaId())).willReturn(Optional.of(sala));

        // Cria uma reserva existente para simular a atualização.
        Reserva reserva = criarReserva(usuario, sala);
        Long idReserva = 1L;

        // Simula que existe uma Reserva cadastrada com o ID informado
        given(reservaRepository.findById(idReserva)).willReturn(Optional.of(reserva));

        // ACT + ASSERT
        RegraNegocioException exception = Assertions.assertThrows(
                RegraNegocioException.class,
                () -> reservaService.atualizar(idReserva, reservaRequestHoraInicialMaior));

        // 1. Verificação de comportamento (Behavior Verification): Você verifica que determinadas chamadas aconteceram.

        // "Então Repository, você DEVERIA verificar que a busca do usuario por ID usando o método 'findById()' aconteceu".
        then(usuarioRepository).should().findById(reservaRequestHoraInicialMaior.usuarioId());

        // "Então Repository, você DEVERIA verificar que a busca de sala por ID usando o método 'findById' aconteceu".
        then(salaRepository).should().findById(reservaRequestHoraInicialMaior.salaId());

        // "Então Repository, você DEVERIA verificar se foi chamado o 'findById' do objeto reserva.
        then(reservaRepository).should().findById(idReserva);

        // Como o horário da reserva é inválido, o fluxo deve parar antes de validar conflitos e salvar.

        // "Então Repository, você DEVERIA verificar se NUNCA  foi chamado o 'findBySalaIdAndDataAndStatusAndIdNot()' do objeto reserva.
        then(reservaRepository).should(never()).findBySalaIdAndDataAndStatusAndIdNot(reservaRequestHoraInicialMaior.salaId(), reservaRequestHoraInicialMaior.data(), StatusReserva.ATIVA, idReserva);

        // "Então Repository, você DEVERIA verificar o 'save()' para reserva NUNCA aconteceu.
        then(reservaRepository).should(never()).save(any(Reserva.class));

        // 2. Verificação de estado (State Verification): Você verifica o resultado obtido.

        // Verificando se a exceção lançada contém a mensagem e o status esperados.
        Assertions.assertAll(
                () -> Assertions.assertEquals(
                        "A hora inicial deve ser anterior à hora final.",
                        exception.getMessage()),
                () -> Assertions.assertEquals(
                        HttpStatus.BAD_REQUEST,
                        exception.getStatus())
        );
    }

    @Test
    void naoDeveriaAtualizarReservaQuandoHoraInicialForIgualHoraFinal() {
        //Arrange

        // Cria uma requisição de reserva com hora inicial igual à hora final.
        ReservaRequest reservaRequestHoraInicialIgual = new ReservaRequest(
                1L,
                1L,
                LocalDate.now(),
                LocalTime.of(13, 0, 0),
                LocalTime.of(13, 0, 0),
                20
        );

        Usuario usuario = criarUsuario();

        // Simula que existe um Usuário no banco com o ID informado na requisição.
        given(usuarioRepository.findById(reservaRequestHoraInicialIgual.usuarioId())).willReturn(Optional.of(usuario));

        // Simula que existe uma Sala no banco com o ID informado na requisição.
        Sala sala = criarSala();
        given(salaRepository.findById(reservaRequestHoraInicialIgual.salaId())).willReturn(Optional.of(sala));

        // Cria uma reserva existente para simular a atualização.
        Reserva reserva = criarReserva(usuario, sala);
        Long idReserva = 1L;

        // Simula que existe uma Reserva cadastrada com o ID informado
        given(reservaRepository.findById(idReserva)).willReturn(Optional.of(reserva));

        // ACT + ASSERT
        RegraNegocioException exception = Assertions.assertThrows(
                RegraNegocioException.class,
                () -> reservaService.atualizar(idReserva, reservaRequestHoraInicialIgual));

        // 1. Verificação de comportamento (Behavior Verification): Você verifica que determinadas chamadas aconteceram.

        // "Então Repository, você DEVERIA verificar que a busca do usuario por ID usando o método 'findById()' aconteceu".
        then(usuarioRepository).should().findById(reservaRequestHoraInicialIgual.usuarioId());

        // "Então Repository, você DEVERIA verificar que a busca de sala por ID usando o método 'findById' aconteceu".
        then(salaRepository).should().findById(reservaRequestHoraInicialIgual.salaId());

        // "Então Repository, você DEVERIA verificar se foi chamado o 'findById' do objeto reserva.
        then(reservaRepository).should().findById(idReserva);

        // Como o horário da reserva é inválido, o fluxo deve parar antes de validar conflitos e salvar.

        // "Então Repository, você DEVERIA verificar se NUNCA  foi chamado o 'findBySalaIdAndDataAndStatusAndIdNot()' do objeto reserva.
        then(reservaRepository).should(never()).findBySalaIdAndDataAndStatusAndIdNot(reservaRequestHoraInicialIgual.salaId(), reservaRequestHoraInicialIgual.data(), StatusReserva.ATIVA, idReserva);

        // "Então Repository, você DEVERIA verificar o 'save()' para reserva NUNCA aconteceu.
        then(reservaRepository).should(never()).save(any(Reserva.class));

        // 2. Verificação de estado (State Verification): Você verifica o resultado obtido.

        // Verificando se a exceção lançada contém a mensagem e o status esperados.
        Assertions.assertAll(
                () -> Assertions.assertEquals(
                        "A hora inicial deve ser anterior à hora final.",
                        exception.getMessage()),
                () -> Assertions.assertEquals(
                        HttpStatus.BAD_REQUEST,
                        exception.getStatus())
        );
    }

    @Test
    void naoDeveriaAtualizarReservaQuandoHorarioEstiverForaDoFuncionamento() {
        //Arrange

        // Cria uma requisição de reserva hora inicial maior que final.
        ReservaRequest reservaRequestForaHorarioFuncionamento = new ReservaRequest(
                1L,
                1L,
                LocalDate.now(),
                LocalTime.of(7, 0, 0),
                LocalTime.of(19, 0, 0),
                20
        );

        Usuario usuario = criarUsuario();

        // Simula que existe um Usuário no banco com o ID informado na requisição.
        given(usuarioRepository.findById(reservaRequestForaHorarioFuncionamento.usuarioId())).willReturn(Optional.of(usuario));

        // Simula que existe uma Sala no banco com o ID informado na requisição.
        Sala sala = criarSala();
        given(salaRepository.findById(reservaRequestForaHorarioFuncionamento.salaId())).willReturn(Optional.of(sala));

        // Cria uma reserva existente para simular a atualização.
        Reserva reserva = criarReserva(usuario, sala);
        Long idReserva = 1L;

        // Simula que existe uma Reserva cadastrada com o ID informado
        given(reservaRepository.findById(idReserva)).willReturn(Optional.of(reserva));

        // ACT + ASSERT
        RegraNegocioException exception = Assertions.assertThrows(
                RegraNegocioException.class,
                () -> reservaService.atualizar(idReserva, reservaRequestForaHorarioFuncionamento));

        // 1. Verificação de comportamento (Behavior Verification): Você verifica que determinadas chamadas aconteceram.

        // "Então Repository, você DEVERIA verificar que a busca do usuario por ID usando o método 'findById()' aconteceu".
        then(usuarioRepository).should().findById(reservaRequestForaHorarioFuncionamento.usuarioId());

        // "Então Repository, você DEVERIA verificar que a busca de sala por ID usando o método 'findById' aconteceu".
        then(salaRepository).should().findById(reservaRequestForaHorarioFuncionamento.salaId());

        // "Então Repository, você DEVERIA verificar se foi chamado o 'findById' do objeto reserva.
        then(reservaRepository).should().findById(idReserva);

        // Como o horário da reserva é inválido, o fluxo deve parar antes de validar conflitos e salvar.

        // "Então Repository, você DEVERIA verificar se NUNCA  foi chamado o 'findBySalaIdAndDataAndStatusAndIdNot()' do objeto reserva.
        then(reservaRepository).should(never()).findBySalaIdAndDataAndStatusAndIdNot(reservaRequestForaHorarioFuncionamento.salaId(), reservaRequestForaHorarioFuncionamento.data(), StatusReserva.ATIVA, idReserva);

        // "Então Repository, você DEVERIA verificar o 'save()' para reserva NUNCA aconteceu.
        then(reservaRepository).should(never()).save(any(Reserva.class));

        // 2. Verificação de estado (State Verification): Você verifica o resultado obtido.

        // Verificando se a exceção lançada contém a mensagem e o status esperados.
        Assertions.assertAll(
                () -> Assertions.assertEquals(
                        "Reservas devem ocorrer entre 08:00 e 18:00.",
                        exception.getMessage()),
                () -> Assertions.assertEquals(
                        HttpStatus.BAD_REQUEST,
                        exception.getStatus())
        );
    }

    @Test
    void naoDeveriaAtualizarReservaQuandoExistirConflitoDeHorario() {
        //Arrange

        Usuario usuario = criarUsuario();
        // Simula que existe um Usuário no banco com o ID informado na requisição.
        given(usuarioRepository.findById(reservaRequest.usuarioId())).willReturn(Optional.of(usuario));

        // Simula que existe uma Sala no banco com o ID informado na requisição.
        Sala sala = criarSala();
        given(salaRepository.findById(reservaRequest.salaId())).willReturn(Optional.of(sala));

        // Cria uma reserva existente para simular a atualização.
        Reserva reserva = criarReserva(usuario, sala);
        Long idReserva = 1L;

        // Simula que existe uma Reserva cadastrada com o ID informado
        given(reservaRepository.findById(idReserva)).willReturn(Optional.of(reserva));

        // Cria outra reserva já existente para gerar conflito de horário.
        Reserva reservaExistente = criarReservaExistente(usuario, sala);

        // Simula que já existe uma reserva ativa para a mesma sala e data
        given(reservaRepository.findBySalaIdAndDataAndStatusAndIdNot(reservaRequest.salaId(), reservaRequest.data(), StatusReserva.ATIVA, idReserva)).willReturn(List.of(reservaExistente));

        // ACT + ASSERT
        RegraNegocioException exception = Assertions.assertThrows(
                RegraNegocioException.class,
                () -> reservaService.atualizar(idReserva, reservaRequest));

        // 1. Verificação de comportamento (Behavior Verification): Você verifica que determinadas chamadas aconteceram.

        // "Então Repository, você DEVERIA verificar que a busca do usuario por ID usando o método 'findById()' aconteceu".
        then(usuarioRepository).should().findById(reservaRequest.usuarioId());

        // "Então Repository, você DEVERIA verificar que a busca de sala por ID usando o método 'findById' aconteceu".
        then(salaRepository).should().findById(reservaRequest.salaId());

        // "Então Repository, você DEVERIA verificar se foi chamado o 'findById' do objeto reserva.
        then(reservaRepository).should().findById(idReserva);

        // "Então Repository, você DEVERIA verificar se foi chamado o 'findBySalaIdAndDataAndStatusAndIdNot()' do objeto reserva.
        then(reservaRepository).should().findBySalaIdAndDataAndStatusAndIdNot(reservaRequest.salaId(), reservaRequest.data(), StatusReserva.ATIVA, idReserva);

        // Como já existe uma reserva para esse horário, o fluxo deve parar antes de salvar.

        // "Então Repository, você DEVERIA verificar o 'save()' para reserva NUNCA aconteceu.
        then(reservaRepository).should(never()).save(any(Reserva.class));

        // 2. Verificação de estado (State Verification): Você verifica o resultado obtido.

        // Verificando se a exceção lançada contém a mensagem e o status esperados.
        Assertions.assertAll(
                () -> Assertions.assertEquals(
                        "Conflito de horário. Já existe uma reserva para o período informado.",
                        exception.getMessage()),
                () -> Assertions.assertEquals(
                        HttpStatus.CONFLICT,
                        exception.getStatus())
        );
    }

    @Test
    void naoDeveriaAtualizarReservaQuandoQuantidadePessoasForMaiorCapacidadeSala() {
        //Arrange

        // Cria uma requisição de reserva com quantidade de pessoas superior a capacidade da sala.
        ReservaRequest reservaRequestQtdPessoasMaiorCapacidade = new ReservaRequest(
                1L,
                1L,
                LocalDate.now(),
                LocalTime.of(14, 0, 0),
                LocalTime.of(15, 0, 0),
                30
        );

        Usuario usuario = criarUsuario();

        // Simula que existe um Usuário no banco com o ID informado na requisição.
        given(usuarioRepository.findById(reservaRequestQtdPessoasMaiorCapacidade.usuarioId())).willReturn(Optional.of(usuario));

        // Simula que existe uma Sala no banco com o ID informado na requisição.
        Sala sala = criarSala();
        given(salaRepository.findById(reservaRequestQtdPessoasMaiorCapacidade.salaId())).willReturn(Optional.of(sala));

        // Cria uma reserva para simular a atualização.
        Reserva reserva = criarReserva(usuario, sala);
        Long idReserva = 1L;

        // Simula que existe uma Reserva cadastrada com o ID informado
        given(reservaRepository.findById(idReserva)).willReturn(Optional.of(reserva));

        // Simula que não existe nenhuma reserva conflitante para a sala na data informada.
        given(reservaRepository.findBySalaIdAndDataAndStatusAndIdNot(reservaRequestQtdPessoasMaiorCapacidade.salaId(), reservaRequestQtdPessoasMaiorCapacidade.data(), StatusReserva.ATIVA, idReserva)).willReturn(List.of());

        // ACT + ASSERT
        RegraNegocioException exception = Assertions.assertThrows(
                RegraNegocioException.class,
                () -> reservaService.atualizar(idReserva, reservaRequestQtdPessoasMaiorCapacidade));

        // 1. Verificação de comportamento (Behavior Verification): Você verifica que determinadas chamadas aconteceram.

        // "Então Repository, você DEVERIA verificar que a busca do usuario por ID usando o método 'findById()' aconteceu".
        then(usuarioRepository).should().findById(reservaRequestQtdPessoasMaiorCapacidade.usuarioId());

        // "Então Repository, você DEVERIA verificar que a busca de sala por ID usando o método 'findById' aconteceu".
        then(salaRepository).should().findById(reservaRequestQtdPessoasMaiorCapacidade.salaId());

        // "Então Repository, você DEVERIA verificar se foi chamado o 'findById' do objeto reserva.
        then(reservaRepository).should().findById(idReserva);

        // "Então Repository, você DEVERIA verificar se foi chamado o 'findBySalaIdAndDataAndStatusAndIdNot()' do objeto reserva.
        then(reservaRepository).should().findBySalaIdAndDataAndStatusAndIdNot(reservaRequestQtdPessoasMaiorCapacidade.salaId(), reservaRequestQtdPessoasMaiorCapacidade.data(), StatusReserva.ATIVA, idReserva);

        // Como a quantidade de pessoas excede a capacidade da sala, o fluxo deve parar antes de salvar a reserva.
        // "Então Repository, você DEVERIA verificar que o método 'save()' para reserva nunca aconteceu.
        then(reservaRepository).should(never()).save(any(Reserva.class));

        // 2. Verificação de estado (State Verification): Você verifica o resultado obtido.

        // Verificando se a exceção lançada contém a mensagem e o status esperados.
        Assertions.assertAll(
                () -> Assertions.assertEquals(
                        "A quantidade de pessoas excede a capacidade máxima da sala.",
                        exception.getMessage()),
                () -> Assertions.assertEquals(
                        HttpStatus.CONFLICT,
                        exception.getStatus())
        );
    }

    @Test
    void deveriaDeletarReservaPorIdQuandoEstiverCadastrada() {
        // ARRANGE

        // Cria um usuário para a reserva.
        Usuario usuario = criarUsuario();

        // Cria uma sala para a reserva.
        Sala sala = criarSala();

        // Cria reservas para simular o delete
        Reserva reserva = criarReserva(usuario, sala);
        Long id = 1L;
        given(reservaRepository.findById(id)).willReturn(Optional.of(reserva));

        // ACT
        reservaService.deletar(id);

        // ASSERT

        // 1. Verificação de comportamento (Behavior Verification): Você verifica que determinadas chamadas aconteceram.

        // Então Repository, você DEVERIA verificar que o 'findById()' aconteceu
        then(reservaRepository).should().findById(id);

        // Então Repository, você DEVERIA verificar que foi chamado o método 'delete()' recebendo a entity Reserva.
        then(reservaRepository).should().delete(reserva);

    }

    @Test
    void deveriaLancarExcecaoQuandoTentarDeletarReservaPorIdInexistente() {
        // ARRANGE

        Long id = 1L;
        given(reservaRepository.findById(id)).willReturn(Optional.empty());

        // ACT
        RegraNegocioException exception = Assertions.assertThrows(
                RegraNegocioException.class,
                () -> reservaService.deletar(id)
        );

        // ASSERT

        // 1. Verificação de comportamento (Behavior Verification): Você verifica que determinadas chamadas aconteceram.

        // Então Repository, você DEVERIA verificar que o 'findById()' aconteceu
        then(reservaRepository).should().findById(id);

        // Então Repository, você DEVERIA verificar que NUNCA foi chamado o método 'delete()'
        then(reservaRepository).should(never()).delete(any(Reserva.class));

        // 2. Verificação de estado (State Verification): Você verifica o resultado obtido.

        Assertions.assertAll(

                () -> Assertions.assertEquals(
                        "Reserva com id " + id + " não encontrada.",
                        exception.getMessage()),
                () -> Assertions.assertEquals(
                        HttpStatus.NOT_FOUND, exception.getStatus())

        );

    }

    private Usuario criarUsuario() {
        Usuario usuario = new Usuario(
                "Usuário Teste",
                "teste@gmail.com",
                "123"
        );
        ReflectionTestUtils.setField(usuario, "id", 1L); // Atribuindo um ID ao usuário persistido no banco

        return usuario;
    }

    private Sala criarSala() {
        Sala sala = new Sala(
                1,
                20
        );
        ReflectionTestUtils.setField(sala, "id", 1L);

        return sala;
    }

    private Reserva criarReserva(Usuario usuario, Sala sala) {
        Reserva reserva = new Reserva(
                LocalDate.now(),
                LocalTime.of(13, 0, 0),
                LocalTime.of(14, 0, 0),
                20
        );
        ReflectionTestUtils.setField(reserva, "id", 1L);
        reserva.setUsuario(usuario);
        reserva.setSala(sala);

        return reserva;
    }

    private Reserva criarReservaExistente(Usuario usuario, Sala sala) {
        // Criar duas reservas para simular conflitos de horário
        Reserva reservaExistente = new Reserva(
                LocalDate.now(),
                LocalTime.of(13, 30, 0),
                LocalTime.of(14, 30, 0),
                20
        );
        ReflectionTestUtils.setField(reservaExistente, "id", 1L);
        reservaExistente.setUsuario(usuario);
        reservaExistente.setSala(sala);
        return reservaExistente;
    }
}
package br.com.jhohannesfreitas.roomreservationapi.service;

import br.com.jhohannesfreitas.roomreservationapi.domain.entity.Sala;
import br.com.jhohannesfreitas.roomreservationapi.dto.SalaRequest;
import br.com.jhohannesfreitas.roomreservationapi.dto.SalaResponse;
import br.com.jhohannesfreitas.roomreservationapi.exception.RegraNegocioException;
import br.com.jhohannesfreitas.roomreservationapi.repository.SalaRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class SalaServiceTest {

    @InjectMocks
    private SalaService salaService;

    @Mock
    private SalaRepository salaRepository;

    private SalaRequest salaRequest;

    @BeforeEach
    void setUp() {
        salaRequest = new SalaRequest(1, 20);
    }

    @Test
    void deveriaCadastrarSalaQuandoNaoTiverNumeroJaCadastrado() {
        // ARRANGE

        //1- Simula que NÃO existe uma sala cadastrado com o número vindo de salaRequest, retornando false
        given(salaRepository.existsByNumero(salaRequest.numero())).willReturn(false);

        //2- Simula o retorno do banco após o save(), atribuindo um ID a sala persistida.
        Sala sala = criarSala();
        given(salaRepository.save(any(Sala.class))).willReturn(sala);

        // ACT
        SalaResponse salaResponse = salaService.cadastrar(salaRequest);

        // ASSERT

        // 1. Verificação de comportamento (Behavior Verification): Você verifica que determinadas chamadas aconteceram.

        // "Então Repository, você DEVERIA verificar se foi chamado o 'existsByNumero()' 1x passando o salaRequest.numero.
        then(salaRepository).should().existsByNumero(salaRequest.numero());
        // "Então Repository, você DEVERIA verificar se foi chamado o 'save()' 1x para qualquer objeto sala.
        then(salaRepository).should().save(any(Sala.class));

        // 2. Verificação de estado (State Verification): Você verifica o resultado obtido.

        // Verificando se os dados retornados são iguais aos da entidade persistida (Sala).
        Assertions.assertAll(
                () -> Assertions.assertEquals(sala.getId(), salaResponse.id()),
                () -> Assertions.assertEquals(sala.getNumero(), salaResponse.numero()),
                () -> Assertions.assertEquals(sala.getCapacidade(), salaResponse.capacidade())
        );
    }

    @Test
    void naoDeveriaCadastrarSalaQuandoTiverNumeroJaCadastrado() {
        // ARRANGE

        //1- Simula que NÃO existe uma sala cadastrado com o número vindo de salaRequest, retornando false
        given(salaRepository.existsByNumero(salaRequest.numero())).willReturn(true);

        // ACT
        RegraNegocioException exception = Assertions.assertThrows(
                RegraNegocioException.class,
                () -> salaService.cadastrar(salaRequest));

        // ASSERT

        // 1. Verificação de comportamento (Behavior Verification): Você verifica que determinadas chamadas aconteceram.

        // "Então Repository, você DEVERIA verificar se foi chamado o 'existsByNumero()' 1x passando o salaRequest.numero".
        then(salaRepository).should().existsByNumero(salaRequest.numero());
        // "Então Repository, você DEVERIA verificar se NUNCA foi chamado o 'save()' para qualquer objeto sala".
        then(salaRepository).should(never()).save(any(Sala.class));

        // 2. Verificação de estado (State Verification): Você verifica o resultado obtido.

        // Verificando se o valor esperado é igual ao obtido para os atributos da exceção: message e status
        Assertions.assertAll(
                () -> Assertions.assertEquals("Sala já cadastrada com este número.", exception.getMessage()),
                () -> Assertions.assertEquals(HttpStatus.CONFLICT, exception.getStatus())
        );

    }

    @Test
    void deveriaListarSalasQuandoTiverCadastradas() {
        // ARRANGE

        Sala sala = criarSala();
        given(salaRepository.findAll()).willReturn(List.of(sala));

        // ACT
        List<SalaResponse> salaResponseList = salaService.listar();

        // ASSERT

        // 1. Verificação de comportamento (Behavior Verification): Você verifica que determinadas chamadas aconteceram.

        // Então Repository, você DEVERIA verificar que o 'findAll()' aconteceu 1x para o objeto Sala.
        then(salaRepository).should().findAll();

        // 2. Verificação de estado (State Verification): Você verifica o resultado obtido.

        // Obtém a primeira sala da lista para validar seus dados.
        SalaResponse salaResponse = salaResponseList.getFirst();

        Assertions.assertAll(
                () -> Assertions.assertEquals(1, salaResponseList.size()), // Verifica se a lista retornada possui a quantidade esperada de registros.
                () -> Assertions.assertEquals(sala.getId(), salaResponse.id()),
                () -> Assertions.assertEquals(sala.getNumero(), salaResponse.numero()),
                () -> Assertions.assertEquals(sala.getCapacidade(), salaResponse.capacidade()),
                () -> Assertions.assertEquals(sala.getStatus(), salaResponse.status())
        );
    }

    @Test
    void deveriaRetornarListaVaziaQuandoNaoExistiremSalasCadastradas() {
        // ARRANGE

        given(salaRepository.findAll()).willReturn(List.of());

        // ACT
        List<SalaResponse> salaResponseList = salaService.listar();

        // ASSERT

        // 1. Verificação de comportamento (Behavior Verification): Você verifica que determinadas chamadas aconteceram.

        // Então Repository, você DEVERIA verificar que o 'findAll()' aconteceu 1x para o objeto Sala.
        then(salaRepository).should().findAll();

        // 2. Verificação de estado (State Verification): Você verifica o resultado obtido.

        // Verifica se o método retornou uma lista vazia.
        Assertions.assertTrue(salaResponseList.isEmpty());
    }

    @Test
    void deveriaBuscarSalaPorIdQuandoTiverCadastrada() {
        // ARRANGE
        Sala sala = criarSala();
        given(salaRepository.findById(sala.getId())).willReturn(Optional.of(sala));

        // ACT
        SalaResponse salaResponse = salaService.listarPorId(sala.getId());

        // ASSERT

        // 1. Verificação de comportamento (Behavior Verification): Você verifica que determinadas chamadas aconteceram.

        // Então Repository, você DEVERIA verificar que o 'findById()' foi chamado 1x.
        then(salaRepository).should().findById(sala.getId());

        // 2. Verificação de estado (State Verification): Você verifica o resultado obtido.

        Assertions.assertAll(
                () -> Assertions.assertEquals(sala.getId(), salaResponse.id()),
                () -> Assertions.assertEquals(sala.getNumero(), salaResponse.numero()),
                () -> Assertions.assertEquals(sala.getCapacidade(), salaResponse.capacidade()),
                () -> Assertions.assertEquals(sala.getStatus(), salaResponse.status())
        );
    }

    @Test
    void naoDeveriaBuscarSalaPorIdQuandoNaoTiverCadastrada() {
        // ARRANGE
        Long id = 1L;
        given(salaRepository.findById(id)).willReturn(Optional.empty());

        // ACT + ASSERT

        RegraNegocioException exception = Assertions.assertThrows(
                RegraNegocioException.class,
                () -> salaService.listarPorId(id)
        );

        // 1. Verificação de comportamento (Behavior Verification): Você verifica que determinadas chamadas aconteceram.

        // Então Repository, você DEVERIA verificar que o 'findById()' foi chamado 1x.
        then(salaRepository).should().findById(id);

        // 2. Verificação de estado (State Verification): Você verifica o resultado obtido.

        Assertions.assertAll(
                () -> Assertions.assertEquals(
                        "Sala com id " + id + " não encontrada.",
                        exception.getMessage()),
                () -> Assertions.assertEquals(
                        HttpStatus.NOT_FOUND,
                        exception.getStatus())
        );
    }

    @Test
    void deveriaAtualizarSalaComSucesso() {
        // ARRANGE

        // Simular que o ID da sala existe no meu banco
        Sala sala = criarSala();
        given(salaRepository.findById(sala.getId())).willReturn(Optional.of(sala));

        // Simula a verificação se existe OUTRA Sala com este mesmo número, retorna um Optional vazio
        given(salaRepository.findByNumero(salaRequest.numero())).willReturn(Optional.empty());

        // Simula o retorno do banco após o save(), devolvendo a entidade persistida.
        given(salaRepository.save(any(Sala.class))).willReturn(sala);

        // ACT
        SalaResponse salaResponse = salaService.atualizar(sala.getId(), salaRequest);

        // ASSERT
        // 1. Verificação de comportamento (Behavior Verification): Você verifica que determinadas chamadas aconteceram.

        // Então Repository, você DEVERIA verificar que o 'findById()' foi chamado 1x.
        then(salaRepository).should().findById(sala.getId());
        then(salaRepository).should().findByNumero(salaRequest.numero());
        then(salaRepository).should().save(any(Sala.class));

        // 2. Verificação de estado (State Verification): Você verifica o resultado obtido.

        Assertions.assertAll(
                () -> Assertions.assertEquals(sala.getId(), salaResponse.id()),
                () -> Assertions.assertEquals(sala.getNumero(), salaResponse.numero()),
                () -> Assertions.assertEquals(sala.getCapacidade(), salaResponse.capacidade()),
                () -> Assertions.assertEquals(sala.getStatus(), salaResponse.status())
        );
    }

    @Test
    void naoDeveriaAtualizarSalaQuandoIdNaoEstiverCadastrado() {
        // ARRANGE

        // Simular que o ID da sala NÃO existe no meu banco
        Long id = 1L;
        given(salaRepository.findById(id)).willReturn(Optional.empty());


        // ACT + ASSERT
        RegraNegocioException exception = Assertions.assertThrows(
                RegraNegocioException.class,
                () -> salaService.atualizar(id, salaRequest)
        );

        // 1. Verificação de comportamento (Behavior Verification): Você verifica que determinadas chamadas aconteceram.

        // Então Repository, você DEVERIA verificar que o 'findById()' foi chamado 1x.
        then(salaRepository).should().findById(id);

        // Então Repository, você DEVERIA verificar que NUNCA foi chamado o método 'findByNumero()' recebendo algum int.
        then(salaRepository).should(never()).findByNumero(anyInt());

        // Então Repository, você DEVERIA verificar que NUNCA foi chamado o método 'save()' recebendo alguma entity Sala.
        then(salaRepository).should(never()).save(any(Sala.class));

        // 2. Verificação de estado (State Verification): Você verifica o resultado obtido.

        Assertions.assertAll(
                () -> Assertions.assertEquals(
                        "Sala com id " + id + " não encontrada.",
                        exception.getMessage()),
                () -> Assertions.assertEquals(
                        HttpStatus.NOT_FOUND,
                        exception.getStatus())
        );
    }

    @Test
    void naoDeveriaAtualizarSalaQuandoNumeroJaEstiverCadastrado() {
        // ARRANGE

        // Simular que o ID da sala existe no meu banco
        Sala sala = criarSala();
        given(salaRepository.findById(sala.getId())).willReturn(Optional.of(sala));

        // Outra sala
        Sala outraSala = new Sala(
                2,
                30
        );
        ReflectionTestUtils.setField(outraSala, "id", 2L);

        // Simula a verificação se existe OUTRA Sala com este mesmo número, retorna um Optional outraSala
        given(salaRepository.findByNumero(salaRequest.numero())).willReturn(Optional.of(outraSala));


        // ACT + ASSERT
        RegraNegocioException exception = Assertions.assertThrows(
                RegraNegocioException.class,
                () -> salaService.atualizar(sala.getId(), salaRequest)
        );

        // 1. Verificação de comportamento (Behavior Verification): Você verifica que determinadas chamadas aconteceram.

        // Então Repository, você DEVERIA verificar que o 'findById()' foi chamado 1x.
        then(salaRepository).should().findById(sala.getId());
        then(salaRepository).should().findByNumero(salaRequest.numero());

        // Então Repository, você DEVERIA verificar que NUNCA foi chamado o método 'save()' recebendo alguma entity Sala.
        then(salaRepository).should(never()).save(any(Sala.class));

        // 2. Verificação de estado (State Verification): Você verifica o resultado obtido.

        // Verificando se o valor esperado é igual ao obtido para os atributos da exceção: message e status
        Assertions.assertAll(
                () -> Assertions.assertEquals(
                        "Sala já cadastrada com este número.",
                        exception.getMessage()),
                () -> Assertions.assertEquals(HttpStatus.CONFLICT,
                        exception.getStatus())
        );
    }

    @Test
    void deveriaDeletarSalaPorIdQuandoEstiverCadastrada() {
        //Arrange
        Sala sala = criarSala();
        given(salaRepository.findById(sala.getId())).willReturn(Optional.of(sala));

        // Act
        salaService.deletar(sala.getId());

        // Assert

        // 1. Verificação de comportamento (Behavior Verification): Você verifica que determinadas chamadas aconteceram.

        // Então Repository, você DEVERIA verificar que o 'findById()' foi chamado 1x.
        then(salaRepository).should().findById(sala.getId());

        // Então Repository, você DEVERIA verificar que foi chamado o método 'delete()' recebendo a entity Sala.
        then(salaRepository).should().delete(sala);
    }

    @Test
    void naoDeveriaDeletarSalaPorIdQuandoNaoEstiverCadastrada() {
        //Arrange
        Long id = 1L;
        given(salaRepository.findById(id)).willReturn(Optional.empty());

        // Act + Assert
        RegraNegocioException exception = Assertions.assertThrows(
                RegraNegocioException.class,
                () -> salaService.deletar(id)
        );

        // 1. Verificação de comportamento (Behavior Verification): Você verifica que determinadas chamadas aconteceram.

        // Então Repository, você DEVERIA verificar que o 'findById()' foi chamado 1x.
        then(salaRepository).should().findById(id);

        // Então Repository, você DEVERIA verificar que NUNCA foi chamado o método 'delete()' recebendo alguma entity Sala.
        then(salaRepository).should(never()).delete(any(Sala.class));

        // 2. Verificação de estado (State Verification): Você verifica o resultado obtido.

        // Verificando se o valor esperado é igual ao obtido para os atributos da exceção: message e status
        Assertions.assertAll(
                () -> Assertions.assertEquals(
                        "Sala com id " + id + " não encontrada.",
                        exception.getMessage()),
                () -> Assertions.assertEquals(HttpStatus.NOT_FOUND,
                        exception.getStatus())
        );
    }

    private Sala criarSala() {
        Sala sala = new Sala(
                1,
                20
        );
        ReflectionTestUtils.setField(sala, "id", 1L);

        return sala;
    }
}
package br.com.jhohannesfreitas.roomreservationapi.service;

import br.com.jhohannesfreitas.roomreservationapi.domain.entity.Usuario;
import br.com.jhohannesfreitas.roomreservationapi.dto.UsuarioRequest;
import br.com.jhohannesfreitas.roomreservationapi.dto.UsuarioResponse;
import br.com.jhohannesfreitas.roomreservationapi.exception.RegraNegocioException;
import br.com.jhohannesfreitas.roomreservationapi.repository.UsuarioRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @InjectMocks
    private UsuarioService usuarioService;

    @Mock
    private UsuarioRepository usuarioRepository;

    private UsuarioRequest usuarioRequest;

    @BeforeEach
    void setUp() {
        usuarioRequest = new UsuarioRequest("Usuario Teste", "teste@teste.com", "teste@123");
    }

    // Sugestão de nome para testes: deveria[Comportamento]Quando[Cenário]
    @Test
    void deveriaCadastrarUsuarioQuandoNaoEstiverEmailCadastrado() {

        //ARRANGE (PREPARAR) - Configurar o ambiente
        //1- Simula que NÃO existe um usuário cadastrado com este e-mail, retornando false
        BDDMockito.given(usuarioRepository.existsByEmail(usuarioRequest.email())).willReturn(false);

        //2- Simula o retorno do banco após o save(), atribuindo um ID ao usuário persistido.
        Usuario usuario = criarUsuario();
        BDDMockito.given(usuarioRepository.save(any(Usuario.class))).willReturn(usuario);

        //ACT (AGIR) - A ação que se deseja testar é executada (método)
        UsuarioResponse response = usuarioService.cadastrar(usuarioRequest);

        //ASSERT (VERIFICAR) - Verifica se o resultado obtido após a ação está de acordo com o que se esperava do teste

        // "Então Repository, você DEVERIA verificar se foi chamado o 'save()' 1x para algum objeto Usuario.
        then(usuarioRepository).should().save(any(Usuario.class));

        // "Então Repository, você DEVERIA verificar se foi chamado o 'existsByEmail()' 1x para algum objeto Usuario.
        then(usuarioRepository).should().existsByEmail(usuarioRequest.email());

        // "Verifique se o nome que eu esperava é igual ao nome que o método retornou."
        // assertEquals(valorEsperado, valorObtido);

        // Com assertAll, você recebe os erros de uma vez.
        Assertions.assertAll(
                () -> Assertions.assertEquals(1L, response.id()),
                () -> Assertions.assertEquals(usuario.getNome(), response.nome()),
                () -> Assertions.assertEquals(usuario.getEmail(), response.email())
        );

        // Com vários assertEquals, o teste para no primeiro erro.
        //Assertions.assertEquals(1L,response.id());
        //Assertions.assertEquals(usuario.getNome(),response.nome());
        //Assertions.assertEquals(usuario.getEmail(),response.email());
    }

    @Test
    void naoDeveriaCadastrarUsuarioQuandoEmailJaEstiverCadastrado() {

        //ARRANGE (PREPARAR) - Configurar o ambiente

        // Simula que já existe um usuário cadastrado com este e-mail, retornando true
        BDDMockito.given(usuarioRepository.existsByEmail(usuarioRequest.email())).willReturn(true);

        //ACT + ASSERT

        // Execute esse código e verifique se ele lança essa exceção, captura a exceção para validar seus dados.
        // () -> "Uma função que não recebe parâmetros."
        RegraNegocioException exception = Assertions.assertThrows(
                RegraNegocioException.class,
                () -> usuarioService.cadastrar(usuarioRequest)); // "() -> Aqui está um código que pode explodir. Execute e veja se explode com a exceção correta."

        // Verificando se o valor esperado é igual ao obtido para os atributos da exceção: message e status
        Assertions.assertAll(
                () -> Assertions.assertEquals(
                        "Usuário já cadastrado com este e-mail",
                        exception.getMessage()),
                () -> Assertions.assertEquals(
                        HttpStatus.CONFLICT,
                        exception.getStatus())
        );

        // Então Repository, você DEVERIA verificar que o 'existsByEmail()' aconteceu 1x para o e-mail informado.
        then(usuarioRepository).should().existsByEmail(usuarioRequest.email());

        // Então Repository, você DEVERIA verificar que o 'save()' NUNCA aconteceu para o objeto Usuario.
        then(usuarioRepository).should(never()).save(any(Usuario.class));

    }

    @Test
    void deveriaListarUsuariosQuandoTiverCadastrados() {
        // ARRANGE
        Usuario usuario = criarUsuario();
        BDDMockito.given(usuarioRepository.findAll()).willReturn(List.of(usuario));

        // ACT
        List<UsuarioResponse> listResponse = usuarioService.listar();

        // ASSERT

        // Então Repository, você DEVERIA verificar que o 'findAll()' aconteceu 1x para o objeto Usuario.
        then(usuarioRepository).should().findAll();

        // "Verifique se o nome que eu esperava é igual ao nome que o método retornou."
        // assertEquals(valorEsperado, valorObtido);
        Assertions.assertEquals(1, listResponse.size());
        UsuarioResponse usuarioResponse = listResponse.get(0); // Pega o primero elemento da lista e coloca dentro da variável usuarioResponse
        Assertions.assertAll(
                () -> Assertions.assertEquals(usuario.getId(), usuarioResponse.id()),
                () -> Assertions.assertEquals(usuario.getNome(), usuarioResponse.nome()),
                () -> Assertions.assertEquals(usuario.getEmail(), usuarioResponse.email())
        );

    }

    @Test
    void deveriaRetornarListaVaziaQuandoNaoTiverUsuariosCadastrados() {
        // 1 - ARRANGE
        BDDMockito.given(usuarioRepository.findAll()).willReturn(List.of()); // lista imutável vazia

        // 2 - ACT
        List<UsuarioResponse> listResponse = usuarioService.listar();

        // 3 - ASSERT

        // Então Repository, você DEVERIA verificar que o 'findAll()' aconteceu 1x para o objeto Usuario.
        then(usuarioRepository).should().findAll();

        // "Verifique se o método retornou uma lista vazia"
        Assertions.assertTrue(listResponse.isEmpty()); // Verifica se é verdade que a lista deve estar vazia
    }

    @Test
    void deveriaBuscarUsuarioPorIdQuandoEstiverCadastrado() {

        // 1 - Arrange
        Usuario usuario = criarUsuario();

        // "QUANDO o método findById() do usuarioRepository for chamado passando esse ID, ENTÃO devolva um Optional contendo esse usuário."
        when(usuarioRepository.findById(usuario.getId())).thenReturn(Optional.of(usuario));

        // 2 - ACT
        UsuarioResponse usuarioResponse = usuarioService.listarPorId(usuario.getId());

        // 3 - ASSERT

        // Então Repository, você DEVERIA verificar que o 'findById()' aconteceu 1x passando o ID do Usuario.
        then(usuarioRepository).should().findById(usuario.getId());

        // Verificando se o valor esperado é igual ao valor retornado pelo service
        Assertions.assertAll(
                () -> Assertions.assertEquals(usuario.getId(), usuarioResponse.id()),
                () -> Assertions.assertEquals(usuario.getNome(), usuarioResponse.nome()),
                () -> Assertions.assertEquals(usuario.getEmail(), usuarioResponse.email())
        );
    }

    @Test
    void naoDeveriaRetornarUsuarioPorIdQuandoNaoEstiverCadastrado() {

        // 1 - Arrange
        Long id = 1L;

        // QUANDO o método findById() do usuarioRepository for chamado passando esse ID,
        // ENTÃO devolva um Optional vazio simulando usuário inexistente
        when(usuarioRepository.findById(id)).thenReturn(Optional.empty());

        // 2 - ACT + Assert
        RegraNegocioException exception = Assertions.assertThrows(
                RegraNegocioException.class,
                () -> usuarioService.listarPorId(id)
        );

        // Verificando se o valor esperado é igual ao obtido para os atributos da exceção: message e status
        Assertions.assertAll(
                () -> Assertions.assertEquals(
                        "Usuário com id " + id + " não encontrado.",
                        exception.getMessage()),
                () -> Assertions.assertEquals(
                        HttpStatus.NOT_FOUND,
                        exception.getStatus())
        );

        // Então Repository, você DEVERIA verificar que o 'findById()' aconteceu 1x passando o ID do Usuario.
        then(usuarioRepository).should().findById(1L);
    }

    @Test
    void deveriaAtualizarUsuarioComSucesso() {
        // 1- ARRANGE

        // Busca o id no banco pra saber se o usuário existe
        Usuario usuario = criarUsuario();
        BDDMockito.given(usuarioRepository.findById(usuario.getId())).willReturn(Optional.of(usuario));

        // Verificar se existe OUTRO usuário com o e-mail informado
        BDDMockito.given(usuarioRepository.findByEmail(usuarioRequest.email())).willReturn(Optional.empty());

        // Simula o retorno do banco após o save(), devolvendo a entidade persistida.
        BDDMockito.given(usuarioRepository.save(any(Usuario.class))).willReturn(usuario);

        // 2 - ACT
        UsuarioResponse usuarioResponse = usuarioService.atualizar(usuario.getId(), usuarioRequest);

        // 3 - ASSERT

        // Behavior verification
        then(usuarioRepository).should().findById(usuario.getId());
        then(usuarioRepository).should().findByEmail(usuarioRequest.email());

        // "Então Repository, você DEVERIA verificar se foi chamado o 'save()' 1x para algum objeto Usuario.
        then(usuarioRepository).should().save(any(Usuario.class));

        // State verification
        Assertions.assertAll(
                () -> Assertions.assertEquals(usuario.getId(), usuarioResponse.id()),
                () -> Assertions.assertEquals(usuarioRequest.nome(), usuarioResponse.nome()),
                () -> Assertions.assertEquals(usuarioRequest.email(), usuarioResponse.email())
        );


    }

    @Test
    void naoDeveriaAtualizarUsuarioQuandoIdNaoEstiverCadastrado() {
        // 1- ARRANGE

        // Busca o id no banco pra saber se o usuário existe
        Long id = 1L;
        BDDMockito.given(usuarioRepository.findById(id)).willReturn(Optional.empty());

        // 2 - ACT + 3 - ASSERT

        RegraNegocioException exception = Assertions.assertThrows(
                RegraNegocioException.class,
                () -> usuarioService.atualizar(id, usuarioRequest)
        );

        // 1. Verificação de comportamento (Behavior Verification): Você verifica que determinadas chamadas aconteceram.

        // "Então Repository, você DEVERIA verificar se foi chamado o 'findById()' 1x passando o id do Usuário.
        then(usuarioRepository).should().findById(id);

        // 2. Verificação de estado (State Verification): Você verifica o resultado obtido.

        // Verificando se o valor esperado é igual ao obtido para os atributos da exceção: message e status
        Assertions.assertAll(
                () -> Assertions.assertEquals(
                        "Usuário com id " + id + " não encontrado.",
                        exception.getMessage()),
                () -> Assertions.assertEquals(
                        HttpStatus.NOT_FOUND,
                        exception.getStatus())
        );


    }

    @Test
    void naoDeveriaAtualizarUsuarioQuandoEmailJaEstiverCadastrado() {
        // 1- ARRANGE

        // Busca o id no banco pra saber se o usuário existe
        Usuario usuario = criarUsuario();
        BDDMockito.given(usuarioRepository.findById(usuario.getId())).willReturn(Optional.of(usuario));

        // Outro usuario
        Usuario outroUsuario = new Usuario(
                usuarioRequest.nome(),
                usuarioRequest.email(),
                usuarioRequest.senha());
        ReflectionTestUtils.setField(outroUsuario, "id", 2L);

        // Verificar se existe OUTRO usuário com o e-mail informado
        BDDMockito.given(usuarioRepository.findByEmail(usuarioRequest.email())).willReturn(Optional.of(outroUsuario));

        // 2 - ACT + 3 - ASSERT
        RegraNegocioException exception = Assertions.assertThrows(
                RegraNegocioException.class,
                () -> usuarioService.atualizar(usuario.getId(), usuarioRequest)
        );

        // 1. Verificação de comportamento (Behavior Verification): Você verifica que determinadas chamadas aconteceram.

        // "Então Repository, você DEVERIA verificar se foi chamado o 'findById()' 1x passando o id do Usuario.
        then(usuarioRepository).should().findById(usuario.getId());

        // "Então Repository, você DEVERIA verificar se foi chamado o 'findByEmail()' 1x passando o e-mail vindo da requisição.
        then(usuarioRepository).should().findByEmail(usuarioRequest.email());

        // "Então Repository, você DEVERIA verificar se NUNCA foi chamado o 'save()' para o objeto usuario.
        then(usuarioRepository).should(never()).save(any(Usuario.class));

        // 2. Verificação de estado (State Verification): Você verifica o resultado obtido.

        // Verificando se o valor esperado é igual ao obtido para os atributos da exceção: message e status
        Assertions.assertAll(
                () -> Assertions.assertEquals(
                        "Já existe um usuário cadastrado com este e-mail.",
                        exception.getMessage()),
                () -> Assertions.assertEquals(
                        HttpStatus.CONFLICT,
                        exception.getStatus())
        );
    }

    @Test
    void deveriaDeletarUsuarioPorIdQuandoEstiverCadastrado() {

        // 1 - Arrange
        Usuario usuario = criarUsuario();

        // "QUANDO o método findById() do usuarioRepository for chamado passando esse ID, ENTÃO devolva um Optional contendo esse usuário."
        when(usuarioRepository.findById(usuario.getId())).thenReturn(Optional.of(usuario));

        // 2 - ACT
        usuarioService.deletar(usuario.getId());

        // 3 - ASSERT
        // Então Repository, você DEVERIA verificar que o 'findById()' aconteceu 1x passando o ID do Usuario.
        then(usuarioRepository).should().findById(usuario.getId());

        // Então Repository, você DEVERIA verificar que o 'deleteById()' aconteceu 1x passando o objeto Usuario.
        then(usuarioRepository).should().delete(usuario);

    }

    @Test
    void naoDeveriaDeletarUsuarioPorIdQuandoNaoEstiverCadastrado() {

        // 1 - Arrange
        Long id = 1L;

        // Simula que não existe usuário cadastrado com o ID informado, returnando um Optional vazio
        when(usuarioRepository.findById(id)).thenReturn(Optional.empty());

        // 2 - ACT + ASSERT
        RegraNegocioException exception = Assertions.assertThrows(
                RegraNegocioException.class,
                () -> usuarioService.deletar(id)
        );

        // Verificando se o valor esperado é igual ao obtido para os atributos da exceção: message e status
        Assertions.assertAll(
                () -> Assertions.assertEquals(
                        "Usuário com id " + id + " não encontrado.",
                        exception.getMessage()),
                () -> Assertions.assertEquals(
                        HttpStatus.NOT_FOUND,
                        exception.getStatus()
                )
        );

        // Então Repository, você DEVERIA verificar que o 'findById()' aconteceu 1x passando o ID do Usuario.
        then(usuarioRepository).should().findById(id);

        // Então Repository, você DEVERIA verificar que o 'delete()' NUNCA aconteceu passando o objeto Usuario.
        then(usuarioRepository).should(never()).delete(any(Usuario.class));

    }

    private Usuario criarUsuario() {
        Usuario usuario = new Usuario(
                usuarioRequest.nome(),
                usuarioRequest.email(),
                usuarioRequest.senha()
        );
        ReflectionTestUtils.setField(usuario, "id", 1L); // Atribuindo um ID ao usuário persistido no banco

        return usuario;
    }
}
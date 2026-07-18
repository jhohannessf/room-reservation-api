package br.com.jhohannesfreitas.roomreservationapi.service;

import br.com.jhohannesfreitas.roomreservationapi.domain.entity.Usuario;
import br.com.jhohannesfreitas.roomreservationapi.dto.UsuarioRequest;
import br.com.jhohannesfreitas.roomreservationapi.dto.UsuarioResponse;
import br.com.jhohannesfreitas.roomreservationapi.exception.RegraNegocioException;
import br.com.jhohannesfreitas.roomreservationapi.mapper.UsuarioMapper;
import br.com.jhohannesfreitas.roomreservationapi.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public UsuarioResponse cadastrar(UsuarioRequest usuarioRequest) {

        // Verifica se o usuario já existe no banco com este e-mail
        if (usuarioRepository.existsByEmail(usuarioRequest.email())) {
            throw new RegraNegocioException("Usuário já cadastrado com este e-mail",
                    HttpStatus.CONFLICT);
        }

        // Transforma o meu DTO recebido como parâmetro na requisição em uma Entity
        Usuario usuario = UsuarioMapper.toEntity(usuarioRequest);

        // Salvo meu usuário
        Usuario usuarioSalvo = usuarioRepository.save(usuario);

        // Retorno o meu DTO de resposta
        return UsuarioMapper.toResponse(usuarioSalvo);

    }

    public List<UsuarioResponse> listar() {
        return usuarioRepository.findAll()
                .stream()
                .map(UsuarioMapper::toResponse) // usuario -> UsuarioMapper.toResponse(usuario)
                .toList();

    }

    public UsuarioResponse listarPorId(Long id) {
        // Busca a Entity Usuario no banco pelo ID, se não encontrar lança exception
        // Caso encontre, transforma a Entity Usuario em DTO UsuarioResponse, passando como parâmetro
        // o método 'buscarPorId' que retorna Um usuario do ID informado
        return UsuarioMapper.toResponse(buscarPorId(id));
    }

    @Transactional
    public UsuarioResponse atualizar(Long id, UsuarioRequest usuarioRequest) {

        // Busca o id no banco pra saber se o usuário existe
        Usuario usuario = buscarPorId(id);

        // Verificar se existe OUTRO usuário com o e-mail informado
        validarEmailDisponivel(usuarioRequest.email(),id);

        // Settar os dados do usuarioRequest (DTO) na minha Entity
        usuario.atualizar(usuarioRequest);

        // Salvo a minha Entity atualizada com os dados passados do UsuarioRequest(DTO) como parâmetro
        Usuario usuarioAtualizado = usuarioRepository.save(usuario);

        // Retorna a resposta transformando a minha Entity Usuario em DTO UsuarioResponse
        return UsuarioMapper.toResponse(usuarioAtualizado);

    }

    @Transactional
    public void deletar(Long id) {
        // Busca se o ID do usuário existe no banco
        Usuario usuario = buscarPorId(id);

        // Deleta o usuário
        usuarioRepository.delete(usuario);
    }

    private void validarEmailDisponivel(String email, Long id) {
        // Verificar se existe OUTRO usuário com o e-mail informado
        Optional<Usuario> outroUsuario = usuarioRepository.findByEmail(email);
        if (outroUsuario.isPresent()) {
            Usuario usuarioComMesmoEmail = outroUsuario.get(); // ".get = Me entregue o valor de referência que está dentro desse Optional"
            if (!usuarioComMesmoEmail.getId().equals(id)) {
                throw new RegraNegocioException("Já existe um usuário cadastrado com este e-mail.",
                        HttpStatus.CONFLICT);
            }
        }
    }

    private Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Usuário com id " + id + " não encontrado.",
                        HttpStatus.NOT_FOUND));
    }
}

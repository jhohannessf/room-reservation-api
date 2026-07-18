package br.com.jhohannesfreitas.roomreservationapi.mapper;

import br.com.jhohannesfreitas.roomreservationapi.domain.entity.Usuario;
import br.com.jhohannesfreitas.roomreservationapi.dto.UsuarioRequest;
import br.com.jhohannesfreitas.roomreservationapi.dto.UsuarioResponse;

public class UsuarioMapper {

    // Mapper de entrada: Transforma uma DTO em uma Entity
    public static Usuario toEntity(UsuarioRequest dto) {
        return new Usuario(
                dto.nome(),
                dto.email(),
                dto.senha()
        );
    }

    // Mapper de saída: Transforma uma Entity em um DTO
    public static UsuarioResponse toResponse(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail()
        );
    }

}

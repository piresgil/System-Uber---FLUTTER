package application.mappers;

import application.dto.ColaboradorDTO;
import application.model.Colaborador;

/**
 * Mapper responsável por converter entre Colaborador e ColaboradorDTO.
 * Evita expor diretamente a entidade JPA e facilita a comunicação com a API.
 */
public class ColaboradorMapper {

    /**
     * Converte uma entidade Colaborador para ColaboradorDTO.
     *
     * @param entity Entidade Colaborador
     * @return DTO correspondente
     */
    public static ColaboradorDTO toDTO(Colaborador entity) {
        if (entity == null) return null;

        return new ColaboradorDTO(
                entity.getId(),
                entity.getNome(),
                entity.getEmail(),
                entity.getTelefone(),
                entity.getTipo()
        );
    }

    /**
     * Converte um ColaboradorDTO para a entidade Colaborador.
     *
     * @param dto DTO recebido da API
     * @return Entidade Colaborador
     */
    public static Colaborador toEntity(ColaboradorDTO dto) {
        if (dto == null) return null;

        Colaborador c = new Colaborador();
        c.setId(dto.getId());
        c.setNome(dto.getNome());
        c.setEmail(dto.getEmail());
        c.setTelefone(dto.getTelefone());
        c.setTipo(dto.getTipo());
        return c;
    }
}

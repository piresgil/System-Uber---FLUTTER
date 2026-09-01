package application.mappers;

import application.dto.ContratoDTO;
import application.model.Contrato;

/**
 * Mapper responsável por converter entre Contrato e ContratoDTO.
 */
public class ContratoMapper {

    public static ContratoDTO toDTO(Contrato entity) {
        if (entity == null) return null;

        return new ContratoDTO(
                entity.getId(),
                entity.getNumero(),
                entity.getDescricao()
        );
    }

    public static Contrato toEntity(ContratoDTO dto) {
        if (dto == null) return null;

        Contrato c = new Contrato();
        c.setId(dto.getId());
        c.setNumero(dto.getNumero());
        c.setDescricao(dto.getDescricao());
        return c;
    }
}

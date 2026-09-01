package application.mappers;

import application.dto.CarroDTO;
import application.model.Carro;

/**
 * Mapper responsável por converter entre a entidade Carro e o CarroDTO.
 *
 * O objetivo deste mapper é:
 * - Evitar expor diretamente a entidade JPA ao cliente
 * - Controlar exatamente quais campos são enviados/recebidos pela API
 * - Facilitar a conversão entre DTO ↔ Entidade
 *
 * Este mapper trabalha apenas com os campos essenciais definidos no CarroDTO.
 * Relações complexas (motoristas, cartões, despesas) não são tratadas aqui.
 */
public class CarroMapper {

    /**
     * Converte uma entidade Carro para um CarroDTO.
     *
     * @param entity Entidade Carro vinda da base de dados
     * @return DTO contendo apenas os dados essenciais
     */
    public static CarroDTO toDTO(Carro entity) {
        if (entity == null) return null;

        return new CarroDTO(
                entity.getId(),
                entity.getMarca(),
                entity.getModelo(),
                entity.getMatricula()
        );
    }

    /**
     * Converte um CarroDTO para a entidade Carro.
     *
     * Apenas os campos básicos são preenchidos.
     * Relações e listas permanecem nulas, sendo tratadas no Service se necessário.
     *
     * @param dto Dados recebidos da API
     * @return Entidade Carro pronta para persistência
     */
    public static Carro toEntity(CarroDTO dto) {
        if (dto == null) return null;

        Carro c = new Carro();
        c.setId(dto.getId());
        c.setMarca(dto.getMarca());
        c.setModelo(dto.getModelo());
        c.setMatricula(dto.getMatricula());
        return c;
    }
}

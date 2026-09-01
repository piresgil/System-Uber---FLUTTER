package application.mappers;

import application.dto.CartaoDTO;
import application.model.Cartao;
import application.model.Carro;

/**
 * Mapper responsável por converter entre Cartao e CartaoDTO.
 *
 * O DTO utiliza apenas o ID do carro, enquanto a entidade utiliza
 * o objeto Carro completo. O carregamento do Carro real deve ser
 * feito no Service ou Controller.
 */
public class CartaoMapper {

    /**
     * Converte entidade → DTO.
     *
     * @param entity entidade Cartao
     * @return CartaoDTO com dados essenciais
     */
    public static CartaoDTO toDTO(Cartao entity) {
        if (entity == null) return null;

        return new CartaoDTO(
                entity.getId(),
                entity.getTipo(),
                entity.getNumero(),
                entity.getContrato(),
                entity.getNome(),
                entity.getCarro() != null ? entity.getCarro().getId() : null
        );
    }

    /**
     * Converte DTO → entidade.
     *
     * Observação:
     * - O Carro real NÃO é carregado aqui.
     * - Apenas criamos um objeto Carro com ID para manter a referência.
     * - O Service/Controller deve substituir pelo Carro real carregado do BD.
     *
     * @param dto CartaoDTO recebido da API
     * @return entidade Cartao pronta para persistência
     */
    public static Cartao toEntity(CartaoDTO dto) {
        if (dto == null) return null;

        Cartao c = new Cartao();
        c.setId(dto.getId());
        c.setTipo(dto.getTipo());
        c.setNumero(dto.getNumero());
        c.setContrato(dto.getContrato());
        c.setNome(dto.getNome());

        if (dto.getCarroId() != null) {
            Carro carro = new Carro();
            carro.setId(dto.getCarroId());
            c.setCarro(carro);
        }

        return c;
    }
}

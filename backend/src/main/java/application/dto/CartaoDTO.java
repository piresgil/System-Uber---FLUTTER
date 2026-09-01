package application.dto;

import application.model.TipoCartao;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) para a entidade Cartao.
 *
 * Este DTO expõe apenas os dados essenciais do cartão,
 * evitando enviar a entidade JPA completa para o cliente.
 * Relações são representadas apenas por IDs.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartaoDTO {

    /** Identificador único do cartão */
    private Long id;

    /** Tipo do cartão (COMBUSTIVEL, PORTAGEM, etc.) */
    private TipoCartao tipo;

    /** Número físico do cartão */
    private String numero;

    /** Número do contrato associado ao cartão */
    private String contrato;

    /** Nome associado ao cartão (ex: nome do condutor) */
    private String nome;

    /** ID do carro ao qual o cartão está associado */
    private Long carroId;
}

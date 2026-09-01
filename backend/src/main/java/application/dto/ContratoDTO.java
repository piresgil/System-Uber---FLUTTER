package application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para a entidade Contrato.
 * Usado para transferir dados entre API e cliente.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContratoDTO {

    private Long id;
    private String numero;
    private String descricao;
}

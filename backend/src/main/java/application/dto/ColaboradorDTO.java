package application.dto;

import application.model.TipoColaborador;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para a entidade Colaborador.
 *
 * Usado para transferir dados entre a API e o cliente,
 * evitando expor diretamente a entidade JPA.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ColaboradorDTO {

    /**
     * Identificador único do colaborador.
     */
    private Long id;

    /**
     * Nome completo do colaborador.
     */
    private String nome;

    /**
     * Email do colaborador.
     */
    private String email;

    /**
     * Telefone do colaborador.
     */
    private String telefone;

    /**
     * Tipo do colaborador (MOTORISTA, ADMINISTRATIVO, etc).
     */
    private TipoColaborador tipo;
}

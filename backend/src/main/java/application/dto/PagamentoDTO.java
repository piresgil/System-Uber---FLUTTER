package application.dto;

import application.model.Plataforma;
import application.model.TipoPagamento;
import lombok.*;

import java.time.LocalDate;

/**
 * DTO para a entidade Pagamento.
 * Relações complexas são representadas apenas por IDs.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagamentoDTO {

    private Long id;
    private Long colaboradorId;

    private Plataforma plataforma;
    private LocalDate data;
    private Double valor;
    private TipoPagamento tipoPagamento;

    private boolean ativo;
}



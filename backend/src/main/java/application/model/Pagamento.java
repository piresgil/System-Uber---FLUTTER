package application.model;

import jakarta.persistence.*;
import lombok.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.time.LocalDate;

/**
 * Entidade Pagamento
 * Representa um pagamento efetuado por um colaborador,
 * podendo estar associado a um cartão e a uma plataforma.
 */
@Entity
@Table(name = "tb_pagamento")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Pagamento {

    // Constructor simplificado
    public Pagamento(Colaborador colaborador,
                     Plataforma plataforma,
                     LocalDate data,
                     Double valor,
                     TipoPagamento tipoPagamento) {

        this.colaborador = colaborador;
        this.plataforma = plataforma;
        this.data = data;
        this.valor = valor;
        this.tipoPagamento = tipoPagamento;
        this.ativo = true;
    }

    /** Identificador único do pagamento */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Colaborador responsável pelo pagamento */
    @ManyToOne
    @JoinColumn(name = "colaborador_id", nullable = false)
    @NotNull(message = "O colaborador é obrigatório")
    private Colaborador colaborador;

    /** Plataforma onde o pagamento ENUN */
    @Enumerated(EnumType.STRING)
    private Plataforma plataforma;

    /** Data do pagamento */
    @Column(nullable = false)
    @NotNull(message = "A data é obrigatória")
    private LocalDate data;

    /** Valor do pagamento */
    @Column(nullable = false)
    @Positive(message = "O valor deve ser positivo")
    private Double valor;

    /** Tipo do pagamento (ABASTECIMENTO, PORTAGEM, OUTRO) */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull(message = "O tipo de pagamento é obrigatório")
    private TipoPagamento tipoPagamento;

    /** Soft delete */
    @Column(nullable = false)
    private boolean ativo = true;

    /** Controle de concorrência */
    @Version
    private Integer version;
}

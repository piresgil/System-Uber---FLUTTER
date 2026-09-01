package application.model;

import jakarta.persistence.*;
import lombok.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidade Cartao
 * Representa um cartão associado a um carro, podendo ser usado para despesas
 * como combustível, portagens, etc.
 */
@Entity
@Table(name = "tb_cartao")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Cartao {

    /** Identificador único do cartão */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Tipo do cartão (Combustível, Portagem, etc.) */
    @NotNull(message = "O tipo do cartão não pode estar vazio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 100)
    private TipoCartao tipo;

    /** Número único do cartão */
    @NotNull(message = "O número não pode ser nulo")
    @Column(nullable = false, length = 100, unique = true)
    private String numero;

    /** Número de contrato associado ao cartão */
    @NotBlank(message = "O número de contrato não pode estar vazio")
    @Column(nullable = false, length = 100)
    private String contrato;

    /** Nome associado ao cartão (ex: nome do condutor) */
    @NotBlank(message = "O nome não pode estar vazio")
    @Column(nullable = false, length = 100)
    private String nome;

    /**
     * Lista de despesas associadas ao cartão.
     * Cascade ALL → ao remover cartão, remove despesas.
     * orphanRemoval → despesas sem cartão são removidas.
     */
    @OneToMany(mappedBy = "cartao", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Despesa> despesas = new ArrayList<>();

    /**
     * Carro ao qual o cartão pertence.
     * Muitos cartões podem estar associados ao mesmo carro.
     */
    @ManyToOne
    @JoinColumn(name = "carro_id")
    private Carro carro;

    /** Soft delete: TRUE = ativo, FALSE = removido logicamente */
    @Column(nullable = false)
    private boolean ativo = true;

    /** Controle de concorrência otimista */
    @Version
    private Integer version;

 //  /** Construtor útil para criação manual */
 //  public Cartao(TipoCartao tipo, String numero, String contrato, String nome, Carro carro) {
 //      this.tipo = tipo;
 //      this.numero = numero;
 //      this.contrato = contrato;
 //      this.nome = nome;
 //      this.carro = carro;
 //  }
    public Cartao(Long id, TipoCartao tipo, String numero, String contrato, String nome, Carro carro) {
        this.id = id;
        this.tipo = tipo;
        this.numero = numero;
        this.contrato = contrato;
        this.nome = nome;
        this.carro = carro;
    }

    /** Adiciona uma despesa ao cartão */
    public void addDespesa(Despesa despesa) {
        despesas.add(despesa);
        despesa.setCartao(this);
    }

    /** Remove uma despesa do cartão */
    public void removeDespesa(Despesa despesa) {
        despesas.remove(despesa);
        despesa.setCartao(null);
    }

    @Override
    public String toString() {
        return "Cartao{" +
                "id=" + id +
                ", tipo=" + tipo +
                ", numero='" + numero + '\'' +
                ", contrato='" + contrato + '\'' +
                ", nome='" + nome + '\'' +
                ", carro=" + (carro != null ? carro.getMatricula() : "nenhum") +
                '}';
    }
}

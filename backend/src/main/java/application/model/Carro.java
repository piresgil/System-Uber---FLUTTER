package application.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Where;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Entidade Carro
 *
 * Representa um veículo no sistema, com suporte a:
 * - Soft delete (campo ativo)
 * - Filtro automático de registros ativos (@Where)
 * - Controle de concorrência otimista (@Version)
 * - Validações de campos obrigatórios
 * - Relações com Cartão, Colaborador e Despesa
 */
@Entity
@Table(name = "tb_carro")

/**
 * Aplica automaticamente um filtro em TODAS as consultas:
 * Apenas carros com ativo = true serão retornados.
 * Isto implementa soft delete de forma transparente.
 */
@Where(clause = "ativo = true")

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Carro {

    /**
     * Identificador único do carro.
     * Gerado automaticamente pelo banco de dados.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Marca do carro (ex: Toyota, BMW).
     * Campo obrigatório.
     */
    @NotBlank(message = "A marca do carro não pode estar vazia")
    @Column(nullable = false, length = 100)
    private String marca;

    /**
     * Modelo do carro (ex: Corolla, Série 3).
     * Campo obrigatório.
     */
    @NotBlank(message = "O modelo do carro não pode estar vazio")
    @Column(nullable = false, length = 100)
    private String modelo;

    /**
     * Matrícula do carro.
     * Deve ser única no sistema.
     */
    @NotBlank(message = "A matrícula do carro não pode estar vazia")
    @Column(nullable = false, length = 100, unique = true)
    private String matricula;

    /**
     * Indica se o carro é alugado (true) ou próprio (false).
     */
    @Column(nullable = false)
    private boolean alugado;

    /**
     * Quilometragem atual do carro.
     * Campo obrigatório.
     */
    @NotNull(message = "A quilometragem não pode ser nula")
    private Double kilometragem;

    /**
     * Indica se o carro está ativo no sistema.
     * Usado para soft delete.
     */
    @Column(nullable = false)
    private boolean ativo = true;

    /**
     * Controle de concorrência otimista.
     * Evita conflitos em atualizações simultâneas.
     */
    @Version
    private Integer version;

    /**
     * Relação 1:N com Cartão.
     * Um carro pode ter vários cartões associados.
     * Cascade ALL → operações no carro afetam os cartões.
     * orphanRemoval → cartões órfãos são removidos automaticamente.
     */
    @OneToMany(mappedBy = "carro", fetch = FetchType.EAGER,
            cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Cartao> cartoes = new ArrayList<>();

    /**
     * Relação N:N com Colaborador.
     * Um carro pode ter vários motoristas.
     * Um motorista pode conduzir vários carros.
     */
    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(
            name = "carro_colaborador",
            joinColumns = @JoinColumn(name = "carro_id"),
            inverseJoinColumns = @JoinColumn(name = "colaborador_id")
    )
    private List<Colaborador> motoristas = new ArrayList<>();

    /**
     * Relação 1:N com Despesa.
     * Um carro pode ter várias despesas associadas.
     */
    @OneToMany(mappedBy = "carro", fetch = FetchType.EAGER,
            cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Despesa> despesas = new ArrayList<>();

    /**
     * Igualdade baseada apenas no ID.
     * Essencial para entidades JPA.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Carro carro)) return false;
        return Objects.equals(id, carro.id);
    }

    /**
     * Hash baseado no ID.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Representação textual útil para logs e debugging.
     */
    @Override
    public String toString() {
        return "Carro{" +
                "id=" + id +
                ", marca='" + marca + '\'' +
                ", modelo='" + modelo + '\'' +
                ", matricula='" + matricula + '\'' +
                ", kilometragem=" + kilometragem +
                ", alugado=" + alugado +
                ", ativo=" + ativo +
                '}';
    }
}

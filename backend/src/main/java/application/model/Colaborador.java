package application.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.Objects;

/**
 * Entidade Colaborador
 *
 * Representa um colaborador do sistema, podendo assumir diferentes funções
 * através do campo tipo (ex: MOTORISTA, ADMINISTRATIVO, etc).
 *
 * Inclui:
 * - Validações de campos obrigatórios
 * - Relação com Pagamento
 * - Suporte a enum TipoColaborador
 */
@Entity
@Table(name = "tb_colaborador")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Colaborador {

    /**
     * Identificador único do colaborador.
     * Gerado automaticamente pelo banco de dados.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nome completo do colaborador.
     * Campo obrigatório.
     */
    @NotBlank(message = "O nome não pode estar em branco.")
    @Column(nullable = false, length = 100)
    private String nome;

    /**
     * Email do colaborador.
     * Deve ser único e válido.
     */
    @Email(message = "O email é inválido.")
    @NotBlank(message = "O email não pode estar em branco.")
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    /**
     * Telefone do colaborador.
     * Não é exposto no JSON por motivos de privacidade.
     */
    @JsonIgnore
    @NotBlank(message = "O telefone não pode estar em branco.")
    @Column(nullable = false, length = 20)
    private String telefone;

    /**
     * Tipo do colaborador.
     * Define se é MOTORISTA, ADMINISTRATIVO, GESTOR, etc.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoColaborador tipo;

    /**
     * Lista de pagamentos associados ao colaborador.
     * A remoção do colaborador remove também os pagamentos.
     */
    @OneToMany(mappedBy = "colaborador", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Pagamento> pagamentos;

    /**
     * Igualdade baseada apenas no ID.
     * Essencial para entidades JPA.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Colaborador that)) return false;
        return Objects.equals(id, that.id);
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
        return "Colaborador{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", email='" + email + '\'' +
                ", telefone='" + telefone + '\'' +
                ", tipo=" + tipo +
                '}';
    }
}

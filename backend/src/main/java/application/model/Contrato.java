package application.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.util.Objects;

/**
 * Entidade Contrato
 *
 * Representa um contrato registado no sistema.
 * Inclui validações, mapeamento JPA e suporte a operações CRUD.
 */
@Entity
@Table(name = "tb_contrato")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Contrato {

    /**
     * Identificador único do contrato.
     * Gerado automaticamente pelo banco de dados.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Número do contrato.
     * Campo obrigatório e não pode estar vazio.
     */
    @NotBlank(message = "O número do contrato não pode estar em branco.")
    @Column(nullable = false, length = 50)
    private String numero;

    /**
     * Descrição do contrato.
     * Campo opcional, mas útil para identificar o propósito do contrato.
     */
    @Column(length = 255)
    private String descricao;

    /**
     * Igualdade baseada apenas no ID.
     * Essencial para entidades JPA.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Contrato that)) return false;
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
        return "Contrato{" +
                "id=" + id +
                ", numero='" + numero + '\'' +
                ", descricao='" + descricao + '\'' +
                '}';
    }
}

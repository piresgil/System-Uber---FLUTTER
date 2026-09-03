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

@Entity
@Table(name = "tb_carro")
@Where(clause = "ativo = true")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Carro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "A marca do carro não pode estar vazia")
    @Column(nullable = false, length = 100)
    private String marca;

    @NotBlank(message = "O modelo do carro não pode estar vazio")
    @Column(nullable = false, length = 100)
    private String modelo;

    @NotBlank(message = "A matrícula do carro não pode estar vazia")
    @Column(nullable = false, length = 100, unique = true)
    private String matricula;

    @Column(nullable = false)
    private boolean alugado;

    @NotNull(message = "A quilometragem não pode ser nula")
    private Double kilometragem;

    @Column(nullable = false)
    private boolean ativo = true;

    // Campos específicos para as 3 fotos pedidas
    @Column(length = 500)
    private String documentoUrl;

    @Column(length = 500)
    private String seguroUrl;

    @Column(length = 500)
    private String inspecaoUrl;

    @Version
    private Integer version;

    @OneToMany(mappedBy = "carro", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Cartao> cartoes = new ArrayList<>();

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(
            name = "carro_colaborador",
            joinColumns = @JoinColumn(name = "carro_id"),
            inverseJoinColumns = @JoinColumn(name = "colaborador_id")
    )
    private List<Colaborador> motoristas = new ArrayList<>();

    @OneToMany(mappedBy = "carro", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Despesa> despesas = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Carro carro)) return false;
        return Objects.equals(id, carro.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
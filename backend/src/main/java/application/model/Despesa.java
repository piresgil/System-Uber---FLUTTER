/**
 * @author Daniel Gil
 */
package application.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.time.Instant;
import java.util.Objects;

/**
 * Classe base que representa uma despesa associada a um carro e um motorista.
 * Esta classe é abstrata e será estendida por outras classes de tipos específicos de despesa.
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED) // Define a estratégia de herança para a tabela no banco de dados
@DiscriminatorColumn(name = "tipo", discriminatorType = DiscriminatorType.STRING)
// Coluna para diferenciar os tipos de despesa
@Table(name = "tb_despesa") // Nome da tabela no banco de dados (apenas na classe base)
public class Despesa {

    // Identificador único da despesa
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // A geração de valores para o ID é feita automaticamente pelo banco
    private Long id;

    @ManyToOne(cascade = CascadeType.ALL) // ou CascadeType.ALL
    @JoinColumn(name = "cartao_id", nullable = false)
    @NotNull(message = "O cartao não pode estar vazio")
    @Valid // Validação em cascata para o objeto Cartao
    private Cartao cartao;

    // Nome da despesa (Ex: "Abastecimento de combustível")
    @Column(nullable = false, length = 100) // Nome não pode ser nulo e tem limite de 100 caracteres
    private String nome;

    // Descrição detalhada da despesa
    @Column(nullable = false, length = 100) // Descrição não pode ser nula e tem limite de 100 caracteres
    private String descricao;

    // Relacionamento com o carro associado à despesa
    @ManyToOne(cascade = CascadeType.MERGE) // Reatacha automaticamente o carro
    @JoinColumn(name = "carro_id", nullable = false) // Garante que cada despesa esteja associada a um carro
    @NotNull(message = "O carro não pode estar vazio")
    @Valid // Validação em cascata para o objeto Carro
    private Carro carro;

    // Relacionamento com o motorista responsável pela despesa
    @ManyToOne
    @JoinColumn(name = "colaborador_id", nullable = true)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Colaborador motorista;

    // Data e hora em que a despesa ocorreu
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "GMT")
    @Column(nullable = false)
    @NotNull(message = "A data não pode estar vazia")
    private Instant data;

    // Valor da despesa
    @Column(nullable = false)
    @NotNull(message = "O valor não pode estar vazio")
    @Positive(message = "O valor deve ser positivo")
    private Double valor;

    @Column(length = 100)
    @PositiveOrZero(message = "A quantidade deve ser positiva ou zero")
    private Integer quantidade;

    @Column(length = 100)
    @PositiveOrZero(message = "A unidade deve ser positiva ou zero")
    private Double unidade;

    // Construtor padrão necessário para a persistência no JPA
    public Despesa() {
    }

    // Construtor sem quantidade e unidade
    public Despesa(Long id, Cartao cartao, String nome, String descricao, Carro carro, Colaborador motorista, Instant data, Double valor) {
        this(id, cartao, nome, descricao, carro, motorista, data, valor, null, null); // quantidade e unidade são null por padrão
    }

    // Construtor com parâmetros para facilitar a criação de objetos de despesa
    public Despesa(Long id, Cartao cartao, String nome, String descricao, Carro carro, Colaborador motorista, Instant data, Double valor, Integer quantidade, Double unidade) {
        this.id = id;
        this.cartao = cartao;
        this.nome = nome;
        this.descricao = descricao;
        this.carro = carro;
        this.motorista = motorista;
        this.data = data;
        this.valor = valor;
        this.quantidade = quantidade;
        this.unidade = unidade;
    }

    // Getters e Setters para acessar e modificar os atributos

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Cartao getCartao() {
        return cartao;
    }

    public void setCartao(Cartao cartao) {
        this.cartao = cartao;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Carro getCarro() {
        return carro;
    }

    public void setCarro(Carro carro) {
        this.carro = carro;
    }

    public Colaborador getMotorista() {
        return motorista;
    }

    public void setMotorista(Colaborador motorista) {
        this.motorista = motorista;
    }

    public Instant getData() {
        return data;
    }

    public void setData(Instant data) {
        this.data = data;
    }

    public Double getValor() {
        return valor;
    }

    public void setValor(Double valor) {
        this.valor = valor;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public Double getUnidade() {
        return unidade;
    }

    public void setUnidade(Double unidade) {
        this.unidade = unidade;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Despesa despesa = (Despesa) o;
        return Objects.equals(id, despesa.id); // Compara as despesas com base no ID

    }

    @Override
    public String toString() {
        return "Despesa{" +
                "id=" + id +
                ", cartao=" + (cartao != null ? cartao.getId() : "null") +
                ", nome='" + nome + '\'' +
                ", descricao='" + descricao + '\'' +
                ", carro=" + (carro != null ? carro.getId() : "null") +
                ", motorista=" + (motorista != null ? motorista.getId() : "null") +
                ", data=" + data +
                ", valor=" + valor +
                '}';
    }
}
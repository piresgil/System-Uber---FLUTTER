/**
 * @author Daniel Gil
 */
package application.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.util.Objects;

/**
 * Classe que representa a entidade "User" no banco de dados.
 * Esta entidade será mapeada para a tabela "tb_user".
 */
@Entity // Indica que esta classe é uma entidade JPA
@Table(name = "tb_user") // Define o nome da tabela no banco de dados
public class User {

    @Id // Define a chave primária da tabela
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Geração automática do ID pelo banco de dados
    private Long id;

    @NotBlank(message = "O nome é obrigatório") // Validação: impede que o nome seja nulo ou vazio
    @Column(nullable = false, length = 100) // Define que o campo "nome" não pode ser nulo e limita seu tamanho
    private String nome;

    @Email(message = "O email é inválido.")
    @NotBlank(message = "O email é obrigatório") // Validação para evitar valores nulos ou em branco
    @Column(nullable = false, unique = true, length = 150)
    // Define que o email deve ser único e com tamanho máximo de 150 caracteres
    private String email;

    @NotBlank(message = "A password é obrigatória") // Garante que a senha não seja vazia
    @JsonIgnore // Evita que a senha seja serializada (por questões de segurança)
    @Column(nullable = false) // Garante que o campo "password" não pode ser nulo
    private String password;

    @Transient // Não será salvo no banco
    private int passwordLength;

    /**
     * Construtor vazio necessário para JPA.
     */
    public User() {
    }

    /**
     * Construtor com parâmetros para facilitar a criação de objetos da classe.
     *
     * @param id       Identificador único do usuário
     * @param nome     Nome do usuário
     * @param email    Email do usuário
     * @param password Senha do usuário (criptografada)
     */
    public User(Long id, String nome, String email, String password) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.password = password;
        this.passwordLength = password.length(); // Armazena o tamanho da senha original
    }

    // Métodos Getters e Setters para acesso e modificação dos atributos

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getPasswordLength() {
        return passwordLength;
    }

    public void setPasswordLength(int passwordLength) {
        this.passwordLength = passwordLength;
    }

    /**
     * Método equals para comparar objetos da classe "User".
     * Dois usuários são considerados iguais se tiverem o mesmo ID.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true; // Verifica se é o mesmo objeto na memória
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    /**
     * Método hashCode para gerar um código único para cada usuário.
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    /**
     * Método toString para representar o usuário como string.
     * Nota: Por segurança, a password não deveria ser incluída aqui.
     */
    @Override
    public String toString() {
        return "Utilizador: " +
                "Nome: '" + nome + '\'' +
                ", Email: '" + email + '\'';
        // + ", Password: '" + password + '\''; // ⚠️ RISCO DE SEGURANÇA! Não expor a senha em logs
    }
}

/**
 * @author Daniel Gil
 */
package application.repositories;

import application.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Interface que representa o repositório de usuários na aplicação.
 * <p>
 * O `UserRepository` estende `JpaRepository<User, Long>`, o que significa que ele herda
 * vários métodos úteis para manipulação de dados, como salvar, deletar, atualizar e buscar usuários.
 * Estes métodos são automaticamente implementados pelo Spring Data JPA.
 * </p>
 * <p>
 * A anotação `@Repository` é utilizada para indicar que esta interface é um componente do Spring
 * responsável pela interação com a base de dados. Embora seja uma boa prática incluir, essa anotação
 * é opcional, pois o `JpaRepository` já a inclui internamente.
 * </p>
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Método responsável por verificar se já existe um usuário com o e-mail fornecido.
     *
     * @param email O e-mail a ser verificado.
     * @return true se já existir um usuário com este e-mail, false caso contrário.
     */
    boolean existsByEmail(String email); // Método que consulta a base de dados para verificar se o e-mail já existe.

    /**
     * Verifica se já existe um usuário com o e-mail fornecido, ignorando o usuário com o ID especificado.
     * Usado principalmente para evitar que o próprio usuário em edição seja considerado como duplicado.
     *
     * @param email O e-mail a ser verificado.
     * @param id    O ID do usuário a ser ignorado na verificação.
     * @return true se já existir um usuário com este e-mail e que não seja o usuário com o ID fornecido, false caso contrário.
     */
    Boolean existsByEmailAndIdNot(String email, Long id);
}

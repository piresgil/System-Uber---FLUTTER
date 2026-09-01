/**
 * @author Daniel Gil
 */
package application.repositories;

import application.model.Cartao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Interface User repository
 * extends JPA Repository, Function<>
 * que abstrai o acesso a dados.
 * Ele serve como uma camada intermediária entre a lógica de negócio da sua aplicação e o banco de dados.
 * JpaRepository ja tem @Repository
 */
@Repository
public interface CartaoRepository extends JpaRepository<Cartao, Long> {

}

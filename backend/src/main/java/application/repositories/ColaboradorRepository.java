/**
 * Repositório para a entidade Colaborador, responsável pelo acesso ao banco de dados.
 *
 * @author Daniel Gil
 */
package application.repositories;

import application.model.Colaborador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Interface que representa o repositório da entidade Colaborador.
 *
 * @Repository Indica que esta interface é um componente Spring que interage com o banco de dados.
 * No entanto, não é estritamente necessário, pois JpaRepository já inclui essa anotação internamente.
 *
 * JpaRepository<Colaborador, Long> fornece métodos CRUD padrão, como:
 * - save() -> Salva um novo colaborador ou atualiza um existente.
 * - findById() -> Busca um colaborador pelo ID.
 * - findAll() -> Retorna todos os colaboradores cadastrados.
 * - deleteById() -> Exclui um colaborador pelo ID.
 * - Entre outros métodos prontos para uso.
 */
@Repository
public interface ColaboradorRepository extends JpaRepository<Colaborador, Long> {
    Boolean existsByEmail(String email);

    Boolean existsByEmailAndIdNot(String email, Long id);
}

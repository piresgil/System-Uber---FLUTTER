/**
 * @author Daniel Gil
 */
package application.repositories;

import application.model.Pagamento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {
}

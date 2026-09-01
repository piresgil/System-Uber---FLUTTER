package application.repositories;

import application.model.Contrato;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositório JPA para a entidade Contrato.
 * Fornece operações CRUD automáticas.
 */
@Repository
public interface ContratoRepository extends JpaRepository<Contrato, Long> {
}

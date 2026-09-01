package application.services;

import application.model.Cartao;
import application.model.Colaborador;
import application.model.Pagamento;
import application.repositories.PagamentoRepository;
import application.services.exceptions.DatabaseException;
import application.services.exceptions.ResourceNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Serviço responsável pela lógica de negócio da entidade Pagamento.
 * Carrega entidades relacionadas, valida existência e trata exceções.
 */
@Service
public class PagamentoService {

    @Autowired
    private PagamentoRepository repository;

    @Autowired
    private ColaboradorService colaboradorService;

    @Autowired
    private CartaoService cartaoService;

    /**
     * Retorna todos os pagamentos.
     */
    public List<Pagamento> findAll() {
        return repository.findAll();
    }

    /**
     * Busca um pagamento pelo ID.
     */
    public Pagamento findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id));
    }

    /**
     * Insere um novo pagamento.
     * Carrega Colaborador e Cartão reais antes de salvar.
     */
    public Pagamento insert(Pagamento obj) {

        carregarEntidadesRelacionadas(obj);

        return repository.save(obj);
    }

    /**
     * Remove um pagamento pelo ID.
     */
    public void delete(Long id) {
        try {
            if (!repository.existsById(id)) {
                throw new ResourceNotFoundException(id);
            }
            repository.deleteById(id);

        } catch (ResourceNotFoundException e) {
            throw e;

        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Não foi possível eliminar o pagamento. Verifique dependências.");
        }
    }

    /**
     * Atualiza um pagamento existente.
     */
    public Pagamento update(Long id, Pagamento obj) {
        try {
            Pagamento entity = repository.getReferenceById(id);

            updateData(entity, obj);

            return repository.save(entity);

        } catch (EntityNotFoundException e) {
            throw new ResourceNotFoundException(id);
        }
    }

    /**
     * Atualiza apenas os campos permitidos.
     */
    private void updateData(Pagamento entity, Pagamento obj) {

        if (obj.getColaborador() != null && obj.getColaborador().getId() != null) {
            Colaborador col = colaboradorService.findById(obj.getColaborador().getId());
            entity.setColaborador(col);
        }

        entity.setPlataforma(obj.getPlataforma());
        entity.setData(obj.getData());
        entity.setValor(obj.getValor());
        entity.setTipoPagamento(obj.getTipoPagamento());
        entity.setAtivo(obj.isAtivo());
    }


    /**
     * Carrega Colaborador e Cartão reais a partir dos IDs.
     */
    private void carregarEntidadesRelacionadas(Pagamento obj) {

        // Carregar colaborador real
        if (obj.getColaborador() != null && obj.getColaborador().getId() != null) {
            Colaborador col = colaboradorService.findById(obj.getColaborador().getId());
            obj.setColaborador(col);
        }

    }

    /**
     * Soft delete — marca o pagamento como inativo.
     */
    public void softDelete(Long id) {
        Pagamento p = findById(id);
        p.setAtivo(false);
        repository.save(p);
    }
}

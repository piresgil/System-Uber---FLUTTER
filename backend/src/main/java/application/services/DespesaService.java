package application.services;

import application.model.Cartao;
import application.model.Despesa;
import application.repositories.CartaoRepository;
import application.repositories.DespesaRepository;
import application.services.exceptions.DatabaseException;
import application.services.exceptions.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DespesaService {

    // Injeção de Dependências para o repositório de despesas.
    @Autowired
    private DespesaRepository repository;
    @Autowired
    private CartaoRepository cartaoRepository;

    // Logger para registrar eventos e facilitar o rastreamento de erros.
    private static final Logger logger = LoggerFactory.getLogger(DespesaService.class);
    private final Map<Long, Despesa> cache = new HashMap<>();

    public void limparCache() {
        cache.clear();
    }

    /**
     * Construtor com injeção de dependências.
     *
     * @param repository Repositório de despesas.
     */
    @Autowired
    public DespesaService(DespesaRepository repository) {
        this.repository = repository;
    }

    /**
     * Retorna todas as despesas cadastradas.
     *
     * @return Lista de todas as despesas.
     */
    @Transactional(readOnly = true)
    public List<Despesa> findAll() {
        if (!cache.isEmpty()) {
            return new ArrayList<>(cache.values()); // Retorna os valores já no cache
        }

        List<Despesa> despesas = repository.findAll();
        despesas.forEach(despesa -> cache.put(despesa.getId(), despesa)); // Armazena no cache
        return despesas;
    }

    /**
     * Busca uma despesa pelo ID.
     * Utiliza a classe Optional para lidar com a possibilidade de um valor nulo.
     * Se a despesa não for encontrada, lança uma ResourceNotFoundException.
     *
     * @param id ID da despesa.
     * @return A despesa encontrada.
     */
    @Transactional(readOnly = true)
    public Despesa findById(Long id) {
        // Se a despesa não for encontrada, loga o erro e lança uma exceção.
        return repository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Despesa com ID {} não encontrada!", id);
                    return new ResourceNotFoundException(id);
                });
    }

    /**
     * Insere uma nova despesa no banco de dados.
     * A operação é realizada dentro de uma transação.
     *
     * @param obj Objeto Despesa a ser inserido.
     * @return A despesa inserida.
     */
    @Transactional
    public Despesa insert(@Valid Despesa obj) {
        try {
            // Verifica se o Cartao está detached e recarrega
            if (obj.getCartao() != null && obj.getCartao().getId() != null) {
                Cartao cartao = cartaoRepository.findById(obj.getCartao().getId())
                        .orElseThrow(() -> new DatabaseException("Cartão não encontrado com o ID: " + obj.getCartao().getId()));
                obj.setCartao(cartao); // Associa o Cartao recarregado à Despesa
            }

            Despesa savedDespesa = repository.save(obj); // Salva no banco de dados
            // Despesa savedDespesa = repository.save(obj); // Salva no banco de dados
            cache.put(savedDespesa.getId(), savedDespesa); // Atualiza o cache
            logger.info("Despesa com ID {} cadastrado com sucesso.", savedDespesa.getId());
            return savedDespesa;
        } catch (DataIntegrityViolationException e) {
            logger.error("Erro ao salvar Despesa: {}", e.getMessage());
            throw new DatabaseException("Erro ao salvar o Despesa: violação de integridade.");
        }
    }

    /**
     * Atualiza uma despesa existente.
     * Se a despesa não for encontrada, lança uma ResourceNotFoundException.
     *
     * @param id  ID da despesa a ser atualizada.
     * @param obj Objeto Despesa com os novos dados.
     * @return A despesa atualizada.
     */
    @Transactional
    public Despesa update(Long id, @Valid Despesa obj) {
        try {
            return repository.findById(id)
                    .map(entity -> {
                        // Atualiza os campos
                        entity.setCartao(obj.getCartao());
                        entity.setNome(obj.getNome());
                        entity.setDescricao(obj.getDescricao());
                        entity.setCarro(obj.getCarro());
                        entity.setMotorista(obj.getMotorista());
                        entity.setData(obj.getData());
                        entity.setValor(obj.getValor());

                        // Salva e retorna o carro atualizado
                        Despesa updatedDespesa = repository.save(entity);
                        logger.info("Carro com ID {} atualizado com sucesso.", id);
                        return updatedDespesa;
                    })
                    .orElseThrow(() -> new ResourceNotFoundException(id));
        } catch (DataIntegrityViolationException e) {
            logger.error("Erro ao atualizar carro ID {}: {}", id, e.getMessage());
            throw new DatabaseException("Erro ao atualizar o carro: violação de integridade.");
        }
    }

    /**
     * Deleta uma despesa com base no ID.
     * Se a despesa não existir, lança uma ResourceNotFoundException.
     * Se houver um problema de integridade de dados, lança uma DatabaseException.
     *
     * @param id ID da despesa a ser deletada.
     */
    @Transactional
    public void delete(Long id) {
        try {
            repository.deleteById(id);
            cache.remove(id); // Remove do cache
        } catch (ResourceNotFoundException e) {
            logger.error("Erro ao deletar despesa com ID {}: não encontrada.", id);
            throw new ResourceNotFoundException(id);
        } catch (DataIntegrityViolationException e) {
            // Caso haja violação de integridade no banco de dados (ex: restrições de chave estrangeira).
            logger.error("Erro ao deletar despesa com ID {}: violação de integridade.", id);
            throw new DatabaseException(e.getMessage());
        }
    }
}

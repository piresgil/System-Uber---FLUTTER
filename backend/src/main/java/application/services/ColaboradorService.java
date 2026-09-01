package application.services;

import application.model.Colaborador;
import application.repositories.ColaboradorRepository;
import application.services.exceptions.DatabaseException;
import application.services.exceptions.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Valid;
import java.util.*;

/**
 * Serviço responsável por operações de CRUD e cache da entidade Colaborador.
 * Esta classe é gerida pelo Spring através da anotação @Service.
 */
@Service
public class ColaboradorService {

    private static final Logger logger = LoggerFactory.getLogger(ColaboradorService.class);

    /**
     * Cache simples em memória para reduzir acessos ao banco.
     * NÃO deve ser static — cada instância do serviço deve gerir o seu próprio cache.
     */
    private final Map<Long, Colaborador> cache = new HashMap<>();

    /**
     * Repositório injetado pelo Spring.
     * NÃO pode ser static — Spring não injeta campos static.
     */
    @Autowired
    private ColaboradorRepository repository;

    /**
     * Limpa o cache manualmente.
     */
    public void limparCache() {
        cache.clear();
    }

    /**
     * Retorna todos os colaboradores.
     * Usa cache para melhorar performance.
     */
    @Transactional(readOnly = true)
    public List<Colaborador> findAll() {

        // Se o cache já tem dados, devolve-os
        if (!cache.isEmpty()) {
            return new ArrayList<>(cache.values());
        }

        // Caso contrário, carrega do banco
        List<Colaborador> colaboradores = repository.findAll();

        // Preenche o cache
        colaboradores.forEach(c -> cache.put(c.getId(), c));

        return colaboradores;
    }

    /**
     * Busca um colaborador pelo ID.
     */
    @Transactional(readOnly = true)
    public Colaborador findById(Long id) {
        return repository.findById(id).orElseThrow(() -> {
            logger.error("Colaborador com ID {} não encontrado!", id);
            return new ResourceNotFoundException(id);
        });
    }

    /**
     * Insere um novo colaborador.
     */
    @Transactional
    public Colaborador insert(@Valid Colaborador obj) {
        try {
            Colaborador saved = repository.save(obj);

            // Atualiza cache
            cache.put(saved.getId(), saved);

            logger.info("Colaborador com ID {} cadastrado com sucesso.", saved.getId());
            return saved;

        } catch (DataIntegrityViolationException e) {
            logger.error("Erro ao salvar Colaborador: {}", e.getMessage());
            throw new DatabaseException("Erro ao salvar o Colaborador: violação de integridade.");
        }
    }

    /**
     * Atualiza um colaborador existente.
     */
    @Transactional
    public Colaborador update(Long id, @Valid Colaborador obj) {
        try {
            return repository.findById(id)
                    .map(entity -> {

                        entity.setNome(obj.getNome());
                        entity.setEmail(obj.getEmail());
                        entity.setTelefone(obj.getTelefone());

                        Colaborador updated = repository.save(entity);

                        // Atualiza cache
                        cache.put(updated.getId(), updated);

                        logger.info("Colaborador com ID {} atualizado com sucesso.", id);
                        return updated;
                    })
                    .orElseThrow(() -> new ResourceNotFoundException(id));

        } catch (DataIntegrityViolationException e) {
            logger.error("Erro ao atualizar Colaborador ID {}: {}", id, e.getMessage());
            throw new DatabaseException("Erro ao atualizar o Colaborador: violação de integridade.");
        }
    }

    /**
     * Exclui um colaborador pelo ID.
     */
    @Transactional
    public void delete(Long id) {
        try {
            repository.deleteById(id);

            // Remove do cache
            cache.remove(id);

            logger.info("Colaborador com ID {} deletado com sucesso.", id);

        } catch (EmptyResultDataAccessException e) {
            logger.warn("Tentativa de deletar Colaborador inexistente: ID {}", id);
            throw new ResourceNotFoundException(id);

        } catch (DataIntegrityViolationException e) {
            logger.error("Erro ao excluir Colaborador ID {}: {}", id, e.getMessage());
            throw new DatabaseException("Não foi possível eliminar o Colaborador. Verifique dependências.");
        }
    }

    /**
     * Verifica duplicação de email.
     */
    public Boolean existsByEmail(String email) {
        return repository.existsByEmail(email);
    }

    /**
     * Verifica duplicação de email em edição.
     */
    public Boolean existsByEmailAndIdNot(String email, Long id) {
        return repository.existsByEmailAndIdNot(email, id);
    }
}

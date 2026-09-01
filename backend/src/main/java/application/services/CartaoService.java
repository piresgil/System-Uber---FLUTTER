package application.services;

import application.model.Cartao;
import application.repositories.CartaoRepository;
import application.services.exceptions.DatabaseException;
import application.services.exceptions.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Valid;
import java.util.*;

/**
 * Serviço responsável pela gestão dos cartões.
 * Contém operações de consulta, criação, atualização e remoção.
 *
 * Implementa também um cache simples em memória para melhorar performance
 * em operações de leitura.
 */
@Service
@Slf4j
public class CartaoService {

    private final CartaoRepository repository;

    /** Cache simples para armazenar cartões já consultados */
    private final Map<Long, Cartao> cache = new HashMap<>();

    @Autowired
    public CartaoService(CartaoRepository repository) {
        this.repository = repository;
    }

    /** Limpa o cache manualmente */
    public void limparCache() {
        cache.clear();
    }

    /**
     * Retorna todos os cartões cadastrados.
     * Utiliza cache para evitar consultas repetidas ao banco.
     */
    @Transactional(readOnly = true)
    public List<Cartao> findAll() {
        if (!cache.isEmpty()) {
            return new ArrayList<>(cache.values());
        }

        List<Cartao> cartoes = repository.findAll();
        cartoes.forEach(c -> cache.put(c.getId(), c));

        return cartoes;
    }

    /**
     * Busca um cartão pelo ID.
     *
     * @param id identificador do cartão
     * @return cartão encontrado
     * @throws ResourceNotFoundException se não existir
     */
    @Transactional(readOnly = true)
    public Cartao findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> {
                    log.error("Cartão com ID {} não encontrado!", id);
                    return new ResourceNotFoundException(id);
                });
    }

    /**
     * Regista um novo cartão.
     *
     * @param obj entidade Cartao validada
     * @return cartão salvo
     */
    @Transactional
    public Cartao insert(@Valid Cartao obj) {
        try {
            Cartao saved = repository.save(obj);
            cache.put(saved.getId(), saved);

            log.info("Cartão com ID {} registado com sucesso.", saved.getId());
            return saved;

        } catch (DataIntegrityViolationException e) {
            log.error("Erro ao salvar cartão: {}", e.getMessage());
            throw new DatabaseException("Erro ao salvar o cartão: violação de integridade.");
        }
    }

    /**
     * Atualiza um cartão existente.
     *
     * @param id  identificador do cartão
     * @param obj dados atualizados
     * @return cartão atualizado
     */
    @Transactional
    public Cartao update(Long id, @Valid Cartao obj) {
        try {
            return repository.findById(id)
                    .map(entity -> {

                        entity.setTipo(obj.getTipo());
                        entity.setNumero(obj.getNumero());
                        entity.setNome(obj.getNome());
                        entity.setContrato(obj.getContrato());
                        entity.setCarro(obj.getCarro());

                        Cartao updated = repository.save(entity);
                        cache.put(updated.getId(), updated);

                        log.info("Cartão com ID {} atualizado com sucesso.", id);
                        return updated;

                    })
                    .orElseThrow(() -> new ResourceNotFoundException(id));

        } catch (DataIntegrityViolationException e) {
            log.error("Erro ao atualizar cartão ID {}: {}", id, e.getMessage());
            throw new DatabaseException("Erro ao atualizar o cartão: violação de integridade.");
        }
    }

    /**
     * Remove um cartão pelo ID.
     *
     * @param id identificador do cartão
     */
    @Transactional
    public void delete(Long id) {
        try {
            repository.deleteById(id);
            cache.remove(id);

            log.info("Cartão com ID {} eliminado com sucesso.", id);

        } catch (EmptyResultDataAccessException e) {
            log.warn("Tentativa de eliminar cartão inexistente: ID {}", id);
            throw new ResourceNotFoundException(id);

        } catch (DataIntegrityViolationException e) {
            log.error("Erro ao eliminar cartão ID {}: {}", id, e.getMessage());
            throw new DatabaseException("Não foi possível eliminar o cartão. Verifique dependências.");
        }
    }

    /**
     * Soft delete — marca o cartão como inativo.
     *
     * @param id identificador do cartão
     */
    @Transactional
    public void softDelete(Long id) {
        Cartao cartao = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id));

        cartao.setAtivo(false);
        repository.save(cartao);

        log.info("Cartão com ID {} marcado como inativo.", id);
    }
}

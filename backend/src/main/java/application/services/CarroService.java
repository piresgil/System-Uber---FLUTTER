package application.services;

import application.model.Carro;
import application.model.Cartao;
import application.model.Despesa;
import application.repositories.CarroRepository;
import application.services.exceptions.DatabaseException;
import application.services.exceptions.ResourceNotFoundException;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Valid;
import java.util.*;

/**
 * Serviço responsável pela gestão de Carros.
 *
 * Funcionalidades:
 * - Cache interno para melhorar performance
 * - Busca, inserção, atualização e remoção
 * - Soft delete com reatribuição de dependências
 * - Tratamento de exceções com logs detalhados
 * - Suporte a concorrência otimista
 */
@Service
@RequiredArgsConstructor // Injeta dependências automaticamente via construtor
public class CarroService {

    /**
     * Repositório JPA responsável pelo acesso ao banco de dados.
     * Marcado como final para uso com @RequiredArgsConstructor.
     */
    private final CarroRepository repository;

    /**
     * Logger para auditoria e debugging.
     */
    private static final Logger logger = LoggerFactory.getLogger(CarroService.class);

    /**
     * Cache simples em memória para reduzir acessos ao banco.
     * Mapeia ID → Carro.
     */
    private final Map<Long, Carro> cache = new HashMap<>();

    /**
     * Limpa o cache manualmente.
     * Útil após operações de escrita.
     */
    public void limparCache() {
        cache.clear();
    }

    /**
     * Retorna todos os carros ativos.
     *
     * - Usa cache para evitar consultas repetidas.
     * - @Transactional(readOnly = true) evita locks e melhora performance.
     */
    @Transactional(readOnly = true)
    public List<Carro> findAll() {

        // Se já temos dados no cache, devolvemos imediatamente
        if (!cache.isEmpty()) {
            return new ArrayList<>(cache.values());
        }

        // Caso contrário, buscamos no banco
        List<Carro> carros = repository.findAll();

        // Armazenamos no cache
        carros.forEach(carro -> cache.put(carro.getId(), carro));

        return carros;
    }

    /**
     * Busca um carro pelo ID.
     *
     * @param id ID do carro
     * @return Carro encontrado
     * @throws ResourceNotFoundException se não existir
     */
    @Transactional(readOnly = true)
    public Carro findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Carro com ID {} não encontrado!", id);
                    return new ResourceNotFoundException(id);
                });
    }

    /**
     * Insere um novo carro no sistema.
     *
     * - Validações são aplicadas automaticamente via @Valid.
     * - Atualiza o cache após salvar.
     */
    @Transactional
    public Carro insert(@Valid Carro obj) {
        try {
            Carro savedCarro = repository.save(obj);
            cache.put(savedCarro.getId(), savedCarro);

            logger.info("Carro com ID {} cadastrado com sucesso.", savedCarro.getId());
            return savedCarro;

        } catch (DataIntegrityViolationException e) {
            logger.error("Erro ao salvar carro: {}", e.getMessage());
            throw new DatabaseException("Erro ao salvar o carro: violação de integridade.");
        }
    }

    /**
     * Atualiza um carro existente.
     *
     * - Busca o carro
     * - Atualiza campos permitidos
     * - Salva no banco
     * - Registra logs
     */
    @Transactional
    public Carro update(Long id, @Valid Carro obj) {
        try {
            return repository.findById(id)
                    .map(entity -> {

                        // Atualiza campos editáveis
                        entity.setMarca(obj.getMarca());
                        entity.setModelo(obj.getModelo());
                        entity.setMatricula(obj.getMatricula());
                        entity.setKilometragem(obj.getKilometragem());

                        Carro updatedCarro = repository.save(entity);

                        logger.info("Carro com ID {} atualizado com sucesso.", id);
                        return updatedCarro;

                    })
                    .orElseThrow(() -> new ResourceNotFoundException(id));

        } catch (DataIntegrityViolationException e) {
            logger.error("Erro ao atualizar carro ID {}: {}", id, e.getMessage());
            throw new DatabaseException("Erro ao atualizar o carro: violação de integridade.");
        }
    }

    /**
     * Remove um carro definitivamente do banco.
     *
     * - Se o ID não existir → ResourceNotFoundException
     * - Se houver dependências → DatabaseException
     */
    @Transactional
    public void delete(Long id) {
        try {
            repository.deleteById(id);
            cache.remove(id);

            logger.info("Carro com ID {} deletado com sucesso.", id);

        } catch (EmptyResultDataAccessException e) {
            logger.warn("Tentativa de deletar carro inexistente: ID {}", id);
            throw new ResourceNotFoundException(id);

        } catch (DataIntegrityViolationException e) {
            logger.error("Erro ao excluir carro ID {}: {}", id, e.getMessage());
            throw new DatabaseException("Não foi possível eliminar o carro. Verifique dependências.");
        }
    }

    /**
     * Soft delete:
     *
     * - Marca o carro como inativo
     * - Reatribui despesas e cartões para um carro padrão (ID 1)
     * - Evita problemas de integridade referencial
     * - Suporta concorrência otimista
     */
    @Transactional
    public void softDeleteCarro(Long id) {
        try {
            Carro carro = repository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException(id));

            // Marca como inativo
            carro.setAtivo(false);

            // Carro padrão para reatribuição
            Carro carroPadrao = repository.findById(1L)
                    .orElseThrow(() -> new ResourceNotFoundException(1L));

            // Reatribui despesas
            for (Despesa despesa : carro.getDespesas()) {
                despesa.setCarro(carroPadrao);
            }

            // Reatribui cartões
            for (Cartao cartao : carro.getCartoes()) {
                cartao.setCarro(carroPadrao);
            }

            repository.save(carro);
            cache.remove(id);

            logger.info("Soft delete aplicado ao carro ID {}.", id);

        } catch (OptimisticLockException e) {
            logger.warn("Concorrência detectada ao remover carro ID {}. Tentando novamente...", id);
            softDeleteCarro(id); // Retry automático
        }
    }
}

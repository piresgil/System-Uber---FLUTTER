package application.services;

import application.dto.DespesaDTO;
import application.model.Carro;
import application.model.Cartao;
import application.model.Colaborador;
import application.model.Despesa;
import application.repositories.CarroRepository;
import application.repositories.CartaoRepository;
import application.repositories.ColaboradorRepository;
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

/**
 * Serviço responsável pela lógica de negócio da entidade Despesa.
 * Atualizado para suportar operações baseadas em DTO (incluindo o armazenamento da faturaUrl)
 * e manter a consistência de cache e base de dados.
 * 
 * @author Daniel Gil (adaptado)
 */
@Service
public class DespesaService {

    @Autowired
    private DespesaRepository repository;
    
    @Autowired
    private CartaoRepository cartaoRepository;

    @Autowired
    private CarroRepository carroRepository;

    @Autowired
    private ColaboradorRepository colaboradorRepository;

    // Logger para registrar eventos e facilitar o rastreamento de erros.
    private static final Logger logger = LoggerFactory.getLogger(DespesaService.class);
    private final Map<Long, Despesa> cache = new HashMap<>();

    public void limparCache() {
        cache.clear();
    }

    /**
     * Construtor com injeção de dependências.
     */
    @Autowired
    public DespesaService(DespesaRepository repository) {
        this.repository = repository;
    }

    /**
     * Retorna todas as despesas cadastradas.
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
     */
    @Transactional(readOnly = true)
    public Despesa findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Despesa com ID {} não encontrada!", id);
                    return new ResourceNotFoundException(id);
                });
    }

    /**
     * Insere uma nova despesa no banco de dados usando a entidade direta.
     */
    @Transactional
    public Despesa insert(@Valid Despesa obj) {
        try {
            if (obj.getCartao() != null && obj.getCartao().getId() != null) {
                Cartao cartao = cartaoRepository.findById(obj.getCartao().getId())
                        .orElseThrow(() -> new DatabaseException("Cartão não encontrado com o ID: " + obj.getCartao().getId()));
                obj.setCartao(cartao);
            }

            Despesa savedDespesa = repository.save(obj);
            cache.put(savedDespesa.getId(), savedDespesa);
            logger.info("Despesa com ID {} cadastrada com sucesso.", savedDespesa.getId());
            return savedDespesa;
        } catch (DataIntegrityViolationException e) {
            logger.error("Erro ao salvar Despesa: {}", e.getMessage());
            throw new DatabaseException("Erro ao salvar a Despesa: violação de integridade.");
        }
    }

    /**
     * Insere uma nova despesa a partir de um DespesaDTO (veio do Flutter),
     * resolvendo as relações por ID e salvando também a faturaUrl.
     */
    @Transactional
    public Despesa insertFromDTO(DespesaDTO dto) {
        try {
            Despesa despesa = new Despesa();
            despesa.setNome(dto.getNome());
            despesa.setDescricao(dto.getDescricao());
            despesa.setData(dto.getData());
            despesa.setValor(dto.getValor());
            despesa.setQuantidade(dto.getQuantidade());
            despesa.setUnidade(dto.getUnidade());
            despesa.setFaturaUrl(dto.getFaturaUrl()); // Atribui o URL da fatura/recibo

            // Resolve e valida o Cartão
            if (dto.getCartaoId() != null) {
                Cartao cartao = cartaoRepository.findById(dto.getCartaoId())
                        .orElseThrow(() -> new DatabaseException("Cartão não encontrado com o ID: " + dto.getCartaoId()));
                despesa.setCartao(cartao);
            }

            // Resolve e valida o Carro
            if (dto.getCarroId() != null) {
                Carro carro = carroRepository.findById(dto.getCarroId())
                        .orElseThrow(() -> new DatabaseException("Carro não encontrado com o ID: " + dto.getCarroId()));
                despesa.setCarro(carro);
            }

            // Resolve o Motorista (opcional)
            if (dto.getMotoristaId() != null) {
                Colaborador motorista = colaboradorRepository.findById(dto.getMotoristaId())
                        .orElseThrow(() -> new DatabaseException("Motorista não encontrado com o ID: " + dto.getMotoristaId()));
                despesa.setMotorista(motorista);
            }

            Despesa savedDespesa = repository.save(despesa);
            cache.put(savedDespesa.getId(), savedDespesa);
            logger.info("Despesa DTO com ID {} cadastrada com sucesso.", savedDespesa.getId());
            return savedDespesa;
        } catch (DataIntegrityViolationException e) {
            logger.error("Erro ao salvar Despesa via DTO: {}", e.getMessage());
            throw new DatabaseException("Erro ao salvar a Despesa: violação de integridade.");
        }
    }

    /**
     * Atualiza uma despesa existente a partir de um DespesaDTO.
     */
    @Transactional
    public Despesa updateFromDTO(Long id, DespesaDTO dto) {
        try {
            return repository.findById(id)
                    .map(entity -> {
                        entity.setNome(dto.getNome());
                        entity.setDescricao(dto.getDescricao());
                        entity.setData(dto.getData());
                        entity.setValor(dto.getValor());
                        entity.setQuantidade(dto.getQuantidade());
                        entity.setUnidade(dto.getUnidade());
                        entity.setFaturaUrl(dto.getFaturaUrl()); // Atualiza o URL da fatura/recibo

                        // Atualiza Cartão se fornecido
                        if (dto.getCartaoId() != null) {
                            Cartao cartao = cartaoRepository.findById(dto.getCartaoId())
                                    .orElseThrow(() -> new DatabaseException("Cartão não encontrado com o ID: " + dto.getCartaoId()));
                            entity.setCartao(cartao);
                        }

                        // Atualiza Carro se fornecido
                        if (dto.getCarroId() != null) {
                            Carro carro = carroRepository.findById(dto.getCarroId())
                                    .orElseThrow(() -> new DatabaseException("Carro não encontrado com o ID: " + dto.getCarroId()));
                            entity.setCarro(carro);
                        }

                        // Atualiza Motorista se fornecido
                        if (dto.getMotoristaId() != null) {
                            Colaborador motorista = colaboradorRepository.findById(dto.getMotoristaId())
                                    .orElseThrow(() -> new DatabaseException("Motorista não encontrado com o ID: " + dto.getMotoristaId()));
                            entity.setMotorista(motorista);
                        }

                        Despesa updatedDespesa = repository.save(entity);
                        cache.put(id, updatedDespesa);
                        logger.info("Despesa com ID {} atualizada com sucesso via DTO.", id);
                        return updatedDespesa;
                    })
                    .orElseThrow(() -> new ResourceNotFoundException(id));
        } catch (DataIntegrityViolationException e) {
            logger.error("Erro ao atualizar despesa ID {}: {}", id, e.getMessage());
            throw new DatabaseException("Erro ao atualizar a despesa: violação de integridade.");
        }
    }

    /**
     * Atualiza apenas o URL da fatura de uma despesa específica.
     */
    @Transactional
    public Despesa updateFaturaUrl(Long id, String faturaUrl) {
        try {
            Despesa despesa = findById(id);
            despesa.setFaturaUrl(faturaUrl);
            
            Despesa updatedDespesa = repository.save(despesa);
            cache.put(id, updatedDespesa); // Atualiza também o cache
            logger.info("URL da fatura da despesa ID {} atualizado com sucesso.", id);
            return updatedDespesa;
        } catch (DataIntegrityViolationException e) {
            logger.error("Erro ao atualizar fatura da despesa ID {}: {}", id, e.getMessage());
            throw new DatabaseException("Erro ao atualizar a fatura da despesa.");
        }
    }

    /**
     * Atualiza uma despesa existente com base na entidade direta.
     */
    @Transactional
    public Despesa update(Long id, @Valid Despesa obj) {
        try {
            return repository.findById(id)
                    .map(entity -> {
                        entity.setCartao(obj.getCartao());
                        entity.setNome(obj.getNome());
                        entity.setDescricao(obj.getDescricao());
                        entity.setCarro(obj.getCarro());
                        entity.setMotorista(obj.getMotorista());
                        entity.setData(obj.getData());
                        entity.setValor(obj.getValor());
                        entity.setFaturaUrl(obj.getFaturaUrl());

                        Despesa updatedDespesa = repository.save(entity);
                        cache.put(id, updatedDespesa);
                        logger.info("Despesa com ID {} atualizada com sucesso.", id);
                        return updatedDespesa;
                    })
                    .orElseThrow(() -> new ResourceNotFoundException(id));
        } catch (DataIntegrityViolationException e) {
            logger.error("Erro ao atualizar despesa ID {}: {}", id, e.getMessage());
            throw new DatabaseException("Erro ao atualizar a despesa: violação de integridade.");
        }
    }

    /**
     * Deleta uma despesa com base no ID.
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
            logger.error("Erro ao deletar despesa com ID {}: violação de integridade.", id);
            throw new DatabaseException(e.getMessage());
        }
    }
}
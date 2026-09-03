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
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Valid;
import java.util.List;

/**
 * Serviço responsável pela lógica de negócio da entidade Despesa.
 * 
 * @author Daniel Gil (adaptado)
 */
@Service
public class DespesaService {

    private final DespesaRepository repository;
    private final CartaoRepository cartaoRepository;
    private final CarroRepository carroRepository;
    private final ColaboradorRepository colaboradorRepository;

    private static final Logger logger = LoggerFactory.getLogger(DespesaService.class);

    @Autowired
    public DespesaService(DespesaRepository repository, 
                          CartaoRepository cartaoRepository, 
                          CarroRepository carroRepository, 
                          ColaboradorRepository colaboradorRepository) {
        this.repository = repository;
        this.cartaoRepository = cartaoRepository;
        this.carroRepository = carroRepository;
        this.colaboradorRepository = colaboradorRepository;
    }

    @Transactional(readOnly = true)
    public List<Despesa> findAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Despesa findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Despesa com ID {} não encontrada!", id);
                    return new ResourceNotFoundException(id);
                });
    }

    @Transactional
    public Despesa insert(@Valid Despesa obj) {
        try {
            if (obj.getCartao() != null && obj.getCartao().getId() != null) {
                Cartao cartao = cartaoRepository.findById(obj.getCartao().getId())
                        .orElseThrow(() -> new DatabaseException("Cartão não encontrado com o ID: " + obj.getCartao().getId()));
                obj.setCartao(cartao);
            }

            Despesa savedDespesa = repository.save(obj);
            logger.info("Despesa com ID {} cadastrada com sucesso.", savedDespesa.getId());
            return savedDespesa;
        } catch (DataIntegrityViolationException e) {
            logger.error("Erro ao salvar Despesa: {}", e.getMessage());
            throw new DatabaseException("Erro ao salvar a Despesa: violação de integridade.");
        }
    }

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
            despesa.setFaturaUrl(dto.getFaturaUrl());

            if (dto.getCartaoId() != null) {
                Cartao cartao = cartaoRepository.findById(dto.getCartaoId())
                        .orElseThrow(() -> new DatabaseException("Cartão não encontrado com o ID: " + dto.getCartaoId()));
                despesa.setCartao(cartao);
            }

            if (dto.getCarroId() != null) {
                Carro carro = carroRepository.findById(dto.getCarroId())
                        .orElseThrow(() -> new DatabaseException("Carro não encontrado com o ID: " + dto.getCarroId()));
                despesa.setCarro(carro);
            }

            if (dto.getMotoristaId() != null) {
                Colaborador motorista = colaboradorRepository.findById(dto.getMotoristaId())
                        .orElseThrow(() -> new DatabaseException("Motorista não encontrado com o ID: " + dto.getMotoristaId()));
                despesa.setMotorista(motorista);
            }

            Despesa savedDespesa = repository.save(despesa);
            logger.info("Despesa DTO com ID {} cadastrada com sucesso.", savedDespesa.getId());
            return savedDespesa;
        } catch (DataIntegrityViolationException e) {
            logger.error("Erro ao salvar Despesa via DTO: {}", e.getMessage());
            throw new DatabaseException("Erro ao salvar a Despesa: violação de integridade.");
        }
    }

    @Transactional
    public Despesa updateFromDTO(Long id, DespesaDTO dto) {
        try {
            Despesa entity = repository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException(id));

            entity.setNome(dto.getNome());
            entity.setDescricao(dto.getDescricao());
            entity.setData(dto.getData());
            entity.setValor(dto.getValor());
            entity.setQuantidade(dto.getQuantidade());
            entity.setUnidade(dto.getUnidade());
            entity.setFaturaUrl(dto.getFaturaUrl());

            // Relações: se vier ID atualiza, se vier null limpa ou mantém conforme a regra de negócio
            if (dto.getCartaoId() != null) {
                Cartao cartao = cartaoRepository.findById(dto.getCartaoId())
                        .orElseThrow(() -> new DatabaseException("Cartão não encontrado com o ID: " + dto.getCartaoId()));
                entity.setCartao(cartao);
            } else {
                entity.setCartao(null);
            }

            if (dto.getCarroId() != null) {
                Carro carro = carroRepository.findById(dto.getCarroId())
                        .orElseThrow(() -> new DatabaseException("Carro não encontrado com o ID: " + dto.getCarroId()));
                entity.setCarro(carro);
            } else {
                entity.setCarro(null);
            }

            if (dto.getMotoristaId() != null) {
                Colaborador motorista = colaboradorRepository.findById(dto.getMotoristaId())
                        .orElseThrow(() -> new DatabaseException("Motorista não encontrado com o ID: " + dto.getMotoristaId()));
                entity.setMotorista(motorista);
            } else {
                entity.setMotorista(null);
            }

            Despesa updatedDespesa = repository.save(entity);
            logger.info("Despesa com ID {} atualizada com sucesso via DTO.", id);
            return updatedDespesa;
        } catch (DataIntegrityViolationException e) {
            logger.error("Erro ao atualizar despesa ID {}: {}", id, e.getMessage());
            throw new DatabaseException("Erro ao atualizar a despesa: violação de integridade.");
        }
    }

    @Transactional
    public Despesa updateFaturaUrl(Long id, String faturaUrl) {
        try {
            Despesa despesa = findById(id);
            despesa.setFaturaUrl(faturaUrl);
            
            Despesa updatedDespesa = repository.save(despesa);
            logger.info("URL da fatura da despesa ID {} atualizado com sucesso.", id);
            return updatedDespesa;
        } catch (DataIntegrityViolationException e) {
            logger.error("Erro ao atualizar fatura da despesa ID {}: {}", id, e.getMessage());
            throw new DatabaseException("Erro ao atualizar a fatura da despesa.");
        }
    }

    @Transactional
    public Despesa update(Long id, @Valid Despesa obj) {
        try {
            Despesa entity = repository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException(id));

            entity.setCartao(obj.getCartao());
            entity.setNome(obj.getNome());
            entity.setDescricao(obj.getDescricao());
            entity.setCarro(obj.getCarro());
            entity.setMotorista(obj.getMotorista());
            entity.setData(obj.getData());
            entity.setValor(obj.getValor());
            entity.setFaturaUrl(obj.getFaturaUrl());

            Despesa updatedDespesa = repository.save(entity);
            logger.info("Despesa com ID {} atualizada com sucesso.", id);
            return updatedDespesa;
        } catch (DataIntegrityViolationException e) {
            logger.error("Erro ao atualizar despesa ID {}: {}", id, e.getMessage());
            throw new DatabaseException("Erro ao atualizar a despesa: violação de integridade.");
        }
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException(id);
        }
        try {
            repository.deleteById(id);
            logger.info("Despesa com ID {} eliminada com sucesso.", id);
        } catch (DataIntegrityViolationException e) {
            logger.error("Erro ao deletar despesa com ID {}: violação de integridade.", id);
            throw new DatabaseException("Não é possível eliminar esta despesa pois está associada a outros registos.");
        }
    }
}
package application.services;

import application.dto.CarroDTO;
import application.dto.DespesaDTO;
import application.model.Carro;
import application.repositories.CarroRepository;
import application.services.exceptions.DatabaseException;
import application.services.exceptions.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CarroService {

    private final CarroRepository repository;
    private static final Logger logger = LoggerFactory.getLogger(CarroService.class);

    @Autowired
    public CarroService(CarroRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<CarroDTO> findAll() {
        return repository.findAll().stream()
                .map(this.toDTOConverter())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CarroDTO findById(Long id) {
        Carro carro = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id));
        return toDTOConverter().apply(carro);
    }

    @Transactional
    public CarroDTO insert(CarroDTO dto) {
        try {
            Carro carro = new Carro();
            carro.setMarca(dto.getMarca());
            carro.setModelo(dto.getModelo());
            carro.setMatricula(dto.getMatricula());
            carro.setAlugado(dto.isAlugado());
            carro.setKilometragem(dto.getKilometragem());
            carro.setAtivo(true);
            carro.setDocumentoUrl(dto.getDocumentoUrl());
            carro.setSeguroUrl(dto.getSeguroUrl());
            carro.setInspecaoUrl(dto.getInspecaoUrl());

            Carro saved = repository.save(carro);
            logger.info("Carro com ID {} criado com sucesso.", saved.getId());
            return toDTOConverter().apply(saved);
        } catch (DataIntegrityViolationException e) {
            logger.error("Erro ao criar carro: {}", e.getMessage());
            throw new DatabaseException("Erro ao criar o carro: violação de integridade (matrícula duplicada?).");
        }
    }

    @Transactional
    public CarroDTO update(Long id, CarroDTO dto) {
        try {
            Carro carro = repository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException(id));

            carro.setMarca(dto.getMarca());
            carro.setModelo(dto.getModelo());
            carro.setMatricula(dto.getMatricula());
            carro.setAlugado(dto.isAlugado());
            carro.setKilometragem(dto.getKilometragem());
            carro.setDocumentoUrl(dto.getDocumentoUrl());
            carro.setSeguroUrl(dto.getSeguroUrl());
            carro.setInspecaoUrl(dto.getInspecaoUrl());

            Carro updated = repository.save(carro);
            logger.info("Carro com ID {} atualizado com sucesso.", id);
            return toDTOConverter().apply(updated);
        } catch (DataIntegrityViolationException e) {
            logger.error("Erro ao atualizar carro ID {}: {}", id, e.getMessage());
            throw new DatabaseException("Erro ao atualizar o carro.");
        }
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException(id);
        }
        try {
            repository.deleteById(id);
            logger.info("Carro com ID {} eliminado com sucesso.", id);
        } catch (DataIntegrityViolationException e) {
            logger.error("Erro ao eliminar carro ID {}: violação de integridade.", id);
            throw new DatabaseException("Não é possível eliminar este carro pois está associado a outros registos.");
        }
    }

    private java.util.function.Function<Carro, CarroDTO> toDTOConverter() {
        return carro -> {
            CarroDTO dto = new CarroDTO();
            dto.setId(carro.getId());
            dto.setMarca(carro.getMarca());
            dto.setModelo(carro.getModelo());
            dto.setMatricula(carro.getMatricula());
            dto.setAlugado(carro.isAlugado());
            dto.setKilometragem(carro.getKilometragem());
            dto.setAtivo(carro.isAtivo());
            dto.setDocumentoUrl(carro.getDocumentoUrl());
            dto.setSeguroUrl(carro.getSeguroUrl());
            dto.setInspecaoUrl(carro.getInspecaoUrl());

            if (carro.getDespesas() != null) {
                List<DespesaDTO> despesaDTOs = carro.getDespesas().stream().map(despesa -> {
                    DespesaDTO dDto = new DespesaDTO();
                    dDto.setId(despesa.getId());
                    dDto.setNome(despesa.getNome());
                    dDto.setDescricao(despesa.getDescricao());
                    dDto.setData(despesa.getData());
                    dDto.setValor(despesa.getValor());
                    dDto.setQuantidade(despesa.getQuantidade());
                    dDto.setUnidade(despesa.getUnidade());
                    dDto.setFaturaUrl(despesa.getFaturaUrl());
                    return dDto;
                }).collect(Collectors.toList());
                dto.setDespesas(despesaDTOs);
            }

            return dto;
        };
    }
}
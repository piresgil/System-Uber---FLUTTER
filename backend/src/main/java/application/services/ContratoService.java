package application.services;

import application.model.Contrato;
import application.repositories.ContratoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Serviço responsável pela lógica de negócio da entidade Contrato.
 * Fornece operações CRUD utilizando o ContratoRepository.
 */
@Service
public class ContratoService {

    @Autowired
    private ContratoRepository repository;

    /**
     * Retorna todos os contratos.
     */
    public List<Contrato> findAll() {
        return repository.findAll();
    }

    /**
     * Busca um contrato pelo ID.
     */
    public Contrato findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contrato não encontrado: " + id));
    }

    /**
     * Insere um novo contrato.
     */
    public Contrato insert(Contrato obj) {
        obj.setId(null); // garante criação
        return repository.save(obj);
    }

    /**
     * Atualiza um contrato existente.
     */
    public Contrato update(Long id, Contrato obj) {
        Contrato entity = findById(id);
        updateData(entity, obj);
        return repository.save(entity);
    }

    /**
     * Remove um contrato pelo ID.
     */
    public void delete(Long id) {
        findById(id);
        repository.deleteById(id);
    }

    /**
     * Atualiza apenas os campos permitidos.
     */
    private void updateData(Contrato entity, Contrato obj) {
        entity.setNumero(obj.getNumero());
        entity.setDescricao(obj.getDescricao());
    }
}

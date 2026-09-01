package application.controllers;

import application.dto.ContratoDTO;
import application.mappers.ContratoMapper;
import application.model.Contrato;
import application.services.ContratoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador REST responsável por gerir operações relacionadas à entidade Contrato.
 *
 * Este controlador expõe endpoints HTTP para:
 * - Listar contratos
 * - Buscar contrato por ID
 * - Criar novo contrato
 * - Atualizar contrato existente
 * - Remover contrato
 *
 * Utiliza DTOs para evitar expor diretamente a entidade JPA.
 */
@RestController
@RequestMapping("/contratos")
public class ContratoRestController {

    /**
     * Serviço responsável pela lógica de negócio da entidade Contrato.
     */
    @Autowired
    private ContratoService service;

    /**
     * Retorna todos os contratos registados.
     *
     * @return Lista de ContratoDTO
     */
    @GetMapping
    public ResponseEntity<List<ContratoDTO>> findAll() {
        List<ContratoDTO> list = service.findAll()
                .stream()
                .map(ContratoMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    /**
     * Busca um contrato pelo ID.
     *
     * @param id Identificador do contrato
     * @return ContratoDTO correspondente
     */
    @GetMapping("/{id}")
    public ResponseEntity<ContratoDTO> findById(@PathVariable Long id) {
        Contrato obj = service.findById(id);
        return ResponseEntity.ok(ContratoMapper.toDTO(obj));
    }

    /**
     * Regista um novo contrato.
     *
     * @param dto Dados do contrato enviados pelo cliente
     * @return ContratoDTO criado com URI do novo recurso
     */
    @PostMapping
    public ResponseEntity<ContratoDTO> insert(@RequestBody ContratoDTO dto) {
        Contrato entity = ContratoMapper.toEntity(dto);
        entity = service.insert(entity);

        URI uri = URI.create("/contratos/" + entity.getId());
        return ResponseEntity.created(uri).body(ContratoMapper.toDTO(entity));
    }

    /**
     * Atualiza um contrato existente.
     *
     * @param id  Identificador do contrato a atualizar
     * @param dto Dados atualizados
     * @return ContratoDTO atualizado
     */
    @PutMapping("/{id}")
    public ResponseEntity<ContratoDTO> update(@PathVariable Long id, @RequestBody ContratoDTO dto) {
        Contrato entity = ContratoMapper.toEntity(dto);
        entity = service.update(id, entity);
        return ResponseEntity.ok(ContratoMapper.toDTO(entity));
    }

    /**
     * Remove um contrato pelo ID.
     *
     * @param id Identificador do contrato
     * @return Resposta sem conteúdo (204)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

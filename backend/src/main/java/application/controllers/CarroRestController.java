package application.controllers;

import application.dto.CarroDTO;
import application.mappers.CarroMapper;
import application.model.Carro;
import application.services.CarroService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador REST responsável pela gestão de Carros.
 *
 * Funcionalidades:
 * - Listar carros
 * - Buscar por ID
 * - Criar novo carro
 * - Atualizar carro existente
 * - Remover carro
 *
 * Utiliza DTOs para evitar exposição direta da entidade.
 */
@RestController
@RequestMapping("/carros")
@RequiredArgsConstructor // Injeta CarroService automaticamente via construtor
public class CarroRestController {

    /**
     * Serviço responsável pela lógica de negócio dos carros.
     * Declarado como final para uso com @RequiredArgsConstructor.
     */
    private final CarroService service;

    /**
     * Retorna todos os carros ativos.
     *
     * @return Lista de CarroDTO
     */
    @GetMapping
    public ResponseEntity<List<CarroDTO>> findAll() {

        // Converte entidades → DTOs
        List<CarroDTO> list = service.findAll()
                .stream()
                .map(CarroMapper::toDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(list);
    }

    /**
     * Busca um carro pelo ID.
     *
     * @param id ID do carro
     * @return CarroDTO correspondente
     */
    @GetMapping("/{id}")
    public ResponseEntity<CarroDTO> findById(@PathVariable Long id) {

        Carro obj = service.findById(id);
        return ResponseEntity.ok(CarroMapper.toDTO(obj));
    }

    /**
     * Regista um novo carro.
     *
     * @param dto Dados do carro
     * @return CarroDTO criado
     */
    @PostMapping
    public ResponseEntity<CarroDTO> insert(@RequestBody CarroDTO dto) {

        Carro entity = CarroMapper.toEntity(dto);
        entity = service.insert(entity);

        // Cria URI do novo recurso
        URI uri = URI.create("/carros/" + entity.getId());

        return ResponseEntity.created(uri).body(CarroMapper.toDTO(entity));
    }

    /**
     * Atualiza um carro existente.
     *
     * @param id  ID do carro
     * @param dto Dados atualizados
     * @return CarroDTO atualizado
     */
    @PutMapping("/{id}")
    public ResponseEntity<CarroDTO> update(@PathVariable Long id, @RequestBody CarroDTO dto) {

        Carro entity = CarroMapper.toEntity(dto);
        entity = service.update(id, entity);

        return ResponseEntity.ok(CarroMapper.toDTO(entity));
    }

    /**
     * Remove um carro definitivamente.
     *
     * @param id ID do carro
     * @return 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {

        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

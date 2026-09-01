package application.controllers;

import application.dto.PagamentoDTO;
import application.mappers.PagamentoMapper;
import application.model.Pagamento;
import application.services.PagamentoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/pagamentos")
public class PagamentoRestController {

    @Autowired
    private PagamentoService service;

    /**
     * Retorna todos os pagamentos.
     */
    @GetMapping
    public ResponseEntity<List<PagamentoDTO>> findAll() {
        List<PagamentoDTO> list = service.findAll()
                .stream()
                .map(PagamentoMapper::toDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(list);
    }

    /**
     * Retorna um pagamento pelo ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<PagamentoDTO> findById(@PathVariable Long id) {
        Pagamento obj = service.findById(id);
        return ResponseEntity.ok(PagamentoMapper.toDTO(obj));
    }

    /**
     * Regista um novo pagamento.
     */
    @PostMapping
    public ResponseEntity<PagamentoDTO> insert(@Valid @RequestBody PagamentoDTO dto) {

        Pagamento entity = PagamentoMapper.toEntity(dto);
        entity = service.insert(entity);

        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(entity.getId())
                .toUri();

        return ResponseEntity.created(uri).body(PagamentoMapper.toDTO(entity));
    }

    /**
     * Atualiza um pagamento existente.
     */
    @PutMapping("/{id}")
    public ResponseEntity<PagamentoDTO> update(@PathVariable Long id,
                                               @Valid @RequestBody PagamentoDTO dto) {

        Pagamento entity = PagamentoMapper.toEntity(dto);
        entity = service.update(id, entity);

        return ResponseEntity.ok(PagamentoMapper.toDTO(entity));
    }

    /**
     * Remove um pagamento pelo ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

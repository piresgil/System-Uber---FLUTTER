package application.controllers;

import application.dto.CartaoDTO;
import application.mappers.CartaoMapper;
import application.model.Cartao;
import application.model.Carro;
import application.services.CartaoService;
import application.services.CarroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

/**
 * No insert/update:
 * - O Mapper cria um Carro apenas com ID
 * - O Controller carrega o Carro real via CarroService
 */

@RestController
@RequestMapping("/cartoes")
public class CartaoRestController {

    @Autowired
    private CartaoService service;

    @Autowired
    private CarroService carroService;

    @GetMapping
    public ResponseEntity<List<CartaoDTO>> findAll() {
        List<CartaoDTO> list = service.findAll()
                .stream()
                .map(CartaoMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CartaoDTO> findById(@PathVariable Long id) {
        Cartao obj = service.findById(id);
        return ResponseEntity.ok(CartaoMapper.toDTO(obj));
    }

    @PostMapping
    public ResponseEntity<CartaoDTO> insert(@RequestBody CartaoDTO dto) {
        Cartao entity = CartaoMapper.toEntity(dto);

        if (dto.getCarroId() != null) {
            Carro carro = carroService.findById(dto.getCarroId());
            entity.setCarro(carro);
        }

        entity = service.insert(entity);
        URI uri = URI.create("/cartoes/" + entity.getId());
        return ResponseEntity.created(uri).body(CartaoMapper.toDTO(entity));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CartaoDTO> update(@PathVariable Long id, @RequestBody CartaoDTO dto) {
        Cartao entity = CartaoMapper.toEntity(dto);

        if (dto.getCarroId() != null) {
            Carro carro = carroService.findById(dto.getCarroId());
            entity.setCarro(carro);
        }

        entity = service.update(id, entity);
        return ResponseEntity.ok(CartaoMapper.toDTO(entity));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

package application.controllers;

import application.dto.CarroDTO;
import application.services.CarroService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/carros")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class CarroRestController {

    private final CarroService service;

    @GetMapping
    public ResponseEntity<List<CarroDTO>> findAll() {
        List<CarroDTO> list = service.findAll();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CarroDTO> findById(@PathVariable Long id) {
        CarroDTO dto = service.findById(id);
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    public ResponseEntity<?> insert(@RequestBody CarroDTO dto) {
        try {
            CarroDTO newDto = service.insert(dto);
            URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(newDto.getId())
                    .toUri();
            return ResponseEntity.created(uri).body(newDto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao criar carro: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody CarroDTO dto) {
        try {
            CarroDTO updatedDto = service.update(id, dto);
            return ResponseEntity.ok(updatedDto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao atualizar carro: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            service.delete(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao eliminar carro: " + e.getMessage());
        }
    }
}
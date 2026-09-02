package application.controllers;

import application.model.Carro;
import application.services.CarroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/carros")
@CrossOrigin(origins = "*") // Permite que o Flutter (Web/Mobile) aceda a estes endpoints sem bloqueios CORS
public class CarroController {

    private final CarroService carroService;

    @Autowired
    public CarroController(CarroService carroService) {
        this.carroService = carroService;
    }

    // 1. Listar todos os carros (GET: /api/carros)
    @GetMapping
    public List<Carro> listarTodos() {
        return carroService.findAll();
    }

    // 2. Buscar carro por ID (GET: /api/carros/{id})
    @GetMapping("/{id}")
    public ResponseEntity<Carro> buscarPorId(@PathVariable Long id) {
        Carro carro = carroService.findById(id);
        return Optional.ofNullable(carro)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 3. Registar/Criar novo carro (POST: /api/carros)
    @PostMapping
    public ResponseEntity<Carro> criarCarro(@RequestBody Carro carro) {
        try {
            Carro novoCarro = carroService.insert(carro);
            return ResponseEntity.ok(novoCarro);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // 4. Atualizar carro existente (PUT: /api/carros/{id})
    @PutMapping("/{id}")
    public ResponseEntity<Carro> atualizarCarro(@PathVariable Long id, @RequestBody Carro carroDetails) {
        try {
            carroService.update(id, carroDetails);
            return ResponseEntity.ok(carroDetails);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // 5. Eliminar carro (DELETE: /api/carros/{id})
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCarro(@PathVariable Long id) {
        try {
            carroService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
package application.controllers;

import application.model.Colaborador;
import application.services.ColaboradorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * Controlador REST responsável pela gestão de Colaboradores.
 * Substitui o antigo ColaboradorController de JavaFX para servir a API REST ao Flutter.
 */
@RestController
@RequestMapping("/colaboradores")
@CrossOrigin(origins = "*") // Permite o acesso a partir do Flutter Web sem bloqueios CORS
@RequiredArgsConstructor
public class ColaboradorRestController {

    private final ColaboradorService colaboradorService;

    /**
     * Retorna todos os colaboradores registados.
     */
    @GetMapping
    public ResponseEntity<List<Colaborador>> findAll() {
        List<Colaborador> lista = colaboradorService.findAll();
        return ResponseEntity.ok(lista);
    }

    /**
     * Busca um colaborador pelo ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Colaborador> findById(@PathVariable Long id) {
        Colaborador colaborador = colaboradorService.findById(id);
        return ResponseEntity.ok(colaborador);
    }

    /**
     * Regista um novo colaborador, validando se o e-mail já existe.
     */
    @PostMapping
    public ResponseEntity<?> insert(@RequestBody Colaborador colaborador) {
        if (colaboradorService.existsByEmail(colaborador.getEmail())) {
            return ResponseEntity.badRequest().body("Este e-mail já está em uso.");
        }

        try {
            Colaborador salvo = colaboradorService.insert(colaborador);
            URI uri = URI.create("/colaboradores/" + salvo.getId());
            return ResponseEntity.created(uri).body(salvo);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Atualiza um colaborador existente.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Colaborador colaboradorDetails) {
        try {
            Colaborador atualizado = colaboradorService.update(id, colaboradorDetails);
            return ResponseEntity.ok(atualizado);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Remove um colaborador.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        colaboradorService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
package application.controllers;

import application.model.Pagamento;
import application.services.PagamentoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * Controlador REST responsável pela gestão de Pagamentos.
 * Substitui o antigo ListagemPagamentosController de JavaFX para servir a API REST ao Flutter.
 */
@RestController
@RequestMapping("/pagamentos")
@CrossOrigin(origins = "*") // Permite o acesso a partir do Flutter Web sem bloqueios CORS
@RequiredArgsConstructor
public class PagamentoRestController {

    private final PagamentoService pagamentoService;

    /**
     * Retorna todos os pagamentos registados.
     */
    @GetMapping
    public ResponseEntity<List<Pagamento>> findAll() {
        List<Pagamento> lista = pagamentoService.findAll();
        return ResponseEntity.ok(lista);
    }

    /**
     * Busca um pagamento pelo ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Pagamento> findById(@PathVariable Long id) {
        Pagamento pagamento = pagamentoService.findById(id);
        return ResponseEntity.ok(pagamento);
    }

    /**
     * Regista um novo pagamento.
     */
    @PostMapping
    public ResponseEntity<?> insert(@RequestBody Pagamento pagamento) {
        try {
            Pagamento salvo = pagamentoService.insert(pagamento);
            URI uri = URI.create("/pagamentos/" + salvo.getId());
            return ResponseEntity.created(uri).body(salvo);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Atualiza um pagamento existente.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Pagamento pagamentoDetails) {
        try {
            Pagamento atualizado = pagamentoService.update(id, pagamentoDetails);
            return ResponseEntity.ok(atualizado);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Remove um pagamento.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        pagamentoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
package application.controllers;

import application.model.User;
import application.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * Controlador REST responsável pela gestão de Utilizadores.
 * Substitui o antigo UserController de JavaFX para servir a API REST ao Flutter.
 */
@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "*") // Permite o acesso a partir do Flutter Web sem bloqueios CORS
@RequiredArgsConstructor
public class UserRestController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    /**
     * Retorna todos os utilizadores registados.
     */
    @GetMapping
    public ResponseEntity<List<User>> findAll() {
        List<User> lista = userService.findAll();
        return ResponseEntity.ok(lista);
    }

    /**
     * Busca um utilizador pelo ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<User> findById(@PathVariable Long id) {
        User user = userService.findById(id);
        return ResponseEntity.ok(user);
    }

    /**
     * Regista um novo utilizador (com encriptação de password).
     */
    @PostMapping
    public ResponseEntity<?> insert(@RequestBody User user) {
        try {
            // Valida se o email já existe
            if (userService.existsByEmail(user.getEmail())) {
                return ResponseEntity.badRequest().body("Este e-mail já está em uso.");
            }

            // Encripta a password antes de guardar
            user.setPassword(passwordEncoder.encode(user.getPassword()));

            User salvo = userService.insert(user);
            URI uri = URI.create("/users/" + salvo.getId());
            return ResponseEntity.created(uri).body(salvo);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Atualiza um utilizador existente.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody User userDetails) {
        try {
            // Se a password foi alterada (ou enviada de novo), convém encriptar, 
            // ou podes delegar a lógica de verificação para o service.
            if (userDetails.getPassword() != null && !userDetails.getPassword().isBlank()) {
                userDetails.setPassword(passwordEncoder.encode(userDetails.getPassword()));
            }

            User atualizado = userService.update(id, userDetails);
            return ResponseEntity.ok(atualizado);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Remove um utilizador.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}